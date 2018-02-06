package com.example.wscdemo.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by Administrator on 2018/1/29.
 */

public class PermissionUtils {
    public static void grantSettingsWritePermission(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.System.canWrite(context)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivityForResult(intent, REQUEST_CODE);
                context.startActivity(intent);
            }
        }

    }
}
