package com.xyz.screen.recorder.CoderlyticsActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.xyz.screen.recorder.R;


public class PrivacyPolicyActivity extends Activity {

    boolean isPrivacyAllow = false;
    PrivacyShared sharedPrefs_obj;
     Button btnDone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_privacy_policy);

        sharedPrefs_obj=new PrivacyShared(this);
        if (sharedPrefs_obj.getFirstTerm())
        {

            startActivity(new Intent(PrivacyPolicyActivity.this, SplashScreenActivity.class));

            finish();

        }

      btnDone=findViewById(R.id.btn_start);

    }

    public void getAllowed(View view) {

            sharedPrefs_obj.setFirstTerm(true);
            startActivity(new Intent(PrivacyPolicyActivity.this, SplashScreenActivity.class));
            finish();


    }

    public void PrivacyRead(View view) {
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://unitechprivacypolicy.blogspot.com/2019/03/privacy-policy.html"));
        startActivity(browser);
    }


public class PrivacyShared {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public PrivacyShared(Context context) {
        prefs = context.getSharedPreferences("PrivacyPolicunitech", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }



    public void setFirstTerm(boolean isTrue) {
        editor.putBoolean("privacy_unitech", isTrue);
        editor.commit();
    }

    public boolean getFirstTerm() {
        return prefs.getBoolean("privacy_unitech", false);

    }
}
}

