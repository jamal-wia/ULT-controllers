package com.navigation.ult_controllers

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import java.util.*

class ColorFragment : Fragment() {

    private val args by lazy { requireArguments() }
    private val ctx by lazy { requireContext() }

    private val colorArgs: Int by lazy { args.getInt(COLOR_ARG_KEY) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(ctx).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setBackgroundColor(colorArgs)
        }
    }

    companion object {

        private const val COLOR_ARG_KEY = "color"

        fun createInstance(color: Int = -1): ColorFragment {
            val color1 = if (color != -1) {
                Color.rgb(color, color, color)
            } else {
                val random = Random()
                Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
            }

            return ColorFragment().apply {
                arguments = bundleOf(COLOR_ARG_KEY to color1)
            }
        }
    }
}