package com.joyofplaying.babytracker;

import java.util.concurrent.atomic.AtomicReference;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;

import com.joyofplaying.babytracker.Constants.BaseHelperMembers;
import com.joyofplaying.babytracker.Data.ChildData;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;
import com.joyofplaying.babytracker.LocalData.ChildDataTable;
import com.joyofplaying.babytracker.LocalData.Query;

@ReportsCrashes(formKey = "", mailTo = "babytracker@pacifier.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_log_toast)
public class BabyTracker extends Application implements BaseHelperMembers {

	private static BabyTracker App = null;

	public static BabyTracker getBabyTracker() {
		return App;
	}

	private LocalData database = null;
	private Query query = null;
	private final Settings settings = new Settings();
	private SharedPreferences appSettings = null;
	private final AtomicReference<ChildData> pendingEditChild = new AtomicReference<ChildData>();

	private LocalData.ChildDataTable childDB = null;

	@Override
	public BabyTracker getApp() {
		return this;
	}

	@Override
	public Context getAppContext() {
		return this;
	}

	@Override
	public SharedPreferences getAppSettings() {
		return this.appSettings;
	}

	@Override
	public RegisteredChild getChild() {
		return getSettings().getChild();
	}

	@Override
	public LocalData.ChildDataTable getChildDB() {
		if (this.childDB == null) {
			if (getChild() != null) {
				this.childDB = getDatabase().createChildDataRecorder(getChild());
			}
		}
		return this.childDB;

	}

	@Override
	public String getChildName() {
		return getSettings().getChildName();
	}

	@Override
	public ChildDataTable getChildQuery() {
		ChildDataTable query = getDatabase().createChildDataRecorder(getChild());
		return query;
	}

	@Override
	public int getColor(int id) {
		return getResources().getColor(id);
	}

	public LocalData getDatabase() {
		return this.database;
	}

	@Override
	public Drawable getDrawable(int id) {
		return getResources().getDrawable(id);
	}

	public ChildData getPendingEditChild() {
		return pendingEditChild.get();
	}

	public Query getQuery() {
		if (this.query == null) {
			this.query = getDatabase().createNewQuery(getChild());
		}
		return this.query;
	}

	@Override
	public Settings getSettings() {
		return this.settings;
	}

	@Override
	public RegisteredUser getUser() {
		return getSettings().getUser();
	}

	@Override
	public void onActivate() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		BabyTracker.App = this;

		if (Constants.UseAcra) {
			// turn on crash log reporting
			org.acra.ACRA.init(this);
		}

		this.appSettings = getSharedPreferences(Constants.SettingsName, 0);
		this.database = new LocalData(this);
		this.database.open();
		this.settings.restoreFromSettings();
	}

	public void reset() {
		this.database.resetDatabase();
		this.database.setupDatabase();
		setChild(null);
		this.settings.reset();
		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.clear();
		editor.commit();
	}

	@Override
	public void restoreFromSettings() {
		getSettings().restoreFromSettings();
		// getApp().setPendingEditChild(null);
		int id = getSettings().getPendingEditChild();
		if (id != 0) {
			if (getChildDB() != null) {
				getApp().setPendingEditChild(getChildDB().getFromId(id));
			}
		}
	}

	@Override
	public void saveToSettings() {
		SharedPreferences.Editor editor = getAppSettings().edit();
		saveToSettings(editor);
		editor.commit();
	}

	@Override
	public void saveToSettings(Editor editor) {
		getSettings().saveToSettings(editor);
	}

	@Override
	public void saveToSettings(String key, boolean value) {
		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	@Override
	public void saveToSettings(String key, float value) {
		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	@Override
	public void saveToSettings(String key, int value) {
		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.putInt(key, value);
		editor.commit();
	}

	@Override
	public void saveToSettings(String key, String value) {
		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void setChild(RegisteredChild value) {
		this.query = null;
		this.childDB = null;
		this.pendingEditChild.set(null);
		getSettings().setChild(value);
	}

	public void setChildDB(ChildDataTable value) {
		this.childDB = value;
	}

	public void setPendingEditChild(ChildData value) {
		this.pendingEditChild.set(value);
		getSettings().setPendingEditChild(value != null ? value.id : 0);
	}
}
