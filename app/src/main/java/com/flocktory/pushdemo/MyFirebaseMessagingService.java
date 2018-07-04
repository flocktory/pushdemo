package com.flocktory.pushdemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "gcm messaging service";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String m = "Message data payload: " + remoteMessage.getData();
            Log.d(TAG, m);
        }


        Map<String, String> data = remoteMessage.getData();
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        String landingUrl = data.get("url");
        String iconUrl = notification.getIcon();
        //String imageUrl = notification.getImage();
        String messageTitle = notification.getTitle();
        String messageBody = notification.getBody();


        // show notification

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(landingUrl));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, browserIntent,
                PendingIntent.FLAG_ONE_SHOT);


        // по умолчанию всегда передается одна картинка
        // можно как разместить ее справа в пуше (получится небольшое изображение),
        // так и использовать ее в качестве рич-картинки, а справа в пуше разместить лого сайта
        // https://monosnap.com/file/OQotv1Zch0Ef116oYNX7uBafLcRbWI

        Bitmap smallImage = (new FlocktoryApiClient(getApplicationContext())).getPicture(iconUrl);
        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        // картинка справа
                        .setLargeIcon(smallImage)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        // большая картинка снизу
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(smallImage))
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }




}
