package com.superlist.app.data.preferences

import android.content.Context

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userPhone: String?
        get() = prefs.getString(KEY_USER_PHONE, null)
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    var familiaActualId: String?
        get() = prefs.getString(KEY_FAMILIA_ACTUAL, null)
        set(value) = prefs.edit().putString(KEY_FAMILIA_ACTUAL, value).apply()

    var familiaActualNombre: String?
        get() = prefs.getString(KEY_FAMILIA_NOMBRE, null)
        set(value) = prefs.edit().putString(KEY_FAMILIA_NOMBRE, value).apply()

    val isLoggedIn: Boolean
        get() = token != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    private val authToken: String
        get() = "Bearer ${token.orEmpty()}"

    fun getAuthHeader(): String = authToken

    companion object {
        private const val PREFS_NAME = "superlist_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_FAMILIA_ACTUAL = "familia_actual"
        private const val KEY_FAMILIA_NOMBRE = "familia_nombre"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
