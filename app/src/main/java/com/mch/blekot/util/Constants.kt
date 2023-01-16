package com.mch.blekot.util

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
    //const val ID = "Oficina";
    //const val MAC_ADDRESS = "CC:37:4D:3B:11:3A";
    //const val DEVICE_NAME = "WeLockGE4CK";
    //const val DEVICE_ID_NUMBER = "21471477";

    /*********************Prueba100********************/

    const val ID = "PRUEBA100"
    const val MAC_ADDRESS = "D6:F5:3B:E4:6D:F5"
    const val DEVICE_NAME = "WeLockE31J8"
    const val DEVICE_ID_NUMBER = "21471175"
    const val URL_TCP = "https://tcpmch.fly.dev/"

    /*****************SocketConnection*****************/

    // public final static String ID = "Prueba1";
    const val ACTION_LOG = "Log"
    const val ACTION_ADMIN = "admin"
    const val RESPONSE_SOCKET_BLUETOOTH = "ResponseSocketBluetooth"
    const val MESSAGE = "Conectado"

    //const val URL_TCP = "http://tcpmch-env.eba-5tfqg8e4.eu-west-1.elasticbeanstalk.com/";
    //const val URL_TCP = "http://192.168.1.113:3002/";
    const val CODE_INDEX = 20
    const val CODE_USER = 15
    const val CODE_TIMES = 65000

    // Contantes para el servicio
    const val ACTION_RUN_SERVICE = "com.rdajila.tandroidsocketio.services.action.RUN_SERVICE"
    const val EXTRA_MSG = "com.rdajila.tandroidsocketio.services.extra.MEMORY"
    const val EXTRA_COUNTER = "com.rdajila.tandroidsocketio.services.extra.COUNT"

    /********************ForBLE********************/

    const val MAX_SEND_DATA = 20
    const val MIN_DAYS_PASSWORD = 1

    /*****************UserResponse*****************/

    const val MSG_OK = "Se ha procesado exitosamente la peticion"
    const val MSG_PENDANT = "Hay una peticion pendiente!!"
    const val MSG_KO = "Error con el dispositivo Bluetooth!!"

    const val MSG_PARAMS = "Error, el valor de los parametros son erroneos!"


    /****************SocketResponse****************/

    const val CODE_MSG_OK = 1
    const val CODE_MSG_PENDANT = 0
    const val CODE_MSG_PARAMS = 2
    const val CODE_MSG_KO = -1

    const val STATUS_LOCK = -1
    const val STATUS_ARDUINO_OK = 1
    const val STATUS_ARDUINO_ERROR = -1

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
    const val ACTION_GET_BATTERY = "getBattery"
    const val ACTION_TIME_SYNCHRONIZED = "timeSynchronized"

    /*****************ACTION MANAGER*****************/

    const val OPEN_LOCK = 0
    const val NEW_CODE = 1
    const val SET_CARD = 2
    const val OPEN_PORTAL = 3
    const val SYNC_TIME = 4
    const val READ_RECORD = 5
    const val GET_BATTERY = 6
    const val TIME_SYNCHRONIZED = 7

}