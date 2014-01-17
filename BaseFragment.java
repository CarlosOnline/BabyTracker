package com.joyofplaying.babytracker;

import java.text.MessageFormat;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.joyofplaying.babytracker.Constants.BaseFragmentMembers;
import com.joyofplaying.babytracker.Constants.FragmentStates;
import com.joyofplaying.babytracker.Constants.FragmentTypes;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;
import com.joyofplaying.babytracker.LocalData.ChildDataTable;

public abstract class BaseFragment extends Fragment implements BaseFragmentMembers {

	private LogoImage logo = null;
	private String name = "";
	private FragmentStates state = FragmentStates.None;
	private FragmentTypes type = null;

	public void onCreateView(FragmentTypes type, View view) {
		Utility.LogMethod();
		this.type = type;
		this.name = type.toString();
		LogMethod();
		getActivityEx().onPageCreated(this);
		this.logo = new LogoImage(this, view, null);
	}

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

	@Override
	public LogoImage getLogo() {
		return this.logo;
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
		getActivityEx().onPageCreated(this);
		this.state = FragmentStates.Resumed;

		if (this.logo != null) {
			this.logo.onResume();
		}

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
		if (this.logo != null) {
			this.logo.onActivate();
		}
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
		if (getActivity() != null) {
			return (BabyTrackerActivity) getActivity();
		}
		return (BabyTrackerActivity) getActivity();
	}

	@Override
	public Settings getSettings() {
		return getApp().getSettings();
	}

	@Override
	public SharedPreferences getAppSettings() {
		return getApp().getAppSettings();
	}

	@Override
	public Context getAppContext() {
		return getApp().getApplicationContext();
	}

	@Override
	public int getColor(int id) {
		return getApp().getColor(id);
	}

	public String getName() {
		return this.name;
	}

	public FragmentTypes getType() {
		return this.type;
	}

	protected boolean getActive() {
		return getReady() && getActivityEx().getCurrentFragmentType() == this.type;
	}

	protected boolean getActivated() {
		return getActive() && this.state == FragmentStates.Activated;
	}

	@Override
	public BabyTracker getApp() {
		return (BabyTracker) getActivity().getApplication();
	}

	@Override
	public RegisteredChild getChild() {
		return getApp().getChild();
	}

	@Override
	public ChildDataTable getChildDB() {
		return getApp().getChildDB();
	}

	@Override
	public String getChildName() {
		return getApp().getChildName();
	}

	@Override
	public ChildDataTable getChildQuery() {
		return getApp().getChildQuery();
	}

	@Override
	public Drawable getDrawable(int id) {
		return getApp().getDrawable(id);
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

	protected boolean isType(FragmentTypes type) {
		return getType() == type;
	}

	protected boolean isEntryPage() {
		return getType() == FragmentTypes.EntryPage;
	}

	protected boolean isStatsPage() {
		return getType() == FragmentTypes.StatsPage;
	}

	protected boolean isDataGridPage() {
		return getType() == FragmentTypes.DataGridPage;
	}

	@Override
	public void restoreFromSettings() {
		if (!getReady())
			return;
		LogMethod();

		getApp().restoreFromSettings();

		if (this.logo != null) {
			this.logo.restoreFromSettings();
		}
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
		if (this.logo != null) {
			this.logo.saveToSettings(editor);
		}
	}
}
