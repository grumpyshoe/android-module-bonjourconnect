package com.grumpyshoe.module.bonjourconnect.impl

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.grumpyshoe.module.bonjourconnect.BonjourConnect
import com.grumpyshoe.module.bonjourconnect.models.NetworkService
import org.jetbrains.anko.doAsync

class BonjourConnectImpl(context: Context) : BonjourConnect {

    val nsdManager: NsdManager = context.getSystemService(AppCompatActivity.NSD_SERVICE) as NsdManager
    var running = false
    lateinit var listener : BonjourDiscoveryListener

    /**
     * get infos for a given service
     *
     * @param type - service type (e.g. '_my_service._tcp.')
     * @param searchTimeout - time estimated to find service (default : 5 Sec.)
     * @param onServiceInfoReceived - function that is called if service has been found
     * @param onError - function that is called on any error
     */
    override fun getServiceInfo(
        serviceType: String,
        searchTimeout: Long,
        onServiceInfoReceived: (NetworkService) -> Unit,
        onError: (BonjourConnect.ErrorType) -> Unit
    ) {

        if (serviceType.isEmpty()) {
            onError(BonjourConnect.ErrorType.TYPE_MISSING)
            return
        }

        if (!running) {
            var serviceFound = false
            running = true
            val startTime = System.currentTimeMillis()

            Log.d(TAG, "start discovery for $serviceType")

            listener = BonjourDiscoveryListener(
                onServiceAvailable = {

                   // get service information
                    getServiceInformation(
                        service = it,
                        onSuccess = {service ->
                            if (service.type.contains(serviceType)) {
                                // check for timeout delay has reached
                                if ((System.currentTimeMillis() - startTime) < searchTimeout) {
                                    onServiceInfoReceived(service)
                                }
                                serviceFound = true
                            }
                            else{
                                Log.i(TAG, "Unknown Service discovered: ${service.type}")
                            }
                        },
                        onError = onError)

                    stopDiscovering(listener)
                    running = false

                },
                onError = onError
            )

            startDiscovering(
                listener = listener,
                serviceType = serviceType)

                doAsync {
                    Thread.sleep(searchTimeout)
                    stopDiscovering(listener)
                    if (!serviceFound) {
                        onError(BonjourConnect.ErrorType.TIMEOUT)
                        running = false
                    }
                }
        }
    }

    private fun startDiscovering(listener: BonjourDiscoveryListener, serviceType: String) {

        Log.d(TAG, "nsdManager start discovery for $serviceType | $listener")

        nsdManager.discoverServices(
            serviceType,
            NsdManager.PROTOCOL_DNS_SD,
            listener
        )
    }

    private fun stopDiscovering(listener: BonjourDiscoveryListener) {

        Log.d(TAG, "nsdManager stop discovery for $listener")
        try {
            nsdManager.stopServiceDiscovery(listener)
        }catch (e:Exception){
            Log.e(TAG, e.message, e)
            // just ignore if for now :)
        }
    }

    private fun getServiceInformation(service: NsdServiceInfo, onSuccess:(NetworkService) -> Unit, onError:(BonjourConnect.ErrorType) ->Unit) {

        Log.d(TAG, "getServiceInformation $service")

        nsdManager.resolveService(service, BonjourResolveListener(
            onSuccess = {
                onSuccess(it)
            },
            onError = onError
        ))
    }

    companion object {
        private const val TAG = "BonjourConnect"
    }
}