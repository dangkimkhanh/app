package com.example.h2h

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.glance.layout.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.adapter.PostAdapter
import com.example.h2h.databinding.ActivityProfileBinding
import com.example.h2h.models.CreatePost
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var postAdapter: PostAdapter
    private lateinit var postsList: MutableList<CreatePost>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProfileActivity", "onCreate() called")
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        postsList = mutableListOf()

        // Set up RecyclerView for posts
        postAdapter = PostAdapter(postsList)
        binding.postsRecyclerProfile.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = postAdapter
        }

        // Toolbar setup
        setSupportActionBar(binding.toolbarprofile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // Load user data from Firebase
        loadUserData()

        // Load posts from Firebase
        loadPosts()

        // Set up button for creating a new post
        binding.postInputLayout.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }
        binding.editProfile.setOnClickListener {
            val edtBottomSheet = EditProfileBtn()
            edtBottomSheet.show(supportFragmentManager, "AvtBottomSheet")
        }

        // Back button handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    // Load user data from Firebase
    private fun loadUserData() {
        val userRef = database.reference.child("users").child(auth.currentUser?.uid ?: "")
        userRef.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.introduction.text = user.introduction
                    binding.username.text = user.name
                    Picasso.get().load(user.profileImageUrl).into(binding.avatarImageView)
                    Picasso.get().load(user.profileImageUrl).into(binding.avatarImage)
                    Picasso.get().load(user.coverImageUrl).into(binding.coverImage)
                    setupGenderAge(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Failed to read user data.", error.toException())
                Toast.makeText(this@ProfileActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupGenderAge(user: User) {
        val age = calculateAge(user.dateOfBirth)
        if (user.gender == "Nam") {
            binding.viewGender.setImageResource(com.example.h2h.R.drawable.ic_male)
            binding.viewAge.apply {
                text = "$age+"
                setTextColor(Color.parseColor("#06C8FF"))
            }
        } else if (user.gender == "Nữ") {
            binding.viewGender.setImageResource(com.example.h2h.R.drawable.ic_female)
            binding.viewAge.apply {
                text = "$age+"
                setTextColor(Color.parseColor("#F65EBF"))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAge(dateOfBirth: String): Int {
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd/M/yyyy")
        )
        for (formatter in formatters) {
            try {
                val birthDate = LocalDate.parse(dateOfBirth, formatter)
                val currentDate = LocalDate.now()
                return Period.between(birthDate, currentDate).years
            } catch (e: DateTimeParseException) {
                // Ignore exception and try the next formatter
            }
        }
        Log.e("ProfileActivity", "Error parsing date of birth: $dateOfBirth")
        return 0
    }

    // Load posts from Firebase and sort by newest first
    private fun loadPosts() {
        val userId = auth.currentUser?.uid ?: return // Kiểm tra userId, nếu null thì thoát hàm
        val postsRef = database.reference.child("posts")
        val query = postsRef.orderByChild("userId").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ProfileActivity", "DataSnapshot: $snapshot")
                postsList.clear()

                Log.d("ProfileActivity", "Number of posts loaded: ${snapshot.childrenCount}")
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(CreatePost::class.java)
                    Log.d("ProfileActivity", "Post loaded: $post") // Log thông tin bài đăng
                    post?.let { postsList.add(it) }
                }

                // Sort posts by timestamp in descending order (newest first)
                postsList.sortByDescending { it.timestamp }

                Log.d("ProfileActivity", "Posts list size: ${postsList.size}") // Thêm đoạn log này
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Failed to load posts.", error.toException())
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}