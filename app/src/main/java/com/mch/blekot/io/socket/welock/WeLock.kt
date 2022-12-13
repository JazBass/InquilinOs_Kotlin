package com.mch.blekot.io.socket.welock

import android.util.Log
import com.mch.blekot.ble.Ble
import com.mch.blekot.services.SocketSingleton
import com.mch.blekot.util.Constants
import com.mch.blekot.util.UtilDevice
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

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
    private var mDays: Int = 1

    //Chueca 9
    private val deviceName = Constants.DEVICE_NAME
    private val deviceIdNumber = Constants.DEVICE_ID_NUMBER

    private lateinit var mRndNumber: String
    private lateinit var mDevicePower: String

    private lateinit var mAction: String

    private var ble = Ble(this)

    override fun openLock() {
        mAction = Constants.ACTION_OPEN_LOCK

        ble.sendBle()
    }

    override fun setNewCode(newPassword: String, days: Int) {
        mAction = Constants.ACTION_NEW_CODE
        mNewPassword = newPassword
        mDays = days

        ble.sendBle()
    }

    override fun setNewCard(qr: String, type: String) {
        mAction = Constants.ACTION_SET_CARD
        mQr = qr
        mType = type

        ble.sendBle()
    }

    fun getHex() {

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

                val startDate: Int = ((System.currentTimeMillis() / 1000) - 28800).toInt()
                val endDate: Int = startDate + (86400 * mDays)

                SocketSingleton.getSocketInstance().startTime = startDate.toString()
                SocketSingleton.getSocketInstance().endTime = endDate.toString()

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
            val code = dataJson.getString("code").toInt()
            if (code == 0) {
                val res = dataJson.getString("data")

                Log.i("Action", "onResponse: $res")
                ble.writeDataWeLockResponse(code = res)
            } else {
                ble.disconnectGatt()
                SocketSingleton.getSocketInstance().isProcessActive = false;
                UtilDevice.sendResponseToServer(status = Constants.CODE_MSG_PARAMS);
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