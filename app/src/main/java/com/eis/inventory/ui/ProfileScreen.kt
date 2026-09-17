package com.eis.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Http
import com.eis.inventory.data.Session

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val user = Session.current()
    var confirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (user?.display ?: "?").trim().take(1).uppercase(),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(user?.display ?: "-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("@" + (user?.username ?: "-"), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        StatusPill(if (user?.isAdmin == true) "ADMIN" else "USER")
        Spacer(Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                InfoRow("Peran", if (user?.isAdmin == true) "Administrator (akses penuh)" else "Teknisi (barang keluar)")
                InfoRow("Hak akses", if (user?.isAdmin == true) "Semua menu sistem" else "Ambil barang + riwayat")
                InfoRow("Server", Http.base)
                InfoRow("Aplikasi", "EIS v1.0 - Engineering Inventory Sistem")
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { confirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("KELUAR", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Data stok tersimpan di database web yang sama (Vercel Postgres), sehingga pengambilan barang dari aplikasi langsung tampil di dashboard web.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Keluar dari akun?") },
            text = { Text("Anda perlu login ulang untuk memakai aplikasi.") },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    onLogout()
                }) { Text("Keluar") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } }
        )
    }
}
