package com.example.h2h.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.size
import androidx.compose.ui.input.key.key
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.ChatActivity
import com.example.h2h.R
import com.example.h2h.adapter.ListItemChatAdapter
import com.example.h2h.models.UserChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var userChatAdapter: ListItemChatAdapter
    private var userChats: MutableList<UserChat> = mutableListOf()
    private var isDataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        chatRecyclerView = view.findViewById(R.id.item_chat_RecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(context)

        userChatAdapter = ListItemChatAdapter(requireContext(), userChats) { userChat ->
            val intent = Intent(context, ChatActivity::class.java)
            val otherUserId = if (userChat.lastMessage?.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                userChat.lastMessage?.receiverId
            } else {
                userChat.lastMessage?.senderId
            }
            intent.putExtra("OTHER_USER_ID", otherUserId)
            startActivity(intent)
        }
        chatRecyclerView.adapter = userChatAdapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userChatsRef = FirebaseDatabase.getInstance().reference.child("userChats")

        if (!isDataLoaded) {
            userChatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userChats.clear()
                    for (chatSnapshot in snapshot.children) {
                        val chatId = chatSnapshot.key
                        if (chatId != null && chatId.contains(currentUserId)) {
                            val chatRef = FirebaseDatabase.getInstance().reference.child("userChats").child(chatId)
                            chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val userChat = snapshot.getValue(UserChat::class.java)
                                    if (userChat != null) {
                                        userChats.add(userChat)
                                        userChatAdapter.notifyItemInserted(userChats.size - 1)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("ChatFragment", "Lỗi khi lấy dữ liệu UserChat: ${error.message}")
                                }
                            })
                        }
                    }
                    isDataLoaded = true
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatFragment", "Lỗi khi lấy danh sách chatId: ${error.message}")
                }
            })
        }

        // Lắng nghe các thay đổi trong dữ liệu Firebase (ví dụ: sử dụng ChildEventListener)
        // và đặt lại isDataLoaded = false khi có thay đổi

        return view
    }
}