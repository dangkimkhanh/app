package com.example.h2h

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.with
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.h2h.adapter.PostAdapter
import com.example.h2h.models.CreatePost
import com.example.h2h.models.Relationship
import com.example.h2h.models.User
import com.example.h2h.services.RelationshipHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class UserProfileActivity : AppCompatActivity() {
    private lateinit var currentUserId: String
    private lateinit var otherUserId: String
    private lateinit var relationshipHelper: RelationshipHelper
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postsList = mutableListOf<CreatePost>()
    private lateinit var toolbarTitle: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var startChatTextView: TextView
    private lateinit var userStatusTextView: TextView
    private lateinit var userCoverImageView: ImageView
    private lateinit var userAvatarImageView: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_user)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        toolbarTitle = findViewById(R.id.uer_toolbar_title)
        userNameTextView = findViewById(R.id.user_name_view)
        userStatusTextView = findViewById(R.id.text_status)
        userCoverImageView = findViewById(R.id.cover_image)
        startChatTextView = findViewById(R.id.star_chat)
        userAvatarImageView = findViewById(R.id.user_avatar_image)
        setSupportActionBar(findViewById(R.id.toolbar_user_profile))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        currentUserId = intent.getStringExtra("CURRENT_USER_ID") ?: ""
        otherUserId = intent.getStringExtra("OTHER_USER_ID") ?: ""
        startChatTextView.setOnClickListener{
             intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("OTHER_USER_ID", otherUserId)
            startActivity(intent)

        }
        val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(otherUserId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    toolbarTitle.text = user.name
                    userNameTextView.text = user.name
                    userStatusTextView.text = user.introduction
                    Picasso.get().load(user.coverImageUrl).into(userCoverImageView) // Sử dụng Picasso để load ảnh bìa
                    userAvatarImageView.post {
                        Picasso.get().load(user.profileImageUrl).into(userAvatarImageView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })
        val relationshipsRef = FirebaseDatabase.getInstance().reference.child("relationships")
        relationshipsRef
            .orderByChild("userId1")
            .equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val relationship = snapshot.getValue(Relationship::class.java)
                        if (relationship?.userId2 == otherUserId) {
                            val relationshipId = snapshot.key // Lấy relationshipId

                            // Khởi tạo RelationshipHelper với relationshipId
                            relationshipHelper = RelationshipHelper(currentUserId, otherUserId)

                            // Cập nhật UI với RelationshipHelper
                            val relationshipComposeView = findViewById<ComposeView>(R.id.relationship_compose_view)
                            relationshipComposeView.setContent {
                                relationshipHelper.HandleRelationshipClick()
                            }

                            return // Thoát khỏi vòng lặp khi tìm thấy relationshipId
                        }
                    }

                    // Nếu không tìm thấy relationshipId, tạo mới
                    val newRelationshipId = relationshipsRef.push().key ?: ""
                    relationshipHelper = RelationshipHelper(currentUserId, otherUserId)

                    // Cập nhật UI với RelationshipHelper
                    val relationshipComposeView = findViewById<ComposeView>(R.id.relationship_compose_view)
                    relationshipComposeView.setContent {
                        relationshipHelper.HandleRelationshipClick()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Xử lý lỗi
                }
            })
        // Lấy bài đăng của người dùng khác
        postsRecyclerView = findViewById(R.id.posts_recycler_view)
        postAdapter = PostAdapter(postsList)
        postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserProfileActivity)
            adapter = postAdapter
        }
        val postsRef = FirebaseDatabase.getInstance().reference.child("posts")
        val query = postsRef.orderByChild("userId").equalTo(otherUserId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postsList.clear()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(CreatePost::class.java)
                    post?.let { postsList.add(it) }
                }
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
