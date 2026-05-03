package com.superlist.app

import android.app.Application
import com.superlist.app.data.preferences.TokenManager

class SuperListApp : Application() {
    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager.getInstance(this)
    }
}
