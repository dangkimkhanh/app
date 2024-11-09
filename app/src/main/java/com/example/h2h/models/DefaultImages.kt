package com.example.h2h.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.h2h.R
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

data class DefaultImages(
    val maleAvatarUrl: String? = null,
    val femaleAvatarUrl: String? = null,
    val coverUrl: String? = null
)
/*val saveButton = view.findViewById<Button>(R.id.save)*/

/*saveButton.setOnClickListener {
    saveButton.setOnClickListener {
        lifecycleScope.launch {
            try {
                val defaultImages = uploadDefaultImages(requireContext())
                // Use defaultImages.maleAvatarUrl, defaultImages.femaleAvatarUrl, and defaultImages.coverUrl
                // to update the user profile in Firebase Realtime Database
                Toast.makeText(requireContext(), "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Tải ảnh lên thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}*/
/*private suspend fun uploadDefaultImages(context: Context): DefaultImages {
    val storage = Firebase.storage
    val storageRef = storage.reference

    val maleAvatarBitmap = (ContextCompat.getDrawable(context, R.drawable.default_avatar_male) as BitmapDrawable).bitmap
    val femaleAvatarBitmap = (ContextCompat.getDrawable(context, R.drawable.default_avatar_female) as BitmapDrawable).bitmap
    val coverBitmap = (ContextCompat.getDrawable(context, R.drawable.cover_image_default) as BitmapDrawable).bitmap

    val maleAvatarUrl = uploadImage(storageRef, "male_avatar.jpg", maleAvatarBitmap).await()
    val femaleAvatarUrl = uploadImage(storageRef, "female_avatar.jpg", femaleAvatarBitmap).await()
    val coverUrl = uploadImage(storageRef, "cover.jpg", coverBitmap).await()

    return DefaultImages(maleAvatarUrl.toString(), femaleAvatarUrl.toString(),
        coverUrl.toString()
    )
}
private fun uploadImage(storageRef: StorageReference, filename: String, bitmap: Bitmap): Task<Uri> {
    val imagesRef = storageRef.child("images/$filename")
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()
    return imagesRef.putBytes(data).continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        imagesRef.downloadUrl
    }
}*/
