package com.volio.vn.callscreen.helpers

import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.util.Log
import com.volio.vn.callscreen.R
import com.volio.vn.callscreen.extensions.ensureBackgroundThread
import com.volio.vn.callscreen.extensions.getMyContactsCursor
import com.volio.vn.callscreen.extensions.getPhoneNumberTypeText
import com.volio.vn.callscreen.extensions.isConference
import com.volio.vn.callscreen.models.PhoneNumber
import com.volio.vn.callscreen.models.contact.Contact

fun getCallContact(context: Context, call: Call?, callback: (Contact) -> Unit) {
    if (call.isConference()) {
        // callback(Contact(context.getString(R.string.conference), "", "", ""))
        return
    }

    val privateCursor = context.getMyContactsCursor(false, true)
    ensureBackgroundThread {
        val callContact = Contact()
        val handle = try {
            call?.details?.handle?.toString()
        } catch (e: NullPointerException) {
            null
        }

        if (handle == null) {
            callback(callContact)
            return@ensureBackgroundThread
        }

        val uri = Uri.decode(handle)
        if (uri.startsWith("tel:")) {
            val number = uri.substringAfter("tel:")
            ContactsHelper(context).getContacts { contacts ->
                Log.d("NAzxxxxi", "mmmmm: $contacts")
                val privateContacts = MyContactsContentProvider.getContacts(context, privateCursor)
                if (privateContacts.isNotEmpty()) {
                    contacts.addAll(privateContacts)
                }

                val contactsWithMultipleNumbers = contacts.filter { it.phoneNumbers.size > 1 }
                val numbersToContactIDMap = HashMap<String, Int>()
                contactsWithMultipleNumbers.forEach { contact ->
                    contact.phoneNumbers.forEach { phoneNumber ->
                        numbersToContactIDMap[phoneNumber.value] = contact.contactId
                        numbersToContactIDMap[phoneNumber.normalizedNumber] = contact.contactId
                    }
                }

                val contact = contacts.firstOrNull { it.doesHavePhoneNumber(number) }
                contact?.let {
                    callback(it)
                } ?: run {
                    val phoneNumber = PhoneNumber(number, 0, "", number, false)
                    callContact.phoneNumbers.add(phoneNumber)
                    callback(callContact)
                }
            }
        }
    }
}
