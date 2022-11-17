package com.mch.blekot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mch.blekot.databinding.ActivityMainBinding
import com.mch.blekot.services.DeviceSocketIO
import com.mch.blekot.util.Constants


class MainActivity : AppCompatActivity() {

    val ACTION_RUN_SERVICE = "com.mch.blekot.services.action.RUN_SERVICE"
    val ACTION_MEMORY_EXIT = "com.mch.blekot.services.action.MEMORY_EXIT"


    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val filter = IntentFilter(ACTION_RUN_SERVICE)
        filter.addAction(ACTION_MEMORY_EXIT)

        // Crear un nuevo ResponseReceiver
        val receiver = ResponseReceiver()

        // Registrar el receiver y su filtro
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        // Iniciar el servicio
        val intentDeviceSocketIO = Intent(applicationContext, DeviceSocketIO::class.java)
        startService(intentDeviceSocketIO)
    }


        /*---------------------write char---------------------*/

    private class ResponseReceiver : BroadcastReceiver() {

        // Filtro de acciones que serán alertadas
        val ACTION_RUN_SERVICE = "com.rdajila.tandroidsocketio.services.action.RUN_SERVICE"
        val ACTION_MEMORY_EXIT = "com.rdajila.tandroidsocketio.services.action.MEMORY_EXIT"
        val COUNTER = "com.rdajila.tandroidsocketio.services.extra.COUNT"
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                ACTION_RUN_SERVICE -> {

                    Log.d("TAG", "Servicio iniciado escucha desde MainActivity...")
                    Log.d("TAG", "Main-MSG: " + intent.getStringExtra(Constants.EXTRA_MSG))
                    Log.d("TAG", "Main-Counter: " + intent.getStringExtra(Constants.EXTRA_COUNTER))
                }
                ACTION_MEMORY_EXIT -> {
                    // Guardar info en base de datos que el servicio ha sido destruido
                    Log.d("TAG", "Servicio finalizado escucha desde MainActivity...")
                }
            }
        }
    }
}