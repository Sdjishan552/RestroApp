package com.companyapp.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.R
import com.companyapp.databinding.ActivityAuthBinding
import com.companyapp.utils.FirebaseHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_CREATE = "create"
        const val MODE_JOIN = "join"
        private const val RC_SIGN_IN = 9001
        private const val TAG = "AuthActivity"
    }

    private lateinit var binding: ActivityAuthBinding
    private lateinit var googleClient: GoogleSignInClient
    private var mode = MODE_CREATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_CREATE
        setupUI()
        setupGoogle()
    }

    private fun setupUI() {
        if (mode == MODE_CREATE) {
            binding.tvTitle.text = "Create Your Company"
            binding.tvDescription.text = getString(R.string.auth_description_admin)
            binding.tvBadge.text = "ADMIN"
            binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_admin)
        } else {
            binding.tvTitle.text = "Join Your Company"
            binding.tvDescription.text = getString(R.string.auth_description_employee)
            binding.tvBadge.text = "EMPLOYEE"
            binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_employee)
        }
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSignInGoogle.setOnClickListener { signIn() }
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        setLoading(true)
        startActivityForResult(googleClient.signInIntent, RC_SIGN_IN)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                if (!FirebaseHelper.isGmailAddress(account.email)) {
                    setLoading(false)
                    googleClient.signOut()
                    Toast.makeText(this, getString(R.string.gmail_required), Toast.LENGTH_LONG).show()
                    return
                }
                firebaseAuth(account)
            } catch (e: ApiException) {
                setLoading(false)
                Log.e(TAG, "Google sign-in failed: statusCode=${e.statusCode}", e)
                Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuth(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseHelper.auth.signInWithCredential(credential)
            .addOnSuccessListener {
                setLoading(false)
                val next = if (mode == MODE_CREATE) {
                    Intent(this, CompanySetupActivity::class.java)
                } else {
                    Intent(this, JoinCompanyActivity::class.java)
                }
                startActivity(next)
                finish()
            }
            .addOnFailureListener { error ->
                setLoading(false)
                Log.e(TAG, "Firebase authentication failed", error)
                Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSignInGoogle.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.tvLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }
}