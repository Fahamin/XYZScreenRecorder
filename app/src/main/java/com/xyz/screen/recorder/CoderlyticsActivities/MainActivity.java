package com.xyz.screen.recorder.CoderlyticsActivities;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.ads.InterstitialAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.xyz.screen.recorder.BaseActivity;
import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverInterface;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.PermissionResultListener;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbRecordTime;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStageRecord;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStopService;
import com.xyz.screen.recorder.CoderlyticsServices.BubbleControlService;
import com.xyz.screen.recorder.CoderlyticsServices.RecorderService;
import com.xyz.screen.recorder.CoderlyticsFragments.BaseFragment;
import com.xyz.screen.recorder.CoderlyticsFragments.DisplayScreenshotsFragment;
import com.xyz.screen.recorder.CoderlyticsFragments.SettingsFragment;
import com.xyz.screen.recorder.CoderlyticsFragments.VideosShowingFragment;
import com.xyz.screen.recorder.adsMAnager.FacebookInterAds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;

public class MainActivity extends BaseActivity implements ObserverInterface
//        implements OnNavigationItemSelectedListener
{
    public static MainActivity mainActivity_obj;
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
    com.google.android.gms.ads.interstitial.InterstitialAd mInterstitialAd;
    com.facebook.ads.InterstitialAd fbinterstitialAd;
    BottomAppBar bottomAppBar;
    private FragmentManager fragmentManager;
    private PermissionResultListener mPermissionResultListener;
    private DisplayScreenshotsFragment mScreenshotFragment;
    private SettingsFragment mSettingsFragment;
    private FragmentTransaction mTransaction;
    private VideosShowingFragment mVideosFragment;
    private SharedPreferences prefs;
    private String TAG="TAG";

    private RelativeLayout btnFloatButton;
    private ImageView imRecord;
    private LinearLayout loRecord;
    private TextView tvTimeRecord;

    public static boolean hasPermissions(Context context, String... strArr) {
        if (!(context == null || strArr == null)) {
            for (String checkSelfPermission : strArr) {
                if (ActivityCompat.checkSelfPermission(context, checkSelfPermission) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void backFragment() {
    }

    private void initEvents() {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void onDirectoryChanged() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        String str = "action";
        super.onCreate(bundle);
        setContentView(R.layout.content_multiple_fab);
        ObserverUtils.getInstance().registerObserver((ObserverInterface<ObserverUtils>) this);


        try {
            Log.i("imains", " startact");
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimaryDark));
            }


            btnFloatButton = findViewById(R.id.btn_floatbutton);
            loRecord = findViewById(R.id.lo_record);
            tvTimeRecord = (TextView) findViewById(R.id.tv_time_record);
            imRecord = (ImageView) findViewById(R.id.im_record);
            initControl();



            initViews();
            initEvents();
            addVideoFragment();


            mSettingsFragment = SettingsFragment.newInstance();
            if (getIntent() != null && getIntent().getExtras().containsKey(str) && getIntent().getExtras().get(str).equals("setting")) {
                addSettingsFragment();
            }
        } catch (Exception unused) {
        }

        mainActivity_obj = this;
        findViewById(R.id.btnItemTVVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        findViewById(R.id.btnItemTVImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addVideoFragment();
                //addScreenshotFragment();
            }
        });
        findViewById(R.id.btnItemSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mInterstitialAd != null){
                    mInterstitialAd.show(MainActivity.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            addSettingsFragment();
                            requestNewInterstitial();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            mInterstitialAd = null;
                            Log.d("TAG", "The ad was shown.");
                        }
                    });
                }
                else
                    addSettingsFragment();

            }
        });


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new FacebookInterAds().showFbInterSplash();
            }
        }, 800);

        fbinterstitialAd = new InterstitialAd(this, getString(R.string.fb_inters_dlt));
        fbinterstitialAd.loadAd();

        requestNewInterstitial();


    }

    private void initControl() {
        btnFloatButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!BubbleControlService.isCountdown) {
                    showFloatbtnRecord(RecorderService.isRecording);
                    ObserverUtils.getInstance().notifyObservers(new EvbStageRecord(!BubbleControlService.isRecording));
                }
            }
        });
    }

    public void showFloatbtnRecord(boolean z) {
        if (!z) {
            loRecord.setVisibility(View.INVISIBLE);
            imRecord.setVisibility(View.VISIBLE);
            return;
        }
        loRecord.setVisibility(View.VISIBLE);
        imRecord.setVisibility(View.INVISIBLE);
    }

    public void notifyAction(Object obj) {
        if (obj instanceof EvbRecordTime) {
            showFloatbtnRecord(true);
            tvTimeRecord.setText(((EvbRecordTime) obj).time);
        } else if (obj instanceof EvbStageRecord) {
            tvTimeRecord.setText("00:00");
            showFloatbtnRecord(((EvbStageRecord) obj).isStart);
        } else if (obj instanceof EvbStopService) {
            //btnFloatButton.setVisibility(8);
            loRecord.setVisibility(View.INVISIBLE);
            imRecord.setVisibility(View.VISIBLE);
        }
    }


    private void initViews() {
        try {
            fragmentManager = getSupportFragmentManager();

            bottomAppBar = findViewById(R.id.bottomAppBar);
            setSupportActionBar(bottomAppBar);


            bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                    View view = getLayoutInflater().inflate(R.layout.bottomsheet_navigation, null);

                    NavigationView navigationView = view.findViewById(R.id.navigationView);
                    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                            NavigationItemSelected(menuItem);
                            bottomSheetDialog.dismiss();
                            return true;
                        }
                    });

                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();

                }
            });

        } catch (Exception unused) {
        }
    }

    @Override
    protected void onResume() {
        if (hasPermissions(MainActivity.this, PERMISSIONS) && BubbleControlService.getInstance() == null) {
            MainActivity.this.startService();
            btnFloatButton.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    public void setPermissionResultListener(PermissionResultListener permissionResultListener) {
        mPermissionResultListener = permissionResultListener;
    }

    public boolean requestPermissionStorage() {
        Log.i("iaminfg", "requestPermissionStorage()");
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        new Builder(this).setTitle(getString(R.string.storage_permission_request_title))
                .setMessage(getString(R.string.storage_permission_request_summary))
                .setPositiveButton(getString(R.string.ok), new OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (!hasPermissions(MainActivity.this, PERMISSIONS)) {


                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, CoderlyticsConstants.EXTDIR_REQUEST_CODE);


                        }
                        dialogInterface.dismiss();
                    }
                }).setNeutralButton("EXIT", new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).setCancelable(false).create().show();
        return false;
    }

    private void addFragment(final Fragment fragment, int pos) {
//        AllFragmentsTab allFragmentsTab=new AllFragmentsTab(pos);

        try {
            mTransaction = fragmentManager.beginTransaction();
            mTransaction.replace(R.id.fragment_content, fragment);
            mTransaction.commit();

            if (fragment instanceof BaseFragment) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                ((BaseFragment) fragment).onVisibleFragment();
                            }
                        });
                    }
                }, 100);
            }


        } catch (Exception unused) {
        }
    }

    private void privacyDialog() {

        AlertDialog alertPrivacy = null;

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.privacy_dialog, null);
        alertDialog.setView(view);

        final TextView privacyData = view.findViewById(R.id.privacyData);
        final StringBuilder total = new StringBuilder();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader reader = null;
                    reader = new BufferedReader(
                            new InputStreamReader(getAssets().open("privacy.txt")));

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        total.append(line);
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("lengthL", "run: asdaca" + total.length());
                        privacyData.setText(Html.fromHtml(total.toString().trim()));
                    }
                });
            }

        }).start();


        try {
            alertPrivacy = alertDialog.create();
            alertPrivacy.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        } catch (Exception e) {
            e.printStackTrace();
        }

        final TextView privacyButt = view.findViewById(R.id.privacyButton);

        if (alertPrivacy != null && alertPrivacy.isShowing()) {
            alertPrivacy.dismiss();
        } else {
            alertPrivacy.show();
        }

        final AlertDialog finalAlertPrivacy = alertPrivacy;
        privacyButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalAlertPrivacy.dismiss();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        menuItem.getItemId();
        return super.onOptionsItemSelected(menuItem);
    }

    //@Override
    public boolean NavigationItemSelected(MenuItem menuItem) {
        try {

            switch (menuItem.getItemId()) {
                case R.id.nav_gallery:
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addScreenshotFragment();

                        }
                    }, 250);

                    break;
                case R.id.nav_more:
                    Utils.openURL(this, Utils.getAppUrl(this));
                    break;
                case R.id.nav_policy:

