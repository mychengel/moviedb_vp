package com.vp.moviedatabase.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Menambahkan ?api_key=...&language=en-US ke setiap request, jadi tidak perlu diulang di TmdbApi. */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("language", "en-US")
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}