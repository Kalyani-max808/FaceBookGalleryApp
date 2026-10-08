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

private const val TAG = "FAN"

class AdmobUtility(
    private val ctx: FragmentActivity?,
    val appInterfaces: AppInterfaces,
    var SPLASH_SCREEN: Boolean = false
) {

    /*
     * ------------------------------------------------------------
     * AD STATE
     * ------------------------------------------------------------
     *
     * mInterstitialAd is assigned ONLY after onAdLoaded().
     *
     * This is important because simply creating an
     * InterstitialAd object does NOT mean the ad is loaded.
     */

    private var mInterstitialAd: InterstitialAd? = null

    private var mAdIsLoading = false

    private var mAdIsLoaded = false

    /*
     * ------------------------------------------------------------
     * FALLBACK NAVIGATION
     * ------------------------------------------------------------
     */

    var proceedToNextScreen: () -> Unit = {

        if (SPLASH_SCREEN) {

            SPLASH_SCREEN = false

            appInterfaces.loadStartScreen()
        }
    }

    /*
     * ------------------------------------------------------------
     * FACEBOOK AUDIENCE NETWORK INTERSTITIAL LISTENER
     * ------------------------------------------------------------
     */

    private val interstitialAdListener =
        object : InterstitialAdListener {

            override fun onInterstitialDisplayed(ad: Ad) {

                Log.d(
                    TAG,
                    "Facebook interstitial displayed."
                )

                /*
                 * The displayed interstitial has now been consumed.
                 * Do not try to reuse it.
                 */
                mInterstitialAd = null
                mAdIsLoaded = false

                /*
                 * Start the ad frequency timer.
                 */
                getAdOpenTimestamp()
            }

            override fun onInterstitialDismissed(ad: Ad) {

                Log.d(
                    TAG,
                    "Facebook interstitial dismissed."
                )

                /*
                 * Make absolutely sure the old ad is removed.
                 */
                clearOldInterstitialAd()

                setAdIsNotLoading()

                /*
                 * Continue navigation.
                 *
                 * Navigation happens HERE, not in
                 * onInterstitialDisplayed(), so it will not
                 * happen twice.
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

                AppUtils().logDebugMsg(
                    "Facebook interstitial error: ${adError.errorMessage}"
                )

                /*
                 * Remove failed ad state.
                 */
                clearOldInterstitialAd()

                setAdIsNotLoading()

                /*
                 * NEVER leave splash waiting after an error.
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
                 * IMPORTANT:
                 *
                 * The ad is assigned ONLY here.
                 *
                 * Creating InterstitialAd does not mean that
                 * the ad has actually loaded.
                 */
                mInterstitialAd = ad as InterstitialAd

                mAdIsLoaded = true

                setAdIsNotLoading()

                /*
                 * If splash is waiting for the ad,
                 * show it now.
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
         * Set a safe fallback callback immediately.
         *
         * This prevents splash from getting stuck if something
         * fails before loadNextScreen() supplies the real callback.
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

    /*
     * ------------------------------------------------------------
     * CHECK INTERSTITIAL ID
     * ------------------------------------------------------------
     */

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
         * If there is no network or the ad timer has not expired,
         * continue without showing an ad.
         */
        if (isNetworkNotAvailOrTimerNotExpired()) {

            proceedToNextScreen()

            return
        }

        /*
         * Ad is already completely loaded.
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
         * Load it for the next opportunity, but do not make
         * the user wait for it.
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
         * Do not start another load while one is already running.
         */
        if (mAdIsLoading) {

            Log.d(
                TAG,
                "Facebook interstitial is already loading."
            )

            return
        }

        /*
         * Already have a completely loaded ad.
         */
        if (isAdLoaded()) {

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

        /*
         * Mark loading BEFORE starting the SDK request.
         */
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
             * Create the Facebook interstitial.
             *
             * IMPORTANT:
             *
             * Do NOT assign this object to mInterstitialAd yet.
             *
             * It is only a loading object at this point.
             */
            val interstitialAd =
                InterstitialAd(
                    activity,
                    placementId
                )

            /*
             * Start loading.
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
             * Never leave splash waiting after an exception.
             */
            if (SPLASH_SCREEN) {

                SPLASH_SCREEN = false

                appInterfaces.loadStartScreen()
            }
        }
    }

    /*
     * ------------------------------------------------------------
     * SHOW INTERSTITIAL FROM SPLASH
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

    /*
     * ------------------------------------------------------------
     * SHOW INTERSTITIAL
     * ------------------------------------------------------------
     */

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

        if (ad == null || !mAdIsLoaded) {

            Log.d(
                TAG,
                "Facebook interstitial is not ready."
            )

            proceedToNextScreen()

            return
        }

        try {

            Log.d(
                TAG,
                "Calling Facebook interstitial show()."
            )

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
             * Try to preload another ad.
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

        /*
         * Start frequency timer after the ad is displayed.
         */
        AdObject
            .INTERSTITIAL_LENGTH_MILLISECONDS
            .startTimer()

        /*
         * IMPORTANT:
         *
         * Do NOT call proceedToNextScreen() here.
         *
         * Navigation is handled after the interstitial is
         * dismissed.
         */
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

        /*
         * An InterstitialAd object existing does NOT necessarily
         * mean the ad is loaded.
         *
         * Both conditions must be true.
         */
        return mInterstitialAd != null && mAdIsLoaded
    }

    private fun setAdIsLoading() {

        mAdIsLoading = true
    }

    private fun setAdIsNotLoading() {

        mAdIsLoading = false
    }

    private fun clearOldInterstitialAd() {

        mInterstitialAd = null

        mAdIsLoaded = false
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