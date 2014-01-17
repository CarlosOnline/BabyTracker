package com.joyofplaying.babytracker;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Utility {

	public enum Formats {
		Full, DateTime, Date, DateShort, Time, TimeAmPm, MilitaryTime,
	}

	private static class DateConversion {
		private final static String reSpace = "[ ]+";
		private final static String reSlash = "[/]";
		private final static String reDash = "[-]";
		private final static String reMonth = "(0?[1-9]|1[012])";
		private final static String reDay = "(0?[1-9]|[12][0-9]|3[01])";
		private final static String reMonthDay = reMonth + reSlash + reDay;
		private final static String reYear = "(19|20)?\\d\\d";
		private final static String reTime1 = "0?(10|11|12|[1-9]):[0-5][0-9][ ]+(am|pm|AM|PM)";
		private final static String reTime2 = "([0-2])?[0-9]:[0-5][0-9]";
		private final static String reTime3 = "([0-2])?[0-9]:[0-5][0-9]:[0-5][0-9]";

		private final static String[][] rePatterns = {
				{ reYear + reDash + reMonth + reDash + reDay + reSpace + reTime3, "yyyy-MM-dd HH:mm:ss" },
				{ reYear + reDash + reMonth + reDash + reDay + reSpace + reTime1, "yyyy-MM-dd hh:mm a" },
				{ reYear + reDash + reMonth + reDash + reDay + reSpace + reTime2, "yyyy-MM-dd HH:mm" },
				{ reYear + reDash + reMonth + reDash + reDay, "yyyy-MM-dd" },
				{ reMonthDay + reSlash + reYear, "M/d/y" }, { reMonthDay, "M/d" },
				{ reMonthDay + reSpace + reTime1, "M/d h:m a" }, { reMonthDay + reSpace + reTime2, "M/d H:m" },
				{ reMonthDay + reSlash + reYear + reSpace + reTime1, "M/d/y h:m a" },
				{ reMonthDay + reSlash + reYear + reSpace + reTime2, "M/d/y H:m" }, { reTime1, "h:m a" },
				{ reTime2, "H:m" }, };

		private static String getDatePattern(String input) {

			for (String[] pattern : rePatterns) {
				if (Pattern.matches(pattern[0], input)) {
					return pattern[1];
				}
			}

			LogError("Invalid date/time {0}", input);
			return "M/d/y";
		}

		private static String getDisplayFormat(Formats format) {
			switch (format) {
			default:
			case Full:
				return "yyyy-MM-dd HH:mm:ss";

			case DateTime:
				return "yyyy-MM-dd HH:mm";

			case Date:
				return "yyyy-MM-dd";

			case DateShort:
				return "M/d";

			case Time:
				return "h:mm";

			case TimeAmPm:
				return "h:mm a";

			case MilitaryTime:
				return "HH:mm";
			}
		}

	} // DateConversion

	private static Object AppUtility = null;
	public static String AppPackageName = "com.joyofplaying.babytracker";
	public static String AppSettingsName = "BabyTracker";
	public static Activity Activity = null;

	private Utility(Activity activity) {
		Activity = activity;
		if (AppUtility == null) {
			AppUtility = this;
		}
	}

	public static Utility NewUtility(Activity activity) {
		if (AppUtility == null) {
			AppUtility = new Utility(activity);
		}
		return (Utility) AppUtility;
	}

	public static boolean IsNullOrEmpty(String Str) {
		if (Str == null || Str.isEmpty())
			return true;
		else
			return false;
	}

	public static boolean IsValid(String Str) {
		return !IsNullOrEmpty(Str);
	}

	public static int countOf(String str, String lookFor) {
		int count = 0;
		int idx = str.indexOf(lookFor);
		while (idx != -1) {
			count++;
			idx = str.indexOf(lookFor, idx + 1);
		}
		return count;
	}

	public static void showToast(String message, Object... args) {
		String str = MessageFormat.format(message, args);
		Toast toast = Toast.makeText(getAppContext(), str, Toast.LENGTH_LONG);
		toast.show();
	}

	@SuppressWarnings("unused")
	public static String getClassName(String name) {
		if (Constants.ReleaseMode)
			return "";

		String packageName = AppPackageName + ".";
		if (name.startsWith(packageName)) {
			name = name.substring(packageName.length());
		}
		return name;
	}

	@SuppressWarnings("unused")
	public static String getMethodName(final int depth) {
		if (Constants.ReleaseMode)
			return "";

		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		final StackTraceElement cur = ste[1 + depth];
		String className = getClassName(cur.getClassName());
		return className + "." + cur.getMethodName();
	}

	public static void Log(String message, Object... args) {
		if (Constants.DebugMode) {
			String str = MessageFormat.format(message, args);
			Log.i(AppSettingsName, str);
		}
	}

	public static void Log(ContentValues values) {
		if (Constants.DebugMode) {
			String output = Utility.toString(values);
			Utility.Log("{0}", output);
		}
	}

	public static void LogMethod() {
		if (Constants.DebugMode) {
			String method = getMethodName(3);
			Log(">{0}", method);
		}
	}

	public static void LogMethod(String message, Object... args) {
		if (Constants.DebugMode) {
			String str = MessageFormat.format(message, args);
			String method = getMethodName(3);
			Log(">{0} {1}", method, str);
		}
	}

	public static void Error(String Message) {
		showToast(Message);

		if (Constants.DebugMode) {
			Log.e(AppSettingsName, Message);
		}
	}

	public static void Error(String message, Object... args) {
		String str = MessageFormat.format(message, args);
		showToast(str);
		if (Constants.DebugMode) {
			Log.e(AppSettingsName, str);
		}
	}

	public static void LogError(String message, Object... args) {
		if (Constants.DebugMode) {
			String str = MessageFormat.format(message, args);
			Log.e(AppSettingsName, str);
		}
	}

	public static void LogWarning(String message, Object... args) {
		if (Constants.DebugMode) {
			String str = MessageFormat.format(message, args);
			Log.w(AppSettingsName, str);
		}
	}

	public static void LogExceptionCallStack(Exception e) {
		if (Constants.DebugMode) {
			for (StackTraceElement trace : e.getStackTrace()) {
				Utility.LogError("     " + trace.toString());
			}
		}
	}

	@SuppressWarnings("unused")
	public static void LogCursorRows(Cursor cursor) {
		if (Constants.ReleaseMode)
			return;

		Utility.LogMethod();

		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Utility.LogCursor(cursor);
				cursor.moveToNext();
			}
		}
	}

	@SuppressWarnings("unused")
	public static void LogCursorRowsFlat(Cursor cursor) {
		if (Constants.ReleaseMode)
			return;

		Utility.LogMethod();

		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Utility.LogCursorFlat(cursor);
				cursor.moveToNext();
			}
		}
	}

	@SuppressWarnings("unused")
	public static void LogCursor(Cursor cursor) {
		if (Constants.ReleaseMode)
			return;

		if (cursor == null)
			return;
		if (cursor.getCount() == 0) {
			Utility.Log("Cursor is empty");
			return;
		}
		for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
			try {
				Utility.Log("[ {0} ] {1} == {2}", idx, cursor.getColumnName(idx), cursor.getString(idx));
			} catch (Exception ex) {
				Utility.Log("[ {0} ] failed {1}", idx, ex.toString());
			}
		}
	}

	@SuppressWarnings("unused")
	public static void LogCursorFlat(Cursor cursor) {
		if (Constants.ReleaseMode)
			return;

		if (cursor == null)
			return;
		if (cursor.getCount() == 0) {
			Utility.Log("Cursor is empty");
			return;
		}
		StringBuilder sb = new StringBuilder();

		for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
			try {
				sb.append(cursor.getString(idx) + ",");
			} catch (Exception ex) {
				sb.append("error " + idx + ",");
			}
		}
		Utility.Log(sb.toString());
	}

	public static int RandomInt(int MaxValue) {
		Random rand = new Random();
		return rand.nextInt(MaxValue);
	}

	public static String AmountToString(double value) {
		int i = (int) value;
		double d = i;
		if (value == d)
			return Integer.toString(i); // no decimal

		return Double.toString(value);
	}

	public static boolean IsValidDate(String date) {
		if (date.endsWith("/"))
			return false;

		SimpleDateFormat format = new SimpleDateFormat("M/D");
		format.setLenient(false);
		try {
			Date parsed = format.parse(date);
			return parsed != null ? true : false;
		} catch (ParseException ex) {

		}

		return false;
	}

	@SuppressLint("SimpleDateFormat")
	public static boolean IsValidSqlDate(String date) {
		if (date.endsWith("/"))
			return false;

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		format.setLenient(false);
		try {
			Date parsed = format.parse(date);
			return parsed != null ? true : false;
		} catch (ParseException ex) {

		}

		return false;
	}

	public static boolean IsValidTime(String time) {
		if (time.endsWith(":"))
			return false;

		SimpleDateFormat format = new SimpleDateFormat("h:m");
		format.setLenient(false);
		try {
			Date parsed = format.parse(time);
			return parsed != null ? true : false;
		} catch (ParseException ex) {

		}

		return false;
	}

	public static String toCsvRow(Cursor cursor) {
		if (cursor == null || cursor.getCount() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();

		String value;
		int count = cursor.getColumnCount();
		for (int idx = 0; idx < count; idx++) {
			try {
				value = cursor.getString(idx);
			} catch (Exception ex) {
				value = "";
			}
			// UNDONE: Format value?
			sb.append(value + (idx < count - 1 ? "," : ""));
		}
		return sb.toString();
	}

	public static String toString(ContentValues values) {
		StringBuilder sb = new StringBuilder();
		sb.append("ContentValues {\n");
		for (String key : values.keySet()) {
			Object obj = values.get(key);
			String value = obj != null ? obj.toString() : "(null)";
			sb.append(String.format("   %s == %s\n", key, value));
		}
		sb.append("}");
		return sb.toString();
	}

	public static String toUIDate() {
		String date = Utility.toSqlDate();
		return toUIDate(date);
	}

	public static String toUIDate(String date) {
		Calendar cal = Utility.toCalendar(date);
		return toUIDate(cal);
	}

	public static String toUIDate(Calendar cal) {
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		return String.format("%d/%d/%d", month, day, year);
	}

	public static String toUIDateShort() {
		return toUIDateShort(Calendar.getInstance());
	}

	public static String toUIDateShort(Calendar cal) {
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return String.format("%d/%d", month, day);
	}

	public static Date toDate(String input) {
		if (input == "") {
			input = toSqlDate();
		}

		DateFormat formatter;
		Date date;
		String pattern = "";
		try {
			pattern = DateConversion.getDatePattern(input);
			formatter = new SimpleDateFormat(pattern);
			formatter.setLenient(true);
			date = formatter.parse(input);
		} catch (Exception ex) {
			LogError("Invalid date [{0}] ex={2} [{1}]", input, pattern, ex.toString());
			date = new Date();
		}

		return date;
	}

	public static String toString(Date date) {
		return toString(date, Formats.DateTime);
	}

	public static String toString(Date date, Formats format) {
		DateFormat formatter;
		String pattern = DateConversion.getDisplayFormat(format);
		try {
			formatter = new SimpleDateFormat(pattern);
			formatter.setLenient(false);
			String fmt = formatter.format(date);
			return fmt;
		} catch (Exception ex) {
			LogError("Invalid date ex={0} [{1}] [{2}]", ex.toString(), date.toString(), pattern);
			return date.toString();
		}
	}

	public static String normalizeDate(String input) {
		Date date = toDate(input);
		return toString(date);
	}

	public static Calendar toCalendar(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return cal;
	}

	public static String toSqlDate(Calendar cal) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(cal.getTimeInMillis());
		return sdf.format(date);
	}

	public static String toSqlDate(int year, int monthOfYear, int dayOfMonth) {
		return String.format("%s-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
	}

	public static String toSqlDate() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		return String.format("%s-%02d-%02d", today.year, today.month + 1, today.monthDay);
	}

	public static String toSqlDate(int delta) {
		Calendar cal = Utility.toCalendar(Utility.toSqlDate());
		cal.add(Calendar.DATE, delta);
		return Utility.toSqlDate(cal);
	}

	public static String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = Utility.Activity.managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			// HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			// THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	public static File makeExternalFile(String fileName, String ext) {
		File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "BabyTracker");
		folder.mkdirs();
		return new File(folder.getAbsolutePath(), fileName + ext);
	}

	@SuppressLint("SimpleDateFormat")
	public static File getUniqueFile(String fileName, String ext) {
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss.SSS");
		return makeExternalFile(fileName, "." + sdf.format(new Date()) + ext);
	}

	public static Uri getUniqueFileUri(String fileName, String ext) {
		File file = getUniqueFile(fileName, ext);
		return Uri.fromFile(file);
	}

	public static void runDelayed(final Runnable runnable, int delay) {
		new Handler().postDelayed(runnable, delay);
	}

	public static Context getAppContext() {
		return BabyTrackerActivity.getActivity().getAppContext();
	}

	@SuppressWarnings("unused")
	private class SampleRunnableWithParam implements Runnable {
		Object data;

		SampleRunnableWithParam(Object data) {
			this.data = data;
		}

		@Override
		public void run() {
			Utility.Log(data.toString());
		}
	}

	public static class UX {

		public static Bitmap scaleBitmap(Bitmap bm, float scalingFactor) {
			int scaleHeight = (int) (bm.getHeight() * scalingFactor);
			int scaleWidth = (int) (bm.getWidth() * scalingFactor);

			return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
		}

		public static int lightenColor(int color, float factor) {
			float r = Color.red(color) * factor;
			float g = Color.green(color) * factor;
			float b = Color.blue(color) * factor;
			int ir = Math.min(255, (int) r);
			int ig = Math.min(255, (int) g);
			int ib = Math.min(255, (int) b);
			int ia = Color.alpha(color);
			return (Color.argb(ia, ir, ig, ib));
		}

		public static float getBitmapScalingFactor(View view, Bitmap bm) {
			return getScalingFactor(view, bm.getWidth());
		}

		public static float getScalingFactor(View view, int width) {
			// Get display width from device
			int displayWidth = Activity.getWindowManager().getDefaultDisplay().getWidth();

			// Get margin to use it for calculating to max width of the view
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
			int leftMargin = layoutParams.leftMargin;
			int rightMargin = layoutParams.rightMargin;

			// Calculate the max width of the view
			int viewWidth = displayWidth - (leftMargin + rightMargin);

			// Calculate scaling factor and return it
			return ((float) viewWidth / (float) width);
		}

		public static int getScreenOrientation() {
			// getActivity().getResources().getConfiguration().orientation
			// may be wrong on some devices

			int orientation = Configuration.ORIENTATION_UNDEFINED;
			Display getOrient = Activity.getWindowManager().getDefaultDisplay();
			if (getOrient.getWidth() == getOrient.getHeight()) {
				orientation = Configuration.ORIENTATION_SQUARE;
			} else {
				if (getOrient.getWidth() < getOrient.getHeight()) {
					orientation = Configuration.ORIENTATION_PORTRAIT;
				} else {
					orientation = Configuration.ORIENTATION_LANDSCAPE;
				}
			}
			return orientation;
		}

		public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {

				// Calculate ratios of height and width to requested height and
				// width
				final int heightRatio = Math.round((float) height / (float) reqHeight);
				final int widthRatio = Math.round((float) width / (float) reqWidth);

				// Choose the smallest ratio as inSampleSize value, this will
				// guarantee
				// a final image with both dimensions larger than or equal to
				// the
				// requested height and width.
				inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
			}

			return inSampleSize;
		}

		public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeResource(res, resId, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeResource(res, resId, options);
		}

	}

} // Utility
