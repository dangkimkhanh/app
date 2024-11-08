package com.example.h2h.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.with
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import androidx.fragment.app.Fragment
import androidx.wear.compose.material.placeholder
import com.bumptech.glide.Glide
import com.example.h2h.LoginActivity
import com.example.h2h.ProfileActivity
import com.example.h2h.R
import com.example.h2h.databinding.FragmentAccountBinding
import com.example.h2h.models.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.getValue
import androidx.appcompat.app.AlertDialog

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

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
        val uid = Firebase.auth.currentUser?.uid ?: return // Lấy UID của người dùng hiện tại
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java) // Lấy thông tin người dùng
                if (user != null) {
                    binding.username.text = user.name // Hiển thị tên người dùng
                    // Hiển thị avatar
                    val avatarUrl = snapshot.child("avatar").getValue(String::class.java)
                    if (avatarUrl != null) {
                        // Hiển thị avatar từ đường dẫn avatarUrl bằng Glide
                        Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.ic_avatar) // Placeholder trong khi tải ảnh
                            .error(R.drawable.ic_avatar) // Ảnh hiển thị nếu tải ảnh thất bại
                            .into(binding.viewAvatar) // ImageView hiển thị avatar
                    } else {
                        // Hiển thị avatar mặc định
                        val defaultAvatar = if (user.gender == "Nam") {
                            R.drawable.default_avatar_male
                        } else {
                            R.drawable.default_avatar_female
                        }
                        binding.viewAvatar.setImageResource(defaultAvatar)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
                // ...
            }
        })

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.username.text = user.name

                    val avatarUrl = snapshot.child("avatar").getValue(String::class.java)
                    if (avatarUrl != null) {
                        // Hiển thị avatar từ đường dẫn avatarUrl bằng Glide
                        // ...
                    } else {
                        // Lấy thông tin gender từ snapshot
                        val gender = snapshot.child("gender").getValue(String::class.java) ?: ""

                        // Hiển thị avatar mặc định
                        val defaultAvatar = if (gender == "Nam") {
                            R.drawable.default_avatar_male
                        } else {
                            R.drawable.default_avatar_female
                        }
                        binding.viewAvatar.setImageResource(defaultAvatar)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
                // ...
            }
        })
        binding.logOut.setOnClickListener {
            // Tạo AlertDialog để xác nhận đăng xuất
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Xác nhận đăng xuất")
            builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?")
            builder.setPositiveButton("Có") { dialog, which ->
                // Thực hiện đăng xuất
                FirebaseAuth.getInstance().signOut()

                // Chuyển hướng đến LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                // Kết thúc AccountFragment
                requireActivity().finish()
            }
            builder.setNegativeButton("Không") { dialog, which ->
                // Không làm gì cả
            }
            builder.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}