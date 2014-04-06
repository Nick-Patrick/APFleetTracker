package com.aphaulage.apfleettracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NotificationUtil {

    public static final int NOTIFICATION_ID = 100;


    public NotificationCompat.Builder prepareBuilder(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.button_blue_selector)
                        .setContentTitle(context.getString(R.string.day_end))
                        .setContentText(context.getString(R.string.app_name)).setOngoing(true);

        Intent resultIntent = new Intent(context, ActiveJobActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder;
    }

    public NotificationCompat.Builder prepareProxyNotification(Context context){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.button_black_selector)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setOngoing(false);

        Intent resultIntent = new Intent(context, ActiveJobActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder;

    }
}