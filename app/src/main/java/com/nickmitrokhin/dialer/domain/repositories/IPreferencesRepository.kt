package com.nickmitrokhin.dialer.domain.repositories

import com.nickmitrokhin.dialer.domain.models.ContactsPrefs
import com.nickmitrokhin.dialer.domain.models.SettingsPrefs
import com.nickmitrokhin.dialer.domain.models.UserPreferences
import kotlinx.coroutines.flow.Flow

interface IPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun saveContactsPrefs(preferences: ContactsPrefs)
    suspend fun saveSettingsPrefs(preferences: SettingsPrefs)
}