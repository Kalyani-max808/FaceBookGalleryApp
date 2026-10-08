package com.facebook.galleryapp

import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.facebook.galleryapp.AdObject.isTimerInProgress
import com.facebook.galleryapp.AdObject.mCountDownTimer
import com.facebook.galleryapp.NetworkWorker.isOnline
import java.sql.Timestamp
import java.util.Date

private var mInterstitialAd: InterstitialAd? = null
private var mAdIsLoading: Boolean = false

var TAG = "Admob"

class AdmobUtility(
    private val ctx: FragmentActivity?,
    val appInterfaces: AppInterfaces,
    var SPLASH_SCREEN: Boolean = false
) {

    /*
     * IMPORTANT:
     *
     * Always have a valid fallback callback.
     *
     * This prevents the splash screen from getting stuck if
     * the ad fails before loadNextScreen() supplies a callback.
     */
    var proceedToNextScreen: () -> Unit = {
        if (SPLASH_SCREEN) {
            SPLASH_SCREEN = false
            appInterfaces.loadStartScreen()
        }
    }

    /*
     * Facebook Audience Network Interstitial listener
     */
    private val interstitialAdListener = object : InterstitialAdListener {

        override fun onInterstitialDisplayed(ad: Ad) {

            Log.d(TAG, "Facebook interstitial displayed.")

            /*
             * The interstitial has been consumed.
             * It must not be reused.
             */
            mInterstitialAd = null

            /*
             * Start the ad-frequency timer.
             */
            getAdOpenTimestamp()
        }

        override fun onInterstitialDismissed(ad: Ad) {

            Log.d(TAG, "Facebook interstitial dismissed.")

            clearOldInterstitialAd()

            /*
             * Continue to the requested screen immediately.
             */
            proceedToNextScreen()

            /*
             * Preload another ad for the next opportunity.
             */
            loadAdWithConnectivityCheck()
        }

        override fun onError(
            ad: Ad,
            adError: AdError
        ) {

            Log.e(
                TAG,
                "Facebook interstitial error: ${adError.errorMessage}"
            )

            clearOldInterstitialAd()

            setAdIsNotLoading()

            /*
             * VERY IMPORTANT:
             *
             * Never leave the splash screen waiting for an ad
             * that failed.
             */
            if (SPLASH_SCREEN) {
                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()
            } else {
                proceedToNextScreen()
            }
        }

        override fun onAdLoaded(ad: Ad) {

            Log.d(
                TAG,
                "Facebook interstitial loaded successfully."
            )

            AppUtils().logDebugMsg(
                "Facebook interstitial loaded successfully."
            )

            /*
             * Store the loaded Facebook interstitial.
             */
            mInterstitialAd = ad as InterstitialAd

            setAdIsNotLoading()

            /*
             * If this is the splash screen, show it now.
             */
            showAdIfInSplashScreen()
        }

        override fun onAdClicked(ad: Ad) {

            Log.d(
                TAG,
                "Facebook interstitial clicked."
            )
        }

        override fun onLoggingImpression(ad: Ad) {

            Log.d(
                TAG,
                "Facebook interstitial impression."
            )
        }
    }

    /*
     * ------------------------------------------------------------
     * INITIALIZATION
     * ------------------------------------------------------------
     */

    init {

        showAlertIfInterstitialIDNotSet()

        /*
         * Set the fallback callback immediately.
         *
         * This is important because the splash screen can start
         * before loadNextScreen() has supplied its callback.
         */
        if (SPLASH_SCREEN) {

            proceedToNextScreen = {
                if (SPLASH_SCREEN) {
                    SPLASH_SCREEN = false
                    appInterfaces.loadStartScreen()
                }
            }
        }

        loadAdWithConnectivityCheck()
    }

    private fun showAlertIfInterstitialIDNotSet() {

        if (AdObject.INTERSTITIAL_ID.isEmpty()) {

            Log.d(
                TAG,
                "Facebook interstitial placement ID is empty."
            )

            /*
             * Do not block splash if there is no placement ID.
             */
            if (SPLASH_SCREEN) {
                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()
            }
        }
    }

    /*
     * ------------------------------------------------------------
     * LOAD NEXT SCREEN
     * ------------------------------------------------------------
     */

    fun loadNextScreen(cb: () -> Unit) {

        /*
         * Store the real callback supplied by the caller.
         */
        proceedToNextScreen = cb

        /*
         * If there is no network or the ad timer hasn't expired,
         * continue without showing an ad.
         */
        if (isNetworkNotAvailOrTimerNotExpired()) {

            proceedToNextScreen()

            return
        }

        /*
         * Ad already available.
         */
        if (isAdLoaded()) {

            if (!showAdWithConnectivityCheck()) {
                proceedToNextScreen()
            }

            return
        }

        /*
         * Ad is not ready.
         *
         * Start loading it for the next opportunity, but DO NOT
         * keep the user waiting for it.
         */
        loadAdWithConnectivityCheck()

        Log.d(
            TAG,
            "Interstitial not ready. Loading for next opportunity."
        )

        proceedToNextScreen()
    }

    /*
     * ------------------------------------------------------------
     * LOAD FACEBOOK INTERSTITIAL
     * ------------------------------------------------------------
     */

    fun loadAdWithConnectivityCheck() {

        /*
         * No placement ID.
         */
        if (AdObject.INTERSTITIAL_ID.isEmpty()) {

            Log.d(
                TAG,
                "No Facebook interstitial placement ID."
            )

            if (SPLASH_SCREEN) {
                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()
            }

            return
        }

        /*
         * Don't start another load while one is already running.
         */
        if (mAdIsLoading) {

            Log.d(
                TAG,
                "Facebook interstitial is already loading."
            )

            return
        }

        /*
         * Already have a loaded ad.
         */
        if (mInterstitialAd != null) {

            Log.d(
                TAG,
                "Facebook interstitial already loaded."
            )

            /*
             * If splash is waiting, show it.
             */
            if (SPLASH_SCREEN) {
                showAdIfInSplashScreen()
            }

            return
        }

        /*
         * No internet.
         */
        if (isOnline != true) {

            Log.d(
                TAG,
                "No internet connection for Facebook interstitial."
            )

            if (SPLASH_SCREEN) {

                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()
            }

            return
        }

        setAdIsLoading()

        loadInterstitialAd()
    }

    /*
     * ------------------------------------------------------------
     * CREATE + LOAD FACEBOOK INTERSTITIAL
     * ------------------------------------------------------------
     */

    private fun loadInterstitialAd() {

        val activity = ctx

        if (activity == null) {

            Log.e(
                TAG,
                "Activity is null. Cannot create Facebook interstitial."
            )

            setAdIsNotLoading()

            if (SPLASH_SCREEN) {

                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()
            }

            return
        }

        val placementId = AdObject.INTERSTITIAL_ID

        if (placementId.isEmpty()) {

            Log.e(
                TAG,
                "Facebook interstitial placement ID is empty."
            )

            setAdIsNotLoading()

            if (SPLASH_SCREEN) {

                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()
            }

            return
        }

        try {

            /*
             * THIS WAS MISSING IN YOUR ORIGINAL CODE.
             *
             * We must create the Facebook InterstitialAd object
             * before calling loadAd().
             */
            val interstitialAd =
                InterstitialAd(
                    activity,
                    placementId
                )

            /*
             * Store the object so it can be shown later.
             */
            mInterstitialAd = interstitialAd

            /*
             * Load the Facebook interstitial.
             */
            interstitialAd.loadAd(
                interstitialAd
                    .buildLoadAdConfig()
                    .withAdListener(interstitialAdListener)
                    .build()
            )

            Log.d(
                TAG,
                "Facebook interstitial load request started."
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Exception while loading Facebook interstitial.",
                e
            )

            clearOldInterstitialAd()

            setAdIsNotLoading()

            /*
             * NEVER leave splash waiting after an exception.
             */
            if (SPLASH_SCREEN) {

                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()
            }
        }
    }

    /*
     * ------------------------------------------------------------
     * SHOW INTERSTITIAL
     * ------------------------------------------------------------
     */

    private fun showAdIfInSplashScreen() {

        if (!SPLASH_SCREEN) {
            return
        }

        if (!isAdLoaded()) {

            Log.d(
                TAG,
                "Splash interstitial is not ready."
            )

            SPLASH_SCREEN = false
            appInterfaces.loadStartScreen()

            return
        }

        Log.d(
            TAG,
            "Showing Facebook interstitial from splash."
        )

        showInterstitialAd()
    }

    private fun showAdWithConnectivityCheck(): Boolean {

        if (isAdLoaded()) {

            showInterstitialAd()

            return true
        }

        if (SPLASH_SCREEN) {

            SPLASH_SCREEN = false
            appInterfaces.loadStartScreen()
        }

        return false
    }

    private fun showInterstitialAd() {

        val ad = mInterstitialAd

        if (ad == null) {

            Log.d(
                TAG,
                "Facebook interstitial is not ready."
            )

            proceedToNextScreen()

            return
        }

        try {

            /*
             * Facebook Audience Network show call.
             */
            ad.show()

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Exception while showing Facebook interstitial.",
                e
            )

            clearOldInterstitialAd()

            setAdIsNotLoading()

            /*
             * Never block the user.
             */
            if (SPLASH_SCREEN) {

                SPLASH_SCREEN = false
                appInterfaces.loadStartScreen()

            } else {

                proceedToNextScreen()
            }

            /*
             * Try loading another ad for the future.
             */
            loadAdWithConnectivityCheck()
        }
    }

    /*
     * ------------------------------------------------------------
     * AD FAILURE / RELOAD
     * ------------------------------------------------------------
     */

    private fun reloadAdToShowLater() {

        AdObject.TIME_LAST_LOADED =
            Timestamp(Date().time)

        clearOldInterstitialAd()

        setAdIsNotLoading()

        /*
         * Splash must always continue.
         */
        if (SPLASH_SCREEN) {

            SPLASH_SCREEN = false

            appInterfaces.loadStartScreen()

            return
        }

        proceedToNextScreen()
    }

    /*
     * ------------------------------------------------------------
     * TIMER
     * ------------------------------------------------------------
     */

    private fun getAdOpenTimestamp() {

        AdObject
            .INTERSTITIAL_LENGTH_MILLISECONDS
            .startTimer()

        /*
         * Do not block the current screen.
         *
         * Preload the next ad.
         */
        loadAdWithConnectivityCheck()

        /*
         * Continue the navigation after the ad was displayed.
         */
        proceedToNextScreen()
    }

    private fun Long.startTimer() {

        isTimerInProgress = true

        createTimer(this)

        mCountDownTimer?.start()
    }

    /*
     * ------------------------------------------------------------
     * AD STATE
     * ------------------------------------------------------------
     */

    private fun isAdLoaded(): Boolean {

        return mInterstitialAd != null
    }

    private fun setAdIsLoading() {

        mAdIsLoading = true
    }

    private fun setAdIsNotLoading() {

        mAdIsLoading = false
    }

    private fun clearOldInterstitialAd() {

        mInterstitialAd = null
    }

    /*
     * ------------------------------------------------------------
     * NETWORK / TIMER LOGIC
     * ------------------------------------------------------------
     */

    private fun isNetworkNotAvailOrTimerNotExpired(): Boolean {

        if (isOnline == false) {
            return true
        }

        if (!showAdOrNot()) {
            return true
        }

        return false
    }

    private fun showAdOrNot(): Boolean {

        if (didTimerNotStart()) {
            return true
        }

        if (isTimerExpired()) {
            return true
        }

        return false
    }

    private fun didTimerNotStart(): Boolean {

        return mCountDownTimer == null
    }

    fun isTimerExpired(): Boolean {

        return !isTimerInProgress
    }

    /*
     * ------------------------------------------------------------
     * COUNTDOWN TIMER
     * ------------------------------------------------------------
     */

    private fun createTimer(milliseconds: Long) {

        mCountDownTimer?.cancel()

        mCountDownTimer =
            object : CountDownTimer(
                milliseconds,
                500
            ) {

                override fun onTick(
                    millisUntilFinished: Long
                ) {
                    // No action required.
                }

                override fun onFinish() {

                    isTimerInProgress = false

                    Log.d(
                        TAG,
                        "Interstitial timer expired."
                    )
                }
            }
    }
}
