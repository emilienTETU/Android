package com.example.myapp.data.model

import com.example.myapp.BuildConfig
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import com.google.gson.GsonBuilder
import com.example.myapp.remote.LedStatus
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * ApiService
 */
interface ApiService {

    @GET("/status")
    fun readStatus(@Query("identifier") identifier: String?): Call<LedStatus>

    @POST("/status")
    fun writeStatus(@Body status: LedStatus): Call<LedStatus>

    object Builder {
        /**
         * Create a singleton only for simplicity. Should be done through a DI system instead.
         */
        val instance = build()

        /**
         * Build an ApiService instance
         */
        private fun build(): ApiService {
            val gson = GsonBuilder().create() // JSON deserializer/serializer

            // Create the OkHttp Instance
            val okHttpClient = OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder().addHeader("Accept", "application/json").build()
                        chain.proceed(request)
                    }
                    .build()

            return Retrofit.Builder()
                    .baseUrl(BuildConfig.URI_REMOTE_SERVER)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(ApiService::class.java)
        }
    }
}
