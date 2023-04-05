package com.mch.blekot.model.welock

import okhttp3.*
import java.util.*
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import com.mch.blekot.model.ble.Ble
import com.mch.blekot.common.Constants
import com.mch.blekot.common.ActionManager

object WeLock {

    /*WeLock's API paths*/
    private const val urlWeLock = "https://api.we-lock.com"
    private const val PATH_TOKEN = "/API/Auth/Token"

    private var mHttpClient: OkHttpClient = OkHttpClient()

    private var mToken: String? = null

    private lateinit var mRndNumber: String
    private lateinit var mDevicePower: String


    /*Pedimos la token*/
    fun getToken(battery: String, rdmNumber: String, action: Int) {

        Log.i("Thread: ", Thread.currentThread().name)

        if (action == Constants.TIME_SYNCHRONIZED) {
            Ble.disconnectGatt()
            ActionManager.sendResponseToServer(Constants.SYNC_TIME_OK)
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

    /*Callback para recibir la token*/
    private var tokenCallback: Callback = object : Callback {

        override fun onFailure(p0: Call, p1: IOException) {
            TODO("Not yet implemented")
        }

        override fun onResponse(p0: Call, response: Response) {
            val dataJson = JSONObject(response.body()?.string()!!).getJSONObject("data")
            mToken = dataJson.getString("accessToken")
            Log.i("Token", "onResponse: $mToken")

            //Pedimos el path y el Json que se gestionan en la clase JsonManager
            val data = ActionManager.getPostData(mDevicePower, mRndNumber)
            val json = data["json"]
            val path = data["path"]

            postWithToken(
                path!!,
                json,
                actionCallback
            )
        }
    }

    /*Callback donde recibimos el código que le enviaremos a la manija*/
    private var actionCallback: Callback = object : Callback {

        override fun onFailure(p0: Call, p1: IOException) {
            Log.i("Action Callback", "FAIL")
        }

        override fun onResponse(p0: Call, response: Response) {

            val dataJson = JSONObject(response.body()?.string()!!)
            val code = dataJson.getString("code").toInt()
            if (code == 0) {
                val res = dataJson.getString("data")
                Log.i("WeLock", "$dataJson it's ok")
                Ble.writeDataWeLockResponse(code = res)
            } else {
                Log.i("WeLock", dataJson.toString())
                Ble.disconnectGatt()
                ActionManager.sendResponseToServer(status = Constants.CODE_MSG_PARAMS)
                Log.i("WeLock", "$dataJson error")
            }
        }
    }

    @Throws(IOException::class)
    fun post(path: String, json: String, callback: Callback?) {
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

        Log.i("WeLock", "postWithToken")

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