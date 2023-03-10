package com.mch.blekot.io.welock

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.mch.blekot.MainActivity
import com.mch.blekot.io.ble.ActionManager
import com.mch.blekot.io.ble.Ble
import com.mch.blekot.io.socket.SocketSingleton
import com.mch.blekot.util.Constants

object BatteriesManager {

    private var phoneBattery = -1
    private var isCharging = false

    fun getDevicesBatteries() {
        val context = MainActivity.applicationContext()

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            context.registerReceiver(null, iFilter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        phoneBattery = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }!!.toInt()

        Ble.connectDevice(true)

    }

    fun sendResponse(devicePower: Int) {
        val newJson = """{
            phoneBattery: $phoneBattery,
            isCharging: $isCharging,
            lockBattery: $devicePower
        }""".trimIndent()

        Log.i("Battery", newJson)
        ActionManager.sendResponseToServer(
            status = Constants.CODE_MSG_OK,
            phoneBattery = phoneBattery,
            deviceBattery = devicePower,
            isCharging = isCharging
        )
        SocketSingleton.socketInstance!!.isProcessActive = false
        Ble.disconnectGatt()
    }





}