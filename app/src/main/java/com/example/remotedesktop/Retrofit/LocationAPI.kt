package com.example.remotedesktop.Retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface LocationAPI {
        @POST("/api/sendLocationUpdates/{channelId}")
        fun sendLocationUpdates(
            @Path("channelId") channelId: String,
            @Body request: LocationAPIRequest
        ): Call<LocationResponse>
    }