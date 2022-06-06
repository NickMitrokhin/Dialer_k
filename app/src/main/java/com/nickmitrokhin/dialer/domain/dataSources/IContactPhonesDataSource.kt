package com.nickmitrokhin.dialer.domain.dataSources

interface IContactPhonesDataSource {
    suspend fun fetchContactPhones(contactID: String): List<String>
}