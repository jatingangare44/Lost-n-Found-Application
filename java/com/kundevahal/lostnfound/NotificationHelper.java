package com.kundevahal.lostnfound;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationHelper{
    private static final String CHANNEL_ID = "lost_item_channel";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lost Item Channel";
            String description = "Channel for lost item notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendNotificationToAuthenticators(Context context, String title, String message) {
        DatabaseReference authenticatorsRef = FirebaseDatabase.getInstance().getReference("Authenticator");
        authenticatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot authenticatorSnapshot : snapshot.getChildren()) {
                    String authenticatorId = authenticatorSnapshot.getKey();

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.lostnfoundpxnbg)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(authenticatorId.hashCode(), builder.build());
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationHelper", "Failed to fetch authenticators: " + error.getMessage());
            }
        });
    }

    public void sendNotificationToUsers(Context context,String email, String title, String message) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("accepted_items");
        usersRef.orderByChild("userEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.lostnfoundpxnbg)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(userId.hashCode(), builder.build());
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationHelper", "Failed to fetch users: " + error.getMessage());
            }
        });
    }

    // Method to listen for new item additions by users
    public void listenForNewItemAdded(Context context) {
        DatabaseReference userItemsRef = FirebaseDatabase.getInstance().getReference("Users"); // Reference to the user items node

        userItemsRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // When a new item is added by a user, trigger a notification to authenticators
                String newItemTitle = "New Lost Item Added";
                String newItemMessage = "A new lost item has been added. Please verify.";

                sendNotificationToAuthenticators(context, newItemTitle, newItemMessage);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Handle item updates if necessary
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle item removal if necessary
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // Handle child moved if necessary
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("NotificationHelper", "Error fetching new items: " + databaseError.getMessage());
            }
        });
    }

}
