package com.task.chatterbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUser: FirebaseUser? = firebaseAuth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        logout_button.setOnClickListener {
            firebaseAuth.signOut()
            finish()
            startRegisterActivity()
        }
    }

    override fun onStart() {
        super.onStart()

        if(currentUser == null) {
            finish()
            startRegisterActivity()
        }
    }

}
