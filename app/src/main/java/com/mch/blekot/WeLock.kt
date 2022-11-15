package com.mch.blekot

import android.util.Log
import com.squareup.okhttp.*
import java.io.IOException
import kotlin.math.log

class WeLock(
    private var rndNumber: String,
    private var devicePower: String,
    private var mAction: String,
    private val ble: Ble
) {

    private val urlWeLock = "https://api.we-lock.com"
    private val PATH_CARD = "/API/Device/DeviceCardCommand"
    private val PATH_CODE = "/API/Device/DeviceSetTemporaryPassword"
    private val PATH_OPEN = "/API/Device/DeviceUnLockCommand"
    private val PATH_TOKEN = "/API/Auth/Token"

    private var client: OkHttpClient = OkHttpClient()
    private var mToken: String? = null


    fun getHex() {
        val yeison = "{appID: \"WELOCK2202161033\", deviceNumber: \"21471618\", " +
                "deviceBleName: \"WeLockAWPOR\", devicePower: \"$devicePower\", " +
                "deviceRandomFactor: \"$rndNumber\"}"
        when (mAction) {
            "openLock" -> postWithToken(
                PATH_OPEN,
                yeison,
                actionCallback
            )
        }
        Log.i("json", yeison)
    }

    fun getToken() {
        post(
            "/API/Auth/Token", newJson(
                "WELOCK2202161033",
                "349910dfcdfac75df0fd1cf2cbb02adb"
            ), tokenCallback
        )
    }

    private var tokenCallback: Callback = object : Callback {
        override fun onFailure(request: Request?, e: IOException?) {
            Log.i("TokenCallback", "FAIL")
        }

        @Throws(IOException::class)
        override fun onResponse(response: Response) {
            mToken = response.body().string().split("\"")[9]
            Log.i("WELOCK", "onResponse: $mToken")
            getHex()
        }
    }


    private var actionCallback: Callback = object : Callback {
        override fun onFailure(request: Request?, e: IOException?) {

        }

        @Throws(IOException::class)
        override fun onResponse(response: Response) {
            val res = response.body().string().split("\"")[3]
            Log.i("WeLock", "onResponse: $res")
            ble.startBle(res)
        }
    }


    fun newJson(appID: String, secret: String): String {
        return """{appID: "$appID", secret: "$secret"}"""
    }

    @Throws(IOException::class)
    fun post(path: String, json: String?, callback: Callback?) {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), json
        )
        val request: Request = Request.Builder()
            .url(urlWeLock + path)
            .addHeader("Authorization", " Bearer $mToken")
            .post(body)
            .build()
        val call: Call = client.newCall(request)
        call.enqueue(callback)
    }

    @Throws(IOException::class)
    fun postWithToken(path: String, json: String?, callback: Callback?) {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), json?.toByteArray()
        )
        val request: Request = Request.Builder()
            .url(urlWeLock + path)
            .addHeader("Authorization", " Bearer $mToken")
            .post(body)
            .build()
        val call: Call = client.newCall(request)
        call.enqueue(callback)
    }

}