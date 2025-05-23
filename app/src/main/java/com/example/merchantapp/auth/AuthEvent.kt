package com.example.merchantapp.auth // Make sure this package matches the directory

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A simple event bus for authentication-related events.
 */
object AuthEventBus { // DEFINITION of AuthEventBus
    private val _events = MutableSharedFlow<AuthEvent>(replay = 0)
    val events = _events.asSharedFlow()

    suspend fun postEvent(event: AuthEvent) {
        _events.emit(event)
    }
}

/**
 * Sealed class representing different authentication events.
 */
sealed class AuthEvent { // DEFINITION of AuthEvent
    object TokenExpiredOrInvalid : AuthEvent()
    // You could add other auth events here in the future if needed
}