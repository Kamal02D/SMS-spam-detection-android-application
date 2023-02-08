package com.dev.smsspamdetector

interface OnResponseRecieved {
    fun onReceiveResponse(result: String,ip:String)
    fun onError()
}