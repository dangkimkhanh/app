package com.example.h2h

import android.R
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.get
import com.example.h2h.databinding.ActivityProfileBinding
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.getValue

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Authentication và Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

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

        // Truy vấn dữ liệu người dùng từ Firebase
        val userRef = database.reference.child("users").child(auth.currentUser?.uid ?: "")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    // Hiển thị dữ liệu lên các view
                    binding.username.text = user.name
                    binding.introduction.text = user.introduction

                    // Tải ảnh đại diện và ảnh bìa bằng Picasso, thay đổi kích thước và giảm chất lượn

                    Picasso.get()
                        .load(user.profileImageUrl)
                        .into(binding.avatarImageView)
                    Picasso.get()
                        .load(user.profileImageUrl)
                        .into(binding.avatarImage)

                    Picasso.get()
                        .load(user.coverImageUrl)
                        .into(binding.coverImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
                Log.e("ProfileActivity", "Failed to read user data.", error.toException())
                Toast.makeText(this@ProfileActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        })

        // Xử lý sự kiện khi người dùng bấm vào sửa thông tin
        binding.editProfile.setOnClickListener {
            val edtBottomSheet = EditProfileBtn()
            edtBottomSheet.show(supportFragmentManager, "AvtBottomSheet")
        }

    }

    // Xử lý sự kiện khi người dùng bấm vào nút quay lại trên Toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                finish() // Quay lại trang trước
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}