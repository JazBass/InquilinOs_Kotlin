package com.mch.blekot.common

object Constants {

    /***********************CAS43***********************/
    //const val ID = "CAS43";
    //const val MAC_ADDRESS = "E0:D2:1A:65:67:F4";
    //const val DEVICE_NAME = "WeLockKX2PV";
    //const val DEVICE_ID_NUMBER = "21470403";

    /************************CRM************************/

    //const val ID = "CRM";
    //const val MAC_ADDRESS = "C7:12:48:82:08:2F";
    //const val DEVICE_NAME = "WeLockAWPOR";
    //const val DEVICE_ID_NUMBER = "21471618";

    /*********************Oficina**********************/

    const val ID = "PRUEBA100"

    var MAC_ADDRESS: String? = null
    var DEVICE_NAME: String? = null
    var DEVICE_ID_NUMBER: String? = null
    var IP_ARDUINO = "http://192.168.1.150/"

    //Server de pruebas
    //const val URL_TCP: String = "https://tcpmch2022.fly.dev"

    //Localhost Javier :)
    const val URL_TCP = "http://192.168.0.76:3002/"

    const val PATH_OPEN_PORTAL = "portal/open"

    /*********************Prueba100********************/

    //const val ID = "PRUEBA100"
    //const val MAC_ADDRESS = "D6:F5:3B:E4:6D:F5"
    //const val DEVICE_NAME = "WeLockE31J8"
    //const val DEVICE_ID_NUMBER = "21471175"

    /*****************SocketConnection*****************/

    // Contantes para el servicio
    const val ACTION_RUN_SERVICE = "com.rdajila.tandroidsocketio.services.action.RUN_SERVICE"
    const val EXTRA_MSG = "com.rdajila.tandroidsocketio.services.extra.MEMORY"
    const val EXTRA_COUNTER = "com.rdajila.tandroidsocketio.services.extra.COUNT"

    /***************SocketParameters***************/

    const val ACTION_LOG = "Log"
    const val ACTION_ADMIN = "admin"
    const val RESPONSE_SOCKET_BLUETOOTH = "ResponseSocketBluetooth"
    const val MESSAGE = "Conectado"

    const val PARAMETER_CMD = "cmd"
    const val PARAMETER_CLIENT_FROM = "clientFrom"
    const val PARAMETER_MAC_ADDRESS = "macAddress"
    const val PARAMETER_DEVICE_ID = "deviceID"
    const val PARAMETER_DEVICE_NAME ="deviceName"
    const val PARAMETER_CODE = "code"
    const val PARAMETER_DAYS = "days"
    const val PARAMETER_QR = "Qr"
    const val PARAMETER_TYPE = "type"
    const val PARAMETER_IP_ARDUINO = "ipArduino"

    /********************BLE********************/

    const val CODE_USER = 15
    const val MAX_SEND_DATA = 20
    const val MIN_DAYS_PASSWORD = 1

    /****************SocketResponse****************/

    const val CODE_MSG_OK = 1
    const val CODE_MSG_KO = -1
    const val CODE_MSG_PARAMS = 2
    const val CODE_MSG_PENDANT = 0
    const val CODE_MSG_NULL_POINT = -2

    const val MSG_PENDANT = "¡Hay una peticion pendiente!"
    const val MSG_KO = "¡Error con el dispositivo Bluetooth!"
    const val MSG_OK = "¡Se ha procesado exitosamente la peticion!"
    const val MSG_NULL_POINT = "¡Faltan enviar parametros al móvil!"
    const val MSG_PARAMS = "¡Error en WeLock-API, valor de parametros erroneo o manija no activada!"

    const val STATUS_LOCK = -1
    const val STATUS_ARDUINO_OK = 1
    const val STATUS_ARDUINO_ERROR = -1
    const val STATUS_BLE_DISCONNECT = -1

    const val SYNC_TIME_OK = 1
    const val SYNC_TIME_ERROR = -1

    const val ERROR_ARDUINO = "IP del arduino mal escrita"
    const val ERROR_MAC_ADDRESS = "Mac Address mal escrito"
    const val ERROR_LENGTH = "Error en la cantidad de caracteres"
    const val ERROR_ONLY_NUMBERS = "El ID debe contener solo numeros"

    /*******************Recorder*******************/

    const val NOISE_MIN = 50.00
    const val DECIBEL_DATA_LENGTH = 60
    const val INTERVAL_GET_DECIBEL: Long = 1000

    /*************Socket CMD MANAGER**************/

    const val ACTION_OPEN_LOCK = "openLock"
    const val ACTION_CONFIG = "config"
    const val ACTION_NEW_CODE = "newCode"
    const val ACTION_SET_CARD = "setCard"
    const val ACTION_OPEN_PORTAL = "openPortal"
    const val ACTION_SYNC_TIME = "syncTime"
    const val ACTION_GET_BATTERY = "getBattery"
    const val ACTION_TIME_SYNCHRONIZED = "timeSynchronized"

    /*****************ACTION MANAGER*****************/

    const val OPEN_LOCK = 0
    const val NEW_CODE = 1
    const val SET_CARD = 2
    const val SYNC_TIME = 4
    const val READ_RECORD = 5
    const val GET_BATTERY = 6
    const val TIME_SYNCHRONIZED = 7

}