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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    var txtCurrentTime : TextView? = null
    var skipEnd : TextView? = null
    var btnPlayPause: ImageView? = null
    var btnPlaySong: ImageView? = null
    var isSuffer = false
    var animation: Animation? = null
    var seekBar: SeekBar? = null
    var startPoint = 0
    var endPoint = 0
    var isRepeatOneEnabled = false
    val handler = Handler(Looper.getMainLooper())
    val utilities = Utilities()
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
                observeCurrentSongTitle()
                updateSeekBar()
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
        btnPlayPause = findViewById(R.id.btn_play_pause)

        txtTitlePlay = findViewById(R.id.txt_songname)
        txtAuthorPlay = findViewById(R.id.txt_author)
        btnPlaySong = findViewById(R.id.play)
        imgSongPlay = findViewById(R.id.imv_image_song)
        txtCurrentTime = findViewById(R.id.txt_skipNext)
        skipEnd = findViewById(R.id.txt_skipEnd)
        seekBar = findViewById(R.id.seekbar)
        seekBar?.max = 100
        animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.rotate)
        val updateSeekBarRunnable = object : Runnable {
            override fun run() {
                // Gọi hàm updateSeekBar để cập nhật vị trí của seekbar
                updateSeekBar()
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(updateSeekBarRunnable, 1000)

        adapter = SongListAdapter(listener = {
            val song = it.tag as Song

            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.putExtra("SONG_ID", song.id)
            intent.action = "ACTION_PLAY"
            startService(intent)
            imgSongPlay?.startAnimation(animation)
            playControl?.visibility = View.VISIBLE

        })

        recyclerView.adapter = adapter
        val btnNext = findViewById<TextView>(R.id.btn_next)
        btnNext.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.action = "ACTION_NEXT"
            startService(intent)
        }
        val btnNextPlayer = findViewById<TextView>(R.id.skipNext)
        btnNextPlayer.setOnClickListener {
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
        val btnPrevPlayer = findViewById<TextView>(R.id.skipPrevious)
        btnPrevPlayer.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.action = "ACTION_PREV"
            startService(intent)
        }

        btnPlayPause?.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            if(musicService != null && musicService!!.mediaPlayer.isPlaying) {
                intent.action = "ACTION_PAUSE"
                btnPlayPause?.setImageResource(R.drawable.baseline_play_arrow_24)
                btnPlaySong?.setImageResource(R.drawable.baseline_play_circle_24)
                animation?.cancel()
                imgSongPlay?.clearAnimation()
//                handler.removeCallbacks(updateSeekBarRunnable)
            } else {
                intent.action = "ACTION_CONTINUE"
                btnPlayPause?.setImageResource(R.drawable.baseline_pause_24)
                btnPlaySong?.setImageResource(R.drawable.baseline_pause_circle_outline_24)
                imgSongPlay?.startAnimation(animation)
                updateSeekBar()
            }
            startService(intent)
        }

        btnPlaySong?.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            if(musicService != null && musicService!!.mediaPlayer.isPlaying) {
//                handler.removeCallbacks(updateSeekBarRunnable)
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
                updateSeekBar()
            }
            startService(intent)
        }

        val repeatAll = findViewById<TextView>(R.id.repeat)
        repeatAll.setOnClickListener{
            val intent = Intent(this@MainActivity, MusicService::class.java)
            val clickedColor = ContextCompat.getColor(this@MainActivity, R.color.color_click)
            if (!isRepeatOneEnabled) { // Nếu nút chưa được nhấn trước đó
                if (musicService != null && musicService!!.mediaPlayer.isPlaying) {
                    intent.action = "ACTION_REPEAT_ONE"
                    startService(intent)
                    repeatAll.setTextColor(clickedColor)
                    isRepeatOneEnabled = true // Đặt trạng thái của nút thành true
                }
            } else { // Nếu nút đã được nhấn trước đó
                intent.action = "ACTION_OFF_ALL_REPEAT_MODE"
                startService(intent)
                repeatAll.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                isRepeatOneEnabled = false // Đặt trạng thái của nút thành false
            }

            // Thay đổi màu của các drawable trong nút
            repeatAll.compoundDrawables.forEach { drawable ->
                if (drawable != null) {
                    if (isRepeatOneEnabled) {
                        drawable.setColorFilter(clickedColor, PorterDuff.Mode.SRC_ATOP)
                    } else {
                        // Đặt lại màu của drawable thành màu gốc
                        drawable.colorFilter = null
                    }
                }
            }
        }
        val btnMix = findViewById<TextView>(R.id.mixSong)
        btnMix?.setOnClickListener {
            val intent = Intent(this@MainActivity, MusicService::class.java)
            val clickedColor = ContextCompat.getColor(this@MainActivity, R.color.color_click)
            if (!isSuffer) { // Nếu nút chưa được nhấn trước đó
                if (musicService != null && musicService!!.mediaPlayer.isPlaying) {
                    intent.action = "ACTION_SUFFER"
                    startService(intent)
                    btnMix.setTextColor(clickedColor)
                    isSuffer = true // Đặt trạng thái của nút thành true
                }
            } else { // Nếu nút đã được nhấn trước đó
                intent.action = "ACTION_OFF_SUFFER"
                startService(intent)
                btnMix.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                isSuffer = false // Đặt trạng thái của nút thành false
            }
            // Thay đổi màu của các drawable trong nút
            btnMix.compoundDrawables.forEach { drawable ->
                if (drawable != null) {
                    if (isSuffer) {
                        drawable.setColorFilter(clickedColor, PorterDuff.Mode.SRC_ATOP)
                    } else {
                        // Đặt lại màu của drawable thành màu gốc
                        drawable.colorFilter = null
                    }
                }
            }
        }
