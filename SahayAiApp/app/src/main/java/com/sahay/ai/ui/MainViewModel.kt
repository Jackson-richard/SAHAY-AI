package com.sahay.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahay.ai.data.CaseStatus
import com.sahay.ai.data.CheckIn
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

    private val _chatMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatMessages: StateFlow<List<Pair<String, String>>> = _chatMessages
    
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
            onResult(success)
        }
    }

    fun loginOfficial(email: String, pass: String, onResult: (Pair<Boolean, String>) -> Unit) {
        android.util.Log.d("ApiDebug", "ViewModel state - Preparing Official Login for email: $email")
        viewModelScope.launch {
            val success = caseRepository.loginOfficial(email, pass)
            onResult(success)
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

    fun sendChatMessage(message: String, onComplete: () -> Unit = {}) {
        val caseId = currentProfile.value?.caseId
        val sessionToken = com.sahay.ai.network.SessionManager.victimSessionToken
        if (caseId == null || sessionToken == null) {
            _chatMessages.value = _chatMessages.value + ("AI Support" to "Your session has expired. Please log in again.")
            onComplete()
            return
        }

        val userMsg = "You" to message
        _chatMessages.value = _chatMessages.value + userMsg
        
        viewModelScope.launch {
            val context = "Active distress tracking in progress."
            val response = aiRepository.getChatResponse(context, message, caseId, sessionToken, selectedLanguage)
            _chatMessages.value = _chatMessages.value + ("AI Support" to response.first)
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
    }
}
