package com.example.h2h.models


data class Like(
    val userId: String,
    val timestamp: Any? = null // Sử dụng Any? để có thể lưu trữ ServerValue.TIMESTAMP
)
// Hàm để xử lý like
//    private fun handleLike(postId: String) {
//        val likesRef = FirebaseDatabase.getInstance().getReference("likes").child(postId)
//        val userId = Firebase.auth.currentUser!!.uid
//
//        likesRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    // Người dùng đã like, xóa like
//                    likesRef.child(userId).removeValue()
//                    // Giảm số lượt like trong bài viết (cần cập nhật UI)
//                } else {
//                    // Người dùng chưa like, thêm like
//                    val newLike = Like(userId = userId, timestamp = ServerValue.TIMESTAMP)
//                    likesRef.child(userId).setValue(newLike)
//                    // Tăng số lượt like trong bài viết (cần cập nhật UI)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("CreatePostActivity", "Lỗi khi xử lý like", error.toException())
//            }
//        })
//    }
//
//    // Hàm để xử lý comment
//    private fun handleComment(postId: String, commentContent: String) {
//        val commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postId)
//        val userId = Firebase.auth.currentUser!!.uid
//        // Lấy userName và userAvatar từ Firebase Database hoặc một nguồn dữ liệu khác
//        val userName = "" // Thay thế bằng cách lấy userName từ database
//        val userAvatar = "" // Thay thế bằng cách lấy userAvatar từ database
//
//        val newCommentRef = commentsRef.push()
//        val commentId = newCommentRef.key
//
//        val newComment = Comment(
//            commentId = commentId,
//            content = commentContent,
//            userId = userId,
//            userName = userName,
//            userAvatar = userAvatar,
//            timestamp = ServerValue.TIMESTAMP
//        )
//
//        newCommentRef.setValue(newComment)
//            .addOnSuccessListener {
//                // Cập nhật số lượng comment trong bài viết (cần cập nhật UI)
//            }
//            .addOnFailureListener {
//                Log.e("CreatePostActivity", "Lỗi khi xử lý comment", it)
//            }
//    }