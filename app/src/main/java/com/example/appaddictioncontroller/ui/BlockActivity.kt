package com.example.appaddictioncontroller.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.appaddictioncontroller.databinding.ActivityBlockBinding
import java.util.concurrent.TimeUnit

class BlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockBinding
    private val handler = Handler(Looper.getMainLooper())
    private var blockUntil: Long = 0L

    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            updateTimer()
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appName = intent.getStringExtra("APP_NAME") ?: "This app"
        blockUntil = intent.getLongExtra("BLOCK_UNTIL", 0L)

        binding.appNameText.text = appName
        binding.closeButton.setOnClickListener {
            finish()
        }

        // Prevent back button navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - prevent user from going back
            }
        })

        // Start timer updates
        handler.post(updateTimerRunnable)
    }

    private fun updateTimer() {
        val currentTime = System.currentTimeMillis()
        val remainingTime = blockUntil - currentTime

        if (remainingTime <= 0) {
            // Block period is over
            finish()
            return
        }

        val hours = TimeUnit.MILLISECONDS.toHours(remainingTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60

        binding.timeRemainingText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimerRunnable)
    }
}

