package com.eis.inventory.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.eis.inventory.data.Session

private data class EisTab(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val user = Session.current()
    val admin = Session.isAdmin()
    var tab by remember { mutableStateOf(0) }

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
                    Column {
                        Text("EIS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "Engineering Inventory Sistem",
                            fontSize = 11.sp,
                            color = Color(0xFFBDEDE8)
                        )
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
        Box(Modifier.fillMaxSize().padding(padding)) {
            val label = tabs.getOrNull(tab)?.label ?: "Stok"
            when (label) {
                "Stok" -> StockScreen(admin = admin)
                "Kelola" -> ManageScreen()
                "Riwayat" -> HistoryScreen(admin = admin, viewer = user?.display ?: "")
                else -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
