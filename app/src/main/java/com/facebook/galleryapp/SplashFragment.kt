package com.facebook.galleryapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

private lateinit var appInterfaces: AppInterfaces

class SplashFragment : Fragment() {

    private var splashAdInitialized = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is AppInterfaces) {
            appInterfaces = context
        } else {
            throw IllegalStateException(
                "Host Activity must implement AppInterfaces"
            )
        }
    }

    override fun onStart() {
        super.onStart()

        /*
         * Prevent creating the FAN ad manager multiple times
         * if onStart() is called again.
         */
        if (splashAdInitialized) {
            return
        }

        splashAdInitialized = true

        /*
         * Set Facebook Audience Network interstitial
         * placement ID.
         */
        AdObject.INTERSTITIAL_ID =
            getString(R.string.INTERSTITIAL_ID)

        /*
         * Create the FAN ad manager for the splash screen.
         */
        AdObject.admob =
            AdmobUtility(
                requireActivity(),
                appInterfaces,
                SPLASH_SCREEN = true
            )

        AdObject.SPLASH_CALLED = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.splash_fragment,
            container,
            false
        )
    }
}