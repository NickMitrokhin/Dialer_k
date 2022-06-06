package com.nickmitrokhin.dialer.domain.repositories

import com.nickmitrokhin.dialer.domain.models.Contact

interface IContactsRepository {
    suspend fun getContacts(): List<Contact>
    suspend fun getContactPhones(contactID: String): List<String>
}