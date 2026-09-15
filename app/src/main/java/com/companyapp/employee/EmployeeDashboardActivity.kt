package com.companyapp.employee

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.companyapp.admin.FinanceActivity
import com.companyapp.admin.OrdersActivity
import com.companyapp.admin.ProductsActivity
import com.companyapp.admin.ReportsActivity
import com.companyapp.admin.StockManagementActivity
import com.companyapp.auth.WelcomeActivity
import com.companyapp.databinding.ActivityEmployeeDashboardBinding
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class EmployeeDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeDashboardBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        setupHeader()
        loadPermissions()
        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    private fun setupHeader() {
        val user = FirebaseHelper.auth.currentUser
        val first = user?.displayName?.split(" ")?.firstOrNull() ?: "Employee"
        binding.tvWelcome.text = "Hello, $first"
        binding.tvCompanyName.text = session.getCompanyName()
        if (user?.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).circleCrop().into(binding.ivAvatar)
        }
    }

    private fun loadPermissions() {
        val user = FirebaseHelper.auth.currentUser ?: return
        FirebaseHelper.checkEmployeeAuthorized(session.getCompanyId(), user.email ?: "") { authorized, employee ->
            if (!authorized || employee == null) {
                Toast.makeText(this, "Access revoked. Contact your admin.", Toast.LENGTH_LONG).show()
                logout()
                return@checkEmployeeAuthorized
            }
            val p = employee.permissions
            showCard(binding.cardStock.root, p.stock)
            showCard(binding.cardOrders.root, p.orders)
            showCard(binding.cardFinance.root, p.finance)
            showCard(binding.cardProducts.root, p.products)
            showCard(binding.cardReports.root, p.reports)

            val anyVisible = p.stock || p.orders || p.finance || p.products || p.reports
            binding.tvNoAccess.visibility = if (anyVisible) View.GONE else View.VISIBLE

            binding.cardStock.root.setOnClickListener { startActivity(Intent(this, StockManagementActivity::class.java)) }
            binding.cardOrders.root.setOnClickListener { startActivity(Intent(this, OrdersActivity::class.java)) }
            binding.cardFinance.root.setOnClickListener { startActivity(Intent(this, FinanceActivity::class.java)) }
            binding.cardProducts.root.setOnClickListener { startActivity(Intent(this, ProductsActivity::class.java)) }
            binding.cardReports.root.setOnClickListener { startActivity(Intent(this, ReportsActivity::class.java)) }
        }
    }

    private fun showCard(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
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
