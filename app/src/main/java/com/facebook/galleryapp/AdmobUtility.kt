package com.facebook.galleryapp

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.facebook.ads.* // Import all Facebook Ads classes
import com.facebook.galleryapp.AdObject.isTimerInProgress // Re-import isTimerInProgress
import com.facebook.galleryapp.AdObject.mCountDownTimer // Re-import mCountDownTimer
import com.facebook.galleryapp.NetworkWorker.adLimitEnabled
import com.facebook.galleryapp.NetworkWorker.isOnline
import java.sql.Timestamp
import java.util.*

// Changed to Facebook's InterstitialAd
private var mInterstitialAd: InterstitialAd? = null
private var mAdIsLoading: Boolean = false


var TAG = "FacebookAdUtility" // Changed TAG to reflect Facebook Ads

class AdmobUtility(private val ctx: FragmentActivity?, val appInterfaces: AppInterfaces, var SPLASH_SCREEN: Boolean = false) {
    var proceedToNextScreen: () -> Unit? = {
//        AppUtils().showSnackbarMsg("Ad failed to Load.Callback Not set.No connection.")
    }

    // Declare interstitialAdListener as a class member
    private lateinit var interstitialAdListener: InterstitialAdListener


    init {
        // Initialize the Facebook Audience Network SDK here if not already done in Application class
        // AudienceNetworkAds.initialize(ctx) // Consider moving this to MyApp.kt for app-wide initialization

        // Setup the InterstitialAdListener once
        setupInterstitialAdListener()

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
        if (isNetworkNotAvailOrTimerNotExpired()) { // Timer logic re-integrated here
            proceedToNextScreen()
            return
        }
        if (isAdLoaded()) { // This now checks Facebook's ad state
            if (!showAdWithConnectivityCheck()) { // This now shows Facebook ad
                proceedToNextScreen()
            }
        } else {
            loadAdWithConnectivityCheck() // This now loads Facebook ad
//            AppUtils().logErrorMsg("The interstitial wasn't loaded yet. Loading it now and will show it next time.")
            Log.d("ERROR", "The interstitial wasn't loaded yet. Loading it now and will show it next time.")
            proceedToNextScreen() //show the ad next time.
        }
    }

    /*---------------------------------------------------------------------------------------------------------*/
    fun loadAdWithConnectivityCheck() {
        // isAdNotLoadedAndNetworkAvail() now checks for Facebook's mInterstitialAd
        if (isAdNotLoadedAndNetworkAvail()) {
            setAdIsLoading()
            loadInterstitialAd() // This now loads Facebook's InterstitialAd
        } else if (SPLASH_SCREEN == true) {
            appInterfaces.loadStartScreen()
        }
    }

    private fun loadInterstitialAd() {
        // Instantiate Facebook InterstitialAd if not already
        if (mInterstitialAd == null) {
            mInterstitialAd = InterstitialAd(ctx, AdObject.INTERSTITIAL_ID)
        }

        // Load Facebook InterstitialAd using buildLoadAdConfig
        mInterstitialAd?.loadAd(
            mInterstitialAd!!.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build()
        )
    }

    // Setup the InterstitialAdListener for Facebook Ads
    private fun setupInterstitialAdListener() {
        interstitialAdListener = object : InterstitialAdListener {
            override fun onError(ad: Ad, adError: AdError) {
                Log.e(TAG, "Ad Failed to Load: " + adError.errorMessage)
                clearOldInterstitialAd()
                setAdIsNotLoading()
                // checkIfAdLimitEnabled(adError) // AdMob specific error code check commented out
                AppUtils().showSnackbarMsg("Interstitial failed to load.${adError.errorMessage}") // Using your existing snackbar
                onAdFailedLogic()
            }

            override fun onAdLoaded(ad: Ad) {
                Log.d(TAG, "Ad loaded successfully.")
                mInterstitialAd = ad as InterstitialAd // Cast to Facebook's InterstitialAd
                setAdIsNotLoading()
                showAdIfInSplashScreen()
            }

            override fun onAdClicked(ad: Ad) {
                Log.d(TAG, "Ad clicked.")
            }

            override fun onLoggingImpression(ad: Ad) {
                Log.d(TAG, "Ad impression logged.")
            }

            override fun onInterstitialDisplayed(ad: Ad) {
                Log.d(TAG, "Ad showed fullscreen content.")
                // mInterstitialAd = null // Facebook recommends nulling after dismissal, not display
                getAdOpenTimestamp() // Timer logic re-integrated here
            }

            override fun onInterstitialDismissed(ad: Ad) {
                Log.d(TAG, "Ad was dismissed.")
                clearOldInterstitialAd()
                loadAdWithConnectivityCheck() // Load a new ad for next time
                proceedToNextScreen()
            }
        }
    }

