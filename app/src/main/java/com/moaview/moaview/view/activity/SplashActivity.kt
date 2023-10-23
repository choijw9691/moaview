package com.moaview.moaview.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.moaview.moaview.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) // 시간 0.5초 이후 실행
    }
}