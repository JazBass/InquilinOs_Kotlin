package com.mch.blekot.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.mch.blekot.io.socket.SocketSingleton
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SocketService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.d(TAG, "Servicio creado...")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio iniciado...")

        //Init es para instanciar el socket en el singleton, solo lo pasamos aqui
        SocketSingleton.socketInstance!!
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        //timerTask.cancel();
        val ACTION_MEMORY_EXIT = "com.rdajila.tandroidsocketio.services.action.MEMORY_EXIT"
        val localIntent = Intent(ACTION_MEMORY_EXIT)

        // Emitir el intent a la actividad
        LocalBroadcastManager.getInstance(this@SocketService).sendBroadcast(localIntent)
        Log.d(TAG, "Servicio destruido...")
    }

    companion object {
        private val TAG = SocketService::class.java.simpleName
    }
}