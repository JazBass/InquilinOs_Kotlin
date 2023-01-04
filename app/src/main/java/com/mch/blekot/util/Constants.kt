package com.mch.blekot.util

object Constants {

    // Datos de configuracion Socket/Manija CAS43
    //const val ID = "CAS43";
    //const val MAC_ADDRESS = "E0:D2:1A:65:67:F4";
    //const val DEVICE_NAME = "WeLockKX2PV";
    //const val DEVICE_ID_NUMBER = "21470403";

    // Datos de prueba
    //const val ID = "PRUEBA100";
    //const val MAC_ADDRESS = "C7:12:48:82:08:2F";
    //const val DEVICE_NAME = "WeLockAWPOR";
    //const val DEVICE_ID_NUMBER = "21471618";

    // Datos de OFICINA
    //const val ID = "Oficina";
    //const val MAC_ADDRESS = "CC:37:4D:3B:11:3A";
    //const val DEVICE_NAME = "WeLockGE4CK";
    //const val DEVICE_ID_NUMBER = "21471477";

    //Chueca9
    const val ID = "Prueba9"
    const val MAC_ADDRESS = "D6:F5:3B:E4:6D:F5"
    const val DEVICE_NAME = "WeLockE31J8"
    const val DEVICE_ID_NUMBER = "21471175"
    const val URL_TCP = "http://192.168.0.15:3002/"

    // Constantes para la conexion SOCKET
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

    const val ACTION_OPEN_LOCK = "openLock"
    const val ACTION_NEW_CODE = "newCode"
    const val ACTION_SET_CARD = "setCard"
    const val ACTION_OPEN_PORTAL = "openPortal"
    const val ACTION_SYNC_TIME = "syncTime"

    const val MAX_SEND_DATA = 20
    const val MIN_DAYS_PASSWORD = 1

    const val MSG_OK = "Se ha procesado exitosamente la peticion"
    const val MSG_PENDANT = "Hay una peticion pendiente!!"
    const val MSG_KO = "Error con el dispositivo Bluetooth!!"

    const val MSG_PARAMS = "Error, el valor de los parametros son erroneos!"

    const val CODE_MSG_OK = 1
    const val CODE_MSG_PENDANT = 0
    const val CODE_MSG_PARAMS = 2
    const val CODE_MSG_KO = -1

    const val STATUS_LOCK = -1
    const val STATUS_ARDUINO_OK = 1
    const val STATUS_ARDUINO_ERROR = -1

    const val SYNC_TIME_OK = 1
    const val SYNC_TIME_ERROR = -1

    const val RUIDO_MIN = 50.00
    const val DECIBEL_DATA_LENGTH = 60
    const val INTERVAL_GET_DECIBEL: Long = 1000
}