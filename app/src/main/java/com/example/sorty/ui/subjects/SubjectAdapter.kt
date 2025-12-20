package com.example.sorty.ui.subjects

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.R
// 👇 CRITICAL: Explicitly import the correct data model
import com.example.sorty.data.models.Subject

class SubjectsAdapter(
    private var subjectList: List<Subject>,
    private val onClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSubjectName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvSubjectDesc)
        val imgFolder1: ImageView = itemView.findViewById(R.id.folder_icon1)
        val imgFolder2: ImageView = itemView.findViewById(R.id.folder_icon2)

        fun bind(subject: Subject) {
            tvName.text = subject.name
            tvDesc.text = subject.description

            try {
                val colorInt = Color.parseColor(subject.color)
                imgFolder1.setColorFilter(colorInt, PorterDuff.Mode.MULTIPLY)
                imgFolder2.setColorFilter(colorInt, PorterDuff.Mode.MULTIPLY)
            } catch (e: Exception) {
                val defaultColor = Color.parseColor("#4CAF50")
                imgFolder1.setColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY)
                imgFolder2.setColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY)
            }

            itemView.setOnClickListener { onClick(subject) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_folder, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size

    fun updateData(newSubjects: List<Subject>) {
        this.subjectList = newSubjects
        notifyDataSetChanged()
    }
}