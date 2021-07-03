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

    fun goForward(fragment: Fragment, tag: String? = null): Boolean {
        return false
    }

    fun goForwardChain(vararg fragments: Fragment, tag: String? = null): Boolean {
        return false
    }

    fun goBack(): Boolean {
        return false
    }

    fun replace(fragment: Fragment, tag: String? = null): Boolean {
        return false
    }

    fun reset(): Boolean {
        return false
    }

    fun reset(fragment: Fragment, tag: String? = null): Boolean {
        return false
    }
}