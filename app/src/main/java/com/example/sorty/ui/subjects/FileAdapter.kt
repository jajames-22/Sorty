package com.example.sorty.ui.subjects

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.R
import com.example.sorty.data.models.SubjectFile // 👈 IMPORTANT: Import the renamed model

class FileAdapter(
    private var fileList: List<SubjectFile>, // 👈 Use SubjectFile here
    private val onFileClicked: (SubjectFile) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFileName: TextView = itemView.findViewById(R.id.tv_file_name)
        val ivFileIcon: ImageView = itemView.findViewById(R.id.iv_file_icon)

        fun bind(file: SubjectFile) {
            tvFileName.text = file.name

            // 👇 Logic to pick specific icons based on file type
            if (file.type.contains("image", ignoreCase = true)) {
                // Shows Image Icon
                ivFileIcon.setImageResource(R.drawable.baseline_image_24)
            } else if (file.type.contains("pdf", ignoreCase = true)) {
                // Shows PDF Icon
                ivFileIcon.setImageResource(R.drawable.baseline_picture_as_pdf_24)
            } else {
                // Shows Generic File Icon (Default)
                ivFileIcon.setImageResource(R.drawable.baseline_insert_drive_file_24)
            }

            itemView.setOnClickListener { onFileClicked(file) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(fileList[position])
    }

    override fun getItemCount(): Int = fileList.size

    fun updateFiles(newFiles: List<SubjectFile>) { // 👈 Use SubjectFile here
        fileList = newFiles
        notifyDataSetChanged()
    }

    // Inside FileAdapter class

    // 👇 Add this helper function
    fun getFileAt(position: Int): SubjectFile {
        return fileList[position]
    }
}