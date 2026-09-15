package com.companyapp.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.R
import com.companyapp.auth.WelcomeActivity
import com.companyapp.databinding.ActivityAdminSettingsBinding
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class AdminSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminSettingsBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCopyCode.setOnClickListener { copyShareCode() }
        binding.btnLogout.setOnClickListener { confirmLogout() }
        loadCompany()
    }

    private fun loadCompany() {
        binding.tvCompanyName.text = session.getCompanyName()
        binding.tvOwnerEmail.text = FirebaseHelper.auth.currentUser?.email ?: ""
        binding.progressBar.visibility = View.VISIBLE
        FirebaseHelper.getCompany(session.getCompanyId(),
            onSuccess = { company ->
                binding.progressBar.visibility = View.GONE
                binding.tvShareCode.text = company?.shareCode ?: "------"
            },
            onError = {
                binding.progressBar.visibility = View.GONE
                binding.tvShareCode.text = "------"
            }
        )
    }

    private fun copyShareCode() {
        val code = binding.tvShareCode.text.toString()
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("share_code", code))
        Toast.makeText(this, getString(R.string.code_copied), Toast.LENGTH_SHORT).show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(getString(R.string.logout)) { _, _ -> logout() }
            .setNegativeButton(getString(R.string.cancel), null)
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
