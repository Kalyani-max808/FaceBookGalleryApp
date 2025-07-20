package com.test.imagetemplate03

import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.facebook.galleryapp.AdObject
import com.facebook.galleryapp.AdObject.isTimerInProgress
import com.facebook.galleryapp.AdObject.mCountDownTimer
import com.facebook.galleryapp.AppInterfaces
import com.facebook.galleryapp.AppUtils
import com.facebook.galleryapp.NetworkWorker.isOnline
import java.sql.Timestamp
import java.util.*


private var mInterstitialAd: InterstitialAd? = null
private var mAdIsLoading: Boolean = false
/******************/

var TAG = "Admob"

class AdmobUtility(private val ctx: FragmentActivity?, val appInterfaces: AppInterfaces, var SPLASH_SCREEN: Boolean = false) {
    var proceedToNextScreen: () -> Unit? = {
//        AppUtils().showSnackbarMsg("Ad failed to Load.Callback Not set.No connection.")
    }

    private var interstitialAdListener = object : InterstitialAdListener {
        override fun onInterstitialDisplayed(ad: Ad) {
            // Interstitial ad displayed callback
            Log.e(TAG, "Interstitial ad displayed.")
            Log.d(TAG, "Ad showed fullscreen content.")
            mInterstitialAd = null
            getAdOpenTimestamp()
        }

        override fun onInterstitialDismissed(ad: Ad) {
            // Interstitial dismissed callback
            Log.e(TAG, "Interstitial ad dismissed.")
            Log.d(TAG, "Ad was dismissed.")
            clearOldInterstitialAd()
            loadAdWithConnectivityCheck()
            proceedToNextScreen()
        }

        override fun onError(ad: Ad, adError: AdError) {
            // Ad error callback
            Log.e(TAG, "Interstitial ad failed to load: " + adError.errorMessage)
            Log.d(TAG, "Ad failed to show.")
            clearOldInterstitialAd()
            reloadAdToShowLater()
        }

        override fun onAdLoaded(ad: Ad) {
            // Interstitial ad is loaded and ready to be displayed
            Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
            AppUtils().logDebugMsg("Ad loaded successfully.")
            Log.d(TAG,"Ad loaded successfully.")
            mInterstitialAd = ad as InterstitialAd
            setAdIsNotLoading()
            showAdIfInSplashScreen()
        }

        override fun onAdClicked(ad: Ad) {
            // Ad clicked callback
            Log.d(TAG, "Interstitial ad clicked!")
        }

        override fun onLoggingImpression(ad: Ad) {
            // Ad impression logged callback
            // Please refer to Monetization Manager or Reporting API for final impression numbers
            Log.d(TAG, "Interstitial ad impression logged!")
        }
    }

    init {
        showAlertIfInterstitialIDNotSet()
        loadAdWithConnectivityCheck()
    }

    private fun showAlertIfInterstitialIDNotSet() {
        if (AdObject.INTERSTITIAL_ID.isEmpty()) {
            Log.d(TAG,"Please setup interstitial ID.")
//            AppUtils().showSnackbarMsg("Please setup the Interstitial ID.")
        }

    }


    /*------------LOAD THE NEXT SCREEN AFTER SHOWING THE INTERSTITIAL AD---------------------*/
    fun loadNextScreen(cb: () -> Unit) {
        proceedToNextScreen = cb
        if (isNetworkNotAvailOrTimerNotExpired()) {
            proceedToNextScreen()
            return
        }
        if (isAdLoaded()) {
            if (!showAdWithConnectivityCheck()) {
                proceedToNextScreen()
            }
        } else {
            loadAdWithConnectivityCheck()
//            AppUtils().logErrorMsg("The interstitial wasn't loaded yet. Loading it now and will show it next time.")
            Log.d("ERROR", "The interstitial wasn't loaded yet. Loading it now and will show it next time.")
            proceedToNextScreen() //show the ad next time.
        }
    }

