package com.facebook.galleryapp

import android.util.Log
import kotlinx.coroutines.*


object NetworkWorker {

    private val  mSleepDurationConnected:Int = 5
    private val  mSleepDurationDisconnected:Int = 10
    var isOnline:Boolean = true
    var adLimitEnabled: Boolean =false



    fun runNetworkCheckingThread(){
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            try {
                while (!adLimitEnabled){
                    isOnline=isDeviceOnline()
                    logTheDeviceStatus()
                    sleepForSomeTime(isOnline)
                }
            } catch (throwable: Throwable) {
                // Log the stack trace as a string for better error reporting
                Log.e("error", "Error in network checking thread: ${throwable.stackTraceToString()}")

            }
        }

    }

    private fun logTheDeviceStatus() {
        Log.d("NetworkStatus", isOnline.toString())
    }

    private fun sleepForSomeTime(networkStatus: Boolean) {
        try {
            if (networkStatus) sleepUsingCoroutines(mSleepDurationConnected)
            else sleepUsingCoroutines(mSleepDurationDisconnected)
        }catch (ex:Exception){
            Log.e("error", "Error during sleep: ${ex.stackTraceToString()}")
        }
    }

    private fun sleepUsingCoroutines(timeinSec:Int) {
        try {
            runBlocking { delay(timeinSec * 1000L) }
        }catch (ex:Exception){
            Log.e("ex","Error during coroutine delay: ${ex.stackTraceToString()}")
        }
    }

    private fun  isDeviceOnline():Boolean {
        try {
            /*Pinging to Google server to check for actual internet access*/
            val command = "ping -c 1 google.com"
            val status = Runtime.getRuntime().exec(command).waitFor() == 0
            return status
        } catch (e: Exception) {
            Log.e("NetworkWorker", "Ping check failed: ${e.stackTraceToString()}")
        }
        return false
    }
}
