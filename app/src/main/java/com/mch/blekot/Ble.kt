package com.mch.blekot

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.mch.blekot.util.Constants
import java.lang.Thread.sleep
import java.util.*

class Ble(private val mContext: Context) {

    private lateinit var mCode: String
    private lateinit var mAction: String
    private lateinit var mNewCode: String

    private val TAG = "Main Activity"

    //private val MAC_ADRESS = "C7:12:48:82:08:2F"
    private val MAC_ADRESS = "D6:F5:3B:E4:6D:F5"
    private val SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val NOTIFY_CHARACTERISTIC = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    private val WRITE_CHARACTER = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var characteristicNotify: BluetoothGattCharacteristic? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null

    /*para dividir en paquetes*/
    private val maxPackageSize = 20
    private lateinit var pack: Array<ByteArray>
    private var packAmount = 0

    /*--------------------------BLE--------------------------*/

    fun startBle(code: String, action: String? = "", newCode: String? = null) {

        //Tomamos el codigo y lo guardamos en una variable global
        mCode = code
        if (newCode != null) {
            mNewCode = newCode
        }
        mAction = action!!

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = btAdapter.getRemoteDevice(MAC_ADRESS)
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) !=
            PackageManager.PERMISSION_GRANTED
        ) {
        }
        device.connectGatt(mContext, true, mGattCallback)
    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        /*-----------------------1º-----------------------*/
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                }
                gatt.discoverServices()
                Log.i(TAG, "onConnectionStateChange: Discover Services")
            }
        }

        /*-----------------------2º-----------------------*/
        /*Nos suscribimos a las notificaciones y escribimos el descriptor*/

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            characteristicNotify =
                gatt.getService(SERVICE_UUID).getCharacteristic(NOTIFY_CHARACTERISTIC)
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
            }
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
                sendBLE(gatt)
            } else Log.i(TAG, "onDescriptorWrite: Descriptor is not connected")
        }

        fun String.decodeHex(): ByteArray {
            check(length % 2 == 0)
            return chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }

        /*-----------------------4ª-----------------------*/

        // Aqui escribimos las caracteristicas

        private fun sendBLE(gatt: BluetoothGatt) {

            characteristicWrite = gatt.getService(SERVICE_UUID).getCharacteristic(WRITE_CHARACTER)
            val codeHex = mCode.decodeHex()
            val maxByteArraySize = codeHex.size
            val plusOne = if ((maxByteArraySize % 20) > 0) 1 else 0
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            }

            packAmount = maxByteArraySize / maxPackageSize + plusOne
            pack = emptyArray()
                for (j in 1..packAmount) {
                pack += if (maxByteArraySize - ((j - 1) * maxPackageSize) >= maxPackageSize) {
                    arrayOf(
                        codeHex.copyOfRange(
                            maxPackageSize * (j - 1),
                            maxPackageSize * j
                        )
                    )
                } else {
                    arrayOf(codeHex.copyOfRange(maxPackageSize * (j - 1), maxByteArraySize))
                }

            }
            for (i in 0 until packAmount){
                characteristicWrite!!.value = pack[i]
                characteristicWrite!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                gatt.writeCharacteristic(characteristicWrite)
                Log.i(TAG, "charWrite: ${characteristicWrite!!.value.toHexString()}")
                sleep(500)
            }
        }

        @ExperimentalUnsignedTypes
        fun ByteArray.toHexString() =
            asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

        /*-----------------------5º-----------------------*/

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
//            if (counter<packAmount) {
//                if (ActivityCompat.checkSelfPermission(
//                        mContext,
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                }
//                sleep(500)
//                characteristicWrite!!.value = pack[counter]
//                Log.i("PACK", pack[counter].toString())
//                characteristicWrite!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//                gatt.writeCharacteristic(characteristicWrite)
//                counter++
//              }
            if (characteristicWrite === characteristic) {
                Log.i("Char Writed", "Char: ${characteristic.value.toHexString()}")
            } else Log.i(TAG, "ERROR: Write is not ok")
        }

        /*-----------------------6ª-----------------------*/
        /* Al recibir la respuesta de la manija lanzamos la request http*/

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.value[0].toInt() == 85 && characteristic.value[1].toInt() == 48) {
                val rndNumber = characteristic.value[2].toUByte().toInt()
                val devicePower = characteristic.value[3].toUByte().toInt()
                val myJason = "{\"rndNumber\":$rndNumber, \"battery\":$devicePower}"
                Log.i(TAG, "onCharacteristicChanged: $myJason")


                when (mAction) {
                    Constants.ACTION_OPEN_LOCK ->
                        WeLock("$rndNumber", "$devicePower", mAction, this@Ble)
                            .getToken()

                    Constants.ACTION_NEW_CODE ->
                        WeLock("$rndNumber", "$devicePower", mAction, this@Ble)
                            .getToken(code = mNewCode)

                    Constants.ACTION_SET_CARD ->
                        WeLock("$rndNumber", "$devicePower", mAction, this@Ble)
                            .getToken()
                }
            } else if (characteristic.value[0].toInt() == 85 &&
                characteristic.value[1].toInt() == 49
            ) {
                if (characteristic.value[2].toInt() == 1)
                    Log.i(TAG, "RECIBIDO CORRECTAMENTE")
                else Log.i(TAG, "ERROR")

            }
            Log.i(TAG, "onCharacteristicChanged: Received")
            if (ActivityCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            }
            gatt.disconnect()
            gatt.close()
        }

    }

}