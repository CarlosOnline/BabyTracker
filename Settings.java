package com.joyofplaying.babytracker;

import android.content.SharedPreferences;

import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;

public class Settings extends BaseHelper {

	private static final String BABY_TRACKER_SHOW_BUTTON_CAPTIONS = "BabyTracker_show_button_captions";
	private static final String BABY_TRACKER_REGISTERED = "BabyTracker_registered";
	private static final String BABY_TRACKER_PENDING_EDIT_CHILD = "BabyTracker_pendingEditChild";
	private static final String BABY_TRACKER_DEBUG_MODE = "BabyTracker_debug_mode";
	private static final String BABY_TRACKER_SQL_TABLE_VERSION = "BabyTracker_sqlTableVersion";
	private static final String BABY_TRACKER_PHOTO_URL = "BabyTracker_photo_url";

	private boolean debugMode = false;
	private boolean registered = false;
	private boolean selectDataAfterSubmit = false;
	private boolean showButtonCaptions = true;
	private boolean useAcra = true;

	private String photoUrl = "";
	private RegisteredUser user = null;
	private RegisteredChild child = null;

	private int pendingEditChild = 0;
	private float sqlVersion = LocalData.SqlTableVersion;

	public Settings() {
		super(null);
	}

	@Override
	public RegisteredChild getChild() {
		return this.child;
	}

	@Override
	public String getChildName() {
		RegisteredChild child = getChild();
		if (child != null) {
			String name = child.name;
			if (Utility.IsValid(name))
				return name;
		}

		return getString(R.string.logo_caption);
	}

	public boolean getDebugMode() {
		return this.debugMode;
	}

	public int getPendingEditChild() {
		return this.pendingEditChild;
	}

	public boolean getSelectDateAfterSubmit() {
		return this.selectDataAfterSubmit;
	}

	public void setSelectDateAfterSubmit(boolean value) {
		this.selectDataAfterSubmit = value;
	}

	public String getPhotoUrl() {
		return this.photoUrl;
	}

	public boolean getRegistered() {
		return this.registered && this.child != null;
	}

	public boolean getShowButtonCaptions() {
		this.showButtonCaptions = getAppSettings().getBoolean(BABY_TRACKER_SHOW_BUTTON_CAPTIONS, true);
		return this.showButtonCaptions;
	}

	public float getSqlVersion() {
		return this.sqlVersion;
	}

	public boolean getUseAcra() {
		this.useAcra = org.acra.ACRA.getACRASharedPreferences().getBoolean(org.acra.ACRA.PREF_ENABLE_ACRA, true);
		return this.useAcra;
	}

	@Override
	public RegisteredUser getUser() {
		return this.user;
	}

	public void reset() {
		this.user = null;
		this.child = null;
		this.registered = false;

		this.debugMode = false;
		this.registered = false;
		this.showButtonCaptions = true;
		this.useAcra = true;

		this.photoUrl = "";
		this.user = null;
		this.child = null;

		this.pendingEditChild = 0;
		this.sqlVersion = LocalData.SqlTableVersion;

	}

	@Override
	public void restoreFromSettings() {
		// super.restoreFromSettings();

		Utility.LogMethod();

		this.debugMode = getAppSettings().getBoolean(BABY_TRACKER_DEBUG_MODE, false);
		this.pendingEditChild = getAppSettings().getInt(BABY_TRACKER_PENDING_EDIT_CHILD, 0);
		this.photoUrl = getAppSettings().getString(BABY_TRACKER_PHOTO_URL, "");
		this.registered = getAppSettings().getBoolean(BABY_TRACKER_REGISTERED, false);
		this.showButtonCaptions = getAppSettings().getBoolean(BABY_TRACKER_SHOW_BUTTON_CAPTIONS, false);
		this.sqlVersion = getAppSettings().getFloat(BABY_TRACKER_SQL_TABLE_VERSION, LocalData.SqlTableVersion);
		this.useAcra = org.acra.ACRA.getACRASharedPreferences().getBoolean(org.acra.ACRA.PREF_ENABLE_ACRA, true);
		this.user = RegisteredUser.restoreFromSettings(getAppSettings());
		this.child = RegisteredChild.restoreFromSettings(getAppSettings());
		if (this.child != null) {
			// Always use child's photoUrl if it is valid
			if (this.child.id != 0 && 0 != this.photoUrl.compareTo(this.child.photoUrl)) {
				setPhotoUrl(this.child.photoUrl);
			}
		}
	}

