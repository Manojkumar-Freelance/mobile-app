package com.example.appaddictioncontroller.ui

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.appaddictioncontroller.databinding.ActivityBlockBinding

class BlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appName = intent.getStringExtra("APP_NAME") ?: "App"
        val blockUntil = intent.getLongExtra("BLOCK_UNTIL", 0L)
        val remainingTime = blockUntil - System.currentTimeMillis()
        
        binding.blockTitle.text = "Time to Pause $appName"
        
        if (remainingTime > 0) {
            startTimer(remainingTime)
        } else {
            binding.timerText.text = "See you tomorrow"
        }
        
        binding.closeAppButton.setOnClickListener {
            finishAffinity() // Close app completely
        }
        
        onBackPressedDispatcher.addCallback(this) {
            // Disable back button
        }
    }
    
    private fun startTimer(durationMillis: Long) {
        val timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.timerText.text = "00:00"
                finish() // Unblock
            }
        }
        timer.start()
    }
}
