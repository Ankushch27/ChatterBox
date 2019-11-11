package com.task.chatterbox

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_user_chat.*
import kotlinx.android.synthetic.main.custom_chat_bar.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class UserChatActivity : AppCompatActivity() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private lateinit var ref: DatabaseReference
    private var storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var currentUserID: String
    private lateinit var userID: String

    private lateinit var chatListRecyclerView: RecyclerView
    private var userMessageList: MutableList<Messages> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter

    private var REQUEST_CAMERA = 1
    private var REQUEST_GALLERY = 2
    private var REQUEST_DOCUMENT = 3
    private lateinit var imagePath: String
    private lateinit var photoURI: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_chat)

        currentUserID = firebaseAuth.currentUser?.uid.toString()
        userID = intent.getStringExtra("userID")
        ref = FirebaseDatabase.getInstance().reference
//        ref.keepSynced(true)

        setupActionBar()
        setupRecyclerView()
        loadMessages()

        sendImageView.setOnClickListener {
            sendMessage()
        }

        attachmentImageView.setOnClickListener {
            selectFileType()
        }
    }

    private fun selectFileType() {
        val items = arrayOf<CharSequence>("Camera", "Gallery", "Document", "Audio", "Cancel")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select File Type!")
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Camera" -> openCamera()
                items[item] == "Gallery" -> openGallery()
                items[item] == "Document" -> selectDocument()
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
            }

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, "com.chatterbox.android.fileprovider", photoFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, REQUEST_CAMERA)
            }
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        imagePath = image.absolutePath
        return image
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    private fun selectDocument() {
        val documentIntent = Intent(Intent.ACTION_GET_CONTENT)
        documentIntent.type = "application/*"
        startActivityForResult(documentIntent, REQUEST_DOCUMENT)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when (requestCode) {
                REQUEST_CAMERA -> CropImage.activity(photoURI).start(this)
                REQUEST_GALLERY -> {
                    val uri = data?.data
                    CropImage.activity(uri).start(this)
                }
                REQUEST_DOCUMENT -> {
                    val docUri = data?.data
                    sendDocMessage(docUri)
                }
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                text_message_layout.visibility = View.INVISIBLE
                image_message_layout.visibility = View.VISIBLE
                Picasso.get().load(result.uri).networkPolicy(NetworkPolicy.OFFLINE, NetworkPolicy.NO_CACHE).into(image_message_ImageView)

                button_sendImage.setOnClickListener {
                    sendImageMessage(result.uri)
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()

        if(messageText.isNotEmpty()){
            val senderRef = "messages/$currentUserID/$userID"
            val receiverRef = "messages/$userID/$currentUserID"
            val messagePushID = ref.child("messages").child(currentUserID).child(userID).push().key

            val messageBody = Messages(messageText, messagePushID, currentUserID, "text")

            val messageUserMap = HashMap<String, Messages>()
            messageUserMap["$senderRef/$messagePushID"] = messageBody
            messageUserMap["$receiverRef/$messagePushID"] = messageBody
            messageEditText.setText("")
            ref.updateChildren(messageUserMap as Map<String, Any>).addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    val notificationMap = HashMap<String, String>()
                    notificationMap["from"] = currentUserID
                    notificationMap["type"] = "message"

                    ref.child("notifications").child(userID).push().setValue(notificationMap)
                }
            }
        }
    }

    private fun sendImageMessage(uri: Uri) {
        upload_progressBar.show()

        val senderRef = "messages/$currentUserID/$userID"
        val receiverRef = "messages/$userID/$currentUserID"
        val messagePushID = ref.child("messages").child(currentUserID).child(userID).push().key
        val imageRef = storageRef.child("message_images").child("$messagePushID.jpg")
        imageRef.putFile(uri).addOnCompleteListener{task ->
            if(task.isSuccessful){
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val messageBody = Messages(downloadUrl.toString(), messagePushID, currentUserID, "image")

                    val messageUserMap = HashMap<String, Messages>()
                    messageUserMap["$senderRef/$messagePushID"] = messageBody
                    messageUserMap["$receiverRef/$messagePushID"] = messageBody

                    ref.updateChildren(messageUserMap as Map<String, Any>).addOnCompleteListener{
                        if(it.isSuccessful) {
                            upload_progressBar.hide()
                            toast("Upload successful")
                            text_message_layout.visibility = View.VISIBLE
                            image_message_layout.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun sendDocMessage(docUri: Uri?) {
        val senderRef = "messages/$currentUserID/$userID"
        val receiverRef = "messages/$userID/$currentUserID"
        val messagePushID = ref.child("messages").child(currentUserID).child(userID).push().key
        val docRef = storageRef.child("message_documents").child("$messagePushID.file")
        docRef.putFile(docUri!!).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                docRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val messageBody = Messages(downloadUrl.toString(), messagePushID, currentUserID, "document")

                    val messageUserMap = HashMap<String, Messages>()
                    messageUserMap["$senderRef/$messagePushID"] = messageBody
                    messageUserMap["$receiverRef/$messagePushID"] = messageBody

                    ref.updateChildren(messageUserMap as Map<String, Any>).addOnCompleteListener{
                        if(it.isSuccessful) {
                            toast("Document upload successful")
                        }
                    }
                }
            }
        }.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred)/it.totalByteCount
        }
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
}
