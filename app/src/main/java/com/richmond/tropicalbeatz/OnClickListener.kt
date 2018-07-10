package com.richmond.tropicalbeatz

interface OnEventListener<T> {
    fun onSuccess(`object`: T)
    fun onFailure(e: Exception)
}
