package com.facebook.galleryapp

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.SystemClock
import androidx.core.app.JobIntentService
import android.util.Log
import android.widget.Toast

/**
 * Example implementation of a JobIntentService.
 */
class SimpleJobIntentService : JobIntentService() {

    internal val mHandler = Handler()

    override fun onHandleWork(intent: Intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        Log.i("SimpleJobIntentService", "Executing work: $intent")
        var label: String? = intent.getStringExtra("label")
        if (label == null) {
            label = intent.toString()
        }
        toast("Executing: $label")
        for (i in 0..4) {
            Log.i("SimpleJobIntentService", "Running service " + (i + 1)
                    + "/5 @ " + SystemClock.elapsedRealtime())
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
            }

        }
        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime())
    }

    override fun onDestroy() {
        super.onDestroy()
        toast("All work complete")
    }

    // Helper for showing tests
    internal fun toast(text: CharSequence) {
        mHandler.post { Toast.makeText(this@SimpleJobIntentService, text, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        /**
         * Unique job ID for this service.
         */
        internal val JOB_ID = 1000

        /**
         * Convenience method for enqueuing work in to this service.
         */
        internal fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, SimpleJobIntentService::class.java, JOB_ID, work)
        }
    }
}