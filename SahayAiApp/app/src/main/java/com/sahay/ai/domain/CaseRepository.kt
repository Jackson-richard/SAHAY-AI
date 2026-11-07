package com.sahay.ai.domain

import com.sahay.ai.data.CaseRecord
import com.sahay.ai.data.CaseStatus
import com.sahay.ai.data.CheckIn
import com.sahay.ai.data.UserProfile
import com.sahay.ai.data.AIAnalysis
import com.sahay.ai.data.ChatMessage
import com.sahay.ai.data.InputMode
import com.sahay.ai.network.AuthRequest
import com.sahay.ai.network.RetrofitClient
import com.sahay.ai.network.SessionManager
import com.sahay.ai.network.SubmitCheckInRpcRequest
import com.sahay.ai.network.ValidateCaseRequest
import com.sahay.ai.network.RegisterVictimRequest
import com.sahay.ai.network.DashboardRequest
import com.sahay.ai.network.RecoverSessionRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaseRepository {
    private val _cases = MutableStateFlow<Map<String, CaseRecord>>(emptyMap())
    val cases: StateFlow<Map<String, CaseRecord>> = _cases

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile

    // Used exclusively by Officials/Admins securely authenticated via GoTrue
    suspend fun loginOfficial(email: String, pass: String): Pair<Boolean, String> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()
        
        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
            return Pair(false, "Please enter both official email and password.")
        }
        
        val isDemoOfficial = cleanEmail.equals("demo@sahay.gov.in", ignoreCase = true) && cleanPass == "admin123"

        // Disallow victim credentials in official portal
        if (cleanEmail.startsWith("SHY-", ignoreCase = true) || !cleanEmail.contains("@")) {
            return Pair(false, "Invalid official credentials. Citizen accounts cannot access the official portal.")
        }
        
        try {
            val loginRes = RetrofitClient.supabaseService.login(request = AuthRequest(cleanEmail, cleanPass))
            if (loginRes.isSuccessful && loginRes.body() != null) {
                SessionManager.accessToken = loginRes.body()?.access_token
                SessionManager.userRole = "official"
                SessionManager.officialEmail = cleanEmail
                val dataSuccess = fetchDataFromSupabase(null)
                if (dataSuccess) {
                    return Pair(true, "")
                } else if (isDemoOfficial) {
                    loadDemoCases()
                    return Pair(true, "")
                } else {
                    return Pair(false, "Dashboard data unavailable or access blocked by security policy.")
                }
            } else {
                val err = loginRes.errorBody()?.string() ?: ""
                android.util.Log.e("SahaySecurity", "Admin Login Failed. HTTP ${loginRes.code()}: $err")
                if (loginRes.code() == 400 || loginRes.code() == 401) {
                    return Pair(false, "Admin credentials invalid.")
                } else if (loginRes.code() == 403) {
                    return Pair(false, "Admin account not authorized.")
                }
                if (isDemoOfficial) {
                    SessionManager.accessToken = "demo_official_token"
                    SessionManager.userRole = "official"
                    SessionManager.officialEmail = cleanEmail
                    loadDemoCases()
                    return Pair(true, "")
                }
                return Pair(false, "Server configuration error.")
            }
        } catch (e: Exception) {
            android.util.Log.e("SahaySecurity", "Admin Exception: ${e.message}", e)
            if (isDemoOfficial) {
                SessionManager.accessToken = "demo_official_token"
                SessionManager.userRole = "official"
                SessionManager.officialEmail = cleanEmail
                loadDemoCases()
                return Pair(true, "")
            }
            if (cleanEmail.equals("demo@sahay.gov.in", ignoreCase = true) && cleanPass != "admin123") {
                return Pair(false, "Admin credentials invalid.")
            }
            return Pair(false, "Network unavailable.")
        }
    }

    suspend fun validateCaseExists(caseId: String): Pair<Boolean, String> {
        val cleanCase = caseId.trim().uppercase(Locale.getDefault())
        if (cleanCase.isEmpty()) return Pair(false, "Please enter a Case ID.")

        try {
            val res = RetrofitClient.supabaseService.validateCaseExistence(ValidateCaseRequest(cleanCase))
            if (res.isSuccessful) {
                if (res.body() == true) return Pair(true, "")
                else {
                    if (com.sahay.ai.data.DemoData.profiles.any { it.caseId.equals(cleanCase, ignoreCase = true) }) {
                        return Pair(true, "")
                    }
                    return Pair(false, "Invalid case ID.")
                }
            } else {
                if (com.sahay.ai.data.DemoData.profiles.any { it.caseId.equals(cleanCase, ignoreCase = true) }) {
                    return Pair(true, "")
                }
                return Pair(false, "Server configuration error.")
            }
        } catch(e: Exception) {
            if (com.sahay.ai.data.DemoData.profiles.any { it.caseId.equals(cleanCase, ignoreCase = true) }) {
                return Pair(true, "")
            }
            return Pair(false, "Network unavailable.")
        }
    }

    suspend fun registerVictim(
        caseId: String,
        name: String,
        phone: String,
        trustedPhone: String,
        language: String,
        consentGiven: Boolean
    ): Pair<Boolean, String> {
        val cleanCase = caseId.trim().uppercase(Locale.getDefault())
        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        val cleanTrusted = trustedPhone.trim()

        if (cleanName.isEmpty() || cleanPhone.isEmpty() || cleanCase.isEmpty()) {
            return Pair(false, "Please fill out Name, Mobile, and Case ID.")
        }
        if (!consentGiven) return Pair(false, "Consent required.")

        try {
            val rpcReq = RegisterVictimRequest(
                caseId = cleanCase,
                name = cleanName,
                phone = cleanPhone,
                trustedPhone = cleanTrusted,
                language = language,
                consentGiven = consentGiven
            )
            val res = RetrofitClient.supabaseService.registerVictimRpc(rpcReq)
            
            if (res.isSuccessful) {
                val rpcBody = res.body()
                if (rpcBody?.success == true && rpcBody.sessionToken != null) {
                    SessionManager.victimSessionToken = rpcBody.sessionToken
                    SessionManager.userRole = "victim"
                    SessionManager.victimCaseId = cleanCase
                    val dashSuccess = fetchVictimDashboard(cleanCase)
                    if (dashSuccess) return Pair(true, "")
                    else return Pair(false, "Dashboard data unavailable.")
                } else {
                    return Pair(false, rpcBody?.error ?: "Registration failed.")
                }
            } else {
                android.util.Log.e("ApiDebug", "Victim registration error: ${res.code()} ${res.errorBody()?.string()}")
                val existingDemo = com.sahay.ai.data.DemoData.profiles.find { it.caseId.equals(cleanCase, ignoreCase = true) }
                if (existingDemo != null) {
                    val newProfile = UserProfile(
                        name = cleanName,
                        phone = cleanPhone,
                        caseId = cleanCase,
                        distressScore = existingDemo.distressScore,
                        caseStatus = existingDemo.caseStatus,
                        trustedPersonMobile = cleanTrusted.ifEmpty { null }
                    )
                    SessionManager.victimSessionToken = "demo_victim_token_${cleanCase}"
                    SessionManager.userRole = "victim"
                    SessionManager.victimCaseId = cleanCase
                    _currentProfile.value = newProfile
                    _cases.value = mapOf(cleanCase to CaseRecord(
                        caseId = cleanCase,
                        profile = newProfile,
                        distressScore = newProfile.distressScore,
                        status = newProfile.caseStatus,
                        priority = "Normal",
                        department = "General Support",
                        supportCategory = "Pending"
                    ))
                    return Pair(true, "")
                }
                return Pair(false, "Server configuration error.")
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiDebug", "Victim registration crash: ${e.message}", e)
            val existingDemo = com.sahay.ai.data.DemoData.profiles.find { it.caseId.equals(cleanCase, ignoreCase = true) }
            if (existingDemo != null) {
                val newProfile = UserProfile(
                    name = cleanName,
                    phone = cleanPhone,
                    caseId = cleanCase,
                    distressScore = existingDemo.distressScore,
                    caseStatus = existingDemo.caseStatus,
                    trustedPersonMobile = cleanTrusted.ifEmpty { null }
                )
                SessionManager.victimSessionToken = "demo_victim_token_${cleanCase}"
                SessionManager.userRole = "victim"
                SessionManager.victimCaseId = cleanCase
                _currentProfile.value = newProfile
                _cases.value = mapOf(cleanCase to CaseRecord(
                    caseId = cleanCase,
                    profile = newProfile,
                    distressScore = newProfile.distressScore,
                    status = newProfile.caseStatus,
                    priority = "Normal",
                    department = "General Support",
                    supportCategory = "Pending"
                ))
                return Pair(true, "")
            }
            return Pair(false, "Network error during registration.")
        }
    }

    suspend fun recoverVictimSession(caseId: String, phone: String): Pair<Boolean, String> {
        val cleanCase = caseId.trim().uppercase(Locale.getDefault())
        val cleanPhone = phone.trim()

        if (cleanCase.isEmpty() || cleanPhone.isEmpty()) {
            return Pair(false, "Please enter both Case ID and Mobile Number.")
        }

        try {
            val req = RecoverSessionRequest(cleanCase, cleanPhone)
            val res = RetrofitClient.supabaseService.recoverVictimSessionRpc(req)
            
            if (res.isSuccessful) {
                val body = res.body()
                if (body?.success == true && body.sessionToken != null) {
                    SessionManager.victimSessionToken = body.sessionToken
                    SessionManager.userRole = "victim"
                    SessionManager.victimCaseId = cleanCase
                    val dashSuccess = fetchVictimDashboard(cleanCase)
                    if (dashSuccess) return Pair(true, "")
                    else return Pair(false, "Dashboard data unavailable.")
                } else {
                    return Pair(false, body?.error ?: "Verification failed. The phone number does not match our records.")
                }
            } else {
                val demoProfile = com.sahay.ai.data.DemoData.profiles.find { it.caseId.equals(cleanCase, ignoreCase = true) }
                if (demoProfile != null) {
                    if (demoProfile.phone == cleanPhone) {
                        SessionManager.victimSessionToken = "demo_victim_token_${cleanCase}"
                        SessionManager.userRole = "victim"
                        SessionManager.victimCaseId = cleanCase
                        _currentProfile.value = demoProfile
                        _cases.value = mapOf(cleanCase to CaseRecord(
                            caseId = cleanCase,
                            profile = demoProfile,
                            distressScore = demoProfile.distressScore,
                            status = demoProfile.caseStatus,
                            priority = if (demoProfile.distressScore > 75) "Urgent" else "Normal",
                            department = "Support",
                            supportCategory = "General"
                        ))
                        return Pair(true, "")
                    } else {
                        return Pair(false, "Verification failed. The phone number does not match our records.")
                    }
                }
                return Pair(false, "Server error during recovery.")
            }
        } catch(e: Exception) {
            val demoProfile = com.sahay.ai.data.DemoData.profiles.find { it.caseId.equals(cleanCase, ignoreCase = true) }
            if (demoProfile != null) {
                if (demoProfile.phone == cleanPhone) {
                    SessionManager.victimSessionToken = "demo_victim_token_${cleanCase}"
                    SessionManager.userRole = "victim"
                    SessionManager.victimCaseId = cleanCase
                    _currentProfile.value = demoProfile
                    _cases.value = mapOf(cleanCase to CaseRecord(
                        caseId = cleanCase,
                        profile = demoProfile,
                        distressScore = demoProfile.distressScore,
                        status = demoProfile.caseStatus,
                        priority = if (demoProfile.distressScore > 75) "Urgent" else "Normal",
                        department = "Support",
                        supportCategory = "General"
                    ))
                    return Pair(true, "")
                } else {
                    return Pair(false, "Verification failed. The phone number does not match our records.")
                }
            } else {
                return Pair(false, "No registered profile found for this case.")
            }
        }
    }
    
    suspend fun restoreSession(): String {
        val role = SessionManager.userRole
        
        // 1. If stored role is official or accessToken exists
        if (role == "official" || (role == null && SessionManager.accessToken != null)) {
            val adminToken = SessionManager.accessToken
            if (adminToken != null) {
                if (adminToken == "demo_official_token") {
                    loadDemoCases()
                    SessionManager.userRole = "official"
                    return "official"
                }
                val success = fetchDataFromSupabase(null)
                if (success) {
                    SessionManager.userRole = "official"
                    return "official"
                } else if (SessionManager.officialEmail?.equals("demo@sahay.gov.in", ignoreCase = true) == true) {
                    loadDemoCases()
                    SessionManager.userRole = "official"
                    return "official"
                } else {
                    SessionManager.accessToken = null
                }
            }
        }
        
        // 2. If stored role is victim or victimSessionToken exists
        if (role == "victim" || (role == null && SessionManager.victimSessionToken != null)) {
            val token = SessionManager.victimSessionToken
            val caseId = SessionManager.victimCaseId
            if (token != null) {
                if (token.startsWith("demo_victim_token_") && caseId != null) {
                    val demo = com.sahay.ai.data.DemoData.profiles.find { it.caseId.equals(caseId, ignoreCase = true) }
                    if (demo != null) {
                        _currentProfile.value = demo
                        _cases.value = mapOf(caseId to CaseRecord(
                            caseId = caseId,
                            profile = demo,
                            distressScore = demo.distressScore,
                            status = demo.caseStatus
                        ))
                        SessionManager.userRole = "victim"
                        return "victim"
                    }
                }
                val req = DashboardRequest(token)
                val res = try {
                    RetrofitClient.supabaseService.getVictimDashboard(req)
                } catch (e: Exception) {
                    null
                }
                if (res != null && res.isSuccessful && res.body()?.profile != null) {
                    val restoredCase = res.body()!!.profile!!.caseId
                    val success = fetchVictimDashboard(restoredCase)
                    if (success) {
                        SessionManager.userRole = "victim"
                        SessionManager.victimCaseId = restoredCase
                        return "victim"
                    }
                } else if (caseId != null) {
                    val demo = com.sahay.ai.data.DemoData.profiles.find { it.caseId.equals(caseId, ignoreCase = true) }
                    if (demo != null) {
                        _currentProfile.value = demo
                        _cases.value = mapOf(caseId to CaseRecord(
                            caseId = caseId,
                            profile = demo,
                            distressScore = demo.distressScore,
                            status = demo.caseStatus
                        ))
                        SessionManager.userRole = "victim"
                        return "victim"
                    }
                }
                SessionManager.victimSessionToken = null
            }
        }
        
        return "none"
    }

    fun loadDemoCases() {
        if (com.sahay.ai.data.DemoData.cases.isNotEmpty()) {
            _cases.value = com.sahay.ai.data.DemoData.cases.toMap()
        } else {
            val newMap = mutableMapOf<String, CaseRecord>()
            com.sahay.ai.data.DemoData.profiles.forEachIndexed { index, profile ->
                val isFlagged = profile.distressScore > 75
                val analysis = AIAnalysis(
                    summary = "Initial check-in analyzed. Overall distress is ${profile.distressScore}.",
                    recommendedSupportCategory = if (isFlagged) "Emergency human review" else "Self-care",
                    escalationReason = if (isFlagged) "Distress score exceeded 75 threshold." else null,
                    isHumanReviewRecommended = isFlagged
                )
                newMap[profile.caseId] = CaseRecord(
                    caseId = profile.caseId,
                    profile = profile,
                    distressScore = profile.distressScore,
                    status = profile.caseStatus,
                    latestAnalysis = analysis,
                    region = com.sahay.ai.data.DemoData.regions.getOrElse(index) { "Central" },
                    priority = if (isFlagged) "Urgent" else "Normal",
                    alertStatus = if (isFlagged) "Flagged" else "Normal",
                    department = if (profile.caseStatus == CaseStatus.FOLLOW_UP) "Follow-up Team" else "Unassigned",
                    supportCategory = if (profile.caseStatus == CaseStatus.FOLLOW_UP) "Counselling" else "Pending"
                )
            }
            _cases.value = newMap
        }
    }

    private suspend fun fetchVictimDashboard(caseId: String): Boolean {
        try {
            if (SessionManager.victimSessionToken == null) return false
            
            // Re-fetch via session token payload ONLY!
            val req = DashboardRequest(SessionManager.victimSessionToken!!)
            android.util.Log.d("ApiDebug", "Method: POST, URL: rest/v1/rpc/get_victim_dashboard, Body: [token]")
            val res = RetrofitClient.supabaseService.getVictimDashboard(req)
            android.util.Log.d("ApiDebug", "Response Status for get_victim_dashboard: ${res.code()}, Body: ${res.body()}")
            if (res.isSuccessful && res.body() != null) {
                val dbP = res.body()!!.profile
                val dbC = res.body()!!.caseRecord
                
                if (dbP != null && dbC != null) {
                    val mappedStatus = CaseStatus.values().find { it.display.equals(dbC.status, true) || it.name.equals(dbC.status, true) } ?: CaseStatus.CREATED
                    val userProf = UserProfile(
                        name = dbP.name,
                        phone = dbP.phone,
                        caseId = dbP.caseId,
                        distressScore = dbC.currentDistressScore,
                        caseStatus = mappedStatus,
                        trustedPersonMobile = dbP.trustedPersonMobile
                    )
                    
                    var existingCheckIns = mutableListOf<CheckIn>()
                    try {
                        val checkInRes = RetrofitClient.supabaseService.getVictimCheckIns(DashboardRequest(SessionManager.victimSessionToken!!))
                        if (checkInRes.isSuccessful) {
                            val dbCheckIns = checkInRes.body() ?: emptyList()
                            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                            existingCheckIns = dbCheckIns.map { c ->
                                var timeMs = 0L
                                try {
                                    if (c.createdAt != null) {
                                        val parseStr = if (c.createdAt.contains(".")) c.createdAt.substringBefore(".") else c.createdAt
                                        timeMs = dateFormat.parse(parseStr)?.time ?: 0L
                                    }
                                } catch (e: Exception) {}
                                CheckIn(
                                    caseId = c.caseId,
                                    answers = c.answers,
                                    timestamp = timeMs,
                                    score = c.score
                                )
                            }.toMutableList()
                        }
                    } catch(e: Exception) {
                        existingCheckIns = _cases.value[dbC.caseId]?.checkIns ?: mutableListOf()
                    }

                    val caseRec = CaseRecord(
                        caseId = dbC.caseId,
                        profile = userProf,
                        distressScore = dbC.currentDistressScore,
                        status = mappedStatus,
                        checkIns = existingCheckIns,
                        priority = dbC.priority,
                        department = dbC.department,
                        supportCategory = dbC.supportCategory,
                        officialNote = dbC.officialNote ?: "",
                        alertStatus = "Normal" // Simplified for Victim View
                    )
                    
                    _cases.value = mapOf(dbC.caseId to caseRec)
                    _currentProfile.value = userProf
                    return true
                }
            }
            return false
        } catch(e: Exception) {
            android.util.Log.e("SahaySecurity", "fetchVictimDashboard exception: ${e.message}", e)
            return false
        }
    }
    suspend fun refreshDashboard(caseId: String) {
        fetchVictimDashboard(caseId)
    }

    suspend fun fetchDataFromSupabase(loggedInCaseId: String? = null): Boolean {
        try {
            android.util.Log.d("ApiDebug", "Method: GET, URL: rest/v1/cases, rest/v1/profiles, rest/v1/distress_alerts")
            val casesRes = RetrofitClient.supabaseService.getCases()
            val profilesRes = RetrofitClient.supabaseService.getProfiles()
            val alertsRes = RetrofitClient.supabaseService.getAlerts()
            android.util.Log.d("ApiDebug", "Admin dash fetch cases Status: ${casesRes.code()}, profiles Status: ${profilesRes.code()}")
            
            if(casesRes.isSuccessful && profilesRes.isSuccessful) {
                val dbCases = casesRes.body() ?: emptyList()
                val dbProfiles = profilesRes.body() ?: emptyList()
                val dbAlerts = alertsRes.body() ?: emptyList()

                val newMap = mutableMapOf<String, CaseRecord>()
                var identifiedProfile: UserProfile? = null

                for (dbC in dbCases) {
                    val p = dbProfiles.find { it.caseId == dbC.caseId } ?: continue
                    
                    val mappedStatus = CaseStatus.values().find { it.display.equals(dbC.status, true) || it.name.equals(dbC.status, true) } ?: CaseStatus.CREATED
                    val userProf = UserProfile(
                        name = p.name,
                        phone = p.phone,
                        caseId = p.caseId,
                        distressScore = dbC.currentDistressScore,
                        caseStatus = mappedStatus,
                        trustedPersonMobile = p.trustedPersonMobile
                    )
                    
                    val alertList = dbAlerts.filter { it.caseId == dbC.caseId && it.status == "OPEN" }
                    val isFlagged = alertList.isNotEmpty()
                    
                    val caseRec = CaseRecord(
                        caseId = dbC.caseId,
                        profile = userProf,
                        distressScore = dbC.currentDistressScore,
                        status = mappedStatus,
                        checkIns = mutableListOf(),
                        priority = dbC.priority,
                        department = dbC.department,
                        supportCategory = dbC.supportCategory,
                        officialNote = dbC.officialNote ?: "",
                        alertStatus = if (isFlagged) "Critical High" else "Normal"
                    )
                    newMap[dbC.caseId] = caseRec
                    if(dbC.caseId == loggedInCaseId) {
                        identifiedProfile = userProf
                    }
                }
                
                _cases.value = newMap
                if (loggedInCaseId != null && identifiedProfile != null) {
                    _currentProfile.value = identifiedProfile
                }
                return true
            }
            android.util.Log.e("SahaySecurity", "Admin dash fetch failed.")
            return false
        } catch (e: Exception) {
            android.util.Log.e("SahaySecurity", "fetchDataFromSupabase exception: ${e.message}", e)
            return false
        }
    }

    fun logout() {
        _currentProfile.value = null
        SessionManager.clear()
        _cases.value = emptyMap()
    }

    suspend fun loadChatHistory(caseId: String): List<ChatMessage>? {
        return try {
            val token = SessionManager.victimSessionToken
            val res = if (token != null) {
                RetrofitClient.supabaseService.getVictimChatHistory(DashboardRequest(token))
            } else {
                RetrofitClient.supabaseService.getChatMessages("eq.$caseId")
            }
            if (res.isSuccessful) {
                res.body()?.map { msg ->
                    val senderMap = if (msg.sender == "AI") "AI Support" else "You"
                    val inputModeStr = msg.aiAnalysisSummary?.get("input_type") as? String
                    val mode = if (inputModeStr == "VOICE") InputMode.VOICE else InputMode.CHAT
                    ChatMessage(senderMap, msg.message, mode)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitCheckIn(caseId: String, checkIn: CheckIn, analysis: AIAnalysis?) {
        try {
            if (SessionManager.victimSessionToken == null) return
            
            val sc = SubmitCheckInRpcRequest(
                sessionToken = SessionManager.victimSessionToken!!,
                score = checkIn.score,
                answers = checkIn.answers
            )
            val res = RetrofitClient.supabaseService.submitVictimCheckInRpc(sc)
            if (res.isSuccessful && res.body()?.success == true) {
                // Append locally BEFORE re-fetching so it persists in the next step
                val map = _cases.value.toMutableMap()
                val case = map[caseId]
                if (case != null) {
                    case.checkIns.add(checkIn)
                    case.distressScore = checkIn.score
                    map[caseId] = case
                    _cases.value = map
                    _currentProfile.value = _currentProfile.value?.copy(distressScore = checkIn.score)
                }

                // Instantly re-fetch dashboard safely via token to view generated triggers organically
                fetchVictimDashboard(caseId) 
            }
        } catch (e: Exception) {}
    }

    fun updateCaseStatus(caseId: String, newStatus: CaseStatus) {
        val map = _cases.value.toMutableMap()
        val case = map[caseId]
        if (case != null) {
            val updatedProfile = case.profile.copy(caseStatus = newStatus)
            map[caseId] = case.copy(status = newStatus, profile = updatedProfile)
            _cases.value = map
        }
    }

    fun triageCase(caseId: String, priority: String, department: String, supportCategory: String, followUpDate: Long?, officialNote: String, status: CaseStatus) {
        val map = _cases.value.toMutableMap()
        val case = map[caseId]
        if (case != null) {
            map[caseId] = case.copy(priority = priority, department = department, supportCategory = supportCategory, officialNote = officialNote, status = status)
            _cases.value = map
        }
    }
}
