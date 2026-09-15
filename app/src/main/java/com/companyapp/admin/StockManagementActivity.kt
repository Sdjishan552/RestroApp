package com.companyapp.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.companyapp.R
import com.companyapp.adapter.StockAdapter
import com.companyapp.databinding.ActivityStockManagementBinding
import com.companyapp.models.StockItem
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class StockManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStockManagementBinding
    private lateinit var session: SessionManager
    private val items = mutableListOf<StockItem>()
    private lateinit var adapter: StockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        adapter = StockAdapter(items, onEdit = { openEditor(it) }, onDelete = { confirmDelete(it) })
        binding.rvStock.layoutManager = LinearLayoutManager(this)
        binding.rvStock.adapter = adapter
        binding.btnBack.setOnClickListener { finish() }
        binding.fabAdd.setOnClickListener { openEditor(null) }
    }

    override fun onResume() {
        super.onResume()
        loadStock()
    }

    private fun loadStock() {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseHelper.getStockItems(session.getCompanyId(),
            onSuccess = { list ->
                binding.progressBar.visibility = View.GONE
                items.clear()
                items.addAll(list.sortedBy { it.name.lowercase() })
                adapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.tvTotalItems.text = items.size.toString()
                binding.tvLowStock.text = items.count { it.quantity <= it.minQuantity }.toString()
                binding.tvStockValue.text = "INR %.2f".format(items.sumOf { it.quantity * it.price })
            },
            onError = {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun openEditor(item: StockItem?) {
        val intent = Intent(this, AddStockItemActivity::class.java)
        item?.let {
            intent.putExtra(AddStockItemActivity.EXTRA_ID, it.id)
            intent.putExtra(AddStockItemActivity.EXTRA_NAME, it.name)
            intent.putExtra(AddStockItemActivity.EXTRA_QUANTITY, it.quantity)
            intent.putExtra(AddStockItemActivity.EXTRA_UNIT, it.unit)
            intent.putExtra(AddStockItemActivity.EXTRA_PRICE, it.price)
            intent.putExtra(AddStockItemActivity.EXTRA_CATEGORY, it.category)
            intent.putExtra(AddStockItemActivity.EXTRA_MIN_QUANTITY, it.minQuantity)
        }
        startActivity(intent)
    }

    private fun confirmDelete(item: StockItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete item")
            .setMessage("Delete ${item.name} from stock?")
            .setPositiveButton(getString(R.string.delete)) { _, _ -> deleteItem(item) }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteItem(item: StockItem) {
        FirebaseHelper.deleteStockItem(session.getCompanyId(), item.id,
            onSuccess = {
                Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show()
                loadStock()
            },
            onError = { Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show() }
        )
    }
}
