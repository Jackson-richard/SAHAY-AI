package com.sahay.ai.data

object DemoData {
    // Exact initial states as requested
    // 1. Low-distress (Case 1)
    // 2. Moderate-distress (Case 2)
    // 3. High-distress (Case 3)
    // 4. Flagged case requiring review (Case 4)
    // 5. Case currently in follow-up (Case 5)
    
    val profiles = mutableListOf(
        UserProfile("Ananya Sharma", "9000000001", "SHY-1001", 20, CaseStatus.CREATED),
        UserProfile("Rahul Kumar", "9000000002", "SHY-1002", 50, CaseStatus.CREATED),
        UserProfile("Meera Joseph", "9000000003", "SHY-1003", 70, CaseStatus.TRIAGED),
        UserProfile("Arjun Raj", "9000000004", "SHY-1004", 85, CaseStatus.REVIEWED),
        UserProfile("Kavya Menon", "9000000005", "SHY-1005", 35, CaseStatus.FOLLOW_UP)
    )
    
    val cases = mutableMapOf<String, CaseRecord>()
    
    val regions = listOf("North District", "South District", "East Valley", "West Coast", "Central")
    
    init {
        val regionDistribution = listOf(
            "North District", "South District", "North District", "Central", "West Coast"
        )
        
        profiles.forEachIndexed { index, profile ->
            val isFlagged = profile.distressScore > 75
            
            val analysis = AIAnalysis(
                summary = "Initial check-in analyzed. Overall distress is ${profile.distressScore}.",
                recommendedSupportCategory = if (isFlagged) "Emergency human review" else "Self-care",
                escalationReason = if (isFlagged) "Distress score exceeded 75 threshold." else null,
                isHumanReviewRecommended = isFlagged
            )
            
            cases[profile.caseId] = CaseRecord(
                caseId = profile.caseId,
                profile = profile,
                distressScore = profile.distressScore,
                status = profile.caseStatus,
                latestAnalysis = analysis,
                region = regionDistribution[index],
                priority = if (isFlagged) "Urgent" else "Normal",
                alertStatus = if (isFlagged) "Flagged" else "Normal",
                department = if (profile.caseStatus == CaseStatus.FOLLOW_UP) "Follow-up Team" else "Unassigned",
                supportCategory = if (profile.caseStatus == CaseStatus.FOLLOW_UP) "Counselling" else "Pending"
            )
        }
    }
}
