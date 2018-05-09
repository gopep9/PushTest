package com.example.pushtest;

import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PushReceiver extends BroadcastReceiver {

	final static String TAG=MainActivity.class.getName();
	private Notification messageNotification;
	private NotificationManager messageNotificationManager;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		int noticeId=intent.getIntExtra("noticeId", 0);
		String noticeStr=intent.getStringExtra("noticeStr");
		messageNotification = new Notification();
		messageNotification.icon=ResUtil.getDrawableId(context, "ic_launcher");
		messageNotification.tickerText="tickerText";
		messageNotification.defaults=Notification.DEFAULT_SOUND;
		messageNotification.flags=Notification.FLAG_AUTO_CANCEL;
		messageNotificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		//点击按钮后要启动的intent
		Intent messageIntent=new Intent(context,MainActivity.class);
		PendingIntent messagePendingIntent=PendingIntent.getActivity(context, 0, messageIntent, 0);
		//通过反射兼容16以下的sdk
		Class notificationClass=messageNotification.getClass();
		try {
			Method setLatestEventInfoMethod=notificationClass.getDeclaredMethod("setLatestEventInfo",
				Context.class,CharSequence.class,CharSequence.class,PendingIntent.class);
			setLatestEventInfoMethod.invoke(messageNotification, context,"title",noticeStr,messagePendingIntent);
		}catch(Exception e) {
			Log.e(TAG,"setLatestEventInfoMethod.invoke error:"+e.toString());
		}
		messageNotificationManager.notify(noticeId,
				messageNotification);

	}
}
