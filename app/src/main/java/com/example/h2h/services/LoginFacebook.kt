package com.example.h2h.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.h2h.MainActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginFacebook(private val context: Context, private val auth: FirebaseAuth, private val listener: FacebookLoginListener) {

    private val callbackManager = CallbackManager.Factory.create()

    fun initializeFacebookLogin() {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d("FacebookLogin", "Login canceled")
            }

            override fun onError(error: FacebookException) {
                Log.d("FacebookLogin", "Login error: ${error.message}")
                listener.onLoginFailure(error)
            }
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("FacebookLogin", "handleFacebookAccessToken: $token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful) {
                    checkUserAndNavigate()
                } else {
                    Log.w("FacebookLogin", "signInWithCredential:failure", task.exception)
                    Toast.makeText(context, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                    listener.onLoginFailure(task.exception)
                }
            }
    }

    private fun checkUserAndNavigate() {
        val uid = auth.currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    navigateToMainActivity()
                    listener.onLoginSuccess()
                } else {
                    listener.onUserNeedsRegistration()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FacebookLogin", "signInWithCredential:failure", error.toException())
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                listener.onLoginFailure(error.toException())
            }
        })
    }

    private fun navigateToMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    interface FacebookLoginListener {
        fun onLoginSuccess()
        fun onLoginFailure(exception: Exception?)
        fun onUserNeedsRegistration()
    }
}