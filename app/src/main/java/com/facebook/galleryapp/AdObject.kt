package com.facebook.galleryapp

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import com.test.imagetemplate03.AdmobUtility
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object AdObject {
    var INTERSTITIAL_ID: String = "ca-app-pub-9097893318074265/8507309239"
    var PACKAGE_NAME = ""
    var TARGET_DATE_STRING: String = "10-AUG-2018" //Target Date for the App
    var THRESHOLD_TARGET_HOURS = 12
    var SPLASH_CALLED = false
    const val ADS_MODE_TEST = "TEST"
    const val ADS_MODE_PROD = "PROD"
    var admob: AdmobUtility? = null // to hold admob object
    var connectivityManager: ConnectivityManager? = null
    var FRAGMENT_LOADED: Boolean = false
    val fragmentsStack = NoDuplicateStack()
    const val GRID_IMAGE_WIDTH = 500
    const val GRID_THRESHOLD = 5
    const val AD_DISPLAY_SCROLL_COUNT = 5
    const val INTERSTITIAL_LENGTH_MILLISECONDS = 110 * 1000L
    var TIME_LAST_LOADED: Timestamp? = null //last time ad loaded time stamp
    var mCountDownTimer: CountDownTimer? = null
    var isTimerInProgress = false
    var snackbarContainer:View?=null
    val DUMMY_IMAGE_URI="others/dummy_image.jpg"




    fun isNetworkAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager?.getNetworkCapabilities(connectivityManager?.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
                }
            }
        } else {
            try {
                val activeNetworkInfo = connectivityManager?.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    Log.i("update_status", "Network is available : true")
                    return true
                }
            } catch (e: Exception) {
                Log.i("update_status", "" + e.message)
            }
        }
        Log.i("update_status", "Network is available : FALSE ")
        return false
    }

}