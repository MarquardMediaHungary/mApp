package com.onceapps.m.models;

import android.text.TextUtils;

import com.onceapps.core.util.Logger;
import com.onceapps.core.util.SerializationUtils;

import java.io.IOException;
import java.util.ArrayList;

public class FollowedList extends ArrayList<FollowedItem> {

    @Override
    public String toString() {
        try {
            return SerializationUtils.serializeToJson(this);
        } catch (Exception e) {
            Logger.error(e, "Error serializing FollowedList");
            throw new IllegalStateException("un-serializable object", e);
        }
    }

    public static FollowedList fromJsonString(String jsonString) {
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return SerializationUtils.deserializeJson(FollowedList.class, jsonString);
            } catch (IOException e) {
                Logger.error(e, "Error deserializing FollowedList");
            }
        }

        return null;
    }

    public boolean isFollowing(Integer id) {
        boolean isFollowing = false;
        for (FollowedItem item : this) {
            if (item.getFollowedId().equals(id)) {
                isFollowing = true;
            }
        }
        return isFollowing;
    }

}
