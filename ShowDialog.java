package com.joyofplaying.babytracker;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences.Editor;

import com.joyofplaying.babytracker.CalendarDialog.OnCalendarChangedListener;
import com.joyofplaying.babytracker.Constants.DialogTypes;

public class ShowDialog extends BaseHelper {
	private DialogTypes dialogType = DialogTypes.None;
	private int stackLevel = 0;

	public ShowDialog(BaseFragment fragment, DialogTypes type) {
		super(fragment);
		this.dialogType = type;
	}

	public static void showSetupDialog(BaseFragment owner) {
		ShowDialog dlg = new ShowDialog(owner, DialogTypes.Setup);
		dlg.showDialog();
	}

	public static void showDatePickerDialog(BaseFragment owner, int month, int day, int year, OnCalendarChangedListener callBack) {
		ShowDialog dlg = new ShowDialog(owner, DialogTypes.DatePicker);
		dlg.showDatePickerDialog(month, day, year, callBack);
	}

	public static void showDatePickerDialog(BaseFragment owner, OnCalendarChangedListener callBack) {
		ShowDialog dlg = new ShowDialog(owner, DialogTypes.DatePicker);
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		dlg.showDatePickerDialog(month, day, year, callBack);
	}

	public static void showOptionsDialog(BaseFragment owner) {
		ShowDialog dlg = new ShowDialog(owner, DialogTypes.Options);
		dlg.showDialog();
	}

	private void showDialog() {
		showDialog(getDialogFragment());
	}

	private void showDatePickerDialog(int month, int day, int year, OnCalendarChangedListener callBack) {
		DialogFragment dlgFragment = CalendarDialog.newInstance(getActivity(), stackLevel, month, day, year, callBack);
		showDialog(dlgFragment);
	}

	@SuppressLint("CommitTransaction")
	private void showDialog(DialogFragment dlgFragment) {
		Utility.LogMethod("{0}", this.dialogType.toString());

		if (dlgFragment == null) {
			Utility.LogError("No DialogFragment for {0}", this.dialogType.toString());
			return;
		}

		stackLevel++;
		saveToSettings();

		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		dlgFragment.show(ft, "dialog");
	}

	private DialogFragment getDialogFragment() {
		switch (this.dialogType) {
		case Setup:
			return SetupDialog.newInstance(getActivity(), stackLevel, getFragment());

		case DatePicker:
			Utility.LogError("DatePicker must be initialized properly");
			return null;

		case Options:
			// Create and show the dialog.
			return SettingsDialog.newInstance(getActivity(), stackLevel);
		}
		return null;
	}

	private FragmentManager getFragmentManager() {
		return getActivity().getFragmentManager();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!getReady())
			return;
		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		if (!getActive())
			return;
	}

	@Override
	public void restoreFromSettings() {
		this.stackLevel = getAppSettings().getInt("ShowDialog_stackLevel", 0);
		this.dialogType = DialogTypes.values()[getAppSettings().getInt("ShowDialog_dialogType", DialogTypes.None.ordinal())];
	}

	@Override
	public void saveToSettings(Editor editor) {
		editor.putInt("ShowDialog_stackLevel", this.stackLevel);
		editor.putInt("ShowDialog_dialogType", this.dialogType.ordinal());
	}
}
