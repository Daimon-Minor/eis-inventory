package com.eis.inventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Category
import com.eis.inventory.data.Fmt
import com.eis.inventory.data.Item
import com.eis.inventory.data.Remote
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class StockAction(val item: Item, val mode: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(admin: Boolean) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf(listOf<Item>()) }
    var cats by remember { mutableStateOf(listOf<Category>()) }
    var query by remember { mutableStateOf("") }
    var catFilter by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }
    var detail by remember { mutableStateOf<Item?>(null) }
    var editing by remember { mutableStateOf<Item?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf<StockAction?>(null) }
    var toDelete by remember { mutableStateOf<Item?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(refresh, query, catFilter, statusFilter) {
        loading = true
        error = null
        if (query.isNotBlank()) delay(280)
        try {
            val res = Remote.items(query.trim(), catFilter, statusFilter)
            items = res.items ?: emptyList()
            cats = res.categories ?: emptyList()
        } catch (e: Exception) {
            error = e.message ?: "Gagal memuat data"
        }
        loading = false
    }

    LaunchedEffect(message) {
        if (message != null) {
            delay(2600)
            message = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                placeholder = { Text("Cari nama atau kode barang") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = catFilter.isEmpty() && statusFilter.isEmpty(),
                        onClick = { catFilter = ""; statusFilter = "" },
                        label = { Text("Semua") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == "MENIPIS",
                        onClick = { statusFilter = if (statusFilter == "MENIPIS") "" else "MENIPIS" },
                        label = { Text("Menipis") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == "HABIS",
                        onClick = { statusFilter = if (statusFilter == "HABIS") "" else "HABIS" },
                        label = { Text("Habis") }
                    )
                }
                items(cats, key = { c -> c.name ?: "-" }) { c ->
                    val name = c.name ?: "-"
                    FilterChip(
                        selected = catFilter == name,
                        onClick = { catFilter = if (catFilter == name) "" else name },
                        label = { Text(name) }
                    )
                }
            }

            message?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            when {
                loading -> LoadingBox()
                error != null -> ErrorBar(error ?: "") { refresh = refresh + 1 }
                items.isEmpty() -> EmptyBox("Belum ada barang yang cocok")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { row -> row.id ?: 0L }) { row ->
                        ItemCard(row) { detail = row }
                    }
                }
            }
        }

        if (admin) {
            FloatingActionButton(
                onClick = { editing = null; showForm = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah barang")
            }
        }
    }

    detail?.let { d ->
        ModalBottomSheet(onDismissRequest = { detail = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(d.name ?: "-", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(
                    (d.sku ?: "-") + "  ·  " + (d.category ?: "Umum"),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(d.status)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        Fmt.qty(d.stok) + " " + (d.unit ?: "pcs") + " tersedia",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                InfoRow("Stok awal", Fmt.qty(d.stok_awal) + " " + (d.unit ?: "pcs"))
                InfoRow("Total masuk", Fmt.qty(d.masuk) + " " + (d.unit ?: "pcs"))
                InfoRow("Total keluar", Fmt.qty(d.keluar) + " " + (d.unit ?: "pcs"))
                InfoRow("Stok saat ini", Fmt.qty(d.stok) + " " + (d.unit ?: "pcs"))
                InfoRow("Satuan", d.unit ?: "pcs")
                InfoRow("Lokasi rak", Fmt.text(d.rack))
                InfoRow("Min. stok", Fmt.qty(d.min_stock))
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { action = StockAction(d, "TAKE"); detail = null },
                    enabled = (d.stok ?: 0.0) > 0.0,
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ambil Barang (Barang Keluar)")
                }
                if (admin) {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { action = StockAction(d, "IN"); detail = null },
                            modifier = Modifier.weight(1f)
                        ) { Text("+ Stok Masuk") }
                        OutlinedButton(
                            onClick = { action = StockAction(d, "OUT"); detail = null },
                            modifier = Modifier.weight(1f)
                        ) { Text("- Stok Keluar") }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { editing = d; showForm = true; detail = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Ubah")
                        }
                        Button(
                            onClick = { toDelete = d; detail = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Hapus")
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    action?.let { act ->
        QtyDialog(
            action = act,
            onDismiss = { action = null },
            onSaved = { msg ->
                message = msg
                action = null
                refresh = refresh + 1
            }
        )
    }

    if (showForm) {
        ItemFormSheet(
            item = editing,
            cats = cats,
            onDismiss = { showForm = false },
            onSaved = { msg ->
                message = msg
                showForm = false
                refresh = refresh + 1
            }
        )
    }

    toDelete?.let { d ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Hapus barang?") },
            text = { Text((d.name ?: "-") + " akan dihapus beserta seluruh riwayat transaksinya.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = d.id ?: 0L
                    toDelete = null
                    scope.launch {
                        try {
                            Remote.deleteItem(id)
                            message = "Barang dihapus"
                        } catch (e: Exception) {
                            message = e.message
                        }
                        refresh = refresh + 1
                    }
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Batal") } }
        )
    }
}

@Composable
private fun QtyDialog(
    action: StockAction,
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val item = action.item
    val take = action.mode == "TAKE"
    val title = when (action.mode) {
        "TAKE" -> "Ambil Barang"
        "IN" -> "Tambah Stok"
        else -> "Kurangi Stok"
    }
    var qty by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(title) },
        text = {
            Column {
                Text(item.name ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    "Stok tersedia: " + Fmt.qty(item.stok) + " " + (item.unit ?: "pcs"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { input -> qty = input.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                    label = { Text("Jumlah") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (take) "Keperluan / catatan" else "Catatan") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                    val q = qty.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (q <= 0.0) {
                        error = "Jumlah harus lebih dari 0"
                        return@TextButton
                    }
                    busy = true
                    error = null
                    scope.launch {
                        try {
                            if (take) {
                                Remote.take(item.id ?: 0L, q, note.trim())
                            } else {
                                Remote.adjust(item.id ?: 0L, action.mode, q, note.trim())
                            }
                            onSaved(
                                (if (take) "Pengambilan tercatat: " else "Stok diperbarui: ") +
                                    Fmt.qty(q) + " " + (item.unit ?: "pcs") + " " + (item.name ?: "")
                            )
                        } catch (e: Exception) {
                            error = e.message ?: "Gagal menyimpan"
                        }
                        busy = false
                    }
                }
            ) { Text(if (busy) "Menyimpan..." else "Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemFormSheet(
    item: Item?,
    cats: List<Category>,
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEdit = item != null
    var name by remember { mutableStateOf(item?.name ?: "") }
    var sku by remember { mutableStateOf(item?.sku ?: "") }
    var category by remember { mutableStateOf(item?.category ?: (cats.firstOrNull()?.name ?: "Umum")) }
    var rack by remember { mutableStateOf(item?.rack ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "pcs") }
    var minStock by remember { mutableStateOf(if (isEdit) Fmt.qty(item?.min_stock) else "0") }
    var initQty by remember { mutableStateOf("0") }
    var expanded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (isEdit) "Ubah Barang" else "Tambah Barang",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Barang") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    cats.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name ?: "-") },
                            onClick = { category = c.name ?: "Umum"; expanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("Kode Barang") },
                placeholder = { Text("kosongkan = otomatis") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = rack,
                onValueChange = { rack = it },
                label = { Text("Lokasi Rak") },
                placeholder = { Text("mis. A-03-2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Satuan") },
                placeholder = { Text("pcs / unit / meter") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = minStock,
                onValueChange = { input -> minStock = input.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                label = { Text("Min. Stok (peringatan menipis)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            if (!isEdit) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = initQty,
                    onValueChange = { input -> initQty = input.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                    label = { Text("Stok Awal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !busy,
                onClick = {
                    if (name.trim().length < 2) {
                        error = "Nama barang minimal 2 karakter"
                        return@Button
                    }
                    busy = true
                    error = null
                    val body = mutableMapOf<String, Any?>(
                        "name" to name.trim(),
                        "category" to category,
                        "sku" to sku.trim(),
                        "rack" to rack.trim(),
                        "unit" to unit.trim().ifBlank { "pcs" },
                        "min_stock" to (minStock.replace(',', '.').toDoubleOrNull() ?: 0.0)
                    )
                    if (!isEdit) {
                        body["qty"] = initQty.replace(',', '.').toDoubleOrNull() ?: 0.0
                    }
                    scope.launch {
                        try {
                            if (isEdit) {
                                Remote.updateItem(item?.id ?: 0L, body)
                            } else {
                                Remote.createItem(body)
                            }
                            onSaved(if (isEdit) "Barang diperbarui" else "Barang baru ditambahkan")
                        } catch (e: Exception) {
                            error = e.message ?: "Gagal menyimpan"
                        }
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (busy) "Menyimpan..." else "SIMPAN", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
