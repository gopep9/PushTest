package com.qdazzle.pushPlugin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class QdPushService extends Service{

	private static QdUserInfo mUserInfo = null;
	private static QdUserInfo mTempUserInfo = null;
	private static SortedSet<QdNotification> mNotifications = new TreeSet<QdNotification>();
	private static String mForgroundProcName="";
	private static final String TAG = QdPushService.class.getName();
	
	private static DatagramSocket mPushServerSocket=null;
	private static volatile Thread mPushServiceThread=null;
	private NotificationManager mNotifManager = null;

	private static Object mNotificationsLock=new Object();
	private static Object mTempUserInfoLock=new Object();
	private static volatile boolean mNotificationsModify=false;
	private static volatile boolean mKeepWorking = false;
	private static volatile boolean mUserInfoNeedUpdate = false;
	private static volatile boolean mIsInited=false;
	
	private static final String NOTIF_PREF_FILE_NAME = "NotifPrefFile";
	private static final String USER_PREF_FILE_NAME = "UserPrefFile";

	private static final int OUT_OF_DATE_VAL = 60;

	private static INotificationService mBinderObj=new INotificationService.Stub() {
		
		@Override
		public boolean unscheduleNotification(int id) throws RemoteException {
			// TODO Auto-generated method stub
			synchronized (mNotificationsLock)
			{
				for (QdNotification note : mNotifications)
				{
					if (note.getId() == id)
					{
						mNotifications.remove(note);
						break;
					}
				}
				mNotificationsModify = true;
			}
			return true;
		}
		
		@Override
		public boolean unscheduleAllnotifications() throws RemoteException {
			// TODO Auto-generated method stub
			synchronized (mNotificationsLock)
			{
				mNotifications.clear();
				mNotificationsModify = true;
			}

			return true;
		}
		
		@Override
		public void stopNotificationService() throws RemoteException {
			// TODO Auto-generated method stub
			mKeepWorking = false; // stop the push service thread
		}
				
		@Override
		public boolean setForgroundProcName(String procName) throws RemoteException {
			// TODO Auto-generated method stub
			mForgroundProcName = procName;
			return true;
		}
		
		//public boolean schedueNotification(int id, int delayMinutes, String title, String content, int periodMinutes)
		@Override
		public boolean schedueNotification(int id, int triggerMinutes, String title, String content, int periodMinutes)
				throws RemoteException {
			// TODO Auto-generated method stub
			synchronized (mNotificationsLock)
			{
				QdNotification note = new QdNotification();
				note.setId(id);
				note.setTimeToNotify(triggerMinutes);
				note.setTitle(title);
				note.setContent(content);
				note.setPeriod(periodMinutes);

				for (QdNotification notetmp : mNotifications)
				{
					if (notetmp.getId() == id)
					{
						mNotifications.remove(notetmp);
						break;
					}
				}
				mNotifications.add(note);
				mNotificationsModify = true;
			}

			return true;
		}

		@Override
		public boolean setPushPollRequestUrlString(String url, int port, int platformId, int channelId, int pushPackId)
				throws RemoteException {
			// TODO Auto-generated method stub
			if(mPushServerSocket==null)
			{
				try {
					mPushServerSocket=new DatagramSocket();
				}catch(SocketException e)
				{
					e.printStackTrace();
					return false;
				}catch (Exception e)
				{
					e.printStackTrace();
					return false;
				}
			}
			synchronized (mTempUserInfoLock) {
				mTempUserInfo = new QdUserInfo();
				mTempUserInfo.setPushUrl(url);
				mTempUserInfo.setPushPort(port);
				mTempUserInfo.setPlatformId(platformId);
				mTempUserInfo.setChannelId(channelId);
				mTempUserInfo.setPushPackId(pushPackId);
				mUserInfoNeedUpdate = true;
			}
			return true;
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinderObj.asBinder();
	}

	
	//在开始后开始一个线程，定时查询，处理请求
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		//停止以后要怎么恢复？
		if (mIsInited == false && mPushServiceThread == null)
		{
			mIsInited = true;
			mKeepWorking = true;

			mNotifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

			mPushServiceThread = new Thread(new Runnable()
			{
				@Override
				public void run() {
					while(mKeepWorking)
					{
						if(updateUserInfo())
						{
							saveUserInfoToPreference();
						}
						
						// if our game is on foreground.
						boolean hasForeGround=checkForground();
						
						/*
						 * check server push 3 times every minute. this will
						 * block the thread for 1 min
						 */
						for (int i = 0; i < 3; i++)
						{
							checkServerPush(60 / 3, hasForeGround);
						}

						/*
						 * check local push time every other minute
						 */
						checkLocalPush(hasForeGround);
						try {
							Thread.sleep(3000);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					saveUserInfoToPreference();
					saveNotifDataToPreference();
				}
			});

			mPushServiceThread.start();

		}

		return START_STICKY;
	}

	
	private static boolean updateUserInfo()
	{
		if (mUserInfoNeedUpdate)
		{
			synchronized (mTempUserInfoLock) {
				mUserInfoNeedUpdate = false;
				mUserInfo = mTempUserInfo;
				mTempUserInfo = null;
				return true;
			}
		}
		return false;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	protected void popNotificationNow(int id,String title,String content)
	{
	}
	
	protected NotificationManager getNotificationManager()
	{
		return mNotifManager;
	}

	@Override
	public void onDestroy()
	{
		mKeepWorking = false;
		if (mPushServerSocket != null)
		{
			mPushServerSocket.close();
		}
		super.onDestroy();
	}

	@SuppressWarnings("unchecked")
	public void loadNotifDataFromPreference()
	{
		ObjectInputStream objectIn = null;
		Object object = null;
		try
		{
			FileInputStream fileIn = getApplicationContext().openFileInput(
					NOTIF_PREF_FILE_NAME);
			objectIn = new ObjectInputStream(fileIn);
			object = objectIn.readObject();

			synchronized (mNotificationsLock)
			{
				if (object != null)
				{
					mNotifications = (SortedSet<QdNotification>) object;
				}
				else
				{
					Log.d(TAG, "Load notif-cached-data-list failed.");
				}
			}
		}
		catch (FileNotFoundException e)
		{
			Log.d(TAG, "Notif-cached-data not exist yet.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (objectIn != null)
			{
				try
				{
					objectIn.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	public void saveNotifDataToPreference()
	{
		ObjectOutputStream objectOut = null;
		try
		{
			FileOutputStream fileOut = getApplicationContext().openFileOutput(
					NOTIF_PREF_FILE_NAME, MODE_PRIVATE);
			objectOut = new ObjectOutputStream(fileOut);
			synchronized (mNotificationsLock)
			{
				objectOut.writeObject(mNotifications);
				mNotificationsModify = false;
			}
			fileOut.getFD().sync();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (objectOut != null)
			{
				try
				{
					objectOut.close();
				}
				catch (IOException e)
				{

				}
			}
		}
	}

	public void loadUserInfoFromPreference()
	{
		ObjectInputStream objectIn = null;
		Object object = null;
		try
		{
			FileInputStream fileIn = getApplicationContext().openFileInput(
					USER_PREF_FILE_NAME);
			objectIn = new ObjectInputStream(fileIn);
			object = objectIn.readObject();

			if (object != null)
			{
				mUserInfo = (QdUserInfo) object;
				mUserInfoNeedUpdate = true;
			}
			else
			{
				Log.d(TAG, "load userinfo failed.");
			}
		}
		catch (FileNotFoundException e)
		{
			Log.d(TAG, "userinfo not exist yet.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (objectIn != null)
			{
				try
				{
					objectIn.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	public void saveUserInfoToPreference()
	{
		ObjectOutputStream objectOut = null;
		try
		{
			FileOutputStream fileOut = getApplicationContext().openFileOutput(
					USER_PREF_FILE_NAME, MODE_PRIVATE);
			objectOut = new ObjectOutputStream(fileOut);
			if (mUserInfo != null)
			{
				objectOut.writeObject(mUserInfo);
			}
			fileOut.getFD().sync();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (objectOut != null)
			{
				try
				{
					objectOut.close();
				}
				catch (IOException e)
				{

				}
			}
		}
	}
	
	private boolean checkForground()
	{
		/*
		 * check if process is on foreground
		 */
		boolean hasForeground = false;
		ActivityManager activitymanager = (ActivityManager) QdPushService.this
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activitymanager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo info : procInfos)
		{
			if (info.processName.equals(mForgroundProcName))
			{
				hasForeground = true;
			}
		}
		return hasForeground;
	}
	
	private void checkLocalPush(boolean hasForeground)
	{
		/*
		 * check local push
		 */
		try
		{
			boolean changed = false;

			long currentMinute = System.currentTimeMillis() / 1000 / 60;
			// pick all notif who is time-out.
			ArrayList<QdNotification> toPopList = new ArrayList<QdNotification>();
			synchronized (mNotificationsLock)
			{
				changed = mNotificationsModify;
				if (mNotifications.isEmpty())
					return;

				for (QdNotification note : mNotifications)
				{
					if (note.getTimeToNotify() <= currentMinute)
					{
						toPopList.add(note);
					}
				}
			}
			// pop notifications to system
			for (QdNotification note : toPopList)
			{
				// pop notification only when no forground
				// and pending notif is not out of date.
				//到期的符合条件的进行推送，否则直接忽略
				if (!hasForeground
						&& currentMinute - note.getTimeToNotify() < OUT_OF_DATE_VAL)
				{
					popNotificationNow(note.getId(),
							note.getTitle(), note.getContent());
				}
				changed = true;
			}

			// change notif list, remove old notfi and update
			// period notif
			synchronized (mNotificationsLock)
			{
				for (QdNotification note : toPopList)
				{
					//在mNotifications里面直接移除到期的，对于周期推送的重新设置周期添加入mNotifications
					mNotifications.remove(note);
					if (note.getPeriod() > 0)
					{
						note.setTimeToNotify(note.getTimeToNotify()+ note.getPeriod());
						mNotifications.add(note);
					}
				}
			}

			if (changed)
			{
				saveNotifDataToPreference();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void checkServerPush(int secsToWait,boolean hasForground)
	{
		String response="";
	
			response=getServerPushMessage(secsToWait, hasForground, mTempUserInfo.getPushUrl(), mTempUserInfo.getPushPort());
		if(null==response||response=="")
		{
			return;
		}
		JSONObject jObject;
		int code=0;
		String tickerText="";
		String title="";
		String content="";
		String triggeringTime="";
		try {
			jObject=new JSONObject(response);
			code=jObject.getInt("code");
			if(0==code)
			{
				tickerText=jObject.getString("tickerText");
				title=jObject.getString("title");
				content=jObject.getString("content");
				triggeringTime=jObject.getString("triggeringTime");
			}else {
				Log.i(TAG,"receivePushMessage getString:"+response);
			}
		}catch(Exception e) {
			Log.e(TAG, "receivePushMessage exception:"+e.toString());
			return;
		}
		if(0==code||
				null!=tickerText||""!=tickerText||
				null!=title||""!=title||
				null!=content||""!=content||
				null!=triggeringTime||""!=triggeringTime)
		{
			
		}
	}
	
	private String getServerPushMessage(int secsToWait,boolean hasForground,String urlStr,int port)
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
}
