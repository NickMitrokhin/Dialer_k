package com.nickmitrokhin.dialer.ui.settings

import androidx.lifecycle.viewModelScope
import com.nickmitrokhin.dialer.data.repositories.PreferencesRepository
import com.nickmitrokhin.dialer.domain.models.SettingsPrefs
import com.nickmitrokhin.dialer.domain.models.UISettingsState
import com.nickmitrokhin.dialer.ui.common.AppViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


sealed class UIAction {
    data class DialCountChange(val value: UShort) : UIAction()
    data class TimeoutChange(val value: UShort) : UIAction()
}

class SettingsViewModel(prefsRepository: PreferencesRepository) :
    AppViewModel<UIAction, UISettingsState>(prefsRepository) {
    override val uiState: StateFlow<UISettingsState>

    init {
        val dialCountFlow = actionState
            .filterIsInstance<UIAction.DialCountChange>()
            .distinctUntilChanged()
            .onStart {
                emit(UIAction.DialCountChange(value = getPreferences().settings.dialCount))
            }
        val timeoutFlow = actionState
            .filterIsInstance<UIAction.TimeoutChange>()
            .distinctUntilChanged()
            .onStart {
                emit(UIAction.TimeoutChange(value = getPreferences().settings.timeout))
            }

        uiState = combine(dialCountFlow, timeoutFlow, ::Pair)
            .map { (dialCount, timeout) ->
                UISettingsState(
                    dialCount.value,
                    timeout.value
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000),
                initialValue = UISettingsState()
            )
    }

    override suspend fun savePreferences(state: UISettingsState) {
        prefsRepository.saveSettingsPrefs(
            SettingsPrefs(
                dialCount = state.dialCount,
                timeout = state.timeout
            )
        )
    }

    fun saveState() {
        runJob {
            savePreferences(uiState.value)
        }
    }
}