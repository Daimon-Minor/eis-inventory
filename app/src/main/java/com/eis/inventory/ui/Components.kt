package com.eis.inventory.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Fmt
import com.eis.inventory.data.Item

@Composable
fun StatusPill(status: String?) {
    val st = (status ?: "").uppercase()
    val bg: Color
    val fg: Color
    when (st) {
        "HABIS" -> { bg = Color(0xFFFBE3E1); fg = Color(0xFF9E1C16) }
        "MENIPIS" -> { bg = Color(0xFFFFF0D6); fg = Color(0xFF8A5300) }
        else -> { bg = Color(0xFFDCF3E4); fg = Color(0xFF166534) }
    }
    Text(
        text = if (st.isBlank()) "AMAN" else st,
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun TypePill(type: String?) {
    val masuk = (type ?: "").equals("IN", true)
    val bg = if (masuk) Color(0xFFDCF3E4) else Color(0xFFFBE3E1)
    val fg = if (masuk) Color(0xFF166534) else Color(0xFF9E1C16)
    Text(
        text = if (masuk) "MASUK" else "KELUAR",
        color = fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun MetaChip(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BrandLogo(size: Int = 68) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape((size / 4).dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("EIS", color = Color.White, fontWeight = FontWeight.Black, fontSize = (size / 3).sp)
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyBox(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📦", fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ErrorBar(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚠️", fontSize = 30.sp)
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Coba Lagi")
        }
    }
}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.name ?: "-",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = (item.sku ?: "-") + "  ·  " + (item.category ?: "Umum"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Fmt.qty(item.stok) + " " + (item.unit ?: "pcs"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    StatusPill(item.status)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row {
                MetaChip("Rak: " + Fmt.text(item.rack))
                Spacer(Modifier.width(8.dp))
                MetaChip("Min: " + Fmt.qty(item.min_stock))
                Spacer(Modifier.width(8.dp))
                MetaChip("Masuk: " + Fmt.qty(item.masuk))
            }
        }
    }
}
