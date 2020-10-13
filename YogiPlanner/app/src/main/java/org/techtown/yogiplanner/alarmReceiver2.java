package org.techtown.yogiplanner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class alarmReceiver2 extends BroadcastReceiver {
    private static String CHANNEL_ID = "channel2";
    private static String CHANNEL_NAME = "channel2";


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MainActivity", "브로드캐스트2 호출됨");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(new NotificationChannel(
                        CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
                ));
            }

            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        ArrayList<Todo> items = MainActivity.alarm_items2;
        int id = intent.getIntExtra("id", 0);

        Todo item = items.get(id);

        builder.setSmallIcon(R.drawable.notification)
                .setNumber(1)
                .setContentTitle(item.getName())
                .setContentText(item.getDate()+" "+item.getTime()+" 할 일의 마감이 1시간 남았어요!")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(1, builder.build());

    }
}