package com.sahay.ai.network

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class SupabaseCheckIn(
    @SerializedName("case_id") val caseId: String,
    @SerializedName("assessment_period_id") val assessmentPeriodId: String,
    @SerializedName("assessment_day") val assessmentDay: String,
    @SerializedName("score") val score: Int,
    @SerializedName("answers") val answers: Map<String, String>,
    @SerializedName("mood_level") val moodLevel: Int? = null,
    @SerializedName("fear_level") val fearLevel: Int? = null,
    @SerializedName("safety_level") val safetyLevel: Int? = null
)

data class SupabaseCase(
    @SerializedName("case_id") val caseId: String,
    @SerializedName("status") val status: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("department") val department: String,
    @SerializedName("support_category") val supportCategory: String,
    @SerializedName("official_note") val officialNote: String?,
    @SerializedName("assessment_progress_days") val assessmentProgressDays: Int,
    @SerializedName("current_distress_score") val currentDistressScore: Int,
    @SerializedName("final_weekly_score") val finalWeeklyScore: Int?,
    @SerializedName("score_status") val scoreStatus: String
)

data class SupabaseProfile(
    @SerializedName("id") val id: String? = null,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("preferred_language") val preferredLanguage: String? = null,
    @SerializedName("consent_given") val consentGiven: Boolean? = null,
    @SerializedName("consent_given_at") val consentGivenAt: String? = null,
    @SerializedName("trusted_person_mobile") val trustedPersonMobile: String? = null
)

data class SupabaseDistressAlert(
    @SerializedName("id") val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("final_score") val finalScore: Int,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("status") val status: String
)

// RPC Requests
data class ValidateCaseRequest(@SerializedName("p_case_id") val caseId: String)

data class RegisterVictimRequest(
    @SerializedName("p_case_id") val caseId: String,
    @SerializedName("p_name") val name: String,
    @SerializedName("p_phone") val phone: String,
    @SerializedName("p_trusted_phone") val trustedPhone: String,
    @SerializedName("p_language") val language: String,
    @SerializedName("p_consent_given") val consentGiven: Boolean
)

data class RecoverSessionRequest(
    @SerializedName("p_case_id") val caseId: String,
    @SerializedName("p_phone") val phone: String
)

data class DashboardRequest(@SerializedName("p_session_token") val sessionToken: String)

data class SubmitCheckInRpcRequest(
    @SerializedName("p_session_token") val sessionToken: String,
    @SerializedName("p_score") val score: Int,
    @SerializedName("p_answers") val answers: Map<String, String>
)

data class RpcResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String? = null,
    @SerializedName("session_token") val sessionToken: String? = null
)

data class VictimDashboardResponse(
    @SerializedName("profile") val profile: SupabaseProfile?,
    @SerializedName("case_record") val caseRecord: SupabaseCase?
)
