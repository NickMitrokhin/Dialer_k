package com.nickmitrokhin.dialer.ui.contacts

//import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.models.ContactsPrefs
import com.nickmitrokhin.dialer.domain.repositories.IPreferencesRepository
import com.nickmitrokhin.dialer.domain.useCases.FilterContactsUseCase
import com.nickmitrokhin.dialer.ui.common.AppViewModel
import kotlinx.coroutines.flow.*


sealed class UIAction {
    data class Search(val query: String) : UIAction()
    data class Scroll(val position: Int) : UIAction()
    data class SearchEnabled(val enabled: Boolean) : UIAction()
}

data class UIState(
    val searchQuery: String = "",
    val scrollPosition: Int = 0,
    val searchEnabled: Boolean = false,
    val contacts: List<Contact> = emptyList()
)

class ContactsViewModel(
    private val filterContactsUseCase: FilterContactsUseCase,
    prefsRepository: IPreferencesRepository
) : AppViewModel<UIAction, UIState>(prefsRepository) {
    override val uiState: StateFlow<UIState>

    init {
        val searchEnabledFlow = actionState
            .filterIsInstance<UIAction.SearchEnabled>()
            .distinctUntilChanged()
            .onStart {
                emit(UIAction.SearchEnabled(enabled = getPreferences().contacts.searchEnabled))
            }
        val searchFlow = actionState
            .filterIsInstance<UIAction.Search>()
            .distinctUntilChanged()
            .onStart {
                emit(UIAction.Search(query = getPreferences().contacts.searchQuery))
            }
        val scrollFlow = actionState
            .filterIsInstance<UIAction.Scroll>()
            .distinctUntilChanged()
            .onStart {
                emit(UIAction.Scroll(position = getPreferences().contacts.scrollPosition))
            }

        uiState = combine(searchFlow, scrollFlow, searchEnabledFlow, ::Triple)
            .map { (search, scroll, searchEnabled) ->
                val currentState = UIState(
                    searchQuery = search.query,
                    scrollPosition = scroll.position,
                    searchEnabled = searchEnabled.enabled,
                    contacts = filterContactsUseCase(if (searchEnabled.enabled) search.query else "")
                )
                runJob {
                    savePreferences(currentState)
                }
                currentState
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000),
                initialValue = UIState()
            )
    }

    override suspend fun savePreferences(state: UIState) {
        prefsRepository.saveContactsPrefs(
            ContactsPrefs(
                searchQuery = state.searchQuery,
                scrollPosition = state.scrollPosition,
                searchEnabled = state.searchEnabled
            )
        )
    }
}