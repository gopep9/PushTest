package com.qdazzle.pushPlugin;

public class Notification {
	private String mTickerText="";
	private String mTitle="";
	private String mContent="";
	private long mTriggeringTime=0;
	
	public Notification(String tickerText,String title,String content,long triggeringTime)
	{
		mTickerText=tickerText;
		mTitle=title;
		mContent=content;
		mTriggeringTime=triggeringTime;
	}
	
	void setTickerText(String tickerText)
	{
		mTickerText=tickerText;
	}
	
	String getTickerText()
	{
		return mTickerText;
	}
	
	void setTitle(String title)
	{
		mTitle=title;
	}
	
	String getTitle()
	{
		return mTitle;
	}
	
	void setContent(String content)
	{
		mContent=content;
	}
	
	String getContent()
	{
		return mContent;
	}
	
	void setTriggeringTime(long triggeringTime)
	{
		mTriggeringTime=triggeringTime;
	}
	
	long getTriggeringTime()
	{
		return mTriggeringTime;
	}
	
	@Override
	public String toString()
	{
		return "tickerText:"+mTickerText+"title:"+mTitle+"content:"+mContent+
				"triggeringTime"+mTriggeringTime;
	}
}
