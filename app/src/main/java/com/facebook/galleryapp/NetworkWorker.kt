package com.facebook.galleryapp

import android.util.Log
import kotlinx.coroutines.*


object NetworkWorker {

    private val  mSleepDurationConnected:Int = 60
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
                    Log.e("error",throwable.printStackTrace().toString())

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
                Log.e("error", ex.printStackTrace().toString())
            }
        }

        private fun sleepUsingCoroutines(timeinSec:Int) {
            try {
                runBlocking { delay(timeinSec * 1000L) }
            }catch (ex:Exception){
                Log.e("ex",ex.printStackTrace().toString())
            }
        }

         private fun  isDeviceOnline():Boolean {
/*
            if (!AdObject.isNetworkAvailable()){
                return false
            }
*/
            try {
                /*Pinging to Google server*/
                val command = "ping -c 1 google.com"
                val status = Runtime.getRuntime().exec(command).waitFor() == 0
                return status
            } catch (e: Exception) {
                e.printStackTrace()
            }  finally {
            }
            return false
        }



}