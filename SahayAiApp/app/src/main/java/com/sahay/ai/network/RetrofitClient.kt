package com.sahay.ai.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val baseUrl: String
        get() {
            val raw = SupabaseConfig.URL.trim()
            val url = if (raw.isNotEmpty()) raw else "https://amxcyyzbdanofrkoveoo.supabase.co"
            return if (url.endsWith("/")) url else "$url/"
        }

    private val supabaseRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val supabaseService: SupabaseService by lazy {
        supabaseRetrofit.create(SupabaseService::class.java)
    }
}
