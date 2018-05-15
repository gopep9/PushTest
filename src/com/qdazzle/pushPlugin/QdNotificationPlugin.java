package com.qdazzle.pushPlugin;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class QdNotificationPlugin {
	private static ServiceConnection mPushServiceConnection;
	static public void startService(Context context,String url,int port,String platformId,String channelId,String NotificationPackId,final String packageId)
	{
		Intent startPushServiceIntent;
		startPushServiceIntent=new Intent(context,PushService.class);
		startPushServiceIntent.putExtra("url", url);
		startPushServiceIntent.putExtra("port", port);
		startPushServiceIntent.putExtra("platformId", platformId);
		startPushServiceIntent.putExtra("channelId", channelId);
		startPushServiceIntent.putExtra("NotificationPackId", NotificationPackId);
		context.startService(startPushServiceIntent);
		mPushServiceConnection=new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				NotificationHelper.setNotificationService(name, null);
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				NotificationHelper.setNotificationService(name, service);
				NotificationHelper.setForgroundProcName(packageId);
			}
		};
		context.bindService(startPushServiceIntent, mPushServiceConnection, Context.BIND_AUTO_CREATE);
	}
	static public void stopService()
	{
		NotificationHelper.stopNotificationService();
	}
}
