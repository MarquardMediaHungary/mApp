package com.onceapps.m.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.RestClient;
import com.onceapps.m.models.Article;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.ui.activites.ArticleActivity_;
import com.onceapps.m.ui.activites.ArticleListActivity_;
import com.onceapps.m.ui.activites.MagazinePreviewActivity_;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Map;

/**
 * MFirebaseMessagingService
 * Created by szipe on 21/06/16.
 */
@EService
public class MFirebaseMessagingService extends FirebaseMessagingService {

    public enum Type {
        APP,
        ARTICLE,
        ISSUE;

        @JsonCreator
        public static Type fromString(String key) {
            for (Type type : Type.values()) {
                if (type.name().equalsIgnoreCase(key)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Bean
    protected RestClient mRestClient;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            String title = ObjectUtils.defaultIfNull(data.get("title"), getString(R.string.app_name));
            String body = ObjectUtils.defaultIfNull(data.get("body"), "");
            Type type = Type.fromString(ObjectUtils.defaultIfNull(data.get("type"), "APP"));
            Long id = (long) -1;
            try {
                id = ObjectUtils.defaultIfNull(Long.parseLong(data.get("id")), (long) -1);
            } catch (NumberFormatException e) {
                Logger.warn("no id in notification");
            }

            sendNotification(title, body, -1, type, id);
        } else {
            Logger.warn("no data in notification");
        }
    }

    public void sendNotification(String title, String content, int notificationId, Type type, Long id) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo_small_white)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{500, 500})
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

        Intent resultIntent = ArticleListActivity_.intent(this).get();
        Class parentActivityClass = ArticleListActivity_.class;
        switch (type) {
            case ARTICLE:
                try {
                    Article article = mRestClient.getArticle(id.intValue());
                    if (!(article == null || (article.getAdditionalProperties() != null && article.getAdditionalProperties().get("code") != null))) {
                        resultIntent = ArticleActivity_.intent(this).article(article).get();
                        parentActivityClass = ArticleActivity_.class;
                    } else {
                        Logger.warn("article not found");
                    }
                } catch (Exception e) {
                    Logger.warn(e, "error getting article");
                }

                break;
            case ISSUE:
                try {
                    Magazine magazine = mRestClient.getMagazine(id.intValue());
                    if (!(magazine == null || (magazine.getAdditionalProperties() != null && magazine.getAdditionalProperties().get("code") != null))) {
                        resultIntent = MagazinePreviewActivity_.intent(this).magazine(magazine).get();
                        parentActivityClass = MagazinePreviewActivity_.class;
                    } else {
                        Logger.warn("magazine not found");
                    }
                } catch (Exception e) {
                    Logger.warn(e, "error getting magazine");
                }
                break;
            default:
                resultIntent = ArticleListActivity_.intent(this).get();
                break;
        }

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(parentActivityClass);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