    /*---------------------------------------------------------------------------------------------------------*/
    fun loadAdWithConnectivityCheck() {
        if (isAdNotLoadedAndNetworkAvail()) {
            setAdIsLoading()
            loadInterstitialAd()
        } else if (SPLASH_SCREEN == true) {
            appInterfaces.loadStartScreen()
        }
    }

    private fun loadInterstitialAd() {
        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        mInterstitialAd?.loadAd(
            mInterstitialAd!!.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build());
    }


    private fun onAdFailedLogic() {
        loadStartScreen()
    }


    private fun showAdIfInSplashScreen(admobUtility: AdmobUtility) {
        /*--SHOW THE AD IF THE SCREEN IS SPLASH SCREEN--*/
        if (admobUtility.SPLASH_SCREEN) {
            admobUtility.proceedToNextScreen = { admobUtility.appInterfaces.loadStartScreen() }
            proceedToNextScreen()
            admobUtility.SPLASH_SCREEN = false
        }
    }

    private fun isAdNotLoadedAndNetworkAvail(): Boolean {
        return true
        return (isOnline == true) and (mInterstitialAd == null)
    }

    private fun showAdWithConnectivityCheck(): Boolean {
        if (isAdLoaded()) {
//            setupAdCallbacks()
            showInterstitialAd()
            return true
        } else if (SPLASH_SCREEN == true) {
            appInterfaces.loadStartScreen()
        }
        return false
    }



    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show()
        } else {
            AppUtils().logDebugMsg("The interstitial ad wasn't ready yet.")
//            Log.d(TAG, "The interstitial ad wasn't ready yet.")
        }
    }

    private fun reloadAdToShowLater() {
        AdObject.TIME_LAST_LOADED = Timestamp(Date().time)
        if (SPLASH_SCREEN) {
            proceedToNextScreen = { appInterfaces.loadStartScreen() }
            SPLASH_SCREEN = false
        }
        proceedToNextScreen()
    }

    private fun showAdIfInSplashScreen() {
        /*--SHOW THE AD IF THE SCREEN IS SPLASH SCREEN--*/
        if (SPLASH_SCREEN) {
            proceedToNextScreen = { appInterfaces.loadStartScreen() }
            proceedToNextScreen()
            SPLASH_SCREEN = false
        }
    }

    private fun getAdOpenTimestamp() {
        AdObject.INTERSTITIAL_LENGTH_MILLISECONDS.startTimer()
        loadAdWithConnectivityCheck()
        proceedToNextScreen()
    }

    private fun Long.startTimer() {
        isTimerInProgress = true
        createTimer(this)
        mCountDownTimer?.start()
    }



}

private fun AdmobUtility.loadStartScreen() {
    /*--SHOW THE AD IF THE SCREEN IS SPLASH SCREEN--*/
    if (SPLASH_SCREEN == true) {
        proceedToNextScreen = { appInterfaces.loadStartScreen() }
        SPLASH_SCREEN = false
    }
    proceedToNextScreen()
}

private fun isAdLoaded(): Boolean {
    if (mInterstitialAd != null) {
        return true
    } else return false
}

private fun setAdIsLoading() {
    mAdIsLoading = true
}

private fun clearOldInterstitialAd() {
    mInterstitialAd = null
}

private fun setAdIsNotLoading() {
    mAdIsLoading = false
}

private fun isNetworkNotAvailOrTimerNotExpired(): Boolean {
    if ((isOnline == false) or !showAdOrNot()) {
        return true
    } else return false
}

private fun showAdOrNot(): Boolean {
    var result = false
    if (didTimerNotStart()) {
        result = true
    } else if (isTimerExpired()) {
        result = true
    }
    return result
}


private fun didTimerNotStart(): Boolean {
    return mCountDownTimer == null
}

fun isTimerExpired(): Boolean {
    if (isTimerInProgress) return false else return true
}


private fun createTimer(milliseconds: Long) {
    mCountDownTimer?.cancel()
    mCountDownTimer = object : CountDownTimer(milliseconds, 500) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            isTimerInProgress = false
        }
    }
}

