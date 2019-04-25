package com.grumpyshoe.module.networkmanager

import com.grumpyshoe.module.networkmanager.models.NetworkService

interface NetworkManager {

    enum class ERROR_TYPE { TIMEOUT, DISCOVERY_ERROR, SERVICE_LOST, RESOLVE_ERROR, UNKNOWN}

    fun getServiceInfo(
        type: String,
        searchTimeout: Long = 3000L,
        onServiceInfoReceived: (NetworkService) -> Unit,
        onError: ((ERROR_TYPE) -> Unit) = {}
    )
}