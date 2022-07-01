package com.nickmitrokhin.dialer.ui.contacts

//import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.models.ContactsPrefs
import com.nickmitrokhin.dialer.domain.repositories.IPreferencesRepository
import com.nickmitrokhin.dialer.domain.useCases.FilterContactsUseCase
import com.nickmitrokhin.dialer.ui.common.AppViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


sealed class UIAction {
    data class Search(val query: String) : UIAction()
    data class Scroll(val position: Int) : UIAction()
    data class SearchEnabled(val enabled: Boolean) : UIAction()
}

data class UIState(
    val searchQuery: String = "",
    val scrollPosition: Int = 0,
    val searchEnabled: Boolean = false
)

class ContactsViewModel(
    private val filterContactsUseCase: FilterContactsUseCase,
    prefsRepository: IPreferencesRepository
) : AppViewModel<UIAction, UIState>(prefsRepository) {
    override val uiState: StateFlow<UIState>
    val contacts: Flow<List<Contact>>

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

        contacts = combine(searchFlow, searchEnabledFlow, ::Pair)
            .map { (search, searchEnabled) ->
                filterContactsUseCase(if (searchEnabled.enabled) search.query else "")
            }

        uiState = combine(searchFlow, scrollFlow, searchEnabledFlow, ::Triple)
            .map { (search, scroll, searchEnabled) ->
                val currentState = UIState(search.query, scroll.position, searchEnabled.enabled)
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