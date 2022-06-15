package com.iraimjanov.taxometr.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iraimjanov.taxometr.models.Users

class CheckUsers {
    private val realReference = Firebase.database.getReference("users")

    fun checkUsers() {
        realReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val users = child.getValue(Users::class.java)
                    if (users != null) {
                        listUsernames.add(users.username)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    companion object {
        var listUsernames = HashSet<String>()
    }
}