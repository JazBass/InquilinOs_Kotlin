package com.mch.blekot.io.socket.welock

interface WeLockAux {

    fun openLock(){}

    fun setNewCode(newPassword: String){}

    fun setNewCard(qr: String, type: String){}

}