package com.mch.blekot.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.util.Log
import com.mch.blekot.services.SocketSingleton
import com.mch.blekot.io.socket.welock.WeLock
import com.mch.blekot.util.Constants
import com.mch.blekot.util.HexUtil
import java.util.*

class Ble(weLock: WeLock) {

    private lateinit var mCode: String

    private val TAG = "Main Activity"

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
                writeChar(gatt)
            } else Log.i(TAG, "onDescriptorWrite: Descriptor is not connected")
        }

        fun String.decodeHex(): ByteArray {
            check(length % 2 == 0)

            return chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()

        }

        /*-----------------------4ª-----------------------*/

        @ExperimentalUnsignedTypes
        fun ByteArray.toHexString() =
            asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }


        /*-----------------------5º-----------------------*/

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

        /*-----------------------6ª-----------------------*/

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

            } else if (characteristic.value[0].toInt() == 85 &&
                characteristic.value[1].toInt() == 49
            ) {
                if (characteristic.value[2].toInt() == 1) {
                    sendResponse("Success")
                    Log.i(TAG, "")
                }
                else Log.i(TAG, "ERROR")
                // Finaliza la accion con el bluetooth
                SocketSingleton.getSocketInstance().isProcesoActivo = false;
            }
            Log.i(TAG, "onCharacteristicChanged: Received")
            gatt.disconnect()
            gatt.close()
        }

    }

    private fun sendResponse(responseJson: String) {
        SocketSingleton.getSocketInstance().socket.emit("Response", responseJson)
        Log.i(TAG, "sendResponse: $responseJson")
    }

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
}