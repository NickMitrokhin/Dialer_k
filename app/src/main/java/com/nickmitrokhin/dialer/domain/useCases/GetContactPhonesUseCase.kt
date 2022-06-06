package com.nickmitrokhin.dialer.domain.useCases

import com.nickmitrokhin.dialer.domain.repositories.IContactsRepository

class GetContactPhonesUseCase(private val repository: IContactsRepository) {
    suspend operator fun invoke(contactID: String) = repository.getContactPhones(contactID)
}