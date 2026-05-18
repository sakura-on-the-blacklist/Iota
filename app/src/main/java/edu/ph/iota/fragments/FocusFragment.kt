package edu.ph.iota.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.ph.iota.R

class FocusFragment : Fragment() {

    private lateinit var projectListContainer: LinearLayout
    private lateinit var mainPrefs: SharedPreferences
    private var projects: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.pomodoro_main_activity, container, false)

        projectListContainer = view.findViewById(R.id.pomodoro_main_project_list)
        mainPrefs = requireContext().getSharedPreferences("PomodoroMainPrefs", Context.MODE_PRIVATE)

        view.findViewById<View>(R.id.pomodoro_main_add_btn).setOnClickListener {
            showAddProjectDialog()
        }

        loadProjects()
        return view
    }

    private fun loadProjects() {
        projectListContainer.removeAllViews()

        val projectSet =
            mainPrefs.getStringSet("project_list", emptySet()) ?: emptySet()
        projects = projectSet.toMutableList()

        for (project in projects) {
            createProjectView(project)
        }
    }

    private fun createProjectView(projectName: String) {
        val itemView = layoutInflater.inflate(R.layout.pomodoro_project_item, projectListContainer, false)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = (5 * resources.displayMetrics.density).toInt()
        params.setMargins(0, 0, 0, margin)
        itemView.layoutParams = params

        val nameTv = itemView.findViewById<TextView>(R.id.project_name_tv)
        nameTv.text = projectName

        // Open detail fragment instead of starting an Activity
        itemView.setOnClickListener {
            openProjectDetail(projectName)
        }

        val editBtn = itemView.findViewById<View>(R.id.project_edit_btn)
        editBtn.setOnClickListener {
            showEditMenu(projectName)
        }

        projectListContainer.addView(itemView)
    }

    private fun openProjectDetail(projectName: String) {
        val fragment = FocusDetailFragment().apply {
            arguments = Bundle().apply {
                putString("PROJECT_NAME", projectName)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showEditMenu(oldName: String) {
        val options = arrayOf("Rename", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Project")
            .setItems(options) { _, which ->
                if (which == 0) {
                    showRenameDialog(oldName)
                } else {
                    deleteProject(oldName)
                }
            }
            .show()
    }

    private fun showRenameDialog(oldName: String) {
        val input = EditText(requireContext()).apply {
            setText(oldName)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Rename Project")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != oldName) {
                    projects.remove(oldName)
                    projects.add(newName)
                    saveAndRefresh()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProject(name: String) {
        projects.remove(name)
        saveAndRefresh()
    }

    private fun showAddProjectDialog() {
        val input = EditText(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle("New Project")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    projects.add(name)
                    saveAndRefresh()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveAndRefresh() {
        mainPrefs.edit()
            .putStringSet("project_list", projects.toSet())
            .apply()
        loadProjects()
    }
}