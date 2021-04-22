package com.navigation.ult_controllers

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

class NumberFragment : Fragment(R.layout.fragment_number) {

    private val num by lazy { requireArguments()[NUM_KEY] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view as TextView
        view.text = "NumberFragment $num"
    }

    companion object {
        private const val NUM_KEY = "num"

        fun createInstance(num: Int): NumberFragment {
            return NumberFragment().apply {
                arguments = bundleOf(NUM_KEY to num)
            }
        }
    }
}