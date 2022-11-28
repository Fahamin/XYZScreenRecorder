package com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface;

import java.util.ArrayList;
import java.util.List;

public class ObserverUtils<T> implements Subject<ObserverInterface<T>, T> {
    private static ObserverUtils instance;
    private List<ObserverInterface> observers = new ArrayList();

    public static ObserverUtils getInstance() {
        if (instance == null) {
            instance = new ObserverUtils();
        }
        return instance;
    }

    public void registerObserver(ObserverInterface<T> observerInterface) {
        if (!this.observers.contains(observerInterface)) {
            this.observers.add(observerInterface);
        }
    }

    public void removeObserver(ObserverInterface<T> observerInterface) {
        if (this.observers.contains(observerInterface)) {
            this.observers.remove(observerInterface);
        }
    }

    public void notifyObservers(T t) {
        for (ObserverInterface notifyAction : this.observers) {
            notifyAction.notifyAction(t);
        }
    }
}
