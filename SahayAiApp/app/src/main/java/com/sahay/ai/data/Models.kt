package com.sahay.ai.data

enum class CaseStatus(val display: String) {
    CREATED("Created"),
    REVIEWED("Reviewed"),
    TRIAGED("Triaged"),
    ASSIGNED("Assigned"),
    SUPPORT_INITIATED("Support Initiated"),
    FOLLOW_UP("Follow-up"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class DistressLevel { LOW, MODERATE, HIGH, CRITICAL }

fun getDistressLevel(score: Int): DistressLevel = when {
    score >= 75 -> DistressLevel.CRITICAL
    score >= 50 -> DistressLevel.HIGH
    score >= 25 -> DistressLevel.MODERATE
    else -> DistressLevel.LOW
}

data class UserProfile(
    val name: String,
    val phone: String,
    val caseId: String,
    var distressScore: Int,
    var caseStatus: CaseStatus,
    val trustedPersonMobile: String? = null
)

data class CheckIn(
    val caseId: String,
    val answers: Map<String, String>,
    val timestamp: Long,
    val score: Int
)

data class AIAnalysis(
    val summary: String,
    val recommendedSupportCategory: String,
    val escalationReason: String?,
    val isHumanReviewRecommended: Boolean,
    val emotionalDistressLevel: Double = 0.0,
    val fearLevel: Double = 0.0,
    val feelingUnsafe: Boolean = false,
    val urgency: String = "low"
)

data class CaseRecord(
    val caseId: String,
    val profile: UserProfile,
    var distressScore: Int,
    var status: CaseStatus,
    val checkIns: MutableList<CheckIn> = mutableListOf(),
    var latestAnalysis: AIAnalysis? = null,
    val history: MutableList<String> = mutableListOf("Case Created"),
    
    var priority: String = "Normal",
    var department: String = "Unassigned",
    var supportCategory: String = "Pending",
    var region: String = "North District",
    var alertStatus: String = "Normal",
    var officialNote: String = "",
    var nextFollowUpDate: Long? = null
)

data class RegionData(
    val regionName: String,
    val totalCases: Int,
    val activeCases: Int,
    val flaggedCases: Int,
    val avgScore: Int
)

data class TrustedContact(
    val name: String,
    val phone: String,
    val useForSupport: Boolean
)

enum class InputMode {
    CHAT,
    VOICE
}

data class ChatMessage(
    val sender: String,
    val message: String,
    val inputMode: InputMode = InputMode.CHAT
)
