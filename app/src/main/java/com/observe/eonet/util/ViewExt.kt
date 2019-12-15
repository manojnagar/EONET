package com.observe.eonet.util

import android.view.View

var View.visible: Boolean
  get() = visibility == View.VISIBLE
  set(value) {
    visibility = if (value) View.VISIBLE else View.GONE
  }

fun View.makeVisible() {
  this.visible = true
}

fun View.makeInVisible() {
  this.visible = false
}

fun makeInVisible(vararg views: View) {
  for (view in views) {
    view.makeInVisible()
  }
}