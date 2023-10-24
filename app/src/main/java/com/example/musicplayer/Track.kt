package com.example.musicplayer

import android.net.Uri


data class Track(val title: String, val artist: String, val icon: ByteArray?, val mediaUri: Uri)