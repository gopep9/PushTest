package com.qdazzle.pushPlugin;

import android.content.Context;
import android.content.Intent;

public class QdNotificationPlugin {
	public void startService(Context context,String url,int port,String platformId,String channelId,String NotificationPackId)
	{
		Intent startPushServiceIntent;
		startPushServiceIntent=new Intent(context,PushService.class);
		startPushServiceIntent.putExtra("url", url);
		startPushServiceIntent.putExtra("port", port);
		startPushServiceIntent.putExtra("platformId", platformId);
		startPushServiceIntent.putExtra("channelId", channelId);
		startPushServiceIntent.putExtra("NotificationPackId", NotificationPackId);
		context.startService(startPushServiceIntent);
	}
	public void stopService()
	{
		
	}
}
