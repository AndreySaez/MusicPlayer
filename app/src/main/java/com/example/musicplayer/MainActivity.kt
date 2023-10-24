package com.example.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.util.UnstableApi

@UnstableApi class MainActivity : AppCompatActivity() {

    private var playBtn: ImageView? = null
    private var nextBtn: ImageView? = null
    private var previousBtn: ImageView? = null
    private var title: TextView? = null
    private var artist: TextView? = null
    private var icon: ImageView? = null

    private var service: MusicService? = null


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            val binder = iBinder as MusicService.MusicBinder
            service = binder.getService()

            service?.getMusicState()?.let { applyMusicState(it) }

            service?.setMusicStateListener {
                applyMusicState(it)
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playBtn = findViewById(R.id.iv_start)
        nextBtn = findViewById(R.id.iv_next)
        previousBtn = findViewById(R.id.iv_previous)
        title = findViewById(R.id.tv_title)
        artist = findViewById(R.id.tv_artist)
        icon = findViewById(R.id.picture)

        initPlayButtons()

        Intent(this, MusicService::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MusicService::class.java).also {
            bindService(it,serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    private fun initPlayButtons() {
        playBtn?.setOnClickListener {
            service?.togglePlayPause()
        }

        nextBtn?.setOnClickListener {
            service?.playNext()
        }

        previousBtn?.setOnClickListener {
            service?.playPrevious()
        }
    }

    private fun applyMusicState(musicState: MusicService.MusicState) {
        title?.text = musicState.track.title
        artist?.text = musicState.track.artist
        playBtn?.setImageResource(
            if (musicState.isPlaying) R.drawable.ic_stop else R.drawable.ic_start
        )
        if (musicState.track.icon != null){
            icon?.setImageBitmap(BitmapFactory.decodeByteArray(musicState.track.icon, 0, musicState.track.icon.size))
        } else {
            icon?.setImageResource(R.drawable.itunes)
        }
    }
}