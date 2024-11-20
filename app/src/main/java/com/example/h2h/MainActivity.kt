package com.example.h2h

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.h2h.Fragment.*
import com.example.h2h.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), AccountFragment.LogoutListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var viewPager: ViewPager2

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FeedFragment()
                1 -> MachFragment()
                2 -> ChatFragment()
                3 -> AccountFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

    override fun onLogout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tắt animation của BottomNavigationView
        binding.bottomNavigation.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_UNLABELED

        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(currentUserId)
            presenceRef.setValue("online")
            presenceRef.onDisconnect().setValue("offline").addOnSuccessListener {
                Log.d("Presence", "onDisconnect listener set successfully")
            }.addOnFailureListener { exception ->
                Log.e("Presence", "Failed to set onDisconnect listener", exception)
            }
        }

        // Lắng nghe sự thay đổi trạng thái kết nối mạng
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                // Cập nhật trạng thái presence thành "offline" khi mất kết nối mạng
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(currentUserId)
                    presenceRef.setValue("offline")
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // Khởi tạo ViewPager2 và ScreenSlidePagerAdapter
        viewPager = findViewById(R.id.viewPager)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 4 // Giữ 4 fragment trong bộ nhớ cache


        // Lắng nghe sự thay đổi trang của ViewPager2 và cập nhật BottomNavigationView
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.bottomNavigation.selectedItemId = R.id.navigation_feed
                    1 -> binding.bottomNavigation.selectedItemId = R.id.navigation_mach
                    2 -> binding.bottomNavigation.selectedItemId = R.id.navigation_message
                    3 -> binding.bottomNavigation.selectedItemId = R.id.navigation_profile
                }
            }
        })

        // Xử lý sự kiện click vào item trong BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_feed -> viewPager.setCurrentItem(0, false) // smoothScroll = false
                R.id.navigation_mach -> viewPager.setCurrentItem(1, false) // smoothScroll = false
                R.id.navigation_message -> viewPager.setCurrentItem(2, false) // smoothScroll = false
                R.id.navigation_profile -> viewPager.setCurrentItem(3, false) // smoothScroll = false
                else -> {}
            }
            true
        }

        // Điều chỉnh padding cho BottomNavigationView
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy đăng ký lắng nghe sự thay đổi trạng thái kết nối mạng
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)

        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(currentUserId)
            presenceRef.setValue("offline")
        }
    }

    interface Scrollable {
        fun scrollToTop()
    }
}