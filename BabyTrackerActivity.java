package com.joyofplaying.babytracker;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.joyofplaying.babytracker.Constants.ActivityRequestType;
import com.joyofplaying.babytracker.Constants.FragmentTypes;
import com.joyofplaying.babytracker.Constants.MenuTypes;
import com.joyofplaying.babytracker.Constants.OnActivityRequest;
import com.joyofplaying.babytracker.Constants.OnContextItemSelected;
import com.joyofplaying.babytracker.Constants.OnEditChildData;
import com.joyofplaying.babytracker.Constants.SaveToSettingsLevels;
import com.joyofplaying.babytracker.Data.ChildData;
import com.joyofplaying.babytracker.Data.RegisteredChild;
import com.joyofplaying.babytracker.Data.RegisteredUser;

public class BabyTrackerActivity extends BaseFragmentActivity implements OnEditChildData {

	public static BabyTrackerActivity getActivity() {
		return (BabyTrackerActivity) BaseFragmentActivity.getActivity();
	}

	private BabyAdapter adapter = null;
	private MenuTypes menuType = MenuTypes.None;
	private OnContextItemSelected contextMenuCallback = null;
	private final OnActivityRequest[] activityCallbacks = new OnActivityRequest[ActivityRequestType.values().length];

	public boolean deleteData(ChildData data) {
		Utility.LogMethod();
		if (getChildDB() == null) {
			return false;
		}

		return getChildDB().delete(data);
	}

	public void dumpSettings() {
		Utility.LogMethod();
		Map<String, ?> map = getAppSettings().getAll();
		if (map != null) {
			SortedSet<String> keys = new TreeSet<String>(map.keySet());
			for (String key : keys) {
				Object oValue = map.get(key);
				String value = "";
				if (oValue != null) {
					value = oValue.toString();
				}
				Utility.Log("    setting: {0} : {1}", key, value);
			}
		}
	}

	public void exportData() {
		String filePath = exportDataToFile();
		if (Utility.IsValid(filePath)) {
			sendExportEmail(filePath);
		}
	}

	private String exportDataToFile() {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Utility.showToast(getString(R.string.missing_sd_card));
			return null;
		}

