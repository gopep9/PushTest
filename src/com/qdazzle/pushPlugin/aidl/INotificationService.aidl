package com.qdazzle.pushPlugin.aidl;

interface INotificationService{
	boolean scheduleNotification(int id,int triggerMinutes,String title,String content,int periodMinutes);
	boolean unscheduleNotification(int id);
	boolean unscheduleAllNotifications();
	boolean setForgroundProcName(String procName);
	void stopNotificationService();
	boolean setPushPollRequestUrlString(String url,int port, String platformId, String channelId, String pushPackId);
}