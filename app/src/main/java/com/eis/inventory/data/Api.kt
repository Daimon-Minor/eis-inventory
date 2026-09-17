package com.eis.inventory.data

import com.eis.inventory.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class ApiException(val status: Int, message: String) : Exception(message)

object Http {
    val base: String = BuildConfig.API_BASE_URL.trimEnd('/')
    private val gson = Gson()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    @Volatile
    var token: String? = null

    private fun detail(text: String, code: Int): String {
        return try {
            val obj = Gson().fromJson(text, JsonObject::class.java)
            val d = obj?.get("detail")
            when {
                d == null || d.isJsonNull -> "Gagal (" + code + ")"
                d.isJsonPrimitive -> d.asString
                else -> d.toString()
            }
        } catch (e: Exception) {
            if (code == 401) "Sesi berakhir, silakan login ulang" else "Gagal (" + code + ")"
        }
    }

    private fun sync(method: String, path: String, body: Any?): String {
        val builder = Request.Builder().url(base + path)
        token?.let { builder.header("Authorization", "Bearer " + it) }
        val payload = if (body == null) null else gson.toJson(body).toRequestBody(jsonType)
        when (method) {
            "GET" -> builder.get()
            "POST" -> builder.post(payload ?: "{}".toRequestBody(jsonType))
            "PATCH" -> builder.patch(payload ?: "{}".toRequestBody(jsonType))
            "DELETE" -> builder.delete(payload)
        }
        client.newCall(builder.build()).execute().use { res ->
            val text = res.body?.string().orEmpty()
            if (!res.isSuccessful) throw ApiException(res.code, detail(text, res.code))
            return text
        }
    }

    suspend fun get(path: String): String = withContext(Dispatchers.IO) { sync("GET", path, null) }
    suspend fun post(path: String, body: Any?): String = withContext(Dispatchers.IO) { sync("POST", path, body) }
    suspend fun patch(path: String, body: Any?): String = withContext(Dispatchers.IO) { sync("PATCH", path, body) }
    suspend fun delete(path: String): String = withContext(Dispatchers.IO) { sync("DELETE", path, null) }

    fun <T> parse(text: String, cls: Class<T>): T = gson.fromJson(text, cls)
    fun raw(text: String): JsonElement = gson.fromJson(text, JsonElement::class.java)
    fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")
}

object Remote {

    suspend fun login(username: String, password: String): LoginRes =
        Http.parse(
            Http.post("/api/login", mapOf("username" to username, "password" to password)),
            LoginRes::class.java
        )

    suspend fun register(username: String, fullName: String, password: String): String =
        Http.post("/api/register", mapOf("username" to username, "full_name" to fullName, "password" to password))

    suspend fun items(query: String = "", category: String = "", status: String = ""): ItemsRes {
        val path = "/api/items?page=1&per_page=200&q=" + Http.enc(query) +
            "&category=" + Http.enc(category) + "&status=" + Http.enc(status)
        return Http.parse(Http.get(path), ItemsRes::class.java)
    }

    suspend fun categories(): List<Category> =
        Http.parse(Http.get("/api/categories"), Array<Category>::class.java).toList()

    suspend fun addCategory(name: String): String =
        Http.post("/api/categories", mapOf("name" to name))

    suspend fun renameCategory(id: Long, name: String): String =
        Http.patch("/api/categories/" + id, mapOf("name" to name))

    suspend fun deleteCategory(id: Long, target: String = "Umum"): String =
        Http.delete("/api/categories/" + id + "?reassign=1&target=" + Http.enc(target))

    suspend fun take(itemId: Long, qty: Double, note: String): String =
        Http.post("/api/take", mapOf("item_id" to itemId, "qty" to qty, "note" to note))

    suspend fun adjust(itemId: Long, type: String, qty: Double, note: String): String =
        Http.post("/api/items/" + itemId + "/stock", mapOf("type" to type, "qty" to qty, "note" to note))

    suspend fun createItem(body: Map<String, Any?>): String = Http.post("/api/items", body)

    suspend fun updateItem(id: Long, body: Map<String, Any?>): String = Http.patch("/api/items/" + id, body)

    suspend fun deleteItem(id: Long): String = Http.delete("/api/items/" + id)

    suspend fun transactions(limit: Int = 100): List<Tx> =
        Http.parse(Http.get("/api/transactions?limit=" + limit), Array<Tx>::class.java).toList()

    suspend fun myTransactions(): List<Tx> =
        Http.parse(Http.get("/api/my-transactions?limit=100"), Array<Tx>::class.java).toList()

    suspend fun users(): List<WebUser> =
        Http.parse(Http.get("/api/users"), Array<WebUser>::class.java).toList()

    suspend fun addUser(name: String, username: String, password: String, role: String): String =
        Http.post("/api/users", mapOf("name" to name, "username" to username,
            "full_name" to name, "password" to password, "role" to role))

    suspend fun deleteUser(id: Long): String = Http.delete("/api/users/" + id)

    suspend fun changePassword(oldPassword: String, newPassword: String): String =
        Http.post("/api/change-password", mapOf(
            "old_password" to oldPassword,
            "new_password" to newPassword
        ))

    /** Sidik jari data untuk auto refresh (stok masuk/keluar, barang, kategori, pengguna). */
    suspend fun pulse(): Pulse = Http.parse(Http.get("/api/pulse"), Pulse::class.java)
}
