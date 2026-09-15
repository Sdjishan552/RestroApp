package com.companyapp.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.R
import com.companyapp.admin.AdminDashboardActivity
import com.companyapp.databinding.ActivityCompanySetupBinding
import com.companyapp.models.Company
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager
import com.companyapp.utils.ShareCodeGenerator

class CompanySetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompanySetupBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        val user = FirebaseHelper.auth.currentUser
        binding.tvGreeting.text = "Hello, ${user?.displayName?.split(" ")?.first() ?: "there"} 👋"

        // Check if already created company
        if (user != null) checkExisting(user.uid)

        binding.btnCreate.setOnClickListener {
            val name = binding.etCompanyName.text.toString().trim()
            if (name.isEmpty()) { binding.tilCompanyName.error = "Please enter your company name"; return@setOnClickListener }
            binding.tilCompanyName.error = null
            createCompany(name)
        }
    }

    private fun checkExisting(uid: String) {
        FirebaseHelper.getCompanyByOwnerId(uid,
            onSuccess = { company ->
                if (company != null) {
                    session.saveAdminSession(company.id, company.name)
                    goToDashboard()
                }
            }, onError = {}
        )
    }

    private fun createCompany(name: String) {
        setLoading(true)
        val user = FirebaseHelper.auth.currentUser ?: return
        val code = ShareCodeGenerator.generate()
        val company = Company(name = name, ownerId = user.uid, ownerEmail = user.email ?: "", shareCode = code)
        FirebaseHelper.createCompany(company,
            onSuccess = { id ->
                session.saveAdminSession(id, name)
                setLoading(false)
                showCode(code, name)
            },
            onError = {
                setLoading(false)
                Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showCode(code: String, companyName: String) {
        binding.formLayout.visibility = View.GONE
        binding.codeLayout.visibility = View.VISIBLE
        binding.tvCompanyNameDisplay.text = companyName
        binding.tvShareCode.text = code
        binding.btnCopyCode.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("share_code", code))
            Toast.makeText(this, getString(R.string.code_copied), Toast.LENGTH_SHORT).show()
        }
        binding.btnContinue.setOnClickListener { goToDashboard() }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }

    private fun setLoading(b: Boolean) {
        binding.btnCreate.isEnabled = !b
        binding.progressBar.visibility = if (b) View.VISIBLE else View.GONE
    }
}
