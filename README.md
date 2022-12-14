# BleKotlin

Aplicación para controlar las viviendas.

Importante: 

    Antes de instalar configurar en com.mch.blekot.util.Constants:
    * 'string ID' con el nombre del piso al que irá para la conexion por socket.
    * MAC ADDRESS, Bluetooth Device Name y Device ID correspondientes a la manija.




Funcionamiento:

    Al iniciar la app se lanza un servicio (DeviceSocketIO) con un socket (SocketSingleton)
    
    Si la orden es para conectarse con la manija, se llama al objeto WeLock el cual conecta con el 
    objeto BLE (ambos singleton)

    Si en cambio la orde es para openPortal se envia una request http en la red local

