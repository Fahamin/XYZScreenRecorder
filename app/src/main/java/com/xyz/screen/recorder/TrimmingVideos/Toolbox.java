package com.xyz.screen.recorder.TrimmingVideos;

import java.text.DecimalFormat;

public class Toolbox {
    public static String converTime(String str) {
        String str2 = "00:00";
        DecimalFormat decimalFormat = new DecimalFormat("00");
        try {
            int parseFloat = (int) Float.parseFloat(str);
            if (parseFloat == 0) {
                return str2;
            }
            if (parseFloat < 60) {
                StringBuilder sb = new StringBuilder();
                sb.append("00:");
                sb.append(decimalFormat.format((long) parseFloat));
                return sb.toString();
            }
            int i = parseFloat / 60;
            int i2 = parseFloat % 60;
            String str3 = ":";
            if (i < 60) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(decimalFormat.format((long) i));
                sb2.append(str3);
                sb2.append(decimalFormat.format((long) i2));
                return sb2.toString();
            }
            int i3 = i / 60;
            StringBuilder sb3 = new StringBuilder();
            sb3.append(decimalFormat.format((long) i3));
            sb3.append(str3);
            sb3.append(decimalFormat.format((long) (i % 60)));
            sb3.append(str3);
            sb3.append(decimalFormat.format((long) i2));
            return sb3.toString();
        } catch (Exception unused) {
            return str2;
        }
    }
}
