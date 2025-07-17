package com.facebook.galleryapp

import android.app.IntentService
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.facebook.galleryapp.AdObject
import java.io.IOException
import java.util.*


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class IntentServiceIsOnline : IntentService("IntentServiceIsOnline") {
    override fun onCreate() {
        super.onCreate()
   /*     try {
            startForeground(1, Notification())
        }catch (e:IllegalStateException){
            Log.e("admob","Unable to start intent service")
        }catch (e:ExceptionInInitializerError){
//            Log.e("admob","ExceptionInInitializerError in worker.Adobj:${AdObject.IsOnline.value}")
        }*/
    }

    override fun onHandleIntent(intent: Intent?) {

        val timerObj = Timer()
      /*  val timerTaskObj = object : TimerTask() {
           override fun run() {
                //perform your action here
               try {
//                   AdObject.IsOnline.postValue(isOnline())
               }catch (e:Exception){
//                   Log.e("admob","ExceptionInInitializerError in worker.Adobj:${AdObject.IsOnline.value}")
               }
            }
        }
        timerObj.schedule(timerTaskObj, 0, 10000)*/
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFoo(param1: String, param2: String) {
        TODO("Handle action Foo")
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        TODO("Handle action Baz")
    }


    fun isOnline():Boolean {

      if (AdObject.isNetworkAvailable()){
          return false
      }
        try {
            /*Pinging to Google server*/
        val command = "ping -c 1 google.com"
            return Runtime.getRuntime().exec(command).waitFor() == 0

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e:InterruptedException) {
            e.printStackTrace()
        } finally {

        }
        return false
    }
    fun sartService(){}
    fun stopService(){
        stopForeground(true)
        stopSelf()
    }

}
