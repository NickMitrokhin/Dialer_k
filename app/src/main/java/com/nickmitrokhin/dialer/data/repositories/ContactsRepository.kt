package com.nickmitrokhin.dialer.data.repositories

import com.nickmitrokhin.dialer.domain.dataSources.IContactDataSource
import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.repositories.IContactsRepository

class ContactsRepository(
    private val contactDataSource: IContactDataSource,
) : IContactsRepository {
    override suspend fun getContacts(): List<Contact> = contactDataSource.fetchContacts()
    override suspend fun getContactPhones(contactID: String): List<String> = contactDataSource.fetchContactPhones(contactID)
}