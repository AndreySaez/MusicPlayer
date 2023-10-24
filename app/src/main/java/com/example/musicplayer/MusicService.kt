package com.example.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager


private const val NOTIFICATION_ID = 2142132
private const val NOTIFICATION_CHANNEL_ID = "myChannelId"

@UnstableApi
class MusicService : Service() {
    private val binder = MusicBinder()

    private var player: ExoPlayer? = null

    private var currentMusicState: MusicState? = null
    private val musicList = mutableListOf<Track>()

    private var stateUpdateListener: ((MusicState) -> Unit)? = null
    override fun onCreate() {
        super.onCreate()
        musicList.addAll(MusicProvider.getMusicList(this))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "My music channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        val notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(MediaDescriptionAdapter(musicList))
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (!ongoing) {
                        stopForeground(STOP_FOREGROUND_DETACH)
                    } else {
                        startForeground(notificationId, notification)
                    }
                }

                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    super.onNotificationCancelled(notificationId, dismissedByUser)
                    stopSelf()
                }

            })
            .build()

        player = ExoPlayer.Builder(this)
            .build()
            .apply {
                notificationManager.setUseFastForwardAction(false)
                notificationManager.setUseRewindAction(false)
                notificationManager.setPlayer(this)
                notificationManager.setColorized(true)
                notificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                notificationManager.setPriority(NotificationCompat.PRIORITY_MAX)


                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                addListener(
                    object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            updateMusicInfo()
                        }
                    }
                )
                musicList.forEach {
                    addMediaItem(MediaItem.fromUri(it.mediaUri))
                }

                updateMusicInfo()
            }
    }

    fun getMusicState() = currentMusicState

    fun setMusicStateListener(listener: ((MusicState) -> Unit)?) {
        stateUpdateListener = listener
    }

    fun playNext() {
        player?.seekToNextMediaItem()
        updateMusicInfo()
    }

    fun togglePlayPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
        updateMusicInfo()
    }

    fun playPrevious() {
        player?.seekToPreviousMediaItem()
        updateMusicInfo()
    }

    private fun updateMusicInfo() {
        val currentTrack = musicList[player?.currentMediaItemIndex ?: 0]
        currentMusicState = MusicState(
            isPlaying = player?.isPlaying ?: false,
            track = currentTrack
        ).also {
            stateUpdateListener?.invoke(it)
        }
    }

    override fun onDestroy() {
        player?.release()
        super.onDestroy()
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder = binder

    data class MusicState(
        val isPlaying: Boolean,
        val track: Track
    )

    class MediaDescriptionAdapter(
        private val musicList: List<Track>
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return musicList[player.currentMediaItemIndex].title
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return null
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return musicList[player.currentMediaItemIndex].artist
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val icon = musicList[player.currentMediaItemIndex].icon
            return icon?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }

    }
}

