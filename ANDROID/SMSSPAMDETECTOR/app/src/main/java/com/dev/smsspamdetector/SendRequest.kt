package com.dev.smsspamdetector

import android.os.AsyncTask
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class SendRequest(val listener : OnResponseRecieved) : AsyncTask<String, Void, ArrayList<String>>() {

        override fun doInBackground(vararg params: String): ArrayList<String> {
            val ipAddress = params[0]
            val port = params[1]
            val param = params[2]
            val url = "http://$ipAddress:$port/process?param=$param"
            Log.d("errorr55" , "request to $url")

            val client = OkHttpClient.Builder()
                .readTimeout(80, TimeUnit.SECONDS)
                .writeTimeout(80, TimeUnit.SECONDS)
                .connectTimeout(80, TimeUnit.SECONDS)
                .build()
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                return arrayListOf(response.body!!.string(),ipAddress)
            }catch (ex : Exception){
                    Log.d("errorr55" , ex.message.toString())
                    return arrayListOf("-1")
                }
        }

        override fun onPostExecute(result: ArrayList<String>) {
            // Update the UI with the result
            if (result[0] == "1") {
                listener.onReceiveResponse(result[0],result[1])
            }else if(result[0] == "-1"){
                listener.onError()
            }else{
                listener.onReceiveResponse(result[0],result[1])
            }
        }

}
