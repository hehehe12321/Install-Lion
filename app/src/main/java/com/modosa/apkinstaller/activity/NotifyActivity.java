package com.modosa.apkinstaller.activity;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.modosa.apkinstaller.R;
import com.modosa.apkinstaller.util.AppInfoUtil;

/**
 * @author dadaewq
 */
public class NotifyActivity extends Activity {
    private String packageLable;
    private String channelId;
    private String channelName;
    private String versionName = "";
    private NotificationManager notificationManager;
    private Bitmap LargeIcon;
    private boolean avLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        channelId = getIntent().getStringExtra("channelId");
        channelName = getIntent().getStringExtra("channelName");
        packageLable = getIntent().getStringExtra("packageLable");
        String packageName = getIntent().getStringExtra("packageName");

        LargeIcon = AppInfoUtil.getApplicationIcon(this, packageName);

        String[] version = AppInfoUtil.getApplicationVersion(this, packageName);
        if (version != null) {
            versionName = version[0];
        }

//        //如果使用 AppInfoUtils.getApkIcon()在InstallActivity用Bundle传LargeIcon
//        LargeIcon = getIntent().getParcelableExtra("LargeIcon");

        int id = (int) System.currentTimeMillis();

        PendingIntent clickIntent = getContentIntent(this, id, packageName);

        notifyLiveStart(this, clickIntent, id);
        finish();

    }

    private void notifyLiveStart(Activity context, PendingIntent intent, int id) {

        NotificationChannel channel;


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager == null) {
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }
            channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            //是否在桌面icon右上角展示小红点
            channel.enableLights(true);
            //是否在久按桌面图标时显示此渠道的通知
            channel.setShowBadge(true);

            notificationManager.createNotificationChannel(channel);

        }

        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                //设置通知栏标题
                .setContentTitle(String.format(getString(R.string.tip_install_over), packageLable))
                //通知产生的时间，会在通知信息里显示
                .setWhen(System.currentTimeMillis())
                //设置小图标（通知栏没有下拉的图标）
                .setSmallIcon(R.drawable.ic_filter_vintage_black_24dp)
                .setAutoCancel(true)
                .setContentIntent(intent);

        if (avLaunch) {
            //设置通知栏显示内容
            builder.setContentText(String.format(getString(R.string.click_run), versionName));
        } else {
            builder.setContentText(versionName);
        }

        if (LargeIcon != null) {
            //设置右侧大图标
            builder.setLargeIcon(LargeIcon);
        }
        //设置点击通知后自动删除通知

        Notification notification = builder.build();
//        .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
//
//        .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
//
//        .setOngoing(false)//ture，设置他为一个正在进行的通知,通常是用来表示一个后台任务,以某种方式正在等待,如一个文件下载,同步操作
//
//        .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果


        notificationManager.notify(id, notification);

    }

    private PendingIntent getContentIntent(Activity context, int id, String packageName) {

        try {
            avLaunch = true;
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            return PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } catch (Exception e) {
            avLaunch = false;
            e.printStackTrace();
            return null;
        }


    }


}
