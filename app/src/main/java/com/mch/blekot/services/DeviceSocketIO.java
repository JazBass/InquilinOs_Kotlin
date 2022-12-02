package com.mch.blekot.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public final class DeviceSocketIO extends Service {

    private static final String TAG = DeviceSocketIO.class.getSimpleName();


    public DeviceSocketIO() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Servicio creado...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Servicio iniciado...");

        //Init es para instanciar el socket en el singleton, solo lo pasamos aqui
        SocketSingleton.getSocketInstance().init(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //timerTask.cancel();
        final String ACTION_MEMORY_EXIT = "com.rdajila.tandroidsocketio.services.action.MEMORY_EXIT";
        Intent localIntent = new Intent(ACTION_MEMORY_EXIT);

        // Emitir el intent a la actividad
        LocalBroadcastManager.getInstance(DeviceSocketIO.this).sendBroadcast(localIntent);

        Log.d(TAG, "Servicio destruido...");
    }
}