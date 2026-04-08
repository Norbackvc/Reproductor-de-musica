package com.example.reproductormusica

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.reproductormusica.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var musicService: MusicService? = null
    private var isBound = false

    private val handler = Handler(Looper.getMainLooper())
    private val seekBarUpdater = object : Runnable {
        override fun run() {
            updateSeekBar()
            handler.postDelayed(this, 500)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            musicService = (binder as MusicService.MusicBinder).getService()
            isBound = true
            updateUI()

            musicService?.onSongChanged = { runOnUiThread { updateUI() } }
            musicService?.onPlaybackStateChanged = { playing ->
                runOnUiThread { updatePlayPauseButton(playing) }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(seekBarUpdater)
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    // ─── UI setup ─────────────────────────────────────────────────────────────

    private fun setupListeners() {
        binding.btnPlayPause.setOnClickListener {
            musicService?.togglePlayPause()
        }

        binding.btnNext.setOnClickListener {
            musicService?.playNext()
        }

        binding.btnPrevious.setOnClickListener {
            musicService?.playPrevious()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService?.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun updateUI() {
        val service = musicService ?: return
        val songs = service.songs
        val index = service.currentIndex
        if (index < 0 || index >= songs.size) return

        val song = songs[index]
        binding.tvTitle.text = song.title
        binding.tvArtist.text = song.artist
        binding.tvAlbum.text = song.album
        binding.seekBar.max = service.duration
        updatePlayPauseButton(service.isPlaying)
        handler.post(seekBarUpdater)
    }

    private fun updatePlayPauseButton(playing: Boolean) {
        binding.btnPlayPause.setImageResource(
            if (playing) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun updateSeekBar() {
        val service = musicService ?: return
        binding.seekBar.progress = service.currentPosition
        binding.tvCurrentTime.text = formatTime(service.currentPosition)
        binding.tvTotalTime.text = formatTime(service.duration)
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
