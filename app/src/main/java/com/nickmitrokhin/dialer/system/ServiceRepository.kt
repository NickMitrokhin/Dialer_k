package com.nickmitrokhin.dialer.system

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.nickmitrokhin.dialer.R
import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus
import com.nickmitrokhin.dialer.domain.repositories.IServiceRepository
import com.nickmitrokhin.dialer.services.DialerService
import java.lang.Exception

class ServiceRepository(private val context: Context) : IServiceRepository, ServiceConnection {
    private var service: DialerService? = null
    private var connectedCallback: (() -> Unit)? = null
    private var activityContextCallback: (() -> Context)? = null
    private var pendingIntentCallback: ((Int, Intent, Int) -> PendingIntent)? = null

    override val serviceEnabled: Boolean get() = service != null

    override fun setServiceConnectedCallback(callback: () -> Unit) {
        connectedCallback = callback
    }

    fun setActivityContextCallback(callback: () -> Context) {
        activityContextCallback = callback
    }

    fun setPendingIntentCallback(callback: (Int, Intent, Int) -> PendingIntent) {
        pendingIntentCallback = callback
    }

    private fun getActivityContext(): Context {
        if (activityContextCallback == null) {
            throw Exception("activityContextCallback is not specified.")
        }
        return activityContextCallback!!()
    }

    override fun startService(phoneNumber: String) {
        if (service == null) {
            val intent = Intent(context, DialerService::class.java)

            if (pendingIntentCallback == null) {
                throw Exception("pendingIntentCallback is not specified.")
            }

            val pIntent = pendingIntentCallback!!(0, intent, 0)

            intent.putExtra(context!!.getString(R.string.calling_phone_no), phoneNumber)
                .putExtra(context!!.getString(R.string.pers_intent), pIntent)
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    override suspend fun startPhoneCall() {
        service?.dial(getActivityContext())
    }

    override suspend fun getCallStatus(): PhoneCallStatus? {
        return service?.callStatus()
    }

    override fun toggleListening(value: Boolean) {
        service?.toggleListeningStatus(value)
    }

    override fun stopService() {
        if (service != null) {
            service!!.toggleListeningStatus(false)
            context.unbindService(this)
            service = null
        }
    }

    override fun onServiceConnected(className: ComponentName, binder: IBinder) {
        service = (binder as DialerService.ServiceBinder).getService()
        if (connectedCallback != null) {
            connectedCallback!!()
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        service = null
    }
}