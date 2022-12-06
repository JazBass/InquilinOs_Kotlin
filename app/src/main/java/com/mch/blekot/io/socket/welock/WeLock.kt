package com.mch.blekot.io.socket.welock

import android.util.Log
import com.mch.blekot.ble.Ble
import com.mch.blekot.util.Constants
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*


class WeLock() : WeLockAux {

    private val urlWeLock = "https://api.we-lock.com"
    private val PATH_CARD = "/API/Device/DeviceCardCommand"
    private val PATH_CODE = "/API/Device/DeviceSetTemporaryPassword"
    private val PATH_OPEN = "/API/Device/DeviceUnLockCommand"
    private val PATH_TOKEN = "/API/Auth/Token"

    private var client: OkHttpClient = OkHttpClient()
    private var mToken: String? = null
    private var mNewPassword: String? = null
    private var mType: String? = null
    private var mQr: String? = null

    //Chueca 9
    private var deviceName = Constants.DEVICE_NAME
    private var deviceIdNumber = Constants.DEVICE_ID_NUMBER

    //Oficina
    //private var deviceName = "WeLockGE4CK"
    //private var deviceIdNumber = "21471477"

    private lateinit var mRndNumber: String
    private lateinit var mDevicePower: String

    private lateinit var mAction: String

    private var ble = Ble(this)

    override fun openLock() {
        mAction = Constants.ACTION_OPEN_LOCK

        ble.sendBle()
    }

    override fun setNewCode(newPassword: String) {
        mAction = Constants.ACTION_NEW_CODE
        mNewPassword = newPassword

        ble.sendBle()
    }

    override fun setNewCard(qr: String, type: String) {
        mAction = Constants.ACTION_SET_CARD
        mQr = qr
        mType = type

        ble.sendBle()
    }

    //Pedimos el codigo a la api
    fun getHex() {
        val startDate: Int = ((System.currentTimeMillis() / 1000) - 28800).toInt()

        val endDate: Int = startDate + 86400

        when (mAction) {
            "openLock" -> {

                val openLockJson = """{
                    appID: "WELOCK2202161033", 
                    deviceNumber: "$deviceIdNumber", 
                    deviceBleName: "$deviceName", 
                    devicePower: "$mDevicePower", 
                    deviceRandomFactor: "$mRndNumber"}
                """.trimIndent()

                postWithToken(
                    PATH_OPEN,
                    openLockJson,
                    actionCallback
                )
            }

            "newCode" -> {

                val newCodeJson = """{
                    appID: "WELOCK2202161033", 
                    deviceNumber: "$deviceIdNumber", 
                    deviceBleName: "$deviceName", 
                    deviceRandomFactor: "$mRndNumber", 
                    password: $mNewPassword, 
                    index: ${Constants.CODE_INDEX}, 
                    user: ${Constants.CODE_USER}, 
                    times: ${Constants.CODE_TIMES}, 
                    startTimestamp: $startDate, 
                    endTimestamp: $endDate}
                """.trimIndent()

                Log.i("Json", newCodeJson)

                postWithToken(
                    PATH_CODE,
                    newCodeJson,
                    actionCallback
                )
            }

            "setCard" -> {

                val newCardJson = """{
                    appID: "WELOCK2202161033",
                    deviceNumber: "$deviceIdNumber",
                    deviceBleName: "$deviceName",
                    deviceRandomFactor: "$mRndNumber",
                    cardQr: "$mQr",
                    type: "$mType"}""".trimIndent() // FIXME: CHANGE: OK OK. set type by post

                postWithToken(
                    PATH_CARD,
                    newCardJson,
                    actionCallback
                )
            }

        }
    }


    fun getToken(battery: String, rdmNumber: String) {

        mRndNumber = rdmNumber
        mDevicePower = battery

        post(
            PATH_TOKEN, newJson(
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
            val dataJson = JSONObject(response.body()?.string()!!).getJSONObject("data")
            mToken = dataJson.getString("accessToken")

            //mToken = response.body()?.string()?.split("\"")?.get(9)

            Log.i("Token", "onResponse: $mToken")
            getHex()
        }
    }


    private var actionCallback: Callback = object : Callback {

        override fun onFailure(p0: Call, p1: IOException) {
            Log.i("Action Callback", "FAIL")
        }

        override fun onResponse(p0: Call, response: Response) {

            val dataJson = JSONObject(response.body()?.string()!!)
            val res = dataJson.getString("data")

            //val res = response.body()?.string()?.split("\"")?.get(3)
            Log.i("Action", "onResponse: $res")
            //ble.sendBle(code = res)
            ble.writeDataWeLockResponse(code = res)
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