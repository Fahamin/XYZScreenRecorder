package com.xyz.screen.recorder.adsMAnager;

import android.content.Context;
import android.util.Log;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.xyz.screen.recorder.R;


public class FacebookInterAds {
    public static InterstitialAd fbinterSplash;






    /////////////SPLASH FB AD  /////////////

    public void loadFbInterSplash(Context context) {


        fbinterSplash = new InterstitialAd(context, context.getString(R.string.fb_inter_splash));
        AdSettings.addTestDevice("d352ac46-71b1-478d-9c8f-b66fe003783d");
        fbinterSplash.loadAd();
    }


    public void showFbInterSplash() {
        Log.i("iamin", "showFbAdsconnected: "+fbinterSplash);
        if (fbinterSplash != null) {
            if (fbinterSplash.isAdLoaded())
            {

                fbinterSplash.show();
                Log.i("iamin", "showFbAdsconnected: loaded");

            }
            else
            {
                Log.i("iamin", "showFbAdsconnected: not  loaded");

            }
            fbinterSplash.setAdListener(new AbstractAdListener() {
                @Override
                public void onError(Ad ad, AdError error) {
                    super.onError(ad, error);
                    Log.i("iamin", "showFbAdsconnected: not  loaded error == "+error.getErrorMessage());

                }
            });
        }
    }












}
