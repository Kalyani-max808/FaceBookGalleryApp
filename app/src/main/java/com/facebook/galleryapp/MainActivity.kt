package com.facebook.galleryapp

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log // Added for logging
// Removed Button import as showInterstitialButton is removed
import android.widget.LinearLayout // Import LinearLayout for banner container
import android.widget.Toast // Import Toast for messages
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.facebook.ads.* // Import all necessary Facebook Ads classes
import com.facebook.galleryapp.AdObject.FRAGMENT_LOADED
import com.facebook.galleryapp.AdObject.fragmentsStack
// Removed AdObject.mCountDownTimer as it was AdMob-specific
import com.facebook.galleryapp.NetworkWorker.adLimitEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), AppInterfaces {

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    // Removed BannerLoaded as Facebook's AdView handles loading state internally
    private var DB_NAME = "db_temp01.db" //CHANGE THE DB NAME FOR EVERY APP

    private val StartScreen = "START_SCREEN"
    private val TOPICS = "TOPICS"
    private val MENUS = "MENUS"
    private val ITEM = "ITEM"
    private val BookmarkMenu = "BOOKMARK_MENU"
    private val BookmarkItem = "BOOKMARK_ITEM"
    private val PrivacyPolicy = "PRIVACY_POLICY"

    // Changed from AdMob AdView to Facebook Audience Network AdView
    private var adView: AdView? = null
    // Added LinearLayout to hold the Facebook banner ad
    private lateinit var bannerContainer: LinearLayout

    // Removed Interstitial Ad declarations
    // private var interstitialAd: InterstitialAd? = null
    // private lateinit var showInterstitialButton: Button // Button to trigger interstitial ad

    // Declare bannerAdListener as a class member so it can be accessed in loadBannerWithConnectivityCheck
    private lateinit var bannerAdListener: AdListener

    // Removed interstitialAdListener declaration
    // private lateinit var interstitialAdListener: InterstitialAdListener

    // New flag to track if banner ad is currently loading
    private var isBannerAdLoading: Boolean = false


    init {
        // Initialization block if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Facebook Audience Network SDK
        AudienceNetworkAds.initialize(this)
        // Register your test device and enable test mode
        // IMPORTANT: Set AdSettings.setTestMode(false) and remove AdSettings.addTestDevice() for production!
        AdSettings.addTestDevice("d5484b3f-5330-48f0-ba03-3986f5d1057e")
        AdSettings.setTestMode(true)

        // Find the banner container from XML (assuming it has id @+id/banner_container)
        bannerContainer = findViewById(R.id.adBanner)

      // In your MainActivity's onCreate() method, after setContentView() and before any ad loading
        AdObject.INTERSTITIAL_ID = getString(R.string.INTERSTITIAL_ID)
        startANRWatchDog()
        // initializeAdmob() removed - AdMob initialization is no longer needed
        initializeNavgraph() // Retained as it's part of your app's navigation
        loadSplashScreen()

        // Setup Facebook Banner Ad
        setupFacebookBannerAd()
        loadBannerWithConnectivityCheck() // This will now load the Facebook banner

        // Removed Setup Facebook Interstitial Ad call
        // setupFacebookInterstitialAd()

        startNetworkMonitoringServiceUsingCoroutines()
        runInitializationInBackground()
        setDefaultExceptionHandler()
    }

    private fun startANRWatchDog() {
//        ANRWatchDog().start()
    }

    private fun runInitializationInBackground() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            AdObject.snackbarContainer = findViewById(R.id.clMainActivity)
            // adBanner = findViewById(R.id.adBanner) removed - replaced by bannerContainer
            loadDataFromAssets()
            setupDB()
            initializeAdobject()
            createBookmarkDir()
        }
    }

    private fun setupDB() {
        DB_NAME = getAssetsDBFileName()
        DataBaseHelper(this, DB_NAME).let { ItemDataset.mDbHelper = it }
        ItemDataset.TOPIC_ID = 1
        ItemDataset.MENU_ID = 1
    }

    private fun getAssetsDBFileName() = packageName.toString().replace(".", "_")

    private fun initializeAdobject() {
        AdObject.connectivityManager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        AdObject.PACKAGE_NAME = packageName
    }

    private fun createBookmarkDir() {
        ItemDataset.APP_DIR = File(filesDir, "bookmarks")
        ItemDataset.APP_DIR?.let { it.mkdirs() }
    }

    private fun loadDataFromAssets() {
        AppUtils().loadGalleryFromAssets(applicationContext)
    }

    private fun initializeNavgraph() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    // initializeAdmob() removed

    private fun setDefaultExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { t, e -> System.err.println(e.printStackTrace()) }
    }

    private fun startNetworkMonitoringServiceUsingCoroutines() {
        NetworkWorker.runNetworkCheckingThread()
    }


    /*----------------------ON BACK PRESS FOR THE ACTIVITY AND FRAGMENTS-------------------*/
    override fun onBackPressed() {
//        if (isLastScreen()) exitApplication()
        if (isNotLastScreen()) {
            fragmentsStack.pop()
            loadPreviousFragment()
        } else {
            FRAGMENT_LOADED = false /*WHEN APP IS GOING INTO BACKGROUND, SET FRAGMENT_LOADED = FALSE*/
            AdObject.SPLASH_CALLED = false
            exitApplication()
        }
    }

    private fun exitApplication() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
        finish()
        finishAffinity()
        exitProcess(0)
    }

    private fun isNotLastScreen(): Boolean {
        return fragmentsStack.size > 1
    }

    /*--------------------------------------------SCREEN LOADING VIA FRAGMENTS--------------------------------------------------------*/
    /*-----------------------SCREEN 0 - THE SPLASH SCREEN----------------------*/
    override fun loadSplashScreen() {
        if (!AdObject.SPLASH_CALLED) navigateToScreenUsingNagGraph(SplashFragment())
    }

    /*-----------------------SCREEN 0 - THE TEST MODE SCREEN----------------------*/
    override fun loadTestModeScreen() {
        if (!isFinishing) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment, TestModeFragment())
                commitAllowingStateLoss()
            }
            this.loadBannerWithConnectivityCheck()
        }
    }

    /*-----------------------SCREEN 0 - THE MAIN SCREEN----------------------*/
    override fun loadStartScreen() {
        navigateToScreenUsingNagGraph(StartScreenFragment())
        addFragmentToStack(StartScreen)
    }

    override fun loadPrivacyPolicy() {
        navigateToScreenUsingNagGraph(PrivacyPolicyFragment())
        addFragmentToStack(PrivacyPolicy)
    }

    /*-----------------------SCREEN 1 - THE IMAGE TOPICS----------------------*/
    override fun loadImageTopics() {
        navigateToScreenUsingNagGraph(TopicFragment())
        addFragmentToStack(TOPICS)
    }

    /*---------------------SCREEN 2 - IMAGE MENUS---------------------*/
    override fun loadMenus() {
        navigateToScreenUsingNagGraph(MenuFragment())
        addFragmentToStack(MENUS)
    }

    /*------------------------------SCREEN 3 - THE IMAGE ITEM------------------------*/
    override fun loadItem() {
        navigateToScreenUsingNagGraph(ItemFragment02())
        addFragmentToStack(ITEM)
    }

    /*------------------------------SCREEN 4 - THE BOOK MARK MENU------------------------*/
    override fun loadBookMarkMenu() {
        navigateToScreenUsingNagGraph(BookmarkFragment())
        addFragmentToStack(BookmarkMenu)
    }

    /*------------------------------SCREEN 5 - THE BOOK MARK ITEM------------------------*/
    override fun loadBookMarkItem() {
        navigateToScreenUsingNagGraph(BookMarkItemFragment())
        addFragmentToStack(BookmarkItem)
    }

    private fun addFragmentToStack(fragmentScreen: String) {
        FRAGMENT_LOADED = true
        fragmentsStack.push(fragmentScreen)
    }

    /*--------------------------------------------SCREEN LOADING VIA FRAGMENTS--------------------------------------------------------*/
    private fun navigateToScreenUsingNagGraph(destinationFrag: Fragment) {
        if (!isFinishing) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment, destinationFrag)
                commitAllowingStateLoss()
                addToBackStack(null)
            }
            FRAGMENT_LOADED = true
            this.loadBannerWithConnectivityCheck()
        }
    }


    // Function to set up Facebook Banner Ad
    private fun setupFacebookBannerAd() {
        val BANNER_AD_PLACEMENT_ID = getString(R.string.BANNER_ID)
        adView = AdView(this, BANNER_AD_PLACEMENT_ID, AdSize.BANNER_HEIGHT_50)
        bannerContainer.addView(adView)

        bannerAdListener = object : AdListener {
            override fun onError(ad: Ad, adError: AdError) {
                Log.e("FAN_BANNER_DEBUG", "Banner Ad failed to load: " + adError.errorMessage)
                AppUtils().showSnackbarMsg("Banner failed to load.${adError.errorMessage}")
                isBannerAdLoading = false // Reset loading flag on error
            }
            override fun onAdLoaded(ad: Ad) {
                Log.d("FAN_BANNER_DEBUG", "Banner Ad loaded")
                isBannerAdLoading = false // Reset loading flag on success
            }
            override fun onAdClicked(ad: Ad) { Log.d("FAN_BANNER_DEBUG", "Banner Ad clicked") }
            override fun onLoggingImpression(ad: Ad) { Log.d("FAN_BANNER_DEBUG", "Banner Ad impression logged") }
        }
    }

    // Updated function to load Facebook Banner Ad
    private fun loadBannerWithConnectivityCheck() {
        if (adView != null && AdObject.isNetworkAvailable() && !adLimitEnabled) {
            // Check if the ad is currently loading to avoid redundant calls
            if (!isBannerAdLoading) { // Using the new flag
                isBannerAdLoading = true // Set flag to true before loading
                adView?.loadAd(adView!!.buildLoadAdConfig().withAdListener(bannerAdListener).build())
            } else {
                Log.d("FAN_BANNER_DEBUG", "Banner Ad already loading.")
            }
        } else {
            Log.d("FAN_BANNER_DEBUG", "Banner Ad not loaded due to network, ad limit, or adView not initialized.")
        }
    }

    // Removed setupFacebookInterstitialAd() function and all related logic
    // private fun setupFacebookInterstitialAd() { /* ... */ }


    /*--------------TO RESTORE THE SCREEN STATE ON RESUME---------------*/
    override fun onResume() {
        super.onResume()
        resumePausedFragment()
    }

    private fun resumePausedFragment() {
        if (FRAGMENT_LOADED == true) {
            val prevScreen = fragmentsStack.peek()
            when (prevScreen) {
                StartScreen -> loadStartScreen()
                TOPICS -> loadImageTopics()
                MENUS -> loadMenus()
                ITEM -> loadItem()
                BookmarkItem -> loadBookMarkItem()
                BookmarkMenu -> loadBookMarkMenu()
                PrivacyPolicy -> loadPrivacyPolicy()
            }

        } else {
            loadSplashScreen()
        }
    }
    private fun loadPreviousFragment(){
        when (fragmentsStack.pop() as String) {
            StartScreen -> {
                loadStartScreen()
            }
            TOPICS -> {
                loadImageTopics()
            }
            MENUS -> loadMenus()
            ITEM -> loadItem()
            BookmarkItem -> loadBookMarkItem()
            BookmarkMenu -> loadBookMarkMenu()
            PrivacyPolicy -> loadPrivacyPolicy()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        // Important: You must call destroy on AdView
        adView?.destroy()
        super.onDestroy()
    }
}
