package com.example.h2h.Fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.semantics.text
import androidx.fragment.app.Fragment
import androidx.glance.visibility
import com.example.h2h.ProfileActivity
import com.example.h2h.R
import com.example.h2h.databinding.FragmentAccountBinding
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    interface LogoutListener {
        fun onLogout()
    }

    private var logoutListener: LogoutListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LogoutListener) {
            logoutListener = context
        } else {
            throw RuntimeException("$context must implement LogoutListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnViewProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        val uid = Firebase.auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        userRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.username.text = user.name

                    binding.viewAvatar.post {
                        Picasso.get().load(user.profileImageUrl).into(binding.viewAvatar)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AccountFragment", "Lỗi khi lấy dữ liệu người dùng", error.toException())
            }
        })

        binding.logOut.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Xác nhận đăng xuất")
            builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?")
            builder.setPositiveButton("Có") { dialog, which ->
                try {
                    val currentUserId = Firebase.auth.currentUser?.uid
                    if (currentUserId != null) {
                        val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(currentUserId)

                        presenceRef.setValue("offline").addOnSuccessListener {
                            FirebaseAuth.getInstance().signOut()
                            logoutListener?.onLogout()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AccountFragment", "Lỗi khi đăng xuất", e)
                    Toast.makeText(requireContext(), "Lỗi khi đăng xuất", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Không") { dialog, which ->
                // Không làm gì
            }
            builder.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        logoutListener = null
    }
}