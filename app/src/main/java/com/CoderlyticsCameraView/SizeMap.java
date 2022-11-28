package com.CoderlyticsCameraView;

import androidx.collection.ArrayMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

class SizeMap {
    private final ArrayMap<AspectRatio, SortedSet<Size>> mRatios = new ArrayMap<>();

    SizeMap() {
    }

    public boolean add(Size size) {
        for (AspectRatio aspectRatio : this.mRatios.keySet()) {
            if (aspectRatio.matches(size)) {
                SortedSet sortedSet = (SortedSet) this.mRatios.get(aspectRatio);
                if (sortedSet.contains(size)) {
                    return false;
                }
                sortedSet.add(size);
                return true;
            }
        }
        TreeSet treeSet = new TreeSet();
        treeSet.add(size);
        this.mRatios.put(AspectRatio.of(size.getWidth(), size.getHeight()), treeSet);
        return true;
    }

    public void remove(AspectRatio aspectRatio) {
        this.mRatios.remove(aspectRatio);
    }

    
    public Set<AspectRatio> ratios() {
        return this.mRatios.keySet();
    }

    
    public SortedSet<Size> sizes(AspectRatio aspectRatio) {
        return (SortedSet) this.mRatios.get(aspectRatio);
    }

    
    public void clear() {
        this.mRatios.clear();
    }

    
    public boolean isEmpty() {
        return this.mRatios.isEmpty();
    }
}
