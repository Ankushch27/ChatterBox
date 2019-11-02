package com.task.chatterbox

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.register_main.*
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private var verificationId : String? = null

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_main)

        register_button.setOnClickListener {
            validatePhone()
        }

        verify_button.setOnClickListener {
            verifyPhone()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = firebaseAuth.currentUser
        currentUser?.let {
            finish()
            startChatActivity()
        }
    }

    private fun validatePhone() {
        val phone = phoneEditText.text.toString().trim()

        if(phone.isEmpty() || phone.length != 10){
            toast("Enter a valid phone number")
            return
        }

        val phoneNumber = "+91$phone"

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, 60, TimeUnit.SECONDS, this, phoneAuthCallbacks
        )
        register_layout.visibility = View.GONE
        verification_layout.visibility = View.VISIBLE
    }

    private fun verifyPhone() {
        progressBar.show()
        val code = codeEditText.text.toString().trim()

        if(code.isEmpty()) {
            toast("Code required")
            return
        }

        verificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private val phoneAuthCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
            val code = credential?.smsCode
            codeEditText.setText(code)
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            toast(exception.message!!)
        }

        override fun onCodeSent(id: String?, token: PhoneAuthProvider.ForceResendingToken?) {
            super.onCodeSent(id, token)
            progressBar.hide()
            verificationId = id
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential?) {
        credential?.let {
            FirebaseAuth.getInstance().signInWithCredential(it)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        val currentUser: FirebaseUser? = firebaseAuth.currentUser
                        val userID = currentUser?.uid
                        saveUserDetails(userID)
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            toast(task.exception.toString())
                        } else {
                            toast("Login failed: Some error occurred")
                        }
                    }
                }
        }
    }

    private fun saveUserDetails(userID: String?) {
        val phone = phoneEditText.text.toString().trim()
        ref.child(userID!!).child("phone").setValue(phone).addOnCompleteListener {
            finish()
            startProfileActivity()
        }
    }
}
