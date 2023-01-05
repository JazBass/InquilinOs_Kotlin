package com.mch.blekot.services

import com.mch.blekot.util.UtilDevice.sendResponseToServer
import com.mch.blekot.io.socket.welock.WeLock.openLock
import com.mch.blekot.io.socket.welock.WeLock.setNewCode
import com.mch.blekot.io.socket.welock.WeLock.setNewCard
import com.mch.blekot.io.socket.welock.WeLock.syncTime
import okhttp3.OkHttpClient
import org.json.JSONArray
import com.mch.blekot.services.SocketSingleton
import com.mch.blekot.util.UtilDevice
import org.json.JSONObject
import com.mch.blekot.util.ProcessDataJson
import com.mch.blekot.io.socket.welock.WeLock
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import com.mch.blekot.R
import com.mch.blekot.services.SocketService
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.mch.blekot.util.Constants
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.Request
import java.io.IOException
import java.lang.Exception
import java.net.URI
import java.util.*
import kotlin.jvm.Synchronized

class SocketSingleton private constructor() {
    var isProcessActive = false
    var clientFromServer = ""
    val socket: Socket
    var endTime: String? = null
    var startTime: String? = null
    private val httpClient = OkHttpClient()
    private var context: Context? = null
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    //Constructor
    init {
        val options = IO.Options()
        options.reconnection = true
        socket = IO.socket(URI.create(Constants.URL_TCP), options)
        socket.on(Socket.EVENT_CONNECT) { args: Array<Any?>? ->
            println("Conectado!!")
            socket.emit(Constants.ACTION_LOG, Constants.ID, Constants.MESSAGE)
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) { args: Array<Any> -> println("connect_error: " + args[0]) }
        socket.on(Constants.ACTION_ADMIN) { args: Array<Any?>? ->
            val dataResponse: JSONArray
            try {
                // procesoActivo: TRUE -> No se ejecuta ninguna accion
                // procesoActivo: FALSE -> Se ejecuta accion nueva
                if (isProcessActive) {
                    Log.i(TAG, "Hay una peticion pendiente!!")
                    sendResponseToServer(
                        Constants.CODE_MSG_PENDANT,
                        Constants.STATUS_LOCK,
                        Constants.STATUS_LOCK
                    )
                    return@on
                }
                dataResponse = JSONArray(args)
                val dataJson = JSONObject(dataResponse[1].toString())

                // Obtener dataJSON en un HashMap
                val pDataJson = ProcessDataJson()
                pDataJson.getData(dataJson)
                val action = Objects.requireNonNull(pDataJson.getValue("cmd"))
                    .toString()
                clientFromServer = Objects.requireNonNull(pDataJson.getValue("clientFrom"))
                    .toString()
                isProcessActive = true
                when (action) {

                    Constants.ACTION_OPEN_LOCK -> openLock()

                    Constants.ACTION_NEW_CODE -> {
                        val code = Objects.requireNonNull(pDataJson.getValue("code"))
                            .toString()
                        var days = Objects.requireNonNull(pDataJson.getValue("days"))
                            .toString().toInt()
                        days = if (days == 0) Constants.MIN_DAYS_PASSWORD else days
                        setNewCode(code, days)
                    }

                    Constants.ACTION_SET_CARD -> {
                        val qr = Objects.requireNonNull(pDataJson.getValue("Qr"))
                            .toString()
                        val type = Objects.requireNonNull(pDataJson.getValue("type"))
                            .toString()
                        setNewCard(qr, type)
                    }

                    Constants.ACTION_OPEN_PORTAL -> openPortal()

                    Constants.ACTION_SYNC_TIME -> {
                        val newTime = Objects.requireNonNull(pDataJson.getValue("syncTime"))
                            .toString()
                        syncTime(newTime)
                    }

                    "tvOn" -> launchNotification()
                }
            } catch (e: Exception) {
                isProcessActive = false //Error por JSON
                e.printStackTrace()
                sendResponseToServer(
                    Constants.CODE_MSG_KO,
                    Constants.STATUS_LOCK,
                    Constants.STATUS_LOCK
                )
            }
        }
        socket.on(Socket.EVENT_DISCONNECT) { args: Array<Any> -> println("disconnect due to: " + args[0]) }
        socket.connect()
    }

    private fun launchNotification() {
        val name: CharSequence = "TvNotify"
        val description = "Tv Notify for IFTTT"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager = NotificationManagerCompat.from(context!!)
        notificationManager.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.ico_website)
            .setContentTitle("TV")
            .setContentText("TVON")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.notify(1, builder.build())
    }

    private fun openPortal() {
        try {
            val request = Request.Builder()
                .url("http://192.168.1.150/portal/open")
                .get()
                .build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Log.i("Open Portal", "Response: " + response.body()!!.string())
                sendResponseToServer(
                    status = Constants.STATUS_ARDUINO_OK
                )
            } else throw IOException("Arduino connection fail")

        } catch (e: IOException) {
            e.printStackTrace()
            sendResponseToServer(
                status = Constants.STATUS_ARDUINO_ERROR
            )
        }
        // Si hay un error en la peticion OPEN-PORTAL, se permite realizar otra peticion
        isProcessActive = false
    }

    companion object {
        private val TAG = SocketService::class.java.simpleName
        private const val CHANNEL_ID = "TV"

        @SuppressLint("StaticFieldLeak")
        private var mInstance: SocketSingleton? = null

        @get:Synchronized
        val socketInstance: SocketSingleton?
            get() {
                if (mInstance == null) {
                    mInstance = SocketSingleton()
                }
                return mInstance
            }
    }
}