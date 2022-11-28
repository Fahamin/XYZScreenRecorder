package com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference.BaseSavedState;

public class SavedStateHandler extends BaseSavedState {
    public static final Creator<SavedStateHandler> CREATOR = new Creator<SavedStateHandler>() {
        public SavedStateHandler createFromParcel(Parcel parcel) {
            return new SavedStateHandler(parcel);
        }

        public SavedStateHandler[] newArray(int i) {
            return new SavedStateHandler[i];
        }
    };
    public final Bundle dialogState;
    public final String selectedDir;

    public SavedStateHandler(Parcelable parcelable, String str, Bundle bundle) {
        super(parcelable);
        this.selectedDir = str;
        this.dialogState = bundle;
    }

    public SavedStateHandler(Parcel parcel) {
        super(parcel);
        this.selectedDir = parcel.readString();
        this.dialogState = parcel.readBundle();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.selectedDir);
        parcel.writeBundle(this.dialogState);
    }
}
