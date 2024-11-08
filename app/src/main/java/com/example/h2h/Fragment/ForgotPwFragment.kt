package com.example.h2h.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.h2h.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPwFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var backToLoginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_pw, container, false)

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Ánh xạ các view
        emailEditText = view.findViewById(R.id.et_email)
        resetPasswordButton = view.findViewById(R.id.btn_reset_password)
        backToLoginButton = view.findViewById(R.id.btn_back_to_login)

        // Xử lý sự kiện nút gửi yêu cầu đặt lại mật khẩu
        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            // Kiểm tra các trường hợp
            when {
                email.isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        "Vui lòng nhập gmail của bạn",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !email.endsWith("@gmail.com") -> { // Kiểm tra đuôi tên miền
                    Toast.makeText(
                        requireContext(),
                        "Định dạng Gmail không hợp lệ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    // Gửi yêu cầu đặt lại mật khẩu
                    sendPasswordResetEmail(email)
                }
            }
        }

        // Xử lý sự kiện nút quay lại đăng nhập
        backToLoginButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Yêu cầu đặt lại mật khẩu đã được gửi tới gmail của bạn",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Xử lý các trường hợp lỗi
                    val exception = task.exception
                    when {
                        exception is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(
                                requireContext(),
                                "Gmail không tồn tại",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        exception is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(
                                requireContext(),
                                "Gmail không hợp lệ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                "Không thể gửi yêu cầu. Vui lòng kiểm tra email và thử lại.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
    }
}