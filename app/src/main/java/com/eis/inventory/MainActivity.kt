package com.eis.inventory

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eis.inventory.data.Session
import com.eis.inventory.notif.Notif
import com.eis.inventory.ui.LoginScreen
import com.eis.inventory.ui.MainScreen
import com.eis.inventory.ui.RegisterScreen
import com.eis.inventory.ui.Routes
import com.eis.inventory.ui.theme.EisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.init(this)
        // Channel notifikasi "Peringatan Stok" dibuat sejak awal.
        Notif.ensureChannel(this)
        setContent {
            EisTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EisRoot()
                }
            }
        }
    }
}

@Composable
fun EisRoot() {
    val nav = rememberNavController()
    val start = if (Session.isLoggedIn()) Routes.MAIN else Routes.LOGIN
    val ctx = LocalContext.current

    // Minta izin notifikasi (Android 13+ / API 33 ke atas) saat aplikasi dibuka.
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* hasil izin dipakai di layar Akun */ }

    LaunchedEffect(Unit) {
        Notif.ensureChannel(ctx)
        if (Notif.permissionNeeded() && !Notif.permissionGranted(ctx)) {
            runCatching { permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
    }

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoRegister = { nav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onLoggedIn = {
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(onLogout = {
                Session.clear()
                nav.navigate(Routes.LOGIN) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            })
        }
    }
}
