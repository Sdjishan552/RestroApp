package com.companyapp.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.companyapp.auth.WelcomeActivity
import com.companyapp.databinding.ActivityAdminDashboardBinding
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        setupHeader()
        setupActions()
        loadCounts()
    }

    private fun setupHeader() {
        val user = FirebaseHelper.auth.currentUser
        val firstName = user?.displayName?.split(" ")?.firstOrNull() ?: "Owner"
        binding.tvWelcome.text = "Hello, $firstName"
        binding.tvCompanyName.text = session.getCompanyName()
        binding.tvOwnerEmail.text = user?.email ?: "Google account"
        if (user?.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).circleCrop().into(binding.ivAvatar)
        }
    }

    private fun setupActions() {
        binding.btnLogout.setOnClickListener { confirmLogout() }
        binding.cardStock.root.setOnClickListener { startActivity(Intent(this, StockManagementActivity::class.java)) }
        binding.cardOrders.root.setOnClickListener { startActivity(Intent(this, OrdersActivity::class.java)) }
        binding.cardFinance.root.setOnClickListener { startActivity(Intent(this, FinanceActivity::class.java)) }
        binding.cardProducts.root.setOnClickListener { startActivity(Intent(this, ProductsActivity::class.java)) }
        binding.cardEmployees.root.setOnClickListener { startActivity(Intent(this, EmployeeManagementActivity::class.java)) }
        binding.cardReports.root.setOnClickListener { startActivity(Intent(this, ReportsActivity::class.java)) }
        binding.cardSettings.root.setOnClickListener { startActivity(Intent(this, AdminSettingsActivity::class.java)) }
    }

    private fun loadCounts() {
        val companyId = session.getCompanyId()
        FirebaseHelper.getEmployees(companyId, { employees ->
            binding.tvEmployeeCount.text = employees.size.toString()
        }, { binding.tvEmployeeCount.text = "0" })
        FirebaseHelper.getStockItems(companyId, { items ->
            binding.tvStockCount.text = items.size.toString()
            binding.tvLowStockCount.text = items.count { it.quantity <= it.minQuantity }.toString()
        }, {
            binding.tvStockCount.text = "0"
            binding.tvLowStockCount.text = "0"
        })
    }

    override fun onResume() {
        super.onResume()
        if (::session.isInitialized) loadCounts()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Sign out of this company on this device?")
            .setPositiveButton("Logout") { _, _ -> logout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        FirebaseHelper.auth.signOut()
        session.clear()
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
