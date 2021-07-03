package com.fragmentcontrollers.core.core

import androidx.fragment.app.Fragment

sealed class TransactionType {
    class Forward(val fragment: Fragment, val tag: String? = null) : TransactionType()
    class Replace(val fragment: Fragment, val tag: String? = null) : TransactionType()
    object Back : TransactionType()
    object Reset : TransactionType()
}