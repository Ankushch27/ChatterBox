package com.task.chatterbox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val REQUEST_GALLERY = 1
    private val currentUser: FirebaseUser? = firebaseAuth.currentUser
    private var userRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private var storageRef: StorageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val currentUser: FirebaseUser? = firebaseAuth.currentUser
        val userID = currentUser?.uid

//        userRef.keepSynced(true)
        userRef.child(userID!!).addValueEventListener(databaseListener)

        change_profile_pic.setOnClickListener {
            setProfilePic()
        }

        next_button.setOnClickListener {
            saveUsername(userID)
        }
    }

    private val databaseListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val imgUrl = dataSnapshot.child("profile_image").value.toString()
            Log.i("profile image", imgUrl)
            Picasso.get().load(imgUrl).placeholder(R.drawable.profile_pic).into(profileImageView)
        }

        override fun onCancelled(p0: DatabaseError) {}
    }

    private fun saveUsername(userID: String?) {
        val name = usernameEditText.text.toString().trim()
        userRef.child(userID!!).child("name").setValue(name).addOnCompleteListener {
            finish()
            startChatActivity()
        }
    }

    override fun onStart() {
        super.onStart()

        if (currentUser == null) {
            finish()
            startRegisterActivity()
        }
    }

    private fun setProfilePic() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                profile_progressBar.show()
                val resultUri = result.uri
                val currentUser: FirebaseUser? = firebaseAuth.currentUser
                val userID = currentUser?.uid
                val filepath = storageRef.child("profile_images/${userID}").child("profile_image.jpg")
                filepath.putFile(resultUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        filepath.downloadUrl.addOnSuccessListener { resultUri ->
                            val downloadUrl = resultUri.toString()
                            userRef.child(userID!!).child("profile_image").setValue(downloadUrl)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        profile_progressBar.hide()
                                        toast("Profile Pic updated")
                                    }
                                }
                        }
                    } else {
                        toast("Error in updating Profile Pic")
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.i("Crop error:", error.toString())
            }
        }
    }

}
