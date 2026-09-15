package com.companyapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.R
import com.companyapp.databinding.ActivityJoinCompanyBinding
import com.companyapp.employee.EmployeeDashboardActivity
import com.companyapp.models.Employee
import com.companyapp.utils.FirebaseHelper
import com.companyapp.utils.SessionManager

class JoinCompanyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJoinCompanyBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinCompanyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        val user = FirebaseHelper.auth.currentUser
        binding.tvGreeting.text = "Hello, ${user?.displayName?.split(" ")?.first() ?: "there"} 👋"
        binding.tvEmail.text = user?.email ?: ""
        binding.btnJoin.setOnClickListener {
            val code = binding.etShareCode.text.toString().trim().uppercase()
            if (code.length != 6) { binding.tilShareCode.error = "Enter a valid 6-character code"; return@setOnClickListener }
            binding.tilShareCode.error = null
            joinWith(code)
        }
    }

    private fun joinWith(code: String) {
        setLoading(true)
        val user = FirebaseHelper.auth.currentUser ?: return
        FirebaseHelper.getCompanyByShareCode(code,
            onSuccess = { company ->
                if (company == null) {
                    setLoading(false)
                    binding.tilShareCode.error = getString(R.string.invalid_code)
                    return@getCompanyByShareCode
                }
                FirebaseHelper.checkEmployeeAuthorized(company.id, user.email ?: "") { authorized, existing ->
                    if (!authorized) {
                        setLoading(false)
                        Toast.makeText(this, "Your email is not authorized. Contact your admin.", Toast.LENGTH_LONG).show()
                        return@checkEmployeeAuthorized
                    }
                    val updated = Employee(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString() ?: "",
                        permissions = existing?.permissions ?: com.companyapp.models.Permissions()
                    )
                    FirebaseHelper.markEmployeeJoined(company.id, updated,
                        onSuccess = {
                            session.saveEmployeeSession(company.id, company.name)
                            setLoading(false)
                            startActivity(Intent(this, EmployeeDashboardActivity::class.java))
                            finish()
                        },
                        onError = {
                            setLoading(false)
                            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            onError = {
                setLoading(false)
                Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setLoading(b: Boolean) {
        binding.btnJoin.isEnabled = !b
        binding.progressBar.visibility = if (b) View.VISIBLE else View.GONE
    }
}