    // AdMob specific error code check commented out
    private fun checkIfAdLimitEnabled(adError: AdError) {
        // if (adError.code==3) { // This error code is AdMob specific
        //     adLimitEnabled = true
        //     AppUtils().showSnackbarMsg("AdLimit Enabled for this app.")
        // }
        // For Facebook, you might need to check specific Facebook AdError types or messages
        // if (adError.errorMessage.contains("NO_FILL", ignoreCase = true)) {
        //     adLimitEnabled = true
        //     AppUtils().showSnackbarMsg("AdLimit Enabled for this app (Facebook no fill).")
        // }
    }


    private fun onAdFailedLogic() {
        loadStartScreen()
    }

    private fun showAdFailedToLoadErrorMsg(adError: AdError) { // Changed LoadAdError to AdError
        val error = "domain: ${adError}, code: ${adError.
        errorCode}, " + // Changed code to errorCode for Facebook AdError
                "message: ${adError.errorMessage}" // Changed message to errorMessage for Facebook AdError
        /*Toast.makeText(
                ctx,
                "onAdFailedToLoad() with error $error",
                Toast.LENGTH_SHORT
        ).show()*/
//        AppUtils().logErrorMsg("onAdFailedToLoad() with error $error")
        Log.e(TAG,"onAdFailedToLoad() with error $error")
    }


    private fun isAdNotLoadedAndNetworkAvail(): Boolean {
        // Check if ad is not loaded AND not currently loading, and network is available
        isOnline = true // test
        return (isOnline == true) && (mInterstitialAd == null || !mInterstitialAd!!.isAdLoaded) && (!mAdIsLoading) && (!adLimitEnabled)
    }

    private fun showAdWithConnectivityCheck(): Boolean {
        if (isAdLoaded()) { // This now checks Facebook's ad state
            showInterstitialAd() // This now shows Facebook ad
            return true
        } else if (SPLASH_SCREEN == true) {
            appInterfaces.loadStartScreen()
        }
        return false
    }

    private fun setupAdCallbacks() {
        // This method is largely replaced by the setupInterstitialAdListener() and
        // the withAdListener() call in loadAdWithConnectivityCheck()
        // Keeping it empty as its original purpose is now handled by the InterstitialAdListener.
    }


    private fun showInterstitialAd() {
        if (mInterstitialAd != null && mInterstitialAd!!.isAdLoaded) { // Check if loaded before showing
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
        // Timer logic re-integrated here
        startTimer(AdObject.INTERSTITIAL_LENGTH_MILLISECONDS)
        loadAdWithConnectivityCheck() // This will load a new ad for next time
        proceedToNextScreen() // This will proceed to the next screen after ad is shown
    }

    // Timer methods are now re-integrated
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
    // For Facebook, check if the ad object exists and is marked as loaded
    return mInterstitialAd != null && mInterstitialAd!!.isAdLoaded
}

private fun setAdIsLoading() {
    mAdIsLoading = true
}

private fun clearOldInterstitialAd() {
    mInterstitialAd?.destroy() // Destroy the old ad to release resources
    mInterstitialAd = null
}

private fun setAdIsNotLoading() {
    mAdIsLoading = false
}

private fun isNetworkNotAvailOrTimerNotExpired(): Boolean {
    // Timer logic re-integrated here
    return false //test
    return (isOnline == false) || !showAdOrNot()
}

// Timer related functions are now re-integrated
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
