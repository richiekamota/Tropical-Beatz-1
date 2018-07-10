package com.richmond.tropicalbeatz

class Channel{
    var channelGenre:String? = null
    var channelName:String? = null
    var channelOrigin:String? = null
    var channelURL:String? = null
    constructor(channelGenre:String,channelName:String,channelOrigin:String, channelURL:String){
        this.channelGenre = channelGenre
        this.channelName= channelName
        this.channelOrigin = channelOrigin
        this.channelURL = channelURL
    }
}
