package com.grumpyshoe.module.bonjourconnect

import com.grumpyshoe.module.bonjourconnect.models.NetworkService

interface BonjourConnect {

    enum class ErrorType { TIMEOUT, DISCOVERY_ERROR, SERVICE_LOST, RESOLVE_ERROR, UNKNOWN}

    fun getServiceInfo(
        type: String,
        searchTimeout: Long = 3000L,
        onServiceInfoReceived: (NetworkService) -> Unit,
        onError: ((ErrorType) -> Unit) = {}
    )
}