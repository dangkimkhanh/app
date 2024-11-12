package com.example.h2h

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import com.example.h2h.Fragment.*
import com.example.h2h.databinding.ActivityMainBinding
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragments: MutableList<Fragment> = mutableListOf()
    private lateinit var auth: FirebaseAuth // Khai báo auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Initialize and add fragments to the list
        fragments.add(FeedFragment())
        fragments.add(MapsFragment())
        fragments.add(AccountFragment())
        fragments.add(PostFragment())
        fragments.add(ChatFragment())


        // Replace with the initial fragment
        replaceFragment(fragments[0], false) // Assuming FeedFragment is the initial fragment

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_feed -> replaceFragment(fragments[0])
                R.id.navigation_map -> replaceFragment(fragments[1])
                R.id.navigation_profile -> replaceFragment(fragments[2])
                R.id.navigation_post -> replaceFragment(fragments[3])
                R.id.navigation_message -> replaceFragment(fragments[4])
                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (fragmentManager.findFragmentById(R.id.container) == fragment) {
            // Fragment is already added, scroll to top
            if (fragment is Scrollable) {
                fragment.scrollToTop()
            }
        } else {
            // Fragment is not added, replace it
            fragmentTransaction.replace(R.id.container, fragment)
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(null)
            }
        }

        fragmentTransaction.commit()
    }
}

interface Scrollable {
    fun scrollToTop()
}