package com.mch.blekot.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rdajila.tandroidsocketio.util.Constants;
import com.rdajila.tandroidsocketio.util.ProcessDataJson;

import java.util.Timer;
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
    private static int counter = 0;
    private static final String TAG = DeviceSocketIO.class.getSimpleName();
    TimerTask timerTask;

    // SocketIO
    private IO.Options options = new IO.Options();
    private Socket socket = null;

    public DeviceSocketIO() {
        /*this.options.reconnection = true;
        socket = IO.socket(URI.create(Constants.URL_TCP), options);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Conectado!!");
                socket.emit(Constants.ACTION_LOG, Constants.ID, Constants.MESSAGE);
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect_error: " + args[0]);
            }
        });

        socket.on(Constants.ACTION_ADMIN, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray dataResponse;
                try {
                    dataResponse = new JSONArray(args);
                    JSONObject dataJson = new JSONObject(dataResponse.get(1).toString());

                    // Obtener dataJSON en un HashMap
                    ProcessDataJson pDataJson = new ProcessDataJson();
                    pDataJson.getData(dataJson);
                    //System.out.print(pDataJson);

                    final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                    final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

                    activityManager.getMemoryInfo(memoryInfo);
                    String availMem = memoryInfo.availMem / 1048576 + "MB";

                    Log.d(TAG, availMem);

                    Intent localIntent = new Intent(Constants.ACTION_RUN_SERVICE)
                            .putExtra(Constants.EXTRA_MEMORY, availMem)
                            .putExtra(Constants.EXTRA_COUNTER, String.valueOf(DeviceSocketIO.counter++));

                    // Emitir el intent a la actividad
                    LocalBroadcastManager.getInstance(DeviceSocketIO.this).sendBroadcast(localIntent);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("disconnect due to: " + args[0]);
            }
        });
        */
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

        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect_error: " + args[0]);
            }
        });

        socket.on(Constants.ACTION_ADMIN, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONArray dataResponse;
                try {
                    dataResponse = new JSONArray(args);
                    JSONObject dataJson = new JSONObject(dataResponse.get(1).toString());

                    // Obtener dataJSON en un HashMap
                    ProcessDataJson pDataJson = new ProcessDataJson();
                    pDataJson.getData(dataJson);
                    //System.out.print(pDataJson);

                    final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                    final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

                    activityManager.getMemoryInfo(memoryInfo);
                    // String availMem = memoryInfo.availMem / 1048576 + "MB";
                    String msg = pDataJson.toString();

                    Log.d(TAG, msg);

                    Intent localIntent = new Intent(Constants.ACTION_RUN_SERVICE)
                            .putExtra(Constants.EXTRA_MSG, msg)
                            .putExtra(Constants.EXTRA_COUNTER, String.valueOf(DeviceSocketIO.counter++));

                    // Emitir el intent a la actividad
                    LocalBroadcastManager.getInstance(DeviceSocketIO.this).sendBroadcast(localIntent);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("disconnect due to: " + args[0]);
            }
        });

        socket.connect();

        /*final String ACTION_RUN_SERVICE = "com.rdajila.tandroidsocketio.services.action.RUN_SERVICE";
        final String EXTRA_MEMORY = "com.rdajila.tandroidsocketio.services.extra.MEMORY";
        final String COUNTER = "com.rdajila.tandroidsocketio.services.extra.COUNT";

        final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        Timer timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                activityManager.getMemoryInfo(memoryInfo);
                String availMem = memoryInfo.availMem / 1048576 + "MB";

                Log.d(TAG, availMem);

                Intent localIntent = new Intent(Constants.ACTION_RUN_SERVICE)
                        .putExtra(Constants.EXTRA_MEMORY, availMem)
                        .putExtra(Constants.COUNTER, String.valueOf(DeviceSocketIO.counter++));

                // Emitir el intent a la actividad
                LocalBroadcastManager.getInstance(DeviceSocketIO.this).sendBroadcast(localIntent);
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 5000);*/

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