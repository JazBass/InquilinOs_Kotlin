package com.mch.blekot.io.ble

interface ActionManagerAux {

    fun openLock()

    fun setNewCode(newPassword: String, days: Int)

    fun setNewCard(qr: String, type: String)

    fun syncTime(newTime: String)

    fun getRecord()

    fun getToken(battery: String, rdmNumber: String)

    fun getDevicesBatteries()

    fun openPortal()

    fun launchNotification()
}