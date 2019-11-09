package com.task.chatterbox

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter(private val userMessagesList: MutableList<Messages>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private lateinit var ref: DatabaseReference

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: CircleImageView = itemView.findViewById(R.id.user_profile_pic)
        val receiverMessageText: TextView = itemView.findViewById(R.id.receiverMessageTextView)
        val senderMessageText: TextView = itemView.findViewById(R.id.senderMessageTextView)
        val receiverMessageImage: ImageView = itemView.findViewById(R.id.receiverImageView)
        val senderMessageImage: ImageView = itemView.findViewById(R.id.senderImageView)
        val receiverMessageFile: ImageView = itemView.findViewById(R.id.senderFileImageView)
        val senderMessageFile: ImageView = itemView.findViewById(R.id.senderFileImageView)
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
        ref.keepSynced(true)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("profile_image")) {
                    val receiverImage = dataSnapshot.child("profile_image").value.toString()
                    Picasso.get().load(receiverImage).networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)
                        .placeholder(R.drawable.profile_pic).into(holder.profileImageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        if(messageType.equals("text")){
            if(fromUserID == senderID){
                holder.senderMessageText.visibility = View.VISIBLE
                holder.senderMessageText.text = messages.message
            } else {
                holder.profileImageView.visibility = View.VISIBLE
                holder.receiverMessageText.visibility = View.VISIBLE
                holder.receiverMessageText.text = messages.message
            }
        } else if(messageType.equals("image")){
            if(fromUserID == senderID){
                holder.senderMessageImage.visibility = View.VISIBLE
                Picasso.get().load(messages.message).networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)
                    .into(holder.senderMessageImage)
            } else {
                holder.profileImageView.visibility = View.VISIBLE
                holder.receiverMessageImage.visibility = View.VISIBLE
                Picasso.get().load(messages.message).networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE)
                    .into(holder.receiverMessageImage)
            }
        } else if(messageType.equals("document")){
            if(fromUserID == senderID){
                holder.senderMessageFile.visibility = View.VISIBLE

                holder.itemView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList[position].message))
                    holder.itemView.context.startActivity(intent)
                }
            } else {
                holder.profileImageView.visibility = View.VISIBLE
                holder.receiverMessageFile.visibility = View.VISIBLE

                holder.itemView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList[position].message))
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return userMessagesList.size
    }
}