//                    Utils.openURL(this, getResources().getString(R.string.link_policy));
                    privacyDialog();
                    break;
                case R.id.nav_settings:
                    Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mInterstitialAd != null){
                                mInterstitialAd.show(MainActivity.this);
                                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        addSettingsFragment();
                                        requestNewInterstitial();
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent();
                                        mInterstitialAd = null;
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                            }
                            else
                                addSettingsFragment();


                        }
                    }, 200);
                    break;
                case R.id.nav_share:
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.SEND");
                    intent.putExtra("android.intent.extra.TEXT", Utils.getAppUrl(this));
                    intent.setType("text/plain");
                    startActivity(intent);
                    break;
                case R.id.nav_video:
                    Handler handler3 = new Handler();
                    handler3.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addVideoFragment();


                        }
                    }, 200);
                    break;
            }

        } catch (Exception unused) {
        }
        return true;
    }

    private void addVideoFragment() {
        if (mVideosFragment == null) {
            mVideosFragment = VideosShowingFragment.newInstance();
        }
        addFragment(mVideosFragment, 0);
        if (fbinterstitialAd.isAdLoaded()) {

            fbinterstitialAd.show();

        }

    }

    private void addScreenshotFragment() {
        if (mScreenshotFragment == null) {
            mScreenshotFragment = DisplayScreenshotsFragment.newInstance();
        }

        addFragment(mScreenshotFragment, 1);
        if (fbinterstitialAd.isAdLoaded())
            fbinterstitialAd.show();
    }

    private void addSettingsFragment() {
        if (mSettingsFragment == null) {
            mSettingsFragment = SettingsFragment.newInstance();
        }
        addFragment(mSettingsFragment, 2);

    }

    @TargetApi(23)
    public void requestSystemWindowsPermission(int i) {
        if (!Settings.canDrawOverlays(this)) {
            StringBuilder sb = new StringBuilder();
            sb.append("package:");
            sb.append(BuildConfig.APPLICATION_ID);
            startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(sb.toString())), i);
        }
    }

    @TargetApi(23)
    private void setSystemWindowsPermissionResult(int i) {
        String str = "System Windows Permission";
        if (VERSION.SDK_INT < 23) {
            mPermissionResultListener.onPermissionResult(i, new String[]{str}, new int[]{0});
        } else if (Settings.canDrawOverlays(this)) {
            mPermissionResultListener.onPermissionResult(i, new String[]{str}, new int[]{0});
        } else {
            mPermissionResultListener.onPermissionResult(i, new String[]{str}, new int[]{-1});
        }
    }

    public void requestPermissionCamera() {
        String str = "android.permission.CAMERA";
        if (ContextCompat.checkSelfPermission(this, str) != 0) {
            ActivityCompat.requestPermissions(this, new String[]{str}, CoderlyticsConstants.CAMERA_REQUEST_CODE);
        }
    }

    public void requestPermissionAudio(int i) {
        String str = "android.permission.RECORD_AUDIO";
        if (ContextCompat.checkSelfPermission(this, str) != 0) {
            ActivityCompat.requestPermissions(this, new String[]{str}, i);
        }
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 1110) {
            int length = iArr.length;
            String str = CoderlyticsConstants.TAG;
            if (length <= 0 || iArr[0] == 0) {
                startService();
                Log.d(str, "write storage Permission granted");
                Utils.createDir();
            } else {
                Log.d(str, "write storage Permission Denied");
            }
        }
        PermissionResultListener permissionResultListener = mPermissionResultListener;
        if (permissionResultListener != null) {
            permissionResultListener.onPermissionResult(i, strArr, iArr);
        }
    }

    public void startService() {
        startService(new Intent(this, BubbleControlService.class));
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        com.google.android.gms.ads.interstitial.InterstitialAd.load(this,getResources().getString(R.string.admob_interstitial_id), adRequest, new InterstitialAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");
            }


            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });

    }

    public void launchActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("action", "setting");
        startActivity(intent);

        Log.i("iaminsd", " launchActivity");

    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

}
