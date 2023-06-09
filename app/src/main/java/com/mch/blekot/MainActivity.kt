package com.mch.blekot

import android.util.Log
import android.view.View
import android.os.Bundle
import kotlinx.coroutines.Job
import android.content.Intent
import android.content.Context
import android.widget.EditText
import kotlinx.coroutines.launch
import android.view.WindowManager
import android.content.IntentFilter
import kotlinx.coroutines.MainScope
import android.Manifest.permission.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import com.mch.blekot.model.Interactor
import com.mch.blekot.common.Constants
import android.content.BroadcastReceiver
import androidx.appcompat.app.AlertDialog
import com.mch.blekot.services.SocketService
import androidx.datastore.preferences.core.edit
import androidx.appcompat.app.AppCompatActivity
import com.vmadalin.easypermissions.EasyPermissions
import com.mch.blekot.databinding.ActivityMainBinding
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private val ACTION_RUN_SERVICE = "com.mch.blekot.services.action.RUN_SERVICE"
    private val ACTION_MEMORY_EXIT = "com.mch.blekot.services.action.MEMORY_EXIT"

    //Singleton
    private val Context.dataStore by preferencesDataStore(name = "DEVICE_ID")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        //Para la pantalla esté siempre encendida
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //Pedimos permisos
        methodRequiresTwoPermission()
        //onCLickListeners
        setUpListeners()
        askForDeviceID()
    }

    private fun askForDeviceID() {
        executeAction{
            if(!readDeviceID().isNullOrEmpty()){
                Constants.ID = readDeviceID().toString()
                launchSocketService()
            }else{
                launchAlertDialog()
            }
        }
    }

    private fun launchAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Insterte el ID del dispositivo")
        val editText = EditText(this)
        builder.setView(editText)
        builder.setPositiveButton("OK") { dialog, _ ->
            val deviceId = editText.text.toString()
            executeAction {
                saveDeviceID(deviceId)
            }
        }
        builder.show()
    }

    private fun setUpListeners() {
        with(mBinding){
            fab.setOnClickListener { launchInfoFragment() }
            cancelFab.setOnClickListener { onBackPressed() }
            btnLaunchScan.setOnClickListener {
                MainScope().launch { Interactor.openLock() }
            }
        }
    } 

    private suspend fun saveDeviceID(id: String){
        val deviceIdKey = stringPreferencesKey(Constants.DEVICE_ID_KEY)
        dataStore.edit { preferences ->
            preferences[deviceIdKey] = id
        }
        readDeviceID().also {
            if (!it.isNullOrEmpty()){
                Constants.ID = it
                Log.i("DeviceID", it)
                launchSocketService()
            }
        }
    }

    private suspend fun readDeviceID(): String?{
        val deviceIdKey = stringPreferencesKey(Constants.DEVICE_ID_KEY)
        dataStore.data.first().also { preferences ->
            return preferences[deviceIdKey]
        }
    }

    private val fragment = InfoFragment()

    private fun launchInfoFragment() {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        with(fragmentTransaction) {
            add(R.id.containerMain, fragment, "currentFragment")
            addToBackStack(null)
            setTransition(TRANSIT_FRAGMENT_OPEN)
            commit()
        }
        mBinding.fab.visibility = View.GONE
        mBinding.cancelFab.visibility = View.VISIBLE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        with(mBinding){
            cancelFab.visibility = View.GONE
            fab.visibility = View.VISIBLE
        }
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
                rationale = getString(R.string.ACCEPT_PERMISSIONS),
                requestCode = CODE_REQUEST_PERMISSIONS,
                perms = arrayOf(ACCESS_FINE_LOCATION, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE)
            )
        }
    }

    init {
        instance = this
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

    //Coroutines
    private fun executeAction(block: suspend () -> Unit): Job {
        return MainScope().launch(Dispatchers.Main) {
            block()
        }
    }

    companion object {
        private var instance: MainActivity? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

}