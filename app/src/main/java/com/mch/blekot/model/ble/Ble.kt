package com.mch.blekot.model.ble

import java.util.*
import android.util.Log
import android.bluetooth.*
import kotlinx.coroutines.*
import com.mch.blekot.MainActivity
import com.mch.blekot.common.HexUtil
import com.mch.blekot.model.DeviceData
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.annotation.SuppressLint
import com.mch.blekot.model.Interactor
import com.mch.blekot.common.Constants
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanSettings
import com.mch.blekot.common.HexUtil.toHexString
import androidx.core.content.ContextCompat.getSystemService
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY

object Ble {

    private const val TAG = "Ble"
    private lateinit var mCode: String
    private var characteristicWrite: BluetoothGattCharacteristic? = null
    private var characteristicNotify: BluetoothGattCharacteristic? = null

    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    private val SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val WRITE_CHARACTER = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val NOTIFY_CHARACTERISTIC = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

    private var mIsSyncTime = false
    private lateinit var gattTmp: BluetoothGatt
    private lateinit var mDataQueue: Queue<ByteArray>

    private val bluetoothManager: BluetoothManager? =
        getSystemService(MainActivity.applicationContext(), BluetoothManager::class.java)!!
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    @SuppressLint("MissingPermission")
    suspend fun connectDevice(isSyncTime: Boolean = false) = withContext(Dispatchers.IO) {
        mIsSyncTime = isSyncTime

        if (!isBluetoothEnabled()) {
            Interactor.sendResponseToServer(Constants.STATUS_BLE_DISCONNECT)
            return@withContext
        }

        mCode = "5530"

        var isPaired = false
        var macAddress = ""

        //Verificamos si el dispositivo ya esta emparejado para evitar el scaneo
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceMacAddress = device.address

            isPaired = if (deviceName == DeviceData.DEVICE_NAME) {
                Log.i(TAG, "Disposivo emparejado : $deviceName")
                macAddress = deviceMacAddress
                true
            } else false
        }

        if (!isPaired) {
            scanLeDevice()
        } else {
            val gatt: BluetoothDevice? = bluetoothAdapter.let { adapter ->
                try {
                    return@let adapter?.getRemoteDevice(macAddress)
                } catch (exception: IllegalArgumentException) {
                    Log.w(TAG, "Objeto no encontrado, buscando dispositivo...")
                    return@let null
                }
            }
            gatt?.connectGatt(null, false, mGattCallback)
        }
    }

    private var isScanning = false
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

    @SuppressLint("MissingPermission")
    private suspend fun scanLeDevice() {
        isScanning = true

        val scanFilter = listOf<ScanFilter>(ScanFilter.Builder().setDeviceName(DeviceData.DEVICE_NAME).build())
        val scanSettings = ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_LATENCY).build()

        Log.i("DeviceName: ", "${DeviceData.DEVICE_NAME}")

        bluetoothLeScanner!!.startScan(scanFilter, scanSettings, leScanCallback)
        Log.i(TAG, "Scanning...")
        delay(30000)//30 seconds

        if (isScanning){
            Log.i(TAG, "Scan OUT")
            bluetoothLeScanner.stopScan(leScanCallback)
            Interactor.sendResponseToServer(
                status = Constants.CODE_TIMEOUT_SCAN
            )
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            Log.i(TAG, "onScanResult: ${result?.device?.name}")

            if (result?.device?.name == DeviceData.DEVICE_NAME) {
                Log.i("Device found", "${result?.device}")
                result?.device?.connectGatt(null, false, mGattCallback)
                isScanning = false
                bluetoothLeScanner!!.stopScan(this)
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        /** The following callbacks are in order, each fun execute the next one  **/

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

            Log.i("Services discovered", Thread.currentThread().name)

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

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            var charArray: Array<Int> = emptyArray()

            for (char in characteristic.value) {
                charArray += char.toUByte().toInt()
            }

            readCharacteristics(charArray)
        }

        @SuppressLint("MissingPermission")
        private fun readCharacteristics(characteristic: Array<Int>) {
            executeAction {
                var statusResponse = -1

                if (characteristic[0] == 85) {
                    when (characteristic[1]) {
                        /** All responses start with 48 except SetCard **/
                        48 -> {
                            val rndNumber = characteristic[2]
                            val devicePower = characteristic[3]

                            Log.i(TAG, "rndNumber: $rndNumber, battery: $devicePower")
                            Interactor.getToken(devicePower.toString(), rndNumber.toString())
                            return@executeAction
                        }
                        /** SetCard Response **/
                        49 -> {
                            if (characteristic[2] != 1) Log.i(TAG, "ERROR")
                        }
                    }
                    statusResponse = characteristic[2]
                } else if (characteristic[0] == 165) {
                    statusResponse = characteristic[3]
                }

                Log.i("Status Response", "$statusResponse")
                Interactor.sendResponseToServer(
                    Constants.CODE_MSG_OK,
                    statusMOne = statusResponse
                )
                gattTmp.close()
            }
        }
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

            Interactor.sendResponseToServer(status = Constants.CODE_MSG_KO)

            gattTmp.close()
        }
    }

    private fun writeChar(gatt: BluetoothGatt) {
        val dataIn = HexUtil.hexStringToBytes(mCode)
        mDataQueue = HexUtil.splitByte(dataIn!!, Constants.MAX_SEND_DATA)
        Log.i(TAG, "SIZE: ${mDataQueue.size}")
        writeDataDevice(gatt)
    }

    @SuppressLint("MissingPermission")
    private fun writeDataDevice(gatt: BluetoothGatt) {
        var counter = 1
        while (mDataQueue.peek() != null) {
            if (counter > 1) break
            val data = mDataQueue.poll()

            characteristicWrite = gatt.getService(SERVICE_UUID).getCharacteristic(WRITE_CHARACTER)
            characteristicWrite!!.value = data
            Log.i(TAG, "Sending: ${HexUtil.formatHexString(characteristicWrite!!.value, true)}")
            characteristicWrite!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.writeCharacteristic(characteristicWrite)
            counter++
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter!!.isEnabled
    }

    //Coroutines
    private fun executeAction(block: suspend () -> Unit): Job {
        return MainScope().launch(Dispatchers.IO) {
            block()
        }
    }
}