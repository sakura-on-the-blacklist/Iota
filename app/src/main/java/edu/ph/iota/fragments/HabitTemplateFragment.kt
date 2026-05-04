package edu.ph.iota.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.ph.iota.R
import edu.ph.iota.databinding.FragmentHabitTemplateBinding

class HabitTemplateFragment : Fragment() {

    private var _binding: FragmentHabitTemplateBinding? = null
    private val binding get() = _binding!!

    private var longPressHandler: Handler? = null
    private var longPressRunnable: Runnable? = null
    private var orangeOverlay: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            showCircularOrangeTransition()
        }

        setupPebbleLongPress()
    }

    private fun setupPebbleLongPress() {
        val pebbleView = binding.pebbleView

        longPressHandler = Handler(Looper.getMainLooper())
        longPressRunnable = Runnable {
            showCircularOrangeTransition()
        }

        pebbleView.setOnLongClickListener { true }

        pebbleView.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    longPressHandler?.postDelayed(longPressRunnable!!, 3000)
                    pebbleView.alpha = 0.7f
                    true
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    longPressHandler?.removeCallbacks(longPressRunnable!!)
                    pebbleView.alpha = 1.0f
                    true
                }
                else -> false
            }
        }
    }

    private fun showCircularOrangeTransition() {
        orangeOverlay = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#FF8903"))
            alpha = 0f
        }

        val rootView = requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(orangeOverlay)

        val pebbleView = binding.pebbleView
        val location = IntArray(2)
        pebbleView.getLocationOnScreen(location)

        val rootLocation = IntArray(2)
        binding.root.getLocationOnScreen(rootLocation)

        val centerX = location[0] + pebbleView.width / 2 - rootLocation[0]
        val centerY = location[1] + pebbleView.height / 2 - rootLocation[1]

        val maxRadius = Math.hypot(rootView.width.toDouble(), rootView.height.toDouble()).toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            orangeOverlay?.alpha = 1f
            val anim = ViewAnimationUtils.createCircularReveal(
                orangeOverlay!!,
                centerX,
                centerY,
                0f,
                maxRadius.toFloat()
            )
            anim.duration = 400
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    navigateAndCleanup()
                }
            })
            anim.start()
        } else {
            orangeOverlay?.animate()
                ?.alpha(1f)
                ?.setDuration(300)
                ?.withEndAction {
                    navigateAndCleanup()
                }
                ?.start()
        }
    }

    private fun navigateAndCleanup() {
        try {
            findNavController().navigate(R.id.habitSettingsActivity)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            orangeOverlay?.animate()
                ?.alpha(0f)
                ?.setDuration(200)
                ?.withEndAction {
                    (orangeOverlay?.parent as? ViewGroup)?.removeView(orangeOverlay)
                    orangeOverlay = null
                }
                ?.start()
        }, 300)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        longPressHandler?.removeCallbacks(longPressRunnable!!)
        longPressHandler = null
        longPressRunnable = null
        orangeOverlay?.let {
            (it.parent as? ViewGroup)?.removeView(it)
            orangeOverlay = null
        }
        _binding = null
    }
}