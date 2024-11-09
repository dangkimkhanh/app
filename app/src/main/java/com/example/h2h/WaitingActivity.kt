package com.example.h2h

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class WaitingActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        // Khởi tạo FirebaseAuth
        auth = Firebase.auth

        // Kiểm tra kết nối mạng
        if (!isNetworkConnected()) {
            showNoInternetDialog()
            return // Dừng lại nếu không có kết nối mạng
        }

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
        }, 2000) // Thời gian chờ 2 giây
    }

    // Hàm kiểm tra kết nối mạng
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // Hàm hiển thị thông báo mất kết nối
    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Mất kết nối Internet")
            .setMessage("Vui lòng kết nối Wi-Fi hoặc dữ liệu di động.")
            .setPositiveButton("OK") { _, _ ->
                finishAffinity() // Đóng ứng dụng
            }
            .setCancelable(false) // Không cho phép người dùng đóng dialog bằng cách bấm ra ngoài
            .show()
    }
}