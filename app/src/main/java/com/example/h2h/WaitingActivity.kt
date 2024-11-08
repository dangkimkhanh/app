package com.example.h2h

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class WaitingActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val splashScreenDuration: Long = 2000 // thời gian chờ (2 giây)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        // Khởi tạo FirebaseAuth
        auth = Firebase.auth

        // Chạy coroutine để chuyển hướng sau thời gian chờ
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Người dùng đã đăng nhập, chuyển hướng đến MainActivity
                startActivity(Intent(this@WaitingActivity, MainActivity::class.java))
            } else {
                // Người dùng chưa đăng nhập, chuyển hướng đến LoginActivity
                startActivity(Intent(this@WaitingActivity, LoginActivity::class.java))
            }
            finish() // Kết thúc SplashActivity để người dùng không thể quay lại
        }, splashScreenDuration)
    }
}
