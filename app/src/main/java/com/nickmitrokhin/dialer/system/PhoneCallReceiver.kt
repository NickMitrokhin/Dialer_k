package com.nickmitrokhin.dialer.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.nickmitrokhin.dialer.data.dataSources.ContactDataSource
import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus
import kotlinx.coroutines.Dispatchers
import java.util.*

class PhoneCallReceiver(private val phoneNumber: String) : BroadcastReceiver() {
    interface ChangeListener {
        fun onChange()
    }

    private var startPhoneCallTime: Long = 0
    private var phoneCallStatus: PhoneCallStatus = PhoneCallStatus.IDLE
    private var context: Context? = null
    private var statusListener: ChangeListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (this.context == null) {
            this.context = context
        }

        receiveCore(intent)
    }

    fun setStatusListener(statusListener: ChangeListener?) {
        this.statusListener = statusListener
    }

    private fun fireStatusChange() {
        statusListener?.onChange()
    }

    private fun receiveCore(intent: Intent) {
        val currentAction = intent.action ?: return

        if (currentAction == Intent.ACTION_NEW_OUTGOING_CALL) {
            val callingNumber: String? = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)//"*100#";
            if (callingNumber != null && callingNumber == phoneNumber) {
                //Log.i("dialer", "outgoing number");
                phoneCallStatus = PhoneCallStatus.OUTGOING
            }
            return
        }
        if (currentAction == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    //Log.i("dialer", "ringing");
                    val incomingNo: String? =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    if (phoneCallStatus != PhoneCallStatus.OUTGOING && incomingNo != null && incomingNo == phoneNumber) {
                        phoneCallStatus = PhoneCallStatus.INCOMING
                        fireStatusChange()
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    //Log.i("dialer", "offhook");
                    if (phoneCallStatus == PhoneCallStatus.INCOMING) {
                        phoneCallStatus = PhoneCallStatus.ACCEPTED
                        fireStatusChange()
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (phoneCallStatus == PhoneCallStatus.INCOMING) {
                        //Log.i("dialer", "idle incoming");
                        phoneCallStatus = PhoneCallStatus.IN_ENDED
                        fireStatusChange()
                    } else if (phoneCallStatus == PhoneCallStatus.OUTGOING) {
                        //Log.i("dialer", "idle outgoing");
                        phoneCallStatus = PhoneCallStatus.OUT_ENDED
                        fireStatusChange()
                    }
                }
            }
        }
    }

    fun resetStatus() {
        if (phoneCallStatus != PhoneCallStatus.IDLE) {
            phoneCallStatus = PhoneCallStatus.IDLE
            fireStatusChange()
        }
        startPhoneCallTime = Date().time
    }

    private suspend fun callWasAccepted(): Boolean {
        var result = false
        if (context != null) {
            val provider = ContactDataSource(context!!.contentResolver, Dispatchers.IO)
            result =
                provider.fetchCallDuration(phoneNumber, phoneCallStatus, startPhoneCallTime) > 0
        }
        return result
    }

    suspend fun getStatus(): PhoneCallStatus {
        var result: PhoneCallStatus = phoneCallStatus
        if (result != PhoneCallStatus.ACCEPTED && callWasAccepted()) {
            result = PhoneCallStatus.ACCEPTED
        }
        return result
    }
}