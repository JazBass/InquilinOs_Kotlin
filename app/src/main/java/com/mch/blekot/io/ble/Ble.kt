package com.mch.blekot.io.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.util.Log
import com.mch.blekot.io.socket.SocketSingleton
import com.mch.blekot.io.welock.BatteriesManager
import com.mch.blekot.util.Constants
import com.mch.blekot.util.HexUtil
import java.util.*

object Ble {

    private lateinit var mCode: String

    private lateinit var gattTmp: BluetoothGatt

    private const val TAG = "Ble.kt"

    private val SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val NOTIFY_CHARACTERISTIC = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    private val WRITE_CHARACTER = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var characteristicNotify: BluetoothGattCharacteristic? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null

    private var isOnlyAsk = false

    private lateinit var mBluetoothGatt: BluetoothGatt

    private lateinit var mDataQueue: Queue<ByteArray>

    /*--------------------------BLE--------------------------*/

    fun writeChar(gatt: BluetoothGatt) {
        val dataIn = HexUtil.hexStringToBytes(mCode)
        mDataQueue = HexUtil.splitByte(dataIn!!, Constants.MAX_SEND_DATA)
        Log.i(TAG, "SIZE: ${mDataQueue.size}")
        writeDataDevice(gatt)
    }

    object BleDevice {
        private val adapter = BluetoothAdapter.getDefaultAdapter()
        val gatt: BluetoothDevice? = adapter.let { adapter ->
            try {
                return@let adapter.getRemoteDevice(Constants.MAC_ADDRESS)
            }catch (exception: IllegalArgumentException){
                Log.w(TAG, "Objeto no encontrado, buscando dispositivo...")
                return@let null
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectDevice(isOnlyAsk: Boolean = false) {

        mCode = "5530"

        //Only battery status ask
        Ble.isOnlyAsk = isOnlyAsk

        if (BleDevice.gatt != null) {
            BleDevice.gatt.connectGatt(null, true, mGattCallback)
        }else{
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            btAdapter?.let  { adapter ->
                try {
                    val device = adapter.getRemoteDevice(Constants.MAC_ADDRESS)
                    mBluetoothGatt = device.connectGatt(null, true, mGattCallback)
                }catch (exception: IllegalArgumentException){
                    Log.w(TAG, "Dispositivo no encontrado")
                }
            } ?: run {
                Log.w(TAG, "BluetoothAdapter no inicializado")
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        /** The following callbacks are in order, each one execute the next **/

        /*-----------------------1º-----------------------*/

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
                Log.i(TAG, "Conectado! Buscando servicios...")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "Desconectado")
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

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            var charArray: Array<Int> = emptyArray()

            for (char in characteristic.value){
                charArray += char.toUByte().toInt()
            }

            readCharacteristics(charArray)
        }

        @SuppressLint("MissingPermission")
        private fun readCharacteristics(characteristic: Array<Int>){

            var statusResponse = -1

            if (characteristic[0] == 85){
                when(characteristic[1]){
                    /** All responses start with 48 except SetCard **/
                    48 -> {
                        val rndNumber = characteristic[2]
                        val devicePower = characteristic[3]

                        Log.i(TAG, "onCharacteristicChanged: rndNumber: $rndNumber, battery: $devicePower")

                        if (isOnlyAsk) {
                            BatteriesManager.sendResponse(devicePower)
                            isOnlyAsk = false
                            return
                        }
                        ActionManager.getToken(devicePower.toString(), rndNumber.toString())
                        return
                    }
                    /** SetCard Response **/
                    49 -> {
                        if (characteristic[2] != 1) Log.i(TAG, "ERROR")
                    }
                }
                statusResponse = characteristic[2]
            }else if (characteristic[0] == 165){
                statusResponse = characteristic[3]
            }

            SocketSingleton.socketInstance!!.isProcessActive = false
            Log.i("Status Response", "$statusResponse")
            ActionManager.sendResponseToServer(
                Constants.CODE_MSG_OK,
                statusMOne = statusResponse
            )
            gattTmp.close()
        }

        // TODO: catch lock's errors like motor and wrong response
    }

    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        gattTmp.close()
    }

    @SuppressLint("MissingPermission")
    fun writeDataWeLockResponse(code: String) {
        try {
            if (code.trim().isEmpty()) {
                throw Exception("Error code es vacio!!")
            }
            mCode = code
            writeChar(gattTmp)
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())

            SocketSingleton.socketInstance!!.isProcessActive = false
            ActionManager.sendResponseToServer(status = Constants.CODE_MSG_KO)

            gattTmp.close()
        }
    }


    @SuppressLint("MissingPermission")
    private fun writeDataDevice(gatt: BluetoothGatt) {
        var counter = 1
        while (mDataQueue.peek() != null) {
            if (counter > 1) break
            val data = mDataQueue.poll()

            characteristicWrite = gatt.getService(SERVICE_UUID).getCharacteristic(WRITE_CHARACTER)
            characteristicWrite!!.value = data
            //Log.i(TAG, "Sending: ${characteristicWrite!!.value.toHexString()}")
            Log.i(TAG, "Sending: ${HexUtil.formatHexString(characteristicWrite!!.value, true)}")
            characteristicWrite!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.writeCharacteristic(characteristicWrite)
            counter++
        }
    }

    /*****************UTIL****************/
    @ExperimentalUnsignedTypes
    fun ByteArray.toHexString() =
        asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

}