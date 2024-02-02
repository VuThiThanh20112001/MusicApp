package com.example.musicapp

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


val ATLEAST_TIRAMISU = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

class MainActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: SongListAdapter
    var musicService: MusicService? = null

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if(service is MusicService.Binder) {
                musicService = service.getService()
                musicService?.listSong?.
                observe(this@MainActivity) { listSongs ->
                    adapter.listSong = listSongs
                    adapter.notifyDataSetChanged()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rcv_song)
        val txtSongName = findViewById<TextView>(R.id.txt_play_name)
        val txtSongAuthor = findViewById<TextView>(R.id.txt_play_author)
        val imvSong = findViewById<ImageView>(R.id.imv_play_song)
        adapter = SongListAdapter(listener = {
            val song = it.tag as Song
            val intent1 = Intent(this, PlaySongActivity::class.java)
            intent1.putExtra("TITLE", song.title)
            intent1.putExtra("AUTHOR", song.author)
            intent1.putExtra("IMAGE", song.image)
            startActivity(intent1)

            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.putExtra("SONG_ID", song.id)
            txtSongName.text = song.title
            txtSongAuthor.text = song.title
            imvSong.setImageURI(song.image)
            intent.action = "ACTION_PLAY"
            startService(intent)
        })
        recyclerView.adapter = adapter

        val btnNext = findViewById<TextView>(R.id.btn_next)
        btnNext.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.action = "ACTION_NEXT"
            startService(intent)
        }

        val btnPrev = findViewById<TextView>(R.id.btn_prev)
        btnPrev.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.action = "ACTION_PREV"
            startService(intent)
        }

        val btnPlayPause = findViewById<TextView>(R.id.btn_play_pause)
        btnPlayPause.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            if(musicService != null && musicService!!.mediaPlayer.isPlaying) {
                intent.action = "ACTION_PAUSE"
            } else {
                intent.action = "ACTION_CONTINUE"
            }
            startService(intent)
        }

        if(checkNeedsPermission()) {
            startMusicService()
        } else {
            requestNeedsPermission()
        }
    }

    fun checkNeedsPermission(): Boolean {
        val result: Int
        if (!ATLEAST_TIRAMISU) {
            result = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            result = checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO)
        }
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun requestNeedsPermission() {
        if(!checkNeedsPermission()) {
            val permission: String
            if(!ATLEAST_TIRAMISU) {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE
            } else {
                permission = Manifest.permission.READ_MEDIA_AUDIO
            }
            requestPermissions(
                arrayOf(permission),
                999
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 999) {
            if(!checkNeedsPermission()) {
                finish()
            } else {
                startMusicService()
            }
        }
    }

    fun startMusicService() {
        val intent = Intent(this@MainActivity, MusicService::class.java)
        intent.action = "LOAD_DATA"
        startService(intent)
        bindService(intent, serviceConnection, 0)
    }
    fun optionMenu(menu: Menu) : Boolean {
        menuInflater.inflate(R.menu.search_btn, menu)
        val menuItem: MenuItem = menu.findItem(R.id.search)
        val searchView: SearchView =  menuItem?.actionView as SearchView

        searchSong(searchView)
        return onCreateOptionsMenu(menu)
    }

    fun searchSong(searchView: SearchView) {

    }
}