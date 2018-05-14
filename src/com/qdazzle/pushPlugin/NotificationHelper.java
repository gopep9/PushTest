package com.qdazzle.pushPlugin;

import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

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

	public static boolean scheduleNotification(int id, int delayMinutes,
			String title, String content, int periodMinutes)
	{
		synchronized (mNotiServiceLock)
		{
			if (mNotificationService == null)
			{
				Log.d(TAG, "Notification service null!");
				return false;
			}

			try
			{
				return mNotificationService.scheduleNotification(id,
						delayMinutes, title, content, periodMinutes);
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
				Log.d(TAG, "Notification service null!");
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
				Log.d(TAG, "Notification service null!");
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
			String url, int port, int platformId, int channelId, int pushPackId)
	{
		synchronized (mNotiServiceLock)
		{
			if (mNotificationService == null)
			{
				Log.d(TAG, "Notification service null!");
				return false;
			}

			try
			{
				return mNotificationService.setPushPollRequestUrlString(
						url, port, platformId, channelId, pushPackId);
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
				Log.d(TAG, "Notification service null!");
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
