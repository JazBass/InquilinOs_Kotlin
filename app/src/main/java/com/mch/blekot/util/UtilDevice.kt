package com.mch.blekot.util

import android.util.Log
import com.mch.blekot.services.DeviceSocketIO
import com.mch.blekot.services.SocketSingleton


object UtilDevice {
    private val TAG = DeviceSocketIO::class.java.simpleName
    /**
     * Metodo que envia la respuesta al servidor OK/KO/Pendiente
     * 1 -> OK
     * 0 -> PENDIENTE
     * -1 -> ERROR
     * @param status
     */

    @JvmStatic
    fun sendResponseToServer(status: Int, statusMOne: Int? = Constants.STATUS_LOCK,
                             statusMTwo: Int? = Constants.STATUS_LOCK) {

        val mSocket = SocketSingleton.getSocketInstance()

        val msg: String = when(status){
            Constants.CODE_MSG_OK -> Constants.MSG_OK
            Constants.CODE_MSG_PENDANT -> Constants.MSG_PENDANT
            Constants.CODE_MSG_PARAMS -> Constants.MSG_PARAMS
            else -> Constants.MSG_KO
        }

        val responseJson = """
            { "status":$status,
              "statusMOne":$statusMOne,
              "statusMTwo":$statusMTwo,
              "msg":"$msg",
              "clientFrom":"${mSocket.clientFromServer}",
              "startTime":"${mSocket.startTime}",
              "endTime":"${mSocket.endTime}"
            }
        """.trimIndent()

        mSocket.socket.emit(
            Constants.RESPONSE_SOCKET_BLUETOOTH,
            Constants.ID,
            responseJson
        )
        Log.i(TAG, "sendResponse: $responseJson")
    }
}