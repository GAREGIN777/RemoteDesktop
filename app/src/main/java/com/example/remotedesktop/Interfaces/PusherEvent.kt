package com.example.remotedesktop.Interfaces

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST

const val appId = "1327869"
interface PusherApiService {

    @Headers("Content-Type: application/json")

    @POST("apps/${appId}/events")
    fun triggerEvent(@Body body: PusherEvent): Call<Void>
}

data class PusherEvent(
    val name: String,
    val channels: List<String>,
    val data: String
)
