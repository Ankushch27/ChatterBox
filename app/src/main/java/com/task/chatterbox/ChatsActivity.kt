package com.task.chatterbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chats.*

class ChatsActivity : AppCompatActivity() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUser: FirebaseUser? = firebaseAuth.currentUser
    private lateinit var chatsList: RecyclerView

    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        ref = FirebaseDatabase.getInstance().getReference("users")
//        ref.keepSynced(true)

        chatsList = chats_list_recyclerView
        chatsList.setHasFixedSize(true)
        chatsList.layoutManager = LinearLayoutManager(this)

        contacts_fab.setOnClickListener {
            startContactsActivity()
        }
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
