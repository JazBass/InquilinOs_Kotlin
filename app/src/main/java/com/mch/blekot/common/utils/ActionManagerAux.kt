package com.mch.blekot.common.utils

interface ActionManagerAux {

    suspend fun openLock()

    suspend fun setNewCode(newPassword: String, days: Int)

    suspend fun setNewCard(qr: String, type: String)

    suspend fun syncTime(newTime: String)

    suspend fun getRecord()

    suspend fun getToken(battery: String, rdmNumber: String)

    suspend fun  getDevicesBatteries()

    suspend fun openPortal()

}