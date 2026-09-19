package com.sahay.ai.domain

import com.sahay.ai.data.AIAnalysis
import com.sahay.ai.network.ChatAnalysisRequest
import com.sahay.ai.network.RetrofitClient

import com.sahay.ai.data.InputMode

class AIRepository {
    suspend fun getChatResponse(
        context: String, 
        message: String, 
        caseId: String, 
        sessionToken: String,
        language: String,
        inputMode: InputMode = InputMode.CHAT
    ): Pair<String, AIAnalysis?> {
        return try {
            val enrichedContext = "User Preferred Language: $language. $context"
            val response = RetrofitClient.supabaseService.analyzeChat(
                auth = "Bearer $sessionToken",
                request = ChatAnalysisRequest(context = enrichedContext, message = message, case_id = caseId, input_type = inputMode.name)
            )
            if (response.isSuccessful && response.body() != null) {
                val indicators = response.body()!!.structured_indicators
                val isEscalated = (indicators["emotional_distress_level"] as? Double ?: 0.0) > 7.0
                
                val aiAnalysis = AIAnalysis(
                    summary = indicators["summary"] as? String ?: "No structured summary returned.",
                    recommendedSupportCategory = indicators["recommended_support_category"] as? String ?: "General",
                    escalationReason = if (isEscalated) "Distress level critical." else null,
                    isHumanReviewRecommended = isEscalated,
                    emotionalDistressLevel = indicators["emotional_distress_level"] as? Double ?: 0.0,
                    fearLevel = indicators["fear_level"] as? Double ?: 0.0,
                    feelingUnsafe = indicators["feeling_unsafe"] as? Boolean ?: false,
                    urgency = indicators["urgency"] as? String ?: "low"
                )
                Pair(response.body()!!.reply, aiAnalysis)
            } else {
                android.util.Log.e("ChatApi", "Chat API failed: ${response.code()} ${response.errorBody()?.string()}")
                val errorMsgText = if (language == "Tamil") {
                    "மன்னிக்கவும், தற்போது பதிலளிக்க முடியவில்லை. சிறிது நேரம் கழித்து மீண்டும் முயற்சிக்கவும்."
                } else {
                    "Sorry, I couldn't respond right now. Please try again."
                }
                Pair(errorMsgText, null)
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatApi", "Chat API exception: ${e.message}", e)
            val errorMsgText = if (language == "Tamil") {
                "மன்னிக்கவும், தற்போது பதிலளிக்க முடியவில்லை. சிறிது நேரம் கழித்து மீண்டும் முயற்சிக்கவும்."
            } else {
                "Sorry, I couldn't respond right now. Please try again."
            }
            Pair(errorMsgText, null)
        }
    }
}
