package com.nickmitrokhin.dialer.domain.repositories

import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus

interface IServiceRepository {
    fun setServiceConnectedCallback(callback: () -> Unit)
    fun startService(phoneNumber: String)
    suspend fun startPhoneCall()
    suspend fun getCallStatus(): PhoneCallStatus?
    fun toggleListening(value: Boolean)
    fun stopService()
    val serviceEnabled: Boolean
}