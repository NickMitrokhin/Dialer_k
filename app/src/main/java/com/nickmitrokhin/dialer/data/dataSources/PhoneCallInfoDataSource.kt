package com.nickmitrokhin.dialer.data.dataSources

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus

class PhoneCallInfoDataSource(
    private val context: Context,
    private val phoneNumber: String,
    private val phoneCallStatus: PhoneCallStatus,
    private val startPhoneCallTime: Long
) {
    private fun getFilterExpression(): String {
        val callType =
            if(phoneCallStatus == PhoneCallStatus.OUT_ENDED) CallLog.Calls.OUTGOING_TYPE else CallLog.Calls.INCOMING_TYPE
        return "${CallLog.Calls.TYPE}=${callType} and ${CallLog.Calls.NUMBER}='${phoneNumber}' and ${CallLog.Calls.DATE}>=${startPhoneCallTime}"
    }

    suspend fun fetchCallDuration(): Int {
        var cursor: Cursor? = null
        var result = 0
        try {
            val filterExpression: String = getFilterExpression()
            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI, null,
                filterExpression,
                null, "${CallLog.Calls.DATE} desc"
            )
            if(cursor != null && cursor.moveToFirst()) {
                val colIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
                result = cursor.getString(colIndex).toInt()
            }
        } catch(e: Exception) {
        } finally {
            cursor?.close()
        }
        return result
    }
}