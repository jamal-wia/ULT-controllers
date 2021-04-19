package com.fragmentcontrollers.core.core

import androidx.fragment.app.Fragment

sealed class TransactionType {
    class Forward(val fragment: Fragment) : TransactionType()
    class Replace(val fragment: Fragment) : TransactionType()
    object Back : TransactionType()
    object Reset : TransactionType()
}