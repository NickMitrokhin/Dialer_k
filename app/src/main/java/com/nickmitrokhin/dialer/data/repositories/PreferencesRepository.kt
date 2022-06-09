package com.nickmitrokhin.dialer.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.nickmitrokhin.dialer.domain.models.ContactsPrefs
import com.nickmitrokhin.dialer.domain.models.SettingsPrefs
import com.nickmitrokhin.dialer.domain.models.UserPreferences
import com.nickmitrokhin.dialer.domain.repositories.IPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException


class PreferencesRepository(private val dataStore: DataStore<Preferences>) :
    IPreferencesRepository {
    private object PreferencesKeys {
        val CONTACT_SEARCH_QUERY = stringPreferencesKey("contact_search_query")
        val CONTACT_SCROLL_POSITION = intPreferencesKey("contact_scroll_position")
        val CONTACT_SEARCH_ENABLED = booleanPreferencesKey("contact_search_enabled")
        val SETTINGS_DIAL_COUNT = intPreferencesKey("settings_dial_count")
        val SETTINGS_TIMEOUT = intPreferencesKey("settings_timeout")
    }

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            mapPreferences(prefs)
        }

    private fun mapPreferences(prefs: Preferences): UserPreferences {
        return UserPreferences(
            contacts = getContactsPrefs(prefs),
            settings = getSettingsPrefs(prefs)
        )
    }

    private fun getContactsPrefs(prefs: Preferences): ContactsPrefs {
        return ContactsPrefs(
            searchQuery = prefs[PreferencesKeys.CONTACT_SEARCH_QUERY] ?: "",
            scrollPosition = prefs[PreferencesKeys.CONTACT_SCROLL_POSITION] ?: 0,
            searchEnabled = prefs[PreferencesKeys.CONTACT_SEARCH_ENABLED] ?: false
        )
    }

    private fun getSettingsPrefs(prefs: Preferences): SettingsPrefs {
        return SettingsPrefs(
            dialCount = (prefs[PreferencesKeys.SETTINGS_DIAL_COUNT] ?: 1).toUShort(),
            timeout = (prefs[PreferencesKeys.SETTINGS_TIMEOUT] ?: 10).toUShort()
        )
    }

    override suspend fun saveContactsPrefs(preferences: ContactsPrefs) =
        withContext<Unit>(Dispatchers.IO) {
            dataStore.edit { prefs ->
                prefs[PreferencesKeys.CONTACT_SEARCH_QUERY] = preferences.searchQuery
                prefs[PreferencesKeys.CONTACT_SCROLL_POSITION] = preferences.scrollPosition
                prefs[PreferencesKeys.CONTACT_SEARCH_ENABLED] = preferences.searchEnabled
            }
        }

    override suspend fun saveSettingsPrefs(preferences: SettingsPrefs) =
        withContext<Unit>(Dispatchers.IO) {
            dataStore.edit { prefs ->
                prefs[PreferencesKeys.SETTINGS_DIAL_COUNT] = preferences.dialCount.toInt()
                prefs[PreferencesKeys.SETTINGS_TIMEOUT] = preferences.timeout.toInt()
            }
        }
}