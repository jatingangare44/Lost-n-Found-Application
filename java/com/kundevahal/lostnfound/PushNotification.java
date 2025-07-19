package com.kundevahal.lostnfound;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotification extends FirebaseMessagingService {
    public PushNotification() {
        super();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        getFirebaseMessage(message.getNotification().getTitle(), message.getNotification().getBody());
    }

    private void getFirebaseMessage(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Notification")
                .setSmallIcon(R.drawable.lostnfoundpxnbg)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        managerCompat.notify(102, builder.build());

    }
}
