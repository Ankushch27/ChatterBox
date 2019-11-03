package com.task.chatterbox

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

fun Context.toast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun ProgressBar.show(){
    visibility = View.VISIBLE
}

fun ProgressBar.hide(){
    visibility = View.GONE
}

fun Context.startRegisterActivity() =
    Intent(this, RegisterActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }


fun Context.startProfileActivity() =
    Intent(this, ProfileActivity::class.java).also {
        startActivity(it)
    }

fun Context.startChatActivity() =
    Intent(this, ChatActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startContactsActivity() =
    Intent(this, ContactsActivity::class.java).also {
        startActivity(it)
    }
