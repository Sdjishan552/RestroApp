package com.companyapp.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.R
import com.companyapp.databinding.ActivityComingSoonBinding
import com.companyapp.utils.SessionManager

open class ModulePlaceholderActivity : AppCompatActivity() {
    protected open val moduleTitle: String = "Module"
    protected open val moduleDescription: String = "This module is ready for access control and can be expanded with business data next."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityComingSoonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val session = SessionManager(this)
        binding.tvCompanyName.text = session.getCompanyName()
        binding.tvTitle.text = moduleTitle
        binding.tvDescription.text = moduleDescription
        binding.btnBack.setOnClickListener { finish() }
    }
}

class OrdersActivity : ModulePlaceholderActivity() {
    override val moduleTitle = "Orders"
    override val moduleDescription = "Track order activity and make it visible only to employees with Orders access."
}

class FinanceActivity : ModulePlaceholderActivity() {
    override val moduleTitle = "Finance"
    override val moduleDescription = "Keep finance information limited to the owner and approved finance employees."
}

class ProductsActivity : ModulePlaceholderActivity() {
    override val moduleTitle = "Products"
    override val moduleDescription = "Manage the catalog your team uses across stock, orders and reports."
}

class ReportsActivity : ModulePlaceholderActivity() {
    override val moduleTitle = "Reports"
    override val moduleDescription = "View business summaries after stock, orders and finance data are connected."
}
