package com.example.h2h.Fragment

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.h2h.MainActivity
import com.example.h2h.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.android.gms.common.api.CommonStatusCodes

@Suppress("DEPRECATION")
class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient

    companion object {
        private const val REQ_ONE_TAP = 100
        private const val TAG = "GoogleActivity"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(requireActivity())

        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToMainActivity()
            return null
        }

        // Xử lý click nút đăng nhập Google
        view.findViewById<LinearLayout>(R.id.login_google).setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(requireActivity()) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0, null
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(requireActivity()) { e ->
                    Log.d(TAG, e.localizedMessage!!)
                }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    Log.d(TAG, "Got ID token.")
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Log.d(TAG, "No ID token!")
                }
            } catch (e: ApiException) {
                when (e.statusCode) {
                    CommonStatusCodes.CANCELED -> Log.d(TAG, "One-tap dialog was closed.")
                    CommonStatusCodes.NETWORK_ERROR -> Log.d(TAG, "Network error.")
                    else -> Log.d(
                        TAG, "Couldn't get credential from result: ${e.localizedMessage}"
                    )
                }
            } catch (e: IntentSender.SendIntentException) {
                Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
            }
        }
    }

    // Hiển thị RegisterFragment
    private fun showRegisterFragment() {
        val registerFragment = RegisterFragment()
        val bundle = Bundle()
        bundle.putString("email", auth.currentUser?.email) // Gán email nếu có
        registerFragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            .replace(R.id.fragment_container, registerFragment)
            .addToBackStack(null)
            .commit()
    }

    // Xác thực với Firebase bằng Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    checkUserAndNavigate()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Kiểm tra người dùng và điều hướng
    private fun checkUserAndNavigate() {
        val uid = auth.currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    navigateToMainActivity()
                } else {
                    showRegisterFragment()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "signInWithCredential:failure", error.toException())
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Điều hướng đến MainActivity
    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}