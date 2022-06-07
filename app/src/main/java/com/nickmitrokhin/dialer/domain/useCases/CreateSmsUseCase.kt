package com.nickmitrokhin.dialer.domain.useCases

import com.nickmitrokhin.dialer.domain.ISmsHelper

class CreateSmsUseCase(private val helper: ISmsHelper) {
    operator fun invoke(text: String) {
        helper.createSms(text)
    }
}