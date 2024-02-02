package com.example.musicapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide

class SongListAdapter(var listener: View.OnClickListener? = null) : RecyclerView.Adapter<SongListAdapter.SongViewHodel>() {

    var listSong: List<Song> = emptyList()
    inner class SongViewHodel(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle = itemView.findViewById<TextView>(R.id.song_title)
        val txtAuthor = itemView.findViewById<TextView>(R.id.song_author)
        val imvSong = itemView.findViewById<ImageView>(R.id.song_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHodel {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_item, parent, false)
        val holder = SongViewHodel(view)
        return holder
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: SongViewHodel, position: Int) {
        holder.txtTitle.text = listSong[position].title
        holder.txtAuthor.text = listSong[position].author
        Glide.with(holder.imvSong)
            .load(listSong[position].image)
            .circleCrop()
            .into(holder.imvSong);

        holder.itemView.tag = listSong[position]
        holder.itemView.setOnClickListener(listener)


    }

}