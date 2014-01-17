package com.joyofplaying.babytracker;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.joyofplaying.babytracker.Constants.BaseHelperMembers;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;
import com.joyofplaying.babytracker.LocalData.ChildDataTable;

public abstract class BaseFragmentActivity extends FragmentActivity implements BaseHelperMembers {

	private BabyTracker application = null;
	private static BaseFragmentActivity activity = null;

	public BaseFragmentActivity() {
		super();
		this.application = (BabyTracker) getApplication();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.application = (BabyTracker) getApplication();
		BaseFragmentActivity.activity = this;
		super.onCreate(savedInstanceState);
		Utility.NewUtility(this);
		Utility.LogMethod();
	}

	public void onCreateView(View view) {
		BaseFragmentActivity.activity = this;
	}

	@Override
	public void restoreFromSettings() {
		getApp().restoreFromSettings();
	}

	@Override
	public void saveToSettings() {
		Utility.LogMethod();
		SharedPreferences.Editor editor = getAppSettings().edit();
		saveToSettings(editor);
		editor.commit();
	}

	@Override
	public void saveToSettings(Editor editor) {
		getApp().saveToSettings(editor);
	}

	@Override
	public void saveToSettings(String key, boolean value) {
		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.putBoolean(key, value);
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

	@Override
	public void saveToSettings(String key, float value) {
		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public void saveToSettings(Bundle outState) {
		Utility.LogMethod();
		saveToSettings();

		Map<String, ?> map = getAppSettings().getAll();
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (value == null) {
				// outState.remove(header + key);
			} else if (value instanceof Integer) {
				outState.putInt(key, (Integer) value);
			} else if (value instanceof Long) {
				outState.putLong(key, (Long) value);
			} else if (value instanceof Boolean) {
				outState.putBoolean(key, (Boolean) value);
			} else if (value instanceof Float) {
				outState.putFloat(key, (Float) value);
			} else if (value instanceof CharSequence) {
				outState.putString(key, ((CharSequence) value).toString());
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public static BaseFragmentActivity getActivity() {
		return BaseFragmentActivity.activity;
	}

	@Override
	public BabyTracker getApp() {
		return this.application;
	}

	@Override
	public Context getAppContext() {
		return getApp().getApplicationContext();
	}

	@Override
	public SharedPreferences getAppSettings() {
		return getApp().getAppSettings();
	}

	@Override
	public ChildDataTable getChildQuery() {
		return getApp().getChildQuery();
	}

	@Override
	public int getColor(int id) {
		return getApp().getColor(id);
	}

	@Override
	public Drawable getDrawable(int id) {
		return getApp().getDrawable(id);
	}

	@Override
	public Settings getSettings() {
		return getApp().getSettings();
	}

	@Override
	public RegisteredChild getChild() {
		return getApp().getChild();
	}

	@Override
	public LocalData.ChildDataTable getChildDB() {
		return getApp().getChildDB();
	}

	@Override
	public String getChildName() {
		return getApp().getChildName();
	}

	@Override
	public RegisteredUser getUser() {
		return getApp().getUser();
	}
}
