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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Fmt
import com.eis.inventory.data.Remote
import com.eis.inventory.data.Tx

@Composable
fun HistoryScreen(admin: Boolean, viewer: String = "") {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var rows by remember { mutableStateOf(listOf<Tx>()) }
    var filter by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }

    LaunchedEffect(refresh) {
        loading = true
        error = null
        try {
            rows = if (admin) Remote.transactions(150) else Remote.myTransactions()
        } catch (e: Exception) {
            error = e.message ?: "Gagal memuat riwayat"
        }
        loading = false
    }

    val shown = when (filter) {
        "IN" -> rows.filter { (it.type ?: "").equals("IN", true) }
        "OUT" -> rows.filter { (it.type ?: "").equals("OUT", true) }
        else -> rows
    }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(selected = filter.isEmpty(), onClick = { filter = "" }, label = { Text("Semua") })
            }
            item {
                FilterChip(selected = filter == "IN", onClick = { filter = "IN" }, label = { Text("Masuk") })
            }
            item {
                FilterChip(selected = filter == "OUT", onClick = { filter = "OUT" }, label = { Text("Keluar") })
            }
        }

        Text(
            text = if (admin) "Semua transaksi barang (semua petugas)" else "Riwayat pengambilan saya",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )

        when {
            loading -> LoadingBox()
            error != null -> ErrorBar(error ?: "") { refresh = refresh + 1 }
            shown.isEmpty() -> EmptyBox("Belum ada transaksi")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { row -> row.id ?: 0L }) { row ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(13.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TypePill(row.type)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = row.item_name ?: "-",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = Fmt.qty(row.qty) + " " + (row.unit ?: "pcs"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = Fmt.date(row.created_at) + "  ·  " + (row.actor ?: "-"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!row.note.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = row.note ?: "",
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
