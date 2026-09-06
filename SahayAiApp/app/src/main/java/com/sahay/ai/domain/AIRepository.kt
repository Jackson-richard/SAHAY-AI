package com.sahay.ai.domain

import com.sahay.ai.data.AIAnalysis
import com.sahay.ai.network.ChatAnalysisRequest
import com.sahay.ai.network.RetrofitClient

class AIRepository {
    suspend fun getChatResponse(
        context: String, 
        message: String, 
        caseId: String, 
        sessionToken: String,
        language: String
    ): Pair<String, AIAnalysis?> {
        return try {
            val enrichedContext = "User Preferred Language: $language. $context"
            val response = RetrofitClient.supabaseService.analyzeChat(
                auth = "Bearer $sessionToken",
                request = ChatAnalysisRequest(context = enrichedContext, message = message, case_id = caseId)
            )
            if (response.isSuccessful && response.body() != null) {
                val indicators = response.body()!!.structured_indicators
                val isEscalated = (indicators["emotional_distress_level"] as? Double ?: 0.0) > 7.0
                
                val aiAnalysis = AIAnalysis(
                    summary = indicators["summary"] as? String ?: "No structured summary returned.",
                    recommendedSupportCategory = indicators["recommended_support_category"] as? String ?: "General",
                    escalationReason = if (isEscalated) "Distress level critical." else null,
                    isHumanReviewRecommended = isEscalated
                )
                Pair(response.body()!!.reply, aiAnalysis)
            } else {
                val errString = response.errorBody()?.string() ?: "Unknown error"
                android.util.Log.e("ChatApi", "Chat API failed: ${response.code()} $errString")
                Pair("System Error: HTTP ${response.code()} - $errString", null)
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatApi", "Chat API exception: ${e.message}", e)
            Pair("App Runtime Error: ${e.javaClass.simpleName} - ${e.message}", null)
        }
    }
}
