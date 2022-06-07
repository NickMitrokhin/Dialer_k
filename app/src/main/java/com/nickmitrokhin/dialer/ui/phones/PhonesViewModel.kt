package com.nickmitrokhin.dialer.ui.phones

import androidx.lifecycle.viewModelScope
import com.nickmitrokhin.dialer.data.repositories.PreferencesRepository
import com.nickmitrokhin.dialer.domain.useCases.CreateSmsUseCase
import com.nickmitrokhin.dialer.domain.useCases.GetContactPhonesUseCase
import com.nickmitrokhin.dialer.ui.common.AppViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UIAction {
    data class Contact(val id: String) : UIAction()
    data class CreateSms(val text: String) : UIAction()
}

data class UIState(
    val contactID: String,
    val phones: List<String>
)

class PhonesViewModel(
    private val getContactPhonesUseCase: GetContactPhonesUseCase,
    private val createSmsUseCase: CreateSmsUseCase,
    prefsRepository: PreferencesRepository,
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
        runJob {
            actionState
                .distinctUntilChanged()
                .onStart {
                    emit(UIAction.Contact(id = ""))
                }
                .collectLatest {
                    when (it) {
                        is UIAction.Contact -> {
                            _uiState.emit(
                                UIState(
                                    contactID = it.id,
                                    phones = getContactPhonesUseCase(it.id)
                                )
                            )
                        }
                        is UIAction.CreateSms -> {
                            createSmsUseCase(it.text)
                        }
                    }

                }
        }
    }

    override suspend fun savePreferences(state: UIState) {}
}