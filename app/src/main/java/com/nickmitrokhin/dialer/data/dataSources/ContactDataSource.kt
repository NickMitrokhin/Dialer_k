package com.nickmitrokhin.dialer.data.dataSources

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import com.nickmitrokhin.dialer.domain.dataSources.IContactDataSource
import com.nickmitrokhin.dialer.domain.models.Contact
import com.nickmitrokhin.dialer.domain.models.PhoneCallStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ContactDataSource(
    private val contentResolver: ContentResolver,
    private val dispatcher: CoroutineDispatcher
) : IContactDataSource {
    override suspend fun fetchContacts() = withContext<List<Contact>>(dispatcher) {
        val curContacts: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )
        val result = ArrayList<Contact>()
        curContacts?.use { cContacts ->
            while (cContacts.moveToNext()) {
                val idColumnIndex = cContacts.getColumnIndex(ContactsContract.Contacts._ID)
                val contactID = cContacts.getString(idColumnIndex)

                val nameColumnIndex =
                    cContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val contactName = cContacts.getString(nameColumnIndex)

                val phoneNumberColumnIndex =
                    cContacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val contactHasPhoneNumbers =
                    Integer.parseInt(cContacts.getString(phoneNumberColumnIndex)) > 0

                if (contactHasPhoneNumbers) {
                    val contactUri =
                        ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI,
                            contactID.toLong()
                        )
                    val photoUri =
                        Uri.withAppendedPath(
                            contactUri,
                            ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                        )

                    result.add(Contact(contactID, contactName, photoUri.toString()))
                }
            }
        }

        result
    }

    override suspend fun fetchContactPhones(contactID: String) = withContext<List<String>>(
        dispatcher
    ) {
        val result: ArrayList<String> = ArrayList()
        if (contactID.isNotEmpty()) {
            val curPhones = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactID), null
            )

            curPhones?.use { crPhones ->
                while (crPhones.moveToNext()) {
                    val phoneNumberColumnIndex =
                        crPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val phoneNo =
                        getSanitizedPhoneNumber(crPhones.getString(phoneNumberColumnIndex))
                    if (phoneNo != null && !result.contains(phoneNo)) {
                        result.add(phoneNo)
                    }
                }
            }
        }
        result
    }

    override suspend fun fetchCallDuration(
        phoneNumber: String,
        phoneCallStatus: PhoneCallStatus,
        startPhoneCallTime: Long
    ) = withContext(dispatcher) {
        var cursor: Cursor? = null
        var result = 0
        try {
            val filterExpression: String = getFilterExpression(phoneNumber, phoneCallStatus, startPhoneCallTime)
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI, null,
                filterExpression,
                null, "${CallLog.Calls.DATE} desc"
            )
            if (cursor != null && cursor.moveToFirst()) {
                val colIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
                result = cursor.getString(colIndex).toInt()
            }
        } catch (e: Exception) {
        } finally {
            cursor?.close()
        }
        result
    }

    private fun getFilterExpression(
        phoneNumber: String,
        phoneCallStatus: PhoneCallStatus,
        startPhoneCallTime: Long
    ): String {
        val callType =
            if (phoneCallStatus == PhoneCallStatus.OUT_ENDED) CallLog.Calls.OUTGOING_TYPE else CallLog.Calls.INCOMING_TYPE
        return "${CallLog.Calls.TYPE}=${callType} and ${CallLog.Calls.NUMBER}='${phoneNumber}' and ${CallLog.Calls.DATE}>=${startPhoneCallTime}"
    }

    companion object {
        private fun getSanitizedPhoneNumber(phoneNo: String?): String? {
            return phoneNo?.replace("[()-]".toRegex(), "")?.replace(" ", "")
        }
    }
}