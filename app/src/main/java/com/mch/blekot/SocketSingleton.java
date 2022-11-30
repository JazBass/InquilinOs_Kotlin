package com.mch.blekot;

import android.content.Context;

public class SocketSingleton {

    private static SocketSingleton socketInstance;
    private static Context mContext;

    private SocketSingleton(Context context){
        mContext = context;
    }

    public static synchronized SocketSingleton getSocketInstance(Context context) {
        if (socketInstance!=null){
            socketInstance = new SocketSingleton(context);
        }
        return socketInstance;
    }
}
