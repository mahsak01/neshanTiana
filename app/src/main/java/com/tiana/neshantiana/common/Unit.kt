package com.tiana.neshantiana.common

import android.text.SpannableString
import android.text.style.UnderlineSpan

fun underlineText( text:String): SpannableString {
    val spannableString = SpannableString(text)
    spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
    return spannableString
}