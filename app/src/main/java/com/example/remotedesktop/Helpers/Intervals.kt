package com.example.remotedesktop.Helpers

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper

class Intervals {
    companion object{
        fun setInterval(function: () -> Unit, interval: Long,millisInFuture : Long = Long.MAX_VALUE): CountDownTimer {
            val countDownTimer = object : CountDownTimer(millisInFuture, interval) {
                override fun onTick(millisUntilFinished: Long) {
                    function()
                }

                override fun onFinish() {
                    clearInterval(this)
                    // Restart the timer when it finishes
                    //start()
                }
            }
            countDownTimer.start()
            return countDownTimer
        }

        fun clearInterval(countDownTimer: CountDownTimer) {
            countDownTimer.cancel()
        }
    }
}