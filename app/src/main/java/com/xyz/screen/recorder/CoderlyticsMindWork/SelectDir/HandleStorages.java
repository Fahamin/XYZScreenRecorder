package com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir;

public class HandleStorages {
    private String path;
    private StorageType type;

    public enum StorageType {
        Internal,
        External
    }

    public HandleStorages(String str, StorageType storageType) {
        this.path = str;
        this.type = storageType;
    }

    public String getPath() {
        return this.path;
    }

    public StorageType getType() {
        return this.type;
    }
}
