package com.facebook.galleryapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class WorkerResultReceiver
/**
 * Create a new ResultReceive to receive results.  Your
 * [.onReceiveResult] method will be called from the thread running
 * <var>handler</var> if given, or from an arbitrary thread if null.
 *
 * @param handler the handler object
 */
@SuppressLint("DefaultLocale", "RestrictedApi") constructor

(handler: Handler) : ResultReceiver(handler) {
    private var mReceiver: Receiver? = null

    fun setReceiver(receiver: Receiver) {
        mReceiver = receiver
    }


    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        if (mReceiver != null) {
            mReceiver!!.onReceiveResult(resultCode, resultData)
        }
    }

    interface Receiver {
        fun onReceiveResult(resultCode: Int, resultData: Bundle?)
    }

    companion object {
        private val TAG = "WorkerResultReceiver"
    }
}