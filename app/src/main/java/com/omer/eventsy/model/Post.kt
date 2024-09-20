package com.omer.eventsy.model

import com.google.firebase.Timestamp

data class Post(
    val email: String,
    val details : String,
    val title : String ,
    val downloadUrl : String,
    val profileImageUrl : String?,
    val username : String?,
    var id : String = "",
    val date : Timestamp?
)
