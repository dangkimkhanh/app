package com.example.h2h.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.R
import com.example.h2h.Scrollable

class FeedFragment : Fragment(), Scrollable {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }
    //gây lỗi đóng tap
    private lateinit var recyclerView: RecyclerView
    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }
}