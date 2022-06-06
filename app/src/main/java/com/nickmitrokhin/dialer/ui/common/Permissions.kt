package com.nickmitrokhin.dialer.ui.common

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

enum class Permissions(val value: Int) {
    READ_CONTACTS(1),
    CALL_PHONE(2),
    READ_PHONE_STATE(3),
    PROCESS_OUTGOING_CALLS(4),
    SEND_SMS(5),
    READ_CALL_LOG(6)
}

object PermissionHelper {
    fun requestPermissions(fragment: Fragment, permissions: Array<String>, resultCallback: (Boolean) -> Unit) {
        val resultLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allGranted = true

            for (bResult in result.values) {
                allGranted = allGranted && bResult
            }

            resultCallback(allGranted)
        }

        resultLauncher.launch(permissions)
    }
}