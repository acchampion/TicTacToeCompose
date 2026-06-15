package edu.osu.tictactoecompose.media

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.core.app.TaskStackBuilder

class MediaPlaybackService : BasicPlaybackService() {

    override fun getSingleTopActivity(): PendingIntent? {
        return getActivity(
            this,
            0,
            Intent(this, edu.osu.tictactoecompose.ui.MediaPlaybackActivity::class.java),
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
        )
    }

    override fun getBackStackedActivity(): PendingIntent? {
        return TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@MediaPlaybackService, edu.osu.tictactoecompose.ui.MediaSelectActivity::class.java))
            addNextIntent(Intent(this@MediaPlaybackService, edu.osu.tictactoecompose.ui.MediaPlaybackActivity::class.java))
            getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        }
    }
}
