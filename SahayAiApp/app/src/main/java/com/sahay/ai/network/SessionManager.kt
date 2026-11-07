package com.sahay.ai.network

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "SahaySessionPref"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_VICTIM_TOKEN = "victim_token"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_VICTIM_CASE_ID = "victim_case_id"
    private const val KEY_OFFICIAL_EMAIL = "official_email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        android.util.Log.d("AuthDebug", "AUTH_INIT - SessionManager Initialized")
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
        }

    var victimSessionToken: String?
        get() = prefs.getString(KEY_VICTIM_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_VICTIM_TOKEN, value).apply()
        }

    var userRole: String?
        get() = prefs.getString(KEY_USER_ROLE, null)
        set(value) {
            prefs.edit().putString(KEY_USER_ROLE, value).apply()
        }

    var victimCaseId: String?
        get() = prefs.getString(KEY_VICTIM_CASE_ID, null)
        set(value) {
            prefs.edit().putString(KEY_VICTIM_CASE_ID, value).apply()
        }

    var officialEmail: String?
        get() = prefs.getString(KEY_OFFICIAL_EMAIL, null)
        set(value) {
            prefs.edit().putString(KEY_OFFICIAL_EMAIL, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
