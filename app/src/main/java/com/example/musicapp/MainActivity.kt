package com.example.musicapp

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout

val ATLEAST_TIRAMISU = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

class MainActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: SongListAdapter
    var musicService: MusicService? = null
    var txtTitle : TextView? = null
    var txtAuthor : TextView? = null
    var imgSong : ImageView? = null
    var txtTitlePlay : TextView? = null
    var txtAuthorPlay : TextView? = null
    var imgSongPlay : ImageView? = null
    var playerView : LinearLayout? = null
    var playControl : ConstraintLayout? = null
    var skipNext : TextView? = null
    var skipEnd : TextView? = null
    var btnPlayPause: ImageView? = null
    var btnPlaySong: ImageView? = null
    private var currentSongIndex = -1
    val listSong = MutableLiveData<List<Song>>(emptyList())
    private var isSuffer = false
    var animation: Animation? = null
    var seekBar: SeekBar? = null
    var startPoint = 0
    var endPoint = 0
    companion object {
        const val REQUEST_CODE_PLAY_SONG = 1001
    }

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
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        recyclerView = findViewById(R.id.rcv_song)
        txtTitle = findViewById(R.id.txt_play_name)
        txtAuthor = findViewById(R.id.txt_play_author)
        imgSong = findViewById(R.id.imv_play_song)
        btnPlayPause = findViewById<ImageView>(R.id.btn_play_pause)

        txtTitlePlay = findViewById(R.id.txt_songname)
        txtAuthorPlay = findViewById(R.id.txt_author)
        btnPlaySong = findViewById<ImageView>(R.id.play)
        imgSongPlay = findViewById(R.id.imv_image_song)
        skipNext = findViewById(R.id.txt_skipNext)
        skipEnd = findViewById(R.id.txt_skipEnd)
        seekBar = findViewById(R.id.seekbar)
        animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.rotate)


        adapter = SongListAdapter(listener = {view ->
            val position = recyclerView.getChildAdapterPosition(view)
            val song = adapter.listSong[position]
            currentSongIndex = position

            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.putExtra("SONG_ID", song.id)
            txtTitle?.text = song.title
            txtAuthor?.text = song.title
            val url : Uri? = song.image
            Glide.with(this)
                .load(url)
                .circleCrop()
                .into(imgSong!!)
            intent.action = "ACTION_PLAY"

            txtTitlePlay?.text = song.title
            txtAuthorPlay?.text = song.title
            val url1 : Uri? = song.image
            Glide.with(this)
                .load(url1)
                .circleCrop()
                .into(imgSongPlay!!)
            val songDurationInSeconds = song.duration
            val minutes = (songDurationInSeconds % (1000 * 60 * 60)) / (1000 * 60)
            val seconds = (songDurationInSeconds % (1000 * 60 * 60)) % (1000 * 60) /1000
            val formattedDuration = String.format("%02d:%02d", minutes, seconds)
            skipEnd?.text = formattedDuration

            intent.action = "ACTION_PLAY"
            startService(intent)
            imgSongPlay?.startAnimation(animation)
            playControl?.visibility = View.VISIBLE
        })


        recyclerView.adapter = adapter
        val btnNext = findViewById<TextView>(R.id.btn_next)
        btnNext.setOnClickListener {
            currentSongIndex++
            nextSong(currentSongIndex, imgSong!!, txtTitle!!, txtAuthor!!, skipEnd!!, imgSongPlay!!, txtTitlePlay!!, txtAuthorPlay!!)
        }
        val btnNextPlayer = findViewById<TextView>(R.id.skipNext)
        btnNextPlayer.setOnClickListener {
            currentSongIndex++
            nextSong(currentSongIndex, imgSongPlay!!, txtTitlePlay!!, txtAuthorPlay!!, skipEnd!!, imgSong!!, txtTitle!!, txtAuthor!!)
        }

        val btnPrev = findViewById<TextView>(R.id.btn_prev)
        btnPrev.setOnClickListener {
            currentSongIndex--
            prevSong(currentSongIndex, imgSong!!, txtTitle!!, txtAuthor!!, skipEnd!!, imgSongPlay!!, txtTitlePlay!!, txtAuthorPlay!!)
        }
        val btnPrevPlayer = findViewById<TextView>(R.id.skipPrevious)
        btnPrevPlayer.setOnClickListener {
            currentSongIndex--
            prevSong(currentSongIndex, imgSongPlay!!, txtTitlePlay!!, txtAuthorPlay!!, skipEnd!!, imgSong!!, txtTitle!!, txtAuthor!!)
        }


        btnPlayPause?.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            if(musicService != null && musicService!!.mediaPlayer.isPlaying) {
                intent.action = "ACTION_PAUSE"
                btnPlayPause?.setImageResource(R.drawable.baseline_play_arrow_24)
                btnPlaySong?.setImageResource(R.drawable.baseline_play_circle_24)
                animation?.cancel()
                imgSongPlay?.clearAnimation()
            } else {
                intent.action = "ACTION_CONTINUE"
                btnPlayPause?.setImageResource(R.drawable.baseline_pause_24)
                btnPlaySong?.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                imgSongPlay?.startAnimation(animation)
            }
            startService(intent)
        }

        btnPlaySong?.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            if(musicService != null && musicService!!.mediaPlayer.isPlaying) {
                intent.action = "ACTION_PAUSE"
                btnPlaySong?.setImageResource(R.drawable.baseline_play_circle_24)
                btnPlayPause?.setImageResource(R.drawable.baseline_play_arrow_24)
                animation?.cancel()
                imgSongPlay?.clearAnimation()
            } else {
                intent.action = "ACTION_CONTINUE"
                btnPlaySong?.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                btnPlayPause?.setImageResource(R.drawable.baseline_pause_24)
//                animation?.cancel()
                imgSongPlay?.startAnimation(animation)

            }
            startService(intent)
        }

        val repeatAll = findViewById<TextView>(R.id.repeat)
        repeatAll.setOnClickListener{
            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.action = "ACTION_REPEAT_ONE"
            startService(intent)
            val clickedColor = ContextCompat.getColor(this@MainActivity, R.color.color_click)
            repeatAll.setTextColor(clickedColor)
            repeatAll.compoundDrawables.forEach { drawable ->
                if (drawable != null) {
                    drawable.setColorFilter(clickedColor, PorterDuff.Mode.SRC_ATOP)
                }
            }
        }
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                skipNext?.text = progress.toString()
                musicService!!.mediaPlayer.seekTo(progress)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                startPoint = seekBar!!.progress
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                endPoint = seekBar!!.progress
//                val newPosition = seekBar?.progress ?: 0

                // Gửi vị trí mới tới MusicService để seek đến vị trí mới
                val intent = Intent(this@MainActivity, MusicService::class.java)
                intent.action = "ACTION_SEEK"
                intent.putExtra("SEEK_POS", endPoint)
                startService(intent)
            }

        })

        playerControls()

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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PLAY_SONG && resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra("TITLE1")
            val author = data?.getStringExtra("AUTHOR1")
            val image = data?.getParcelableExtra<Uri>("IMAGE1")

            // Cập nhật giao diện tại đây
            txtTitle?.text = title
            txtAuthor?.text = author
            imgSong?.setImageURI(image)
        }
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

    fun playerControls() {
        playerView = findViewById(R.id.play_view)
        playControl = findViewById(R.id.controller)
        val btnDown = findViewById<ImageView>(R.id.btn_down)
        val appbar = findViewById<AppBarLayout>(R.id.appbar)
        playerView?.setOnClickListener {playControl?.visibility = View.GONE }
        playControl?.setOnClickListener {
            playerView?.visibility = View.VISIBLE
            appbar.visibility = View.GONE
        }
        btnDown.setOnClickListener{
            playerView?.visibility = View.GONE
            appbar.visibility = View.VISIBLE
        }

    }

    fun nextSong(currentSongIndex: Int, image : ImageView, txtTitle : TextView, txtAuthor: TextView,
                 skipEnd: TextView, image1 : ImageView, txtTitle1 : TextView, txtAuthor1: TextView) {
        //            var index = 0
//            if(currentSongIndex != -1) {
//                index = currentSongIndex + 1
//                if(index >= listSong.value!!.size) {
//                    index = 0
//                }
//            }
//            currentSongIndex = index
        val intent = Intent(this@MainActivity, MusicService::class.java)
        intent.action = "ACTION_NEXT"
        txtTitle.text = musicService?.listSong?.value!![currentSongIndex].title
        txtAuthor.text = musicService?.listSong?.value!![currentSongIndex].author
        val url : Uri = musicService?.listSong?.value!![currentSongIndex].image
        Glide.with(this)
            .load(url)
            .circleCrop()
            .into(image)
        txtTitle1.text = musicService?.listSong?.value!![currentSongIndex].title
        txtAuthor1.text = musicService?.listSong?.value!![currentSongIndex].author
        val url1 : Uri = musicService?.listSong?.value!![currentSongIndex].image
        Glide.with(this)
            .load(url1)
            .circleCrop()
            .into(image1)
        val songDurationInSeconds = musicService?.listSong?.value!![currentSongIndex].duration
        val minutes = (songDurationInSeconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (songDurationInSeconds % (1000 * 60 * 60)) % (1000 * 60) /1000
        val formattedDuration = String.format("%02d:%02d", minutes, seconds)
        skipEnd?.text = formattedDuration
        startService(intent)
    }
    fun prevSong(currentSongIndex: Int, image : ImageView, txtTitle : TextView, txtAuthor: TextView, skipEnd: TextView,
                 image1 : ImageView, txtTitle1 : TextView, txtAuthor1: TextView) {
        val intent = Intent(this@MainActivity, MusicService::class.java)
        intent.action = "ACTION_PREV"
        txtTitle.text = musicService?.listSong?.value!![currentSongIndex].title
        txtAuthor.text = musicService?.listSong?.value!![currentSongIndex].author
        val url : Uri = musicService?.listSong?.value!![currentSongIndex].image
        Glide.with(this)
            .load(url)
            .circleCrop()
            .into(image)

        txtTitle1.text = musicService?.listSong?.value!![currentSongIndex].title
        txtAuthor1.text = musicService?.listSong?.value!![currentSongIndex].author
        val url1 : Uri = musicService?.listSong?.value!![currentSongIndex].image
        Glide.with(this)
            .load(url1)
            .circleCrop()
            .into(image1)

        val songDurationInSeconds = musicService?.listSong?.value!![currentSongIndex].duration
        val minutes = (songDurationInSeconds % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (songDurationInSeconds % (1000 * 60 * 60)) % (1000 * 60) /1000
        val formattedDuration = String.format("%02d:%02d", minutes, seconds)
        skipEnd?.text = formattedDuration
        startService(intent)
    }






}