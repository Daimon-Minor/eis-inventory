package com.eis.inventory.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val id: Long? = null,
    val name: String? = null,
    val username: String? = null,
    val role: String? = null,
    val duty_status: String? = null
) {
    val display: String get() = if (!name.isNullOrBlank()) name else if (!username.isNullOrBlank()) username else "Pengguna"

    /** Admin & superuser sama-sama punya akses penuh ke seluruh fitur. */
    val isAdmin: Boolean get() = role.equals("admin", true) || role.equals("superuser", true)
    val isSuperuser: Boolean get() = role.equals("superuser", true)

    val roleLabel: String
        get() = when {
            role.equals("superuser", true) -> "Superuser"
            isAdmin -> "Administrator"
            else -> "Teknisi"
        }

    /** "Status Duty": apakah akun sedang bertugas. */
    val duty: String get() = if (duty_status.equals("off", true)) "off" else "duty"
    val dutyLabel: String get() = if (duty == "off") "Off Duty" else "Duty"
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
) {
    /** Stok sudah menyentuh / di bawah batas minimal. */
    val lowStock: Boolean
        get() {
            val s = stok ?: qty ?: 0.0
            val m = min_stock ?: 0.0
            return m > 0 && s <= m
        }

    val habis: Boolean
        get() = (stok ?: qty ?: 0.0) <= 0.0
}

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
    val actor_name: String? = null,
    val actor_type: String? = null,
    val category: String? = null,
    val created_at: Long? = null
) {
    /** Nama pengambil/petugas yang ditampilkan (selalu nama orang, bukan username). */
    val pengambil: String
        get() = if (!actor_name.isNullOrBlank()) actor_name else if (!actor.isNullOrBlank()) actor else "-"
}

data class Pulse(
    val items: Int? = null,
    val transactions: Int? = null,
    val last_tx_id: Long? = null,
    val categories: Int? = null,
    val users: Int? = null,
    val ts: Long? = null
) {
    /** Sidik jari data: berubah bila ada stok masuk/keluar, barang, kategori, atau pengguna baru. */
    val signature: String get() = "$items|$transactions|$last_tx_id|$categories|$users"
}

data class WebUser(
    val id: Long? = null,
    val name: String? = null,
    val username: String? = null,
    val full_name: String? = null,
    val role: String? = null,
    val duty_status: String? = null,
    val created_at: Long? = null
) {
    val display: String get() = if (!full_name.isNullOrBlank()) full_name else if (!name.isNullOrBlank()) name else "(tanpa nama)"
    val isAdmin: Boolean get() = role.equals("admin", true) || role.equals("superuser", true)
    val isSuperuser: Boolean get() = role.equals("superuser", true)
    val roleLabel: String
        get() = when {
            role.equals("superuser", true) -> "Superuser"
            isAdmin -> "Administrator"
            else -> "Teknisi"
        }
    val dutyLabel: String get() = if (duty_status.equals("off", true)) "Off Duty" else "Duty"
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
