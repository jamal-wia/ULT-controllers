package com.fragmentcontrollers.core.core

import androidx.fragment.app.Fragment

interface CurrentScreenProvider {
    fun getCurrentFragment(): Fragment
}