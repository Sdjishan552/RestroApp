package com.companyapp.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.companyapp.R
import com.companyapp.adapter.StockSummaryAdapter
import com.companyapp.auth.WelcomeActivity
import com.companyapp.databinding.ActivityAdminDashboardBinding
import com.companyapp.models.StockItem
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var session: SessionManager
    private val stockItems = mutableListOf<StockItem>()
    private lateinit var stockAdapter: StockSummaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setupToolbarAndDrawer()
        binding.tvCompanyName.text = session.getCompanyName()
        setupStockList()
        loadCounts()
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        val nav = binding.navView
        val headerView = nav.getHeaderView(0)
        val user = FirebaseHelper.auth.currentUser
        headerView.findViewById<TextView>(R.id.tvWelcome).text =
            "Hello, ${user?.displayName?.split(" ")?.firstOrNull() ?: "Owner"}"
        headerView.findViewById<TextView>(R.id.tvCompanyName).text = session.getCompanyName()
        if (user?.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).circleCrop().into(headerView.findViewById(R.id.ivAvatar))
        }
        nav.setNavigationItemSelectedListener { item -> onDrawerItemSelected(item) }
    }

    private fun onDrawerItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_stock -> startActivity(Intent(this, StockManagementActivity::class.java))
            R.id.nav_orders -> startActivity(Intent(this, OrdersActivity::class.java))
            R.id.nav_finance -> startActivity(Intent(this, FinanceActivity::class.java))
            R.id.nav_products -> startActivity(Intent(this, ProductsActivity::class.java))
            R.id.nav_employees -> startActivity(Intent(this, EmployeeManagementActivity::class.java))
            R.id.nav_reports -> startActivity(Intent(this, ReportsActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, AdminSettingsActivity::class.java))
            R.id.nav_logout -> confirmLogout()
        }
        return true
    }

    private fun setupStockList() {
        stockAdapter = StockSummaryAdapter(stockItems) { item ->
            val intent = Intent(this, AddStockItemActivity::class.java)
            intent.putExtra(AddStockItemActivity.EXTRA_ID, item.id)
            intent.putExtra(AddStockItemActivity.EXTRA_NAME, item.name)
            intent.putExtra(AddStockItemActivity.EXTRA_QUANTITY, item.quantity)
            intent.putExtra(AddStockItemActivity.EXTRA_UNIT, item.unit)
            intent.putExtra(AddStockItemActivity.EXTRA_PRICE, item.price)
            intent.putExtra(AddStockItemActivity.EXTRA_CATEGORY, item.category)
            intent.putExtra(AddStockItemActivity.EXTRA_MIN_QUANTITY, item.minQuantity)
            startActivity(intent)
        }
        binding.rvStockSummary.layoutManager = LinearLayoutManager(this)
        binding.rvStockSummary.adapter = stockAdapter
        binding.tvViewAllStock.setOnClickListener {
            startActivity(Intent(this, StockManagementActivity::class.java))
        }
    }

    private fun loadCounts() {
        val companyId = session.getCompanyId()
        FirebaseHelper.getEmployees(companyId, { employees ->
            binding.tvEmployeeCount.text = employees.size.toString()
        }, { binding.tvEmployeeCount.text = "0" })
        FirebaseHelper.getStockItems(companyId, { items ->
            binding.tvStockCount.text = items.size.toString()
            binding.tvLowStockCount.text = items.count { it.quantity <= it.minQuantity }.toString()
            stockItems.clear()
            stockItems.addAll(items.sortedBy { it.name.lowercase() })
            stockAdapter.notifyDataSetChanged()
            binding.tvEmptyStock.visibility = if (stockItems.isEmpty()) View.VISIBLE else View.GONE
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
        FirebaseHelper.signOut(this) {
            session.clear()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
