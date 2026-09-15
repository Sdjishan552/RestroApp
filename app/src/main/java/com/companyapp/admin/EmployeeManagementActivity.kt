package com.companyapp.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.companyapp.R
import com.companyapp.adapter.EmployeeAdapter
import com.companyapp.databinding.ActivityEmployeeManagementBinding
import com.companyapp.models.Employee
import com.companyapp.models.Permissions
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class EmployeeManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeManagementBinding
    private lateinit var session: SessionManager
    private val employees = mutableListOf<Employee>()
    private lateinit var adapter: EmployeeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        adapter = EmployeeAdapter(
            employees,
            onPermissionsChanged = { employee, permissions -> updatePermissions(employee, permissions) },
            onRemove = { employee -> confirmRemove(employee) }
        )
        binding.rvEmployees.layoutManager = LinearLayoutManager(this)
        binding.rvEmployees.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddEmployee.setOnClickListener { addEmployee() }
        loadEmployees()
    }

    private fun addEmployee() {
        val email = binding.etEmployeeEmail.text.toString().trim().lowercase()
        if (!FirebaseHelper.isGmailAddress(email)) {
            binding.tilEmployeeEmail.error = getString(R.string.gmail_required)
            return
        }
        binding.tilEmployeeEmail.error = null
        setLoading(true)
        val employee = Employee(email = email, permissions = currentPermissions())
        FirebaseHelper.addEmployee(session.getCompanyId(), employee,
            onSuccess = {
                setLoading(false)
                binding.etEmployeeEmail.setText("")
                Toast.makeText(this, getString(R.string.employee_added), Toast.LENGTH_SHORT).show()
                loadEmployees()
            },
            onError = {
                setLoading(false)
                Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun currentPermissions() = Permissions(
        stock = binding.switchStock.isChecked,
        orders = binding.switchOrders.isChecked,
        finance = binding.switchFinance.isChecked,
        products = binding.switchProducts.isChecked,
        reports = binding.switchReports.isChecked
    )

    private fun loadEmployees() {
        binding.progressBar.visibility = View.VISIBLE
        FirebaseHelper.getEmployees(session.getCompanyId(),
            onSuccess = { list ->
                binding.progressBar.visibility = View.GONE
                employees.clear()
                employees.addAll(list.sortedBy { it.email })
                adapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (employees.isEmpty()) View.VISIBLE else View.GONE
            },
            onError = {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updatePermissions(employee: Employee, permissions: Permissions) {
        FirebaseHelper.updateEmployeePermissions(session.getCompanyId(), employee.email, permissions,
            onSuccess = {},
            onError = { Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show() }
        )
    }

    private fun confirmRemove(employee: Employee) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.remove_employee))
            .setMessage(getString(R.string.confirm_remove))
            .setPositiveButton(getString(R.string.delete)) { _, _ -> removeEmployee(employee) }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun removeEmployee(employee: Employee) {
        FirebaseHelper.removeEmployee(session.getCompanyId(), employee.email,
            onSuccess = {
                Toast.makeText(this, getString(R.string.employee_removed), Toast.LENGTH_SHORT).show()
                loadEmployees()
            },
            onError = { Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show() }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.btnAddEmployee.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
