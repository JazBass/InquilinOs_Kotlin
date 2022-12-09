package com.mch.blekot.util;

public final class Constants {
    // Constantes para la conexion SOCKET
    // public final static String ID = "Prueba1";
    public final static String ACTION_LOG = "Log";
    public final static String ACTION_ADMIN = "admin";
    public final static String RESPONSE_SOCKET_BLUETOOTH = "ResponseSocketBluetooth";
    public final static String MESSAGE = "Conectado";
    //public final static String URL_TCP = "http://tcpmch-env.eba-5tfqg8e4.eu-west-1.elasticbeanstalk.com/";
    public final static String URL_TCP = "http://192.168.1.113:3002/";

    public final static int CODE_INDEX = 20;
    public final static int CODE_USER = 15;
    public final static int CODE_TIMES = 65000;

    //public final static String URL_TCP = "http://192.168.0.29:3000";

    // Datos de configuracion Socket/Manija CAS43
    /*public final static String ID = "CAS43";
    public final static String MAC_ADRESS = "E0:D2:1A:65:67:F4";
    public final static String DEVICE_NAME = "WeLockKX2PV";
    public final static String DEVICE_ID_NUMBER = "21470403";*/

    // Datos de prueba
    public final static String ID = "PRUEBA100";
    public final static String MAC_ADRESS = "C7:12:48:82:08:2F";
    public final static String DEVICE_NAME = "WeLockAWPOR";
    public final static String DEVICE_ID_NUMBER = "21471618";

    // Datos de OFICINA
    /*public final static String ID = "Oficina";
    public final static String MAC_ADRESS = "CC:37:4D:3B:11:3A";
    public final static String DEVICE_NAME = "WeLockGE4CK";
    public final static String DEVICE_ID_NUMBER = "21471477";*/

    // public final static String MAC_ADRESS = "D6:F5:3B:E4:6D:F5"; //Chueca9

    // Contantes para el servicio
    public final static String ACTION_RUN_SERVICE = "com.rdajila.tandroidsocketio.services.action.RUN_SERVICE";
    public final static String EXTRA_MSG = "com.rdajila.tandroidsocketio.services.extra.MEMORY";
    public final static String EXTRA_COUNTER = "com.rdajila.tandroidsocketio.services.extra.COUNT";

    public final static String ACTION_OPEN_LOCK = "openLock";
    public final static String ACTION_NEW_CODE = "newCode";
    public final static String ACTION_SET_CARD = "setCard";
    public static final String ACTION_OPEN_PORTAL = "openPortal";

    public final static int MAX_SEND_DATA = 20;
    public final static int MIN_DAYS_PASSWORD = 1;

    public final static String MSG_OK = "Se ha procesado exitosamente la peticion";
    public final static String MSG_PENDANT = "Hay una peticion pendiente!!";
    public final static String MSG_KO = "Error con el dispositivo Bluetooth!!";
    public final static String MSG_PARAMS = "Error, el valor de los parametros son erroneos!";

    public final static int CODE_MSG_OK = 1;
    public final static int CODE_MSG_PENDANT = 0;
    public final static int CODE_MSG_PARAMS = 2;
    public final static int CODE_MSG_KO = -1;
    public final static int STATUS_LOCK = -1;
}