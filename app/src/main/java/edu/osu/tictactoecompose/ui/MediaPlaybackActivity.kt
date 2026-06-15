package edu.osu.tictactoecompose.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_MEDIA_ITEM_TRANSITION
import androidx.media3.common.Player.EVENT_MEDIA_METADATA_CHANGED
import androidx.media3.common.Player.EVENT_TIMELINE_CHANGED
import androidx.media3.common.Player.EVENT_TRACKS_CHANGED
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.databinding.ActivityMediaPlaybackBinding
import edu.osu.tictactoecompose.media.MediaPlaybackService
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import timber.log.Timber

private val classTag = "AudioActivity"

public class MediaPlaybackActivity : AppCompatActivity(), View.OnClickListener {
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    private lateinit var mediaFileUri: Uri

    private val viewBinding by lazy(mode = LazyThreadSafetyMode.NONE) {
        ActivityMediaPlaybackBinding.inflate(layoutInflater)
    }

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var controller: MediaController

    private lateinit var playerView: PlayerView
    private lateinit var mediaItemListView: ListView
    private lateinit var mediaItemListAdapter: MediaItemListAdapter
    private val mediaItemList: MutableList<MediaItem> = mutableListOf()



    private var recordAudioResult = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                if (intent.data != null) {
                    mediaFileUri = intent.data!!
                }
                Timber.v("Audio File URI: %s", "URI")
            }
        }
    }
    private var pickAudioResult = registerForActivityResult(
        GetContent()
    ) { result ->
        val uriString = result.toString()
        mediaFileUri = uriString.toUri()
    }

    @OptIn(ExperimentalMaterial3Api::class, UnstableApi::class) // PlayerView.hideController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(viewBinding.root)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    initializeController()
                    awaitCancellation()
                } finally {
                    playerView.player = null
                    releaseController()
                }
            }
        }

        playerView = findViewById(R.id.media_player_view)

        mediaItemListView = findViewById(R.id.current_playing_list)
        mediaItemListAdapter = MediaItemListAdapter(this, R.layout.folder_items, mediaItemList)
        mediaItemListView.adapter = mediaItemListAdapter
        mediaItemListView.setOnItemClickListener { _, _, position, _ ->
            run {
                if (controller.currentMediaItemIndex == position) {
                    controller.playWhenReady = !controller.playWhenReady
                    if (controller.playWhenReady) {
                        playerView.hideController()
                    }
                } else {
                    controller.seekToDefaultPosition(/* mediaItemIndex= */ position)
                    mediaItemListAdapter.notifyDataSetChanged()
                }
            }
        }

        viewBinding.buttonMediaRecord.setOnClickListener(this)
        viewBinding.buttonMediaSelect.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.media_playback_layout)) { v, windowInsets ->
            val sysBarsCutout = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout() or
                        WindowInsetsCompat.Type.systemGestures()
            )
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = sysBarsCutout.left
                topMargin = sysBarsCutout.top
                rightMargin = sysBarsCutout.right
                bottomMargin = sysBarsCutout.bottom
            }
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED;
        }

        //initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (player == null) {
            initializePlayer()
        }

        try {
            val actionBar = supportActionBar
            if (actionBar != null) {
                actionBar.subtitle = resources.getString(R.string.audio)
            }
        } catch (npe: NullPointerException) {
            Timber.e("Could not set subtitle")
        }


    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onStop() {
        super.onStop()
        //releasePlayer()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->

                viewBinding.mediaPlayerView.player = exoPlayer

                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSizeSd()
                    .build()

                //val mediaItem = MediaItem.fromUri(getString(R.string.media_url_mp4))
                val mediaItem = MediaItem.Builder()
                    .setUri(getString(R.string.media_url_dash))
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build()
                exoPlayer.setMediaItems(listOf(mediaItem), currentItem, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener())
            exoPlayer.release()
        }
        player = null
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    private suspend fun initializeController() {
        controllerFuture =
            MediaController.Builder(
                this,
                SessionToken(this, ComponentName(this, MediaPlaybackService::class.java)),
            )
                .buildAsync()
        updateMediaMetadataUI()
        setController()
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }

    @OptIn(UnstableApi::class) // PlayerView.setShowSubtitleButton
    private suspend fun setController() {
        try {
            controller = controllerFuture.await()
        } catch (t: Throwable) {
            Log.w(classTag, "Failed to connect to MediaController", t)
            return
        }
        playerView.player = controller
        // playerView.setMediaRouteButtonViewProvider(MediaRouteButtonViewProvider())

        updateCurrentPlaylistUI()
        updateMediaMetadataUI()
        playerView.setShowSubtitleButton(controller.currentTracks.isTypeSupported(TRACK_TYPE_TEXT))

        controller.addListener(
            object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    if (events.contains(EVENT_TRACKS_CHANGED)) {
                        playerView.setShowSubtitleButton(player.currentTracks.isTypeSupported(TRACK_TYPE_TEXT))
                    }
                    if (events.contains(EVENT_TIMELINE_CHANGED)) {
                        updateCurrentPlaylistUI()
                    }
                    if (events.contains(EVENT_MEDIA_METADATA_CHANGED)) {
                        updateMediaMetadataUI()
                    }
                    if (events.contains(EVENT_MEDIA_ITEM_TRANSITION)) {
                        // Trigger adapter update to change highlight of current item.
                        mediaItemListAdapter.notifyDataSetChanged()
                    }
                }
            }
        )
    }

    private fun updateMediaMetadataUI() {
        if (!::controller.isInitialized || controller.mediaItemCount == 0) {
            findViewById<TextView>(R.id.media_title).text = getString(R.string.waiting_for_metadata)
            findViewById<TextView>(R.id.media_artist).text = ""
            return
        }

        val mediaMetadata = controller.mediaMetadata
        val title: CharSequence = mediaMetadata.title ?: ""

        findViewById<TextView>(R.id.media_title).text = title
        findViewById<TextView>(R.id.media_artist).text = mediaMetadata.artist
    }

    private fun updateCurrentPlaylistUI() {
        if (!::controller.isInitialized) {
            return
        }
        mediaItemList.clear()
        for (i in 0 until controller.mediaItemCount) {
            mediaItemList.add(controller.getMediaItemAt(i))
        }
        mediaItemListAdapter.notifyDataSetChanged()
    }

    private inner class MediaItemListAdapter(
        context: Context,
        viewID: Int,
        mediaItemList: List<MediaItem>,
    ) : ArrayAdapter<MediaItem>(context, viewID, mediaItemList) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val mediaItem = getItem(position)!!
            val returnConvertView =
                convertView ?: LayoutInflater.from(context).inflate(R.layout.playlist_items, parent, false)

            returnConvertView.findViewById<TextView>(R.id.media_item).text = mediaItem.mediaMetadata.title

            val deleteButton = returnConvertView.findViewById<Button>(R.id.delete_button)
            if (::controller.isInitialized && position == controller.currentMediaItemIndex) {
                // Styles for the current media item list item.
                returnConvertView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.playlist_item_background)
                )
                returnConvertView
                    .findViewById<TextView>(R.id.media_item)
                    .setTextColor(ContextCompat.getColor(context, R.color.white))
                deleteButton.visibility = View.GONE
            } else {
                // Styles for any other media item list item.
                returnConvertView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.player_background)
                )
                returnConvertView
                    .findViewById<TextView>(R.id.media_item)
                    .setTextColor(ContextCompat.getColor(context, R.color.white))
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    controller.removeMediaItem(position)
                    updateCurrentPlaylistUI()
                }
            }

            return returnConvertView
        }
    }
}

private fun playbackStateListener() = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE             -"
        }
        Timber.tag("MediaPlaybackListener").i("Changed state to $stateString")
    }
}
