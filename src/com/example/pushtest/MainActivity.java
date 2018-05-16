package com.example.pushtest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.qdazzle.pushPlugin.NotificationHelper;
import com.qdazzle.pushPlugin.PushService;
import com.qdazzle.pushPlugin.QdNotification;
import com.qdazzle.pushPlugin.QdNotificationPlugin;
import com.qdazzle.pushPlugin.aidl.INotificationService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{

	private Button btnStart;
	private Button btnStop;
	private Button btnDelay;
	private Button btnStartService;
	private Button btnStopServer;
	private TextView messageText;
	
	private EditText triggerTimeEdit;
	private EditText NotificationIdEdit;
	private Button btnAddPush;
	private Button btnDelAllPush;
	
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
		btnStartService=(Button) findViewById(ResUtil.getId(this, "btnStartServer"));
		btnStartService.setOnClickListener(this);
		btnStopServer=(Button) findViewById(ResUtil.getId(this, "btnStopServer"));
		btnStopServer.setOnClickListener(this);
		messageText=(TextView)findViewById(ResUtil.getId(this, "messageText"));
		
		triggerTimeEdit=(EditText)findViewById(ResUtil.getId(this, "triggerTimeEdit"));
		NotificationIdEdit=(EditText)findViewById(ResUtil.getId(this, "NotificationIdEdit"));
		btnAddPush=(Button)findViewById(ResUtil.getId(this, "btnAddPush"));
		btnDelAllPush=(Button)findViewById(ResUtil.getId(this, "btnDelAllPush"));
		btnAddPush.setOnClickListener(this);
		btnDelAllPush.setOnClickListener(this);
	}
	
	private Intent messageIntent=null;
	private int messageNotificationID=1000;
	private Notification messageNotification=null;
	private NotificationManager messageNotificationManager=null;
	private PendingIntent messagePendingIntent=null;
	
//	private static INotificationService mNotificationService=null;
//	private static ServiceConnection mPushServiceConnection;
	Intent startPushServiceIntent;
	@Override
	public void onClick(View v) {
		int id=v.getId();
		if(id==ResUtil.getId(this, "btnStart"))
		{
			getPushMessage();
		}
		else if(id==ResUtil.getId(this, "btnDelay"))
		{
			setAlarm("receive message", System.currentTimeMillis()/1000+10);
		}
		//开启服务
		else if(id==ResUtil.getId(this, "btnStartServer"))
		{
//			Log.e(TAG,"btnStartServer");
//			//启动服务
//			startPushServiceIntent=new Intent(this,PushService.class);
//			startPushServiceIntent.putExtra("Name", MainActivity.class.getPackage().getName());
//			startPushServiceIntent.putExtra("url", "http://172.30.50.1/AndroidPush/pushMessage.php");
//			startPushServiceIntent.putExtra("port", 80);
//			startPushServiceIntent.putExtra("platformId", "90155");
//			startPushServiceIntent.putExtra("channelId", "10052");
//			startPushServiceIntent.putExtra("NotificationId", "1");
//			startService(startPushServiceIntent);
			QdNotificationPlugin.startService(this, "http://172.30.50.1/AndroidPush/pushMessage.php", 80, "90155", "10052", "1", 
					"com.example.pushtest");
			
		}
		else if(id==ResUtil.getId(this, "btnStopServer"))
		{
			QdNotificationPlugin.stopService();
//			mPushServiceConnection=new ServiceConnection() {
//				
//				@Override
//				public void onServiceDisconnected(ComponentName name) {
//					// TODO Auto-generated method stub
////					mNotificationService=INotificationService.Stub.asInterface(null);
//					Log.e(TAG,"onServiceDisconnected");
//					NotificationHelper.setNotificationService(name, null);
//				}
//				
//				@Override
//				public void onServiceConnected(ComponentName name, IBinder service) {
//					// TODO Auto-generated method stub
////					mNotificationService.setPushPollRequestUrlString("172.30.50.1", 80, 90155, 10052, 1);
//					Log.e(TAG,"onServiceConnected");
//					NotificationHelper.setNotificationService(name, service);
//					NotificationHelper.setForgroundProcName("com.example.pushtest");
////					NotificationHelper.setPushPollRequestUrlString("http://172.30.50.1/pushMessage.php", 80, "90155", "10052", "1");
//				}
//			};
//			bindService(startPushServiceIntent, mPushServiceConnection, BIND_AUTO_CREATE);
//
//			NotificationHelper.scheduleNotification(2, (int)(System.currentTimeMillis()/1000/60), "test1pushtitle", "test1pushcontent", 0);
		}
		else if(id==ResUtil.getId(this, "btnAddPush"))
		{
			String triggerTimeStr=triggerTimeEdit.getText().toString();
			String NotificationIdStr=NotificationIdEdit.getText().toString();
			QdNotificationPlugin.addScheduleNotification((int)Integer.valueOf(NotificationIdStr), (int)Integer.valueOf(triggerTimeStr), "title", "content", "tickerText", 0);
		}
		else if(id==ResUtil.getId(this, "btnDelAllPush"))
		{
			QdNotificationPlugin.DeleteAllScheduleNotification();
		}
	}
	private int noticeCount=0;//用于区分不同的PendingIntent，在新生成一个PendingIntent以后后加1
	
	//使用广播，通知PushReceiver推送一条通知
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
	
	//和服务器通信，获得推送的消息并且马上进行推送
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
					URL url = new URL("http://172.30.50.1/pushMessage.php");
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
				messageNotificationID++;
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		QdNotificationPlugin.onDestroy();
	}
}
