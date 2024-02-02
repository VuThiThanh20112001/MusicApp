package com.example.musicapp

import android.net.Uri

data class Song(
    val id: Int,
    val title: String?,
    val author: String?,
    val album: String?,
    val duration: Long,
    val image: Uri
)
