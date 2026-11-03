package com.reportit.app.ui.reports

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reportit.app.R
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.databinding.ItemGrievanceReportBinding

class GrievanceReportAdapter(
    private val onItemClick: (GrievanceReport) -> Unit,
    private val onUpvoteClick: (GrievanceReport) -> Unit
) : ListAdapter<GrievanceReport, GrievanceReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemGrievanceReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(
        private val binding: ItemGrievanceReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: GrievanceReport) {
            binding.tvReportId.text = report.id
            binding.tvReportWard.text = report.wardName
            binding.tvReportTitle.text = report.title
            binding.tvReportLocation.text = report.locationAddress
            // Real Image from ImgBB Cloud, Base64, or Resource Fallback
            val cloudUrl = report.thumbUrl ?: report.imageUrl
            if (!cloudUrl.isNullOrEmpty()) {
                com.reportit.app.utils.ImageUtils.loadUrlToImageView(
                    binding.ivReportThumbnail,
                    cloudUrl,
                    report.originalImageResId
                )
            } else if (!report.imageBase64.isNullOrEmpty()) {
                val bitmap = com.reportit.app.utils.ImageUtils.base64ToBitmap(report.imageBase64)
                if (bitmap != null) {
                    binding.ivReportThumbnail.setImageBitmap(bitmap)
                } else {
                    binding.ivReportThumbnail.setImageResource(report.originalImageResId)
                }
            } else {
                binding.ivReportThumbnail.setImageResource(report.originalImageResId)
            }
            binding.tvSubmissionDate.text = report.submissionDate.split(",").firstOrNull() ?: report.submissionDate

            // Status Badge
            binding.tvStatusBadge.text = report.status.label
            binding.tvStatusBadge.setTextColor(Color.parseColor(report.status.colorHex))
            binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.status.bgHex))

            // Severity Badge
            binding.tvSeverityBadge.text = report.severity.label
            binding.tvSeverityBadge.setTextColor(Color.parseColor(report.severity.colorHex))
            binding.tvSeverityBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(report.severity.bgHex))

            // Department Color Styling
            val deptColorHex = when {
                report.department.contains("Road", ignoreCase = true) -> "#EA580C"
                report.department.contains("Waste", ignoreCase = true) -> "#16A34A"
                report.department.contains("BWSSB", ignoreCase = true) || report.department.contains("Water", ignoreCase = true) -> "#0284C7"
                report.department.contains("BESCOM", ignoreCase = true) || report.department.contains("Lighting", ignoreCase = true) -> "#9333EA"
                else -> "#006494"
            }
            val deptBgHex = when {
                report.department.contains("Road", ignoreCase = true) -> "#FFF7ED"
                report.department.contains("Waste", ignoreCase = true) -> "#F0FDF4"
                report.department.contains("BWSSB", ignoreCase = true) || report.department.contains("Water", ignoreCase = true) -> "#F0F9FF"
                report.department.contains("BESCOM", ignoreCase = true) || report.department.contains("Lighting", ignoreCase = true) -> "#FAF5FF"
                else -> "#E0F2FE"
            }
            binding.tvReportDepartment.setTextColor(Color.parseColor(deptColorHex))
            binding.tvReportDepartment.backgroundTintList = ColorStateList.valueOf(Color.parseColor(deptBgHex))

            // SLA Countdown State
            when {
                report.status == ReportStatus.CITIZEN_CLOSED -> {
                    binding.tvSlaCountdown.text = "Resolved & Verified"
                    binding.tvSlaCountdown.setTextColor(Color.parseColor("#059669"))
                    binding.ivSlaClock.imageTintList = ColorStateList.valueOf(Color.parseColor("#059669"))
                    binding.ivSlaClock.setImageResource(R.drawable.ic_check_circle)
                }
                report.status == ReportStatus.RESOLVED -> {
                    binding.tvSlaCountdown.text = "Pending Citizen Confirmation"
                    binding.tvSlaCountdown.setTextColor(Color.parseColor("#D97706"))
                    binding.ivSlaClock.imageTintList = ColorStateList.valueOf(Color.parseColor("#D97706"))
                    binding.ivSlaClock.setImageResource(R.drawable.ic_hourglass_empty)
                }
                report.status == ReportStatus.ESCALATED || report.slaRemainingHours < 0 -> {
                    val breachHours = kotlin.math.abs(report.slaRemainingHours)
                    binding.tvSlaCountdown.text = "SLA Breached by ${breachHours}h - Escalated"
                    binding.tvSlaCountdown.setTextColor(Color.parseColor("#DC2626"))
                    binding.ivSlaClock.imageTintList = ColorStateList.valueOf(Color.parseColor("#DC2626"))
                    binding.ivSlaClock.setImageResource(R.drawable.ic_warning)
                }
                else -> {
                    binding.tvSlaCountdown.text = "SLA: ${report.slaRemainingHours}h remaining"
                    binding.tvSlaCountdown.setTextColor(Color.parseColor("#2563EB"))
                    binding.ivSlaClock.imageTintList = ColorStateList.valueOf(Color.parseColor("#2563EB"))
                    binding.ivSlaClock.setImageResource(R.drawable.ic_schedule)
                }
            }

            // Upvote UI
            binding.tvUpvoteCount.text = report.upvotesCount.toString()
            if (report.isUserUpvoted) {
                binding.ivUpvote.imageTintList = ColorStateList.valueOf(Color.parseColor("#006494"))
                binding.tvUpvoteCount.setTextColor(Color.parseColor("#006494"))
            } else {
                binding.ivUpvote.imageTintList = ColorStateList.valueOf(Color.parseColor("#64748B"))
                binding.tvUpvoteCount.setTextColor(Color.parseColor("#64748B"))
            }

            binding.layoutUpvote.setOnClickListener {
                onUpvoteClick(report)
            }

            binding.root.setOnClickListener {
                onItemClick(report)
            }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<GrievanceReport>() {
        override fun areItemsTheSame(oldItem: GrievanceReport, newItem: GrievanceReport): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GrievanceReport, newItem: GrievanceReport): Boolean {
            return oldItem == newItem
        }
    }
}
