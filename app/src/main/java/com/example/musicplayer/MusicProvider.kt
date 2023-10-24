package com.example.musicplayer

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

object MusicProvider {
    private val musicList = listOf(
        R.raw.drip,
        R.raw.hollywoods_bleeding,
        R.raw.look_alive,
        R.raw.face_salam,
        R.raw.queen_we_are_the_champions,
        R.raw.rick_astley_never_gonna_give_you_up,
        R.raw.shrek
    )

    fun getMusicList(context: Context) = musicList.map {
        getTrackInfo(context, it)
    }

    private fun getTrackInfo(context: Context, id: Int): Track {
        val retriever = MediaMetadataRetriever()

        val mediaUri =
            Uri.parse(
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$id"
            )
        retriever.setDataSource(context, mediaUri)

        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val icon = retriever.embeddedPicture

        return Track(title.orEmpty(), artist.orEmpty(), icon, mediaUri)
    }
}