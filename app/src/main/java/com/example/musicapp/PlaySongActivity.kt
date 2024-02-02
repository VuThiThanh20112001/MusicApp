package com.example.musicapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PlaySongActivity : AppCompatActivity() {

    lateinit var adapter: SongListAdapter
    var musicService: MusicService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_view)
        val txtTitle = findViewById<TextView>(R.id.txt_songname)
        val txtAuthor = findViewById<TextView>(R.id.txt_author)
        val imgSong = findViewById<ImageView>(R.id.imv_image_song)
        val btnDown = findViewById<TextView>(R.id.btn_down)

        txtTitle.text = intent.getStringExtra("TITLE")
        txtAuthor.text = intent.getStringExtra("AUTHOR")
        val url : Uri? = intent.getParcelableExtra("IMAGE")
//        imgSong.setImageURI(intent.getParcelableExtra("IMAGE"))
        Glide.with(this)
            .load(url)
            .circleCrop()
            .into(imgSong)


        btnDown.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val btnNext = findViewById<TextView>(R.id.skipNext)
        btnNext.setOnClickListener {
            val intent = Intent(this@PlaySongActivity, MusicService::class.java)
            intent.action = "ACTION_NEXT"
            startService(intent)
        }
        val btnPrev = findViewById<TextView>(R.id.skipPrevious)
        btnPrev.setOnClickListener {
            val intent = Intent(this@PlaySongActivity, MusicService::class.java)
            intent.action = "ACTION_PREV"
            startService(intent)
        }

        val btnPlayPause = findViewById<TextView>(R.id.play)
        btnPlayPause.setOnClickListener {
            val intent = Intent(this@PlaySongActivity, MusicService::class.java)
            if(musicService != null && musicService!!.mediaPlayer.isPlaying) {
                intent.action = "ACTION_PAUSE"
            } else {
                intent.action = "ACTION_CONTINUE"
            }
            startService(intent)
        }

    }

}