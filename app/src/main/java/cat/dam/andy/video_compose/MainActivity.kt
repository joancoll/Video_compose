package cat.dam.andy.video_compose

import MyViewModel
import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import cat.dam.andy.video_compose.ui.theme.Video_composeTheme
import androidx.media3.ui.PlayerView
import cat.dam.andy.video_compose.model.MediaElement
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MyViewModel>()

    override fun onStart() {
        super.onStart()
        viewModel.initializePlayer(this, getMediaItems(playList()))
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe the errorMessage StateFlow
        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let {
                    val mediaItemNumber = viewModel.getMediaItemNumber()
                    if (mediaItemNumber != null) {
                        val mediaElement = playList()[mediaItemNumber]
                        val toastMessage = "$it: ${mediaElement.title} (${mediaElement.mimeType})"
                        Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                // clear the error message
                viewModel.clearErrorMessage()
            }
        }

        // Observe the currentMediaItemIndex
        lifecycleScope.launch {
            viewModel.playerState.collect { player ->
                player?.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val mediaItemNumber = viewModel.getMediaItemNumber()
                        if (mediaItemNumber != null) {
                            val mediaElement = playList()[mediaItemNumber]
                            val toastMessage = "Now playing: ${mediaElement.title} (${mediaElement.mimeType})"
                            Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }

        setContent {
            Video_composeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(viewModel)
                }
            }
        }
    }

    @Composable
    fun MyApp(viewModel: MyViewModel) {
        val playerState by viewModel.playerState.collectAsState()
        playerState?.let {
            AndroidView(factory = { context ->
                PlayerView(context).apply {
                    this.player = it
                }
            })
        }
    }

}

private fun getMediaItems(playList: List<MediaElement>): List<MediaItem> {
    val mediaItems = mutableListOf<MediaItem>()
    playList.forEach { element ->
        val mediaItem = MediaItem.Builder()
            .setUri(element.mediaLink)
            .setMimeType(element.mimeType)
            .build()
        mediaItems.add(mediaItem)
    }
    return mediaItems
}

private fun playList(): List<MediaElement> = listOf(
    MediaElement(
        title = "Big Buck Bunny",
        description = "About a rabbit",
        author = "Blender Foundation",
        mediaLink = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
        mimeType = MimeTypes.VIDEO_MP4
    ),
    MediaElement(
        title = "Google Glasses",
        description = "About Google glasses",
        author = "Google Inc",
        mediaLink = "https://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0",
        mimeType = MimeTypes.APPLICATION_MPD
    ),
    MediaElement(
        title = "Jazz in Paris",
        description = "About Jazz in cities",
        author = "Jazz Friends % Company",
        mediaLink = "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3",
        mimeType = MimeTypes.AUDIO_MP4
    ),
)




