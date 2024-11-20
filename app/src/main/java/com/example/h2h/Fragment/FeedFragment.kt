package com.example.h2h.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.h2h.CreatePostActivity
import com.example.h2h.MainActivity
import com.example.h2h.R
import com.example.h2h.adapter.FeedPostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class FeedFragment : Fragment(), MainActivity.Scrollable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedPostAdapter: FeedPostAdapter
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var createPost: LinearLayout
    private lateinit var viewAvatar: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)
        val userRef = database.reference.child("users").child(auth.currentUser?.uid ?: "")
        recyclerView = view.findViewById(R.id.recyclerView_feed_post)
        createPost = view.findViewById(R.id.craete_post_layout)
        viewAvatar = view.findViewById(R.id.avatarImageView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        feedPostAdapter = FeedPostAdapter(requireContext())
        recyclerView.adapter = feedPostAdapter
        feedPostAdapter.loadMorePosts()
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val imageUrl = dataSnapshot.child("profileImageUrl").value.toString()
            Picasso.get().load(imageUrl).into(viewAvatar)
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && !feedPostAdapter.isLoading) {
                    feedPostAdapter.loadMorePosts()
                }
            }
        })
        createPost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }
}