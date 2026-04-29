package edu.ph.iota.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.ph.iota.databinding.ItemReflectionBinding
import edu.ph.iota.models.Reflection

class ReflectionsAdapter(
    private val reflections: List<Reflection>
) : RecyclerView.Adapter<ReflectionsAdapter.ReflectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReflectionViewHolder {
        val binding = ItemReflectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReflectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReflectionViewHolder, position: Int) {
        holder.bind(reflections[position])
    }

    override fun getItemCount(): Int = reflections.size

    inner class ReflectionViewHolder(
        private val binding: ItemReflectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reflection: Reflection) {
            binding.textReflectionContent.text = reflection.content
            binding.textReflectionDate.text = reflection.date
        }
    }
}