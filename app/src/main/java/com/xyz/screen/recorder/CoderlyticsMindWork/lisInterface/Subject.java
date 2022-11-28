package com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface;

public interface Subject<T, K> {
    void notifyObservers(K k);

    void registerObserver(T t);

    void removeObserver(T t);
}
