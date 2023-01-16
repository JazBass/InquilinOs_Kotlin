package com.mch.blekot.util

import android.util.Log
import com.mch.blekot.services.SocketService
import com.mch.blekot.io.socket.SocketSingleton


object UtilDevice {
    private val TAG = SocketService::class.java.simpleName

    /**
     * Metodo que envia la respuesta al servidor OK/KO/Pendiente
     * 1 -> OK
     * 0 -> PENDIENTE
     * -1 -> ERROR
     * @param status
     */

    @JvmStatic
    fun sendResponseToServer(
        status: Int, statusMOne: Int = Constants.STATUS_LOCK,
        statusMTwo: Int = Constants.STATUS_LOCK,
        phoneBattery: Int? = null, deviceBattery: Int? = null,
        isCharging: Boolean? = null, action: Int = 1
    ) {

        val msg: String = when (status) {
            Constants.CODE_MSG_OK -> Constants.MSG_OK
            Constants.CODE_MSG_PENDANT -> Constants.MSG_PENDANT
            Constants.CODE_MSG_PARAMS -> Constants.MSG_PARAMS
            else -> Constants.MSG_KO
        }

        var responseJson = ""

        when (action) {

            Constants.NEW_CODE -> {
                responseJson = """{ 
                    "status":$status,
                    "statusMOne":$statusMOne,
                    "statusMTwo":$statusMTwo,
                    "msg":"$msg",
                    "clientFrom":"${SocketSingleton.socketInstance?.clientFromServer}",
                    "startTime":"${SocketSingleton.socketInstance?.startTime}",
                    "endTime":"${SocketSingleton.socketInstance?.endTime}"
                    }""".trimIndent()
            }

            Constants.GET_BATTERY -> {
                responseJson = """{
                     "status":$status,
                     "clientFrom":"${SocketSingleton.socketInstance?.clientFromServer}",
                     "phoneBattery": $phoneBattery,
                     "isCharging": $isCharging,
                     "lockBattery": $deviceBattery
                     }""".trimIndent()
            }

            else -> {

                responseJson = """{
                     "status":$status,
                     "statusMOne":$statusMOne,
                     "statusMTwo":$statusMTwo,
                     "msg":"$msg",
                     "clientFrom":"${SocketSingleton.socketInstance?.clientFromServer}"
                     }""".trimIndent()

            }
        }

        SocketSingleton.socketInstance?.socket?.emit(
            Constants.RESPONSE_SOCKET_BLUETOOTH,
            Constants.ID,
            responseJson
        )

        Log.i(TAG, "sendResponse: $responseJson")
    }
}