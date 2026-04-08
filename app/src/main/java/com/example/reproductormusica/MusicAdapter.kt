package com.example.reproductormusica

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reproductormusica.databinding.ItemSongBinding

class MusicAdapter(
    private val onSongClick: (Song, Int) -> Unit
) : ListAdapter<Song, MusicAdapter.SongViewHolder>(SongDiffCallback()) {

    private var activeSongIndex: Int = -1

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int) {
            binding.tvSongTitle.text = song.title
            binding.tvSongArtist.text = song.artist
            binding.tvDuration.text = song.formattedDuration()

            // Highlight the currently playing song
            binding.root.isSelected = (position == activeSongIndex)

            binding.root.setOnClickListener {
                onSongClick(song, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /** Updates which song is highlighted as currently playing */
    fun setActiveSong(index: Int) {
        val previous = activeSongIndex
        activeSongIndex = index
        if (previous >= 0) notifyItemChanged(previous)
        if (index >= 0) notifyItemChanged(index)
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem == newItem
    }
}
