package com.modosa.apkinstaller.activity;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.modosa.apkinstaller.R;
import com.modosa.apkinstaller.util.OpUtil;

import java.io.File;


/**
 * @author dadaewq
 */
public class LaunchAppActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        finish();
    }

    private void init() {
        Intent getIntent = getIntent();

        if (getIntent != null) {
            if (getIntent.hasExtra("notificationId")) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(getIntent.getExtras().getInt("notificationId"));
                }
            }
            String EXTRA_PACKAGE_NAME = "android.intent.extra.PACKAGE_NAME";
            String extraRealPath = "realPath";

            if (getIntent.hasExtra("isTemp")) {
                if (getIntent.hasExtra(extraRealPath)) {
                    File apkFile = new File(getIntent.getStringExtra(extraRealPath) + "");
                    OpUtil.startAnotherInstaller(this, apkFile, getIntent.getBooleanExtra("isTemp", false));

                }
            } else if (getIntent.hasExtra(EXTRA_PACKAGE_NAME)) {
                String packagename = getIntent.getStringExtra(EXTRA_PACKAGE_NAME) + "";
                Log.d("PACKAGE_NAME ==>", packagename);
                try {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(packagename);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, e + "", Toast.LENGTH_LONG).show();
                }
            } else {
                if (getIntent.hasExtra(extraRealPath)) {
                    File apkFile = new File(getIntent.getStringExtra(extraRealPath) + "");
                    OpUtil.deleteSingleFile(apkFile);

                    if (apkFile.exists()) {
                        Toast.makeText(this, R.string.tip_delete_apk_fail, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.tip_delete_apk_success, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }

    }
}
