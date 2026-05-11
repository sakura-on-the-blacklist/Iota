package edu.ph.iota.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import edu.ph.iota.R
import edu.ph.iota.adapters.ReflectionsAdapter
import edu.ph.iota.databinding.FragmentPrevReflectionsBinding
import edu.ph.iota.viewmodels.ReflectionViewModel

class PrevReflectionsFragment : Fragment() {

    private var _binding: FragmentPrevReflectionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReflectionViewModel by activityViewModels()

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
        setupObservers()

        // Load reflections from Firebase
        viewModel.loadAllReflections()
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener { view: View ->
            view.findNavController().navigateUp()
        }
    }

    private fun setupPebbleClick() {
        binding.pebbleView.setOnClickListener { view: View ->
            // Optional: Navigate to add reflection screen
            // view.findNavController().navigate(R.id.action_to_addReflection)
        }
    }

    private fun setupRecyclerView() {
        val adapter = ReflectionsAdapter(emptyList())
        binding.recyclerViewReflections.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.reflections.observe(viewLifecycleOwner) { reflections ->
            val adapter = ReflectionsAdapter(reflections)
            binding.recyclerViewReflections.adapter = adapter
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if you have one
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}