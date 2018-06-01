package com.example.pushtest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.qdazzle.pushPlugin.ServerPushPlugin;
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

	private Button btnStartService;
	private Button btnStopServer;
	
	final static String TAG=MainActivity.class.getName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResUtil.getLayoutId(this, "activity_main"));
		initView();
	}
	private void initView() {
		btnStartService=(Button) findViewById(ResUtil.getId(this, "btnStartServer"));
		btnStartService.setOnClickListener(this);
		btnStopServer=(Button) findViewById(ResUtil.getId(this, "btnStopServer"));
		btnStopServer.setOnClickListener(this);
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
		//开启服务
		if(id==ResUtil.getId(this, "btnStartServer"))
		{
//			QdNotificationPlugin.startService(this, "http://172.30.50.1/AndroidPush/pushMessage.php", 80, "90155", "10052", "1", 
//					"com.example.pushtest",30);
			ServerPushPlugin.getInstance().init(this, "http://172.30.50.1/AndroidPush/pushMessage.php", 80, "90155", "10052", "1", "", 60*12);
		}
		else if(id==ResUtil.getId(this, "btnStopServer"))
		{
//			QdNotificationPlugin.stopService();
			ServerPushPlugin.getInstance().stop();
		}
	}	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		QdNotificationPlugin.onDestroy();
	}
}
