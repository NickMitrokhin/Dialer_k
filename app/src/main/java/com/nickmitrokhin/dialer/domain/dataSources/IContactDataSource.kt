package com.nickmitrokhin.dialer.domain.dataSources

import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus

interface IContactDataSource {
    suspend fun fetchContacts(): List<Contact>
    suspend fun fetchContactPhones(contactID: String): List<String>
    suspend fun fetchCallDuration(
        phoneNumber: String,
        phoneCallStatus: PhoneCallStatus,
        startPhoneCallTime: Long
    ): Int
}