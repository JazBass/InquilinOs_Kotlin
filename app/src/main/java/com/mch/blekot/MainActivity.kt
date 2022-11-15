package com.mch.blekot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mch.blekot.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), BleAux {
    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.btnSendBle.setOnClickListener {
            sendBle("5530")
        }
    }

        /*---------------------write char---------------------*/

    override fun sendBle(code: String) {

        //Instanciamos la clase pasandole el contexto
        Ble(applicationContext).startBle(code)
    }

}