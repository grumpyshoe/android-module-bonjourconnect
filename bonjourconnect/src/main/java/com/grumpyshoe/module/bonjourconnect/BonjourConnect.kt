package com.grumpyshoe.module.bonjourconnect

import com.grumpyshoe.module.bonjourconnect.models.NetworkService

interface BonjourConnect {

    enum class ErrorType { TYPE_MISSING, TIMEOUT, DISCOVERY_ERROR, SERVICE_LOST, RESOLVE_ERROR, UNKNOWN}

    fun getServiceInfo(
        type: String,
        searchTimeout: Long = 5000L,
        onServiceInfoReceived: (NetworkService) -> Unit,
        onError: ((ErrorType) -> Unit) = {}
    )
}