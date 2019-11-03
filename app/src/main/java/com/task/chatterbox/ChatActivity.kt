package com.task.chatterbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUser: FirebaseUser? = firebaseAuth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbar = main_toolbar as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Chatter Box"
    }

    override fun onStart() {
        super.onStart()

        if(currentUser == null) {
            finish()
            startRegisterActivity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)

        if(item?.itemId == R.id.option_contacts) {
            startContactsActivity()
        }
        if(item?.itemId == R.id.option_profile) {
            startProfileActivity()
        }
        if(item?.itemId == R.id.option_logout) {
            firebaseAuth.signOut()
            finish()
            startRegisterActivity()
        }

        return true
    }
}
