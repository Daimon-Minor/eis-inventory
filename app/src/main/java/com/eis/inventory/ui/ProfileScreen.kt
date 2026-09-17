package com.eis.inventory.ui

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
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Remote
import com.eis.inventory.data.Session
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val user = Session.current()
    var confirm by remember { mutableStateOf(false) }

    var showPass by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var repeatPass by remember { mutableStateOf("") }
    var passBusy by remember { mutableStateOf(false) }
    var passMsg by remember { mutableStateOf<String?>(null) }
    var passOk by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
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
                InfoRow("Nama pengambil", user?.display ?: "-")
                InfoRow("Username", "@" + (user?.username ?: "-"))
                InfoRow("Peran", if (user?.isAdmin == true) "Administrator (akses penuh)" else "Teknisi (barang keluar)")
                InfoRow("Hak akses", if (user?.isAdmin == true) "Semua menu sistem" else "Ambil barang + riwayat")
            }
        }

        Spacer(Modifier.height(18.dp))
        OutlinedButton(
            onClick = {
                passMsg = null
                passOk = false
                showPass = true
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("GANTI PASSWORD", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
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
            "Barang yang Anda ambil otomatis tercatat di riwayat sistem dengan nama pengambil Anda.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showPass) {
        AlertDialog(
            onDismissRequest = { if (!passBusy) showPass = false },
            title = { Text("Ganti Password") },
            text = {
                Column {
                    Text(
                        "Password baru minimal 6 karakter.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Password sekarang") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Password baru") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repeatPass,
                        onValueChange = { repeatPass = it },
                        label = { Text("Ulangi password baru") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passMsg != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = passMsg ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (passOk) Color(0xFF166534) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !passBusy,
                    onClick = {
                        val problem = when {
                            oldPass.isBlank() -> "Password sekarang wajib diisi"
                            newPass.length < 6 -> "Password baru minimal 6 karakter"
                            newPass != repeatPass -> "Ulangi password baru tidak sama"
                            newPass == oldPass -> "Password baru harus berbeda dari yang sekarang"
                            else -> null
                        }
                        if (problem != null) {
                            passOk = false
                            passMsg = problem
                            return@TextButton
                        }
                        scope.launch {
                            passBusy = true
                            passMsg = null
                            try {
                                Remote.changePassword(oldPass, newPass)
                                passOk = true
                                passMsg = "Password berhasil diganti"
                                oldPass = ""
                                newPass = ""
                                repeatPass = ""
                            } catch (e: Exception) {
                                passOk = false
                                passMsg = e.message ?: "Gagal mengganti password"
                            }
                            passBusy = false
                        }
                    }
                ) { Text(if (passBusy) "Menyimpan…" else "Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showPass = false }) { Text("Tutup") }
            }
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
