package com.nickmitrokhin.dialer.domain.useCases

import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.repositories.IContactsRepository

class GetContactsUseCase(private val repository: IContactsRepository) {
    suspend operator fun invoke(): List<Contact> = repository.getContacts()
}