package com.facebook.galleryapp


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


private lateinit var appInterfaces: AppInterfaces

class SplashFragment : androidx.fragment.app.Fragment() {

    override fun onStart() {
        super.onStart()
        AdObject.admob = AdmobUtility(activity, appInterfaces ,SPLASH_SCREEN = true)
        AdObject.SPLASH_CALLED = true

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the rate_me_layout for this fragment
        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is AppInterfaces){ appInterfaces = activity }
    }
}
