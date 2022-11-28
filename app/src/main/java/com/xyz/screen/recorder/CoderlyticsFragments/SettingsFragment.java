package com.xyz.screen.recorder.CoderlyticsFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
//import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsActivities.MainActivity;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants.ASPECT_RATIO;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.PrefUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir.SelectFolderDialog;
import com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir.OnDirectorySelectedListerner;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.PermissionResultListener;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.HideService;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.ShowService;
import com.xyz.screen.recorder.CoderlyticsServices.BubbleControlService;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

public class SettingsFragment extends Fragment implements PermissionResultListener, OnDirectorySelectedListerner, OnClickListener {

    public MainActivity activity;

    private SwitchCompat cbCamera;
    private SwitchCompat cbFloatControls;


    private SwitchCompat cbVibrate;

    public SelectFolderDialog selectFolderDialog;

    public View mRootView;



    public SharedPreferences prefs;
    String[] resEntries;
    String[] resEntryValues;
    FrameLayout frameLayout;

    private float bitsToMb(float f) {
        return f / 1048576.0f;
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(layoutInflater, viewGroup, bundle);
        mRootView = layoutInflater.inflate(R.layout.fragment_settings, viewGroup, false);
        mRootView.setBackgroundColor(getResources().getColor(R.color.globalWhite));

        initViews();
        initEvents();
        return mRootView;
    }

