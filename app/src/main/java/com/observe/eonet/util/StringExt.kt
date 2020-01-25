package com.observe.eonet.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*


fun String.formattedDateTime(): String {
    return this.convertToDate()?.convertToString() ?: ""
}

@SuppressLint("SimpleDateFormat")
fun Date.convertToString(): String? {
    val formatter = SimpleDateFormat("dd-MMM-yyyy hh:mm a")
    return formatter.format(this)
}

fun String.convertToDate(): Date? {
    //EONet data time format : "2019-12-04T06:00:00Z"
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    return parser.parse(this)
}
