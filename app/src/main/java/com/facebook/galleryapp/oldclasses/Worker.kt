package com.facebook.galleryapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.ResultReceiver
import androidx.core.app.JobIntentService
import android.util.Log
import com.facebook.galleryapp.AdObject
import java.io.IOException
import java.util.*


class Worker : JobIntentService() {
    private var timerTaskObj: TimerTask? = null

    /**
     * Result receiver object to send results
     */
    private var mResultReceiver: ResultReceiver? = null

    @SuppressLint("DefaultLocale", "RestrictedApi")
    override fun onHandleWork(intent: Intent) {
        Log.d(TAG, "onHandleWork() called with: intent = [$intent]")
        /*if (intent.action != null) {
            when (intent.action) {
                ACTION_DOWNLOAD -> {
                    mResultReceiver = intent.getParcelableExtra(RECEIVER)

                    val timerObj = Timer()
                    try {
                        timerTaskObj = object : TimerTask() {
                            override fun run() {
                                //perform your action here
                                try {
//                                    IsOnline.postValue(isOnline())
                                } catch (e: Exception) {
                                    Log.e("Worker", "exception in postValue.")
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        when (ex) {
                            is IllegalStateException -> Log.e("Worker", "IllegalStateException in running worker.")
                            is ExceptionInInitializerError -> Log.e("Worker", "ExceptionInInitializerError in running worker.")
                            else -> Log.e("Worker", "${ex.stackTrace} ")
                        }
                    }
                    try {
                        timerObj?.schedule(timerTaskObj, 0, 10000)
                    } catch (e: Exception) {
                        Log.e("Worker", "exception in running worker.")
                    }
                }
            }
        }*/
    }

    companion object {
        private val TAG = "Worker"
        val RECEIVER = "receiver"
        val SHOW_RESULT = 123

        /**
         * Unique job ID for this service.
         */
        internal val DOWNLOAD_JOB_ID = 1000

        /**
         * Actions download
         */
        private val ACTION_DOWNLOAD = "action.DOWNLOAD_DATA"

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context, workerResultReceiver: WorkerResultReceiver) {
         /*   val intent = Intent(context, Worker::class.java)
            intent.putExtra(RECEIVER, workerResultReceiver)
            intent.action = ACTION_DOWNLOAD
            try {
                enqueueWork(context, Worker::class.java, DOWNLOAD_JOB_ID, intent)
            } catch (e: IllegalStateException) {
                Log.e("Worker", "Error in enqueueWork")

            }*/
        }
    }

    fun isOnline(): Boolean {

        if (!AdObject.isNetworkAvailable()) {
            return false
        }


        try {
            /*Pinging to Google server*/
            val command = "ping -c 1 google.com"
            return Runtime.getRuntime().exec(command).waitFor() == 0

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {

        }
        return false
    }
}
