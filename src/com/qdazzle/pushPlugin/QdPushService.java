package com.qdazzle.pushPlugin;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.SortedSet;
import java.util.TreeSet;

import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class QdPushService extends Service{

	
	private static QdUserInfo mTempUserInfo = null;
	private static SortedSet<QdNotification> mNotifications = new TreeSet<QdNotification>();
	
	private static DatagramSocket mPushServerSocket=null;

	private static Object mNotificationsLock=new Object();
	private static volatile boolean mNotificationsModify=false;
	private static volatile boolean mKeepWorking = false;
	private static boolean mUserInfoNeedUpdate = false;

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
		public boolean setPackageMessage(int platformId, int channelId, int pushPackId) throws RemoteException {
			// TODO Auto-generated method stub
			if(null==mTempUserInfo)
			{
				mTempUserInfo=new QdUserInfo();
			}
			mTempUserInfo.setPlatformId(platformId);
			mTempUserInfo.setChannelId(channelId);
			mTempUserInfo.setPushPackId(pushPackId);
			return true;
		}
		
		@Override
		public boolean setNetworkMessage(String url, int port) throws RemoteException {
			// TODO Auto-generated method stub
			if(mPushServerSocket==null)
			{
				try {
					mPushServerSocket=new DatagramSocket();
				}catch(Exception e)
				{
					e.printStackTrace();
					return false;
				}
			}
			if(null==mTempUserInfo)
			{
				mTempUserInfo=new QdUserInfo();
			}
			mTempUserInfo.setPushUrl(url);
			mTempUserInfo.setPushPort(port);
			mUserInfoNeedUpdate=true;
			return true;
		}
		
		@Override
		public boolean setForgroundProcName(String procName) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean schedueNotification(int id, int delayMinutes, String title, String content, int periodMinutes)
				throws RemoteException {
			// TODO Auto-generated method stub
			synchronized (mNotificationsLock)
			{
				QdNotification note = new QdNotification();
				note.setId(id);
				note.setTimeToNotify(System.currentTimeMillis() / 1000 / 60
						+ delayMinutes);
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

			return false;
		}
	};
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinderObj.asBinder();
	}

}
