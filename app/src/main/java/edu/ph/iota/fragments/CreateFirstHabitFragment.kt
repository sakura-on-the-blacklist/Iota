package edu.ph.iota.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import edu.ph.iota.R
import edu.ph.iota.databinding.FragmentCreateFirstHabitBinding
import edu.ph.iota.viewmodels.HabitSettingViewModel

class CreateFirstHabitFragment : Fragment() {

    private var _binding: FragmentCreateFirstHabitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HabitSettingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateFirstHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreateFirstHabit.setOnClickListener {
            it.findNavController().navigate(R.id.action_createFirstHabit_to_habitTemplate)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}