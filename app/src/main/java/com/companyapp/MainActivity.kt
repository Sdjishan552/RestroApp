package com.companyapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.admin.AdminDashboardActivity
import com.companyapp.auth.WelcomeActivity
import com.companyapp.employee.EmployeeDashboardActivity
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(this)
        val user = FirebaseHelper.auth.currentUser
        val intent = when {
            user == null -> Intent(this, WelcomeActivity::class.java)
            session.isAdmin() -> Intent(this, AdminDashboardActivity::class.java)
            session.isEmployee() -> Intent(this, EmployeeDashboardActivity::class.java)
            else -> Intent(this, WelcomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
