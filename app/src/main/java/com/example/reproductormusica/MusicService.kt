package com.example.reproductormusica

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1
    }

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null

    var songs: List<Song> = emptyList()
    var currentIndex: Int = -1
        private set

    var onSongChanged: ((Int) -> Unit)? = null
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    var onCompletion: (() -> Unit)? = null

    val isPlaying: Boolean get() = mediaPlayer?.isPlaying == true
    val currentPosition: Int get() = mediaPlayer?.currentPosition ?: 0
    val duration: Int get() = mediaPlayer?.duration ?: 0

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    // ─── Playback controls ────────────────────────────────────────────────────

    fun playSong(index: Int) {
        if (index < 0 || index >= songs.size) return
        currentIndex = index
        val song = songs[index]

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(song.path)
            } catch (e: Exception) {
                release()
                mediaPlayer = null
                onPlaybackStateChanged?.invoke(false)
                return
            }
            setOnPreparedListener { player ->
                player.start()
                onSongChanged?.invoke(currentIndex)
                onPlaybackStateChanged?.invoke(true)
                startForeground(NOTIFICATION_ID, buildNotification(song))
            }
            setOnErrorListener { _, _, _ ->
                onPlaybackStateChanged?.invoke(false)
                true
            }
            setOnCompletionListener {
                onCompletion?.invoke()
                playNext()
            }
            prepareAsync()
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            onPlaybackStateChanged?.invoke(false)
        } else {
            player.start()
            onPlaybackStateChanged?.invoke(true)
        }
        updateNotification()
    }

    fun playNext() {
        if (songs.isEmpty()) return
        val next = (currentIndex + 1) % songs.size
        playSong(next)
    }

    fun playPrevious() {
        if (songs.isEmpty()) return
        // If more than 3 seconds into a song, restart it; otherwise go to previous
        if (currentPosition > 3000) {
            seekTo(0)
        } else {
            val prev = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
            playSong(prev)
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    // ─── Notification ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para la reproducción de música en segundo plano"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(song: Song): Notification {
        val playerIntent = Intent(this, PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, playerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        if (currentIndex < 0 || currentIndex >= songs.size) return
        val notification = buildNotification(songs[currentIndex])
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
