package com.nickmitrokhin.dialer.system

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import com.nickmitrokhin.dialer.domain.ISmsHelper


class SmsHelper(private val context: Context) : ISmsHelper {
    private fun createIntent(text: String): Intent {
        val smsIntent = Intent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val defaultSmsPackageName =
                Telephony.Sms.getDefaultSmsPackage(context)
            smsIntent.action = Intent.ACTION_SEND
            smsIntent.type = "text/plain"
            smsIntent.putExtra(Intent.EXTRA_TEXT, text)
            if (defaultSmsPackageName != null) {
                smsIntent.setPackage(defaultSmsPackageName)
            }
        } else {
            smsIntent.action = Intent.ACTION_VIEW
            smsIntent.putExtra("sms_body", text)
            smsIntent.type = "vnd.android-dir/mms-sms"
        }

        return smsIntent
    }

    override fun createSms(text: String) {
        context.startActivity(createIntent(text))
    }
}