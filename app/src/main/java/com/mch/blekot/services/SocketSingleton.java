package com.mch.blekot.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.mch.blekot.R;
import com.mch.blekot.io.socket.welock.WeLock;
import com.mch.blekot.model.Interactor;
import com.mch.blekot.util.Constants;
import com.mch.blekot.common.ProcessDataJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SocketSingleton {


    private static final String TAG = SocketService.class.getSimpleName();
    private boolean isProcessActive;

    private static final String CHANNEL_ID = "TV";

    private String clientFromServer = "";
    private final Socket socket;

    private String endTime;
    private String startTime;

    @SuppressLint("StaticFieldLeak")
    private static SocketSingleton mInstance = null;
    final OkHttpClient httpClient = new OkHttpClient();

    private Context context;

    public void init(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }


    //Constructor
    private SocketSingleton() {

        this.isProcessActive = false;
        final IO.Options options = new IO.Options();
        options.reconnection = true;
        socket = IO.socket(URI.create(Constants.URL_TCP), options);

        socket.on(Socket.EVENT_CONNECT, args -> {
            System.out.println("Conectado!!");
            socket.emit(Constants.ACTION_LOG, Constants.ID, Constants.MESSAGE);
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> System.out.println("connect_error: " + args[0]));

        socket.on(Constants.ACTION_ADMIN, args -> {
            JSONArray dataResponse;
            try {
                // procesoActivo: TRUE -> No se ejecuta ninguna accion
                // procesoActivo: FALSE -> Se ejecuta accion nueva
                if ( this.isProcessActive) {
                    Log.i(TAG, "Hay una peticion pendiente!!");
                    Interactor.sendResponseToServer(Constants.CODE_MSG_PENDANT, Constants.STATUS_LOCK, Constants.STATUS_LOCK);
                    return;
                }

                dataResponse = new JSONArray(args);
                JSONObject dataJson = new JSONObject(dataResponse.get(1).toString());

                // Obtener dataJSON en un HashMap
                ProcessDataJson pDataJson = new ProcessDataJson();
                pDataJson.getData(dataJson);
                String action = (Objects.requireNonNull(pDataJson.getValue("cmd"))).toString();
                clientFromServer = (Objects.requireNonNull(pDataJson.getValue("clientFrom"))).toString();

                this.isProcessActive = true;

                switch (action) {

                    case Constants.ACTION_OPEN_LOCK:
                        WeLock.openLock();
                        break;

                    case Constants.ACTION_NEW_CODE:
                        String code = (Objects.requireNonNull(pDataJson.getValue("code"))).toString();
                        int days = Integer.parseInt((Objects.requireNonNull(pDataJson.getValue("days"))).toString());
                        days = (days == 0)? Constants.MIN_DAYS_PASSWORD: days;
                        WeLock.setNewCode(code, days);
                        break;

                    case Constants.ACTION_SET_CARD:
                        String qr = (Objects.requireNonNull(pDataJson.getValue("Qr"))).toString();
                        String type = (Objects.requireNonNull(pDataJson.getValue("type"))).toString();
                        WeLock.setNewCard(qr, type);
                        break;

                    /*Conexión local con arduino*/

                    case Constants.ACTION_OPEN_PORTAL:
                        Log.i(TAG, "Error OPEN PORTAL!!");
                        openPortal();
                        break;

                    case Constants.ACTION_SYNC_TIME:
                        String newTime = (Objects.requireNonNull(pDataJson.getValue("syncTime"))).toString();
                        WeLock.syncTime(newTime);
                        break;

                        /*Lanzamos notificacion para encender la tv con IFTTT*/

                    case "tvOn":

                        launchNotification();

                        break;
                }

            } catch (Exception e) {
                this.isProcessActive = false; //Error por JSON
                e.printStackTrace();
                Interactor.sendResponseToServer(Constants.CODE_MSG_KO, Constants.STATUS_LOCK, Constants.STATUS_LOCK);
            }// Error X-Desconocido

        });

        socket.on(Socket.EVENT_DISCONNECT, args -> System.out.println("disconnect due to: " + args[0]));

        socket.connect();
    }

    public static synchronized SocketSingleton getSocketInstance() {
        if (mInstance == null) {
            mInstance = new SocketSingleton();
        }
        return mInstance;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getClientFromServer() {
        return clientFromServer;
    }

    private void openPortal() {
        try {
            Request request = new Request.Builder()
                    .url("http://192.168.1.150/portal/open")
                    .get()
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.i("Open Portal", "Response: " + response.body().string());

                Interactor.sendResponseToServer(
                        Constants.STATUS_ARDUINO_OK,
                        null,
                        null
                );

            }else throw new IOException("Arduino connection fail");
        } catch (IOException e) {
            e.printStackTrace();
            Interactor.sendResponseToServer(
                    Constants.STATUS_ARDUINO_ERROR,
                    null,
                    null
            );
        }
        // Si hay un error en la peticion OPEN-PORTAL, se permite realizar otra peticion
        this.isProcessActive = false;
    }

    public boolean isProcessActive() {
        return isProcessActive;
    }

    public void setProcessActive(boolean processActive) {
        this.isProcessActive = processActive;
    }
}
