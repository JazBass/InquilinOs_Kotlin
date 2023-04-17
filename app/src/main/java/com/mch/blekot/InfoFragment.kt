package com.mch.blekot

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        if (SocketSingleton.socketInstance!!.isConnected){
            mBinding.isConnected.text = getString(R.string.CONNECTED)
            mBinding.isConnected.setTextColor(resources.getColor(R.color.connected_green))
        } else{
            mBinding.isConnected.text = getString(R.string.DISCONNECTED)
            mBinding.isConnected.setTextColor(resources.getColor(R.color.disconnected_red))
        }
    }
}