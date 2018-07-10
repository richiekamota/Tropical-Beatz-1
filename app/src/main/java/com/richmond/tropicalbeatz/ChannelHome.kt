package com.richmond.tropicalbeatz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.SurfaceView
import android.view.View
import android.view.animation.LinearInterpolator
import me.bogerchan.niervisualizer.NierVisualizerManager
import me.bogerchan.niervisualizer.renderer.IRenderer
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer
import me.bogerchan.niervisualizer.renderer.circle.CircleRenderer
import me.bogerchan.niervisualizer.renderer.circle.CircleSolidRenderer
import me.bogerchan.niervisualizer.renderer.circle.CircleWaveRenderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType1Renderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType2Renderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType3Renderer
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType4Renderer
import me.bogerchan.niervisualizer.renderer.line.LineRenderer
import me.bogerchan.niervisualizer.renderer.other.ArcStaticRenderer
import me.bogerchan.niervisualizer.util.NierAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_channel_home.*
import kotlinx.android.synthetic.main.progress_overlay.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class ChannelHome : AppCompatActivity() {

    var play: Boolean = false
    var mp: MediaPlayer? = null
    var initialStage = true
    var progressOverlay:FrameLayout? = null
    var buPlayBtn:ImageButton? = null
    var buPauseBtn:ImageButton? = null
    var buRevertBtn:ImageButton? = null
    var channelURL:String? = null
    var channelName:String? = null
    var adFlight:LinearLayout? = null
    lateinit var mAdView : AdView


    companion object {
        val REQUEST_CODE_PERMISSION_AUDIO_FOR_INIT = 1
        val REQUEST_CODE_PERMISSION_AUDIO_FOR_CHANGE_STYLE = 2
    }

    private val svWave by lazy { sv_wave as SurfaceView }
    private var mVisualizerManager: NierVisualizerManager? = null
    private val mRenderers = arrayOf<Array<IRenderer>>(
            arrayOf(ColumnarType1Renderer()),
            arrayOf(ColumnarType2Renderer()),
            arrayOf(ColumnarType3Renderer()),
            arrayOf(ColumnarType4Renderer()),
            arrayOf(LineRenderer(true)),
            arrayOf(CircleBarRenderer()),
            arrayOf(CircleRenderer(true)),
            arrayOf(CircleRenderer(true),
                    CircleBarRenderer(),
                    ColumnarType4Renderer()),
            arrayOf(CircleRenderer(true), CircleBarRenderer(), LineRenderer(true)),
            arrayOf(ArcStaticRenderer(
                    paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#74F40A")
                    }),
                    ArcStaticRenderer(
                            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = Color.parseColor("#FDD017")
                            },
                            amplificationOuter = .83f,
                            startAngle = -90f,
                            sweepAngle = 225f),
                    ArcStaticRenderer(
                            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = Color.parseColor("#7fa9d0fd")
                            },
                            amplificationOuter = .93f,
                            amplificationInner = 0.8f,
                            startAngle = -45f,
                            sweepAngle = 135f),
                    CircleSolidRenderer(
                            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = Color.parseColor("#d2eafe")
                            },
                            amplification = .45f),
                    CircleBarRenderer(
                            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                strokeWidth = 4f
                                color = Color.parseColor("#efe3f2ff")
                            },
                            modulationStrength = 1f,
                            type = CircleBarRenderer.Type.TYPE_A_AND_TYPE_B,
                            amplification = 1f, divisions = 8),
                    CircleBarRenderer(
                            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                strokeWidth = 5f
                                color = Color.parseColor("#74F40A")
                            },
                            modulationStrength = 0.1f,
                            amplification = 1.2f,
                            divisions = 8),
                    CircleWaveRenderer(
                            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                strokeWidth = 6f
                                color = Color.WHITE
                            },
                            modulationStrength = 0.2f,
                            type = CircleWaveRenderer.Type.TYPE_B,
                            amplification = 1f,
                            animator = NierAnimator(
                                    interpolator = LinearInterpolator(),
                                    duration = 20000,
                                    values = floatArrayOf(0f, -360f))),
                    CircleWaveRenderer(
                            paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                strokeWidth = 6f
                                color = Color.parseColor("#7fcee7fe")
                            },
                            modulationStrength = 0.2f,
                            type = CircleWaveRenderer.Type.TYPE_B,
                            amplification = 1f,
                            divisions = 8,
                            animator = NierAnimator(
                                    interpolator = LinearInterpolator(),
                                    duration = 20000,
                                    values = floatArrayOf(0f, -360f))))
    )
    private var mCurrentStyleIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_home)
        var bundle: Bundle = intent.extras
        channelURL = bundle.getString("channelURL")
        channelName = bundle.getString("channelName")
        tvRadioName.text = channelName.toString()
        buPlayBtn = imvPlay as ImageButton
        buPauseBtn = imvPause as ImageButton
        buRevertBtn = imvRevert as ImageButton
        progressOverlay = progress_overlay
        adFlight = adflight
        Toast.makeText(this, channelURL, Toast.LENGTH_LONG).show()

        UpdateChannelInfo().start()
        TextSlide().start()

        var builder = AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton(android.R.string.ok) { dialogueInterface: DialogInterface, i: Int ->
                    finish()
                }
                .setIcon(R.drawable.alert)
                .setCancelable(false)

        svWave.setZOrderOnTop(true)
        svWave.holder.setFormat(PixelFormat.TRANSLUCENT)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_CODE_PERMISSION_AUDIO_FOR_INIT)
        } else {
            mVisualizerManager = NierVisualizerManager()
            mVisualizerManager?.init(0)
        }
        mp = MediaPlayer()

        val channelPlayTask = ChannelPlayTask(applicationContext, object : OnEventListener<Boolean> {

            override fun onSuccess(result: Boolean) {
                Toast.makeText(applicationContext, "Connection successful!", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(e: Exception) {
                builder!!.show()
            }
        })

        //Main execution point

        try {
            if (!play) {
                if (initialStage) {
                    channelPlayTask.execute("$channelURL")
                } else {
                    if (!mp!!.isPlaying)
                        mp!!.start()
                }
                play = true
                useStyle(mCurrentStyleIndex)
            }
        } catch (ex: Throwable) {
            progressOverlay!!.setVisibility(View.GONE)
            builder!!.show()
        }

        MyVisualiser().start()


        //Ads control

        MobileAds.initialize(this, "ca-app-pub-6035374685884193/9741299511")

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                adFlight!!.setVisibility(View.VISIBLE)
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        }

        //App controls

        buPlayBtn!!.setOnClickListener {
            if (!play) {
                //mp!!.start()
                mp!!.setVolume(1f, 1f)
            }
            play = true
            useStyle(mCurrentStyleIndex)
        }

        buPauseBtn!!.setOnClickListener {
            //have to mute as pause mp!!.pause() method caches and causes API and music streaming to be asynchronous
            if (mp!!.isPlaying()) {
                //mp!!.pause()
                mp!!.setVolume(0f, 0f)
            }
            play = false
            mVisualizerManager?.stop()
        }

        buRevertBtn!!.setOnClickListener {
            finish()
            System.exit(0)
            if (mp != null) {
                mp!!.reset()
                mp!!.release()
                finish()
                mp = null
                mVisualizerManager?.stop()
                mVisualizerManager?.release()
            }
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun changeStyle() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_CODE_PERMISSION_AUDIO_FOR_CHANGE_STYLE)
        } else {
            useStyle(++mCurrentStyleIndex)
        }
    }

    private fun useStyle(idx: Int) {
        if (mVisualizerManager == null) {
            val nvm = NierVisualizerManager()
            nvm.init(0)
            mVisualizerManager = nvm
        }
        mVisualizerManager?.start(svWave, mRenderers[idx % mRenderers.size])
    }

    override fun onBackPressed() {
                 AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton(android.R.string.ok) { dialogueInterface:DialogInterface, i:Int ->
                    finish()}
                .setNegativeButton(android.R.string.no) { dialogueInterface:DialogInterface, i:Int ->
                    }
                .setCancelable(false)
                .setIcon(R.drawable.alert).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
        if (mp != null) {
            mp!!.reset()
            mp!!.release()
            mp = null
        }
        mVisualizerManager?.stop()
        mVisualizerManager?.release()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_CODE_PERMISSION_AUDIO_FOR_INIT -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mVisualizerManager = NierVisualizerManager()
                    mVisualizerManager?.init(0)
                    useStyle(++mCurrentStyleIndex)
                } else {
                    Toast.makeText(this,"AUDIO RECORD not permitted!",Toast.LENGTH_LONG).show()
                }
            }
            REQUEST_CODE_PERMISSION_AUDIO_FOR_CHANGE_STYLE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useStyle(++mCurrentStyleIndex)
                } else {
                    Toast.makeText(this,"AUDIO RECORD not permitted!",Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    inner class ChannelPlayTask : AsyncTask<String, String, Boolean> {
        private var mCallBack: OnEventListener<Boolean>
        private var mContext: Context
        var mException: Exception? = null


        constructor(context: Context, callback: OnEventListener<Boolean>){
            this.mCallBack = callback
            this.mContext = context
        }
        override fun doInBackground(vararg strings: String): Boolean? {
        var prepared: Boolean?

                try {
                    mp!!.setDataSource(strings[0])
                    mp!!.setOnCompletionListener { mp ->
                        initialStage = true
                        play = false
                        mp!!.stop()
                        mp!!.release()
                        mp!!.reset()

                    }
                    mp!!.prepare()
                    prepared = true

                } catch (ex: Exception) {
                    mException = ex
                    Log.e("Tropical Beatz", ex.message)
                    prepared = false
                }
                return prepared
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // display the indefinite progressbar and make the channel display disappear
            progressOverlay!!.setVisibility(View.VISIBLE)
            channelHeader!!.setVisibility(ImageView.GONE)
        }

        override fun onPostExecute(aBoolean: Boolean?) {
            super.onPostExecute(aBoolean)
            // when the task is completed, make progressBar gone and the channel display appear

                progressOverlay!!.setVisibility(View.GONE)
                channelHeader!!.setVisibility(ImageView.VISIBLE)
                mp!!.start()
                if (mException == null) {
                    mCallBack.onSuccess(aBoolean!!)
                } else {
                    mCallBack.onFailure(mException!!)
                }

            initialStage = false
        }
    }

    // run another task to extract channel info from the API
    inner class ChannelInfoTask:AsyncTask<String,String,String>(){
        override fun doInBackground(vararg params: String?): String {
            try {
                var url = URL(params[0])
                var urlConnect = url.openConnection() as HttpURLConnection
                urlConnect.connectTimeout = 8000
                var outputString = convertStreamToString(urlConnect.inputStream)
                publishProgress(outputString)
            }catch (ex:Exception){}
            return ""
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            try {
                var json = JSONObject(values[0])
                var songTitle = json.getString("title")
                var album = json.getString("album")
                var artistInfo = json.getJSONObject("artist")
                var artist = artistInfo.getString("name")
                var releaseYear = json.getString("releaseyear")
                tvRadioName.text = channelName.toString()
                if(releaseYear == "" && album == "" || releaseYear == null && album == null){
                    tvAlbumInfo.text = "Album: No album info"
                }else{
                    tvAlbumInfo.text = "Album:" + " "+ album!!.toString() + " - " + releaseYear!!.toString()
                }

                tvArtistInfo.text = "${artist} - ${songTitle}"

                var image = artistInfo.getString("thumb")

                Picasso.get()
                        .load(image!!)
                        .placeholder(R.drawable.flagbig144)
                        .error(R.drawable.flagbig144)
                        .into(imvArtistImage)
            }catch (ex:Exception){}
        }
    }

    fun convertStreamToString(inputStream: InputStream):String {
        var bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var totalString = ""
        try {
            do {
                line = bufferedReader.readLine()
                if (line != null) {
                    totalString += line
                }
            } while (line != null)
        } catch (ex: Exception) {}
        return totalString
    }

    inner class UpdateChannelInfo:Thread{
        constructor():super(){}

        override fun run() {
            while(true){
                try {
                    Thread.sleep(1000)
                }catch (ex:Exception){}
                runOnUiThread {
                    ChannelInfoTask().execute("http://api.laut.fm/station/" + extractStation(channelURL!!) + "/current_song")
                }
            }
        }
    }

    fun extractStation(channelURL:String):String{
        var stationName = channelURL.split("/")
        return stationName[3]
    }

    inner class MyVisualiser:Thread{

        constructor():super(){}

        override fun run() {
            while (true){
                Thread.sleep(4000)
                try {
                      changeStyle()
                }catch (ex:Exception){}
            }
        }
    }

    inner class TextSlide:Thread{
        constructor():super(){}

        override fun run() {
            while (true) {

                try {
                    Thread.sleep(6000)
                } catch (ex: Exception) {}

                runOnUiThread {
                    tvArtistInfo.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.text_anim))
                    tvArtistInfo.setMovementMethod(ScrollingMovementMethod())
                }
            }
        }
    }
}




