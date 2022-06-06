package com.nickmitrokhin.dialer.ui.dialer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.nickmitrokhin.dialer.data.repositories.PreferencesRepository
import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus
import com.nickmitrokhin.dialer.domain.models.UISettingsState
import com.nickmitrokhin.dialer.system.ServiceRepository
import com.nickmitrokhin.dialer.system.Timer
import com.nickmitrokhin.dialer.ui.common.AppViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*


sealed class UIAction {
    data class Start(val phoneNumber: String) : UIAction()
    object Resume : UIAction()
    object Pause : UIAction()
    object PendingResult : UIAction()
}

data class UIState(
    val dialCount: UShort = 0u,
    val timeout: UShort = 0u,
    val currentDialCount: UShort = 0u,
    val currentTimeout: UShort = 0u,
    val finished: Boolean = false
)

class DialerViewModel(
    private val serviceRepository: ServiceRepository,
    prefsRepository: PreferencesRepository
) : AppViewModel<UIAction, UIState>(prefsRepository) {
    private var timer: Timer? = null
    private val currentDialCountState = MutableStateFlow<UShort?>(null)
    private val currentTimeoutState = MutableStateFlow<UShort?>(null)
    private val finishedState = MutableStateFlow(false)
    private var pauseJob: Job? = null
    override val uiState: StateFlow<UIState>

    init {
        collectActions()
        uiState = createStateFlow()
        initServiceConnectedCallback()
    }

    private fun collectActions() {
        runJob {
            actionState
                .distinctUntilChanged()
                .collect {
                    when {
                        it is UIAction.Start -> {
                            start(it.phoneNumber)
                        }
                        it === UIAction.Resume -> {
                            resume()
                        }
                        it === UIAction.Pause -> {
                            pause()
                        }
                        it === UIAction.PendingResult -> {
                            pendingResult()
                        }
                    }
                }
        }
    }

    private fun createStateFlow(): StateFlow<UIState> {
        runJob {
            val sharedPrefs = getPreferences().settings
            currentDialCountState.emit(sharedPrefs.dialCount)
            currentTimeoutState.emit(sharedPrefs.timeout)
        }

        val settingsFlow = MutableSharedFlow<UISettingsState>()
            .distinctUntilChanged()
            .onStart {
                val sharedPrefs = getPreferences().settings
                emit(
                    UISettingsState(
                        dialCount = sharedPrefs.dialCount,
                        timeout = sharedPrefs.timeout
                    )
                )
            }

        return combine(
            settingsFlow,
            currentDialCountState,
            currentTimeoutState,
            finishedState
        )
        { settings, currentDialCount, currentTimeout, finished ->
            UIState(
                dialCount = settings.dialCount,
                timeout = settings.timeout,
                currentDialCount = currentDialCount ?: settings.dialCount,
                currentTimeout = currentTimeout ?: settings.timeout,
                finished = finished
            )
        }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000),
                initialValue = UIState()
            )
    }

    private suspend fun fireFinished() {
        finishedState.emit(true)
    }

    private fun initServiceConnectedCallback() {
        serviceRepository.setServiceConnectedCallback {
            runJob {
                startPhoneCall()
            }
        }
    }

    private fun initTimer() {
        val currentState = uiState.value
        val dialCount = currentState.dialCount
        val currentDialCount = currentState.currentDialCount
        val currentTimeout = currentState.currentTimeout
        val timeout = currentState.timeout
        val timeoutDial = if (currentTimeout == 0.toUShort()) timeout else currentTimeout

        timer = Timer(timeoutDial.toLong(), 1, viewModelScope, { secUntilFinished ->
            currentTimeoutState.emit(secUntilFinished.toUShort())
        }, {
            resetTimer()
            currentTimeoutState.emit(0u)
            reduceCurrentDialCount()
            startPhoneCall()
        })

        runJob {
            if (dialCount == currentDialCount) {
                reduceCurrentDialCount()
            }
            timer!!.start()
        }
    }

    private fun restartTimer() {
        if (timer == null) {
            val dialCount = uiState.value.currentDialCount

            if (dialCount > 0.toUShort()) {
                initTimer()
            } else {
                runJob {
                    fireFinished()
                }
            }
        }
    }

    private fun resetTimer() {
        if (timer != null) {
            timer!!.dispose()
            timer = null
        }
    }

    private suspend fun reduceCurrentDialCount() {
        val currentState = uiState.value
        var currentDialCount = currentState.currentDialCount

        if (serviceRepository.serviceEnabled && currentDialCount > 0.toUShort()) {
            currentDialCountState.emit(--currentDialCount)
        }
    }

    private suspend fun checkServiceStatus() {
        if (serviceRepository.serviceEnabled) {
            val callResult = serviceRepository.getCallStatus()
            if (callResult == PhoneCallStatus.ACCEPTED) {
                fireFinished()
            } else if (callResult == PhoneCallStatus.OUT_ENDED
                || callResult == PhoneCallStatus.IN_ENDED
            ) {
                restartTimer()
            }
        }
    }

    private suspend fun startPhoneCall() {
        serviceRepository.startPhoneCall()
    }

    private fun initPause() {
        pauseJob = runJob { //this is a hack
            delay(300)
        }
    }

    private fun resetPause() {
        if (isPaused) {
            pauseJob!!.cancel()
            pauseJob = null
        }
    }

    private val isPaused get() = pauseJob != null && pauseJob!!.isActive

    fun setActivityContextCallback(callback: () -> Context) {
        serviceRepository.setActivityContextCallback(callback)
    }

    fun setPendingIntentCallback(callback: (Int, Intent, Int) -> PendingIntent) {
        serviceRepository.setPendingIntentCallback(callback)
    }

    private fun start(phoneNumber: String) {
        resetPause()
        if (!serviceRepository.serviceEnabled) {
            serviceRepository.startService(phoneNumber)
        }
    }

    private fun resume() {
        if (serviceRepository.serviceEnabled && !isPaused) {
            runJob {
                serviceRepository.toggleListening(true)
                checkServiceStatus()
                if (timer == null) {
                    restartTimer()
                }
            }
        }
    }

    private fun pause() {
        if (!isPaused) {
            serviceRepository.toggleListening(false)
            resetTimer()
            initPause()
        }
    }

    private fun pendingResult() {
        runJob {
            checkServiceStatus()
        }
    }

    override suspend fun savePreferences(state: UIState) {}

    override fun onCleared() {
        serviceRepository.stopService()
        resetTimer()
        super.onCleared()
    }
}