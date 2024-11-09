package com.example.h2h

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.semantics.text
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

@Suppress("DEPRECATION")
class EditProfileBtn : BottomSheetDialogFragment() {

    private val database = FirebaseDatabase.getInstance().getReference("users")
    private val storage = FirebaseStorage.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_profile_bottom_sheet, container, false)

        view.findViewById<TextView>(R.id.option_change_avt).setOnClickListener {
            selectImage(REQUEST_CODE_PROFILE_IMAGE)
        }

        view.findViewById<TextView>(R.id.option_change_cover).setOnClickListener {
            selectImage(REQUEST_CODE_COVER_IMAGE)
        }

        view.findViewById<TextView>(R.id.option_change_name).setOnClickListener {
            showInputDialog("name", "Tên của bạn sẽ là:")
        }

        view.findViewById<TextView>(R.id.option_edit_introduc).setOnClickListener {
            showInputDialog("introduction", "Giới thiệu về bạn:")
        }

        view.findViewById<TextView>(R.id.option_cancel).setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun updateUserInfo(field: String, value: String) {
        currentUser?.uid?.let { uid ->
            database.child(uid).child(field).setValue(value)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun selectImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            when (requestCode) {
                REQUEST_CODE_PROFILE_IMAGE -> uploadImageToFirebase(imageUri, "profileImageUrl")
                REQUEST_CODE_COVER_IMAGE -> uploadImageToFirebase(imageUri, "coverImageUrl")
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?, fieldName: String) {
        imageUri?.let {
            val imageRef = storage.child("users/${currentUser?.uid}/$fieldName.jpg")
            imageRef.putFile(it)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateUserInfo(fieldName, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Lỗi tải ảnh!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showInputDialog(field: String, title: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Lưu") { dialog, which ->
            val newValue = input.text.toString()
            updateUserInfo(field, newValue)
        }
        builder.setNegativeButton("Hủy") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    companion object {
        private const val REQUEST_CODE_PROFILE_IMAGE = 1
        private const val REQUEST_CODE_COVER_IMAGE = 2
    }
}