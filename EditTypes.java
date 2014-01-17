package com.joyofplaying.babytracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.joyofplaying.babytracker.CalendarDialog.OnCalendarChangedListener;
import com.joyofplaying.babytracker.Constants.Anim;
import com.joyofplaying.babytracker.Constants.DataTypes;
import com.joyofplaying.babytracker.Constants.OnEditChildData;
import com.joyofplaying.babytracker.Constants.OnTypeButtonClick;
import com.joyofplaying.babytracker.Constants.TextTypes;
import com.joyofplaying.babytracker.Data.ChildData;
import com.joyofplaying.babytracker.Utility.Formats;

public class EditTypes extends BaseHelper implements OnFocusChangeListener, OnTypeButtonClick, OnEditChildData,
		OnCalendarChangedListener {

	private final EditText[] editViews = new EditText[TextTypes.values().length];
	ButtonTypes types = null;
	DataGrid dataGrid = null;

	private EditText editDate = null;
	private EditText editTime = null;
	private EditText editAmount = null;
	private EditText editNote = null;
	private Switch switchAMPM = null;
	private Switch switchMin = null;
	private Switch switchOZML = null;

	private Button btnLeft = null;
	private Button btnRight = null;
	private Button btnBack = null;
	private Button btnSubmit = null;
	private Button btnCancel = null;

	private final Button btnPad[] = new Button[10];
	private final Button btnSymbols[] = new Button[5];
	private EditText edit = null;
	private TextTypes editType = TextTypes.Date;
	private final int enabledTextColor;
	private final int disabledTextColor;
	private int month;
	private int day;
	private int year;
	private String default_year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
	private ChildData updateData = null;
	private boolean useDatePicker = true;
	private final OnClickListener numberPadOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onNumberPadBtnClick(v);
		}
	};
	private final OnClickListener editTextOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onEditTextClick(v);
		}
	};
	private final OnClickListener backBtnOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onBackBtnClick(v);
		}
	};
	private final OnClickListener submitBtnOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onSubmitBtnClick(v);
		}
	};
	private final OnClickListener cancelBtnOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			clearUpdateData();
		}
	};

	EditTypes(BaseFragment fragment, View parent, ButtonTypes types) {
		super(fragment);
		this.types = types;
		this.dataGrid = new DataGrid(getFragment(), parent, Constants.MaxDataEntryRows, false);
		this.dataGrid.setHandler(this);
		// this.dataGrid.setReversed(true);
		this.dataGrid.setSelectedChild(getApp().getPendingEditChild());

		this.editDate = (EditText) parent.findViewById(R.id.editDate);
		setupEditText(this.editDate, TextTypes.Date);

		this.editTime = (EditText) parent.findViewById(R.id.editTime);
		setupEditText(this.editTime, TextTypes.Time);

		this.editAmount = (EditText) parent.findViewById(R.id.editAmount);
		setupEditText(this.editAmount, TextTypes.Amount);

		this.editNote = (EditText) parent.findViewById(R.id.editNote);
		setupEditText(this.editNote, TextTypes.Note);

		this.edit = this.editDate;

		this.switchAMPM = (Switch) parent.findViewById(R.id.switchAMPM);
		this.switchOZML = (Switch) parent.findViewById(R.id.switchOZML);
		this.switchMin = (Switch) parent.findViewById(R.id.switchMin);

		int idx = 0;
		this.btnLeft = (Button) parent.findViewById(R.id.btnLeft);
		this.btnLeft.setTag("btnLeft");
		this.btnLeft.setOnClickListener(this.numberPadOnClick);
		this.btnSymbols[idx++] = this.btnLeft;

		this.btnRight = (Button) parent.findViewById(R.id.btnRight);
		this.btnRight.setOnClickListener(this.numberPadOnClick);
		this.btnRight.setTag("btnRight");
		this.btnSymbols[idx++] = this.btnRight;

		this.btnBack = (Button) parent.findViewById(R.id.btnBack);
		this.btnBack.setOnClickListener(this.backBtnOnClick);
		this.btnBack.setTag("btnBack");
		this.btnSymbols[idx++] = this.btnBack;

		this.btnSubmit = (Button) parent.findViewById(R.id.btnSubmit);
		this.btnSubmit.setOnClickListener(this.submitBtnOnClick);
		this.btnSubmit.setTag("btnSubmit");
		this.btnSymbols[idx++] = this.btnSubmit;

		this.btnCancel = (Button) parent.findViewById(R.id.btnCancel);
		this.btnCancel.setOnClickListener(this.cancelBtnOnClick);
		this.btnCancel.setTag("Cancel");
		this.btnSymbols[idx++] = this.btnCancel;

		idx = 0;
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn0);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn1);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn2);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn3);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn4);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn5);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn6);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn7);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn8);
		this.btnPad[idx++] = (Button) parent.findViewById(R.id.btn9);

		idx = 0;
		for (Button btn : this.btnPad) {
			btn.setOnClickListener(this.numberPadOnClick);
			btn.setTag(Integer.toString(idx));
			idx++;
		}

		updateButtonPadding();

		ColorStateList mList = this.btnSubmit.getTextColors();
		this.enabledTextColor = mList.getDefaultColor();
		this.disabledTextColor = getColor(R.color.btn_disabled_text);

		String value = getAppSettings().getString("ButtonTypes_type", DataTypes.Nurse.toString());
		DataTypes type = DataTypes.valueOf(value);
		this.types.setType(type);
		updateViewsFromType();

		this.updateData = getApp().getPendingEditChild();
	}

	private void setupEditText(EditText view, TextTypes type) {
		view.setOnFocusChangeListener(this);
		view.setOnClickListener(this.editTextOnClick);
		view.setTag(type);
		this.editViews[type.ordinal()] = view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!getReady())
			return;

		updateViewsFromTextState();
		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		if (!getActive())
			return;

		LogMethod();
		this.dataGrid.onActivate();
		this.dataGrid.display();
	}

	private void putType(TextTypes type) {
		this.editType = type;
	}

	private DataTypes getDataType() {
		return this.types.getType();
	}

	private View getEditText(TextTypes type) {
		return this.editViews[type.ordinal()];
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
	public void onFocusChange(View view, boolean hasFocus) {
		if (!getReady())
			return;

		// LogMethod("hasFocus: {0} {1}", hasFocus, getDebugInfo(view));
		if (hasFocus) {
			setCurrentButton(view);
		}
	}

	public void onEditTextClick(View view) {
		if (!getReady())
			return;

		// LogMethod("{0}", getDebugInfo(view));
		setCurrentButton(view);

		if (this.edit != null) {
			this.edit.requestFocus();

			switch (this.editType) {
			case Date:
				if (this.useDatePicker) {
					int month = getMonth();
					int day = getDay();
					int year = getYear();
					if (month == 0 || day == 0 || year == 0) {
						Calendar cal = Calendar.getInstance();
						month = cal.get(Calendar.MONTH) + 1;
						day = cal.get(Calendar.DAY_OF_MONTH);
						year = cal.get(Calendar.YEAR);
					}
					ShowDialog.showDatePickerDialog(getFragment(), month, day, year, this);
				}
				break;

			case Time:
				break;

			case Amount:
				break;

			case Note:
				this.editNote.setSelection(this.editNote.getText().length());
				break;
			}
		}
	}

	private boolean onDateAdd(String newCharacter) {
		if (!getReady())
			return false;

		// LogMethod("{0}", getDebugInfo(null));

		String regEx3 = "^(0?[1-9]|1[012])[/](0?[1-9]|[12][0-9]|3[01])[/](19|20)?(\\d\\d?)";
		String regEx2 = "^(0?[1-9]|1[012])[/](0?[1-9]|[12][0-9]|3[01])[/]?";
		String regEx1 = "^(0?[1-9]|1[012])[/]?";

		String cur = this.edit.getText().toString();
		String next = cur + newCharacter;

		Matcher matcher = Pattern.compile(regEx3).matcher(next);
		if (!matcher.matches())
			matcher = Pattern.compile(regEx2).matcher(next);
		if (!matcher.matches())
			matcher = Pattern.compile(regEx1).matcher(next);
		if (!matcher.matches()) {
			// append / in case user not using it
			next = cur + "/" + newCharacter;
			if (!matcher.matches())
				matcher = Pattern.compile(regEx3).matcher(next);
			if (!matcher.matches())
				matcher = Pattern.compile(regEx2).matcher(next);
			if (!matcher.matches())
				matcher = Pattern.compile(regEx1).matcher(next);
		}

		if (matcher.matches()) {
			Utility.Log("valid date groupCount={1}", matcher.groupCount());
			int count = matcher.groupCount();
			switch (count) {
			case 4:
			case 3:
				// no year entry
				return false;

			case 2:
				// Month, Day
				// 1/2
				this.editDate.setText(next);
				return true;

			case 1:
			case 0:
				// none, or 1-9
				this.editDate.setText(next);
				// this.validDate = false;
				return true;
			}
		}

		// this.validDate = Utility.IsValidDate(cur);
		// Utility.Log("bad date pattern validDate={0}", this.validDate);
		return false;
	}

	private String getTag(View view) {
		if (view != null) {
			Object tag = view.getTag();
			if (tag != null) {
				if (tag instanceof String)
					return (String) tag;
				else if (tag instanceof TextTypes)
					return ((TextTypes) tag).toString();
			}
		}
		return "null";
	}

	@SuppressWarnings("unused")
	private String getDebugInfo(View view) {
		if (Constants.ReleaseMode)
			return "";

		String curTag = getTag(this.edit);
		String curFocus = this.edit.hasFocus() ? "true" : "false";

		String nextType = getTag(view);
		String nextFocus = view.hasFocus() ? "true" : "false";

		return String.format("  ****  cur { view:%s focus:%s } - next { view:%s  focus:%s }", curTag, curFocus,
				nextType, nextFocus);
	}

	private boolean onTimeAdd(String newCharacter) {
		if (!getReady())
			return false;

		// LogMethod("{0}", getDebugInfo(null));

		String regEx2 = "^(1[012]|[1-9]):([0-5])([0-9])?";
		String regEx1 = "^(1[012]|[1-9]):?";

		String cur = this.edit.getText().toString();
		String next = cur + newCharacter;

		Matcher matcher = Pattern.compile(regEx2).matcher(next);
		if (!matcher.matches())
			matcher = Pattern.compile(regEx1).matcher(next);
		if (!matcher.matches()) {
			// append / in case user not using it
			next = cur + ":" + newCharacter;
			if (!matcher.matches())
				matcher = Pattern.compile(regEx2).matcher(next);
			if (!matcher.matches())
				matcher = Pattern.compile(regEx1).matcher(next);
		}

		if (matcher.matches()) {
			Utility.Log("valid time groupCount={0}", matcher.groupCount());
			int count = matcher.groupCount();
			switch (count) {
			case 3:
			case 2:
				// hour, minute
				this.editTime.setText(next);
				return true;

			case 1:
			case 0:
				this.editTime.setText(next);
				// this.validTime = false;
				return true;
			}
		}

		// this.validTime = Utility.IsValidTime(cur);
		// Utility.Log("bad time pattern validTime={0}", this.validTime);
		return false;
	}

	public void onNumberPadBtnClick(View view) {
		if (!getReady())
			return;

		Button btn = (Button) view;
		String add = btn.getText().toString();
		LogMethod("adding {0} {1}", add, getDebugInfo(view));

		// setCurrentButton(this.getEditText(this.type));

		switch (this.editType) {
		case Date:
			if (!onDateAdd(add)) {
				moveToNextEdit();
				setCurrentButton(this.editTime);
				onNumberPadBtnClick(view);
				return;
			}
			break;

		case Time:
			if (!onTimeAdd(add)) {
				moveToNextEdit();
				onNumberPadBtnClick(view);
				return;
			}
			break;

		case Amount:
			if (getDataType() == DataTypes.Wet || getDataType() == DataTypes.Poop) {
				setCurrentButton(this.editNote);
			}
			if (!this.edit.hasFocus()) {
				setCurrentButton(this.edit);
			}
			// fallthrough

		default:
			String cur = this.edit.getText().toString();
			this.edit.setText(cur + add);
			break;
		}

		updateViewsFromTextState();
	}

	public void onBackBtnClick(View view) {
		if (!getReady())
			return;

		LogMethod("{0}", getDebugInfo(view));

		String cur = this.edit.getText().toString();
		if (cur.length() == 0) {
			moveToPreviousEdit();
			cur = this.edit.getText().toString();
		}

		LogMethod("truncating {0}", getDebugInfo(view));

		if (cur.length() > 0) {
			String next = cur.substring(0, cur.length() - 1);
			this.edit.setText(next);
		}

		updateViewsFromTextState();
	}

	public void onSubmitBtnClick(View view) {
		if (!getReady())
			return;

		// LogMethod("{0}", getDebugInfo(view));

		if (!getSettings().getRegistered()) {
			saveToSettings();
			ShowDialog.showSetupDialog(getFragment());

			if (!getSettings().getRegistered()) {
				return;
			}
		}

		ChildData data = new ChildData();
		data.date = this.editDate.getText().toString();
		data.time = this.editTime.getText().toString();
		data.ampm = getString(this.switchAMPM.isChecked() ? R.string.pm : R.string.am);
		data.type = getDataType().ordinal();
		switch (getDataType()) {
		case Wet:
		case Poop:
			data.amount = 0;
			data.ozml = "";
			break;
		case Nurse:
			data.amount = Double.parseDouble(this.editAmount.getText().toString());
			data.ozml = getString(R.string.min);
			break;
		case Bottle:
		case Pump:
			data.amount = Double.parseDouble(this.editAmount.getText().toString());
			data.ozml = getString(this.switchOZML.isChecked() ? R.string.ml : R.string.oz);
			break;
		}
		data.description = this.editNote.getText().toString();

		if (Utility.countOf(data.date, "/") == 1) {
			data.date += "/" + this.default_year;
		}

		boolean editMode = this.updateData != null ? true : false;
		if (!editMode) {
			data = getActivity().submitData(data);
		} else {
			data.id = this.updateData.id;
			data = getActivity().updateData(data);
		}

		if (data != null) {
			if (!editMode) {
				this.dataGrid.onAddChild(data, true);
			} else {
				this.dataGrid.onUpdateChild(data);
			}

			if (!getSettings().getDebugMode()) {
				this.editTime.setText("");
				this.editAmount.setText("");
				this.editNote.setText("");
			}

			this.editType = getAppSettings().getBoolean("select_date_after_submit", false) ? TextTypes.Date
					: TextTypes.Time;
			setCurrentButton(this.getEditText(this.editType));
		} else {
			if (editMode)
				Utility.Error(getString(R.string.failed_to_edit_record));
			else
				Utility.Error(getString(R.string.failed_to_add_record));
		}
		clearUpdateData();
	}

	@Override
	@SuppressLint("SimpleDateFormat")
	public void onDateChanged(int year, int monthOfYear, int dayOfMonth) {
		if (!getReady())
			return;

		LogMethod();
		final Calendar dat = Calendar.getInstance();
		dat.set(Calendar.YEAR, year);
		dat.set(Calendar.MONTH, monthOfYear);
		dat.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		this.month = monthOfYear;
		this.day = dayOfMonth;
		this.year = year;

		// cache current year for future use
		this.default_year = String.valueOf(this.year);

		SimpleDateFormat date = new SimpleDateFormat("M/d");
		String newDate = date.format(dat.getTime());
		this.editDate.setText(newDate);

		updateViewsFromTextState();
	}

	/*
	 * Other methods
	 */

	private void moveToPreviousEdit() {
		if (!getReady())
			return;

		LogMethod("from: {0}", getDebugInfo(this.edit));

		int idxType = Math.max(this.editType.ordinal() - 1, 0);
		this.putType(TextTypes.values()[idxType]);
		if (getDataType() == DataTypes.Wet || getDataType() == DataTypes.Poop) {
			if (this.editType == TextTypes.Amount) {
				moveToPreviousEdit();
				return;
			}
		}
		setCurrentButton(this.getEditText(this.editType));
	}

	private void moveToNextEdit() {
		if (!getReady())
			return;

		LogMethod("from: {0}", getDebugInfo(this.edit));

		int idxType = Math.min(this.editType.ordinal() + 1, TextTypes.Note.ordinal());
		this.putType(TextTypes.values()[idxType]);
		if (getDataType() == DataTypes.Wet || getDataType() == DataTypes.Poop) {
			if (this.editType == TextTypes.Amount) {
				moveToNextEdit();
				return;
			}
		}

		setCurrentButton(this.getEditText(this.editType));
	}

	private boolean setCurrentButton(View view) {
		if (!getReady())
			return false;

		LogMethod("{0}", getDebugInfo(view));
		if (this.edit == (EditText) view) {

			if (!this.edit.hasFocus()) {
				this.edit.setSelected(true);
				this.edit.requestFocus();
				// this.edit.requestFocusFromTouch();
			}
			return true;
		} else if (view != null) {
			view.setSelected(false);
		}

		this.edit = (EditText) view;
		// this.type = getType(this.edit);
		this.editType = (TextTypes) view.getTag();

		if (this.edit != null) {
			this.edit.setSelected(true);

			if (!this.edit.hasFocus()) {
				this.edit.requestFocus();
				this.edit.requestFocusFromTouch();
			}

			switch (this.editType) {
			case Date:
				this.btnLeft.setText("/");
				this.btnLeft.setVisibility(View.VISIBLE);
				this.btnLeft.setEnabled(true);
				this.btnRight.setVisibility(View.INVISIBLE);
				break;

			case Time:
				this.btnLeft.setText(":");
				this.btnLeft.setVisibility(View.VISIBLE);
				this.btnRight.setVisibility(View.INVISIBLE);
				break;

			case Amount:
				this.btnLeft.setText(".");
				this.btnLeft.setVisibility(View.VISIBLE);
				this.btnRight.setVisibility(View.INVISIBLE);
				break;

			case Note:
				this.btnLeft.setVisibility(View.INVISIBLE);
				this.btnRight.setVisibility(View.INVISIBLE);
				this.editNote.setSelection(this.editNote.getText().length());
				break;
			}

			updateViewsFromTextState();
		}

		// Utility.Log("Changed to [{0}] Button", this.type.toString());
		return true;
	}

	private void updatePadForDate() {
		if (!getReady())
			return;

		String time = this.editDate.getText().toString();
		int len = time.length();
		byte bytes[] = time.getBytes();
		char last = (len > 0) ? (char) bytes[len - 1] : 0;

		this.btnPad[0].setEnabled(len != 0);
		this.btnPad[1].setEnabled(true);
		this.btnPad[2].setEnabled(true);
		this.btnPad[3].setEnabled(true);
		this.btnPad[4].setEnabled(true);
		this.btnPad[5].setEnabled(true);
		this.btnPad[6].setEnabled(true);
		this.btnPad[7].setEnabled(true);
		this.btnPad[8].setEnabled(true);
		this.btnPad[9].setEnabled(true);

		switch (len) {
		case 1:
			// 1 case or 1-9 case
			this.btnLeft.setEnabled(true);
			break;

		case 2:
			// 10,11,12, or 2/: case
			this.btnLeft.setEnabled(last != '/');
			break;

		case 0:
		case 3:
		case 4:
		case 5:
			// 11/ or 1/4 case
			// 11/5 or 1/25 case
			// 11/14 case
			this.btnLeft.setEnabled(false);
			break;
		}

	}

	private void updatePadForTime() {
		if (!getReady())
			return;

		String time = this.editTime.getText().toString();
		int len = time.length();
		char last = (len > 0) ? (char) time.getBytes()[len - 1] : 0;

		switch (len) {
		case 0:
			this.btnLeft.setEnabled(false);
			this.btnPad[0].setEnabled(true);
			this.btnPad[1].setEnabled(true);
			this.btnPad[2].setEnabled(true);
			this.btnPad[3].setEnabled(true);
			this.btnPad[4].setEnabled(true);
			this.btnPad[5].setEnabled(true);
			this.btnPad[6].setEnabled(true);
			this.btnPad[7].setEnabled(true);
			this.btnPad[8].setEnabled(true);
			this.btnPad[9].setEnabled(true);
			break;

		case 1:
			// 1 or 2-9 case, first digits of time
			this.btnLeft.setEnabled(true);
			if (last >= '2' && last <= '9') {
				this.btnPad[0].setEnabled(true);
				this.btnPad[1].setEnabled(true);
				this.btnPad[2].setEnabled(true);
				this.btnPad[3].setEnabled(true);
				this.btnPad[4].setEnabled(true);
				this.btnPad[5].setEnabled(true);
				this.btnPad[6].setEnabled(false);
				this.btnPad[7].setEnabled(false);
				this.btnPad[8].setEnabled(false);
				this.btnPad[9].setEnabled(false);
			} else if (last == '1') {
				this.btnPad[0].setEnabled(true);
				this.btnPad[1].setEnabled(true);
				this.btnPad[2].setEnabled(true);
				this.btnPad[3].setEnabled(false);
				this.btnPad[4].setEnabled(false);
				this.btnPad[5].setEnabled(false);
				this.btnPad[6].setEnabled(false);
				this.btnPad[7].setEnabled(false);
				this.btnPad[8].setEnabled(false);
				this.btnPad[9].setEnabled(false);
			}
			break;

		case 2:
			// 10,11,12, or 2: case
			if (last >= '0' && last <= '2') {
				// 10,11,12 case
				this.btnLeft.setEnabled(true);
				this.btnPad[0].setEnabled(true);
				this.btnPad[1].setEnabled(true);
				this.btnPad[2].setEnabled(true);
				this.btnPad[3].setEnabled(true);
				this.btnPad[4].setEnabled(true);
				this.btnPad[5].setEnabled(true);
				this.btnPad[6].setEnabled(false);
				this.btnPad[7].setEnabled(false);
				this.btnPad[8].setEnabled(false);
				this.btnPad[9].setEnabled(false);
			} else if (last == ':') {
				// 2: case
				this.btnLeft.setEnabled(false);
				this.btnPad[0].setEnabled(true);
				this.btnPad[1].setEnabled(true);
				this.btnPad[2].setEnabled(true);
				this.btnPad[3].setEnabled(true);
				this.btnPad[4].setEnabled(true);
				this.btnPad[5].setEnabled(true);
				this.btnPad[6].setEnabled(false);
				this.btnPad[7].setEnabled(false);
				this.btnPad[8].setEnabled(false);
				this.btnPad[9].setEnabled(false);
			}
			break;

		case 3:
			// 11: or 1:4 case
			this.btnLeft.setEnabled(false);
			if (last >= '0' && last <= '5') {
				// 1:4 case
				this.btnPad[0].setEnabled(true);
				this.btnPad[1].setEnabled(true);
				this.btnPad[2].setEnabled(true);
				this.btnPad[3].setEnabled(true);
				this.btnPad[4].setEnabled(true);
				this.btnPad[5].setEnabled(true);
				this.btnPad[6].setEnabled(true);
				this.btnPad[7].setEnabled(true);
				this.btnPad[8].setEnabled(true);
				this.btnPad[9].setEnabled(true);
			} else if (last == ':') {
				// 12: case
				this.btnPad[0].setEnabled(true);
				this.btnPad[1].setEnabled(true);
				this.btnPad[2].setEnabled(true);
				this.btnPad[3].setEnabled(true);
				this.btnPad[4].setEnabled(true);
				this.btnPad[5].setEnabled(true);
				this.btnPad[6].setEnabled(false);
				this.btnPad[7].setEnabled(false);
				this.btnPad[8].setEnabled(false);
				this.btnPad[9].setEnabled(false);
			}
			break;

		case 4:
		case 5:
			// 11:5 or 1:45 case
			// 11:54 case
			this.btnLeft.setEnabled(time.indexOf(":") != -1 ? false : true);
			this.btnPad[0].setEnabled(true);
			this.btnPad[1].setEnabled(true);
			this.btnPad[2].setEnabled(true);
			this.btnPad[3].setEnabled(true);
			this.btnPad[4].setEnabled(true);
			this.btnPad[5].setEnabled(true);
			this.btnPad[6].setEnabled(true);
			this.btnPad[7].setEnabled(true);
			this.btnPad[8].setEnabled(true);
			this.btnPad[9].setEnabled(true);
			break;
		}
	}

	private void clearUpdateData() {
		this.updateData = null;
		getApp().setPendingEditChild(null);
		updateViewsFromTextState();
	}

	private void updatePadforAmount() {
		if (!getReady())
			return;

		String amount = this.editAmount.getText().toString();
		this.btnLeft.setEnabled(amount.length() == 0 || amount.contains(".") ? false : true);
		this.btnPad[0].setEnabled(amount.length() == 0 ? false : true);
		this.btnPad[1].setEnabled(true);
		this.btnPad[2].setEnabled(true);
		this.btnPad[3].setEnabled(true);
		this.btnPad[4].setEnabled(true);
		this.btnPad[5].setEnabled(true);
		this.btnPad[6].setEnabled(true);
		this.btnPad[7].setEnabled(true);
		this.btnPad[8].setEnabled(true);
		this.btnPad[9].setEnabled(true);
	}

	private void updateViewsFromTextState() {
		if (!getReady())
			return;

		boolean editMode = this.updateData != null ? true : false;
		// LogMethod("{0}", getDebugInfo(null));

		boolean validDate = Utility.IsValidDate(this.editDate.getText().toString());
		boolean validTime = Utility.IsValidTime(this.editTime.getText().toString());

		this.btnBack.setEnabled(this.edit.length() > 0 || this.edit != this.editDate ? true : false);
		this.btnSubmit
				.setEnabled(validDate
						&& validTime
						&& (this.editAmount.getText().length() > 0 || getDataType() == DataTypes.Wet || getDataType() == DataTypes.Poop) ? true
						: false);
		this.btnCancel.setEnabled(editMode);
		this.btnCancel.setVisibility(editMode ? View.VISIBLE : View.INVISIBLE);

		switch (this.editType) {
		case Date:
			updatePadForDate();
			dismissKeyboard();
			break;

		case Time:
			updatePadForTime();
			dismissKeyboard();
			break;

		case Amount:
			updatePadforAmount();
			dismissKeyboard();
			break;

		case Note:
			break;
		}

		updateButtonColors();
	}

	private void dismissKeyboard() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.edit.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(this.editNote.getWindowToken(), 0);
	}

	private void updateButtonColors() {
		for (Button btn : this.btnPad) {
			btn.setTextColor(btn.isEnabled() ? this.enabledTextColor : this.disabledTextColor);
		}
		for (Button btn : this.btnSymbols) {
			btn.setTextColor(btn.isEnabled() ? this.enabledTextColor : this.disabledTextColor);
		}
	}

	private void updateButtonPadding() {
		for (Button btn : this.btnPad) {
			btn.setPadding(0, 0, 0, 0);
		}
		for (Button btn : this.btnSymbols) {
			btn.setPadding(0, 0, 0, 0);
		}
	}

	@Override
	public void onTypeButtonClick(DataTypes type) {
		if (!getReady())
			return;

		updateViewsFromType();
		updateViewsFromTextState();
	}

	private void updateViewsFromType() {
		if (!getReady())
			return;

		LogMethod();

		switch (getDataType()) {
		case Nurse:
			showAmountRow(true);
			showAmountSwitch(false);
			break;

		case Pump:
		case Bottle:
			showAmountRow(true);
			showAmountSwitch(true);
			break;

		case Wet:
		case Poop:
			showAmountRow(false);
			break;
		}
	}

	private void showAmountRow(boolean show) {
		if (show) {
			if (this.editAmount.getVisibility() != View.VISIBLE) {
				Animation anim = AnimationUtils.loadAnimation(getAppContext(), android.R.anim.fade_in);
				anim.setDuration(Anim.AddAmountRow);
				this.editAmount.startAnimation(anim);
				this.editAmount.setVisibility(View.VISIBLE);
			}

		} else {
			if (this.editType == TextTypes.Amount) {
				// Setup state with prev as time and next as Note
				this.editType = TextTypes.Time;
				setCurrentButton(this.getEditText(TextTypes.Note));
			}

			if (this.editAmount.getVisibility() != View.GONE) {
				Animation anim = AnimationUtils.loadAnimation(getAppContext(), android.R.anim.fade_out);
				anim.setDuration(Anim.RemoveAmountRow);
				this.editAmount.startAnimation(anim);
				this.editAmount.setSelected(false);
				this.editAmount.setVisibility(View.GONE);
				this.switchOZML.setVisibility(View.GONE);
				this.switchMin.setVisibility(View.GONE);
			}
		}

	}

	@SuppressWarnings("unused")
	private void setAmountSwitch(int idOn, int idOff) {
		if (false)
			return;

		if (!getReady())
			return;

		Animation anim = AnimationUtils.loadAnimation(getAppContext(), android.R.anim.fade_in);
		anim.setDuration(Anim.AmountSwitch);
		this.switchOZML.startAnimation(anim);
		this.switchOZML.setTextOn(getActivity().getResources().getString(idOn));
		this.switchOZML.setTextOff(getActivity().getResources().getString(idOff));
		Utility.LogMethod("Set on {0} off: {1}", getActivity().getResources().getString(idOn), getActivity()
				.getResources().getString(idOff));
	}

	private void showAmountSwitch(boolean amount) {
		if (amount) {
			if (this.switchOZML.getVisibility() != View.VISIBLE) {
				this.switchMin.setVisibility(View.GONE);
				this.switchOZML.setVisibility(View.VISIBLE);
			}
		} else {
			if (this.switchMin.getVisibility() != View.VISIBLE) {
				this.switchMin.setChecked(this.switchOZML.isChecked());
				if (this.switchOZML.getWidth() > 0) {
					this.switchMin.setWidth(this.switchOZML.getWidth() - this.switchOZML.getPaddingLeft()
							- this.switchOZML.getPaddingRight());
				}
				this.switchOZML.setVisibility(View.GONE);
				this.switchMin.setVisibility(View.VISIBLE);
			}
		}
	}

	private void setSwitchState(Switch switchView, String state) {
		if (!getReady())
			return;
		boolean isChecked = switchView.getTextOn().toString().compareTo(state) == 0;
		switchView.setChecked(isChecked);
	}

	public void updateDataFromChild(ChildData data) {
		if (!getReady())
			return;

		LogMethod();
		if (data != null) {
			Date date = Utility.toDate(data.date);
			Date time = Utility.toDate(data.time);
			this.editDate.setText(Utility.toString(date, Formats.DateShort));
			this.editTime.setText(Utility.toString(time, Formats.Time));
			this.setSwitchState(this.switchAMPM, data.ampm);
			this.editAmount.setText(Utility.AmountToString(data.amount));
			if (data.type == DataTypes.Pump.ordinal() || data.type == DataTypes.Bottle.ordinal()) {
				this.setSwitchState(this.switchOZML, data.ozml);
			}
			this.editNote.setText(data.description);
			this.types.onTypeBtnClick(DataTypes.values()[data.type]);
			setCurrentButton(getEditText(this.editType != null ? this.editType : TextTypes.Date));
		}

		updateViewsFromTextState();
	}

	@Override
	public void onEditChild(ChildData data) {
		if (!getReady())
			return;

		LogMethod("{0}", data.toString());
		this.updateData = data;
		getApp().setPendingEditChild(data);
		updateDataFromChild(data);
	}

	@Override
	public boolean onDeleteChild(ChildData data) {
		if (!getReady())
			return false;

		LogMethod("{0}", data.toString());
		if (this.updateData == data) {
			clearUpdateData();
		}
		return getActivity().deleteData(data);
	}

	@Override
	public void onUnEditChild(ChildData data) {
		if (!getReady())
			return;

		LogMethod("{0}", data.toString());
		if (this.updateData == data) {
			clearUpdateData();
		}
	}

	@Override
	public void restoreFromSettings() {
		super.restoreFromSettings();
		if (!getReady())
			return;

		LogMethod();
		this.useDatePicker = getAppSettings().getBoolean("EditTypes_useDatePicker", true);
		if (this.dataGrid != null) {
			this.dataGrid.restoreFromSettings();
		}

		// Don't override onEditChild data
		if (this.updateData != null) {
			this.onEditChild(this.updateData);
			return;
		}

		final Calendar cal = Calendar.getInstance();
		String today = Utility.toUIDateShort(cal);
		this.editDate.setText(getAppSettings().getString("EditTypes_date", today));
		this.editTime.setText(getAppSettings().getString("EditTypes_time", ""));
		this.editAmount.setText(getAppSettings().getString("EditTypes_amount", ""));
		this.editNote.setText(getAppSettings().getString("EditTypes_note", ""));
		this.month = getAppSettings().getInt("EditTypes_month", cal.get(Calendar.MONTH));
		this.day = getAppSettings().getInt("EditTypes_day", cal.get(Calendar.DAY_OF_MONTH));
		this.year = getAppSettings().getInt("EditTypes_year", cal.get(Calendar.YEAR));
		this.default_year = getAppSettings().getString("EditTypes_default_year", this.default_year);
		this.switchAMPM.setChecked(getAppSettings().getBoolean("EditTypes_am_pm", true));
		this.switchOZML.setChecked(getAppSettings().getBoolean("EditTypes_oz_ml", true));

		this.editType = TextTypes.values()[getAppSettings().getInt("EditTypes_type", TextTypes.Date.ordinal())];
		setCurrentButton(getEditText(this.editType));

		updateViewsFromTextState();
		updateViewsFromType();
	}

	@Override
	public void saveToSettings(Editor editor) {
		super.saveToSettings(editor);
		if (!canSave())
			return;

		LogMethod();
		editor.putString("EditTypes_date", this.editDate.getText().toString());
		editor.putString("EditTypes_time", this.editTime.getText().toString());
		editor.putString("EditTypes_amount", this.editAmount.getText().toString());
		editor.putString("EditTypes_note", this.editNote.getText().toString());
		editor.putInt("EditTypes_type", this.editType.ordinal());
		editor.putInt("EditTypes_month", this.month);
		editor.putInt("EditTypes_day", this.day);
		editor.putInt("EditTypes_year", this.year);
		editor.putString("EditTypes_default_year", this.default_year);
		editor.putBoolean("EditTypes_useDatePicker", this.useDatePicker);
		editor.putBoolean("EditTypes_am_pm", this.switchAMPM.isChecked());
		editor.putBoolean("EditTypes_oz_ml", this.switchOZML.isChecked());

		if (this.dataGrid != null) {
			this.dataGrid.saveToSettings(editor);
		}
	}
} // EditTypes

