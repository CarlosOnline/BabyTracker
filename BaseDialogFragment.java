package com.joyofplaying.babytracker;

import java.text.MessageFormat;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joyofplaying.babytracker.Constants.BaseHelperMembers;
import com.joyofplaying.babytracker.Constants.FragmentStates;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;
import com.joyofplaying.babytracker.LocalData.ChildDataTable;

public abstract class BaseDialogFragment extends DialogFragment implements BaseHelperMembers {

	private final String name = "";
	private FragmentStates state = FragmentStates.None;
	private BabyTrackerActivity activity = null;
	private BaseFragment fragment = null;

	protected boolean getReady() {
		return this.state.ordinal() >= FragmentStates.Resumed.ordinal();
	}

	protected boolean getReady(String caller) {
		return this.state.ordinal() >= FragmentStates.Resumed.ordinal();
	}

	protected boolean canSave() {
		switch (this.state) {
		case Created:
		case Restored:
		case Started:
		case Paused:
		case Resumed:
		case Activated:
			return true;

		default:
			return false;
		}
	}

	protected void InitDialog(BabyTrackerActivity activity, BaseFragment parent) {
		this.activity = activity;
		this.setFragment(parent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.state = FragmentStates.Created;
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		LogMethod();
		this.state = FragmentStates.Created;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		LogMethod();
		super.onSaveInstanceState(outState);
		saveToSettings();
	}

	@Override
	public void onStart() {
		super.onStart();

		LogMethod();
		this.state = FragmentStates.Started;
	}

	@Override
	public void onPause() {
		super.onPause();

		LogMethod();
		this.state = FragmentStates.Paused;
		saveToSettings();
	}

	// API level 17
	// public void onViewStateRestored(Bundle savedInstanceState) {
	// this.state = FragmentStates.Restored;
	// }

	@Override
	public void onResume() {
		super.onResume();

		LogMethod();
		this.state = FragmentStates.Resumed;

		restoreFromSettings();
	}

	protected void postResume() {
		LogMethod();
		onActivate();
	}

	@Override
	public void onActivate() {
		if (!getActive())
			return;

		LogMethod();
		this.state = FragmentStates.Activated;
	}

	public void onDeActivate() {
		LogMethod();

		if (!getReady())
			return;
	}

	@Override
	public void onStop() {
		super.onStop();

		LogMethod();
		this.state = FragmentStates.Stopped;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		LogMethod();
		this.state = FragmentStates.Destroyed;
	}

	protected BabyTrackerActivity getActivityEx() {
		Activity activity = getActivity();
		if (activity != null) {
			return (BabyTrackerActivity) activity;
		}
		if (this.activity != null) {
			return this.activity;
		}

		return BabyTrackerActivity.getActivity();
	}

	protected BaseFragment getFragment() {
		return this.fragment;
	}

	protected void setFragment(BaseFragment value) {
		this.fragment = value;
	}

	public String getName() {
		return this.name;
	}

	protected boolean getActive() {
		return getReady();
	}

	protected boolean getActivated() {
		return getActive() && this.state == FragmentStates.Activated;
	}

	@SuppressWarnings("unused")
	public void LogMethod() {
		if (Constants.ReleaseMode)
			return;

		String method = Utility.getMethodName(3);
		Utility.Log("<{0}> {1}", getName(), method);
	}

	@SuppressWarnings("unused")
	public void LogMethod(String message, Object... args) {
		if (Constants.ReleaseMode)
			return;

		String str = MessageFormat.format(message, args);
		String method = Utility.getMethodName(3);
		// Utility.Log(String.format("<%s> -30%s %s", getName(), method, str));
		// Utility.Log(String.format("<%s> %s %s", getName(), method, str));
		Utility.Log("<{0}> {1} {2}", getName(), method, str);
	}

	@Override
	public BabyTracker getApp() {
		return BabyTracker.getBabyTracker();
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

	@Override
	public void saveToSettings(String key, boolean value) {
		getApp().saveToSettings(key, value);
	}

	@Override
	public void saveToSettings(String key, int value) {
		getApp().saveToSettings(key, value);
	}

	@Override
	public void saveToSettings(String key, String value) {
		getApp().saveToSettings(key, value);
	}

	@Override
	public void saveToSettings(String key, float value) {
		getApp().saveToSettings(key, value);
	}

	@Override
	public void restoreFromSettings() {
		if (!getReady())
			return;
		LogMethod();
		getApp().restoreFromSettings();
	}

	@Override
	public void saveToSettings() {
		if (!canSave())
			return;

		SharedPreferences.Editor editor = getAppSettings().edit();
		saveToSettings(editor);
		editor.commit();
	}

	@Override
	public void saveToSettings(Editor editor) {
		if (!canSave())
			return;

		LogMethod();
		getApp().saveToSettings(editor);
	}
}
