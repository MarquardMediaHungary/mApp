package com.onceapps.m.models;

import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by balage on 20/04/16.
 * to store local magazine statuses
 */
public class MagazineStatuses extends LinkedHashMap<Integer, MagazineStatus> implements Iterable<MagazineStatus> {

    public MagazineStatuses() {
    }

    public void replace(MagazineStatus magazineStatus) {
        synchronized (this) {
            put(magazineStatus.getMagazine().getId(), magazineStatus);
        }
    }

    @Override
    public MagazineStatus get(Object key) {
        synchronized (this) {
            return super.get(key);
        }
    }

    public MagazineStatus getByMagazine(@NonNull Magazine magazine) {
        synchronized (this) {
            return get(magazine.getId());
        }
    }

    public void add(@NonNull MagazineStatus status) {
        synchronized (this) {
            put(status.getMagazine().getId(), status);
        }
    }

    @Override
    public MagazineStatus put(Integer key, MagazineStatus value) {
        synchronized (this) {
            return super.put(key, value);
        }
    }


    @Override
    public void clear() {
        synchronized (this) {
            super.clear();
        }
    }


    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return super.isEmpty();
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return super.size();
        }
    }

    public void addAll(MagazineStatuses statuses) {
        synchronized (this) {
            for (MagazineStatus status : statuses) {
                put(status.getMagazine().getId(), status);
            }
        }
    }

    public MagazineStatus remove(@NonNull MagazineStatus magazineStatus) {
        synchronized (this) {
            return remove(magazineStatus.getMagazine().getId());
        }
    }

    @Override
    public Iterator<MagazineStatus> iterator() {
        return values().iterator();
    }
}
