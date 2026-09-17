package com.eis.inventory.data

import android.content.Context
import android.content.SharedPreferences

object Session {
    private var prefs: SharedPreferences? = null

    @Volatile
    private var user: User? = null

    fun init(ctx: Context) {
        if (prefs == null) {
            prefs = ctx.applicationContext.getSharedPreferences("eis_session", Context.MODE_PRIVATE)
        }
        val t = prefs?.getString("token", null)
        Http.token = t
        if (!t.isNullOrBlank()) {
            user = User(
                id = prefs?.getLong("uid", 0L),
                name = prefs?.getString("name", null),
                username = prefs?.getString("username", null),
                role = prefs?.getString("role", null),
                duty_status = prefs?.getString("duty", null)
            )
        }
    }

    fun isLoggedIn(): Boolean = !Http.token.isNullOrBlank() && user != null

    fun current(): User? = user

    /** Admin & superuser punya akses penuh. */
    fun isAdmin(): Boolean = user?.isAdmin == true

    /** Hanya admin asli yang boleh membuat superuser. */
    fun isSuperAdmin(): Boolean = (user?.role ?: "").equals("admin", true)

    fun save(token: String, u: User?) {
        Http.token = token
        val nu = u ?: User()
        user = nu
        prefs?.edit()
            ?.putString("token", token)
            ?.putLong("uid", nu.id ?: 0L)
            ?.putString("name", nu.name)
            ?.putString("username", nu.username)
            ?.putString("role", nu.role)
            ?.putString("duty", nu.duty)
            ?.apply()
    }

    fun refresh(u: User?) {
        if (u == null) return
        user = u
        prefs?.edit()
            ?.putLong("uid", u.id ?: 0L)
            ?.putString("name", u.name)
            ?.putString("username", u.username)
            ?.putString("role", u.role)
            ?.putString("duty", u.duty)
            ?.apply()
    }

    fun clear() {
        Http.token = null
        user = null
        prefs?.edit()?.clear()?.apply()
    }
}
