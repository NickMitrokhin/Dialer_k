package com.nickmitrokhin.dialer.ui.phones

import androidx.lifecycle.viewModelScope
import com.nickmitrokhin.dialer.data.repositories.PreferencesRepository
import com.nickmitrokhin.dialer.domain.useCases.GetContactPhonesUseCase
import com.nickmitrokhin.dialer.ui.common.AppViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UIAction {
    data class Contact(val id: String) : UIAction()
}

data class UIState(
    val contactID: String,
    val phones: List<String>
)

class PhonesViewModel(
    private val getContactPhonesUseCase: GetContactPhonesUseCase,
    prefsRepository: PreferencesRepository
) : AppViewModel<UIAction, UIState>(prefsRepository) {
    private val _uiState = MutableStateFlow(
        UIState(
            contactID = "",
            phones = emptyList()
        )
    )
    override val uiState: StateFlow<UIState> = _uiState

    init {
        collectState()
    }

    private fun collectState() {
        viewModelScope.launch {
            actionState
                .filterIsInstance<UIAction.Contact>()
                .distinctUntilChanged()
                .onStart {
                    emit(UIAction.Contact(id = ""))
                }
                .collectLatest {
                    _uiState.emit(
                        UIState(
                            contactID = it.id,
                            phones = getContactPhonesUseCase(it.id)
                        )
                    )
                }
        }
    }

    override suspend fun savePreferences(state: UIState) {}
}