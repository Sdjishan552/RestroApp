package com.companyapp.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.R
import com.companyapp.databinding.ActivityAddStockItemBinding
import com.companyapp.models.StockItem
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class AddStockItemActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_NAME = "name"
        const val EXTRA_QUANTITY = "quantity"
        const val EXTRA_UNIT = "unit"
        const val EXTRA_PRICE = "price"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_MIN_QUANTITY = "min_quantity"
    }

    private lateinit var binding: ActivityAddStockItemBinding
    private lateinit var session: SessionManager
    private var itemId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStockItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveItem() }
        loadExtras()
    }

    private fun loadExtras() {
        itemId = intent.getStringExtra(EXTRA_ID).orEmpty()
        if (itemId.isNotEmpty()) binding.tvTitle.text = "Edit Stock Item"
        binding.etItemName.setText(intent.getStringExtra(EXTRA_NAME).orEmpty())
        binding.etQuantity.setText(intent.getDoubleExtra(EXTRA_QUANTITY, 0.0).takeIf { it > 0.0 }?.toString().orEmpty())
        binding.etUnit.setText(intent.getStringExtra(EXTRA_UNIT).orEmpty())
        binding.etPrice.setText(intent.getDoubleExtra(EXTRA_PRICE, 0.0).takeIf { it > 0.0 }?.toString().orEmpty())
        binding.etCategory.setText(intent.getStringExtra(EXTRA_CATEGORY).orEmpty())
        binding.etMinQuantity.setText(intent.getDoubleExtra(EXTRA_MIN_QUANTITY, 5.0).toString())
    }

    private fun saveItem() {
        val name = binding.etItemName.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().toDoubleOrNull()
        val unit = binding.etUnit.text.toString().trim()
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        val category = binding.etCategory.text.toString().trim()
        val minQuantity = binding.etMinQuantity.text.toString().toDoubleOrNull() ?: 5.0

        var valid = true
        binding.tilItemName.error = null
        binding.tilQuantity.error = null
        binding.tilUnit.error = null
        binding.tilPrice.error = null

        if (name.isEmpty()) {
            binding.tilItemName.error = "Required"
            valid = false
        }
        if (quantity == null || quantity < 0.0) {
            binding.tilQuantity.error = "Enter a valid quantity"
            valid = false
        }
        if (unit.isEmpty()) {
            binding.tilUnit.error = "Required"
            valid = false
        }
        if (price == null || price < 0.0) {
            binding.tilPrice.error = "Enter a valid price"
            valid = false
        }
        if (!valid) return

        setLoading(true)
        val item = StockItem(
            id = itemId,
            name = name,
            quantity = quantity ?: 0.0,
            unit = unit,
            price = price ?: 0.0,
            category = category.ifEmpty { "General" },
            minQuantity = minQuantity
        )
        FirebaseHelper.saveStockItem(session.getCompanyId(), item,
            onSuccess = {
                setLoading(false)
                Toast.makeText(this, getString(R.string.item_saved), Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = {
                setLoading(false)
                Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSave.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
