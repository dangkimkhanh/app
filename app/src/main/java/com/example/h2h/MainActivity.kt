package com.example.h2h

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.h2h.databinding.ActivityMainBinding

import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(FeedFragment())

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_feed -> replaceFragment(FeedFragment())
                R.id.navigation_map -> replaceFragment(MapsFragment())
                R.id.navigation_profile -> replaceFragment(ProfileFragment())
                R.id.navigation_post -> replaceFragment(PostFragment())
                R.id.navigation_message -> replaceFragment(ChatFragment())
                else -> {

                }
                }
                true
            }
    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }
}
