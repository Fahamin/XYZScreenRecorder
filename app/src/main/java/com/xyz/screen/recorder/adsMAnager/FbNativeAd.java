package com.xyz.screen.recorder.adsMAnager;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.xyz.screen.recorder.R;


import java.util.ArrayList;
import java.util.List;

public class FbNativeAd
{
    private final String TAG = "iamingh";
    private NativeAd nativeAd;
    private NativeAdLayout nativeAdLayout;
    private LinearLayout adViewl;
    Activity mview;
    View itemView;
    public void loadNativeAd(Activity viewm , View view) {
        mview= viewm;
        itemView=view;
        nativeAd = new NativeAd(mview, mview.getResources().getString(R.string.fb_nativelist));
        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.i(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.i(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.i(TAG, "Native ad is loaded and ready to be displayed!");
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(nativeAd);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.i(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.i(TAG, "Native ad impression logged!");
            }
        });
        AdSettings.addTestDevice("a144f3b4-fada-45cc-9bd6-212429439494");
        nativeAd.loadAd();
    }

    private void inflateAd(NativeAd nativeAd) {

        nativeAd.unregisterView();
        Log.i(TAG, "Native ad inflateAd()!");

        itemView.findViewById(R.id.ll_fbad_container).setVisibility(View.VISIBLE);
        // Add the Ad view into the ad container.
        nativeAdLayout = itemView.findViewById(R.id.native_ad_container);

        nativeAdLayout.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(mview);
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        adViewl = (LinearLayout) inflater.inflate(R.layout.native_ads_fb, nativeAdLayout, false);
        nativeAdLayout.addView(adViewl);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = itemView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(mview, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        AdIconView nativeAdIcon = adViewl.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adViewl.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adViewl.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adViewl.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adViewl.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adViewl.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adViewl.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adViewl,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }

}
