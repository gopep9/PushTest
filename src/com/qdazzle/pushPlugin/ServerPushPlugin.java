package com.qdazzle.pushPlugin;

import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
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
	private static ServiceConnection mPushServiceConnection;
	private static INotificationService mNotificationService = null;
	
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
		mPushServiceConnection=new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mNotificationService=null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				mNotificationService=INotificationService.Stub.asInterface(service);
			}
		};
		context.bindService(startPushServiceIntent, mPushServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void stop() {
		if(mNotificationService!=null)
		{
			try {
				mNotificationService.stopNotificationService();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}