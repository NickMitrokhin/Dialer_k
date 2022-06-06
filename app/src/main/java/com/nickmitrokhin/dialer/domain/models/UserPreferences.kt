package com.nickmitrokhin.dialer.domain.models

data class UserPreferences (
    val contacts: ContactsPrefs,
    val settings: SettingsPrefs
)