package com.qdazzle.pushPlugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
            Log.i("t3game NotificationReceiver", "t3game Try to restart :" + context.getPackageName());
      		Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
       		launch.addCategory(Intent.CATEGORY_LAUNCHER);
       		launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		context.startActivity(launch);    
    }
}