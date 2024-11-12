package com.example.h2h

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import android.graphics.Color
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.adapter.PostAdapter
import com.example.h2h.models.CreatePost
import kotlin.collections.getValue


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postsList: MutableList<CreatePost>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.postInputLayout.setOnClickListener{
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)


        }


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
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.introduction.text = user.introduction
                    binding.username.text = user.name
                    // Load profile image and cover image using Picasso
                    Picasso.get().load(user.profileImageUrl).into(binding.avatarImage)
                    Picasso.get().load(user.profileImageUrl).into(binding.avatarImageView)
                    Picasso.get().load(user.coverImageUrl).into(binding.coverImage)

                    // Calculate age and set gender icon and color for viewAge
                    val age = calculateAge(user.dateOfBirth)
                    if (user.gender == "Nam") {
                        binding.viewGender.setImageResource(com.example.h2h.R.drawable.ic_male)
                        binding.viewAge.apply {
                            text = "$age+"
                            setTextColor(Color.parseColor("#06C8FF"))  // Correct hex code for blue
                        }
                    } else if (user.gender == "Nữ") {
                        binding.viewGender.setImageResource(com.example.h2h.R.drawable.ic_female)
                        binding.viewAge.apply {
                            text = "$age+"
                            setTextColor(Color.parseColor("#F65EBF"))  // Color for female
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateAge(dateOfBirth: String): Int {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val birthDate = LocalDate.parse(dateOfBirth, formatter)
        val currentDate = LocalDate.now()

        return Period.between(birthDate, currentDate).years
    }
}

