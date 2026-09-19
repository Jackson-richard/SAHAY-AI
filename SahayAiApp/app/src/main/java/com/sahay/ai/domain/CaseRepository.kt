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
        try {
            val loginRes = RetrofitClient.supabaseService.login(request = AuthRequest(email, pass))
            if (loginRes.isSuccessful && loginRes.body() != null) {
                SessionManager.accessToken = loginRes.body()?.access_token
                val dataSuccess = fetchDataFromSupabase(null)
                if (dataSuccess) {
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
                return Pair(false, "Server configuration error.")
            }
        } catch (e: Exception) {
            android.util.Log.e("SahaySecurity", "Admin Exception: ${e.message}", e)
            return Pair(false, "Network unavailable.")
        }
    }

    suspend fun validateCaseExists(caseId: String): Pair<Boolean, String> {
        try {
            val res = RetrofitClient.supabaseService.validateCaseExistence(ValidateCaseRequest(caseId))
            if (res.isSuccessful) {
                if (res.body() == true) return Pair(true, "")
                else return Pair(false, "Invalid case ID.")
            } else {
                val err = res.errorBody()?.string() ?: ""
                android.util.Log.e("SahaySecurity", "Validate Case RPC Failed. HTTP ${res.code()}: $err")
                return Pair(false, "Server configuration error.")
            }
        } catch(e: Exception) {
            android.util.Log.e("SahaySecurity", "Validate Case Exception: ${e.message}", e)
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
        try {
            if (!consentGiven) return Pair(false, "Consent required.")
            val rpcReq = RegisterVictimRequest(
                caseId = caseId,
                name = name,
                phone = phone,
                trustedPhone = trustedPhone,
                language = language,
                consentGiven = consentGiven
            )
            val res = RetrofitClient.supabaseService.registerVictimRpc(rpcReq)
            
            if (res.isSuccessful) {
                val rpcBody = res.body()
                if (rpcBody?.success == true && rpcBody.sessionToken != null) {
                    SessionManager.victimSessionToken = rpcBody.sessionToken
                    val dashSuccess = fetchVictimDashboard(caseId) // Changed from fetchVictimDashboard(caseId) to properly populate flow
                    if (dashSuccess) return Pair(true, "")
                    else return Pair(false, "Dashboard data unavailable.")
                } else {
                    return Pair(false, rpcBody?.error ?: "Registration failed.")
                }
            } else {
                android.util.Log.e("ApiDebug", "Victim registration error: ${res.code()} ${res.errorBody()?.string()}")
                return Pair(false, "Server configuration error.")
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiDebug", "Victim registration crash: ${e.message}", e)
            return Pair(false, "Network error during registration.")
        }
    }

    suspend fun recoverVictimSession(caseId: String, phone: String): Pair<Boolean, String> {
        return try {
            val req = RecoverSessionRequest(caseId, phone)
            val res = RetrofitClient.supabaseService.recoverVictimSessionRpc(req)
            
            if (res.isSuccessful) {
                val body = res.body()
                if (body?.success == true && body.sessionToken != null) {
                    SessionManager.victimSessionToken = body.sessionToken
                    val dashSuccess = fetchVictimDashboard(caseId)
                    if (dashSuccess) return Pair(true, "")
                    else return Pair(false, "Dashboard data unavailable.")
                } else {
                    return Pair(false, body?.error ?: "Recovery failed. Please check your details.")
                }
            } else {
                return Pair(false, "Server error during recovery.")
            }
        } catch(e: Exception) {
            return Pair(false, "Network error during recovery.")
        }
    }
    
    suspend fun restoreSession(): Boolean {
        val token = SessionManager.victimSessionToken
        if (token != null) {
            android.util.Log.d("AuthDebug", "SESSION_FOUND - Victim session token found")
            val req = DashboardRequest(token)
            val res = try {
                RetrofitClient.supabaseService.getVictimDashboard(req)
            } catch (e: Exception) {
                null
            }
            if (res != null && res.isSuccessful && res.body() != null && res.body()!!.profile != null) {
                 val caseId = res.body()!!.profile!!.caseId
                 android.util.Log.d("AuthDebug", "SESSION_RESTORED - Restored case: $caseId")
                 return fetchVictimDashboard(caseId) 
            } else {
                 android.util.Log.d("AuthDebug", "SESSION_EXPIRED - Victim token invalid")
                 SessionManager.victimSessionToken = null
                 return false
            }
        }
        
        // Also check if admin token exists for official restore
        val adminToken = SessionManager.accessToken
        if (adminToken != null) {
            val success = fetchDataFromSupabase(null)
            if (success) {
                android.util.Log.d("AuthDebug", "SESSION_RESTORED - Restored Official session")
                return true
            } else {
                SessionManager.accessToken = null
            }
        }
        
        android.util.Log.d("AuthDebug", "LOGIN_REQUIRED - No active session found")
        return false
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
                        val checkInRes = RetrofitClient.supabaseService.getCheckIns("eq.${dbC.caseId}")
                        if (checkInRes.isSuccessful) {
                            val dbCheckIns = checkInRes.body() ?: emptyList()
                            existingCheckIns = dbCheckIns.map { c ->
                                CheckIn(
                                    caseId = c.caseId,
                                    answers = c.answers,
                                    timestamp = 0L, // Backend doesn't return timestamp, or use created_at if added.
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
            val res = RetrofitClient.supabaseService.getChatMessages("eq.$caseId")
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
