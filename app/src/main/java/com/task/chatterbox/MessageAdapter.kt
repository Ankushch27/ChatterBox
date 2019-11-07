package com.task.chatterbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private val userMessagesList: MutableList<Messages>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private lateinit var ref: DatabaseReference

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: CircleImageView = itemView.findViewById(R.id.user_profile_pic)
        val receiverMessageTextView: TextView = itemView.findViewById(R.id.receiverMessageTextView)
        val senderMessageTextView: TextView = itemView.findViewById(R.id.senderMessageTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_chat_layout, parent, false)
        return MessageViewHolder(view)
    }
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val senderID = firebaseAuth.currentUser?.uid.toString()
        val messages = userMessagesList[position]
        val fromUserID = messages.from
        val messageType = messages.type

        ref = FirebaseDatabase.getInstance().getReference("users").child(fromUserID!!)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("profile_image")) {
                    val receiverImage = dataSnapshot.child("profile_image").value.toString()
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_pic).into(holder.profileImageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        if(messageType.equals("text")){
            holder.profileImageView.visibility = View.INVISIBLE
            holder.receiverMessageTextView.visibility = View.INVISIBLE

            if(fromUserID == senderID){
                holder.senderMessageTextView.setBackgroundResource(R.drawable.sender_message_layout)
                holder.senderMessageTextView.text = messages.message
            } else {
                holder.profileImageView.visibility = View.VISIBLE
                holder.receiverMessageTextView.visibility = View.VISIBLE
                holder.senderMessageTextView.visibility = View.INVISIBLE

                holder.receiverMessageTextView.text = messages.message
            }
        }
    }

    override fun getItemCount(): Int {
        return userMessagesList.size
    }
}
