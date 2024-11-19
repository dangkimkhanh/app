package com.example.h2h.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.R
import com.example.h2h.Scrollable
import com.example.h2h.adapter.FeedPostAdapter

class FeedFragment : Fragment(), Scrollable {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedPostAdapter: FeedPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)
        recyclerView = view.findViewById(R.id.recyclerView_feed_post)
        recyclerView.layoutManager = LinearLayoutManager(context)
        feedPostAdapter = FeedPostAdapter(requireContext())
        recyclerView.adapter = feedPostAdapter

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

        return view
    }

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }
}