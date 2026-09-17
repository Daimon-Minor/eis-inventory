package com.eis.inventory.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.eis.inventory.MainActivity
import com.eis.inventory.R
import com.eis.inventory.data.Fmt
import com.eis.inventory.data.Item
import com.eis.inventory.data.Session

/**
 * Notifikasi "stok menyentuh batas minimal".
 * Muncul hanya untuk akun yang sedang login sebagai admin/superuser
 * (sesuai permintaan), dan tidak diulang-ulang untuk kondisi yang sama.
 */
object Notif {
    const val CHANNEL_STOK = "eis_stok"
    private const val ID_LOW_STOCK = 4201
    private const val PREFS = "eis_notif"

    /** Channel wajib dibuat sejak Android 8 (API 26). */
    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (mgr.getNotificationChannel(CHANNEL_STOK) != null) return
        val ch = NotificationChannel(
            CHANNEL_STOK,
            "Peringatan Stok",
            NotificationManager.IMPORTANCE_HIGH
        )
        ch.description = "Pemberitahuan saat stok menyentuh batas minimal"
        mgr.createNotificationChannel(ch)
    }

    /** Android 13 (API 33) ke atas butuh izin runtime POST_NOTIFICATIONS. */
    fun permissionNeeded(): Boolean = Build.VERSION.SDK_INT >= 33

    fun permissionGranted(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            val ok = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!ok) return false
        }
        return NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    }

    /** Teks status untuk layar Akun. */
    fun statusText(ctx: Context): String = when {
        !permissionNeeded() && !NotificationManagerCompat.from(ctx).areNotificationsEnabled() -> "Dimatikan di pengaturan HP"
        permissionGranted(ctx) -> "Aktif — peringatan stok menipis"
        else -> "Belum diizinkan"
    }

    /**
     * Cek daftar barang: bila ada yang menyentuh batas minimal, kirim notifikasi.
     * Hanya untuk admin/superuser. Mengembalikan jumlah notifikasi yang dikirim.
     */
    fun checkLowStock(ctx: Context, items: List<Item>): Int {
        if (!Session.isAdmin()) return 0
        ensureChannel(ctx)
        val low = items.filter { it.lowStock }
        // Sidik jari kondisi stok: cegah notifikasi berulang untuk kondisi yang sama.
        val sig = low.map { (it.id ?: 0L).toString() + ":" + (it.stok ?: it.qty ?: 0.0) }
            .sorted().joinToString(",")
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (sig == prefs.getString("last_sig", null)) return 0
        prefs.edit().putString("last_sig", sig).apply()
        if (low.isEmpty() || !permissionGranted(ctx)) return 0

        val habis = low.count { it.habis }
        val title = if (habis > 0) "Stok habis: $habis barang"
        else "Stok menipis: ${low.size} barang"
        val lines = low.take(6).joinToString("\n") {
            "• " + (it.name ?: "-") + " — sisa " + Fmt.qty(it.stok ?: it.qty) + " " +
                (it.unit ?: "") + " (min " + Fmt.qty(it.min_stock) + ")"
        } + if (low.size > 6) "\n…dan ${low.size - 6} barang lain" else ""
        val ringkas = (low.first().name ?: "-") + " sisa " +
            Fmt.qty(low.first().stok ?: low.first().qty) + " " + (low.first().unit ?: "")

        val pi = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(ctx, CHANNEL_STOK)
            .setSmallIcon(R.drawable.ic_stat_stok)
            .setContentTitle(title)
            .setContentText(ringkas)
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        return try {
            NotificationManagerCompat.from(ctx).notify(ID_LOW_STOCK, notif)
            1
        } catch (e: SecurityException) {
            0
        }
    }
}
