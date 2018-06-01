package com.qdazzle.pushPlugin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServerPushPlugin{
	final String TAG=ServerPushPlugin.class.getName();
	private ServerPushPlugin() {
	}
	private static ServerPushPlugin serverPushPlugin=null;
	public static ServerPushPlugin getInstance() {
		if(serverPushPlugin==null) {
			serverPushPlugin=new ServerPushPlugin();
		}
		return serverPushPlugin;
	}
	private static Context mContext=null;
	
	public void init(Context context,String url,int port,String platformId,String channelId,
			String notificationPackId,String packageId,long requestPeriod/*分钟数*/)
	{
		Log.i(TAG,"ServerPushPlugin init");
		mContext=context;
		Intent startPushServiceIntent=new Intent(context,PushService.class);
		startPushServiceIntent.putExtra("url", url);
		startPushServiceIntent.putExtra("port", port);
		startPushServiceIntent.putExtra("platformId", platformId);
		startPushServiceIntent.putExtra("channelId", channelId);
		startPushServiceIntent.putExtra("notificationPackId", notificationPackId);
		startPushServiceIntent.putExtra("requestPeriod", requestPeriod);
		context.startService(startPushServiceIntent);
	}
}