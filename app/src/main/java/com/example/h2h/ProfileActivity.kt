package com.example.h2h

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.h2h.databinding.ActivityProfileBinding
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập Toolbar
        setSupportActionBar(binding.toolbarprofile)

        // Hiển thị nút quay lại (up button) trên Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Cài đặt WindowInsets để xử lý khoảng cách padding cho hệ thống điều hướng
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Xử lý sự kiện khi người dùng bấm vào nút quay lại
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Quay lại trang trước
            }
        })
        // Xử lý sự kiện khi người dùng bấm vào ảnh đại diện
        val avatarImage = findViewById<CircleImageView>(R.id.avatar_image)
        avatarImage.setOnClickListener {
            val avtBottomSheet = AvtBottomSheet()
            avtBottomSheet.show(supportFragmentManager, "AvtBottomSheet")
        }
        val coverImage = findViewById<ImageView>(R.id.cover_image)
        coverImage.setOnClickListener {
            val coverBottomSheet = CoverAvtBottomSheet()
            coverBottomSheet.show(supportFragmentManager, "CoverBottomSheet")
        }
    }

    // Xử lý sự kiện khi người dùng bấm vào nút quay lại trên Toolbar
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // Quay lại trang trước
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}