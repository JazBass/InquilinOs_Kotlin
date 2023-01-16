package com.mch.blekot.io.socket

import org.json.JSONArray
import org.json.JSONObject
import android.annotation.SuppressLint
import android.util.Log
import com.mch.blekot.io.ble.ActionManager
import com.mch.blekot.services.SocketService
import com.mch.blekot.util.Constants
import com.mch.blekot.util.ProcessDataJson
import io.socket.client.IO
import io.socket.client.Socket
import java.lang.Exception
import java.net.URI
import java.util.*

class SocketSingleton private constructor() {
    var isProcessActive = false
    var clientFromServer = ""
    val socket: Socket
    var endTime: String? = null
    var startTime: String? = null

    //Constructor
    init {
        val options = IO.Options()
        options.reconnection = true
        socket = IO.socket(URI.create(Constants.URL_TCP), options)
        socket.on(Socket.EVENT_CONNECT) {
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
                    ActionManager.sendResponseToServer(
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

                    Constants.ACTION_OPEN_LOCK -> ActionManager.openLock()

                    Constants.ACTION_NEW_CODE -> {
                        val code = Objects.requireNonNull(pDataJson.getValue("code"))
                            .toString()
                        var days = Objects.requireNonNull(pDataJson.getValue("days"))
                            .toString().toInt()
                        days = if (days == 0) Constants.MIN_DAYS_PASSWORD else days
                        ActionManager.setNewCode(code, days)
                    }

                    Constants.ACTION_SET_CARD -> {
                        val qr = Objects.requireNonNull(pDataJson.getValue("Qr"))
                            .toString()
                        val type = Objects.requireNonNull(pDataJson.getValue("type"))
                            .toString()
                        ActionManager.setNewCard(qr, type)
                    }

                    Constants.ACTION_OPEN_PORTAL -> ActionManager.openPortal()

                    Constants.ACTION_SYNC_TIME -> {
                        val newTime = Objects.requireNonNull(pDataJson.getValue("syncTime"))
                            .toString()
                        ActionManager.syncTime(newTime)
                    }

                    Constants.ACTION_GET_BATTERY ->{
                        ActionManager.getDevicesBatteries()
                    }

                    "tvOn" -> ActionManager.launchNotification()
                }
            } catch (e: Exception) {
                isProcessActive = false //Error por JSON
                e.printStackTrace()
                ActionManager.sendResponseToServer(
                    Constants.CODE_MSG_KO,
                    Constants.STATUS_LOCK,
                    Constants.STATUS_LOCK
                )
            }
        }
        socket.on(Socket.EVENT_DISCONNECT) { args: Array<Any> -> println("disconnect due to: " + args[0]) }
        socket.connect()
    }

    companion object {
        private val TAG = SocketService::class.java.simpleName

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