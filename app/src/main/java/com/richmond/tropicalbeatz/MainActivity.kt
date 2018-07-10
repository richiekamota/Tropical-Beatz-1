package com.richmond.tropicalbeatz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ticket_main.view.*
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    var listChannel = ArrayList<Channel>()
    var channelAdapter:BaseAdapter? = null
    var mainPlayButton: ImageButton? = null
    lateinit var mAdView : AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain the FirebaseAnalytics instance.

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        listChannel.add(Channel("Reggae, Roots and Dancehall", "lautfm/Jahfari", "Germany", "http://stream.laut.fm/jahfari"))
        listChannel.add(Channel("Roots, Reggae Music", "lautfm/Radio Against Babylon", "Germany", "http://stream.laut.fm/radioagainstbabylon"))
        listChannel.add(Channel("Reggae, Roots and Dancehall", "lautfm/Reggae-Paradise", "Germany", "http://laut.fm/reggae-paradise"))
        listChannel.add(Channel("Reggae and Dancehall", "lautfm/Corner-store-sound", "Germany","http://laut.fm/corner-stone-sound"))
        listChannel.add(Channel("Reggae, Roots and Dancehall", "lautfm/Bubble Radio", "Germany", "http://laut.fm/bubble-radio"))
        listChannel.add(Channel("Roots, Reggae Music", "lautfm/Sun Radio", "Germany", "http://laut.fm/sunradio"))
        listChannel.add(Channel("Reggae, Roots and Dancehall", "lautfm/Zany", "Germany", "http://laut.fm/zany"))
        listChannel.add(Channel("Reggae and Dancehall", "lautfm/DJ Papa Lion", "Germany","http://laut.fm/dj-papa-lion"))
        listChannel.add(Channel("Reggae and Dancehall", "181.FM/Reggae", "Western Virginia, USA","http://listen.livestreamingservice.com/181-reggae_128k.mp3"))

        channelAdapter = ChannelListAdapter(this,listChannel)
        tvMain.adapter = channelAdapter

        //Ads control

        MobileAds.initialize(this, "ca-app-pub-6035374685884193/5977061709")

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                listChannel.add(0,Channel("","","","ads"))
                channelAdapter!!.notifyDataSetChanged()
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
                listChannel.removeAt(0)
                channelAdapter!!.notifyDataSetChanged()
            }
        }
    }

    inner class ChannelListAdapter:BaseAdapter {
        var channelList = ArrayList<Channel>()
        var context: Context? = null

        constructor(context: Context, channelList: ArrayList<Channel>) : super() {
            this.context = context
            this.channelList = channelList
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

                var channel = channelList[position]

            if(channel.channelURL.equals("ads")){

                var myView = layoutInflater.inflate(R.layout.ticket_channel_presenter, null)
                return myView

            }else{

                var myView = layoutInflater.inflate(R.layout.ticket_channel_presenter, null)

                mainPlayButton = myView.mainPlay

                myView.tvGenre.text = channel.channelGenre
                myView.tvChannel.text = channel.channelName
                myView.tvOrigin.text = channel.channelOrigin
                mainPlayButton!!.setOnClickListener({
                    var intent = Intent(context!!,ChannelHome::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.putExtra("channelName", channel.channelName!!)
                    intent.putExtra("channelURL", channel.channelURL!!)
                    context!!.startActivity(intent)
                })

                return myView
            }
        }

        override fun getItem(position: Int): Any {
            return channelList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return channelList.size
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        finish()
        System.exit(0)
        super.onDestroy()
    }


}
