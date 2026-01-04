package com.kotlinjs5.jsvideoplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlinjs5.jsvideoplayer.databinding.ActivityMainBinding

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var player: ExoPlayer? = null
    private val recentFiles = mutableListOf<RecentFile>()
    private lateinit var adapter: RecentFilesAdapter

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                playVideo(uri)
                addToRecentFiles(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializePlayer()
        loadRecentFiles()

        adapter = RecentFilesAdapter(recentFiles) { file ->
            playVideo(Uri.parse(file.uri))
        }
        binding.rvRecentFiles.layoutManager = LinearLayoutManager(this)
        binding.rvRecentFiles.adapter = adapter

        binding.btnPickFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }
            pickFileLauncher.launch(intent)
        }

        binding.btnPlayUrl.setOnClickListener {
            val url = binding.etStreamUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                val uri = Uri.parse(url)
                playVideo(uri)
                addToRecentFiles(uri)
            }
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.playerView.useController = true
    }

    private fun playVideo(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    private fun addToRecentFiles(uri: Uri) {
        val name = uri.lastPathSegment ?: uri.toString().takeLast(30)
        val file = RecentFile(uri.toString(), name)
        if (recentFiles.none { it.uri == file.uri }) {
            recentFiles.add(0, file)
            if (recentFiles.size > 15) recentFiles.removeLast()
            adapter.notifyDataSetChanged()
            saveRecentFiles()
        }
    }

    private fun loadRecentFiles() {
        val prefs = getSharedPreferences("recent_files", Context.MODE_PRIVATE)
        val saved = prefs.getString("files", "") ?: ""
        if (saved.isNotEmpty()) {
            val parts = saved.split(",")
            for (i in parts.indices step 2) {
                if (i + 1 < parts.size) {
                    recentFiles.add(RecentFile(parts[i], parts[i + 1]))
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveRecentFiles() {
        val prefs = getSharedPreferences("recent_files", Context.MODE_PRIVATE)
        val flat = recentFiles.flatMap { listOf(it.uri, it.name) }.joinToString(",")
        prefs.edit().putString("files", flat).apply()
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
