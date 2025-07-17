package com.facebook.galleryapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class TestModeFragment : androidx.fragment.app.Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the rate_me_layout for this fragment
        val v = inflater.inflate(R.layout.fragment_test_mode, container, false)
//        v.tvTestMode.text = "The App is in Test mode. Target Date is:${AdObject.TARGET_DATE_STRING}." +
//                "It will show on or before ${AdObject.THRESHOLD_TARGET_HOURS} hours "
        return v
    }



}
