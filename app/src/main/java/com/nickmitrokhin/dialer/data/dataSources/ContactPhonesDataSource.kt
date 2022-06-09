package com.nickmitrokhin.dialer.data.dataSources

import android.content.ContentResolver
import android.provider.ContactsContract
import com.nickmitrokhin.dialer.domain.dataSources.IContactPhonesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactPhonesDataSource(private val contentResolver: ContentResolver) :
    IContactPhonesDataSource {
    override suspend fun fetchContactPhones(contactID: String) = withContext<List<String>>(
        Dispatchers.IO) {
        val result: ArrayList<String> = ArrayList()
        if(contactID.isNotEmpty()) {
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
                    val phoneNo = getSanitizedPhoneNumber(crPhones.getString(phoneNumberColumnIndex))
                    if (phoneNo != null && !result.contains(phoneNo)) {
                        result.add(phoneNo)
                    }
                }
            }
        }
        result
    }

    companion object {
        private fun getSanitizedPhoneNumber(phoneNo: String?): String? {
            return phoneNo?.replace("[()-]".toRegex(), "")?.replace(" ", "")
        }
    }
}

