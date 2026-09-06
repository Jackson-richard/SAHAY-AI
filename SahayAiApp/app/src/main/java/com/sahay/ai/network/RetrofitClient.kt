package com.sahay.ai.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val supabaseRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val supabaseService: SupabaseService by lazy {
        supabaseRetrofit.create(SupabaseService::class.java)
    }
}
