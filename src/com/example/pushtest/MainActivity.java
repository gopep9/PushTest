package com.example.pushtest;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{

	private Button btnStart;
	private Button btnStop;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResUtil.getLayoutId(this, "activity_main"));
		initView();
	}
	private void initView() {
		btnStart=(Button) findViewById(ResUtil.getId(this, "btnStart"));
		btnStart.setOnClickListener(this);
	}
	
	private Intent messageIntent=null;
	private int messageNotificationID=1000;
	private Notification messageNotification=null;
	private NotificationManager messageNotificationManager=null;
	private PendingIntent messagePendingIntent=null;
	@Override
	public void onClick(View v) {
		int id=v.getId();
		if(id==ResUtil.getId(this, "btnStart"))
		{
			Log.e("2018581134","2018581134");
			messageNotification = new Notification();
			messageNotification.icon=ResUtil.getDrawableId(this, "ic_launcher");
			messageNotification.tickerText="新消息";
			messageNotification.defaults=Notification.DEFAULT_SOUND;
			messageNotification.flags=Notification.FLAG_AUTO_CANCEL;
			messageNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			//点击按钮后要启动的intent
			messageIntent=new Intent(this,MainActivity.class);
			messagePendingIntent=PendingIntent.getActivity(this, 0, messageIntent, 0);
			//通过反射兼容16以下的sdk
			Class notificationClass=messageNotification.getClass();
			try {
			Method setLatestEventInfoMethod=notificationClass.getDeclaredMethod("setLatestEventInfo",
					Context.class,CharSequence.class,CharSequence.class,PendingIntent.class);
			setLatestEventInfoMethod.invoke(messageNotification, this,"新消息","您有新消息1",messagePendingIntent);
			}catch(Exception e) {
				Log.e("MainActivity","2018581451"+e.toString());
			}
			messageNotificationManager.notify(messageNotificationID,
					messageNotification);
		}
	}
}
