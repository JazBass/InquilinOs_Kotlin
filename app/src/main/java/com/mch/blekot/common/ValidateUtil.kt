package com.mch.blekot.common

import android.util.Log
import com.mch.blekot.model.DeviceData

object ValidateUtil {

    data class ValidateResponse(val result: Boolean, val msg: String)

    private val DEVICE_SIZE = 8
    private val DEVICE_NAME = 11
    private val macRegex = Regex("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}")
    private val urlRegex = Regex("http://192.168.[0-9]{1,3}.[0-9]{1,3}/")

    private fun String.isMac() = this.matches(macRegex)
    private fun String.isUrl() = this.matches(urlRegex)

    private lateinit var response: String

    fun getResponse() = response

    @Throws(ValidateException::class)
    fun setUpBle (
        macAddress: String,
        deviceName: String,
        deviceId: String
    ) {
        val resMacAddress = validateMacAddress(macAddress)
        val resDeviceId = validateId(deviceId)
        val resDeviceName = validateDeviceName(deviceName)

        if (resMacAddress.result and resDeviceId.result and resDeviceName.result) {
           DeviceData.MAC_ADDRESS = macAddress
           DeviceData.DEVICE_ID_NUMBER = deviceId
           DeviceData.DEVICE_NAME = deviceName
        } else {
            val status = -1
            response = JsonManager.getCredentialsResponse(
                status,
                resDeviceId.msg,
                resDeviceName.msg,
                resMacAddress.msg,
                null
            )
            Log.i("VALIDATE", "EXCEPTION")
             throw(ValidateException())
        }
    }

    @Throws(ValidateException::class)
    fun setUpArduino(ipArduino: String){
        val resUrlArduino = validateUrl(ipArduino)
        if (resUrlArduino.result) DeviceData.IP_ARDUINO = ipArduino
        else{
            val status = -1
            response = JsonManager.getCredentialsResponse(
                status,
                null,
                null,
                null,
                resUrlArduino.msg
            )
            Log.i("VALIDATE", "EXCEPTION")
            throw(ValidateException())
        }
    }

    private fun validateMacAddress(macAddress: String): ValidateResponse {
        return if (macAddress.isMac())
            ValidateResponse(true, macAddress)
        else
            ValidateResponse(false, Constants.ERROR_MAC_ADDRESS)
    }

    private fun validateId(deviceId: String): ValidateResponse {
        return if (deviceId.length == DEVICE_SIZE) {
            if (deviceId.toDoubleOrNull() != null)
                ValidateResponse(true, deviceId)
            else
                ValidateResponse(false, Constants.ERROR_ONLY_NUMBERS)
        } else {
            ValidateResponse(false, Constants.ERROR_LENGTH)
        }
    }

    private fun validateDeviceName(deviceName: String): ValidateResponse {
        return if (deviceName.length == DEVICE_NAME)
            ValidateResponse(true, deviceName)
        else
            ValidateResponse(false, Constants.ERROR_LENGTH)
    }

    private fun validateUrl(url: String): ValidateResponse {
        return if (url.isUrl())
            ValidateResponse(true, url)
        else
            ValidateResponse(false, Constants.ERROR_ARDUINO)
    }

}