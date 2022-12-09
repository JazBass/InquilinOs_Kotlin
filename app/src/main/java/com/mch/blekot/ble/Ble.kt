package com.mch.blekot.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.util.Log
import com.mch.blekot.io.socket.welock.WeLock
import com.mch.blekot.services.SocketSingleton
import com.mch.blekot.util.Constants
import com.mch.blekot.util.HexUtil
import com.mch.blekot.util.UtilDevice
import java.util.*

class Ble(weLock: WeLock) {

    private lateinit var mCode: String

    private lateinit var gattTmp: BluetoothGatt

    private val TAG = "Ble.kt"

    private val SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val NOTIFY_CHARACTERISTIC = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    private val WRITE_CHARACTER = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var characteristicNotify: BluetoothGattCharacteristic? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null

    /*para dividir en paquetes*/
    private var packAmount = 0

    private lateinit var mDataQueue: Queue<ByteArray>

    /*--------------------------BLE--------------------------*/

    fun writeChar(gatt: BluetoothGatt) {
        val dataIn = HexUtil.hexStringToBytes(mCode)
        mDataQueue = HexUtil.splitByte(dataIn, Constants.MAX_SEND_DATA)
        Log.i(TAG, "SIZE: ${mDataQueue.size}")
        writeDataDevice(gatt)
    }

    @SuppressLint("MissingPermission")
    fun sendBle(code: String? = null) {

        //si code es null pedimos rdm number y bateria, si no enviamos el codigo
        mCode = code ?: "5530"

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = btAdapter.getRemoteDevice(Constants.MAC_ADRESS)
        device.connectGatt(null, true, mGattCallback)
    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        /*-----------------------1º-----------------------*/

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
                Log.i(TAG, "onConnectionStateChange: Discover Services")
            }
        }

        /*-----------------------2º-----------------------*/
        /*Nos suscribimos a las notificaciones y escribimos el descriptor*/

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            characteristicNotify =
                gatt.getService(SERVICE_UUID).getCharacteristic(NOTIFY_CHARACTERISTIC)
            gatt.setCharacteristicNotification(characteristicNotify, true)
            val desc = characteristicNotify!!.getDescriptor(CCCD_UUID)
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(desc)
        }

        /*-----------------------3º-----------------------*/

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (descriptor.characteristic === characteristicNotify) {
                gattTmp = gatt
                writeChar(gattTmp)
            } else Log.i(TAG, "onDescriptorWrite: Descriptor is not connected")
        }

        /*-----------------------4º-----------------------*/

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (characteristicWrite === characteristic) {
                Log.i("Char Writed", "Char: ${characteristic.value.toHexString()}")
            } else Log.i(TAG, "ERROR: Write is not ok")

            writeDataDevice(gatt)
        }

        /*-----------------------5ª-----------------------*/

        /* Al recibir la respuesta de la manija lanzamos la request http*/


        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.value[0].toInt() == 85 && characteristic.value[1].toInt() == 48) {
                val rndNumber = characteristic.value[2].toUByte().toInt()
                val devicePower = characteristic.value[3].toUByte().toInt()
                val myJason = "{\"rndNumber\":$rndNumber, \"battery\":$devicePower}"
                Log.i(TAG, "onCharacteristicChanged: $myJason")

                weLock.getToken(devicePower.toString(), rndNumber.toString())

                return

            } else if (characteristic.value[0].toInt() == 85 &&
                characteristic.value[1].toInt() == 49
            ) {
                if (characteristic.value[2].toInt() == 1) {
                    //sendResponse("Success")
                    Log.i(TAG, "")
                }
                else Log.i(TAG, "ERROR")
                // Finaliza la accion con el bluetooth
                //SocketSingleton.getSocketInstance().isProcesoActivo = false;
            }else { // Otras acciones
                // Finaliza la accion con el bluetooth
                //SocketSingleton.getSocketInstance().isProcesoActivo = false;
            }
            Log.i(TAG, "onCharacteristicChanged: Received")

            Log.i(TAG, "DIM: ${characteristic.value.size}")
            Log.i(TAG, "Pos 0: ${characteristic.value[0].toInt()} - ${characteristic.value[0].toUByte().toInt()}")
            Log.i(TAG, "Pos 1: ${characteristic.value[1].toInt()} - ${characteristic.value[1].toUByte().toInt()}")
            Log.i(TAG, "Pos 2: ${characteristic.value[2].toInt()} - ${characteristic.value[2].toUByte().toInt()}")
            Log.i(TAG, "Pos 3: ${characteristic.value[3].toInt()} - ${characteristic.value[3].toUByte().toInt()}")

            //gatt.disconnect()
            //gatt.close()

            // Finaliza la accion con el bluetooth
            SocketSingleton.getSocketInstance().isProcessActive = false;
            UtilDevice.sendResponseToServer(Constants.CODE_MSG_OK, characteristic.value[2].toInt(), characteristic.value[3].toInt())

            gattTmp.disconnect()
            gattTmp.close()
        }

    }

    @SuppressLint("MissingPermission")
    public fun writeDataWeLockResponse(code: String) {
        try {
            if ( code.trim().length == 0 ) {
                throw Exception("Error code es vacio!!")
            }
            mCode = code
            writeChar(gattTmp)
        } catch(e: Exception) {
            // Si hay algun error al obtener respuesta desde WeLock
            // Desconectamos el gatt y permitimos otra peticion
            Log.e(TAG, e.message.toString())
            gattTmp.disconnect()
            gattTmp.close()
            SocketSingleton.getSocketInstance().isProcessActive = false;
            UtilDevice.sendResponseToServer(status = Constants.CODE_MSG_KO);
        }
    }

    @SuppressLint("MissingPermission")
    public fun desconectarGattTmp () {
        gattTmp.disconnect()
        gattTmp.close()
    }

//    private fun sendResponse(responseJson: String) {
//        SocketSingleton.getSocketInstance().socket.emit(Constants.RESPONSE_SOCKET_BLUETOOTH, Constants.ID, responseJson)
//        Log.i(TAG, "sendResponse: $responseJson")
//    }

    @SuppressLint("MissingPermission")
    private fun writeDataDevice(gatt: BluetoothGatt) {
        var counter = 1;
        while (mDataQueue.peek() != null) {
            if (counter > 1) break;
            val data = mDataQueue.poll()

            characteristicWrite = gatt.getService(SERVICE_UUID).getCharacteristic(WRITE_CHARACTER)
            characteristicWrite!!.value = data
            //Log.i(TAG, "Sending: ${characteristicWrite!!.value.toHexString()}")
            Log.i(TAG, "Sending: ${HexUtil.formatHexString(characteristicWrite!!.value, true)}")
            characteristicWrite!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.writeCharacteristic(characteristicWrite)
            counter++;
        }
    }

    /*****************UTIL****************/
    @ExperimentalUnsignedTypes
    fun ByteArray.toHexString() =
        asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

    // TODO: Instanciar el gatt una sola vez

}