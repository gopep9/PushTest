package com.qdazzle.pushPlugin.aidl;

interface INotificationService{
	boolean scheduleNotification(int id,int triggerMinutes,String title,String content,String tickerText,int periodMinutes);
	boolean unscheduleNotification(int id);
	boolean unscheduleAllNotifications();
	boolean setForgroundProcName(String procName);
	void stopNotificationService();
}