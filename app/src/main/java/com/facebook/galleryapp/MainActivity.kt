package com.facebook.galleryapp

import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.facebook.galleryapp.AdObject.FRAGMENT_LOADED
import com.facebook.galleryapp.AdObject.fragmentsStack
import com.facebook.galleryapp.AdObject.mCountDownTimer
import com.facebook.galleryapp.NetworkWorker.adLimitEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), AppInterfaces {

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private var BannerLoaded = false
    private var DB_NAME = "db_temp01.db" //CHANGE THE DB NAME FOR EVERY APP

    private val StartScreen = "START_SCREEN"
    private val TOPICS = "TOPICS"
    private val MENUS = "MENUS"
    private val ITEM = "ITEM"
    private val BookmarkMenu = "BOOKMARK_MENU"
    private val BookmarkItem = "BOOKMARK_ITEM"
    private val PrivacyPolicy = "PRIVACY_POLICY"
    private lateinit var adBanner:com.google.android.gms.ads.AdView

    init {

    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adBanner = findViewById(R.id.adBanner)
        startANRWatchDog()
        initializeAdmob()
  //      initializeNavgraph()
        loadSplashScreen()
        setupBannerAdListeners()
        loadBannerWithConnectivityCheck()
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
            AdObject.snackbarContainer= findViewById(R.id.clMainActivity)
//            adBanner = findViewById(R.id.adBanner)
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

    private fun initializeAdmob() {
        MobileAds.initialize(this) {}
    }

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
        }else{
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
/*
        val backStackEntryCount = navHostFragment?.childFragmentManager?.backStackEntryCount?:2
        if (backStackEntryCount==2) return true
        return false
*/
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
//                addToBackStack(null)  //DONT KEEP IT IN BACKSTACK
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
//        navigateToScreenUsingNagGraph(R.id.action_bookMark_to_bookMarkItem)
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
//    navController.navigate(fragment)
        FRAGMENT_LOADED = true
        this.loadBannerWithConnectivityCheck()
    }
}




    private fun loadBannerWithConnectivityCheck() {
        if (AdObject.isNetworkAvailable() and !adBanner.isLoading and !BannerLoaded and !adLimitEnabled) {
            adBanner.loadAd(AdRequest.Builder().build())
        }
    }

    private fun setupBannerAdListeners() {
        adBanner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                BannerLoaded = true
            }



            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }



            override fun onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                AppUtils().showSnackbarMsg("Banner failed to load.${error.message}")
            }
        }
    }

    /*--------------TO RESTORE THE SCREEN STATE ON RESUME---------------*/
    override fun onResume() {
        super.onResume()
        if (AdObject.isTimerInProgress)
            restartTimer()
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

    private fun restartTimer() {
        AdObject.admob?.startTimer(AdObject.INTERSTITIAL_LENGTH_MILLISECONDS)
    }

    override fun onPause() {
        cancelTimer()
        super.onPause()
    }

    private fun cancelTimer() {
        mCountDownTimer?.cancel()
    }
}

