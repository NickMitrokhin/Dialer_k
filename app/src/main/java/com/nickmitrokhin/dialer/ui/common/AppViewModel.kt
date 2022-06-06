package com.nickmitrokhin.dialer.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nickmitrokhin.dialer.domain.models.UserPreferences
import com.nickmitrokhin.dialer.domain.repositories.IPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class AppViewModel<TAction, TState>(
    protected val prefsRepository: IPreferencesRepository
) : ViewModel() {
    protected val actionState = MutableSharedFlow<TAction>()
    abstract val uiState: StateFlow<TState>

    fun action(value: TAction) {
        runJob {
            actionState.emit(value)
        }
    }

    protected fun runJob(callback: suspend () -> Unit): Job {
        return viewModelScope.launch {
            callback()
        }
    }

    protected suspend fun getPreferences(): UserPreferences {
        return prefsRepository.preferences.first()
    }

    protected abstract suspend fun savePreferences(state: TState)
}