		try {
			File file = Utility.getUniqueFile(getChildName(), ".csv");

			FileOutputStream fos = new FileOutputStream(file);
			getChildDB().exportToFileStream(fos);
			fos.flush();
			fos.close();
			return file.getAbsolutePath();
		} catch (Exception e) {
			// handle exception
			Utility.showToast(getString(R.string.error_saving_exported_data));
			return null;
		}
	}

	private BabyAdapter getAdapter() {
		return this.adapter;
	}

	public FragmentTypes getCurrentFragmentType() {
		return getAdapter().getCurrent();
	}

	public BaseFragment getCurrentPage() {
		return getAdapter().getCurrentPage();
	}

	private LocalData getDatabase() {
		return getApp().getDatabase();
	}

	public EntryPage getEntryPage() {
		return getAdapter().getEntryPage();
	}

	@Override
	public void onActivate() {
		if (this.tryGetCurrentPage() != null) {
			this.getCurrentPage().onActivate();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Utility.LogMethod();
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode < 0 || requestCode > ActivityRequestType.values().length) {
			return;
		}

		boolean succeeded = resultCode == Activity.RESULT_OK;
		OnActivityRequest callback = this.activityCallbacks[requestCode];
		if (callback != null) {
			callback.onActivityRequestRecieved(requestCode, succeeded, data);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Utility.LogMethod();
		if (this.contextMenuCallback != null) {
			return this.contextMenuCallback.onContextItemSelected(item);
		} else if (getAdapter().getCurrentPage() != null) {
			return (getAdapter().getCurrentPage().onContextItemSelected(item));
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.LogMethod();

		final ActionBar bar = getActionBar();
		if (bar != null) {
			bar.setDisplayShowHomeEnabled(true);
			bar.setDisplayShowTitleEnabled(true);
			bar.setDisplayUseLogoEnabled(true);
		}

		setContentView(R.layout.main);

		ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
		this.adapter = new BabyAdapter(this, pager);

		// Needs to be done here in case onResume is not called
		restoreFromSettings();

		if (Constants.DebugMode) {
			dumpSettings();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		Utility.LogMethod();
		super.onCreateContextMenu(menu, view, menuInfo);
		MenuInflater inflater = getMenuInflater();
		switch (this.menuType) {
		case DataTable:
			inflater.inflate(R.layout.table_row_menu, menu);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Utility.LogMethod();
		MenuInflater inflater = getMenuInflater();
		switch (this.menuType) {
		case None:
		case ActionBar:
			inflater.inflate(R.layout.actionbar_menus, menu);
			return true;

		case DataTable:
			inflater.inflate(R.layout.table_row_menu, menu);
			return true;
		}
		return false;
	}

	@Override
	public boolean onDeleteChild(ChildData data) {
		getApp().setPendingEditChild(null);
		return false;
	}

	@Override
	protected void onDestroy() {
		Utility.LogMethod();
		super.onDestroy();
	}

	@Override
	public void onEditChild(ChildData data) {
		Utility.LogMethod();
		getApp().setPendingEditChild(data);
		showEntryPage();
	}

	/**
	 * UI Callback methods
	 */
	public void onMenuItemClick(MenuItem menu) {
		Utility.LogMethod();
		Utility.Log("Menu title = {0} {1}", menu.getTitle(), menu.getItemId());

		getCurrentPage().saveToSettings();

		switch (menu.getItemId()) {
		case R.id.menu_setup:
			ShowDialog.showSetupDialog(getCurrentPage());
			break;

		case R.id.menu_options:
			ShowDialog.showOptionsDialog(getCurrentPage());
			break;

		case R.id.menu_stats_page:
			showStatsPage();
			break;

		case R.id.menu_data_entry:
			showEntryPage();
			break;

		case R.id.menu_data_grid:
			showDataGridPage();
			break;

		case R.id.menu_change_photo:
			getAdapter().getCurrentPage().getLogo().showChangeChildPhotoDialog();
			break;
		}
	}

	public void onPageCreated(BaseFragment page) {
		getAdapter().onPageCreated(page);
	}

	@Override
	protected void onPause() {
		Utility.LogMethod();
		super.onPause();
		saveToSettings();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		FragmentTypes type = getAdapter().getCurrent();
		menu.findItem(R.id.menu_data_entry).setVisible(type != FragmentTypes.EntryPage);
		menu.findItem(R.id.menu_stats_page).setVisible(type != FragmentTypes.StatsPage);
		menu.findItem(R.id.menu_data_grid).setVisible(type != FragmentTypes.DataGridPage);
		return true;
	}

	@Override
	protected void onRestart() {
		Utility.LogMethod();
		super.onRestart();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Utility.LogMethod();
		super.onSaveInstanceState(outState);
		// saveToSettings(outState);
		saveToSettings();
	}

	@Override
	protected void onStop() {
		Utility.LogMethod();
		super.onStop();
	}

	@Override
	public void onUnEditChild(ChildData data) {
		getApp().setPendingEditChild(null);
	}

	public void openContextMenu(View view, OnContextItemSelected callback) {
		this.contextMenuCallback = callback;
		openContextMenu(view);
	}

	/**
	 * Other methods
	 */

	public void registerChild(String babyName, String birthday, String userId, String photoUrl) {
		Utility.LogMethod();

		getSettings().setRegistered(false);
		RegisteredUser user = new RegisteredUser();
		user.name = "Parent";
		user.user_id = userId;
		user.password = "no_password";
		getSettings().setUser(user);

		RegisteredChild child = new RegisteredChild();
		child.name = babyName;
		child.dob = birthday;
		child.photoUrl = photoUrl;
		getApp().setChild(child);
		getSettings().setPhotoUrl(photoUrl);
		saveToSettings();

		getDatabase().setupDatabase();

		LocalData.RegisteredUserTable userDS = getDatabase().createNewUser();
		user = userDS.create(user);
		if (user != null) {
			child.user_token = user.token;
			child = getDatabase().createNewChild().create(child);
			getApp().setChild(child);
			if (child != null && getChildDB() != null) {
				getSettings().setRegistered(true);
				saveToSettings();
				Utility.showToast(getString(R.string.database_has_been_setup), babyName);
				if (getCurrentPage() != null) {
					getCurrentPage().restoreFromSettings();
					getCurrentPage().onActivate();
				}
				return;
			}
		}

		saveToSettings();
		Utility.Error(getString(R.string.failed_to_setup_database), babyName);
	}

	public void reloadViews() {
		restoreFromSettings();
		if (getCurrentPage().getReady()) {
			getCurrentPage().onResume();
			getCurrentPage().onActivate();
		}
	}

	public void resetBabyData() {
		Utility.LogMethod();
		getApp().reset();
		saveBareBonesSettings(SaveToSettingsLevels.SqlVersion);
		restoreFromSettings();
		showEntryPage();
		reloadViews();
		dumpSettings();
	}

	@Override
	public void restoreFromSettings() {
		super.restoreFromSettings();
		Utility.LogMethod();

		try {
			restoreFromSettingsInnerWorker();
		} catch (Exception e) {
			Utility.LogError("Restore exception {0}", e.toString());
			Utility.LogError("Resetting all data, re-attempting the restoreFromSettings()");
			Utility.LogExceptionCallStack(e);
			dumpSettings();
			Utility.showToast(getString(R.string.restore_from_settings_failed));

			for (SaveToSettingsLevels level : SaveToSettingsLevels.values()) {
				try {
					saveBareBonesSettings(level);
					restoreFromSettingsInnerWorker();
					break;
				} catch (Exception e2) {
					Utility.LogError("Restore exception {0}", e2.toString());
					Utility.LogExceptionCallStack(e2);
				}
			}
		}
	}

	public void restoreFromSettingsInnerWorker() {
		getApp().restoreFromSettings();

		float sqlTableVersion = getSettings().getSqlVersion();
		if (sqlTableVersion != LocalData.SqlTableVersion) {
			Utility.LogError("Invalid SqlTableVersion {0} != {1} - Database upgrade is needed", sqlTableVersion,
					LocalData.SqlTableVersion);
			resetBabyData();
			return;
		}

		if (getSettings().getRegistered()) {
			getApp().setChildDB(getDatabase().openChildDataTable(getChild()));
			if (getChildDB() != null) {
				getSettings().setRegistered(true);
			} else {
				Utility.Error(getString(R.string.failed_to_re_connect_to_database));
				getSettings().setRegistered(false);
			}
		}

		getAdapter().restoreFromSettings();
	}

	public void saveBareBonesSettings(SaveToSettingsLevels level) {
		Utility.LogMethod(level.toString());

		getApp().reset();

		SharedPreferences.Editor editor = getAppSettings().edit();
		if (level.ordinal() >= SaveToSettingsLevels.SqlVersion.ordinal()) {
			getSettings().setSqlVersion(LocalData.SqlTableVersion);
		}

		if (level.ordinal() >= SaveToSettingsLevels.User.ordinal()) {
			if (getUser() != null) {
				getUser().saveToSettings(editor);
			}
		}

		if (level.ordinal() >= SaveToSettingsLevels.Child.ordinal()) {
			if (getChild() != null) {
				getChild().saveToSettings(editor);
				getApp().saveToSettings(editor);
			}
		}
		editor.commit();
	}

	public void saveChildToSettings(SharedPreferences.Editor editor) {
		if (getChild() != null) {
			getChild().saveToSettings(editor);
		}
	}

	@Override
	public void saveToSettings(SharedPreferences.Editor editor) {
		super.saveToSettings(editor);
		Utility.LogMethod();
		getAdapter().saveToSettings(editor);
	}

	private void sendExportEmail(String filePath) {
		final String subject = MessageFormat.format(getString(R.string.export_email_subject), getChildName());
		final String body = MessageFormat.format(getString(R.string.export_email_body), getChildName());
		final String to = getSettings().getUser().user_id;
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		Utility.LogMethod("{0}", getSettings().getUser().toString());

		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { to });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));

		startActivity(Intent.createChooser(emailIntent, getString(R.string.email_data_chooser)));
	}

	public void setActivityResultCallback(ActivityRequestType type, OnActivityRequest callback) {
		this.activityCallbacks[type.ordinal()] = callback;
	}

	public void setMenuType(MenuTypes type) {
		this.menuType = type;
	}

	public void showDataGridPage() {
		getAdapter().showPage(FragmentTypes.DataGridPage);
	}

	public void showEntryPage() {
		getAdapter().showPage(FragmentTypes.EntryPage);
	}

	public void showStatsPage() {
		getAdapter().showPage(FragmentTypes.StatsPage);
	}

	public ChildData submitData(ChildData data) {
		Utility.LogMethod();
		if (getChildDB() == null) {
			return null;
		}
		data = getChildDB().create(data);
		return data;
	}

	public BaseFragment tryGetCurrentPage() {
		return getAdapter().tryCurrentPage();
	}

	public DataGridPage tryGetDataGridPage() {
		return (DataGridPage) getAdapter().tryItem(FragmentTypes.DataGridPage);
	}

	public EntryPage tryGetEntryPage() {
		return (EntryPage) getAdapter().tryItem(FragmentTypes.EntryPage);
	}

	public StatsPage tryGetStatsPage() {
		return (StatsPage) getAdapter().tryItem(FragmentTypes.StatsPage);
	}

	public ChildData updateData(ChildData data) {
		Utility.LogMethod();
		if (getChildDB() == null) {
			return null;
		}

		data = getChildDB().update(data);
		return data;
	}
} // BabyTrackerActivity

