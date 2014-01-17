package com.joyofplaying.babytracker;

import java.util.Calendar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.joyofplaying.babytracker.CalendarDialog.OnCalendarChangedListener;
import com.joyofplaying.babytracker.Constants.OnPhotoUrlChange;
import com.joyofplaying.babytracker.Constants.SetupDialogEdits;
import com.joyofplaying.babytracker.Data.RegisteredChild;

public class SetupDialog extends BaseDialogFragment implements OnClickListener, OnKeyListener, OnPhotoUrlChange,
		OnCalendarChangedListener, OnFocusChangeListener {
	private static final String SETUP_DIALOG_PHOTO_URL = "SetupDialog_photoUrl";
	private static final String SETUP_DIALOG_USER_ID = "SetupDialog_user_id";
	private static final String SETUP_DIALOG_CUR_EDIT = "SetupDialog_curEdit";
	private static final String SETUP_DIALOG_USERID = "SetupDialog_userid";
	private static final String SETUP_DIALOG_DOB = "SetupDialog_dob";
	private static final String SETUP_DIALOG_NAME = "SetupDialog_name";
	public Button DoneBtn = null;
	public EditText editName = null;
	public EditText editDOB = null;
	public EditText editEmail = null;
	private LogoImage logo = null;
	private String name = "";
	private String dob = "";
	private String userid = "";
	private int month = Calendar.getInstance().get(Calendar.MONTH);
	private int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	private int year = Calendar.getInstance().get(Calendar.YEAR);
	private SetupDialogEdits curEdit = SetupDialogEdits.Name;

	public static SetupDialog newInstance(BabyTrackerActivity activity, int stackLevel, BaseFragment parent) {
		Utility.LogMethod();
		SetupDialog dlg = new SetupDialog();
		dlg.InitDialog(activity, parent);
		return dlg;
	}

	@Override
	protected void InitDialog(BabyTrackerActivity activity, BaseFragment parent) {
		super.InitDialog(activity, parent);

		Bundle args = new Bundle();
		RegisteredChild child = getSettings().getChild();
		if (child != null) {
			args.putString(SETUP_DIALOG_NAME, child.name);
			args.putString(SETUP_DIALOG_DOB, child.dob);
			args.putString(SETUP_DIALOG_USER_ID, getSettings().getUser().user_id);
			args.putString(SETUP_DIALOG_PHOTO_URL, child.photoUrl);
		}
		this.setArguments(args);
	}

	private int getMonth() {
		return this.month;
	}

	private int getDay() {
		return this.day;
	}

	private int getYear() {
		return this.year;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Utility.LogMethod();
		super.onSaveInstanceState(outState);

		if (!canSave())
			return;

		String name = this.editName.getText().toString();
		String dob = this.editDOB.getText().toString();
		String user_id = this.editEmail.getText().toString();
		outState.putString(SETUP_DIALOG_NAME, name);
		outState.putString(SETUP_DIALOG_DOB, dob);
		outState.putString(SETUP_DIALOG_USER_ID, user_id);
		outState.putString(SETUP_DIALOG_PHOTO_URL, LogoImage.lookupPhotoUrl());
		saveToSettings();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restoreFromSettings();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		Utility.LogMethod();
		getDialog().setTitle("Enter Baby Information");
		View v = inflater.inflate(R.layout.setup_user, container, false);

		restoreFromSettings();

		Bundle args = getArguments();

		if (args.getString("name") != "") {
			this.name = args.getString("name");
		}

		if (args.getString("dob") != "") {
			this.dob = args.getString("dob");
		}

		if (args.getString("userid") != "") {
			this.userid = args.getString("userid");
		}

		// Watch for button clicks.
		this.DoneBtn = (Button) v.findViewById(R.id.btnDone);
		this.DoneBtn.setOnClickListener(this);

		Button selectPhotoBtn = (Button) v.findViewById(R.id.btnSelectPhoto);
		selectPhotoBtn.setOnClickListener(this);

		this.editName = (EditText) v.findViewById(R.id.editBabyName);
		this.editName.setOnKeyListener(this);
		this.editName.setText(this.name);

		this.editDOB = (EditText) v.findViewById(R.id.editBabyDOB);
		this.editDOB.setOnClickListener(this);
		this.editDOB.setOnKeyListener(this);
		this.editDOB.setOnFocusChangeListener(this);
		this.editDOB.setText(args.getString("dob"));

		this.editEmail = (EditText) v.findViewById(R.id.editUserEmail);
		this.editEmail.setOnKeyListener(this);
		this.editEmail.setText(args.getString("user_id"));

		this.logo = new LogoImage(null, v, this);
		// Update SetupDialog's image at load time
		this.onPhotoUrlChange(getSettings().getPhotoUrl());

		updateDoneBtn();
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		this.logo.onResume();

		restoreFromSettings();
		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		this.logo.onActivate();
	}

	@Override
	public void onPhotoUrlChange(String photoUrl) {
		this.logo.onPhotoUrlChange(photoUrl);
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (!getReady())
			return;

		switch (view.getId()) {
		case R.id.editBabyDOB:
			if (hasFocus) {
				saveToSettings();
				ShowDialog.showDatePickerDialog(getFragment(), getMonth(), getDay(), getYear(), this);
			}
			break;
		}
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		Utility.LogMethod();
		switch (view.getId()) {
		case R.id.editBabyDOB:
			saveToSettings();
			ShowDialog.showDatePickerDialog(getFragment(), getMonth(), getDay(), getYear(), this);
			break;

		case R.id.btnDone:
			if (onSetupDoneClick(view)) {
				dismiss();
			}
			break;

		case R.id.btnSelectPhoto:
			String name = this.editName.getText().toString();
			if (name == "" || name == null) {
				name = "babyTrackerImage" + ((int) Math.random() * 1000);
			}
			this.logo.showSelectPhotoDialog(name);
			break;
		}
	}

	@Override
	public void onDateChanged(int year, int monthOfYear, int dayOfMonth) {
		Utility.LogMethod();

		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		this.month = monthOfYear;
		this.day = dayOfMonth;
		this.year = year;

		this.dob = Utility.toSqlDate(cal);

		SharedPreferences.Editor editor = getAppSettings().edit();
		editor.putString(SETUP_DIALOG_DOB, this.dob);
		editor.commit();
	}

	@Override
	public boolean onKey(View view, int id, KeyEvent keyEvent) {
		if (!getReady())
			return false;

		updateDoneBtn();
		return false;
	}

	public boolean onSetupDoneClick(View v) {
		if (!getReady())
			return false;

		Utility.LogMethod();

		this.name = this.editName.getText().toString();
		if (!Utility.IsValid(name)) {
			Utility.showToast(getActivity().getString(R.string.enter_baby_name));
			return false;
		}

		this.dob = this.editDOB.getText().toString();
		if (!Utility.IsValid(this.dob) || (!Utility.IsValidDate(this.dob) && !Utility.IsValidSqlDate(this.dob))) {
			Utility.showToast(getActivity().getString(R.string.enter_birthday));
			return false;
		}

		this.userid = this.editEmail.getText().toString();

		try {
			getActivityEx().registerChild(this.name, this.dob, this.userid, LogoImage.lookupPhotoUrl());
		} catch (Exception ex) {
			Utility.LogError(ex.toString());
			Utility.showToast(getActivity().getString(R.string.setup_failed));
		}

		saveToSettings();

		return true;
	}

	private boolean updateDoneBtn() {
		if (!getReady())
			return false;

		if (this.editName != null && this.editDOB != null && this.DoneBtn != null) {
			boolean enabled = Utility.IsValid(this.editName.getText().toString())
					&& Utility.IsValid(this.editDOB.getText().toString()) ? true : false;
			this.DoneBtn.setEnabled(enabled);
		}

		return false;
	}

	private void updateCurEdit() {
		if (!getReady())
			return;

		if (this.editName.hasFocus()) {
			this.curEdit = SetupDialogEdits.Name;
		} else if (this.editDOB.hasFocus()) {
			// never set focus to DOB
			this.curEdit = SetupDialogEdits.Email;
		} else if (this.editEmail.hasFocus()) {
			this.curEdit = SetupDialogEdits.Email;
		}
	}

	private void updateFocusFromCurEdit() {
		if (!getReady())
			return;

		switch (this.curEdit) {
		default:
		case Name:
			if (this.editName != null)
				this.editName.requestFocus();
			break;
		case DOB:
			// never set focus to DOB
		case Email:
			if (this.editEmail != null)
				this.editEmail.requestFocus();
			break;
		}
	}

	@Override
	public void saveToSettings(SharedPreferences.Editor editor) {
		super.saveToSettings(editor);

		if (!canSave())
			return;

		Utility.LogMethod();

		updateCurEdit();

		this.name = this.editName.getText().toString();
		this.dob = this.editDOB.getText().toString();
		this.userid = this.editEmail.getText().toString();

		editor.putString(SETUP_DIALOG_NAME, this.name);
		editor.putString(SETUP_DIALOG_DOB, this.dob);
		editor.putString(SETUP_DIALOG_USERID, this.userid);
		editor.putInt(SETUP_DIALOG_CUR_EDIT, this.curEdit.ordinal());
	}

	@Override
	public void restoreFromSettings() {
		if (!getReady())
			return;

		super.restoreFromSettings();

		Utility.LogMethod();

		this.name = getAppSettings().getString(SETUP_DIALOG_NAME, this.name);
		this.dob = getAppSettings().getString(SETUP_DIALOG_DOB, this.dob);
		this.userid = getAppSettings().getString(SETUP_DIALOG_USERID, this.userid);
		this.curEdit = SetupDialogEdits.values()[getAppSettings().getInt(SETUP_DIALOG_CUR_EDIT,
				SetupDialogEdits.Name.ordinal())];

		if (this.editName != null) {
			this.editName.setText(this.name);
		}

		if (this.editDOB != null) {
			this.editDOB.setText(this.dob);
		}

		if (this.editEmail != null) {
			this.editEmail.setText(this.userid);
		}
		updateFocusFromCurEdit();
		updateDoneBtn();

		this.logo.onResume();
	}

} // SetupDialog

