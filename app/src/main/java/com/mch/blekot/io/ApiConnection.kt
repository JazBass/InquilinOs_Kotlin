package com.mch.blekot.io

import kotlin.Throws
import android.content.Intent
import android.util.Log
import okhttp3.*
import java.io.IOException

object Ewelink {
    fun turnOffLight() {
        val http = "https://tcpmch.herokuapp.com/?client=Oficina&cmd=toggleLight"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(http)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    assert(response.body() != null)
                    var res = response.body()!!.string()
                    if (res.compareTo("{\"code\":0}") == 0) {
                        res = "Interruptor luz"
                    }
                    Log.d("OKHTTP", "onResponse: $res")
                    val intent = Intent()
                    intent.putExtra("result", res)
                }
            }
        })
    }
}