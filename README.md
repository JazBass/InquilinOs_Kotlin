# InquilinOs APP

Aplicación para controlar las viviendas.

Funcionamiento:

    Al iniciar la app se lanza un servicio (DeviceSocketIO) con un socket (SocketSingleton) que se 
    conecta a nuesta API (TCP MCH)
    
    Luego tenemos la clase ActionManager que gestiona las peticiones basandose en la "ACCION" que 
    queremos realizar.

    En caso de que la acción sea destinada a la manija, la clase BLE se comunica con
    la misma y la clase WeLock se encarga de de las request a la API de WeLock, el proceso el siguiente

    WeLock--> Pedimos el Token de autentificación para poder consumir la API
    BLE ----> Se consulta la bateria y el random number a la manija ya que los enviaremos como parámetros.
    WeLock--> Realizamos la request. (Dependiendo la accion pueden varian los parámetros). Si todo está 
              correcto la API nos devuelve el código
    BLE ----> Le enviamos el código a la manija y tomamos la respuesta

    Si en cambio la orden es para openPortal se envía una request http en la red local al arduino.

    No importa cual sea el resultado, se lo comunicamos a nuestra API TCP MCH.

