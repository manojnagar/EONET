package com.observe.eonet.util

import java.text.SimpleDateFormat


fun String.formatedDateTime(): String {
    //EONet data time format : "2019-12-04T06:00:00Z"
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val formatter = SimpleDateFormat("dd-MMM-yyyy hh:mm a")
    val date = parser.parse(this)
    date?.let {
        return formatter.format(date)
    }
    return ""
}
