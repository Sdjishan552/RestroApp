package com.companyapp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("biz_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ROLE = "role"
        const val KEY_COMPANY_ID = "company_id"
        const val KEY_COMPANY_NAME = "company_name"
        const val ROLE_ADMIN = "admin"
        const val ROLE_EMPLOYEE = "employee"
    }

    fun saveAdminSession(companyId: String, companyName: String) {
        prefs.edit()
            .putString(KEY_ROLE, ROLE_ADMIN)
            .putString(KEY_COMPANY_ID, companyId)
            .putString(KEY_COMPANY_NAME, companyName)
            .apply()
    }

    fun saveEmployeeSession(companyId: String, companyName: String) {
        prefs.edit()
            .putString(KEY_ROLE, ROLE_EMPLOYEE)
            .putString(KEY_COMPANY_ID, companyId)
            .putString(KEY_COMPANY_NAME, companyName)
            .apply()
    }

    fun isAdmin(): Boolean = prefs.getString(KEY_ROLE, null) == ROLE_ADMIN
    fun isEmployee(): Boolean = prefs.getString(KEY_ROLE, null) == ROLE_EMPLOYEE
    fun getCompanyId(): String = prefs.getString(KEY_COMPANY_ID, "") ?: ""
    fun getCompanyName(): String = prefs.getString(KEY_COMPANY_NAME, "") ?: ""
    fun getRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun clear() = prefs.edit().clear().apply()
}
