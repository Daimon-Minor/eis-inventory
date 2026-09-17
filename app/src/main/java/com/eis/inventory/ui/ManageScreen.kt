package com.eis.inventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Category
import com.eis.inventory.data.Remote
import com.eis.inventory.data.Session
import com.eis.inventory.data.WebUser
import kotlinx.coroutines.launch

@Composable
fun ManageScreen() {
    var section by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { section = 0 },
                modifier = Modifier.weight(1f),
                enabled = section != 0
            ) { Text("Kategori") }
            Button(
                onClick = { section = 1 },
                modifier = Modifier.weight(1f),
                enabled = section != 1
            ) { Text("Pengguna") }
        }
        if (section == 0) CategoriesTab() else UsersTab()
    }
}

@Composable
private fun CategoriesTab() {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var cats by remember { mutableStateOf(listOf<Category>()) }
    var newName by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }
    var editTarget by remember { mutableStateOf<Category?>(null) }
    var editName by remember { mutableStateOf("") }
    var delTarget by remember { mutableStateOf<Category?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        loading = true
        error = null
        try {
            cats = Remote.categories()
        } catch (e: Exception) {
            error = e.message ?: "Gagal memuat kategori"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Kategori baru") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                enabled = !busy,
                onClick = {
                    val nm = newName.trim()
                    if (nm.isEmpty()) return@IconButton
                    busy = true
                    scope.launch {
                        try {
                            Remote.addCategory(nm)
                            newName = ""
                            message = "Kategori ditambahkan"
                            refresh = refresh + 1
                        } catch (e: Exception) {
                            message = e.message
                        }
                        busy = false
                    }
                }
            ) { Icon(Icons.Default.PlaylistAdd, contentDescription = "Tambah kategori") }
        }
        message?.let {
            Text(
                it,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        when {
            loading -> LoadingBox()
            error != null -> ErrorBar(error ?: "") { refresh = refresh + 1 }
            cats.isEmpty() -> EmptyBox("Belum ada kategori")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cats, key = { c -> (c.id ?: 0L).toString() + (c.name ?: "") }) { c ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(c.name ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    (c.items ?: 0).toString() + " barang",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { editTarget = c; editName = c.name ?: "" }) { Text("Ubah") }
                            IconButton(onClick = { delTarget = c }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus kategori")
                            }
                        }
                    }
                }
            }
        }
    }

    editTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Ubah nama kategori") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Nama kategori") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val nm = editName.trim()
                    val id = target.id ?: 0L
                    editTarget = null
                    if (nm.isEmpty()) return@TextButton
                    scope.launch {
                        try {
                            Remote.renameCategory(id, nm)
                            message = "Kategori diubah"
                        } catch (e: Exception) {
                            message = e.message
                        }
                        refresh = refresh + 1
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text("Batal") } }
        )
    }

    delTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { delTarget = null },
            title = { Text("Hapus kategori?") },
            text = { Text("Barang pada kategori " + (target.name ?: "-") + " akan dipindah ke kategori Umum.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = target.id ?: 0L
                    delTarget = null
                    scope.launch {
                        try {
                            Remote.deleteCategory(id, "Umum")
                            message = "Kategori dihapus"
                        } catch (e: Exception) {
                            message = e.message
                        }
                        refresh = refresh + 1
                    }
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { delTarget = null }) { Text("Batal") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsersTab() {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var users by remember { mutableStateOf(listOf<WebUser>()) }
    var refresh by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var delTarget by remember { mutableStateOf<WebUser?>(null) }
    val me = Session.current()

    LaunchedEffect(refresh) {
        loading = true
        error = null
        try {
            users = Remote.users()
        } catch (e: Exception) {
            error = e.message ?: "Gagal memuat pengguna"
        }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daftar akun web + aplikasi", fontSize = 12.sp, modifier = Modifier.weight(1f))
            Button(onClick = { showForm = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Tambah")
            }
        }
        message?.let {
            Text(
                it,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        when {
            loading -> LoadingBox()
            error != null -> ErrorBar(error ?: "") { refresh = refresh + 1 }
            users.isEmpty() -> EmptyBox("Belum ada pengguna")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users, key = { u -> u.id ?: 0L }) { u ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(u.display, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "@" + (u.username ?: "-") + "  ·  " + (if (u.isAdmin) "Administrator" else "Teknisi"),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if ((u.username ?: "") != (me?.username ?: "")) {
                                IconButton(onClick = { delTarget = u }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus pengguna")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        UserFormDialog(
            onDismiss = { showForm = false },
            onSaved = { msg ->
                message = msg
                showForm = false
                refresh = refresh + 1
            }
        )
    }

    delTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { delTarget = null },
            title = { Text("Hapus pengguna?") },
            text = { Text("Akun " + target.display + " tidak dapat login lagi.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = target.id ?: 0L
                    delTarget = null
                    scope.launch {
                        try {
                            Remote.deleteUser(id)
                            message = "Pengguna dihapus"
                        } catch (e: Exception) {
                            message = e.message
                        }
                        refresh = refresh + 1
                    }
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { delTarget = null }) { Text("Batal") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFormDialog(onDismiss: () -> Unit, onSaved: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("engineer") }
    var expanded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Tambah pengguna") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = if (role == "admin") "Administrator" else "Teknisi (hanya barang keluar)",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Peran") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Teknisi (hanya barang keluar)") },
                            onClick = { role = "engineer"; expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Administrator (akses penuh)") },
                            onClick = { role = "admin"; expanded = false }
                        )
                    }
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    if (name.trim().isEmpty() || username.trim().isEmpty() || password.length < 6) {
                        error = "Nama, username wajib; password minimal 6 karakter"
                        return@TextButton
                    }
                    busy = true
                    error = null
                    scope.launch {
                        try {
                            Remote.addUser(name.trim(), username.trim(), password, role)
                            onSaved("Pengguna ditambahkan")
                        } catch (e: Exception) {
                            error = e.message ?: "Gagal menyimpan"
                        }
                        busy = false
                    }
                }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Batal") } }
    )
}
