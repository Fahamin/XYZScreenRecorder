package com.CoderlyticsCameraView;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;

public class AspectRatio implements Comparable<AspectRatio>, Parcelable {
    public static final Creator<AspectRatio> CREATOR = new Creator<AspectRatio>() {
        public AspectRatio createFromParcel(Parcel parcel) {
            return AspectRatio.of(parcel.readInt(), parcel.readInt());
        }

        public AspectRatio[] newArray(int i) {
            return new AspectRatio[i];
        }
    };
    private static final SparseArrayCompat<SparseArrayCompat<AspectRatio>> sCache = new SparseArrayCompat<>(16);
    private final int mX;
    private final int mY;

    public int describeContents() {
        return 0;
    }

    public static AspectRatio of(int i, int i2) {
        int gcd = gcd(i, i2);
        int i3 = i / gcd;
        int i4 = i2 / gcd;
        SparseArrayCompat sparseArrayCompat = (SparseArrayCompat) sCache.get(i3);
        if (sparseArrayCompat == null) {
            AspectRatio aspectRatio = new AspectRatio(i3, i4);
            SparseArrayCompat sparseArrayCompat2 = new SparseArrayCompat();
            sparseArrayCompat2.put(i4, aspectRatio);
            sCache.put(i3, sparseArrayCompat2);
            return aspectRatio;
        }
        AspectRatio aspectRatio2 = (AspectRatio) sparseArrayCompat.get(i4);
        if (aspectRatio2 == null) {
            aspectRatio2 = new AspectRatio(i3, i4);
            sparseArrayCompat.put(i4, aspectRatio2);
        }
        return aspectRatio2;
    }

    public static AspectRatio parse(String str) {
        int indexOf = str.indexOf(58);
        String str2 = "Malformed aspect ratio: ";
        if (indexOf != -1) {
            try {
                return of(Integer.parseInt(str.substring(0, indexOf)), Integer.parseInt(str.substring(indexOf + 1)));
            } catch (NumberFormatException e) {
                StringBuilder sb = new StringBuilder();
                sb.append(str2);
                sb.append(str);
                throw new IllegalArgumentException(sb.toString(), e);
            }
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str2);
            sb2.append(str);
            throw new IllegalArgumentException(sb2.toString());
        }
    }

    private AspectRatio(int i, int i2) {
        this.mX = i;
        this.mY = i2;
    }

    public int getX() {
        return this.mX;
    }

    public int getY() {
        return this.mY;
    }

    public boolean matches(Size size) {
        int gcd = gcd(size.getWidth(), size.getHeight());
        return this.mX == size.getWidth() / gcd && this.mY == size.getHeight() / gcd;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof AspectRatio) {
            AspectRatio aspectRatio = (AspectRatio) obj;
            if (this.mX == aspectRatio.mX && this.mY == aspectRatio.mY) {
                z = true;
            }
        }
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mX);
        sb.append(":");
        sb.append(this.mY);
        return sb.toString();
    }

    public float toFloat() {
        return ((float) this.mX) / ((float) this.mY);
    }

    public int hashCode() {
        int i = this.mY;
        int i2 = this.mX;
        return i ^ ((i2 >>> 16) | (i2 << 16));
    }

    public int compareTo(@NonNull AspectRatio aspectRatio) {
        if (equals(aspectRatio)) {
            return 0;
        }
        return toFloat() - aspectRatio.toFloat() > 0.0f ? 1 : -1;
    }

    public AspectRatio inverse() {
        return of(this.mY, this.mX);
    }

    private static int gcd(int i, int i2) {
        while (true) {
            int i3 = i2;
            int i4 = i;
            i = i3;
            if (i == 0) {
                return i4;
            }
            i2 = i4 % i;
        }
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.mX);
        parcel.writeInt(this.mY);
    }
}
