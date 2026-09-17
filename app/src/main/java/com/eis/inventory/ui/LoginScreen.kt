package com.eis.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Remote
import com.eis.inventory.data.Session
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onGoRegister: () -> Unit) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (username.isBlank() || password.isBlank()) {
            error = "Username dan password wajib diisi"
            return
        }
        busy = true
        error = null
        scope.launch {
            try {
                val res = Remote.login(username.trim(), password)
                val tk = res.token
                if (tk.isNullOrBlank()) {
                    error = "Login gagal: token tidak diterima"
                } else {
                    Session.save(tk, res.user)
                    onLoggedIn()
                }
            } catch (e: Exception) {
                error = e.message ?: "Gagal login"
            }
            busy = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            BrandLogo(74)
            Spacer(Modifier.height(14.dp))
            Text("Engineering Inventory Sistem", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Aplikasi stok barang Engineering — terhubung ke web & database yang sama",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.height(26.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "User daftar sendiri untuk mengambil barang. Admin memakai akun admin web.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Lihat password"
                                )
                            }
                        },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { submit() },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (busy) "Memproses..." else "MASUK", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(onClick = onGoRegister, enabled = !busy) {
                            Text("Belum punya akun? Daftar di sini")
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "EIS v1.0.3 · Engineering Inventory Sistem",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
