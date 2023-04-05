package com.mch.blekot

import android.util.Log
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.view.WindowManager
import android.content.IntentFilter
import android.Manifest.permission.*
import com.mch.blekot.common.Constants
import android.content.BroadcastReceiver
import com.mch.blekot.services.SocketService
import androidx.appcompat.app.AppCompatActivity
import com.vmadalin.easypermissions.EasyPermissions
import com.mch.blekot.databinding.ActivityMainBinding
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private val ACTION_RUN_SERVICE = "com.mch.blekot.services.action.RUN_SERVICE"
    private val ACTION_MEMORY_EXIT = "com.mch.blekot.services.action.MEMORY_EXIT"

    private lateinit var mBinding: ActivityMainBinding

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        launchSocketService()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        methodRequiresTwoPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    private fun methodRequiresTwoPermission() {
        if (!EasyPermissions.hasPermissions(
                this,
                ACCESS_FINE_LOCATION,
                RECORD_AUDIO,
                WRITE_EXTERNAL_STORAGE
            )
        ) {
            val CODE_REQUEST_PERMISSIONS = 1
            EasyPermissions.requestPermissions(
                host = this,
                rationale = getString(R.string.ACCEPT_PERMISIONS),
                requestCode = CODE_REQUEST_PERMISSIONS,
                perms = arrayOf(ACCESS_FINE_LOCATION, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE)
            )
        }
    }

    init {
        instance = this
    }

//    private fun launchMicroService(){
//        val intent = Intent(applicationContext, MicroService::class.java)
//        startService(intent)
//    }

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


    companion object {
        private var instance: MainActivity? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

}