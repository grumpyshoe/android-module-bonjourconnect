package com.grumpyshoe.module.bonjourconnect.impl

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.grumpyshoe.module.bonjourconnect.BonjourConnect
import com.grumpyshoe.module.bonjourconnect.models.NetworkService
import org.jetbrains.anko.doAsync
import java.net.InetAddress

class BonjourConnectImpl(context: Context) : BonjourConnect {

    var serviceType: String? = null
    val nsdManager: NsdManager = context.getSystemService(AppCompatActivity.NSD_SERVICE) as NsdManager
    var onServiceInfoReceived: ((networkService: NetworkService) -> Unit)? = null
    var searchTimeout: Long? = null
    var startTime: Long? = null
    var serviceFound = false
    var running = false
    lateinit var onError: (BonjourConnect.ErrorType) -> Unit

    /**
     * get infos for a given service
     *
     * @param type - service type (e.g. '_my_service._tcp.')
     * @param searchTimeout - time estimated to find service (default : 3 Sec.)
     * @param onServiceInfoReceived - function that is called if service has been found
     * @param onError - function that is called on any error
     */
    override fun getServiceInfo(
        type: String,
        searchTimeout: Long,
        onServiceInfoReceived: (NetworkService) -> Unit,
        onError: (BonjourConnect.ErrorType) -> Unit
    ) {

        if (!type.isEmpty()) {
            if (!running) {
                serviceFound = false
                running = true
                startTime = System.currentTimeMillis()
                serviceType = type
                this.searchTimeout = searchTimeout
                this.onError = onError
                this.onServiceInfoReceived = onServiceInfoReceived

                Log.d(TAG, "start discovery for $serviceType")

                nsdManager.discoverServices(
                    serviceType,
                    NsdManager.PROTOCOL_DNS_SD,
                    MyDiscoveryListener(type, startTime!!, searchTimeout, nsdManager, resolveListener, onError) {
                        running = it
                    })

                doAsync {
                    Thread.sleep(searchTimeout)
                    if (!serviceFound) {
                        onError(BonjourConnect.ErrorType.TIMEOUT)
                        running = false
                    }
                }
            }
        } else {
            onError(BonjourConnect.ErrorType.UNKNOWN)
            running = false
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
            running = false
            onError(BonjourConnect.ErrorType.RESOLVE_ERROR)
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve Succeeded. $serviceInfo")

            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host

            Log.d(TAG, "Resolved Service infos: port:$port; host:$host")

            onServiceInfoReceived?.invoke(
                NetworkService(
                    serviceInfo.serviceName,
                    serviceInfo.serviceType,
                    host.hostAddress,
                    port
                )
            )

            doAsync {
                Thread.sleep(2000L)

                serviceFound = true
                running = false
            }

        }
    }

    private class MyDiscoveryListener(
        val serviceType: String,
        val startTime: Long,
        val searchTimeout: Long,
        val nsdManager: NsdManager,
        val resolveListener: NsdManager.ResolveListener,
        val onError: (BonjourConnect.ErrorType) -> Unit,
        val onRunningStateChanged: (Boolean) -> Unit
    ) : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "onDiscoveryStarted")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "onServiceFound $service")
            if (service.serviceType == serviceType && (System.currentTimeMillis() - startTime) < searchTimeout) {
                try {
                    nsdManager.resolveService(service, resolveListener)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "resolveService errory: ${e.message}")
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e(TAG, "onServiceLost: $service")
            onError(BonjourConnect.ErrorType.SERVICE_LOST)
            onRunningStateChanged(false)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "onDiscoveryStopped: $serviceType")
            onRunningStateChanged(false)
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "onStartDiscoveryFailed Error code:$errorCode")
            onRunningStateChanged(false)
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: IllegalArgumentException) {
                Log.i(TAG, "onStartDiscoveryFailed: ${e.message}")
            }
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "onStopDiscoveryFailed Error code:$errorCode")
            onRunningStateChanged(false)
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: IllegalArgumentException) {
                Log.i(TAG, "onStopDiscoveryFailed: ${e.message}")
            }
            onError(BonjourConnect.ErrorType.DISCOVERY_ERROR)
        }
    }

    companion object {
        private const val TAG = "BonjourConnect"
    }
}