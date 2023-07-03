package com.mch.blekot.model

interface InteractorAux {

    suspend fun openLock()

    suspend fun setNewCode(newPassword: String, days: Int, index:Int, times: Int)

    suspend fun setNewCard(qr: String, type: String)

    suspend fun syncTime(newTime: String)

    suspend fun getRecord()

    suspend fun getToken(battery: String, rdmNumber: String)

    suspend fun openPortal()

}