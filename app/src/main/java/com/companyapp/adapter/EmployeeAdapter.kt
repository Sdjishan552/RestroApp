package com.companyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.companyapp.databinding.ItemEmployeeBinding
import com.companyapp.models.Employee
import com.companyapp.models.Permissions

class EmployeeAdapter(
    private val employees: List<Employee>,
    private val onPermissionsChanged: (Employee, Permissions) -> Unit,
    private val onRemove: (Employee) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.VH>() {

    inner class VH(val binding: ItemEmployeeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEmployeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = employees.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val emp = employees[position]
        val b = holder.binding

        b.tvEmployeeName.text = emp.displayName.ifEmpty { emp.email }
        b.tvEmployeeEmail.text = emp.email
        b.tvJoinStatus.text = if (emp.uid.isEmpty()) "Pending" else "Active"
        b.tvJoinStatus.setBackgroundResource(
            if (emp.uid.isEmpty()) com.companyapp.R.drawable.bg_badge_admin
            else com.companyapp.R.drawable.bg_badge_employee
        )

        // Remove listeners before setting values to avoid cascade triggers
        b.switchStock.setOnCheckedChangeListener(null)
        b.switchOrders.setOnCheckedChangeListener(null)
        b.switchFinance.setOnCheckedChangeListener(null)
        b.switchProducts.setOnCheckedChangeListener(null)
        b.switchReports.setOnCheckedChangeListener(null)

        b.switchStock.isChecked = emp.permissions.stock
        b.switchOrders.isChecked = emp.permissions.orders
        b.switchFinance.isChecked = emp.permissions.finance
        b.switchProducts.isChecked = emp.permissions.products
        b.switchReports.isChecked = emp.permissions.reports

        val savePerms = {
            val perms = Permissions(
                stock = b.switchStock.isChecked,
                orders = b.switchOrders.isChecked,
                finance = b.switchFinance.isChecked,
                products = b.switchProducts.isChecked,
                reports = b.switchReports.isChecked
            )
            onPermissionsChanged(emp, perms)
        }

        b.switchStock.setOnCheckedChangeListener { _, _ -> savePerms() }
        b.switchOrders.setOnCheckedChangeListener { _, _ -> savePerms() }
        b.switchFinance.setOnCheckedChangeListener { _, _ -> savePerms() }
        b.switchProducts.setOnCheckedChangeListener { _, _ -> savePerms() }
        b.switchReports.setOnCheckedChangeListener { _, _ -> savePerms() }

        b.btnRemove.setOnClickListener { onRemove(emp) }
    }
}
