# BleKotlin

Aplicación para controlar las viviendas.

Importante: 

    Al instalar configurar el string ID en com.mch.blekot.util.Constants con el nombre del piso 
    al que irá para la conexion por socket.

    Tambien en las clases WeLock y BLE especificar el MAC ADDRESS, Bluetooth Device Name y Device ID
    correspondientes a la manija a utilizar

Funcionamiento:

    Al iniciar la app se lanza un servicio (DeviceSocketIO) con un socket (SocketSingleton)
    
    Si la orden es para conectarse con la manija, se llama a la clase WeLock la cual conecta con la 
    clase BLE 

    Si en cambio la orde es para openPortal se envia una request http en la red local

