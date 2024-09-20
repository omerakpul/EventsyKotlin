package com.omer.eventsy.model

data class Post(
    val email: String,
    val details : String,
    val title : String ,
    val downloadUrl : String,
    val profileImageUrl : String?,
    val username : String?
)
