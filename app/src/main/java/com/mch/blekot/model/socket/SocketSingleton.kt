package com.mch.blekot.model.socket

import org.json.JSONArray
import org.json.JSONObject
import android.annotation.SuppressLint
import android.util.Log
import com.mch.blekot.common.utils.ActionManager
import com.mch.blekot.services.SocketService
import com.mch.blekot.common.Constants
import com.mch.blekot.common.ProcessDataJson
import com.mch.blekot.common.ValidateException
import com.mch.blekot.common.ValidateUtil
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import java.net.URI
import java.util.*
import kotlin.Exception

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

                val macAddress =
                    Objects.requireNonNull(pDataJson.getValue("macAddress")).toString()

                val deviceName =
                    Objects.requireNonNull(pDataJson.getValue("deviceName")).toString()

                val deviceId =
                    Objects.requireNonNull(pDataJson.getValue("deviceId")).toString()

                val ipArduino =
                    Objects.requireNonNull(pDataJson.getValue("ipArduino")).toString()

                ValidateUtil.setUpCredentials(
                    macAddress = macAddress,
                    deviceName = deviceName,
                    deviceId = deviceId,
                    ipArduino = ipArduino
                )

                when (action) {

                    Constants.ACTION_OPEN_LOCK -> executeAction { ActionManager.openLock() }

                    Constants.ACTION_NEW_CODE -> {
                        val code = Objects.requireNonNull(pDataJson.getValue("code"))
                            .toString()
                        var days = Objects.requireNonNull(pDataJson.getValue("days"))
                            .toString().toInt()
                        days = if (days == 0) Constants.MIN_DAYS_PASSWORD else days
                        executeAction { ActionManager.setNewCode(code, days) }
                    }

                    Constants.ACTION_SET_CARD -> {
                        val qr = Objects.requireNonNull(pDataJson.getValue("Qr"))
                            .toString()
                        val type = Objects.requireNonNull(pDataJson.getValue("type"))
                            .toString()
                        executeAction { ActionManager.setNewCard(qr, type) }
                    }

                    Constants.ACTION_OPEN_PORTAL -> executeAction { ActionManager.openPortal() }

                    Constants.ACTION_SYNC_TIME -> {
                        val newTime = Objects.requireNonNull(pDataJson.getValue("syncTime"))
                            .toString()
                        executeAction { ActionManager.syncTime(newTime) }
                    }

                    Constants.ACTION_GET_BATTERY -> {
                        executeAction { ActionManager.getDevicesBatteries() }
                    }

                }
            } catch (e: ValidateException) {
                isProcessActive = false
                socket.emit(
                    Constants.RESPONSE_SOCKET_BLUETOOTH,
                    Constants.ID,
                    ValidateUtil.getResponse()
                )
            }catch (e: java.lang.NullPointerException){
                isProcessActive = false
                ActionManager.sendResponseToServer(
                    Constants.CODE_MSG_NULL_POINT,
                    Constants.STATUS_LOCK,
                    Constants.STATUS_LOCK
                )
            } catch (e: Exception) {
                isProcessActive = false
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

    //coroutines
    private fun executeAction(block: suspend () -> Unit): Job {
        return MainScope().launch(Dispatchers.IO) {
            block()
        }
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