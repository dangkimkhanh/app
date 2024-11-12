package com.example.h2h.Fragment

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.h2h.MainActivity
import com.example.h2h.R
import com.example.h2h.models.DefaultImages
import com.example.h2h.models.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

class RegisterFragment : Fragment() {
    private var isConfirmed = false // Biến theo dõi trạng thái

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Xử lý nút back
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectDateLayout = view.findViewById<LinearLayout>(R.id.select_date)
        val textViewDate = view.findViewById<TextView>(R.id.textViewDate)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val radioGroupGender = view.findViewById<RadioGroup>(R.id.radioGroupGender)
        val radioMale = view.findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = view.findViewById<RadioButton>(R.id.radioFemale)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)
        //tải ảnh

        selectDateLayout.setOnClickListener {
            hideKeyboard()
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val year = currentYear - 16
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    if (selectedYear <= year) {
                        textViewDate.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    } else {
                        Toast.makeText(requireContext(), "Bạn phải từ 15 tuổi trở lên", Toast.LENGTH_SHORT).show()
                    }
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.maxDate = calendar.apply { set(currentYear - 14, 11, 31) }.timeInMillis
            datePickerDialog.show()
        }

        confirmButton.setOnClickListener {

            val name = editTextName.text.toString()
            val dateOfBirth = textViewDate.text.toString()
            val gender = if (radioMale.isChecked) "Nam" else "Nữ"

            if (name.isEmpty() || dateOfBirth.isEmpty() || gender.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val birthYear = dateOfBirth.substringAfterLast("/").toIntOrNull() ?: currentYear
            if (currentYear - birthYear < 15) {
                Toast.makeText(requireContext(), "Bạn phải từ 15 tuổi trở lên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                saveUserToFirebase(name, dateOfBirth, gender)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    override fun onStop() {
        super.onStop()

        // Xóa tài khoản Google nếu người dùng thoát app trước khi xác nhận
        if (!isConfirmed) {
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun saveUserToFirebase(name: String, dateOfBirth: String, gender: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        val storage = Firebase.storage

        lifecycleScope.launch {
            try {
                val maleAvatarUrl = storage.getReference("images/male_avatar.jpg").downloadUrl.await().toString()
                val femaleAvatarUrl = storage.getReference("images/female_avatar.jpg").downloadUrl.await().toString()
                val coverImageUrl = storage.getReference("images/cover.jpg").downloadUrl.await().toString()

                val profileImageUrl = if (gender == "Nam") {
                    maleAvatarUrl
                } else {
                    femaleAvatarUrl
                }

                val user = User(uid, name, dateOfBirth, gender, introduction = "Mình là người mới", profileImageUrl ?: "", coverImageUrl ?: "")
                usersRef.child(uid).setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isConfirmed = true
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), "Đăng ký thất bại, thử lại sau", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Tải ảnh lên thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    ///viết đoạn mã để tải 3 ảnh đó lên tại đây

}


