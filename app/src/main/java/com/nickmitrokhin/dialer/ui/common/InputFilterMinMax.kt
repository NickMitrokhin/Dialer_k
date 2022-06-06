package com.nickmitrokhin.dialer.ui.common

import android.text.InputFilter
import android.text.Spanned
import java.lang.NumberFormatException

class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {
    private fun isInRange(input: Int): Boolean = input in min..max

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var result: CharSequence? = ""

        try {
            val input = (dest.toString() + source.toString()).toInt()
            if(isInRange(input)) {
                result = null
            }
        } catch(e: NumberFormatException) {
        }
        return result
    }
}