    private void initEvents() {
        mRootView.findViewById(R.id.layout_vibrate).setOnClickListener(this);

        mRootView.findViewById(R.id.layout_timer).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_resolution).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_frams).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_bit_rate).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_orientation).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_audio).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_location).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_name_format).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_name_prefix).setOnClickListener(this);
        mRootView.findViewById(R.id.layout_use_float_controls).setOnClickListener(this);

        mRootView.findViewById(R.id.layout_camera_overlay).setOnClickListener(this);


        cbFloatControls.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    try {
                        requestSystemWindowsPermission(CoderlyticsConstants.FLOATING_CONTROLS_SYSTEM_WINDOWS_CODE);
                        if (BubbleControlService.getInstance() == null) {
                            ((MainActivity) getActivity()).startService();
                        }
                        ObserverUtils.getInstance().notifyObservers(new ShowService());
                    } catch (Exception unused) {
                        return;
                    }
                } else {
                    ObserverUtils.getInstance().notifyObservers(new HideService());
                }
                PrefUtils.saveBooleanValue(getActivity(), getString(R.string.preference_floating_control_key), z);
            }
        });


        cbCamera.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    requestCameraPermission();
                    requestSystemWindowsPermission(CoderlyticsConstants.CAMERA_SYSTEM_WINDOWS_CODE);
                }
                PrefUtils.saveBooleanValue(getActivity(), getString(R.string.preference_camera_overlay_key), z);
            }
        });


        cbVibrate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                PrefUtils.saveBooleanValue(getActivity(), getString(R.string.preference_vibrate_key), z);
            }
        });
    }

    private void initViews() {
        setPermissionListener();
        Activity activity2 = getActivity();
        String string = getString(R.string.savelocation_key);
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append(File.separator);
        sb.append(CoderlyticsConstants.APPDIR);
        String readStringValue = PrefUtils.readStringValue(activity2, string, sb.toString());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        selectFolderDialog = new SelectFolderDialog(getActivity());
        selectFolderDialog.setOnDirectoryClickedListerner(this);
        selectFolderDialog.setCurrentDir(readStringValue);
        selectFolderDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {

                SelectFolderDialog.onDirectorySelectedListerner.onDirectorySelected();
            }
        });
        cbFloatControls = (SwitchCompat) mRootView.findViewById(R.id.cb_use_float_controls);

        cbCamera = (SwitchCompat) mRootView.findViewById(R.id.cb_camera_overlay);


        cbVibrate = (SwitchCompat) mRootView.findViewById(R.id.cb_vibrate);
        cbFloatControls.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_floating_control_key), true));

        cbCamera.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_camera_overlay_key), false));


        cbVibrate.setChecked(PrefUtils.readBooleanValue(getActivity(), getString(R.string.preference_vibrate_key), true));

        checkAudioRecPermission();
        if (cbFloatControls.isChecked()) {
            requestSystemWindowsPermission(CoderlyticsConstants.FLOATING_CONTROLS_SYSTEM_WINDOWS_CODE);
        }
        if (cbCamera.isChecked()) {
            requestCameraPermission();
            requestSystemWindowsPermission(CoderlyticsConstants.CAMERA_SYSTEM_WINDOWS_CODE);
        }
        updateResolution();
        updateFPS();
        updateBitRate();
        updateOrientation();
        updateAudio();
        updateFileName();
        updateNamePrefix();
        updateLocation();
        updateTimer();


    }

    private void updateLocation() {
        TextView textView = (TextView) mRootView.findViewById(R.id.value_location);
        Activity activity2 = getActivity();
        String string = getString(R.string.savelocation_key);
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append(File.separator);
        sb.append(CoderlyticsConstants.APPDIR);
        textView.setText(PrefUtils.readStringValue(activity2, string, sb.toString()));
    }


    public void updateNamePrefix() {
        ((TextView) mRootView.findViewById(R.id.value_name_prefix)).setText(PrefUtils.readStringValue(getActivity(), getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX));
    }


    public void updateFileName() {
        TextView textView = (TextView) mRootView.findViewById(R.id.value_name_format);
        StringBuilder sb = new StringBuilder();
        sb.append(PrefUtils.readStringValue(getActivity(), getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX));
        sb.append("_");
        sb.append(PrefUtils.readStringValue(getActivity(), getString(R.string.filename_key), PrefUtils.VALUE_NAME_FORMAT));
        textView.setText(sb.toString());
    }


    public void updateAudio() {
        ((TextView) mRootView.findViewById(R.id.value_audio)).setText(
                Utils.getValue(getResources().getStringArray(R.array.audioSettingsEntries), getResources().getStringArray(R.array.audioSettingsValues), PrefUtils.readStringValue(getActivity(), getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO)));
    }


    public void updateOrientation() {
        ((TextView) mRootView.findViewById(R.id.value_orientation)).setText(
                Utils.getValue(getResources().getStringArray(R.array.orientationEntries), getResources().getStringArray(R.array.orientationValues), PrefUtils.readStringValue(getActivity(), getString(R.string.orientation_key), PrefUtils.VALUE_ORIENTATION)));
    }


    public void updateBitRate() {
        ((TextView) mRootView.findViewById(R.id.value_bit_rate)).setText(
                Utils.getValue(getResources().getStringArray(R.array.bitrateArray), getResources().getStringArray(R.array.bitratesValue), PrefUtils.readStringValue(getActivity(), getString(R.string.bitrate_key), PrefUtils.VALUE_BITRATE)));
    }


    public void updateFPS() {
        ((TextView) mRootView.findViewById(R.id.value_frams)).setText(
                Utils.getValue(getResources().getStringArray(R.array.fpsArray), getResources().getStringArray(R.array.fpsArray), PrefUtils.readStringValue(getActivity(), getString(R.string.fps_key), PrefUtils.VALUE_FRAMES)));
    }


    public void checkAudioRecPermission() {
        char c;
        String[] stringArray = getResources().getStringArray(R.array.audioSettingsEntries);
//        String[] stringArray2 = getResources().getStringArray(R.array.audioSettingsValues);
        String[] stringArray2 = {"1", "1", "2", "3"};
        Log.i("iaminu", "stringArray2 = " + stringArray2[1]);
        Activity activity2 = getActivity();
        String string = getString(R.string.audiorec_key);
        String str = PrefUtils.VALUE_AUDIO;
        String value = Utils.getValue(stringArray, stringArray2, PrefUtils.readStringValue(activity2, string, str));
        int hashCode = value.hashCode();
        if (hashCode != 49) {
            if (hashCode == 50 && value.equals(ExifInterface.GPS_MEASUREMENT_2D)) {
                c = 1;
                if (c == 0) {
                    requestAudioPermission(CoderlyticsConstants.AUDIO_REQUEST_CODE);
                } else if (c == 1) {
                    requestAudioPermission(CoderlyticsConstants.INTERNAL_AUDIO_REQUEST_CODE);
                }
                updateAudio();
            }
        } else if (value.equals(str)) {
            c = 0;
            if (c == 0) {
            }
            updateAudio();
        }
        c = 65535;
        if (c == 0) {
        }
        updateAudio();
    }


    public void updateResolution() {
        ((TextView) mRootView.findViewById(R.id.value_resolution)).setText(
                Utils.getValue(getResources().getStringArray(R.array.resolutionsArray),
                        getResources().getStringArray(R.array.resolutionValues), PrefUtils.readStringValue(getActivity(), getString(R.string.res_key), PrefUtils.VALUE_RESOLUTION)));
    }

    private ArrayList<String> buildEntries(int i) {
        int screenWidth = getScreenWidth(getRealDisplayMetrics());
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(getResources().getStringArray(i)));
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if (screenWidth < Integer.parseInt((String) it.next())) {
                it.remove();
            }
        }
        StringBuilder sb = new StringBuilder();
        String str = "";
        sb.append(str);
        sb.append(screenWidth);
        if (!arrayList.contains(sb.toString())) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str);
            sb2.append(screenWidth);
            arrayList.add(sb2.toString());
        }
        return arrayList;
    }

    private DisplayMetrics getRealDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private int getScreenWidth(DisplayMetrics displayMetrics) {
        return displayMetrics.widthPixels;
    }

    private int getScreenHeight(DisplayMetrics displayMetrics) {
        return displayMetrics.heightPixels;
    }

    @Deprecated
    private ASPECT_RATIO getAspectRatio() {
        float screenWidth = (float) getScreenWidth(getRealDisplayMetrics());
        float screenHeight = (float) getScreenHeight(getRealDisplayMetrics());
        return ASPECT_RATIO.valueOf(screenWidth > screenHeight ? screenWidth / screenHeight : screenHeight / screenWidth);
    }

    private void setPermissionListener() {
        if (getActivity() != null && (getActivity() instanceof MainActivity)) {
            activity = (MainActivity) getActivity();
            activity.setPermissionResultListener(this);
        }
    }

    public void onResume() {
        if (BubbleControlService.getInstance() == null) {
            ((MainActivity) getActivity()).startService();
        }
        super.onResume();
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
    }

    @SuppressLint("ResourceType")
    private void showInternalAudioWarning(boolean z) {
        int i;
        final int i2;
        if (z) {
            i = R.string.alert_dialog_r_submix_audio_warning_message;
            i2 = CoderlyticsConstants.INTERNAL_R_SUBMIX_AUDIO_REQUEST_CODE;
        } else {
            i = R.string.alert_dialog_internal_audio_warning_message;
            i2 = CoderlyticsConstants.INTERNAL_AUDIO_REQUEST_CODE;
        }
        new Builder(activity).setTitle(R.string.alert_dialog_internal_audio_warning_title).setMessage(i).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                requestAudioPermission(i2);
            }
        }).setNegativeButton(R.string.alert_dialog_internal_audio_warning_negative_btn_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                prefs.edit().putBoolean(CoderlyticsConstants.PREFS_INTERNAL_AUDIO_DIALOG_KEY, true).apply();
                requestAudioPermission(CoderlyticsConstants.INTERNAL_AUDIO_REQUEST_CODE);
            }
        }).create().show();
    }

    public void requestAudioPermission(int i) {
        MainActivity mainActivity = activity;
        if (mainActivity != null) {
            mainActivity.requestPermissionAudio(i);
        }
    }

    public void requestCameraPermission() {
        MainActivity mainActivity = activity;
        if (mainActivity != null) {
            mainActivity.requestPermissionCamera();
        }
    }


    public void requestSystemWindowsPermission(int i) {
        if (activity == null || VERSION.SDK_INT < 23) {
            getActivity().startService(new Intent(getActivity(), BubbleControlService.class));
            return;
        }
        activity.requestSystemWindowsPermission(i);
    }

    @SuppressLint("ResourceType")
    private void showPermissionDeniedDialog() {
        new Builder(activity).setTitle(R.string.alert_permission_denied_title).setMessage(R.string.alert_permission_denied_message).setPositiveButton(17039379, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (activity != null) {
                    activity.requestPermissionStorage();
                }
            }
        }).setNegativeButton(17039369, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setIconAttribute(16843605).setCancelable(false).create().show();
    }

    public void onPermissionResult(int i, String[] strArr, int[] iArr) {
        String str = "System Windows permission granted";
        String str2 = "Record audio permission granted.";
        String str3 = "System Windows permission denied";
        String str4 = "0";
        String str5 = "Record audio permission denied";
        String str6 = CoderlyticsConstants.TAG;
        switch (i) {
            case CoderlyticsConstants.EXTDIR_REQUEST_CODE /*1110*/:
                if (iArr.length > 0 && iArr[0] == -1) {
                    Log.d(str6, "Storage permission denied. Requesting again");
                    mRootView.findViewById(R.id.layout_location).setEnabled(false);
                    showPermissionDeniedDialog();
                } else if (iArr.length > 0 && iArr[0] == 0) {
                    mRootView.findViewById(R.id.layout_location).setEnabled(true);
                }
                return;
            case CoderlyticsConstants.AUDIO_REQUEST_CODE:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(str6, str5);
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), str4);
                } else {
                    Log.d(str6, str2);
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO);
                }
                updateAudio();
                return;
            case CoderlyticsConstants.FLOATING_CONTROLS_SYSTEM_WINDOWS_CODE:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(str6, str3);
                    cbFloatControls.setChecked(false);
                } else {
                    Log.d(str6, str);
                    cbFloatControls.setChecked(true);
                    getActivity().startService(new Intent(getActivity(), BubbleControlService.class));
                }
                return;
            case CoderlyticsConstants.CAMERA_REQUEST_CODE:
                if (iArr.length > 0 && iArr[0] == 0) {
                    Log.d(str6, str);
                    requestSystemWindowsPermission(CoderlyticsConstants.CAMERA_SYSTEM_WINDOWS_CODE);
                    break;
                } else {
                    Log.d(str6, str3);
                    cbCamera.setChecked(false);
                    break;
                }

            case CoderlyticsConstants.CAMERA_SYSTEM_WINDOWS_CODE:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(str6, str3);
                    cbCamera.setChecked(false);
                } else {
                    Log.d(str6, str);
                    cbCamera.setChecked(true);
                }
                return;
            case CoderlyticsConstants.INTERNAL_AUDIO_REQUEST_CODE:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(str6, str5);
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), str4);
                } else {
                    Log.d(str6, str2);
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), ExifInterface.GPS_MEASUREMENT_2D);
                }
                updateAudio();
                return;
            case CoderlyticsConstants.INTERNAL_R_SUBMIX_AUDIO_REQUEST_CODE /*1119*/:
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(str6, str5);
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), str4);
                } else {
                    Log.d(str6, str2);
                    PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), "3");
                }
                updateAudio();
                return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Unknown permission request with request code: ");
        sb.append(i);
        Log.d(str6, sb.toString());
    }

    public void onDirectorySelected() {
        Log.d(CoderlyticsConstants.TAG, "In settings fragment");
        if (getActivity() != null && (getActivity() instanceof MainActivity)) {
            ((MainActivity) getActivity()).onDirectoryChanged();
        }
        updateLocation();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_audio:
                openAudioDialog();
                return;
            case R.id.layout_bit_rate:
                openBitRate();
                return;
            case R.id.layout_camera_overlay:
                SwitchCompat switchCompat = cbCamera;
                switchCompat.setChecked(!switchCompat.isChecked());
                return;


            case R.id.layout_frams:
                openFramesDialog();
                return;


            case R.id.layout_location:
                selectFolderDialog.show();
                return;
            case R.id.layout_name_format:
                openNameFormat();
                return;
            case R.id.layout_name_prefix:
                openNamePrefix();
                return;
            case R.id.layout_orientation:
                openOrientationDialog();
                return;
            case R.id.layout_resolution:
                openResolutionDialog();
                return;


            case R.id.layout_timer:
                openTimer();
                return;
            case R.id.layout_use_float_controls:
                SwitchCompat switchCompat5 = cbFloatControls;
                switchCompat5.setChecked(!switchCompat5.isChecked());
                return;
            case R.id.layout_vibrate:
                SwitchCompat switchCompat6 = cbVibrate;
                switchCompat6.setChecked(!switchCompat6.isChecked());
                return;
            default:
                return;
        }
    }

    @SuppressLint("ResourceType")
    private void openTimer() {
        final String[] arr = {"1", "3", "5", "7", "10"};
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_timer_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.timerArray),
                Utils.getPosition(arr,
                        PrefUtils.readStringValue(getActivity(), getString(R.string.timer_key), "3")), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PrefUtils.saveStringValue(getActivity(), getString(R.string.timer_key), arr[i]);
                        dialogInterface.dismiss();
                    }
                });
        builder.setPositiveButton(getString(17039370), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateTimer();
            }
        });
        create.show();
    }


    public void updateTimer() {
        String[] arr = {"1", "3", "5", "7", "10"};
        ((TextView) mRootView.findViewById(R.id.value_timer)).setText(
                Utils.getValue(getResources().getStringArray(R.array.timerArray),
                        arr,
                        PrefUtils.readStringValue(getActivity(), getString(R.string.timer_key), "3")));
    }

    @SuppressLint("ResourceType")
    private void openLanguage() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_language_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.language), Utils.getPosition(getResources().getStringArray(R.array.languageValue), PrefUtils.readStringValue(getActivity(), getString(R.string.language_key), PrefUtils.VALUE_LANGUAGE)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(getActivity(), getString(R.string.language_key), getResources().getStringArray(R.array.languageValue)[i]);
                SettingsFragment settingsFragment = SettingsFragment.this;
                settingsFragment.setLocale(settingsFragment.getResources().getStringArray(R.array.languageValue)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getString(17039370), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateLanguage();
            }
        });
        create.show();
    }

    public void setLocale(String str) {
        Locale locale = new Locale(str);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, displayMetrics);
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }


    public void updateLanguage() {

    }

    private void openNamePrefix() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(getString(R.string.preference_filename_prefix_title));
        final EditText editText = new EditText(getActivity());
        LayoutParams layoutParams = new LayoutParams(-1, -2);
        int convertDpToPixel = Utils.convertDpToPixel(20.0f, getActivity());
        layoutParams.setMargins(convertDpToPixel, convertDpToPixel, convertDpToPixel, convertDpToPixel);
        editText.setLayoutParams(layoutParams);
        editText.setText(PrefUtils.readStringValue(getActivity(), getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX));
        editText.setSelection(editText.getText().toString().length());
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton(getString(17039370), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(getActivity(), getString(R.string.fileprefix_key), editText.getText().toString());
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateNamePrefix();
            }
        });
        create.show();
    }

    private void openNameFormat() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_filename_format_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.filename), Utils.getPosition(getResources().getStringArray(R.array.filename), PrefUtils.readStringValue(getActivity(), getString(R.string.filename_key), PrefUtils.VALUE_NAME_FORMAT)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(getActivity(), getString(R.string.filename_key), getResources().getStringArray(R.array.filename)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateFileName();
            }
        });
        create.show();
    }

    private void openAudioDialog() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_audio_record_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.audioSettingsEntries), Utils.getPosition(getResources().getStringArray(R.array.audioSettingsValues), PrefUtils.readStringValue(getActivity(), getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(getActivity(), getString(R.string.audiorec_key), getResources().getStringArray(R.array.audioSettingsValues)[i]);
                checkAudioRecPermission();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateAudio();
            }
        });
        create.show();
    }

    private void openOrientationDialog() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_orientation_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.orientationEntries),
                Utils.getPosition(getResources().getStringArray(R.array.orientationValues), PrefUtils.readStringValue(getActivity(), getString(R.string.orientation_key), PrefUtils.VALUE_ORIENTATION)), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PrefUtils.saveStringValue(getActivity(), getString(R.string.orientation_key), getResources().getStringArray(R.array.orientationValues)[i]);
                        dialogInterface.dismiss();
                    }
                });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateOrientation();
            }
        });
        create.show();
    }

    private void openBitRate() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_bit_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.bitrateArray), Utils.getPosition(getResources().getStringArray(R.array.bitratesValue), PrefUtils.readStringValue(getActivity(), getString(R.string.bitrate_key), PrefUtils.VALUE_BITRATE)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(getActivity(), getString(R.string.bitrate_key), getResources().getStringArray(R.array.bitratesValue)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateBitRate();
            }
        });
        create.show();
    }

    private void openFramesDialog() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_fps_title);
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.fpsArray), Utils.getPosition(getResources().getStringArray(R.array.fpsArray), PrefUtils.readStringValue(getActivity(), getString(R.string.fps_key), PrefUtils.VALUE_FRAMES)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PrefUtils.saveStringValue(getActivity(), getString(R.string.fps_key), getResources().getStringArray(R.array.fpsArray)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateFPS();
            }
        });
        create.show();
    }

    @SuppressLint("ResourceType")
    private void openResolutionDialog() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.preference_resolution_title);
        final String[] stringArray = getResources().getStringArray(R.array.resolutionsArray);
        builder.setSingleChoiceItems(stringArray, Utils.getPosition(getResources().getStringArray(R.array.resolutionValues), PrefUtils.readStringValue(getActivity(), getString(R.string.res_key), PrefUtils.VALUE_RESOLUTION)), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Integer.parseInt(getNativeRes()) < Integer.parseInt(getResources().getStringArray(R.array.resolutionValues)[i])) {
                    Activity activity = getActivity();
                    StringBuilder sb = new StringBuilder();
                    sb.append(getString(R.string.notsupport));
                    sb.append(stringArray[i]);
                    Toast.makeText(activity, sb.toString(), Toast.LENGTH_LONG).show();
                    return;
                }
                PrefUtils.saveStringValue(getActivity(), getString(R.string.res_key), getResources().getStringArray(R.array.resolutionValues)[i]);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                updateResolution();
            }
        });
        create.show();
    }


    public String getNativeRes() {
        return String.valueOf(getScreenWidth(getRealDisplayMetrics()));
    }
}
