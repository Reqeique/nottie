package com.ultraone.nottie

import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.ultraone.nottie.databinding.ActivitySpashScreenBinding


class SplashScreenActivity : AppCompatActivity() {
    lateinit var binding: ActivitySpashScreenBinding
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        },1000)
    }

}//MaterialDatePicker requires a value for the com.ultraone