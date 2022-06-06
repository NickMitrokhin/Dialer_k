package com.nickmitrokhin.dialer.system

import android.os.CountDownTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Timer(
    startTimer: Long,
    interval: Long,
    private val scope: CoroutineScope,
    private val tickCallback: suspend (Long) -> Unit,
    private val finishCallback: suspend () -> Unit
) : CountDownTimer(startTimer * 1000, interval * 1000) {
    private var lastTickJob: Job? = null
    private var finishJob: Job? = null

    override fun onTick(millisUntilFinish: Long) {
        lastTickJob = scope.launch {
            tickCallback(millisUntilFinish / 1000)
        }
    }

    override fun onFinish() {
        finishJob = scope.launch {
            joinJob(lastTickJob)
            finishCallback()
        }
    }

    fun dispose() {
        cancel()
        cancelJob(lastTickJob)
        cancelJob(finishJob)
    }

    private companion object {
        private suspend fun joinJob(job: Job?) {
            if (job != null && job!!.isActive) {
                job!!.join()
            }
        }

        private fun cancelJob(job: Job?) {
            if (job != null && job!!.isActive) {
                job!!.cancel()
            }
        }
    }
}