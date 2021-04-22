package com.fragmentcontrollers.core.fragment_navigation_controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.fragmentcontrollers.core.core.TransactionType
import com.fragmentcontrollers.core.navigation_controllers.NavigationController

class FragmentNavigationController() : Fragment(), NavigationController {

    private val screens by lazy { (requireArguments().get(Builder.SCREENS) as ArrayList<Fragment>) }
    private val savedTransactions = arrayListOf<TransactionType>()
    private var isResume = false

    constructor(containerId: Int, activity: FragmentActivity?) : this() {
        if (containerId != -1) {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.addToBackStack(this.toString())
                ?.add(containerId, this)
                ?.commit()
        }
    }

    private val navigationContainer by lazy {
        FragmentContainerView(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screens.forEach { goForward(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = navigationContainer

    override fun onResume() {
        super.onResume()
        isResume = true
        if (savedTransactions.isNotEmpty()) restoreTransactions(savedTransactions)
        savedTransactions.clear()
    }

    private fun restoreTransactions(savedTransactions: List<TransactionType>) {
        savedTransactions.forEach {
            when (it) {
                is TransactionType.Forward -> goForward(it.fragment)
                is TransactionType.Replace -> replace(it.fragment)
                is TransactionType.Back -> goBack()
                is TransactionType.Reset -> reset()
            }
        }
    }

    override fun onPause() {
        isResume = false
        super.onPause()
    }

    override fun getCurrentFragment(): Fragment = screens.last()
    override fun getContainerId(): Int = navigationContainer.id
    override fun canGoBack(): Boolean = screens.size > 1
    override fun onBackPressed() {
        if (!isResume) savedTransactions.add(TransactionType.Back)
        childFragmentManager.beginTransaction()
            .detach(screens.last())
            .also { screens.removeLast() }
            .add(navigationContainer.id, screens.last())
            .commit()
    }

    override fun goBack(): Boolean {
        if (!canGoBack()) return false
        onBackPressed()
            .also { return true }
    }

    override fun replace(fragment: Fragment): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Replace(fragment))
            return false
        }
        onBackPressed()
        goForward(fragment)
            .also { return true }
    }

    override fun goForward(fragment: Fragment): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Forward(fragment))
            return false
        }
        childFragmentManager.beginTransaction()
            .replace(navigationContainer.id, fragment)
            .addToBackStack(fragment.toString())
            .commit()
            .also { screens.takeIf { !it.contains(fragment) }?.add(fragment) }
            .also { return true }
    }

    override fun reset(): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Reset)
            return false
        }
        val lastIndex = screens.lastIndex
        for (index in lastIndex downTo 1) {
            childFragmentManager.beginTransaction()
                .detach(screens[index])
                .commit()
                .also { screens.removeAt(index) }
        }
        return true
    }

    override fun reset(fragment: Fragment): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Reset)
            savedTransactions.add(TransactionType.Replace(fragment))
            return false
        }
        val lastIndex = screens.lastIndex
        for (index in lastIndex downTo 0) {
            childFragmentManager.beginTransaction()
                .detach(screens[index])
                .commitNow()
                .also { screens.removeAt(index) }
        }
        goForward(fragment)
        return true
    }

    class Builder {

        private val screens by lazy { arrayListOf<Fragment>() }
        private var containerId = -1
        private var activity: FragmentActivity? = null

        fun addScreen(fragment: Fragment): Builder {
            screens.add(fragment)
            return this
        }

        fun addScreens(fragment: List<Fragment>): Builder {
            screens.addAll(fragment)
            return this
        }

        fun showInContainer(@IdRes containerId: Int, activity: FragmentActivity): Builder {
            this.containerId = containerId
            this.activity = activity
            return this
        }

        fun build(): FragmentNavigationController {
            val args = Bundle()
            args.putSerializable(SCREENS, screens)
            return FragmentNavigationController(containerId, activity).apply {
                arguments = args
            }
        }

        companion object {
            const val SCREENS = "screens"
        }
    }
}