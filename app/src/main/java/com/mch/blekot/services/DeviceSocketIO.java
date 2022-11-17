package com.mch.blekot.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mch.blekot.Ble;
import com.mch.blekot.MainActivity;
import com.mch.blekot.util.Constants;
import com.mch.blekot.util.ProcessDataJson;

import java.util.Objects;
import java.util.TimerTask;

// Librerias para el sockect IO
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public final class DeviceSocketIO extends Service {
    private static final String TAG = DeviceSocketIO.class.getSimpleName();

    // SocketIO
    private IO.Options options = new IO.Options();
    private Socket socket = null;

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

        this.options.reconnection = true;
        socket = IO.socket(URI.create(Constants.URL_TCP), options);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Conectado!!");
                socket.emit(Constants.ACTION_LOG, Constants.ID, Constants.MESSAGE);
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> System.out.println("connect_error: " + args[0]));

        socket.on(Constants.ACTION_ADMIN, args -> {
            JSONArray dataResponse;
            try {
                dataResponse = new JSONArray(args);
                JSONObject dataJson = new JSONObject(dataResponse.get(1).toString());

                // Obtener dataJSON en un HashMap
                ProcessDataJson pDataJson = new ProcessDataJson();
                pDataJson.getData(dataJson);
                String action = (Objects.requireNonNull(pDataJson.getValue("cmd"))).toString();
                Ble ble = new Ble(getApplicationContext());
                switch (action) {
                    case Constants.ACTION_OPEN_LOCK:
                        ble.startBle("5530", action, "");
                        break;

                    case Constants.ACTION_NEW_CODE:
                        String code = (Objects.requireNonNull(pDataJson.getValue("code"))).toString();
                        ble.startBle("5530", action, code);
                        break;

                    case Constants.ACTION_SET_CARD:
                        String qr = (Objects.requireNonNull(pDataJson.getValue("qr"))).toString();
                        String type = (Objects.requireNonNull(pDataJson.getValue("type"))).toString();
                        ble.startBle("5530", action, "");
                        break;
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> System.out.println("disconnect due to: " + args[0]));

        socket.connect();

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