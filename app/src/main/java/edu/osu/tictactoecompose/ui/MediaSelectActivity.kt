package edu.osu.tictactoecompose.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.media.MediaPlaybackService

/**
 * Audio playback Activity using XML and ExoPlayer. (As of June 2, 2026, Jetpack Compose support
 * for ExoPlayer remains a work-in-progress.)
 *
 * @author acc
 */
class MediaSelectActivity : AppCompatActivity() {
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    private val browser: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null

    private lateinit var mediaListAdapter: FolderMediaItemArrayAdapter
    private lateinit var mediaListView: ListView
    private val treePathStack: ArrayDeque<MediaItem> = ArrayDeque()
    private var subItemMediaList: MutableList<MediaItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setting up the layout
        setContentView(R.layout.activity_media_select)
        mediaListView = findViewById(R.id.media_list_view)
        mediaListAdapter = FolderMediaItemArrayAdapter(this, R.layout.folder_items, subItemMediaList)
        mediaListView.adapter = mediaListAdapter

        // setting up on click. When user click on an item, try to display it
        mediaListView.setOnItemClickListener { _, _, position, _ ->
            run {
                val selectedMediaItem = mediaListAdapter.getItem(position)!!
                // TODO(b/192235359): handle the case where the item is playable but it is not a folder
                if (selectedMediaItem.mediaMetadata.isPlayable == true) {
                    val intent = PlayableFolderActivity.createIntent(this, selectedMediaItem.mediaId)
                    startActivity(intent)
                } else {
                    pushPathStack(selectedMediaItem)
                }
            }
        }

        findViewById<ExtendedFloatingActionButton>(R.id.open_player_floating_button)
            .setOnClickListener {
                // Start the session activity that shows the playback activity. The System UI uses the same
                // intent in the same way to start the activity from the notification.
                browser?.sessionActivity?.send()
            }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    popPathStack()
                }
            }
        )

        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), /* requestCode= */ 0)
        }
    }

    @OptIn(UnstableApi::class) // MediaRouteButtonFactory is unstable API.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.select_media_layout)) { v, windowInsets ->
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
        initializeBrowser()
    }

    override fun onStop() {
        releaseBrowser()
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) {
            // Empty results are triggered if a permission is requested while another request was already
            // pending and can be safely ignored in this case.
            return
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, R.string.notification_permission_denied, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun initializeBrowser() {
        browserFuture =
            MediaBrowser.Builder(
                this,
                SessionToken(this, ComponentName(this, MediaPlaybackService::class.java)),
            )
                .buildAsync()
        browserFuture.addListener({ pushRoot() }, ContextCompat.getMainExecutor(this))
    }

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(browserFuture)
    }

    private fun displayChildrenList(mediaItem: MediaItem) {
        val browser = this.browser ?: return

        supportActionBar!!.setDisplayHomeAsUpEnabled(treePathStack.size != 1)
        val childrenFuture =
            browser.getChildren(
                mediaItem.mediaId,
                /* page= */ 0,
                /* pageSize= */ Int.MAX_VALUE,
                /* params= */ null,
            )

        subItemMediaList.clear()
        childrenFuture.addListener(
            {
                val result = childrenFuture.get()!!
                val children = result.value!!
                subItemMediaList.addAll(children)
                mediaListAdapter.notifyDataSetChanged()
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun pushPathStack(mediaItem: MediaItem) {
        treePathStack.addLast(mediaItem)
        displayChildrenList(treePathStack.last())
    }

    private fun popPathStack() {
        treePathStack.removeLast()
        if (treePathStack.isEmpty()) {
            finish()
            return
        }

        displayChildrenList(treePathStack.last())
    }

    private fun pushRoot() {
        // browser can be initialized many times
        // only push root at the first initialization
        if (!treePathStack.isEmpty()) {
            return
        }
        val browser = this.browser ?: return
        val rootFuture = browser.getLibraryRoot(/* params= */ null)
        rootFuture.addListener(
            {
                val result: LibraryResult<MediaItem> = rootFuture.get()!!
                val root: MediaItem = result.value!!
                pushPathStack(root)
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private class FolderMediaItemArrayAdapter(
        context: Context,
        viewID: Int,
        mediaItemList: List<MediaItem>,
    ) : ArrayAdapter<MediaItem>(context, viewID, mediaItemList) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val mediaItem = getItem(position)!!
            val returnConvertView =
                convertView ?: LayoutInflater.from(context).inflate(R.layout.folder_items, parent, false)

            returnConvertView.findViewById<TextView>(R.id.media_item).text = mediaItem.mediaMetadata.title
            return returnConvertView
        }
    }
}