	@Override
	public void saveToSettings(SharedPreferences.Editor editor) {
		// super.saveToSettings(editor);

		Utility.LogMethod();

		editor.putBoolean(BABY_TRACKER_DEBUG_MODE, this.debugMode);
		editor.putInt(BABY_TRACKER_PENDING_EDIT_CHILD, this.pendingEditChild);
		editor.putString(BABY_TRACKER_PHOTO_URL, this.photoUrl);
		editor.putBoolean(BABY_TRACKER_REGISTERED, this.registered);
		editor.putBoolean(BABY_TRACKER_SHOW_BUTTON_CAPTIONS, this.showButtonCaptions);
		editor.putFloat(BABY_TRACKER_SQL_TABLE_VERSION, this.sqlVersion);
		if (this.user != null) {
			this.user.saveToSettings(editor);
		}
		if (this.child != null) {
			this.child.saveToSettings(editor);
		}

		editor = org.acra.ACRA.getACRASharedPreferences().edit();
		editor.putBoolean(org.acra.ACRA.PREF_ENABLE_ACRA, this.useAcra);
		editor.putBoolean(org.acra.ACRA.PREF_DISABLE_ACRA, !this.useAcra);
	}

	public void setChild(RegisteredChild value) {
		this.child = value;
	}

	public void setDebugMode(boolean flag) {
		this.debugMode = flag;
		saveToSettings(BABY_TRACKER_DEBUG_MODE, this.pendingEditChild);
	}

	public void setPendingEditChild(int id) {
		this.pendingEditChild = id;
		saveToSettings(BABY_TRACKER_PENDING_EDIT_CHILD, this.pendingEditChild);
	}

	public void setPhotoUrl(String photoUrl) {
		if (!Utility.IsValid(photoUrl))
			photoUrl = "";

		this.photoUrl = photoUrl;
		Utility.LogMethod("{0}", photoUrl);
		saveToSettings(BABY_TRACKER_PHOTO_URL, photoUrl);

		RegisteredChild child = getChild();
		if (child != null && child.id != 0) {
			if (child.photoUrl.compareTo(photoUrl) != 0) {
				child.photoUrl = photoUrl;

				SharedPreferences.Editor editor = getAppSettings().edit();
				child.saveToSettings(editor);
				editor.commit();
			}
		}
	}

	public void setRegistered(boolean flag) {
		this.registered = flag;
	}

	public void setShowButtonCaptions(boolean value) {
		this.showButtonCaptions = value;
		saveToSettings(BABY_TRACKER_SHOW_BUTTON_CAPTIONS, this.showButtonCaptions);
	}

	public void setSqlVersion(float value) {
		this.sqlVersion = value;
		saveToSettings(BABY_TRACKER_SQL_TABLE_VERSION, this.sqlVersion);
	}

	public void setUseAcra(boolean useAcra) {
		this.useAcra = useAcra;
		SharedPreferences.Editor editor = org.acra.ACRA.getACRASharedPreferences().edit();
		editor.putBoolean(org.acra.ACRA.PREF_ENABLE_ACRA, this.useAcra);
		editor.putBoolean(org.acra.ACRA.PREF_DISABLE_ACRA, !this.useAcra);
		editor.commit();
	}

	public void setUser(RegisteredUser value) {
		this.user = value;
		if (this.user != null) {
			SharedPreferences.Editor editor = getAppSettings().edit();
			this.user.saveToSettings(editor);
			editor.commit();
		}
	}
}
