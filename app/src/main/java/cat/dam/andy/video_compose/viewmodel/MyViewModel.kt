import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyViewModel : ViewModel() {

    private val _playerState = MutableStateFlow<ExoPlayer?>(null)
    val playerState: StateFlow<ExoPlayer?> = _playerState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun initializePlayer(context: Context, mediaList: List<MediaItem>) {
        if (_playerState.value == null) {
            val player = ExoPlayer.Builder(context).build()
            player.setMediaItems(mediaList)
            player.addListener(playerListener())
            player.prepare()
            _playerState.value = player
        }
    }

    fun getMediaItemNumber() = _playerState.value?.currentMediaItemIndex

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun releasePlayer() {
        _playerState.value?.release()
        _playerState.value = null
    }

    override fun onCleared() {
        releasePlayer()
    }

    private fun playerListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d("ExoPlayer:", "changed state to $stateString")
        }

        override fun onPlayerError(error: PlaybackException) {
            if (error.cause is androidx.media3.common.ParserException) {
                val errorMessage = "Error: Incorrect MIME type or malformed media file."
                Log.e("ExoPlayer:", errorMessage)
                _errorMessage.value = errorMessage
            } else {
                Log.e("ExoPlayer:", "Error: ${error.message}")
                _errorMessage.value = "Error: ${error.message}"
            }
        }
    }
}