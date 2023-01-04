package com.mch.blekot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mch.blekot.databinding.ActivityMainBinding
import com.mch.blekot.io.socket.welock.WeLock
import com.mch.blekot.services.MicroService
import com.mch.blekot.services.SocketService
import com.mch.blekot.services.SocketSingleton
import com.mch.blekot.util.Constants


class MainActivity : AppCompatActivity() {

    private val ACTION_RUN_SERVICE = "com.mch.blekot.services.action.RUN_SERVICE"
    private val ACTION_MEMORY_EXIT = "com.mch.blekot.services.action.MEMORY_EXIT"

    private lateinit var mBinding: ActivityMainBinding

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnOpenLock.setOnClickListener { WeLock.openLock() }

        //launchSocketService()

        //launchMicroService()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun launchMicroService(){
        val intent = Intent(applicationContext, MicroService::class.java)
        startService(intent)
    }

    private fun launchSocketService() {
        val filter = IntentFilter(ACTION_RUN_SERVICE)
        filter.addAction(ACTION_MEMORY_EXIT)

        // Crear un nuevo ResponseReceiver
        val receiver = ResponseReceiver()

        // Registrar el receiver y su filtro
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        // Iniciar el servicio
        val intent = Intent(applicationContext, SocketService::class.java)
        startService(intent)
    }


    /*---------------------write char---------------------*/

    private class ResponseReceiver : BroadcastReceiver() {

        // Filtro de acciones que serán alertadas
        val ACTION_RUN_SERVICE = "com.rdajila.tandroidsocketio.services.action.RUN_SERVICE"
        val ACTION_MEMORY_EXIT = "com.rdajila.tandroidsocketio.services.action.MEMORY_EXIT"
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