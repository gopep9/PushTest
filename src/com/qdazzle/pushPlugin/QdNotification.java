package com.qdazzle.pushPlugin;

import java.io.Serializable;
import java.net.ContentHandler;
//存储推送的信息，可以写到本地
public class QdNotification implements Comparable<Object>,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7699169837163100936L;

	//推送的id
	private int mId = -1;
	//推送的时间，以分钟为单位，是绝对时间（时间戳除以60）
	private long mTimeToNotify = 0;
	//推送的标题
	private String mTitle = "";
	//推送的内容
	private String mContent = "";
	//推送延迟的时间（周期）？在推送失败后设置mTimeToNotify加上这个值
	private int mPeriod = 0;

	public int getId()
	{
		return mId;
	}

	public void setId(int mId)
	{
		this.mId = mId;
	}

	public long getTimeToNotify()
	{
		return mTimeToNotify;
	}

	public void setTimeToNotify(long mDelayMinutes)
	{
		this.mTimeToNotify = mDelayMinutes;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public void setTitle(String mTitle)
	{
		this.mTitle = mTitle;
	}

	public String getContent()
	{
		return mContent;
	}

	public void setContent(String mContent)
	{
		this.mContent = mContent;
	}

	public void minusDelayMinutes(int minus)
	{
		this.mTimeToNotify -= minus;
	}

	@Override
	public int compareTo(Object another)
	{
		QdNotification other = (QdNotification) another;
		if (this.mTimeToNotify < other.mTimeToNotify
				|| (this.mTimeToNotify == other.mTimeToNotify && this.mId < other.mId))
		{
			return -1;
		}

		if (this.mId == other.mId)
		{
			return 0;
		}

		return 1;
	}

	@Override
	public boolean equals(Object another)
	{
		return this.mId == ((QdNotification) another).mId;
	}

	@Override
	public int hashCode()
	{
		return this.mId;
	}

	public int getPeriod()
	{
		return mPeriod;
	}

	public void setPeriod(int mPeriod)
	{
		this.mPeriod = mPeriod;
	}
	
	@Override
	public String toString()
	{
		return "mid:"+mId+" mTimeToNotify:"+mTimeToNotify+
				" mTitle:"+mTitle+" mContent:"+mContent+
				" mPeriod:"+mPeriod;
	}

}
