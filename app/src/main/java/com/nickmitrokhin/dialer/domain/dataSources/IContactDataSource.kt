package com.nickmitrokhin.dialer.domain.dataSources

import com.nickmitrokhin.dialer.domain.models.Contact

interface IContactDataSource {
    suspend fun fetchContacts(): List<Contact>
}