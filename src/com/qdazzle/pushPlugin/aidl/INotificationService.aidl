package com.qdazzle.pushPlugin.aidl;

interface INotificationService{
	boolean schedueNotification(int id,int delayMinutes,String title,String content,int periodMinutes);
	boolean unscheduleNotification(int id);
	boolean unscheduleAllnotifications();
	boolean setForgroundProcName(String procName);
	void stopNotificationService();
	boolean setPushPollRequestUrlString(String url,int port, int platformId, int channelId, int pushPackId);
}