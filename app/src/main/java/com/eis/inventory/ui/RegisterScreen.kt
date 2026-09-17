package com.eis.inventory.ui

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Remote
import com.eis.inventory.data.Session
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onBack: () -> Unit, onLoggedIn: () -> Unit) {
    val scope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val u = username.trim()
        when {
            fullName.trim().length < 2 -> { error = "Nama minimal 2 karakter"; return }
            !Regex("^[A-Za-z0-9_.]{3,32}$").matches(u) -> { error = "Username 3-32 karakter (huruf, angka, titik, underscore)"; return }
            password.length < 6 -> { error = "Password minimal 6 karakter"; return }
            password != confirm -> { error = "Konfirmasi password tidak sama"; return }
        }
        busy = true
        error = null
        scope.launch {
            try {
                Remote.register(u, fullName.trim(), password)
                info = "Akun berhasil dibuat, masuk otomatis..."
                val res = Remote.login(u, password)
                val tk = res.token
                if (tk.isNullOrBlank()) {
                    error = "Akun dibuat. Silakan login manual."
                } else {
                    Session.save(tk, res.user)
                    onLoggedIn()
                }
            } catch (e: Exception) {
                error = e.message ?: "Gagal mendaftar"
            }
            busy = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                }
                Text(
                    "Daftar Akun",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(Modifier.height(16.dp))
            BrandLogo(60)
            Spacer(Modifier.height(10.dp))
            Text(
                "Akun ini untuk teknisi/user: hanya dapat mengambil barang (barang keluar) dan terekam otomatis di web.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nama") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
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
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        label = { Text("Konfirmasi Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    info?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { submit() },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (busy) "Mendaftar..." else "DAFTAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
