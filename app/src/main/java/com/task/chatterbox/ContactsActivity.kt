package com.task.chatterbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity() {

    private lateinit var usersList: RecyclerView
    private lateinit var toolbar: Toolbar

    private lateinit var query: DatabaseReference
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<User, ContactsViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        toolbar = contacts_toolbar as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Contacts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        query = FirebaseDatabase.getInstance().getReference("users")

        usersList = users_list
        usersList.setHasFixedSize(true)
        usersList.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .setLifecycleOwner(this)
            .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<User, ContactsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)

                return ContactsViewHolder(view)
            }

            override fun onBindViewHolder(holder: ContactsViewHolder, position: Int, model: User) {
                Picasso.get().load(model.profile_image).into(holder.user_image)
            }
        }
        usersList.adapter = firebaseRecyclerAdapter
    }

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val user_image = itemView.findViewById<CircleImageView>(R.id.user_profile_pic)
        }
}
