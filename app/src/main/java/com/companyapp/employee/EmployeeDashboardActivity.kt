package com.companyapp.employee

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.companyapp.R
import com.companyapp.adapter.StockSummaryAdapter
import com.companyapp.admin.AddStockItemActivity
import com.companyapp.admin.FinanceActivity
import com.companyapp.admin.OrdersActivity
import com.companyapp.admin.ProductsActivity
import com.companyapp.admin.ReportsActivity
import com.companyapp.admin.StockManagementActivity
import com.companyapp.auth.WelcomeActivity
import com.companyapp.databinding.ActivityEmployeeDashboardBinding
import com.companyapp.models.Permissions
import com.companyapp.models.StockItem
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class EmployeeDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeDashboardBinding
    private lateinit var session: SessionManager
    private val stockItems = mutableListOf<StockItem>()
    private lateinit var stockAdapter: StockSummaryAdapter
    private var permissions = Permissions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setupToolbarAndDrawer()
        setupStockList()
        loadPermissions()
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.tvCompanyName.text = session.getCompanyName()

        val nav = binding.navView
        val headerView = nav.getHeaderView(0)
        val user = FirebaseHelper.auth.currentUser
        headerView.findViewById<TextView>(R.id.tvWelcome).text =
            "Hello, ${user?.displayName?.split(" ")?.firstOrNull() ?: "there"}"
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
            R.id.nav_reports -> startActivity(Intent(this, ReportsActivity::class.java))
            R.id.nav_logout -> confirmLogout()
        }
        return true
    }

    private fun setupStockList() {
        stockAdapter = StockSummaryAdapter(stockItems) { item ->
            if (!permissions.stock) return@StockSummaryAdapter
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
    }

    private fun loadPermissions() {
        val user = FirebaseHelper.auth.currentUser ?: return
        FirebaseHelper.checkEmployeeAuthorized(session.getCompanyId(), user.email ?: "") { authorized, employee ->
            if (!authorized || employee == null) {
                Toast.makeText(this, "Access revoked. Contact your admin.", Toast.LENGTH_LONG).show()
                logout()
                return@checkEmployeeAuthorized
            }
            permissions = employee.permissions
            applyPermissionsToMenu()

            val anyVisible = permissions.stock || permissions.orders || permissions.finance ||
                permissions.products || permissions.reports
            binding.tvNoAccess.visibility = if (anyVisible) View.GONE else View.VISIBLE

            if (permissions.stock) {
                binding.stockSection.visibility = View.VISIBLE
                loadStock()
            } else {
                binding.stockSection.visibility = View.GONE
            }
        }
    }

    private fun applyPermissionsToMenu() {
        val menu = binding.navView.menu
        menu.findItem(R.id.nav_stock)?.isVisible = permissions.stock
        menu.findItem(R.id.nav_orders)?.isVisible = permissions.orders
        menu.findItem(R.id.nav_finance)?.isVisible = permissions.finance
        menu.findItem(R.id.nav_products)?.isVisible = permissions.products
        menu.findItem(R.id.nav_reports)?.isVisible = permissions.reports
    }

    private fun loadStock() {
        FirebaseHelper.getStockItems(session.getCompanyId(),
            onSuccess = { list ->
                stockItems.clear()
                stockItems.addAll(list.sortedBy { it.name.lowercase() })
                stockAdapter.notifyDataSetChanged()
                binding.tvEmptyStock.visibility = if (stockItems.isEmpty()) View.VISIBLE else View.GONE
            },
            onError = {}
        )
    }

    override fun onResume() {
        super.onResume()
        if (::session.isInitialized) loadPermissions()
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
        FirebaseHelper.signOut(this) {
            session.clear()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
