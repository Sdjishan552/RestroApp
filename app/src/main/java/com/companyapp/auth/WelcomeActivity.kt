package com.companyapp.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.companyapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animateViews()
        setupClicks()
    }

    private fun animateViews() {
        val views = listOf(binding.ivLogo, binding.tvAppName, binding.tvTagline, binding.cardButtons)
        views.forEach { it.alpha = 0f }
        binding.ivLogo.scaleX = 0.3f
        binding.ivLogo.scaleY = 0.3f

        val anim = AnimatorSet()
        anim.playSequentially(
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 0f, 1f).setDuration(500),
                    ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_X, 0.3f, 1f).setDuration(600),
                    ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_Y, 0.3f, 1f).setDuration(600)
                )
            },
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f).setDuration(400),
                    ObjectAnimator.ofFloat(binding.tvTagline, View.ALPHA, 0f, 1f).setDuration(400)
                )
            },
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.cardButtons, View.ALPHA, 0f, 1f).setDuration(500),
                    ObjectAnimator.ofFloat(binding.cardButtons, View.TRANSLATION_Y, 80f, 0f).setDuration(500)
                )
            }
        )
        anim.start()
    }

    private fun setupClicks() {
        binding.btnCreateCompany.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java).apply {
                putExtra(AuthActivity.EXTRA_MODE, AuthActivity.MODE_CREATE)
            })
        }
        binding.btnJoinCompany.setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java).apply {
                putExtra(AuthActivity.EXTRA_MODE, AuthActivity.MODE_JOIN)
            })
        }
    }
}
