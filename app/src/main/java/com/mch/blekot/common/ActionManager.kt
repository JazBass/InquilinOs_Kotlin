package com.mch.blekot.common

import okhttp3.Request
import android.util.Log
import java.io.IOException
import okhttp3.OkHttpClient
import com.mch.blekot.model.ble.Ble
import com.mch.blekot.model.welock.WeLock
import com.mch.blekot.common.utils.JsonManager
import com.mch.blekot.model.socket.SocketSingleton
import com.mch.blekot.common.utils.ActionManagerAux
import com.mch.blekot.model.welock.BatteriesManager

object ActionManager : ActionManagerAux {

    private var mAction = -1
    private var mRndNumber: String? = null
    private var mDevicePower: String? = null

    private var mDays: Int = 1
    private var mIndex: Int = -1
    private var mTimes: Int = -1
    private var mQr: String? = null
    private var mType: String? = null
    private var mNewTime: String? = null
    private var mNewPassword: String? = null

    //private const val CHANNEL_ID = "TV"

    private val httpClient = OkHttpClient()

    /**
     * BLE Functions *
     * */

    override suspend fun openLock() {
        mAction = Constants.OPEN_LOCK

        Ble.connectDevice()
    }

    override suspend fun setNewCode(newPassword: String, days: Int, index: Int, times: Int) {

        Log.i("Thread: ", Thread.currentThread().name)

        mAction = Constants.NEW_CODE
        mNewPassword = newPassword
        mDays = days
        mIndex = index
        mTimes  = times

        Ble.connectDevice()
    }

    override suspend fun setNewCard(qr: String, type: String) {
        mAction = Constants.SET_CARD
        mQr = qr
        mType = type

        Ble.connectDevice()
    }

    override suspend fun syncTime(newTime: String) {
        mAction = Constants.SYNC_TIME
        mNewTime = newTime

        Ble.connectDevice()
    }

    override suspend fun getRecord() {
        mAction = Constants.READ_RECORD

        Ble.connectDevice()
    }

    /** WeLock API Functions **/

    override suspend fun getToken(battery: String, rdmNumber: String) {

        Log.i("Thread: ", Thread.currentThread().name)

        mRndNumber = rdmNumber
        mDevicePower = battery
        WeLock.getToken(battery, rdmNumber, mAction)
    }

    /** Extras **/

    override suspend fun getDevicesBatteries() {
        BatteriesManager.getDevicesBatteries()
    }

    override suspend fun openPortal() {
        try {
            val request = Request.Builder()
                .url(Constants.IP_ARDUINO + Constants.PATH_OPEN_PORTAL)
                .get()
                .build()
            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                Log.i("Open Portal", "Response: " + response.body()!!.string())
                sendResponseToServer(
                    status = Constants.STATUS_ARDUINO_OK
                )
            } else throw IOException("Arduino connection fail")

        } catch (e: IOException) {
            e.printStackTrace()
            sendResponseToServer(
                status = Constants.STATUS_ARDUINO_ERROR
            )
        }
    }

    fun setAction(action: Int) {
        mAction = action
    }

    fun getTimes(): Int = mTimes

    fun getIndex(): Int = mIndex

    fun getDeviceNewPassword(): String? = mNewPassword

    fun getQR(): String? = mQr

    fun getType(): String? = mType

    fun getNewTime(): String? = mNewTime

    fun getDays(): Int = mDays


    fun getPostData(devicePower: String, rdmNumber: String): Map<String, String> {
        return JsonManager.getPostData(
            action = mAction,
            devicePower = devicePower,
            rdmNumber = rdmNumber
        )
    }

    /**
     * Server response method
     *  1 -> OK
     *  0 -> PENDIENTE
     * -1 -> ERROR
     * @param status
     **/

    // TODO: Record and battery response are not ok

    fun sendResponseToServer(
        status: Int,
        statusMOne: Int = Constants.STATUS_LOCK,
        statusMTwo: Int = Constants.STATUS_LOCK,
        phoneBattery: Int? = null,
        isCharging: Boolean? = null
    ) {
        val responseJson = JsonManager.getServerResponseJson(
            status = status,
            statusMOne = statusMOne,
            statusMTwo = statusMTwo,
            phoneBattery = phoneBattery,
            deviceBattery = mDevicePower?.toInt(),
            isCharging = isCharging,
            action = mAction
        )

        Log.i("ResponseJson", "$responseJson ")

        SocketSingleton.socketInstance?.emitResponse(
            responseJson
        )
    }
}