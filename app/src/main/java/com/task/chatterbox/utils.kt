package com.task.chatterbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

fun Context.startRegisterActivity(extras: Bundle.() -> Unit = {}) =
    Intent(this, RegisterActivity::class.java).also {
        it.putExtras(Bundle().apply(extras))
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }


fun Context.startProfileActivity(extras: Bundle.() -> Unit = {}) =
    Intent(this, ProfileActivity::class.java).also {
        it.putExtras(Bundle().apply(extras))
        startActivity(it)
    }

fun Context.startChatActivity(extras: Bundle.() -> Unit = {}) =
    Intent(this, ChatsActivity::class.java).also {
        it.putExtras(Bundle().apply(extras))
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startContactsActivity(extras: Bundle.() -> Unit = {}) =
    Intent(this, ContactsActivity::class.java).also {
        it.putExtras(Bundle().apply(extras))
        startActivity(it)
    }

fun Context.startUserChatActivity(extras: Bundle.() -> Unit = {}) =
    Intent(this, UserChatActivity::class.java).also {
        it.putExtras(Bundle().apply(extras))
        startActivity(it)
    }
