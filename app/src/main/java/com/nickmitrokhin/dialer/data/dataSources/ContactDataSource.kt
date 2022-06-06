package com.nickmitrokhin.dialer.data.dataSources

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.nickmitrokhin.dialer.domain.dataSources.IContactDataSource
import com.nickmitrokhin.dialer.domain.models.Contact

class ContactDataSource(private val contentResolver: ContentResolver) : IContactDataSource {
    override suspend fun fetchContacts(): List<Contact> {
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
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID.toLong())
                    val photoUri =
                        Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)

                    result.add(Contact(contactID, contactName, photoUri.toString()))
                }
            }
        }

        return result
    }
}