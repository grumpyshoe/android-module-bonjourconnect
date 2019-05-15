package com.grumpyshoe.module.bonjourconnect.impl

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.grumpyshoe.module.bonjourconnect.BonjourConnect

/**
 * Created by thomas on 2019-05-08.
 *
 */
class BonjourDiscoveryListener(
    val onServiceAvailable: (NsdServiceInfo) -> Unit,
    val onError: (BonjourConnect.ErrorType) -> Unit
) : NsdManager.DiscoveryListener {

    val TAG = "DiscoveryListener"

    // Called as soon as service discovery begins.
    override fun onDiscoveryStarted(regType: String) {
        Log.d(TAG, "onDiscoveryStarted")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        // A service was found! Do something with it.
        Log.d(TAG, "onServiceFound $service")
        onServiceAvailable(service)
    }

    override fun onServiceLost(service: NsdServiceInfo) {
        Log.e(TAG, "onServiceLost: $service")
        onError(BonjourConnect.ErrorType.SERVICE_LOST)
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Log.i(TAG, "onDiscoveryStopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(TAG, "onStartDiscoveryFailed Error code:$errorCode")
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(TAG, "onStopDiscoveryFailed Error code:$errorCode")
    }
}