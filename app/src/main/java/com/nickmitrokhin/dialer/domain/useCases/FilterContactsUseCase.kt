package com.nickmitrokhin.dialer.domain.useCases

import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.repositories.IContactsRepository

class FilterContactsUseCase(private val repository: IContactsRepository) {
    suspend operator fun invoke(query: String): List<Contact> {
        var result = repository.getContacts()

        if(query.isNotEmpty()) {
            result = result.filter { it.name.lowercase().contains(query.lowercase()) }
        }

        return result
    }
}