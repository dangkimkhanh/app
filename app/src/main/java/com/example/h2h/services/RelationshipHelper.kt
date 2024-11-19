package com.example.h2h.services

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.h2h.models.Relationship
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class RelationshipHelper(
    private val currentUserId: String,
    private val otherUserId: String,
) {

    private val relationshipsRef: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("relationships")
    private var relationshipText by mutableStateOf("Đang tải...") // Khởi tạo trạng thái ban đầu

    @Composable
    fun HandleRelationshipClick() {
        val coroutineScope = rememberCoroutineScope()
        var showConfirmationDialog by remember { mutableStateOf(false) }
        LaunchedEffect(key1 = otherUserId) {
            checkAndDisplayRelationship(relationshipsRef, currentUserId, otherUserId) { newRelationshipText ->
                relationshipText = newRelationshipText
            }
        }
        Text(
            text = relationshipText,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .background(Color.White)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .padding(1.dp)
                .wrapContentSize(Alignment.Center)
                .clickable {
                    when (relationshipText) {
                        "Bạn bè" -> {
                            showConfirmationDialog = true
                        }
                        "Đang theo dõi" -> {
                            unfollowUser(relationshipsRef, currentUserId, otherUserId) {
                                relationshipText = "Theo dõi"
                            }
                        }
                        "Theo dõi" -> {
                            followUser(relationshipsRef, currentUserId, otherUserId) { newRelationshipText ->
                                relationshipText = newRelationshipText
                            }
                        }
                    }
                }
        )

        if (showConfirmationDialog) {
            ConfirmationDialog(
                onConfirm = {
                    deleteFriendRelationship(relationshipsRef, currentUserId, otherUserId) {
                        relationshipText = "Theo dõi"
                    }
                    showConfirmationDialog = false
                },
                onDismiss = { showConfirmationDialog = false }
            )
        }
    }

    private fun checkAndDisplayRelationship(
        relationshipsRef: DatabaseReference,
        currentUserId: String,
        otherUserId: String,
        onRelationshipUpdated: (String) -> Unit
    ) {
        relationshipsRef.orderByChild("userId1").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val relationship = snapshot.getValue(Relationship::class.java)
                            if (relationship?.userId2 == otherUserId) {
                                checkIfOtherUserFollowsBack(relationshipsRef, otherUserId, currentUserId) { isFollowingBack ->
                                    val newRelationshipText = when {
                                        relationship.relationshipType == "friend" -> "Bạn bè"
                                        relationship.relationshipType == "follower" && isFollowingBack -> "Bạn bè"
                                        relationship.relationshipType == "follower" -> "Đang theo dõi"
                                        else -> "Theo dõi"
                                    }
                                    onRelationshipUpdated(newRelationshipText)
                                }
                                return
                            }
                        }
                    }
                    onRelationshipUpdated("Theo dõi")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("RelationshipHelper", "Error: ${databaseError.message}")
                }
            })
    }

    private fun followUser(
        relationshipsRef: DatabaseReference,
        currentUserId: String,
        otherUserId: String,
        onFollowed: (String) -> Unit
    ) {
        val relationship = Relationship(
            userId1 = currentUserId,
            userId2 = otherUserId,
            relationshipType = "follower"
        )
        relationshipsRef.push().setValue(relationship).addOnSuccessListener {
            checkIfOtherUserFollowsBack(relationshipsRef, otherUserId, currentUserId) { isFollowingBack ->
                if (isFollowingBack) {
                    // Cập nhật quan hệ thành "friend" cho cả hai người dùng
                    updateRelationshipType(relationshipsRef, currentUserId, otherUserId, "friend")
                    updateRelationshipType(relationshipsRef, otherUserId, currentUserId, "friend")
                    onFollowed("Bạn bè")
                } else {
                    onFollowed("Đang theo dõi")
                }
            }
        }
    }

    private fun unfollowUser(
        relationshipsRef: DatabaseReference,
        currentUserId: String,
        otherUserId: String,
        onUnfollowed: () -> Unit
    ) {
        relationshipsRef.orderByChild("userId1").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val relationship = snapshot.getValue(Relationship::class.java)
                        if (relationship?.userId2 == otherUserId && relationship.relationshipType == "follower") {
                            snapshot.ref.removeValue().addOnSuccessListener {
                                onUnfollowed()
                            }
                            return
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RelationshipHelper", "Error: ${error.message}")
                }
            })
    }

    private fun deleteFriendRelationship(
        relationshipsRef: DatabaseReference,
        currentUserId: String,
        otherUserId: String,
        onRelationshipDeleted: () -> Unit
    ) {
        relationshipsRef.orderByChild("userId1").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val relationship = snapshot.getValue(Relationship::class.java)
                        if (relationship?.userId2 == otherUserId && relationship.relationshipType == "friend") {
                            snapshot.ref.removeValue().addOnSuccessListener {
                                onRelationshipDeleted()
                            }
                            return
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RelationshipHelper", "Error: ${error.message}")
                }
            })
    }

    private fun checkIfOtherUserFollowsBack(
        relationshipsRef: DatabaseReference,
        otherUserId: String,
        currentUserId: String,
        onResult: (Boolean) -> Unit
    ) {
        relationshipsRef.orderByChild("userId1").equalTo(otherUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val relationship = snapshot.getValue(Relationship::class.java)
                        if (relationship?.userId2 == currentUserId && relationship.relationshipType == "follower") {
                            onResult(true)
                            return
                        }
                    }
                    onResult(false)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RelationshipHelper", "Error: ${error.message}")
                }
            })
    }

    @Composable
    fun ConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Xác nhận") },
            text = { Text("Bạn có chắc chắn muốn xóa bạn bè?") },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Có")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Không")
                }
            }
        )
    }
    private fun updateRelationshipType(
        relationshipsRef: DatabaseReference,
        userId1: String,
        userId2: String,
        relationshipType: String
    ) {
        relationshipsRef.orderByChild("userId1").equalTo(userId1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val relationship = snapshot.getValue(Relationship::class.java)
                        if (relationship?.userId2 == userId2) {
                            snapshot.ref.child("relationshipType").setValue(relationshipType)
                            return
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RelationshipHelper", "Error: ${error.message}")
                }
            })
    }
}
