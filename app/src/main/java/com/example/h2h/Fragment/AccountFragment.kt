package com.example.h2h.Fragment

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
import com.example.h2h.LoginActivity
import com.example.h2h.ProfileActivity
import com.example.h2h.R
import com.example.h2h.databinding.FragmentAccountBinding
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRef: DatabaseReference
    private lateinit var userListener: ValueEventListener

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

        binding.progressBar.visibility = View.VISIBLE
        val uid = Firebase.auth.currentUser?.uid ?: return
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.username.text = user.name

                    Picasso.get().load(user.profileImageUrl)
                        .into(binding.viewAvatar, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                binding.progressBar.visibility = View.GONE
                            }

                            override fun onError(error: Exception?) {
                                binding.progressBar.visibility = View.GONE
                                // Xử lý lỗi tải ảnh, ví dụ: hiển thị ảnh mặc định
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (error.code == DatabaseError.DISCONNECTED) {
                    // Tắt thông báo lỗi (nếu có)
                    // ...
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Log.e("AccountFragment", "Lỗi khi lấy dữ liệu người dùng", error.toException())
                }
            }
        }

        userRef.addValueEventListener(userListener)

        binding.logOut.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Xác nhận đăng xuất")
            builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?")
            builder.setPositiveButton("Có") { dialog, which ->
                try {
                    userRef.removeEventListener(userListener)
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    requireActivity().finish()
                    Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("AccountFragment", "Lỗi khi đăng xuất", e)
                    Toast.makeText(requireContext(), "Lỗi khi đăng xuất", Toast.LENGTH_SHORT).show()
                }
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