package com.mch.blekot

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.*

class WeLock(
    private var mRndNumber: String,
    private var mDevicePower: String,
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
    private var mCode: String? = null
    private var mType: String? = null
    private var mQr: String? = null


    fun getHex() {
        val startDate: Int = ((System.currentTimeMillis() / 1000) - 28800).toInt()

        val endDate: Int = startDate + 86400


        val openLockJson = """{
            appID: "WELOCK2202161033", 
            deviceNumber: "21471618", 
            deviceBleName: "WeLockAWPOR", 
            devicePower: "$mDevicePower", 
            deviceRandomFactor: "$mRndNumber"}
            """.trimIndent()

        val newCodeJson = """{
            appID: "WELOCK2202161033", 
            deviceNumber: "21471618", 
            deviceBleName: "WeLockAWPOR", 
            devicePower: "$mDevicePower", 
            deviceRandomFactor: "$mRndNumber", 
            password: $mCode, 
            index: 20, 
            user: 15, 
            times: 65000, 
            startTimestamp: $startDate, 
            endTimestamp: $endDate}
        """.trimIndent()





        when (mAction) {
            "openLock" -> postWithToken(
                PATH_OPEN,
                openLockJson,
                actionCallback
            )
            "newCode" -> {
                Log.i("Json", newCodeJson)

                postWithToken(
                    PATH_CODE,
                    newCodeJson,
                    actionCallback
                )
            }

        }
    }

    fun getToken(qr: String? = null, code: String? = null, type: String? = null) {

        when (mAction) {
            "newCode" -> {
                mCode = code!!
            }

            "setCard" -> {
                mQr = qr!!
                mType = type!!
            }
        }

        post(
            "/API/Auth/Token", newJson(
                "WELOCK2202161033",
                "349910dfcdfac75df0fd1cf2cbb02adb"
            ), tokenCallback
        )
    }

    private var tokenCallback: Callback = object : Callback {

        override fun onFailure(p0: Call, p1: IOException) {
            TODO("Not yet implemented")
        }

        override fun onResponse(p0: Call, response: Response) {
            mToken = response.body()?.string()?.split("\"")?.get(9)
            Log.i("Token", "onResponse: $mToken")
            getHex()
        }
    }


    private var actionCallback: Callback = object : Callback {

        override fun onFailure(p0: Call, p1: IOException) {
            Log.i("Action Callback", "FAIL")
        }

        override fun onResponse(p0: Call, response: Response) {
            val res = response.body()?.string()?.split("\"")?.get(3)
            Log.i("Action", "onResponse: $res")
            if (res != null) {
                ble.startBle(code = res)
            }
        }
    }


    private fun newJson(appID: String, secret: String): String {
        return """{appID: "$appID", secret: "$secret"}"""
    }

    @Throws(IOException::class)
    fun post(path: String, json: String?, callback: Callback?) {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"), json
        )
        val request: Request = Request.Builder()
            .url(urlWeLock + path)
            .post(body)
            .build()
        val call: Call = client.newCall(request)
        if (callback != null) {
            call.enqueue(callback)
        }
    }

    @Throws(IOException::class)
    fun postWithToken(path: String, json: String?, callback: Callback?) {
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), json?.toByteArray()!!
        )
        val request: Request = Request.Builder()
            .url(urlWeLock + path)
            .addHeader("Authorization", " Bearer $mToken")
            .post(body)
            .build()
        val call: Call = client.newCall(request)
        if (callback != null) {
            call.enqueue(callback)
        }
    }

}