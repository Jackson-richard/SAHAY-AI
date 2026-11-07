package com.sahay.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahay.ai.data.CaseStatus
import com.sahay.ai.data.CheckIn
import com.sahay.ai.data.ChatMessage
import com.sahay.ai.data.InputMode
import com.sahay.ai.domain.AIRepository
import com.sahay.ai.domain.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val caseRepository = CaseRepository()
    private val aiRepository = AIRepository()

    val cases = caseRepository.cases
    val currentProfile = caseRepository.currentProfile

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages
    
    val isSessionRestored = MutableStateFlow<String?>(null)
    
    init {
        checkSession()
    }
    
    fun checkSession() {
        viewModelScope.launch {
            val restoredRole = caseRepository.restoreSession() 
            if (restoredRole == "victim" && currentProfile.value?.caseId != null) {
                // Let's load the chat messages from DB if possible using CaseRepo
                caseRepository.loadChatHistory(currentProfile.value!!.caseId)?.let { history ->
                    _chatMessages.value = history
                }
            }
            isSessionRestored.value = restoredRole
        }
    }
    
    // Victim Onboarding State
    var selectedLanguage = "English"
    var consentGiven = false
    
    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput
    fun updateChatInput(text: String) { _chatInput.value = text }

    fun validateCaseId(caseId: String, onResult: (Pair<Boolean, String>) -> Unit) {
        android.util.Log.d("ApiDebug", "ViewModel state - Selected Language: $selectedLanguage, Consent Given: $consentGiven")
        viewModelScope.launch {
            val exists = caseRepository.validateCaseExists(caseId)
            onResult(exists)
        }
    }

    fun recoverSession(caseId: String, phone: String, onResult: (Pair<Boolean, String>) -> Unit) {
        viewModelScope.launch {
            val res = caseRepository.recoverVictimSession(caseId, phone)
            if (res.first) {
                isSessionRestored.value = "victim"
            }
            onResult(res)
        }
    }

    fun submitVictimRegistration(
        caseId: String,
        name: String,
        phone: String,
        trustedPhone: String,
        onResult: (Pair<Boolean, String>) -> Unit
    ) {
        android.util.Log.d("ApiDebug", "ViewModel state - Submitting Registration for Case: $caseId")
        viewModelScope.launch {
            val success = caseRepository.registerVictim(
                caseId = caseId,
                name = name,
                phone = phone,
                trustedPhone = trustedPhone,
                language = selectedLanguage,
                consentGiven = consentGiven
            )
            if (success.first) {
                isSessionRestored.value = "victim"
            }
            onResult(success)
        }
    }

    fun loginOfficial(email: String, pass: String, onResult: (Pair<Boolean, String>) -> Unit) {
        android.util.Log.d("ApiDebug", "ViewModel state - Preparing Official Login for email: $email")
        viewModelScope.launch {
            val success = caseRepository.loginOfficial(email, pass)
            if (success.first) {
                isSessionRestored.value = "official"
            }
            onResult(success)
        }
    }

    fun processCheckInAnalysis(text: String, onComplete: () -> Unit) {
        val caseId = currentProfile.value?.caseId
        val sessionToken = com.sahay.ai.network.SessionManager.victimSessionToken
        if (caseId == null || sessionToken == null) {
            onComplete()
            return
        }

        viewModelScope.launch {
            val response = aiRepository.getChatResponse("User Check-in. Please analyze the input distress.", text, caseId, sessionToken, selectedLanguage)
            val analysis = response.second
            
            if (analysis != null) {
                val metadata = mapOf(
                    "text" to text,
                    "emotional_distress_level" to analysis.emotionalDistressLevel.toString(),
                    "fear_level" to analysis.fearLevel.toString(),
                    "feeling_unsafe" to analysis.feelingUnsafe.toString(),
                    "recommended_support_category" to analysis.recommendedSupportCategory
                )

                val checkIn = CheckIn(
                    caseId = caseId,
                    answers = metadata,
                    timestamp = System.currentTimeMillis(),
                    score = 0
                )
                caseRepository.submitCheckIn(caseId, checkIn, analysis)
            }
            onComplete()
        }
    }

    fun submitCheckIn(caseId: String, responses: Map<String, String>, score: Int) {
        viewModelScope.launch {
            val checkIn = CheckIn(
                caseId = caseId,
                answers = responses,
                timestamp = System.currentTimeMillis(),
                score = score
            )
            caseRepository.submitCheckIn(caseId, checkIn, null)
        }
    }

    fun sendChatMessage(message: String, inputMode: InputMode = InputMode.CHAT, onComplete: () -> Unit = {}) {
        val caseId = currentProfile.value?.caseId
        val sessionToken = com.sahay.ai.network.SessionManager.victimSessionToken
        if (caseId == null || sessionToken == null) {
            _chatMessages.value = _chatMessages.value + ChatMessage("AI Support", "Your session has expired. Please log in again.", inputMode)
            onComplete()
            return
        }

        android.util.Log.d("ChatFlow", "CHAT_REQUEST_STARTED - Mode: ${inputMode.name}")
        val userMsg = ChatMessage("You", message, inputMode)
        _chatMessages.value = _chatMessages.value + userMsg
        
        viewModelScope.launch {
            val context = "Active distress tracking in progress."
            val response = aiRepository.getChatResponse(context, message, caseId, sessionToken, selectedLanguage, inputMode)
            
            if (response.first.startsWith("Sorry,") || response.first.startsWith("மன்னிக்கவும்")) {
                android.util.Log.d("ChatFlow", "CHAT_REQUEST_FAILED")
            } else {
                android.util.Log.d("ChatFlow", "CHAT_REQUEST_SUCCESS")
            }
            
            _chatMessages.value = _chatMessages.value + ChatMessage("AI Support", response.first, inputMode)
            
            // Re-fetch chat history immediately to ensure consistency
            caseRepository.loadChatHistory(caseId)?.let { history ->
                _chatMessages.value = history
            }
            
            val analysis = response.second
            if (analysis != null) {
                android.util.Log.d("ChatFlow", "AI_ANALYSIS_SUCCESS - Analysis persisted on backend, NO fake check-in created.")
                // Refresh dashboard to pull the officially computed score from DB
                caseRepository.refreshDashboard(caseId)
            } else {
                android.util.Log.d("ChatFlow", "AI_ANALYSIS_FAILED")
            }
            onComplete()
        }
    }

    fun updateCaseStatus(caseId: String, status: CaseStatus) {
        caseRepository.updateCaseStatus(caseId, status)
    }

    fun triageCase(caseId: String, priority: String, department: String, supportCategory: String, followUpDate: Long?, officialNote: String, status: CaseStatus) {
        caseRepository.triageCase(caseId, priority, department, supportCategory, followUpDate, officialNote, status)
    }
    
    fun logout() {
        caseRepository.logout()
        _chatMessages.value = emptyList()
        selectedLanguage = "English"
        consentGiven = false
        isSessionRestored.value = "none"
    }
}
