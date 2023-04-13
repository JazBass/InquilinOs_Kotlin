package com.mch.blekot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mch.blekot.common.Constants
import com.mch.blekot.databinding.FragmentInfoBinding
import com.mch.blekot.model.socket.SocketSingleton

class InfoFragment : Fragment(R.layout.fragment_info) {

    private lateinit var mBinding: FragmentInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = FragmentInfoBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.IDName.text = Constants.ID
        mBinding.isConnected.text = if (SocketSingleton.socketInstance!!.isConnected) "CONECTADO a ${Constants.URL_TCP}" else "DESCONECTADO"
    }

}