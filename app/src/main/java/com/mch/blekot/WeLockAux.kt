package com.mch.blekot

interface WeLockAux {

    fun openLock(){}

    fun setNewCode(newPassword: String){}

    fun setNewCard(qr: String, type: String){}

}