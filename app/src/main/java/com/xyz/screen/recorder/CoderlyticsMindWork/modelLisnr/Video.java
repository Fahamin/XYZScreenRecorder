package com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr;

import android.graphics.Bitmap;
import java.io.File;
import java.util.Date;

public class Video implements Comparable<Video> {
    private String FileName;
    private File file;
    private long idVideo;
    private boolean isSection = false;
    private boolean isSelected = false;
    private Date lastModified;
    private Bitmap thumbnail;

    public Video(boolean z, Date date)
    {
        this.isSection = z;
        this.lastModified = date;
    }

    public Video(String str, File file2, Bitmap bitmap, Date date) {
        this.FileName = str;
        this.file = file2;
        this.thumbnail = bitmap;
        this.lastModified = date;
    }

    public Video(long j, String str, File file2, Bitmap bitmap, Date date) {
        this.idVideo = j;
        this.FileName = str;
        this.file = file2;
        this.thumbnail = bitmap;
        this.lastModified = date;
    }

    public long getIdVideo() {
        return this.idVideo;
    }

    public String getFileName() {
        return this.FileName;
    }

    public File getFile() {
        return this.file;
    }

    public Bitmap getThumbnail() {
        return this.thumbnail;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public boolean isSection() {
        return this.isSection;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean z) {
        this.isSelected = z;
    }

    public void setSection(boolean z) {
        this.isSection = z;
    }

    public int compareTo(Video video) {
        return getLastModified().compareTo(video.getLastModified());
    }
}
