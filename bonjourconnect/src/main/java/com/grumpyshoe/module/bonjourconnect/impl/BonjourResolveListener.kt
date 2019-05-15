package com.grumpyshoe.module.bonjourconnect.impl

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.grumpyshoe.module.bonjourconnect.BonjourConnect
import com.grumpyshoe.module.bonjourconnect.models.NetworkService
import java.net.InetAddress

/**
 * Created by thomas on 2019-05-08.
 *
 */
class BonjourResolveListener(val onSuccess: (NetworkService) -> Unit, val onError: (BonjourConnect.ErrorType) -> Unit) :
    NsdManager.ResolveListener {

    val TAG = "BonjourResolveListener"

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {

        Log.e(TAG, "Resolve failed: $errorCode")
        onError(BonjourConnect.ErrorType.RESOLVE_ERROR)
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {

        Log.i(TAG, "Resolve Succeeded. $serviceInfo")

        val port: Int = serviceInfo.port
        val host: InetAddress = serviceInfo.host

        Log.d(TAG, "Resolved Service infos: port:$port; host:$host")

        onSuccess.invoke(
            NetworkService(
                serviceInfo.serviceName,
                serviceInfo.serviceType,
                host.hostAddress,
                port
            )
        )
    }
}