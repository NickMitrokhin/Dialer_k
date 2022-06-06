package com.nickmitrokhin.dialer.ui.common

import android.content.Context
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.nickmitrokhin.dialer.data.dataSources.ContactDataSource
import com.nickmitrokhin.dialer.data.dataSources.ContactPhonesDataSource
import com.nickmitrokhin.dialer.data.repositories.ContactsRepository
import com.nickmitrokhin.dialer.data.repositories.PreferencesRepository
import com.nickmitrokhin.dialer.dataStore
import com.nickmitrokhin.dialer.domain.useCases.FilterContactsUseCase
import com.nickmitrokhin.dialer.domain.useCases.GetContactPhonesUseCase
import com.nickmitrokhin.dialer.domain.useCases.GetContactsUseCase
import com.nickmitrokhin.dialer.system.ServiceRepository
import com.nickmitrokhin.dialer.ui.contacts.ContactsViewModel
import com.nickmitrokhin.dialer.ui.dialer.DialerViewModel
import com.nickmitrokhin.dialer.ui.phones.PhonesViewModel
import com.nickmitrokhin.dialer.ui.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import java.lang.IllegalArgumentException

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val context: Context) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return when {
            modelClass.isAssignableFrom(ContactsViewModel::class.java) -> {
                val getContactsUseCase = GetContactsUseCase(createContactsRepository(context))
                val filterContactsUseCase = FilterContactsUseCase(getContactsUseCase)
                ContactsViewModel(filterContactsUseCase, createPreferencesRepository(context)) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(createPreferencesRepository(context)) as T
            }
            modelClass.isAssignableFrom(PhonesViewModel::class.java) -> {
                val getContactPhonesUseCase = GetContactPhonesUseCase(createContactsRepository(context))
                PhonesViewModel(getContactPhonesUseCase, createPreferencesRepository(context)) as T
            }
            modelClass.isAssignableFrom(DialerViewModel::class.java) -> {
                DialerViewModel(ServiceRepository(context), createPreferencesRepository(context)) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }



    companion object {
        private var contactsRepository: ContactsRepository? = null
        private var preferencesRepository: PreferencesRepository? = null

        private fun createContactsRepository(context: Context): ContactsRepository {
            if (contactsRepository == null) {
                contactsRepository = ContactsRepository(
                    contactDataSource = ContactDataSource(context.contentResolver),
                    contactPhonesDataSource = ContactPhonesDataSource(context.contentResolver),
                    dispatcher = Dispatchers.IO
                )
            }
            return contactsRepository!!
        }

        private fun createPreferencesRepository(context: Context): PreferencesRepository {
            if (preferencesRepository == null) {
                preferencesRepository = PreferencesRepository(context.dataStore)
            }
            return preferencesRepository!!
        }
    }
}