//        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                skipNext?.text = progress.toString()
//                musicService!!.mediaPlayer.seekTo(progress)
//
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                startPoint = seekBar!!.progress
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                endPoint = seekBar!!.progress
////                val newPosition = seekBar?.progress ?: 0
//
//                // Gửi vị trí mới tới MusicService để seek đến vị trí mới
//                val intent = Intent(this@MainActivity, MusicService::class.java)
//                intent.action = "ACTION_SEEK"
//                intent.putExtra("SEEK_POS", endPoint)
//                startService(intent)
//            }
//
//        })

        playerControls()

        if(checkNeedsPermission()) {
            startMusicService()
        } else {
            requestNeedsPermission()
        }
    }

    private fun observeCurrentSongTitle() {
        musicService?.currentSongTitle?.observe(this, {title ->
            txtTitle?.text = title
            txtTitlePlay?.text = title
        })

        musicService?.currentSongArtist?.observe(this, { artist ->
            // Cập nhật tác giả của bài hát trên giao diện người dùng
            txtAuthor?.text = artist
            txtAuthorPlay?.text = artist
        })

        musicService?.currentSongImage?.observe(this, { image ->
            // Cập nhật ảnh album của bài hát trên giao diện người dùng
            val url: Uri = image
            Glide.with(this)
                .load(url)
                .circleCrop()
                .into(imgSong!!)
            Glide.with(this)
                .load(url)
                .circleCrop()
                .into(imgSongPlay!!)

        })
        musicService?.currentSongDuration?.observe(this, { duration ->
            skipEnd?.text = utilities.milliSecondsToTimer(duration)
            updateSeekBar()
        })
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
//        playerView?.setOnClickListener {playControl?.visibility = View.GONE }
        playControl?.setOnClickListener {
            playerView?.visibility = View.VISIBLE
            appbar.visibility = View.GONE
        }
        btnDown.setOnClickListener{
            playerView?.visibility = View.GONE
            appbar.visibility = View.VISIBLE
        }

    }

    fun updateSeekBar() {
        if (musicService != null && musicService?.currentSongDuration != null) {
            val totalDuration = musicService!!.currentSongDuration.value ?: 0
            val currentPosition = musicService!!.mediaPlayer.currentPosition.toLong()
            val progress = utilities.getProgressPercentage(currentPosition, totalDuration)
            seekBar?.progress = progress
            val currentDuration = utilities.milliSecondsToTimer(currentPosition)

            // Hiển thị thời gian chạy trên TextView
            txtCurrentTime?.text = currentDuration
        }
    }

}