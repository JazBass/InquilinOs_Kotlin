package com.mch.blekot.common

object Constants {

    /*********************Pruebas**********************/

    var ID = ""
    const val DEVICE_ID_KEY = "DEVICE_ID"

    //Server de pruebas
    const val URL_TCP: String = "https://tcpmch.fly.dev"

    //Localhost Javier :)
    //const val URL_TCP = "http://192.168.0.76:3002/"

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

    const val PARAMETER_QR = "Qr"
    const val PARAMETER_CMD = "cmd"
    const val PARAMETER_DAYS = "days"
    const val PARAMETER_CODE = "code"
    const val PARAMETER_INDEX = "index"
    const val PARAMETER_TIMES = "times"
    const val PARAMETER_TYPE = "type"
    const val PARAMETER_SYNC_TIME = "syncTime"
    const val PARAMETER_DEVICE_ID = "deviceId"
    const val PARAMETER_IP_ARDUINO = "ipArduino"
    const val PARAMETER_DEVICE_NAME ="deviceName"
    const val PARAMETER_MAC_ADDRESS = "macAddress"
    const val PARAMETER_CLIENT_FROM = "clientFrom"

    /********************BLE********************/

    const val CODE_USER = 15
    const val MAX_SEND_DATA = 20
    const val MIN_DAYS_PASSWORD = 1

    /****************SocketResponse****************/

    const val CODE_MSG_OK = 1

    const val CODE_MSG_PENDANT = 0
    const val CODE_MSG_KO = -1
    const val CODE_MSG_NULL_POINT = -2
    const val STATUS_FAIL_VALIDATION = -3
    const val CODE_MSG_BLE_OFF = -4
    const val CODE_TIMEOUT_SCAN = -5
    const val CODE_TIMEOUT = -6
    const val CODE_MSG_PARAMS = -7
    const val STATUS_ARDUINO_ERROR = -8

    const val MSG_PENDANT = "Hay una peticion pendiente"
    const val MSG_KO = "Error con el dispositivo móvil"
    const val MSG_OK = "Se ha procesado exitosamente la peticion"
    const val MSG_NULL_POINT = "Faltan enviar parametros al móvil"
    const val MSG_BLE_OFF = "El bluetooth del móvil no se encuentra activado"
    const val MSG_PARAMS = "Error en WeLock-API, parametros erroneos o manija no activada"
    const val MSG_TIMEOUT_SCAN = "Tiempo de scaneo agotado, vuela a intertalo"
    const val MSG_TIMEOUT = "Tiempo de espera agotado"
    const val MSG_ARDUINO_ERROR = "Error al conectar con arduino"

    const val ERROR_ARDUINO = "IP del arduino mal escrita"
    const val ERROR_MAC_ADDRESS = "Mac Address mal escrito"
    const val ERROR_LENGTH = "Error en la cantidad de caracteres"
    const val ERROR_ONLY_NUMBERS = "El ID debe contener solo numeros"

    const val STATUS_LOCK = -1
    const val STATUS_ARDUINO_OK = 1
    const val STATUS_BLE_DISCONNECT = -1

    const val SYNC_TIME_OK = 1
    const val SYNC_TIME_ERROR = -1

    /*******************Recorder*******************/

    const val NOISE_MIN = 50.00
    const val DECIBEL_DATA_LENGTH = 60
    const val INTERVAL_GET_DECIBEL: Long = 1000

    /*************Socket CMD MANAGER**************/

    const val ACTION_OPEN_LOCK = "openLock"
    const val ACTION_NEW_CODE = "newCode"
    const val ACTION_SET_CARD = "setCard"
    const val ACTION_OPEN_PORTAL = "openPortal"
    const val ACTION_SYNC_TIME = "syncTime"

    /*****************ACTION MANAGER*****************/

    const val OPEN_LOCK = 0
    const val NEW_CODE = 1
    const val SET_CARD = 2
    const val SYNC_TIME = 4
    const val READ_RECORD = 5
    const val GET_BATTERY = 6
    const val TIME_SYNCHRONIZED = 7

}