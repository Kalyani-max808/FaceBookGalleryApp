package com.facebook.galleryapp

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.facebook.galleryapp.AdObject.isTimerInProgress
import com.facebook.galleryapp.AdObject.mCountDownTimer
import com.facebook.galleryapp.NetworkWorker.adLimitEnabled
import com.facebook.galleryapp.NetworkWorker.isOnline
import java.sql.Timestamp
import java.util.*

private var mInterstitialAd: InterstitialAd? = null
private var mAdIsLoading: Boolean = false


var TAG = "Admob"

class AdmobUtility(private val ctx: FragmentActivity?, val appInterfaces: AppInterfaces, var SPLASH_SCREEN: Boolean = false) {
    var proceedToNextScreen: () -> Unit? = {
//        AppUtils().showSnackbarMsg("Ad failed to Load.Callback Not set.No connection.")
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
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(ctx as Context, AdObject.INTERSTITIAL_ID, adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
//                        AppUtils().logErrorMsg("--------Ad Failed to Load------------")
//                        AppUtils().logErrorMsg(adError.message)
                        Log.d(TAG, "Ad Failed to Load")
                        Log.d(TAG, adError.message)
                        clearOldInterstitialAd()
                        setAdIsNotLoading()
                        checkIfAdLimitEnabled(adError)
                        showAdFailedToLoadErrorMsg(adError)
                        onAdFailedLogic()
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                        AppUtils().logDebugMsg("Ad loaded successfully.")
                        Log.d(TAG,"Ad loaded successfully.")
                        mInterstitialAd = interstitialAd
                        setAdIsNotLoading()
//                        AppUtils().showSnackbarMsg("onAdLoaded")
                        showAdIfInSplashScreen()
                    }
                })
    }
    private fun checkIfAdLimitEnabled(adError: LoadAdError) {
        if (adError.code==3) {
            adLimitEnabled = true
//            AppUtils().showSnackbarMsg("AdLimit Enabled for this app.")
        }
    }


    private fun onAdFailedLogic() {
        loadStartScreen()
    }

    private fun showAdFailedToLoadErrorMsg(adError: LoadAdError) {
        val error = "domain: ${adError.domain}, code: ${adError.
        code}, " +
                "message: ${adError.message}"
        /*Toast.makeText(
                ctx,
                "onAdFailedToLoad() with error $error",
                Toast.LENGTH_SHORT
        ).show()*/
//        AppUtils().logErrorMsg("onAdFailedToLoad() with error $error")
        Log.e(TAG,"onAdFailedToLoad() with error $error")
    }


    private fun isAdNotLoadedAndNetworkAvail(): Boolean {
        return (isOnline == true) and (mInterstitialAd == null) and (!adLimitEnabled)
    }

    private fun showAdWithConnectivityCheck(): Boolean {
        if (isAdLoaded()) {
            setupAdCallbacks()
            showInterstitialAd()
            return true
        } else if (SPLASH_SCREEN == true) {
            appInterfaces.loadStartScreen()
        }
        return false
    }

    private fun setupAdCallbacks() {
        /***********NEW*******/
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
//                AppUtils().logDebugMsg("Ad was dismissed.")
                Log.d(TAG, "Ad was dismissed.")
                clearOldInterstitialAd()
                loadAdWithConnectivityCheck()
                proceedToNextScreen()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                AppUtils().logDebugMsg("Ad failed to show.")
                Log.d(TAG, "Ad failed to show.")
                clearOldInterstitialAd()
                reloadAdToShowLater()
            }

            override fun onAdShowedFullScreenContent() {
//                AppUtils().logDebugMsg("Ad showed fullscreen content.")
                Log.d(TAG, "Ad showed fullscreen content.")
                mInterstitialAd = null
                getAdOpenTimestamp()
            }
        }
        /***********NEW*******/

    }


    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(ctx as Activity)
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
        startTimer(AdObject.INTERSTITIAL_LENGTH_MILLISECONDS)
        loadAdWithConnectivityCheck()
        proceedToNextScreen()
    }

    fun startTimer(milliseconds: Long) {
        isTimerInProgress = true
        createTimer(milliseconds)
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

