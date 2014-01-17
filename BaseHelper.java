package com.joyofplaying.babytracker;

import java.text.MessageFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.joyofplaying.babytracker.Constants.BaseHelperMembers;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;
import com.joyofplaying.babytracker.LocalData.ChildDataTable;

public abstract class BaseHelper implements BaseHelperMembers {

	private BaseFragment fragment = null;

	public BaseHelper(BaseFragment fragment) {
		this.fragment = fragment;
	}

	public void onResume() {
		if (!getReady())
			return;
	}

	public void postResume() {
		if (!getReady())
			return;
	}

	@Override
	public void onActivate() {
		if (!getActive())
			return;
	}

	public void setFragment(BaseFragment fragment) {
		this.fragment = fragment;
	}

	protected BaseFragment getFragment() {
		return this.fragment;
	}

	protected String getName() {
		if (this.fragment != null) {
			return this.fragment.getName();
		}
		return "";
	}

	protected boolean getReady() {
		if (this.fragment != null) {
			return this.fragment.getReady(Utility.getMethodName(3));
		}
		return true;
	}

	protected boolean getActivated() {
		if (this.fragment != null) {
			return this.fragment.getActivated();
		}
		return true;
	}

	protected boolean canSave() {
		if (this.fragment != null) {
			return this.fragment.canSave();
		}
		return true;
	}

	protected boolean getActive() {
		if (this.fragment != null) {
			return getReady() && this.fragment.getActive();
		}
		return getReady() && true;
	}

	protected boolean isEntryPage() {
		if (getFragment() != null) {
			return getFragment().isEntryPage();
		}
		return false;
	}

	protected boolean isStatsPage() {
		if (getFragment() != null) {
			return getFragment().isStatsPage();
		}
		return false;
	}

	protected boolean isDataGridPage() {
		if (getFragment() != null) {
			return getFragment().isDataGridPage();
		}
		return false;
	}

	protected BabyTrackerActivity getActivity() {
		if (getFragment() != null) {
			return getFragment().getActivityEx();
		}
		return BabyTrackerActivity.getActivity();
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
	public Resources getResources() {
		return getApp().getResources();
	}

	@Override
	public Settings getSettings() {
		return getApp().getSettings();
	}

	@Override
	public String getString(int resId) {
		return getApp().getString(resId);
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

	protected void updateButtonCaptionState(View caption) {
		int state = getSettings().getShowButtonCaptions() ? View.VISIBLE : View.GONE;
		if (caption.getVisibility() != state) {
			caption.setVisibility(state);
			if (state == View.VISIBLE) {
				// parent.setPadding(parent.getPaddingLeft(),
				// parent.getPaddingTop(), parent.getPaddingRight(), 0);
			} else {
				// parent.setPadding(parent.getPaddingLeft(),
				// parent.getPaddingTop(), parent.getPaddingRight(),
				// parent.getPaddingTop());
			}
		}
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

	public void restoreFromSettings(int delay) {
		Utility.runDelayed(new Runnable() {
			@Override
			public void run() {
				restoreFromSettings();
			}
		}, delay);

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
}
