package com.jay.cloud_board;

import com.jay.cloud_board.base.Config;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger4j {
	private static Logger mLogger;

	public static void setLogger(Logger logger) {
		mLogger = logger;
	}

	public static void d(String TAG, String content) {
		if (mLogger == null) return;
		mLogger.debug(getCurTime() + TAG + "---" + content);
		println(TAG, content);
	}

	public static void i(String TAG, String content) {
		if (mLogger == null) return;
		mLogger.info(getCurTime() + TAG + "---" + content);
		println(TAG, content);
	}

	public static void w(String TAG, String content) {
		if (mLogger == null) return;
		mLogger.warn(getCurTime() + TAG + "---" + content);
		println(TAG, content);
	}

	public static void e(String TAG, String content) {
		if (mLogger == null) return;
		mLogger.error(getCurTime() + TAG + "---" + content);
		System.out.println(TAG + "error----" + content);
	}

	private static String getCurTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyy_MM_dd HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		return format.format(date);
	}

	private static void println(String TAG, String content) {
		if (Config.DEBUG)
			System.out.println(TAG + "----" + content);
	}
}
