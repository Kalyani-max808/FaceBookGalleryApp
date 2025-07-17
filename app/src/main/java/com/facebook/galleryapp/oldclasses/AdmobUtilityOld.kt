package com.facebook.galleryapp

import android.app.ProgressDialog
import androidx.fragment.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.facebook.galleryapp.AdObject
import com.facebook.galleryapp.AppInterfaces
import com.facebook.galleryapp.NetworkWorker.isOnline
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

/*
class AdmobUtilityOld(val ctx: FragmentActivity?, val appInterfaces: AppInterfaces, private val ADS_MODE:String = AdObject.ADS_MODE_TEST, var SPLASH_SCREEN:Boolean = false)  {
    private val mInterstitialAd = InterstitialAd(ctx)
    var AD_LOADING_CALLED:Boolean = false
    var AD_LOADED:Boolean = false
    private val progress = ProgressDialog(ctx).apply {
        isIndeterminate = true
        setTitle("Loading Ad..")
        setMessage("Please Wait..")
        setCancelable(false)
    }
    var callback: ()->Unit? = {Toast.makeText(ctx,"Ad failed to Load.Callback Not set.No connection.",Toast.LENGTH_SHORT).show()}

    */
/*********************INITIALIZATION BLOCK******************************//*


    init {


        //Set the Adunit ID and the load the add initially
        if (ADS_MODE == AdObject.ADS_MODE_TEST){
            mInterstitialAd.adUnitId = AdObject.INTERSTITIAL_TEST_ID
        }
        else{
            //Production Mode and AdIds not blank.
            if ((ADS_MODE == AdObject.ADS_MODE_PROD) and AdObject.INTERSTITIAL_ID.isNotEmpty()){
                mInterstitialAd.adUnitId = AdObject.INTERSTITIAL_ID
            }else{
                Toast.makeText(ctx,"Please setup the Interstitial ID.",Toast.LENGTH_SHORT).show()
            }
        }

//        if (AdObject.IsOnline){  progress.show() } // Show the progress until the ad is loaded --- INSTEAD OF PROGRESS BAR SHOW SPLASH SCREEN

        loadAdwithConnectivityCheck()
        mInterstitialAd.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                AD_LOADED = true
                */
/*--SHOW THE AD IF THE SCREEN IS SPLASH SCREEN--*//*

                if (SPLASH_SCREEN == true){
                    callback = {appInterfaces.loadStartScreen()}
*/
/*--- TO SHOW AD AFTER INITIAL SPLASH SCREEN LODING-------DISABLED DUE TO VIOLATION-------*//*

//                    showAdwithConnectivityCheck()
                    callback.let { it() }
                    SPLASH_SCREEN = false
                }
                dismissWithExceptionHandling()

            }

          */
/*  override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                //Load the ad to show next time
                Toast.makeText(ctx,"Ad Failed to Load",Toast.LENGTH_SHORT).show()
                AD_LOADING_CALLED = false
                AD_LOADED = false
                dismissWithExceptionHandling()
                AdObject.TIME_LAST_LOADED = Timestamp(Date().time)
                *//*
*/
/*--SHOW THE AD IF THE SCREEN IS SPLASH SCREEN--*//*
*/
/*
                if (SPLASH_SCREEN == true){
                    callback = {appInterfaces.loadStartScreen()}
                    SPLASH_SCREEN = false
                }
                callback()
            }*//*


            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
                AD_LOADING_CALLED = false
                dismissWithExceptionHandling()
                AdObject.TIME_LAST_LOADED = Timestamp(Date().time)
                loadAdwithConnectivityCheck()

                callback()

            }

            override fun onAdLeftApplication() {
                dismissWithExceptionHandling()
                AdObject.TIME_LAST_LOADED = Timestamp(Date().time)
                callback()
            }

            override fun onAdClosed() {
                dismissWithExceptionHandling()
                callback()
            }
        }
    }
    */
/*------------LOAD THE NEXT SCREEN AFTER SHOWING THE INTERSTITIAL AD---------------------*//*

    fun loadNextScreen( cb:()->Unit){
        callback = cb
        if (!showAdOrNot()or (isOnline == false)){ //If the internet not avail or timer<60sec
            callback()
            return
        }
        // Show the spinner now.
        progress.show()
*/
/*SHOW THE AD IF THE AD IS LOADED *//*

        if (mInterstitialAd.isLoaded or mInterstitialAd.isLoading) {
            if (!showAdwithConnectivityCheck()){
                dismissWithExceptionHandling()
                callback()} //show the ad if internet is avail only.
        } else {
*/
/*LOAD THE AD IF THE AD WAS NOT LOADED*//*

            loadAdwithConnectivityCheck()
            Log.d("ERROR", "The interstitial wasn't loaded yet. Loading it now and will show it next time.")
            dismissWithExceptionHandling()
            callback() //show the ad next time.
        }
    }
    */
/*------------SHOW THE INTERSTITAL AD------------------------------*//*

    fun showInterstitial( ){

        if (!showAdOrNot()or (isOnline==false)){ //If the internet not avail or timer<60sec
            return
        }
        // Show the spinner now.
        progress.show()

        if (mInterstitialAd.isLoaded or mInterstitialAd.isLoading) {
            if (!showAdwithConnectivityCheck()){return} //show the ad if internet is avail only.
        } else {
            loadAdwithConnectivityCheck()
            Log.d("ERROR", "The interstitial wasn't loaded yet. Loading it now and will show it next time.")
            dismissWithExceptionHandling()
        }
    }
    */
/*---------------------------------------------------------------------------------------------------------*//*

    fun loadAdwithConnectivityCheck(){

        if ((isOnline == true) and (!mInterstitialAd.isLoading and  !mInterstitialAd.isLoaded)) {
            mInterstitialAd.loadAd(AdRequest.Builder().build())
            AD_LOADING_CALLED = true
        }else if (SPLASH_SCREEN == true){
            appInterfaces.loadStartScreen()
        }

    }


    private fun showAdwithConnectivityCheck():Boolean{
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
            return true
        }else if (SPLASH_SCREEN == true){
            appInterfaces.loadStartScreen()
        }

        return false
    }

    fun dismissWithExceptionHandling(){
        try {
            progress.dismiss()
        }
        catch (ex:IllegalArgumentException){
            Log.e("ADError","Some error in progress bar.")
        }

    }


    private fun showAdOrNot():Boolean{
        var result:Boolean = false
        if (AdObject.TIME_LAST_LOADED == null) {
            result = true
        } else{
            //Calculate the difference in seconds
            val diff = TimeUnit.MILLISECONDS.toSeconds (Timestamp(Date().time).time - AdObject.TIME_LAST_LOADED!!.time)
            if (diff >= AdObject.TIME_INTERVAL_AD){
                result = true
            }     }

        return result

    }
}*/
