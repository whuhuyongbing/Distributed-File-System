package common;

import storage.Storage;

/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-11-05 4:56 p.m.
 * @description
 */
public class Leaf extends Node {
    Storage storage;

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Leaf(String name, Storage storage) {
        super(name);
        this.storage = storage;
    }

    public Leaf(String name) {
        this(name, null);
    }
}
