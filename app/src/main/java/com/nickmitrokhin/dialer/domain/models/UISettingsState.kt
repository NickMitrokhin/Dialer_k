package com.nickmitrokhin.dialer.domain.models

data class UISettingsState(
    val dialCount: UShort = 1u,
    val timeout: UShort = 10u
)