package com.fragmentcontrollers.core.core

interface OnBackPressedProvider {
    fun canGoBack(): Boolean
    fun onBackPressed()
}