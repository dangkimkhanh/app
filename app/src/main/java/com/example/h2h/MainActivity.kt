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
import com.example.h2h.Fragment.*
import com.example.h2h.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), AccountFragment.LogoutListener {

    private lateinit var binding: ActivityMainBinding
    private val fragments: MutableList<Fragment> = mutableListOf()
    private lateinit var auth: FirebaseAuth
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onLogout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        fragments.add(FeedFragment())
        fragments.add(MapsFragment())
        fragments.add(AccountFragment())
        fragments.add(MachFragment())
        fragments.add(ChatFragment())

        replaceFragment(fragments[0], false)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_feed -> replaceFragment(fragments[0])
                R.id.navigation_map -> replaceFragment(fragments[1])
                R.id.navigation_profile -> replaceFragment(fragments[2])
                R.id.navigation_mach -> replaceFragment(fragments[3])
                R.id.navigation_message -> replaceFragment(fragments[4])
                else -> {}
            }
            true
        }
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

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (fragmentManager.findFragmentById(R.id.container) == fragment) {
            if (fragment is Scrollable) {
                fragment.scrollToTop()
            }
        } else {
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