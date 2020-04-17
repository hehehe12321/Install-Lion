package com.modosa.apkinstaller.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.modosa.apkinstaller.R;
import com.modosa.apkinstaller.fragment.DpmSettingsFragment;
import com.modosa.apkinstaller.fragment.MainFragment;
import com.modosa.apkinstaller.util.OpUtil;

import java.util.ArrayList;

/**
 * @author dadaewq
 */
public class MainUiActivity extends AppCompatActivity implements MainFragment.MyListener {

    public static final int REQUEST_REFRESH = 233;
    private static final String TAG_MAINUI = "mainUi";
    private static final String TAG_DPM = "dpm";
    private long exitTime = 0;
    private DevicePolicyManager devicePolicyManager;
    private boolean isMain = true;
    private FragmentManager fragmentManager;
    private SharedPreferences spGetPreferenceManager;
    private AlertDialog alertDialog;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();
        swtichIsMainFragment(isMain);

        init();
        confirmPrompt();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == OpUtil.REQUEST_REFRESH_WRITE_PERMISSION && OpUtil.WRITE_PERMISSION.equals(permissions[0]) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickFileToInstall();
        }
    }


    private void init() {
        spGetPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
    }

    private void confirmPrompt() {
        if (!spGetPreferenceManager.getBoolean(OpUtil.SP_KEY_CONFIRM_PROMPT, false)) {
            alertDialog = OpUtil.createDialogConfirmPrompt(this);
            OpUtil.showDialogConfirmPrompt(this, alertDialog);
        }
    }

    private void showMyToast0(final String text) {
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    private void showMyToast0(final int stringId) {
        runOnUiThread(() -> Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_ui, menu);
        return true;
    }

    @Override
    public boolean isDeviceOwner() {
        return devicePolicyManager.isDeviceOwnerApp(getPackageName());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                swtichIsMainFragment(true);
                break;
            case R.id.PickFileToInstall:
//                Log.e("permission", ""+OpUtil.checkWritePermission(this) );
                if (OpUtil.checkWritePermission(this)) {
                    pickFileToInstall();
                } else {
                    OpUtil.requestWritePermission(this);
                }
                break;
            case R.id.Settings:
                Intent settingsIntent = new Intent(Intent.ACTION_VIEW)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setClass(this, SettingsActivity.class);
                try {
                    startActivity(settingsIntent);
                } catch (Exception e) {
                    showMyToast0("" + e);
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void showDialogConfirmPrompt() {

        View view = View.inflate(this, R.layout.confirmprompt_doublecheckbox, null);

        CheckBox checkBox1 = view.findViewById(R.id.confirm_checkbox1);
        CheckBox checkBox2 = view.findViewById(R.id.confirm_checkbox2);
        checkBox1.setText(R.string.checkbox1_instructions_before_use);
        checkBox2.setText(R.string.checkbox2_instructions_before_use);
        checkBox2.setEnabled(false);
        checkBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkBox2.setChecked(false);
            checkBox2.setEnabled(isChecked);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.title_instructions_before_use)
                .setView(view)
                .setPositiveButton(android.R.string.cancel, null)
                .setNeutralButton(android.R.string.ok, (dialog, which) -> {
                    boolean hasBothConfirm = false;
                    if (checkBox1.isChecked() && checkBox2.isChecked()) {
                        hasBothConfirm = true;
                    }
                    spGetPreferenceManager.edit().putBoolean(OpUtil.SP_KEY_CONFIRM_PROMPT, hasBothConfirm).apply();
                });

        alertDialog = builder.create();
        OpUtil.showAlertDialog(this, alertDialog);

        OpUtil.setButtonTextColor(this, alertDialog);
        Button button = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        button.setEnabled(false);
        CountDownTimer timer = new CountDownTimer(20000, 1000) {
            final String oK = getString(android.R.string.ok);

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                button.setText(oK + "(" + millisUntilFinished / 1000 + "s" + ")");
            }

            @Override
            public void onFinish() {
                button.setText(oK);
                button.setEnabled(true);
            }
        };
        //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
        timer.start();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void swtichIsMainFragment(boolean toMain) {

        isMain = toMain;

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(!toMain);
        actionBar.setTitle(toMain ? R.string.app_name : R.string.title_dpm_settings);

        Fragment fragmentMainUi = fragmentManager.findFragmentByTag(TAG_MAINUI);
        Fragment fragmentDpm = fragmentManager.findFragmentByTag(TAG_DPM);


        if (toMain) {
            if (fragmentMainUi == null) {
                fragmentManager.beginTransaction().add(R.id.framelayout, new MainFragment(), TAG_MAINUI).commit();
            } else {
                if (fragmentMainUi.isHidden()) {
                    fragmentMainUi.onActivityResult(REQUEST_REFRESH, MainFragment.RESULT_REFRESH_1, null);
                    fragmentManager.beginTransaction().show(fragmentMainUi).commit();
                }
            }
            if (fragmentDpm != null) {
                fragmentManager.beginTransaction().hide(fragmentDpm).commit();
            }
        } else {
            if (fragmentDpm == null) {
                fragmentManager.beginTransaction().add(R.id.framelayout, new DpmSettingsFragment(), TAG_DPM).commit();
            } else {
                if (fragmentDpm.isHidden()) {
                    fragmentDpm.onActivityResult(REQUEST_REFRESH, DpmSettingsFragment.RESULT_REFRESH_2, null);
                    fragmentManager.beginTransaction().show(fragmentDpm).commit();
                }
            }
            if (fragmentMainUi != null) {
                fragmentManager.beginTransaction().hide(fragmentMainUi).commit();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed() {
        if (isMain) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - exitTime) < 2000) {
                super.onBackPressed();
            } else {
                showMyToast0(R.string.tip_exit);
                exitTime = currentTime;
            }
        } else {
            swtichIsMainFragment(true);
        }
    }


    private void startPickFile(ComponentName componentName) {
        Intent intent = new Intent(OpUtil.MODOSA_ACTION_PICK_FILE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        startActivity(intent);

    }

    private void getAvinstaller(ArrayList<ComponentName> list, ComponentName componentName) {
        if (OpUtil.getEnabledComponentState(this, componentName)) {
            list.add(componentName);
        }
    }


    private void pickFileToInstall() {
//        String[] installerClassName = new String[MainFragment.INSTALLER_SIZE];
        ComponentName[] installerComponentNames = OpUtil.getInstallerComponentNames(this);
        ArrayList<ComponentName> componentNameArrayList = new ArrayList<>();
        for (int i = 1; i < MainFragment.INSTALLER_SIZE; i++) {
//            installerClassName[i] = getPackageName() + ".activity" + ".Install" + i + "Activity";
//
//            installerComponentNames[i] = new ComponentName(getPackageName(), installerClassName[i]);

            getAvinstaller(componentNameArrayList, installerComponentNames[i]);
        }


        if (componentNameArrayList.size() != 0) {

            String[] items = new String[componentNameArrayList.size()];

            ArrayList<String> namelist = new ArrayList<>();
            for (ComponentName componentName : componentNameArrayList) {
                if (componentName == installerComponentNames[1]) {
                    namelist.add(getString(R.string.name_install1));
                } else if (componentName == installerComponentNames[2]) {
                    namelist.add(getString(R.string.name_install2));
                } else if (componentName == installerComponentNames[3]) {
                    namelist.add(getString(R.string.name_install3));
                } else if (componentName == installerComponentNames[4]) {
                    namelist.add(getString(R.string.name_install4));
                } else if (componentName == installerComponentNames[5]) {
                    namelist.add(getString(R.string.name_install5));
                } else if (componentName == installerComponentNames[6]) {
                    namelist.add(getString(R.string.name_install6));
                }
            }

            for (int i = 0; i < namelist.size(); i++) {
                items[i] = namelist.get(i);
            }


            if (items.length == 1) {
                showMyToast0(items[0]);
                startPickFile(componentNameArrayList.get(0));
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.PickFileToInstall)
                        .setItems(items, (dialog, which) -> startPickFile(componentNameArrayList.get(which)));

                alertDialog = builder.create();

                OpUtil.showAlertDialog(this, alertDialog);
            }

        } else {
            showMyToast0(R.string.tip_enable_one_installer);
        }
    }
}
