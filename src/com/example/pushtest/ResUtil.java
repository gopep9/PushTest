package com.example.pushtest;

import android.content.Context;

public class ResUtil {
	public static int getLayoutId(Context context, String params) {
			return context.getResources().getIdentifier(params, "layout",
					context.getPackageName());
		}
	
		public static int getStringId(Context context, String params) {
			return context.getResources().getIdentifier(params, "string",
					context.getPackageName());
		}
	
		public static int getDrawableId(Context context, String params) {
			return context.getResources().getIdentifier(params, "drawable",
					context.getPackageName());
		}
	
		public static int getStyleId(Context context, String params) {
			return context.getResources().getIdentifier(params, "style",
					context.getPackageName());
		}
	
		public static int getId(Context context, String params) {
			return context.getResources().getIdentifier(params, "id",
					context.getPackageName());
		}
		
		public static int getDimenId(Context context, String params) {
			return context.getResources().getIdentifier(params, "dimen",
					context.getPackageName());
		}
	
		public static int getColorId(Context context, String params) {
			return context.getResources().getIdentifier(params, "color",
					context.getPackageName());
		}
	
		public static int getAnimId(Context context, String params) {
			return context.getResources().getIdentifier(params, "anim",
					context.getPackageName());
		}
}
