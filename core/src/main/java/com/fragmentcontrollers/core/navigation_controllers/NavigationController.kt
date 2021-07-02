package com.fragmentcontrollers.core.navigation_controllers

import androidx.fragment.app.Fragment
import com.fragmentcontrollers.core.core.ContainerIdProvider
import com.fragmentcontrollers.core.core.CurrentScreenProvider
import com.fragmentcontrollers.core.core.OnBackPressedProvider
import com.fragmentcontrollers.core.core.TransactionType

interface NavigationController :
    ContainerIdProvider,
    OnBackPressedProvider,
    CurrentScreenProvider {

    fun onTransactionScreenListener(
        transaction: (transactionType: TransactionType, fromFragment: Fragment, toFragment: Fragment) -> Unit
    ) {

    }

    fun goForward(fragment: Fragment): Boolean {
        return false
    }

    fun goForward(vararg fragments: Fragment): Boolean {
        return false
    }

    fun goBack(): Boolean {
        return false
    }

    fun replace(fragment: Fragment): Boolean {
        return false
    }

    fun reset(): Boolean {
        return false
    }

    fun reset(fragment: Fragment): Boolean {
        return false
    }
}