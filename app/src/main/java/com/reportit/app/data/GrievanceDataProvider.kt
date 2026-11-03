package com.reportit.app.data

import com.reportit.app.R
import com.reportit.app.data.model.GrievanceReport
import com.reportit.app.data.model.ReportSeverity
import com.reportit.app.data.model.ReportStatus
import com.reportit.app.data.model.TimelineEvent

object GrievanceDataProvider {

    fun getInitialReports(): MutableList<GrievanceReport> {
        return mutableListOf(
            GrievanceReport(
                id = "RI-2026-004821",
                title = "Deep Road Pothole and Asphalt Depression",
                description = "Large pothole approximately 2 meters wide near Bellandur Junction. Severe risk of two-wheeler skidding and heavy vehicular slowdown.",
                category = "Roads & Infrastructure",
                department = "BBMP Road Infrastructure Division",
                wardName = "Ward 150 - Bellandur, Mahadevapura",
                locationAddress = "Outer Ring Road, near Bellandur Lake Signal, Bengaluru",
                latitude = 12.9260,
                longitude = 77.6762,
                severity = ReportSeverity.HIGH,
                status = ReportStatus.IN_PROGRESS,
                slaTotalHours = 48,
                slaRemainingHours = 18,
                submissionDate = "02 May 2026, 10:30 AM",
                aiConfidence = 96,
                originalImageResId = R.drawable.img_pothole_evidence,
                resolvedImageResId = R.drawable.img_resolved_pothole,
                assignedOfficer = "Eng. Suresh K., BBMP Junior Engineer",
                assignedCrew = "Zonal Asphalt Quick-Patch Team 4 (Truck KA-01-EA-4921)",
                upvotesCount = 34,
                isUserUpvoted = false,
                timeline = mutableListOf(
                    TimelineEvent(
                        stageTitle = "Grievance Logged",
                        timestamp = "02 May 2026, 10:30 AM",
                        description = "Citizen submitted geo-tagged report with camera proof.",
                        actor = "Citizen Reporter",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "AI Triage & Verification",
                        timestamp = "02 May 2026, 10:31 AM",
                        description = "EfficientNet-B0 identified high-severity asphalt fracture (96% confidence).",
                        actor = "Report-It AI Engine",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Departmental Assignment",
                        timestamp = "02 May 2026, 11:15 AM",
                        description = "Assigned to BBMP Ward 150 Road Infrastructure Division.",
                        actor = "Central Control Room",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Field Work In Progress",
                        timestamp = "03 May 2026, 08:45 AM",
                        description = "Asphalt compactor and patch material dispatched to site.",
                        actor = "Eng. Suresh K. (Field Crew 4)",
                        isCompleted = false,
                        isCurrent = true
                    ),
                    TimelineEvent(
                        stageTitle = "Resolution & 2-Way Closure",
                        timestamp = "Pending",
                        description = "Awaiting post-repair photo submission and citizen confirmation.",
                        actor = "Citizen Verification Gate",
                        isCompleted = false,
                        isCurrent = false
                    )
                )
            ),

            GrievanceReport(
                id = "RI-2026-004815",
                title = "Commercial Solid Waste Dumping on Footpath",
                description = "Unattended commercial waste and plastic bags overflowing from roadside containers, blocking pedestrian access.",
                category = "Solid Waste Management",
                department = "BBMP Solid Waste Management (SWM)",
                wardName = "Ward 174 - HSR Layout, Bommanahalli",
                locationAddress = "14th Main Road, Sector 3, HSR Layout, Bengaluru",
                latitude = 12.9116,
                longitude = 77.6389,
                severity = ReportSeverity.MEDIUM,
                status = ReportStatus.RESOLVED,
                slaTotalHours = 24,
                slaRemainingHours = 0,
                submissionDate = "01 May 2026, 03:15 PM",
                aiConfidence = 93,
                originalImageResId = R.drawable.img_garbage_dump,
                resolvedImageResId = R.drawable.img_resolved_pothole,
                assignedOfficer = "Inspector Ramesh N., Zonal SWM Supervisor",
                assignedCrew = "BBMP Compact Truck Crew #12",
                upvotesCount = 19,
                isUserUpvoted = true,
                timeline = mutableListOf(
                    TimelineEvent(
                        stageTitle = "Grievance Logged",
                        timestamp = "01 May 2026, 03:15 PM",
                        description = "Citizen submitted geo-tagged report.",
                        actor = "Citizen Reporter",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "AI Triage & Verification",
                        timestamp = "01 May 2026, 03:16 PM",
                        description = "Automated classification confirmed illegal waste accumulation.",
                        actor = "Report-It AI Engine",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Departmental Routing",
                        timestamp = "01 May 2026, 03:40 PM",
                        description = "Assigned to Bommanahalli SWM Sanitation Division.",
                        actor = "Central Control Room",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Site Clearance Completed",
                        timestamp = "02 May 2026, 07:30 AM",
                        description = "Waste collected, area disinfected with bleaching powder.",
                        actor = "Inspector Ramesh N.",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Citizen Verification Gate",
                        timestamp = "02 May 2026, 08:00 AM",
                        description = "Marked resolved by municipal crew. Citizen validation required.",
                        actor = "You (Citizen Reporter)",
                        isCompleted = false,
                        isCurrent = true
                    )
                )
            ),

            GrievanceReport(
                id = "RI-2026-004792",
                title = "Main Drinking Water Pipeline Burst",
                description = "High-pressure clean water leakage from underground distribution main. Severe road flooding and water loss.",
                category = "Water & Sanitation",
                department = "Bangalore Water Supply & Sewerage Board (BWSSB)",
                wardName = "Ward 150 - Bellandur, Mahadevapura",
                locationAddress = "Kasavanahalli Main Road, near Central Jail Road Junction",
                latitude = 12.9080,
                longitude = 77.6740,
                severity = ReportSeverity.CRITICAL,
                status = ReportStatus.ESCALATED,
                slaTotalHours = 12,
                slaRemainingHours = -4,
                submissionDate = "29 Apr 2026, 11:20 AM",
                aiConfidence = 98,
                originalImageResId = R.drawable.img_water_leakage,
                resolvedImageResId = null,
                assignedOfficer = "Assistant Executive Engineer Prakash G., BWSSB South-East",
                assignedCrew = "BWSSB Emergency Pipeline Valve Crew",
                upvotesCount = 52,
                isUserUpvoted = false,
                timeline = mutableListOf(
                    TimelineEvent(
                        stageTitle = "Grievance Logged",
                        timestamp = "29 Apr 2026, 11:20 AM",
                        description = "Critical pipeline rupture logged by citizen.",
                        actor = "Citizen Reporter",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "AI Triage & Verification",
                        timestamp = "29 Apr 2026, 11:21 AM",
                        description = "High-priority flood hazard flagged by computer vision.",
                        actor = "Report-It AI Engine",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Emergency Dispatch",
                        timestamp = "29 Apr 2026, 11:45 AM",
                        description = "Valve shutoff order issued to BWSSB zonal team.",
                        actor = "BWSSB Control Desk",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "SLA Target Breached",
                        timestamp = "30 Apr 2026, 11:20 AM",
                        description = "12-hour statutory deadline exceeded. Automatic escalation flag raised to Chief Engineer.",
                        actor = "Automated SLA Monitor",
                        isCompleted = false,
                        isCurrent = true
                    )
                )
            ),

            GrievanceReport(
                id = "RI-2026-004780",
                title = "Broken Streetlight Pole and Exposed Wires",
                description = "Streetlight fixture knocked down by vehicle impact. Dangling electrical wires creating night accident hazard.",
                category = "Electrical & Lighting",
                department = "Bangalore Electricity Supply Company (BESCOM)",
                wardName = "Ward 160 - Koramangala, South Zone",
                locationAddress = "80 Feet Road, 4th Block, Koramangala, Bengaluru",
                latitude = 12.9340,
                longitude = 77.6250,
                severity = ReportSeverity.HIGH,
                status = ReportStatus.ASSIGNED,
                slaTotalHours = 24,
                slaRemainingHours = 14,
                submissionDate = "02 May 2026, 06:10 AM",
                aiConfidence = 91,
                originalImageResId = R.drawable.img_streetlight_broken,
                resolvedImageResId = null,
                assignedOfficer = "Section Officer Anand V., BESCOM Koramangala Sub-Division",
                assignedCrew = "BESCOM Line Maintenance Van #6",
                upvotesCount = 14,
                isUserUpvoted = false,
                timeline = mutableListOf(
                    TimelineEvent(
                        stageTitle = "Grievance Logged",
                        timestamp = "02 May 2026, 06:10 AM",
                        description = "Report logged with electrical hazard warning.",
                        actor = "Citizen Reporter",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "AI Triage & Verification",
                        timestamp = "02 May 2026, 06:11 AM",
                        description = "Classified under BESCOM Electrical Safety Category.",
                        actor = "Report-It AI Engine",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Work Order Created",
                        timestamp = "02 May 2026, 07:00 AM",
                        description = "Dispatched to Line Maintenance Sub-station.",
                        actor = "BESCOM Central Desk",
                        isCompleted = true,
                        isCurrent = true
                    )
                )
            ),

            GrievanceReport(
                id = "RI-2026-004765",
                title = "Asphalt Patch and Pothole Leveling Completed",
                description = "Repaired deep pothole on Sarjapur Outer Ring Road service lane.",
                category = "Roads & Infrastructure",
                department = "BBMP Road Infrastructure Division",
                wardName = "Ward 150 - Bellandur, Mahadevapura",
                locationAddress = "Sarjapur Road, near Iblur Junction, Bengaluru",
                latitude = 12.9180,
                longitude = 77.6680,
                severity = ReportSeverity.MEDIUM,
                status = ReportStatus.CITIZEN_CLOSED,
                slaTotalHours = 48,
                slaRemainingHours = 0,
                submissionDate = "24 Apr 2026, 09:15 AM",
                aiConfidence = 95,
                originalImageResId = R.drawable.img_pothole_evidence,
                resolvedImageResId = R.drawable.img_resolved_pothole,
                assignedOfficer = "Eng. Suresh K., BBMP Junior Engineer",
                assignedCrew = "Zonal Asphalt Quick-Patch Team 4",
                upvotesCount = 41,
                isUserUpvoted = true,
                timeline = mutableListOf(
                    TimelineEvent(
                        stageTitle = "Grievance Logged",
                        timestamp = "24 Apr 2026, 09:15 AM",
                        description = "Initial report submitted.",
                        actor = "Citizen Reporter",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Site Work Completed",
                        timestamp = "25 Apr 2026, 04:30 PM",
                        description = "Hot bitumen mix laid and leveled with roller.",
                        actor = "Field Crew 4",
                        isCompleted = true,
                        isCurrent = false
                    ),
                    TimelineEvent(
                        stageTitle = "Citizen Verified & Closed",
                        timestamp = "26 Apr 2026, 10:00 AM",
                        description = "Citizen verified on-ground completion. +15 Civic Score awarded.",
                        actor = "You (Citizen Reporter)",
                        isCompleted = true,
                        isCurrent = true
                    )
                )
            )
        )
    }
}
