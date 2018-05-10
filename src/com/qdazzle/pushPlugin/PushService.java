package com.qdazzle.pushPlugin;

import java.lang.reflect.Method;

import com.example.pushtest.MainActivity;
import com.example.pushtest.R;
import com.example.pushtest.ResUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushService extends QdPushService
{
	final static String TAG=PushService.class.getName();
	@SuppressWarnings("deprecation")
	protected void popNotificationNow(int id, String title, String content)
	{
		//Log.v("t3game", "t3game notification : id "+id + "title : "+title+" content : "+ content);
			
		Notification messageNotification = new Notification();
		//暂时使用R.xxx索引资源
		messageNotification.icon=R.drawable.ic_launcher;
		messageNotification.tickerText=content;
		messageNotification.defaults=Notification.DEFAULT_SOUND;
		messageNotification.flags|=Notification.FLAG_AUTO_CANCEL;
		messageNotification.flags|=Notification.FLAG_SHOW_LIGHTS;
//		NotificationManager messageNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Intent broadcastIntent=new Intent(this,Notification.class);
		PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);
		
		Class notificationClass=messageNotification.getClass();
		try {
			Method setLatestEventInfoMethod=notificationClass.getDeclaredMethod("setLatestEventInfo",
					Context.class,CharSequence.class,CharSequence.class,PendingIntent.class);
			setLatestEventInfoMethod.invoke(messageNotification, this,title, content, pendingIntent);
		}catch(Exception e) {
			Log.e(TAG,e.toString());
			return;
		}
		this.getNotificationManager().notify(id,messageNotification);
//		Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		
//
//		n.flags |= Notification.FLAG_SHOW_LIGHTS;
//		n.flags |= Notification.FLAG_AUTO_CANCEL;
//
//		n.defaults = Notification.DEFAULT_ALL;
//
//		n.icon = R.drawable.app_icon;    //2130837504;
//		
//		
//		
//		
//		
//		
//		n.when = System.currentTimeMillis();
//
//
//
//		n.setLatestEventInfo(this, title, content, pendingIntent);
//		
//		this.getNotificationManager().notify(id, n);
	}
}
