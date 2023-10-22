package com.example.musicplayer

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var position = 0

        val playBtn = findViewById<ImageView>(R.id.iv_start)
        val nextBtn = findViewById<ImageView>(R.id.iv_next)
        val previousBtn = findViewById<ImageView>(R.id.iv_previous)


        val face = R.raw.face_salam
        val queen = R.raw.queen_we_are_the_champions
        val rick = R.raw.rick_astley_never_gonna_give_you_up
        val shrek = R.raw.shrek


        val musicList = arrayListOf(
            face,
            queen,
            rick,
            shrek,
        )

        val player: MediaPlayer = MediaPlayer.create(this, musicList[position])



        playBtn.setOnClickListener {
            if (!player.isPlaying) {
                player.start()
                playBtn.setImageResource(R.drawable.ic_stop)
            } else {
                player.pause()
                playBtn.setImageResource(R.drawable.ic_start)
            }
        }

        nextBtn.setOnClickListener {
            if (player.isPlaying) {
                player.stop()
                player.reset()
                val nextTrackFd =
                    resources.openRawResourceFd(musicList[++position % musicList.size])
                player.setDataSource(nextTrackFd)
                player.prepare()
                player.start()
            }
        }


    }
}