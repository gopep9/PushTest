package com.qdazzle.pushPlugin;

import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
//用于在外部提供接口，可能没有机会用到（直接用服务向服务器请求推送计划，不需要外部的干预）
public class NotificationHelper {
	private static final String TAG = NotificationHelper.class.getName();

	private static Object mNotiServiceLock = new Object();

	private static INotificationService mNotificationService = null;

	public static void setNotificationService(ComponentName name,
			IBinder service)
	{
		synchronized (mNotiServiceLock)
		{
			if (service == null)
			{
				mNotificationService = null;
			}
			else
			{
				mNotificationService = INotificationService.Stub
						.asInterface(service);
			}
		}
	}

	public static INotificationService getNotificationService()
	{
		return mNotificationService;
	}

	public static boolean scheduleNotification(int id, int TimeToNotify,
			String title, String content, String tickerText, int periodMinutes)
	{
		synchronized (mNotiServiceLock)
		{
			if (mNotificationService == null)
			{
				Log.i(TAG, "Notification service null!");
				return false;
			}

			try
			{
				return mNotificationService.scheduleNotification(id,
						TimeToNotify, title, content, tickerText, periodMinutes);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean unscheduleNotification(int id)
	{
		synchronized (mNotiServiceLock)
		{
			if (mNotificationService == null)
			{
				Log.i(TAG, "Notification service null!");
				return false;
			}

			try
			{
				return mNotificationService.unscheduleNotification(id);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean unscheduleAllNotifications()
	{
		synchronized (mNotiServiceLock)
		{
			if (mNotificationService == null)
			{
				Log.i(TAG, "Notification service null!");
				return false;
			}

			try
			{
				return mNotificationService.unscheduleAllNotifications();
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean setPushPollRequestUrlString(
			String url, int port, String platformId, String channelId, String NotificationId)
	{
		synchronized (mNotiServiceLock)
		{
			if (mNotificationService == null)
			{
				Log.i(TAG, "Notification service null!");
				return false;
			}

			try
			{
				return mNotificationService.setPushPollRequestUrlString(
						url, port, platformId, channelId, NotificationId);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean setForgroundProcName(String procName)
	{
		synchronized (mNotiServiceLock)
		{
			if (mNotificationService == null)
			{
				Log.i(TAG, "Notification service null!");
				return false;
			}

			try
			{
				return mNotificationService.setForgroundProcName(procName);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean stopNotificationService() {
		synchronized (mNotiServiceLock) {
			if(mNotificationService == null)
			{
				Log.i(TAG,"Notification service null!");
				return false;
			}
			
			try 
			{
				mNotificationService.stopNotificationService();
				return true;
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return false;
	}


	public static boolean readyForNotificationService()
	{
		synchronized (mNotiServiceLock)
		{
			return mNotificationService != null;
		}
	}

	public static void unbindService()
	{
		synchronized (mNotiServiceLock)
		{
			mNotificationService = null;
		}
	}
}
