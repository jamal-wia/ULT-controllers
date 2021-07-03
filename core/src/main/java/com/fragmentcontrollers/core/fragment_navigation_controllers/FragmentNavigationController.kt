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

    private val ignoreEqualArgs by lazy { args.getBoolean(Builder.IGNORE_EQUAL_KEY, false) }
    private val screenTagsArgs: ArrayList<String?> by lazy {
        args.getStringArrayList(Builder.SCREENS_TAGS_KEY) ?: arrayListOf()
    }
    private val screensArgs: ArrayList<Fragment> by lazy {
        val list = args[Builder.SCREENS_KEY] as? List<*>
        list?.map { it as Fragment } as? ArrayList ?: arrayListOf()
    }

    private val savedTransactions = arrayListOf<TransactionType>()
    private var isResume = false
    private var firstOnCreate = true

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
        firstOnCreate = savedInstanceState == null
        if (screensArgs.isNotEmpty()) {
            for (i in 0 until screensArgs.size) {
                childFragmentManager.beginTransaction()
                    .replace(navigationContainer.id, screensArgs[i], screenTagsArgs[i])
                    .addToBackStack(tag ?: screensArgs[i].toString())
                    .commit()
            }
        }
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
                is TransactionType.Forward -> goForward(it.fragment, it.tag)
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
        childFragmentManager.popBackStack()
        screensArgs.takeIf { it.isNotEmpty() }?.removeLast()
        screenTagsArgs.takeIf { it.isNotEmpty() }?.removeLast()
    }

    override fun goBack(): Boolean {
        if (!canGoBack()) return false
        if (!isResume) {
            savedTransactions.add(TransactionType.Back)
            return false
        }
        onBackPressed()
        return true
    }

    override fun replace(fragment: Fragment, tag: String?): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Replace(fragment, tag))
            return false
        }
        if (screensArgs.isEmpty()) {
            return goForward(fragment, null)
        }
        if (ignoreEqualArgs
            && tag != null
            && screenTagsArgs.contains(tag)
        ) {
            return false
        }
        childFragmentManager.beginTransaction()
            .replace(navigationContainer.id, fragment, tag)
            .also {
                screensArgs.takeIf { it.isNotEmpty() }?.removeLast()
                screensArgs.add(fragment)
                screenTagsArgs.takeIf { it.isNotEmpty() }?.removeLast()
                screenTagsArgs.add(tag)
            }
            .addToBackStack(tag ?: fragment.toString())
            .commit()
        return true
    }

    override fun goForward(fragment: Fragment, tag: String?): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Forward(fragment, tag))
            return false
        }
        if (ignoreEqualArgs
            && tag != null
            && screenTagsArgs.contains(tag)
        ) {
            return false
        }
        childFragmentManager.beginTransaction()
            .replace(navigationContainer.id, fragment, tag)
            .addToBackStack(tag ?: fragment.toString())
            .commit()
        screensArgs.add(fragment)
        screenTagsArgs.add(tag)
        return true
    }

    override fun goForwardChain(vararg fragments: Fragment, tag: String?): Boolean {
        fragments.forEach { goForward(it, tag) }
        return true
    }

    override fun reset(): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Reset)
            return false
        }
        val lastIndex = screensArgs.lastIndex
        for (index in lastIndex downTo 1) {
            goBack()
        }
        return true
    }

    override fun reset(fragment: Fragment, tag: String?): Boolean {
        if (!isResume) {
            savedTransactions.add(TransactionType.Reset)
            savedTransactions.add(TransactionType.Replace(fragment))
            return false
        }
        reset()
        replace(fragment)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        args.putSerializable(Builder.SCREENS_KEY, screensArgs.toList() as ArrayList)
        screensArgs.clear()
        args.putStringArrayList(Builder.SCREENS_TAGS_KEY, screenTagsArgs.toList() as ArrayList)
        screenTagsArgs.clear()
    }

    class Builder {

        private val screens by lazy { hashMapOf<String?, Fragment>() }
        private var tag: String? = null
        private var containerId = -1
        private var fragmentManager: FragmentManager? = null
        private var ignoreEqual = false

        fun addScreenToChain(fragment: Fragment, tag: String? = null): Builder {
            screens[tag] = fragment
            return this
        }

        fun addScreensToChain(fragments: List<Pair<String?, Fragment>>): Builder {
            screens.putAll(fragments)
            return this
        }

        fun addScreensToChain(vararg fragments: Pair<String?, Fragment>): Builder {
            screens.putAll(fragments)
            return this
        }

        fun newRootScreen(fragment: Fragment): Builder {
            screens.clear()
            screens[tag] = fragment
            return this
        }

        fun newRootScreenChain(fragments: List<Pair<String?, Fragment>>): Builder {
            screens.clear()
            screens.putAll(fragments)
            return this
        }

        fun newRootScreenChain(vararg fragments: Pair<String?, Fragment>): Builder {
            screens.clear()
            screens.putAll(fragments)
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
        fun changeTag(tag: String): Builder {
            this.tag = tag
            return this
        }

        /**
         * Ignore equal screen by tag, don't worked if tag = null
         * */
        fun ignoreEqualScreen(ignore: Boolean): Builder {
            this.ignoreEqual = ignore
            return this
        }

        fun build(): FragmentNavigationController {
            val args = Bundle()
            if (screens.isNotEmpty()) {
                args.putStringArrayList(
                    SCREENS_TAGS_KEY,
                    screens.keys.toList() as? ArrayList ?: arrayListOf()
                )
                args.putSerializable(SCREENS_KEY, screens.values.toList() as? ArrayList)
            }
            args.putBoolean(IGNORE_EQUAL_KEY, ignoreEqual)

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
            const val SCREENS_TAGS_KEY = "screens_tags"
            const val SCREENS_KEY = "screens"
            const val IGNORE_EQUAL_KEY = "ignore_equal"
        }
    }
}