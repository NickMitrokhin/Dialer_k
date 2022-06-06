package com.nickmitrokhin.dialer.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.telephony.TelephonyManager
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus
import com.nickmitrokhin.dialer.system.PhoneCallReceiver

class DialerService : Service(), PhoneCallReceiver.ChangeListener {
    inner class ServiceBinder : Binder() {
        fun getService(): DialerService {
            return this@DialerService
        }
    }

    private val binder: IBinder = ServiceBinder()
    private var receiver: PhoneCallReceiver? = null
    private lateinit var phoneNumber: String
    private var persistentIntent: PendingIntent? = null

    override fun onBind(intent: Intent): IBinder? {
        loadParameters(intent)
        createReceiver()
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        unbindCore()
        return super.onUnbind(intent)
    }

    override fun onChange() {
        try {
            persistentIntent?.send()
        } catch (e: Exception) {
        }
    }

    private fun loadParameters(intent: Intent) {
        phoneNumber = intent.getStringExtra(getString(R.string.calling_phone_no)) as String
        persistentIntent = intent.getParcelableExtra(getString(R.string.pers_intent))
    }

    private fun createReceiver() {
        receiver = PhoneCallReceiver(phoneNumber)
        val intFilter = IntentFilter()

        intFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL)
        intFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(receiver, intFilter)
    }

    private fun unbindCore() {
        if (receiver != null) {
            persistentIntent = null
            unregisterReceiver(receiver)
            receiver = null
        }
    }

    fun toggleListeningStatus(flag: Boolean) {
        val listener = if (flag) this else null
        receiver?.setStatusListener(listener as? PhoneCallReceiver.ChangeListener)
    }

    suspend fun dial(context: Context) {
        receiver?.resetStatus()

        val callIntent = Intent(Intent.ACTION_CALL)

        callIntent.data = Uri.parse(context.getString(R.string.phone_no_prefix) + phoneNumber)
        context.startActivity(callIntent)
    }

    suspend fun callStatus(): PhoneCallStatus {
        return receiver?.getStatus() ?: PhoneCallStatus.IDLE
    }
}