package com.task.chatterbox

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_chat.*
import kotlinx.android.synthetic.main.custom_chat_bar.*

class UserChatActivity : AppCompatActivity() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private lateinit var ref: DatabaseReference
    private lateinit var currentUserID: String
    private lateinit var userID: String

    private lateinit var chatListRecyclerView: RecyclerView
    private var userMessageList: MutableList<Messages> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_chat)

        currentUserID = firebaseAuth.currentUser?.uid.toString()
        userID = intent.getStringExtra("userID")
        ref = FirebaseDatabase.getInstance().reference
        setupActionBar()

        setupRecyclerView()

        sendImageView.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(userMessageList)
        chatListRecyclerView = userChatRecyclerView
        chatListRecyclerView.setHasFixedSize(true)
        chatListRecyclerView.layoutManager = LinearLayoutManager(this)
        chatListRecyclerView.adapter = messageAdapter
    }

    private fun setupActionBar() {
        val username = intent.getStringExtra("username")
        val profileImage = intent.getStringExtra("profileImage")

        val chatActionBar = View.inflate(applicationContext, R.layout.custom_chat_bar, null)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.customView = chatActionBar

        usernameTextView.text = username
        Picasso.get().load(profileImage).into(userProfileImageView)
    }

    override fun onStart() {
        super.onStart()

        loadMessages()
    }

    private fun loadMessages() {
        ref.child("messages").child(currentUserID).child(userID).addChildEventListener(
            object: ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val messages = dataSnapshot.getValue(Messages::class.java)
                    userMessageList.add(messages!!)
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(p0: DatabaseError) {}

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                override fun onChildRemoved(p0: DataSnapshot) {}
            }
        )
    }

    private fun sendMessage() {
        val message = messageEditText.text.toString().trim()

        if(message.isNotEmpty()){
            val senderRef = "messages/$currentUserID/$userID"
            val receiverRef = "messages/$userID/$currentUserID"
            val messagePushID = ref.child("messages").child(currentUserID).child(userID).push().key

            val messageBody = Messages(message, currentUserID, "text")

            val messageUserMap = HashMap<String, Messages>()
            messageUserMap["$senderRef/$messagePushID"] = messageBody
            messageUserMap["$receiverRef/$messagePushID"] = messageBody

            ref.updateChildren(messageUserMap as Map<String, Any>).addOnCompleteListener{
                if(it.isSuccessful) {
                    messageEditText.setText("")
                }
            }
        }
    }
    }
