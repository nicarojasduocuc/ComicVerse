package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_PENDING_ORDER_REF = "pending_order_reference"

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
    
    fun savePendingOrderReference(context: Context, reference: String) {
        getPreferences(context).edit().apply {
            putString(KEY_PENDING_ORDER_REF, reference)
            apply()
        }
    }
    
    fun getPendingOrderReference(context: Context): String? {
        return getPreferences(context).getString(KEY_PENDING_ORDER_REF, null)
    }
    
    fun clearPendingOrderReference(context: Context) {
        getPreferences(context).edit().apply {
            remove(KEY_PENDING_ORDER_REF)
            apply()
        }
    }
    
    fun setNeedsNavigateToOrders(context: Context, needs: Boolean) {
        getPreferences(context).edit().apply {
            putBoolean("needs_navigate_to_orders", needs)
            apply()
        }
    }
    
    fun needsNavigateToOrders(context: Context): Boolean {
        val needs = getPreferences(context).getBoolean("needs_navigate_to_orders", false)
        if (needs) {
            // Limpiar el flag después de leerlo
            setNeedsNavigateToOrders(context, false)
        }
        return needs
    }
    
    fun setIsProcessingPayment(context: Context, isProcessing: Boolean) {
        getPreferences(context).edit().apply {
            putBoolean("is_processing_payment", isProcessing)
            apply()
        }
    }
    
    fun isProcessingPayment(context: Context): Boolean {
        return getPreferences(context).getBoolean("is_processing_payment", false)
    }
}
