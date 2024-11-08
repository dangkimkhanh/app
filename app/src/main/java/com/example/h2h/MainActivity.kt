package com.example.h2h

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.h2h.Fragment.AccountFragment
import com.example.h2h.Fragment.ChatFragment
import com.example.h2h.Fragment.FeedFragment
import com.example.h2h.Fragment.FragmentTest
import com.example.h2h.Fragment.MapsFragment
import com.example.h2h.Fragment.PostFragment
import com.example.h2h.databinding.ActivityMainBinding
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Thay thế fragment ban đầu
        replaceFragment(FragmentTest(), false) // Không thêm vào back stack ban đầu

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_feed -> replaceFragment(FeedFragment())
                R.id.navigation_map -> replaceFragment(MapsFragment())
                R.id.navigation_profile -> replaceFragment(AccountFragment())
                R.id.navigation_post -> replaceFragment(PostFragment())
                R.id.navigation_message -> replaceFragment(ChatFragment())
                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null) // Thêm vào back stack nếu cần
        }
        fragmentTransaction.commit()
    }
}

