package com.qdazzle.pushPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class PushService extends Service {
	private static final String TAG=PushService.class.getName();
	private static volatile Thread mPushServiceThread=null;
	private static volatile boolean mKeepWorking=false;
	private static SortedSet<Notification> mNotifications = new TreeSet<Notification>((Comparator<Notification>)new Comparator<Notification>() {

		@Override
		public int compare(Notification o1, Notification o2) {
			// TODO Auto-generated method stub
			return (int)(o1.getTriggeringTime()-o2.getTriggeringTime());
		}
	});
	private static long nextRequestTime=0;
	private static INotificationService mBinderObj=new INotificationService.Stub() {
		@Override
		public void stopNotificationService() throws RemoteException {
			// TODO Auto-generated method stub
			mKeepWorking=false;
			mPushServiceThread=null;
		}
	};
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinderObj.asBinder();
	}

	@Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		
		final String url=intent.getStringExtra("url");
		final int port=intent.getIntExtra("port", 80);
		final String platformId=intent.getStringExtra("platformId");
		final String channelId=intent.getStringExtra("channelId");
		final String notificationPackId=intent.getStringExtra("notificationPackId");
		final long requestPeriod=intent.getLongExtra("requestPeriod", 60*12);
		
		mKeepWorking=true;
		if(mPushServiceThread!=null)
			return -1;
		mPushServiceThread=new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.i(TAG,"onStartCommand start thread");
				while(mKeepWorking)
				{
					if(nextRequestTime<currentMinute())
					{
						nextRequestTime=currentMinute()+requestPeriod;
						checkServerPush(10,url,port,platformId,channelId,notificationPackId);
					}
					Log.e(TAG,mNotifications.toString());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		mPushServiceThread.start();
		return 0;
	}
	
	private void checkServerPush(int secsToWait,String url,int port,String platformId,String channelId,String notificationPackId)
	{
		String requestStr=url+"?platformId="+platformId+"&channelId="+channelId;
		String responseStr=getServerPushMessage(secsToWait, requestStr, port);
		if(responseStr==null||responseStr=="")
		{
			return;
		}

		JSONObject jObject;
		int code=0;
		String tickerText="";
		String title="";
		String content="";
		long triggeringTime=0;
		try {
			jObject=new JSONObject(responseStr);
			code=jObject.getInt("code");
			if(0==code)
			{
				JSONArray jsonArrays=jObject.getJSONArray("pushMessageArray");
				for(int i=0;i<jsonArrays.length();i++)
				{	
					JSONObject jsonArray=jsonArrays.getJSONObject(i);
					tickerText=jsonArray.getString("tickerText");
					title=jsonArray.getString("title");
					content=jsonArray.getString("content");
					triggeringTime=jsonArray.getLong("triggeringTime");
					mNotifications.add(new Notification(tickerText, title, content, triggeringTime));
//					Log.e(TAG,"notification set"+AlarmPushPlugin.currentNotification);
//					if(!AlarmPushPlugin.getInstance().checkNotificationIsSet(triggeringTime)&&triggeringTime>currentMinute()) {
//						AlarmPushPlugin.getInstance().addNotification(triggeringTime);
//						addAlarmToNotification(NotificationId, triggeringTime, title, content, tickerText,context);
//						NotificationId++;
//					}
				}
			}else {
				Log.i(TAG,"receivePushMessage getString:"+responseStr);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	private String getServerPushMessage(int secsToWait,String urlStr,int port)
	{
		try {
			int timeLeftMiliSec=secsToWait*1000;
			StringBuilder response=new StringBuilder();
			BufferedReader reader=null;
			URL url=new URL(urlStr);
			HttpURLConnection connection=(HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(timeLeftMiliSec);
			connection.setReadTimeout(timeLeftMiliSec);
			InputStream inputStream=connection.getInputStream();
			reader=new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while((line=reader.readLine())!=null) {
				response.append(line);
			}
			Log.i(TAG,"request:"+urlStr+" port:"+port+" and receive string:"+response.toString());
			return response.toString();
		}catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG,"connect error:"+e.toString());
		}
		return "";
	}
	
	private long currentMinute()
	{
		return System.currentTimeMillis()/1000/60;
	}
}
