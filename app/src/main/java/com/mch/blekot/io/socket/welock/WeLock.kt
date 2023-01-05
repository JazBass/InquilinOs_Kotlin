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


object WeLock {

    /*WeLock's API paths*/
    private const val urlWeLock = "https://api.we-lock.com"
    private const val PATH_CARD = "/API/Device/DeviceCardCommand"
    private const val PATH_CODE = "/API/Device/DeviceSetTemporaryPassword"
    private const val PATH_OPEN = "/API/Device/DeviceUnLockCommand"
    private const val PATH_TOKEN = "/API/Auth/Token"
    private const val PATH_SYNC_DATE = "/API/Device/DeviceSyncTime"

    private var mHttpClient: OkHttpClient = OkHttpClient()

    private var mToken: String? = null
    private var mNewPassword: String? = null
    private var mType: String? = null
    private var mQr: String? = null
    private var mDays: Int = 1
    private var mNewTime: String? = null

    private const val deviceName = Constants.DEVICE_NAME
    private const val deviceIdNumber = Constants.DEVICE_ID_NUMBER

    private lateinit var mRndNumber: String
    private lateinit var mDevicePower: String

    private lateinit var mAction: String

    /*
    * La anotacion "@JvmStatic" es para que la funcion sea de tipo static al ser invocada desdecodigo JAVA
    */

    @JvmStatic
    fun openLock() {
        mAction = Constants.ACTION_OPEN_LOCK

        Ble.connectDevice()
    }

    @JvmStatic
    fun setNewCode(newPassword: String, days: Int) {
        mAction = Constants.ACTION_NEW_CODE
        mNewPassword = newPassword
        mDays = days

        Ble.connectDevice()
    }

    @JvmStatic
    fun setNewCard(qr: String, type: String) {
        mAction = Constants.ACTION_SET_CARD
        mQr = qr
        mType = type

        Ble.connectDevice()
    }

    @JvmStatic
    fun syncTime(newTime: String) {
        mAction = Constants.ACTION_SYNC_TIME
        mNewTime = newTime

        Ble.connectDevice()
    }

    /*Pedimos la token*/
    fun getToken(battery: String, rdmNumber: String) {

        /*
        *Cuando syncronizamos el tiempo el callback nos trae de vuelta aqui, por eso desconectamos
        * el BleGatt, mandamos el mensaje al server y terminamos la operacion
        */

        if (mAction == "TimeSynchronized") {
            Ble.disconnectGatt()
            UtilDevice.sendResponseToServer(Constants.SYNC_TIME_OK)
            SocketSingleton.getSocketInstance().isProcessActive = false
            return
        }

        mRndNumber = rdmNumber
        mDevicePower = battery

        post(
            PATH_TOKEN,
            """{appID: "WELOCK2202161033", secret: "349910dfcdfac75df0fd1cf2cbb02adb"}""",
            tokenCallback
        )
    }


    /*Callback para recibir la toquen*/
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

    /*Aqui controlamos el pedido a la API dependiendo que querramos hacer*/
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

                //28800 = 8h -> Diferencia con China
                //7200 = 2h -> Para asegurarse que el codigo este funcional
                val startDate: Int = ((System.currentTimeMillis() / 1000) - 28800 - 7200).toInt()
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

            "syncTime" -> {
                val syncTimeJson = """{
                    appID: "WELOCK2202161033",
                    deviceNumber: "$deviceIdNumber",
                    deviceBleName: "$deviceName",
                    timestamp: $mNewTime,
                    deviceRandomFactor: "$mRndNumber"}""".trimIndent()

                Log.i("jsonTime: ", syncTimeJson)

                postWithToken(
                    PATH_SYNC_DATE,
                    syncTimeJson,
                    actionCallback
                )

                mAction = "TimeSynchronized"
            }


        }
    }


    /*Callback donde recibimos el codigo que le enviaremos a la manija*/
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
                Ble.writeDataWeLockResponse(code = res)
            } else {
                Log.i("ERROR", dataJson.toString())
                Ble.disconnectGatt()
                SocketSingleton.getSocketInstance().isProcessActive = false
                UtilDevice.sendResponseToServer(status = Constants.CODE_MSG_PARAMS)
            }
        }
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
        val call: Call = mHttpClient.newCall(request)
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
        val call: Call = mHttpClient.newCall(request)
        if (callback != null) {
            call.enqueue(callback)
        }
    }

}