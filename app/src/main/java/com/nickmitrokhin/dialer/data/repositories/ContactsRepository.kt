package com.nickmitrokhin.dialer.data.repositories

import com.nickmitrokhin.dialer.domain.dataSources.IContactDataSource
import com.nickmitrokhin.dialer.domain.dataSources.IContactPhonesDataSource
import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.repositories.IContactsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val contactDataSource: IContactDataSource,
    private val contactPhonesDataSource: IContactPhonesDataSource,
    private val dispatcher: CoroutineDispatcher
) : IContactsRepository {
    override suspend fun getContacts(): List<Contact> = withContext(dispatcher) {
        contactDataSource.fetchContacts()
    }

    override suspend fun getContactPhones(contactID: String): List<String> = withContext(dispatcher) {
        contactPhonesDataSource.fetchContactPhones(contactID)
    }
}