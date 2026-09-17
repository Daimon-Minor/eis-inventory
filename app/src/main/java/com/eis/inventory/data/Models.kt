package com.eis.inventory.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val id: Long? = null,
    val name: String? = null,
    val username: String? = null,
    val role: String? = null
) {
    val display: String get() = if (!name.isNullOrBlank()) name else if (!username.isNullOrBlank()) username else "Pengguna"
    val isAdmin: Boolean get() = (role ?: "").equals("admin", true)
    val roleLabel: String get() = if (isAdmin) "Administrator" else "Teknisi"
}

data class LoginRes(val token: String? = null, val user: User? = null)

data class Item(
    val id: Long? = null,
    val name: String? = null,
    val category: String? = null,
    val sku: String? = null,
    val rack: String? = null,
    val unit: String? = null,
    val min_stock: Double? = null,
    val stok_awal: Double? = null,
    val stok: Double? = null,
    val masuk: Double? = null,
    val keluar: Double? = null,
    val qty: Double? = null,
    val last_move: Long? = null,
    val status: String? = null
)

data class Category(val id: Long? = null, val name: String? = null, val items: Int? = null)

data class ItemsRes(
    val items: List<Item>? = null,
    val total: Int? = null,
    val categories: List<Category>? = null,
    val racks: List<String>? = null
)

data class Tx(
    val id: Long? = null,
    val item_id: Long? = null,
    val item_name: String? = null,
    val type: String? = null,
    val qty: Double? = null,
    val unit: String? = null,
    val note: String? = null,
    val actor: String? = null,
    val actor_type: String? = null,
    val category: String? = null,
    val created_at: Long? = null
)

data class WebUser(
    val id: Long? = null,
    val name: String? = null,
    val username: String? = null,
    val full_name: String? = null,
    val role: String? = null,
    val created_at: Long? = null
) {
    val display: String get() = if (!full_name.isNullOrBlank()) full_name else if (!name.isNullOrBlank()) name else "(tanpa nama)"
    val isAdmin: Boolean get() = (role ?: "").equals("admin", true)
}

object Fmt {
    fun qty(v: Double?): String {
        val d = v ?: 0.0
        return if (d == Math.floor(d) && !d.isInfinite()) d.toLong().toString()
        else String.format(Locale.US, "%.2f", d)
    }

    fun text(v: String?): String = if (v.isNullOrBlank()) "-" else v

    fun date(ts: Long?): String {
        if (ts == null || ts <= 0L) return "-"
        val ms = if (ts > 100000000000L) ts else ts * 1000L
        return SimpleDateFormat("dd MMM yyyy - HH:mm", Locale("id", "ID")).format(Date(ms))
    }
}
