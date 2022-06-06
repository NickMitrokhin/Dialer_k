package com.nickmitrokhin.dialer.domain.useCases

import com.nickmitrokhin.dialer.domain.models.Contact

class FilterContactsUseCase(private val getContactsUseCase: GetContactsUseCase) {
    suspend operator fun invoke(query: String): List<Contact> {
        var result = getContactsUseCase()

        if(query.isNotEmpty()) {
            result = result.filter { it.name.lowercase().contains(query.lowercase()) }
        }

        return result
    }
}