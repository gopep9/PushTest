package com.qdazzle.pushPlugin.aidl;

interface INotificationService{
	boolean scheduleNotification(int id,int delayMinutes,String title,String content,int periodMinutes);
	boolean unscheduleNotification(int id);
	boolean unscheduleAllNotifications();
	boolean setForgroundProcName(String procName);
	void stopNotificationService();
	boolean setPushPollRequestUrlString(String url,int port, int platformId, int channelId, int pushPackId);
}