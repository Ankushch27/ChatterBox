package com.task.chatterbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity() {

    private lateinit var usersList: RecyclerView

    private lateinit var ref: DatabaseReference
    private lateinit var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<User, ContactsViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        supportActionBar?.title = "Contacts"

        ref = FirebaseDatabase.getInstance().getReference("users")
        ref.keepSynced(true)

        usersList = users_list
        usersList.setHasFixedSize(true)
        usersList.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(ref, User::class.java)
            .setLifecycleOwner(this)
            .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<User, ContactsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)

                return ContactsViewHolder(view)
            }

            override fun onBindViewHolder(holder: ContactsViewHolder, position: Int, model: User) {
                val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
                val userID = getRef(position).key
                if(userID != currentUserID) {
                    Picasso.get().load(model.profile_image).networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)
                        .into(holder.profileImageView)
                    holder.usernameTextView.text = model.name

                    holder.itemView.setOnClickListener {
                        startUserChatActivity {
                            putString("userID", userID)
                            putString("username", model.name)
                            putString("profileImage", model.profile_image)
                        }
                    }
                } else {
                    holder.profileImageView.visibility = View.GONE
                    holder.usernameTextView.visibility = View.GONE
                }
            }
        }
        usersList.adapter = firebaseRecyclerAdapter
    }

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: CircleImageView = itemView.findViewById(R.id.user_profile_pic)
        val usernameTextView: TextView = itemView.findViewById(R.id.username)
    }
}
