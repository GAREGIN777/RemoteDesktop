package com.example.remotedesktop.Retrofit

import com.example.remotedesktop.DataClasses.PusherPoint
import com.google.gson.annotations.SerializedName

data class LocationAPIRequest(
        @SerializedName("points") val points: List<PusherPoint>
    )