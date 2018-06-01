package com.qdazzle.pushPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.pushtest.R;
import com.example.pushtest.ResUtil;
import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class PushService extends Service {
	private static final String TAG=PushService.class.getName();
	private static volatile Thread mPushServiceThread=null;
	private static volatile boolean mKeepWorking=false;
	private static int mNotificationId=143825;
	private static SortedSet<QdNotification> mNotifications = new TreeSet<QdNotification>((Comparator<QdNotification>)new Comparator<QdNotification>() {

		@Override
		public int compare(QdNotification o1, QdNotification o2) {//使用时间作为唯一标识
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
					Log.i(TAG,"current time"+currentMinute()+mNotifications.toString());
					checkPushExpire();
					try {
						Thread.sleep(1000*60);
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
					mNotifications.add(new QdNotification(tickerText, title, content, triggeringTime));
				}
			}else {
				Log.i(TAG,"receivePushMessage getString:"+responseStr);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private void checkPushExpire()
	{
		for (QdNotification notification : mNotifications) {
			if(notification.getTriggeringTime()<currentMinute()&&notification.getTriggeringTime()+60<currentMinute())
			{
				//过期
				mNotifications.remove(notification);
				break;
			}
			else if(notification.getTriggeringTime()<currentMinute()&&notification.getTriggeringTime()+60>=currentMinute()) {
				popNotificationNow(mNotificationId++, notification.getTitle(), notification.getContent(), notification.getTickerText());
				mNotifications.remove(notification);
				break;
			}
		}
	}
	
	protected void popNotificationNow(int notificationId, String title, String content, String tickerText)
	{
		//Log.v("t3game", "t3game notification : id "+id + "title : "+title+" content : "+ content);
			
		Log.e(TAG,"popNotificationNow");
		Notification messageNotification = new Notification();
		//暂时使用R.xxx索引资源
		messageNotification.icon=ResUtil.getDrawableId(this, "ic_launcher");//R.drawable.ic_launcher;
		messageNotification.tickerText=tickerText;
		messageNotification.defaults=Notification.DEFAULT_SOUND;
		messageNotification.flags|=Notification.FLAG_AUTO_CANCEL;
		messageNotification.flags|=Notification.FLAG_SHOW_LIGHTS;
		NotificationManager messageNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		//尝试不用receive，直接打开activity
  		Intent launch = this.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
   		launch.addCategory(Intent.CATEGORY_LAUNCHER);
   		launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent messagePendingIntent=PendingIntent.getActivity(this, notificationId, launch, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Class notificationClass=messageNotification.getClass();
		try {
			Method setLatestEventInfoMethod=notificationClass.getDeclaredMethod("setLatestEventInfo",
				Context.class,CharSequence.class,CharSequence.class,PendingIntent.class);
			setLatestEventInfoMethod.invoke(messageNotification, this,title,content,messagePendingIntent);
		}catch(Exception e) {
			Log.e(TAG,"setLatestEventInfoMethod.invoke error:"+e.toString());
		}
		Log.e(TAG,"messageNotificationManager.notify(notificationId,messageNotification);");
		messageNotificationManager.notify(notificationId,messageNotification);
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
