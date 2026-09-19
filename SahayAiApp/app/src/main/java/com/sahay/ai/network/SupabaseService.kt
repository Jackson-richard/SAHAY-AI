package com.sahay.ai.network

import com.sahay.ai.data.CaseRecord
import com.sahay.ai.data.CheckIn
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH

import com.sahay.ai.BuildConfig

object SupabaseConfig {
    val URL = BuildConfig.SUPABASE_URL
    val ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
}

data class ChatAnalysisRequest(
    val context: String,
    val message: String,
    val case_id: String,
    val assessment_period_id: String = "DEFAULT",
    val input_type: String = "CHAT"
)

data class ChatAnalysisResponse(
    val reply: String,
    val structured_indicators: Map<String, Any>
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val access_token: String,
    val user: Any?
)

object SessionManager {
    var accessToken: String? = null
    var victimSessionToken: String? = null
}

interface SupabaseService {
    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Body request: AuthRequest
    ): Response<AuthResponse>
    @POST("functions/v1/chat-analysis")
    suspend fun analyzeChat(
        @Body request: ChatAnalysisRequest,
        @Header("Authorization") auth: String,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<ChatAnalysisResponse>
    @GET("rest/v1/cases?select=*")
    suspend fun getCases(
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}"
    ): Response<List<SupabaseCase>>

    @GET("rest/v1/profiles?select=*")
    suspend fun getProfiles(
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}"
    ): Response<List<SupabaseProfile>>

    @GET("rest/v1/distress_alerts?select=*")
    suspend fun getAlerts(
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}"
    ): Response<List<SupabaseDistressAlert>>

    @POST("rest/v1/check_ins")
    suspend fun submitCheckIn(
        @Body checkIn: SupabaseCheckIn,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<Unit>

    @GET("rest/v1/check_ins?order=created_at.asc")
    suspend fun getCheckIns(
        @retrofit2.http.Query("case_id") caseIdFilter: String,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}"
    ): Response<List<SupabaseCheckIn>>

    @POST("rest/v1/rpc/validate_case_existence")
    suspend fun validateCaseExistence(
        @Body request: ValidateCaseRequest,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<Boolean>

    @GET("rest/v1/chat_messages?select=*&order=created_at.asc")
    suspend fun getChatMessages(
        @retrofit2.http.Query("case_id") caseIdFilter: String,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}"
    ): Response<List<SupabaseChatMessage>>

    @POST("rest/v1/rpc/register_victim")
    suspend fun registerVictimRpc(
        @Body request: RegisterVictimRequest,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<RpcResponse>

    @POST("rest/v1/rpc/recover_victim_session")
    suspend fun recoverVictimSessionRpc(
        @Body request: RecoverSessionRequest,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<RpcResponse>

    @POST("rest/v1/rpc/get_victim_dashboard")
    suspend fun getVictimDashboard(
        @Body request: DashboardRequest,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<VictimDashboardResponse>

    @POST("rest/v1/rpc/submit_victim_checkin")
    suspend fun submitVictimCheckInRpc(
        @Body request: SubmitCheckInRpcRequest,
        @Header("apikey") apikey: String = SupabaseConfig.ANON_KEY,
        @Header("Authorization") auth: String = "Bearer ${SessionManager.accessToken ?: SupabaseConfig.ANON_KEY}",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<RpcResponse>
}
