package edu.ph.iota.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import edu.ph.iota.R
import edu.ph.iota.adapters.ReflectionsAdapter
import edu.ph.iota.databinding.FragmentPrevReflectionsBinding
import edu.ph.iota.models.Reflection

class PrevReflectionsFragment : Fragment() {

    private var _binding: FragmentPrevReflectionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrevReflectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseButton()
        setupPebbleClick()
        setupRecyclerView()
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener { view: View ->
            view.findNavController().navigateUp()
        }
    }

    private fun setupPebbleClick() {
        binding.pebbleView.setOnClickListener { view: View ->
        }
    }

    private fun setupRecyclerView() {
        val reflections = getSampleReflections()
        val adapter = ReflectionsAdapter(reflections)
        binding.recyclerViewReflections.adapter = adapter
    }

    private fun getSampleReflections(): List<Reflection> {
        return listOf(
            Reflection(
                id = 1,
                content = "i wanted to open my phone just to check the notifications... but i decided to wash my face anyway! it was hard but that felt great",
                date = "April 1, 2026"
            ),
            Reflection(
                id = 2,
                content = "i wanted to open my phone just to check the notifications... but i decided to wash my face anyway! it was hard but that felt great",
                date = "April 2, 2026"
            ),
            Reflection(
                id = 3,
                content = "i wanted to open my phone just to check the notifications... but i decided to wash my face anyway! it was hard but that felt great",
                date = "April 3, 2026"
            ),
            Reflection(
                id = 4,
                content = "its getting kinda easier now to go straight wash my face without looking at my phone first.",
                date = "April 4, 2026"
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}