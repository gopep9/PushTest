package com.example.pushtest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

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
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

	private Button btnStart;
	private Button btnStop;
	private TextView messageText;
	final static String TAG=MainActivity.class.getName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResUtil.getLayoutId(this, "activity_main"));
		initView();
	}
	private void initView() {
		btnStart=(Button) findViewById(ResUtil.getId(this, "btnStart"));
		btnStart.setOnClickListener(this);
		messageText=(TextView)findViewById(ResUtil.getId(this, "messageText"));
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
			messageNotification.tickerText="tickerText";
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
			setLatestEventInfoMethod.invoke(messageNotification, this,"title","new message",messagePendingIntent);
			}catch(Exception e) {
				Log.e("MainActivity","2018581451"+e.toString());
			}
			messageNotificationManager.notify(messageNotificationID,
					messageNotification);
			getMessage();
//			String msg=getMessage();
//			messageText.setText(msg);
		}
	}
	
	private String getMessage()
	{
		String str="a";
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					StringBuilder response=new StringBuilder();
					BufferedReader reader=null;
					URL url = new URL("http://172.25.0.1");
					HttpURLConnection connection=(HttpURLConnection)url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream inputStream=connection.getInputStream();
					reader=new BufferedReader(new InputStreamReader(inputStream));
					String line;
					while((line=reader.readLine())!=null) {
						response.append(line);
					}
					showResponse(response.toString());
				}catch (Exception e) {
					Log.e(TAG,e.toString());
					// TODO: handle exception
				}
			}
		});
		thread.start();
		return "";
	}
	
	private void showResponse(final String response) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				messageText.setText(response);
			}
		});
	}
}
