package com.example.remotedesktop.DataClasses

data class PusherPoint(
    val lat: Double,
    val lng: Double,
    val speed: Int,
    val bearing : Float,
    val timestamp: Long
)

data class PusherPointsWrapper(
    val points: List<PusherPoint>
)

/*data class PusherPoint() {

}*/