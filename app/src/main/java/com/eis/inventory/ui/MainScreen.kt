package com.eis.inventory.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Remote
import com.eis.inventory.data.Session
import com.eis.inventory.notif.Notif
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class EisTab(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val user = Session.current()
    val admin = Session.isAdmin()
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(0) }

    // Auto refresh: pantau perubahan data (stok masuk/keluar dari web) setiap 7 detik.
    var autoTick by remember { mutableStateOf(0) }
    var signature by remember { mutableStateOf("") }
    var updatedAt by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf<String?>(null) }
    val clock = remember { SimpleDateFormat("HH:mm:ss", Locale("id", "ID")) }

    LaunchedEffect(Unit) {
        var tick = 0
        while (true) {
            try {
                val pulse = Remote.pulse()
                val sig = pulse.signature
                val changed = signature.isNotEmpty() && sig != signature
                if (changed) {
                    autoTick += 1
                    notice = "Stok berubah — data diperbarui"
                }
                signature = sig
                updatedAt = clock.format(Date())
                // Notifikasi stok menyentuh batas minimal — khusus akun admin/superuser.
                // Dicek saat ada perubahan data dan berkala tiap ~35 detik.
                if (admin && (changed || tick % 5 == 0)) {
                    runCatching { Remote.items() }.getOrNull()?.items?.let {
                        Notif.checkLowStock(ctx, it)
                    }
                }
                tick += 1
            } catch (e: Exception) {
                // koneksi terputus sesaat: lewati siklus ini, coba lagi berikutnya
            }
            delay(7000)
        }
    }

    LaunchedEffect(notice) {
        if (notice != null) {
            delay(2800)
            notice = null
        }
    }

    val tabs = if (admin) {
        listOf(
            EisTab("Stok", Icons.Default.Inventory2),
            EisTab("Kelola", Icons.Default.Category),
            EisTab("Riwayat", Icons.Default.History),
            EisTab("Akun", Icons.Default.Person)
        )
    } else {
        listOf(
            EisTab("Stok", Icons.Default.Inventory2),
            EisTab("Riwayat", Icons.Default.History),
            EisTab("Akun", Icons.Default.Person)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandLogo(size = 34)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("EIS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                text = "Engineering Inventory Sistem",
                                fontSize = 11.sp,
                                color = Color(0xFFBDEDE8)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SyncBar(
                status = notice ?: if (updatedAt.isBlank()) "" else "Diperbarui $updatedAt · otomatis",
                busy = false,
                onRefresh = { autoTick += 1 }
            )
            Box(Modifier.fillMaxSize()) {
                val label = tabs.getOrNull(tab)?.label ?: "Stok"
                when (label) {
                    "Stok" -> StockScreen(admin = admin, autoTick = autoTick)
                    "Kelola" -> ManageScreen()
                    "Riwayat" -> HistoryScreen(
                        admin = admin,
                        viewer = user?.display ?: "",
                        autoTick = autoTick
                    )
                    else -> ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}
