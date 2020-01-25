package com.example.follo;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static void displayNotification(Context context, String title, String body){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NotificationActivity.CHANNEL_ID)
                        .setSmallIcon(R.drawable.messages)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationMngr = NotificationManagerCompat.from(context);

        notificationMngr.notify(1,mBuilder.build());

    }
}
