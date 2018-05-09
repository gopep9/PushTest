package com.example.pushtest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
	private Button btnDelay;
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
		btnDelay=(Button) findViewById(ResUtil.getId(this, "btnDelay"));
		btnDelay.setOnClickListener(this);
		messageText=(TextView)findViewById(ResUtil.getId(this, "messageText"));
		
		setAlarm("receive message", System.currentTimeMillis()/1000+10);
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
			getPushMessage();
		}
		else if(id==ResUtil.getId(this, "btnDelay"))
		{
			
		}
	}
	private int noticeCount=0;//用于区分不同的PendingIntent，在新生成一个PendingIntent以后后加1
	private void setAlarm(String noticeStr,long timestamp)
	{
		long longTime=timestamp*1000;
		if(longTime>System.currentTimeMillis())
		{
			Intent intent=new Intent(MainActivity.this,PushReceiver.class);
			intent.putExtra("noticeId", noticeCount);
			intent.putExtra("noticeStr", noticeStr);
			PendingIntent pi=PendingIntent.getBroadcast(MainActivity.this, noticeCount, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager am=(AlarmManager)getSystemService(Activity.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, longTime, pi);
			//下面的代码是用SharedPreferences存储这次的推送信息
			SharedPreferences sharedPreferences=getSharedPreferences("Qdazzle_push",Context.MODE_PRIVATE );
			Editor editor=sharedPreferences.edit();
			editor.putLong("timestamp_"+noticeCount, longTime);
			editor.putString("noticeStr_"+noticeCount, noticeStr);
			editor.putInt("noticeCount", noticeCount);
			editor.commit();
			noticeCount++;
		}
	}
	
	private String getPushMessage()
	{
		String str="a";
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					StringBuilder response=new StringBuilder();
					BufferedReader reader=null;
					URL url = new URL("http://172.25.0.1/pushMessage.php");
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
					receivePushMessage(response.toString());
				}catch (Exception e) {
					Log.e(TAG,"connect error:"+e.toString());
					// TODO: handle exception
				}
			}
		});
		thread.start();
		return "";
	}
	
	private void receivePushMessage(final String response) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				messageText.setText(response);
				JSONObject jObject;
				int code=0;
				String tickerText="";
				String title="";
				String body="";
				try {
					jObject = new JSONObject(response);
					code = jObject.getInt("code");
					if(0==code)
					{
						tickerText=jObject.getString("tickerText");
						title=jObject.getString("title");
						body=jObject.getString("body");
					}else {
						Log.i(TAG,"receivePushMessage getString:"+response);
					}
				}catch (Exception e) {
					// TODO: handle exception
					Log.e(TAG,"receivePushMessage exception:"+e.toString());
					return;
				}
				Log.i(TAG,"receivePushMessage code:"+code+"tickerText:"+tickerText+"body:"+body);
				messageNotification = new Notification();
				messageNotification.icon=ResUtil.getDrawableId(MainActivity.this, "ic_launcher");
				messageNotification.tickerText=tickerText;
				messageNotification.defaults=Notification.DEFAULT_SOUND;
				messageNotification.flags=Notification.FLAG_AUTO_CANCEL;
				messageNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				//点击按钮后要启动的intent
				messageIntent=new Intent(MainActivity.this,MainActivity.class);
				messagePendingIntent=PendingIntent.getActivity(MainActivity.this, 0, messageIntent, 0);
				//通过反射兼容16以下的sdk
				Class notificationClass=messageNotification.getClass();
				try {
					Method setLatestEventInfoMethod=notificationClass.getDeclaredMethod("setLatestEventInfo",
						Context.class,CharSequence.class,CharSequence.class,PendingIntent.class);
					setLatestEventInfoMethod.invoke(messageNotification, MainActivity.this,title,body,messagePendingIntent);
				}catch(Exception e) {
					Log.e("MainActivity","setLatestEventInfoMethod.invoke error:"+e.toString());
				}
				messageNotificationManager.notify(messageNotificationID,
						messageNotification);
			}
		});
	}
}
