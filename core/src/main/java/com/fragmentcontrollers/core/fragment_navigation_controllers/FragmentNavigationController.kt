package com.fragmentcontrollers.core.fragment_navigation_controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.fragmentcontrollers.core.core.TransactionType
import com.fragmentcontrollers.core.navigation_controllers.NavigationController

class FragmentNavigationController : Fragment(), NavigationController {

    private val args by lazy { requireArguments() }
    private val ctx by lazy { requireContext() }

    private val screensArgs: ArrayList<Fragment> by lazy {
        val list = args[Builder.SCREENS] as List<*>
        list.map { it as Fragment } as ArrayList
    }

    private val savedTransactions = arrayListOf<TransactionType>()
    private var isResume = false

    private val navigationContainer by lazy {
        FragmentContainerView(ctx).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screensArgs.forEach { goForward(it) }
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

    override fun getCurrentFragment(): Fragment = screensArgs.last()
    override fun getContainerId(): Int = navigationContainer.id
    override fun canGoBack(): Boolean = screensArgs.size > 1
    override fun onBackPressed() {
        if (!isResume) {
            savedTransactions.add(TransactionType.Back)
            return
        }
        childFragmentManager.beginTransaction()
            .detach(screensArgs.last())
            .also { screensArgs.removeLast() }
            .add(navigationContainer.id, screensArgs.last())
            .commit()
    }

    override fun goBack(): Boolean {
        if (!canGoBack()) return false
        onBackPressed()
        return true
    }

    override fun replace(fragment: Fragment): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Replace(fragment))
            return false
        }
        onBackPressed()
        goForward(fragment)
        return true
    }

    override fun goForward(fragment: Fragment): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Forward(fragment))
            return false
        }
        childFragmentManager.beginTransaction()
            .replace(navigationContainer.id, fragment, fragment.tag ?: fragment.toString())
            .addToBackStack(fragment.tag ?: fragment.toString())
            .commit()
            .also { screensArgs.takeIf { !it.contains(fragment) }?.add(fragment) }
            .also { return true }
    }

    override fun reset(): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Reset)
            return false
        }
        val lastIndex = screensArgs.lastIndex
        for (index in lastIndex downTo 1) {
            childFragmentManager.beginTransaction()
                .detach(screensArgs[index])
                .commit()
                .also { screensArgs.removeAt(index) }
        }
        return true
    }

    override fun reset(fragment: Fragment): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Reset)
            savedTransactions.add(TransactionType.Replace(fragment))
            return false
        }
        val lastIndex = screensArgs.lastIndex
        for (index in lastIndex downTo 0) {
            childFragmentManager.beginTransaction()
                .detach(screensArgs[index])
                .commitNow()
                .also { screensArgs.removeAt(index) }
        }
        goForward(fragment)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        args.putSerializable(Builder.SCREENS, screensArgs)
    }

    class Builder {

        private val screens by lazy { arrayListOf<Fragment>() }
        private var tag: String? = null
        private var containerId = -1
        private var fragmentManager: FragmentManager? = null

        fun addScreenToChain(fragment: Fragment): Builder {
            screens.add(fragment)
            return this
        }

        fun addScreensToChain(fragments: List<Fragment>): Builder {
            screens.addAll(fragments)
            return this
        }

        fun addScreensToChain(vararg fragments: Fragment): Builder {
            screens.addAll(fragments)
            return this
        }

        fun newRootScreen(fragment: Fragment): Builder {
            screens.clear()
            screens.add(fragment)
            return this
        }

        fun newRootScreenChain(fragments: List<Fragment>): Builder {
            screens.clear()
            screens.addAll(fragments)
            return this
        }

        fun newRootScreenChain(vararg fragments: Fragment): Builder {
            screens.clear()
            screens.addAll(fragments)
            return this
        }

        fun show(
            @IdRes containerId: Int,
            fragmentManager: FragmentManager,
            tag: String? = null
        ): Builder {
            this.containerId = containerId
            this.fragmentManager = fragmentManager
            this.tag = tag
            return this
        }

        /**
         * The tag will not be used if you do not call show()
         * */
        fun setTag(tag: String): Builder {
            this.tag = tag
            return this
        }

        fun build(): FragmentNavigationController {
            val args = Bundle()
            args.putSerializable(SCREENS, screens)

            val fragmentNavigationController = FragmentNavigationController()
            fragmentNavigationController.arguments = args

            if (containerId != -1 && fragmentManager != null) {
                fragmentManager!!.beginTransaction()
                    .replace(
                        containerId, fragmentNavigationController,
                        tag ?: fragmentNavigationController.toString()
                    )
                    .addToBackStack(tag)
                    .commit()
            }
            return fragmentNavigationController
        }

        companion object {
            const val SCREENS = "screens"
        }
    }
}