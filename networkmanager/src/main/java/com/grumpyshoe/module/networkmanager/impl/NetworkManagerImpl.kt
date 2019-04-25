package com.grumpyshoe.module.networkmanager.impl

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.grumpyshoe.module.networkmanager.NetworkManager
import com.grumpyshoe.module.networkmanager.models.NetworkService
import org.jetbrains.anko.doAsync
import java.net.InetAddress

class NetworkManagerImpl(context: Context) : NetworkManager {

    var serviceType: String? = null
    val nsdManager: NsdManager = context.getSystemService(AppCompatActivity.NSD_SERVICE) as NsdManager
    var onServiceFound: ((networkService: NetworkService) -> Unit)? = null
    var searchTimeout: Long? = null
    var startTime: Long? = null
    var serviceFound = false
    var running = false
    lateinit var onError: (NetworkManager.ERROR_TYPE) -> Unit

    /**
     * get infos for a given service
     *
     * @param type - service type (e.g. '_my_service._tcp.')
     * @param searchTimeout - time estimated to find service (default : 3 Sec.)
     * @param onServiceFound - function that is called if service has been found
     * @param onError - function that is called on any error
     */
    override fun getServiceInfo(
        type: String,
        searchTimeout: Long,
        onServiceFound: (NetworkService) -> Unit,
        onError: (NetworkManager.ERROR_TYPE) -> Unit
    ) {

        if (!running && !type.isEmpty()) {
            running = true
            this.searchTimeout = searchTimeout
            serviceType = type
            this.onError = onError
            startTime = System.currentTimeMillis()

            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, MyDiscoveryListener(type, startTime!!, searchTimeout, nsdManager, resolveListener, onError){
                running = it
            })

            doAsync {
                Thread.sleep(searchTimeout)
                if (!serviceFound) {
                    onError(NetworkManager.ERROR_TYPE.TIMEOUT)
                    running = false
                }
            }
        } else {
            onError(NetworkManager.ERROR_TYPE.UNKNOWN)
        }
    }

    // Instantiate a new DiscoveryListener
//    private val discoveryListener: NsdManager.DiscoveryListener? = null

    class MyDiscoveryListener(
        val serviceType: String,
        val startTime: Long,
        val searchTimeout: Long,
        val nsdManager: NsdManager,
        val resolveListener: NsdManager.ResolveListener,
        val onError: (NetworkManager.ERROR_TYPE) -> Unit,
        val onRunningStateChanged: (Boolean) -> Unit
    ) : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success $service")
            if (service.serviceType == serviceType && (System.currentTimeMillis() - startTime!!) < searchTimeout!!) {
                try {
                    nsdManager.resolveService(service, resolveListener)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "resolveService errory: ${e.message}")
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e(TAG, "service lost: $service")
            onError(NetworkManager.ERROR_TYPE.SERVICE_LOST)
//            running = false
            onRunningStateChanged(false)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
//            running = false
            onRunningStateChanged(false)
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
//            running = false
            onRunningStateChanged(false)
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: IllegalArgumentException) {
                Log.i(TAG, "onStartDiscoveryFailed: ${e.message}")
            }
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
//            running = false
            onRunningStateChanged(false)
            try {
                nsdManager.stopServiceDiscovery(this)
            } catch (e: IllegalArgumentException) {
                Log.i(TAG, "onStopDiscoveryFailed: ${e.message}")
            }
            onError(NetworkManager.ERROR_TYPE.DISCOVERY_ERROR)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
            running = false
            onError(NetworkManager.ERROR_TYPE.RESOLVE_ERROR)
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")

            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host

            Log.d(TAG, "Resolved Service infos: port:$port; host:$host")

            onServiceFound?.invoke(
                NetworkService(
                    serviceInfo.serviceName,
                    serviceInfo.serviceType,
                    host.hostAddress,
                    port
                )
            )

            serviceFound = true
            running = false

        }
    }

    companion object {
        private const val TAG = "NetworkManager"
    }
}