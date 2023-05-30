package com.volio.vn.callscreen.services

import android.net.Uri
import android.telecom.Call
import android.telecom.CallScreeningService
import com.volio.vn.callscreen.extensions.getMyContactsCursor
import com.volio.vn.callscreen.extensions.isNumberBlocked
import com.volio.vn.callscreen.helpers.SimpleContactsHelper
import normalizePhoneNumber

class SimpleCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = Uri.decode(callDetails.handle?.toString())?.substringAfter("tel:")
        if (number != null && isNumberBlocked(number.normalizePhoneNumber())) {
            respondToCall(callDetails, isBlocked = true)
        } else if (number != null ) {
            val simpleContactsHelper = SimpleContactsHelper(this)
            val privateCursor = getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
            simpleContactsHelper.exists(number, privateCursor) { exists ->
                respondToCall(callDetails, isBlocked = !exists)
            }
        } else {
            respondToCall(callDetails, isBlocked = false)
        }
    }

    private fun respondToCall(callDetails: Call.Details, isBlocked: Boolean) {
        val response = CallResponse.Builder()
            .setDisallowCall(isBlocked)
            .setRejectCall(isBlocked)
            .setSkipCallLog(isBlocked)
            .setSkipNotification(isBlocked)
            .build()

        respondToCall(callDetails, response)
    }

    companion object{

    }
}
