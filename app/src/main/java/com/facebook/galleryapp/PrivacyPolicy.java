package com.facebook.galleryapp;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicy extends AppCompatActivity  {
    private WebView mWebview ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebview  = new WebView(this);

        mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript

        final Activity activity = this;

        mWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        });

//        mWebview .loadUrl("http://mindgamesoft.in/privacy.html");
        mWebview .loadUrl("file:///android_asset/privacy_policy.html");
        setContentView(mWebview );

    }


}
