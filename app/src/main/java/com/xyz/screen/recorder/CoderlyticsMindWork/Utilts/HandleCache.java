package com.xyz.screen.recorder.CoderlyticsMindWork.Utilts;

import androidx.collection.LruCache;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.Photo;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.Video;
import java.util.ArrayList;

public class HandleCache {
    private static HandleCache instance;
    private ArrayList<Photo> arrPhotos = new ArrayList<>();
    private ArrayList<Video> arrVideos = new ArrayList<>();
    private ArrayList<Video> arrVideosEdited = new ArrayList<>();
    private LruCache<Object, Object> lru = new LruCache<>(10485760);

    private HandleCache() {
    }

    public static HandleCache getInstance() {
        if (instance == null) {
            instance = new HandleCache();
        }
        return instance;
    }

    public LruCache<Object, Object> getLru() {
        return this.lru;
    }

    public ArrayList<Photo> getArrPhotos() {
        return this.arrPhotos;
    }

    public void setArrPhotos(ArrayList<Photo> arrayList) {
        this.arrPhotos.clear();
        this.arrPhotos.addAll(arrayList);
    }

    public ArrayList<Video> getArrVideos() {
        return this.arrVideos;
    }

    public ArrayList<Video> getArrVideosEdited() {
        return this.arrVideosEdited;
    }

    public void setArrVideos(ArrayList<Video> arrayList) {
        this.arrVideos.clear();
        this.arrVideos.addAll(arrayList);
    }

    public void setArrVideosEdited(ArrayList<Video> arrayList) {
        this.arrVideosEdited.clear();
        this.arrVideosEdited.addAll(arrayList);
    }
}
