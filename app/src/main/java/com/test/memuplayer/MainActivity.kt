package com.test.memuplayer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    val fd by lazy {assets.openFd(cancionActual)}
    val mp by lazy {
        val m = MediaPlayer()
        m.setDataSource(
            fd.fileDescriptor,
            fd.startOffset,
            fd.length
        )
        fd.close()
        m.prepare()
        m
    }


    val controllers by lazy {
        listOf(R.id.btn_ant, R.id.btn_stop, R.id.btn_play, R.id.btn_sig).map {
            findViewById<MaterialButton>(it)
        }
    }

    object ci{
        val prev = 0
        val stop = 1
        val play = 2
        val next = 3
    }

    val nombreCancion by lazy {
        findViewById<TextView>(R.id.songName)
    }

    val canciones by lazy {
        val nombreficheros = assets.list("")?.toList() ?: listOf()
        nombreficheros.filter {it.contains(".mp3")}
    }

    var cancionActualIndex = 0
        set(value){
            var v = if(value==-1){
                canciones.size-1
            }else{
                value%canciones.size
            }
            field = v
            cancionActual = canciones[v]
        }

    lateinit var cancionActual:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        controllers[ci.play].setOnClickListener (this::playClicked)
        controllers[ci.stop].setOnClickListener (this::stopClicked)
        controllers[ci.prev].setOnClickListener(this::prevClicked)
        controllers[ci.next].setOnClickListener(this::nextClicked)
        cancionActual = canciones[cancionActualIndex]
        nombreCancion.text = cancionActual
    }


override fun onStart() {
    super.onStart()
    Log.d(tag, "onStart called")
    try {
        val recyclerView = findViewById<RecyclerView>(R.id.rv)
        recyclerView.apply {
            adapter = AdaptadorCanciones(canciones, this@MainActivity)
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    } catch (e: Exception) {
        Log.e(tag, "Error initializing RecyclerView: ${e.message}")
    }
}

    fun playClicked(v: View){
        if (!mp.isPlaying) {
            mp.start()
            controllers[ci.play].setBackgroundDrawable(resources.getDrawable(R.drawable.pausa))
            nombreCancion.visibility = View.VISIBLE
        }else{
            mp.pause()
            controllers[ci.play].setBackgroundDrawable(resources.getDrawable(R.drawable.reproducir))
        }
    }

    fun stopClicked(v: View){
        if(mp.isPlaying){
            mp.pause()
            nombreCancion.visibility = View.INVISIBLE
        }
        mp.seekTo(0)
    }
    fun nextClicked(v: View){
        cancionActualIndex++
        refreshSong()
    }

    fun prevClicked(v: View){
        cancionActualIndex--
        refreshSong()
    }

    fun refreshSong(){
        mp.reset()
        var fd = assets.openFd(cancionActual)
        mp.setDataSource(
            fd.fileDescriptor,
            fd.startOffset,
            fd.length
        )
        mp.prepare()
        playClicked(controllers[ci.play])
        nombreCancion.text = cancionActual
    }
}