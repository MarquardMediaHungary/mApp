package com.onceapps.m.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.onceapps.core.util.Broadcast;
import com.onceapps.m.models.MediaStatus;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;

@EReceiver
public class MediaListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // leave this empty
    }

    @ReceiverAction(actions = Intent.ACTION_MEDIA_REMOVED)
    void onMediaRemoved() {
        Broadcast.postEvent(MediaStatus.Unavailable);
    }

    @ReceiverAction(actions = Intent.ACTION_MEDIA_MOUNTED)
    void onMediaMounted() {
        Broadcast.postEvent(MediaStatus.Mounted);
    }
}
