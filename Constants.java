package com.joyofplaying.babytracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;

import com.joyofplaying.babytracker.Data.ChildData;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;
import com.joyofplaying.babytracker.LocalData.ChildDataTable;

public class Constants {

	public static final boolean ReleaseMode = true;
	public static final boolean DebugMode = !ReleaseMode;
	public static final boolean UseAcra = true;

	public static class Anim {
		public static final int AddAmountRow = 500;
		public static final int RemoveAmountRow = 200;
		public static final int RemoveDataRow = 250;
		public static final int AmountSwitch = 750;
	}

	public static class Delay {
		public static final int RemoveAmountRow = 200;
		public static final int DisplayRows = 1000;
		public static final int RestoreEntryType = 1000;
		public static final int RestoreFromSettings = 2000;
	}

	public static final String TAG = "BabyTrackerActivity";
	public static final String SettingsName = "BabyTracker";

	public static final int ButtonBarLength = 6;
	public static final int LogoImagePadding = 3;
	public static final int MaxDataEntryRows = 5;
	public static final int MaxDataGridRows = 15;
	public static final int LogoImageDefaultWidth = 50;
	public static final int LogoImageDefaultHeight = 50;
	public static final int FragmentCount = FragmentTypes.values().length;

	// Constants

	// Enumerations
	public enum DataTypes {
		Nurse, Bottle, Pump, Wet, Poop, // Nap,
	}

	public enum TextTypes {
		Date, Time, Amount, Note,
	}

	public enum DialogTypes {
		None, Setup, DatePicker, Options,
	}

	public enum MenuTypes {
		None, ActionBar, DataTable,
	}

	public enum ActivityRequestType {
		None,
		// TakePicture,
		// CropPicture,
		GetPhoto,
	}

	public enum FragmentTypes {
		EntryPage, StatsPage, DataGridPage,
	}

	public enum DateTypes {
		AllDates, Last24, Today, Yesterday, ThisWeek, ThisMonth, SelectDate, StartDate, EndDate,
	}

	public enum SpinDataTypes {
		All, Nurse, Bottle, Pump, Wet, Poop, // Nap,
	}

	public enum SaveToSettingsLevels {
		All(4), Child(3), User(2), SqlVersion(1), None(0);

		private final int id;

		SaveToSettingsLevels(int id) {
			this.id = id;
		}

		public int getValue() {
			return id;
		}
	}

	public enum FragmentStates {
		None, Destroyed, Created, Stopped, Restored, Started, Paused, Resumed, Activated,
	}

	public enum SetupDialogEdits {
		Name, DOB, Email,
	}

	public interface BaseHelperMembers {
		// public void onResume();

		public void onActivate();

		public void restoreFromSettings();

		public void saveToSettings();

		public void saveToSettings(SharedPreferences.Editor editor);

		public BabyTracker getApp();

		public Context getAppContext();

		public SharedPreferences getAppSettings();

		public RegisteredChild getChild();

		public LocalData.ChildDataTable getChildDB();

		public String getChildName();

		public ChildDataTable getChildQuery();

		public int getColor(int id);

		public Drawable getDrawable(int id);

		public Resources getResources();

		public Settings getSettings();

		public String getString(int resId);

		public RegisteredUser getUser();

		public void saveToSettings(String key, boolean value);

		public void saveToSettings(String key, int value);

		public void saveToSettings(String key, String value);

		public void saveToSettings(String key, float value);
	}

	public interface BaseFragmentMembers extends BaseHelperMembers {
		public LogoImage getLogo();

		@Override
		public void onActivate();
	}

	public interface OnTypeButtonClick {
		void onTypeButtonClick(DataTypes type);
	}

	public interface OnDateSelectionChange {
		void onDateSelectionChange();
	}

	public interface OnActivityRequest {
		public void onActivityRequestRecieved(int requestType, boolean succeeded, Intent data);
	}

	public interface OnPhotoUrlChange {
		void onPhotoUrlChange(String photoUrl);
	}

	public interface OnEditChildData {
		public void onEditChild(ChildData data);

		public boolean onDeleteChild(ChildData data);

		public void onUnEditChild(ChildData data);
	}

	public interface OnContextItemSelected {
		public boolean onContextItemSelected(MenuItem item);
	}

	public interface OnScrollViewChanged {
		void onScrollChanged(TableLayoutScrollView scrollView, int x, int y, int oldx, int oldy);

		void onScrolledToBottom();
	}

	public static class ViewDimensions {
		private int width;
		private int height;

		ViewDimensions() {
			this.width = 0;
			this.height = 0;
		}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public void setWidth(int width) {
			this.width = width;
		}
	}
}
