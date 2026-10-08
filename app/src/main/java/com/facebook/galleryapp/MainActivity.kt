package com.facebook.galleryapp

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.galleryapp.AdObject.FRAGMENT_LOADED
import com.facebook.galleryapp.AdObject.fragmentsStack
import com.facebook.galleryapp.NetworkWorker.adLimitEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), AppInterfaces {

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private var DB_NAME = "db_temp01.db"

    private val StartScreen = "START_SCREEN"
    private val TOPICS = "TOPICS"
    private val MENUS = "MENUS"
    private val ITEM = "ITEM"
    private val BookmarkMenu = "BOOKMARK_MENU"
    private val BookmarkItem = "BOOKMARK_ITEM"
    private val PrivacyPolicy = "PRIVACY_POLICY"


    // =========================================================
    // FACEBOOK AUDIENCE NETWORK BANNER
    // =========================================================

    private var adView: AdView? = null

    private lateinit var bannerContainer: LinearLayout

    private var isBannerAdLoading = false
    private var isBannerAdLoaded = false

    /*
     * IMPORTANT:
     *
     * Once loadAd() has been called for this AdView,
     * do not call it again immediately.
     *
     * This prevents:
     *
     * 1002 - Ad was re-loaded too frequently
     */
    private var bannerLoadAttempted = false

    private lateinit var bannerAdListener: AdListener


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        // -----------------------------------------------------
        // FACEBOOK AUDIENCE NETWORK
        // -----------------------------------------------------

        initializeFacebookAudienceNetwork()


        // -----------------------------------------------------
        // BANNER
        // -----------------------------------------------------

        bannerContainer = findViewById(R.id.adBanner)

        setupFacebookBannerAd()

        /*
         * Load FAN banner ONCE.
         *
         * Do not repeatedly load it when fragments change.
         */
        loadBannerWithConnectivityCheck()


        // -----------------------------------------------------
        // APP INITIALIZATION
        // -----------------------------------------------------

        startANRWatchDog()

        initializeNavgraph()

        loadSplashScreen()

        startNetworkMonitoringServiceUsingCoroutines()

        runInitializationInBackground()

        setDefaultExceptionHandler()


        // -----------------------------------------------------
        // MODERN BACK BUTTON
        // -----------------------------------------------------

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (isNotLastScreen()) {

                        /*
                         * Remove current screen.
                         */
                        fragmentsStack.pop()

                        /*
                         * Display previous screen.
                         *
                         * IMPORTANT:
                         * loadPreviousFragment() must NOT
                         * push the screen again.
                         */
                        loadPreviousFragment()

                    } else {

                        FRAGMENT_LOADED = false

                        AdObject.SPLASH_CALLED = false

                        exitApplication()
                    }
                }
            }
        )
    }


    // =========================================================
    // FACEBOOK AUDIENCE NETWORK INITIALIZATION
    // =========================================================

    private fun initializeFacebookAudienceNetwork() {

        if (!AudienceNetworkAds.isInitialized(this)) {

            AudienceNetworkAds.initialize(this)

            Log.d(
                "FAN_INIT",
                "Facebook Audience Network initialization requested"
            )

        } else {

            Log.d(
                "FAN_INIT",
                "Facebook Audience Network already initialized"
            )
        }


        /*
         * TEST MODE
         *
         * Only use these during development.
         *
         * NEVER ship production APK with:
         *
         * AdSettings.setTestMode(true)
         */

        // AdSettings.addTestDevice("YOUR_TEST_DEVICE_HASH")
        // AdSettings.setTestMode(true)
    }


    // =========================================================
    // SETUP FAN BANNER
    // =========================================================

    private fun setupFacebookBannerAd() {

        if (adView != null) {

            Log.d(
                "FAN_BANNER_DEBUG",
                "AdView already exists"
            )

            return
        }


        val placementId =
            getString(R.string.BANNER_ID)


        Log.d(
            "FAN_BANNER_DEBUG",
            "Creating FAN banner"
        )


        adView = AdView(
            this,
            placementId,
            AdSize.BANNER_HEIGHT_50
        )


        bannerAdListener =
            object : AdListener {

                override fun onError(
                    ad: Ad,
                    adError: AdError
                ) {

                    isBannerAdLoading = false

                    isBannerAdLoaded = false

                    Log.e(
                        "FAN_BANNER_DEBUG",
                        "Banner failed: " +
                                "${adError.errorCode} - " +
                                adError.errorMessage
                    )


                    /*
                     * IMPORTANT:
                     *
                     * DO NOT reset bannerLoadAttempted here.
                     *
                     * If the error is:
                     *
                     * 1001 - No fill
                     *
                     * immediately trying again can result in:
                     *
                     * 1002 - Ad was re-loaded too frequently
                     */
                }


                override fun onAdLoaded(
                    ad: Ad
                ) {

                    isBannerAdLoading = false

                    isBannerAdLoaded = true

                    Log.d(
                        "FAN_BANNER_DEBUG",
                        "FAN banner loaded successfully"
                    )
                }


                override fun onAdClicked(
                    ad: Ad
                ) {

                    Log.d(
                        "FAN_BANNER_DEBUG",
                        "FAN banner clicked"
                    )
                }


                override fun onLoggingImpression(
                    ad: Ad
                ) {

                    Log.d(
                        "FAN_BANNER_DEBUG",
                        "FAN banner impression logged"
                    )
                }
            }


        // -----------------------------------------------------
        // ADD BANNER TO CONTAINER
        // -----------------------------------------------------

        bannerContainer.removeAllViews()


        adView?.let { banner ->

            bannerContainer.addView(
                banner
            )

            Log.d(
                "FAN_BANNER_DEBUG",
                "FAN banner added to container"
            )
        }
    }


    // =========================================================
    // LOAD FAN BANNER
    // =========================================================

    private fun loadBannerWithConnectivityCheck() {

        val banner = adView

        if (banner == null) {
            Log.d("FAN_BANNER_DEBUG", "AdView is null")
            return
        }

        if (adLimitEnabled) {
            Log.d("FAN_BANNER_DEBUG", "Ad loading skipped: adLimitEnabled")
            return
        }

        if (isBannerAdLoaded) {
            Log.d("FAN_BANNER_DEBUG", "Banner already loaded - skipping reload")
            return
        }

        if (isBannerAdLoading) {
            Log.d("FAN_BANNER_DEBUG", "Banner already loading - skipping reload")
            return
        }

        if (bannerLoadAttempted) {
            Log.d("FAN_BANNER_DEBUG", "Banner load already attempted - skipping reload")
            return
        }

        bannerLoadAttempted = true
        isBannerAdLoading = true

        Log.d("FAN_BANNER_DEBUG", "Loading FAN banner")

        val loadConfig = banner
            .buildLoadAdConfig()
            .withAdListener(bannerAdListener)
            .build()

        banner.loadAd(loadConfig)
    }


    // =========================================================
    // ANR WATCHDOG
    // =========================================================

    private fun startANRWatchDog() {

        // ANRWatchDog().start()
    }


    // =========================================================
    // BACKGROUND INITIALIZATION
    // =========================================================

    private fun runInitializationInBackground() {

        val scope =
            CoroutineScope(
                Dispatchers.Default
            )


        scope.launch {

            AdObject.snackbarContainer =
                findViewById(
                    R.id.clMainActivity
                )


            loadDataFromAssets()

            setupDB()

            initializeAdobject()

            createBookmarkDir()
        }
    }


    // =========================================================
    // DATABASE
    // =========================================================

    private fun setupDB() {

        DB_NAME =
            getAssetsDBFileName()


        DataBaseHelper(
            this,
            DB_NAME
        ).let {

            ItemDataset.mDbHelper = it
        }


        ItemDataset.TOPIC_ID = 1

        ItemDataset.MENU_ID = 1
    }


    private fun getAssetsDBFileName(): String {

        return packageName.replace(
            ".",
            "_"
        )
    }


    // =========================================================
    // AD OBJECT
    // =========================================================

    private fun initializeAdobject() {

        AdObject.connectivityManager =
            applicationContext.getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager


        AdObject.PACKAGE_NAME =
            packageName
    }


    // =========================================================
    // BOOKMARK DIRECTORY
    // =========================================================

    private fun createBookmarkDir() {

        ItemDataset.APP_DIR =
            File(
                filesDir,
                "bookmarks"
            )


        ItemDataset.APP_DIR?.mkdirs()
    }


    // =========================================================
    // LOAD ASSETS
    // =========================================================

    private fun loadDataFromAssets() {

        AppUtils()
            .loadGalleryFromAssets(
                applicationContext
            )
    }


    // =========================================================
    // NAVIGATION INITIALIZATION
    // =========================================================

    private fun initializeNavgraph() {

        navHostFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.nav_host_fragment
                ) as NavHostFragment


        navController =
            navHostFragment.navController
    }


    // =========================================================
    // EXCEPTION HANDLER
    // =========================================================

    private fun setDefaultExceptionHandler() {

        Thread.setDefaultUncaughtExceptionHandler {
                _, e ->

            Log.e(
                "APP_CRASH",
                "Uncaught exception",
                e
            )
        }
    }


    // =========================================================
    // NETWORK MONITORING
    // =========================================================

    private fun startNetworkMonitoringServiceUsingCoroutines() {

        NetworkWorker.runNetworkCheckingThread()
    }


    // =========================================================
    // EXIT APPLICATION
    // =========================================================

    private fun exitApplication() {

        val intent =
            Intent(
                Intent.ACTION_MAIN
            )


        intent.addCategory(
            Intent.CATEGORY_HOME
        )


        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP


        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK


        startActivity(intent)


        finish()

        finishAffinity()

        exitProcess(0)
    }


    // =========================================================
    // STACK
    // =========================================================

    private fun isNotLastScreen(): Boolean {

        return fragmentsStack.size > 1
    }


    // =========================================================
    // SPLASH
    // =========================================================

    override fun loadSplashScreen() {

        if (!AdObject.SPLASH_CALLED) {

            navigateToScreenUsingNagGraph(
                SplashFragment()
            )
        }
    }


    // =========================================================
    // TEST MODE
    // =========================================================

    override fun loadTestModeScreen() {

        if (!isFinishing) {

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.nav_host_fragment,
                    TestModeFragment()
                )
                .commitAllowingStateLoss()

            /*
             * DO NOT reload FAN here.
             *
             * Banner already belongs to Activity.
             */
        }
    }


    // =========================================================
    // START SCREEN
    // =========================================================

    override fun loadStartScreen() {

        navigateToScreenUsingNagGraph(
            StartScreenFragment()
        )

        addFragmentToStack(
            StartScreen
        )
    }


    // =========================================================
    // PRIVACY POLICY
    // =========================================================

    override fun loadPrivacyPolicy() {

        navigateToScreenUsingNagGraph(
            PrivacyPolicyFragment()
        )

        addFragmentToStack(
            PrivacyPolicy
        )
    }


    // =========================================================
    // TOPICS
    // =========================================================

    override fun loadImageTopics() {

        navigateToScreenUsingNagGraph(
            TopicFragment()
        )

        addFragmentToStack(
            TOPICS
        )
    }


    // =========================================================
    // MENUS
    // =========================================================

    override fun loadMenus() {

        navigateToScreenUsingNagGraph(
            MenuFragment()
        )

        addFragmentToStack(
            MENUS
        )
    }


    // =========================================================
    // ITEMS
    // =========================================================

    override fun loadItem() {

        navigateToScreenUsingNagGraph(
            ItemFragment02()
        )

        addFragmentToStack(
            ITEM
        )
    }


    // =========================================================
    // BOOKMARK MENU
    // =========================================================

    override fun loadBookMarkMenu() {

        navigateToScreenUsingNagGraph(
            BookmarkFragment()
        )

        addFragmentToStack(
            BookmarkMenu
        )
    }


    // =========================================================
    // BOOKMARK ITEM
    // =========================================================

    override fun loadBookMarkItem() {

        navigateToScreenUsingNagGraph(
            BookMarkItemFragment()
        )

        addFragmentToStack(
            BookmarkItem
        )
    }


    // =========================================================
    // ADD FRAGMENT TO STACK
    // =========================================================

    private fun addFragmentToStack(
        fragmentScreen: String
    ) {

        FRAGMENT_LOADED = true

        fragmentsStack.push(
            fragmentScreen
        )
    }


    // =========================================================
    // NAVIGATION
    // =========================================================

    private fun navigateToScreenUsingNagGraph(
        destinationFrag: Fragment
    ) {

        if (isFinishing) {
            return
        }


        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.nav_host_fragment,
                destinationFrag
            )
            .commitAllowingStateLoss()


        FRAGMENT_LOADED = true

        /*
         * IMPORTANT:
         *
         * NO FAN load here.
         *
         * The banner belongs to the Activity and was already
         * loaded from onCreate().
         */
    }


    // =========================================================
    // RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        resumePausedFragment()
    }


    private fun resumePausedFragment() {

        if (FRAGMENT_LOADED == true) {

            if (fragmentsStack.size == 0) {
                return
            }


            val prevScreen =
                fragmentsStack.peek()


            /*
             * IMPORTANT:
             *
             * Do NOT call loadStartScreen(), loadMenus(),
             * etc. here because those functions push the
             * screen into the stack again.
             *
             * Only restore the Fragment itself.
             */

            when (prevScreen) {

                StartScreen ->
                    navigateToScreenUsingNagGraph(
                        StartScreenFragment()
                    )

                TOPICS ->
                    navigateToScreenUsingNagGraph(
                        TopicFragment()
                    )

                MENUS ->
                    navigateToScreenUsingNagGraph(
                        MenuFragment()
                    )

                ITEM ->
                    navigateToScreenUsingNagGraph(
                        ItemFragment02()
                    )

                BookmarkItem ->
                    navigateToScreenUsingNagGraph(
                        BookMarkItemFragment()
                    )

                BookmarkMenu ->
                    navigateToScreenUsingNagGraph(
                        BookmarkFragment()
                    )

                PrivacyPolicy ->
                    navigateToScreenUsingNagGraph(
                        PrivacyPolicyFragment()
                    )
            }

        } else {

            loadSplashScreen()
        }
    }


    // =========================================================
    // LOAD PREVIOUS FRAGMENT
    // =========================================================

    private fun loadPreviousFragment() {

        if (fragmentsStack.size == 0) {
            return
        }


        when (
            fragmentsStack.peek() as String
        ) {

            StartScreen ->
                navigateToScreenUsingNagGraph(
                    StartScreenFragment()
                )

            TOPICS ->
                navigateToScreenUsingNagGraph(
                    TopicFragment()
                )

            MENUS ->
                navigateToScreenUsingNagGraph(
                    MenuFragment()
                )

            ITEM ->
                navigateToScreenUsingNagGraph(
                    ItemFragment02()
                )

            BookmarkItem ->
                navigateToScreenUsingNagGraph(
                    BookMarkItemFragment()
                )

            BookmarkMenu ->
                navigateToScreenUsingNagGraph(
                    BookmarkFragment()
                )

            PrivacyPolicy ->
                navigateToScreenUsingNagGraph(
                    PrivacyPolicyFragment()
                )
        }
    }


    // =========================================================
    // PAUSE
    // =========================================================

    override fun onPause() {

        super.onPause()
    }


    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        Log.d(
            "FAN_BANNER_DEBUG",
            "Destroying FAN banner"
        )


        adView?.destroy()

        adView = null


        isBannerAdLoading = false

        isBannerAdLoaded = false


        super.onDestroy()
    }
}
