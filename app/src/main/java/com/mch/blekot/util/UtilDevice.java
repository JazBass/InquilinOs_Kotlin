package com.mch.blekot.util;

import android.util.Log;

import com.mch.blekot.services.DeviceSocketIO;
import com.mch.blekot.services.SocketSingleton;

public final class UtilDevice {

    private static final String TAG = DeviceSocketIO.class.getSimpleName();

    /**
     * Metodo que envia la respuesta al servidor OK/KO/Pendiente
     * 1 -> OK
     * 0 -> PENDIENTE
     * -1 -> ERROR
     * @param status
     */
    public static void sendResponseToServer(int status, int statusMOne, int statusMTwo) {
        //"{\"status\": 1, \"msg\": \"Se ha procesado exitosamente la peticion\"}"
        int statusCode = status;
        String msg;

        if( status == Constants.CODE_MSG_OK ){
            msg = Constants.MSG_OK;
        }else if(status == Constants.CODE_MSG_PENDIENTE){
            msg = Constants.MSG_PENDIENTE;
        }else if(status == Constants.CODE_MSG_PARAMS){
            msg = Constants.MSG_PARAMS;
        }else{
            msg = Constants.MSG_KO;
        }

        String responseJson = "".concat("{")
        .concat("\"status\":").concat(Integer.toString(statusCode)).concat(",")
                .concat("\"statusMOne\":").concat(Integer.toString(statusMOne)).concat(",")
                .concat("\"statusMTwo\":").concat(Integer.toString(statusMTwo)).concat(",")
                .concat("\"msg\":").concat("\"").concat(msg).concat("\"").concat(",")
                .concat("\"clientFrom\":").concat("\"").concat(SocketSingleton.getSocketInstance().getClienteFromServer()).concat("\"")
                .concat("}");

        SocketSingleton.getSocketInstance().getSocket().emit(Constants.RESPONSE_SOCKET_BLUETOOTH, Constants.ID, responseJson);

        Log.i(TAG, "sendResponse: " + responseJson);
    }
}