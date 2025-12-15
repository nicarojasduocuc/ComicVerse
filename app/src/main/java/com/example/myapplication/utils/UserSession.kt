package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(context: Context, userId: Int) {
        getPreferences(context).edit().apply {
            putInt(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, -1)
    }
    
    fun saveUserEmail(context: Context, email: String) {
        getPreferences(context).edit().apply {
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }
    
    fun getUserEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout(context: Context) {
        getPreferences(context).edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}
