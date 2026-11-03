package com.reportit.app.ui.admin

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reportit.app.R
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.databinding.ItemAdminTriageBinding
import com.reportit.app.utils.ImageUtils

class AdminTriageAdapter(
    private val onAssignCrew: (GrievanceReport) -> Unit,
    private val onMarkResolved: (GrievanceReport) -> Unit
) : ListAdapter<GrievanceReport, AdminTriageAdapter.TriageViewHolder>(AdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TriageViewHolder {
        val binding = ItemAdminTriageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TriageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TriageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TriageViewHolder(
        private val binding: ItemAdminTriageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: GrievanceReport) {
            binding.tvAdminReportId.text = "${report.id} • ${report.category}"
            binding.tvAdminLocation.text = "${report.wardName} (${report.locationAddress})"
            binding.tvAdminDescription.text = report.description.ifEmpty { "Civic grievance reported via mobile camera." }
            binding.tvAdminAiBadge.text = "⚡ AI Verified: ${report.aiConfidence}%"
            binding.tvAdminReporter.text = "by ${report.reportedBy}"

            // Status Badge
            binding.tvAdminStatusBadge.text = report.status.label
            binding.tvAdminStatusBadge.setTextColor(Color.parseColor(report.status.colorHex))
            binding.tvAdminStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.status.bgHex))

            // Real Photo or Drawable Fallback
            if (!report.imageBase64.isNullOrEmpty()) {
                val bitmap = ImageUtils.base64ToBitmap(report.imageBase64)
                if (bitmap != null) {
                    binding.ivAdminThumb.setImageBitmap(bitmap)
                } else {
                    binding.ivAdminThumb.setImageResource(report.originalImageResId)
                }
            } else {
                binding.ivAdminThumb.setImageResource(report.originalImageResId)
            }

            // Crew Assignment Banner
            if (!report.assignedCrew.isNullOrEmpty()) {
                binding.tvAdminCrewAssigned.visibility = View.VISIBLE
                binding.tvAdminCrewAssigned.text = "👷 Assigned to: ${report.assignedCrew}"
            } else if (!report.assignedOfficer.isNullOrEmpty()) {
                binding.tvAdminCrewAssigned.visibility = View.VISIBLE
                binding.tvAdminCrewAssigned.text = "👷 Assigned Officer: ${report.assignedOfficer}"
            } else {
                binding.tvAdminCrewAssigned.visibility = View.GONE
            }

            // Button actions
            if (report.status == ReportStatus.RESOLVED || report.status == ReportStatus.CITIZEN_CLOSED) {
                binding.btnAdminAssign.isEnabled = false
                binding.btnAdminAssign.text = "Completed"
                binding.btnAdminResolve.visibility = View.GONE
            } else {
                binding.btnAdminAssign.isEnabled = true
                binding.btnAdminAssign.text = if (report.assignedCrew != null) "👷 Re-Assign" else "👷 Assign Crew"
                binding.btnAdminResolve.visibility = View.VISIBLE

                binding.btnAdminAssign.setOnClickListener {
                    onAssignCrew(report)
                }
                binding.btnAdminResolve.setOnClickListener {
                    onMarkResolved(report)
                }
            }
        }
    }

    class AdminDiffCallback : DiffUtil.ItemCallback<GrievanceReport>() {
        override fun areItemsTheSame(oldItem: GrievanceReport, newItem: GrievanceReport): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GrievanceReport, newItem: GrievanceReport): Boolean =
            oldItem == newItem
    }
}
