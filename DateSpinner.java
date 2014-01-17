package com.joyofplaying.babytracker;

import java.util.Date;

import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.joyofplaying.babytracker.CalendarDialog.OnCalendarChangedListener;
import com.joyofplaying.babytracker.Constants.DateTypes;
import com.joyofplaying.babytracker.Constants.OnDateSelectionChange;
import com.joyofplaying.babytracker.Constants.SpinDataTypes;
import com.joyofplaying.babytracker.LocalData.Query;
import com.joyofplaying.babytracker.Utility.Formats;

public class DateSpinner extends BaseHelper implements OnClickListener, OnItemSelectedListener,
		OnCalendarChangedListener {

	private Spinner dateSpinner = null;
	private Spinner typeSpinner = null;
	private String maxDate = Utility.toSqlDate();
	private String minDate = Utility.toSqlDate();
	private DateTypes dateType = DateTypes.SelectDate;
	private SpinDataTypes spinDataType = SpinDataTypes.All;
	private TextView textStartDate = null;
	private TextView textEndDate = null;
	private boolean dateSpinnerInitialized = false;
	private OnDateSelectionChange callback = null;
	private SpinDataTypes previousSpinDataType = null;
	private DateTypes previousDateType = null;
	private String previousMinDate = null;
	private String prevousMaxDate = null;

	public DateSpinner(BaseFragment fragment, View parent, OnDateSelectionChange callback) {
		super(fragment);

		this.callback = callback;

		this.minDate = getApp().getQuery().queryMinDate();
		this.maxDate = getApp().getQuery().queryMaxDate();

		this.dateSpinner = (Spinner) parent.findViewById(R.id.spinDates);
		this.dateSpinner.setOnItemSelectedListener(this);
		this.typeSpinner = (Spinner) parent.findViewById(R.id.spinTypes);
		if (this.typeSpinner != null) {
			this.typeSpinner.setOnItemSelectedListener(this);
		}
		this.textStartDate = (TextView) parent.findViewById(R.id.textStartDate);
		this.textStartDate.setOnClickListener(this);
		this.textEndDate = (TextView) parent.findViewById(R.id.textEndDate);
		this.textEndDate.setOnClickListener(this);
		this.textStartDate.setText(toDate(this.minDate));
		this.textEndDate.setText(toDate(this.maxDate));
	}

	private String toDate(String dateTime) {
		Date date = Utility.toDate(dateTime);
		return Utility.toString(date, Formats.Date);
	}

	private void onDateSelectionChange() {
		if (!getReady())
			return;

		if (true == compareToPreviousValues()) {
			Utility.Log("onDateSelectionChange deferred - no change");
			return;
		}

		this.textStartDate.setText(toDate(this.minDate));
		this.textEndDate.setText(toDate(this.maxDate));

		if (this.callback != null) {
			this.callback.onDateSelectionChange();
		}
	}

	public String getMinDate() {
		return this.minDate;
	}

	public String getMaxDate() {
		return this.maxDate;
	}

	public SpinDataTypes getSpinDataType() {
		return this.spinDataType;
	}

	private void cachePreviousValues() {
		if (!getReady())
			return;

		this.previousDateType = this.dateType;
		this.previousMinDate = this.minDate;
		this.prevousMaxDate = this.maxDate;
		this.previousSpinDataType = this.spinDataType;
	}

	private boolean compareToPreviousValues() {
		if (!getReady())
			return false;

		return this.previousDateType == this.dateType && this.previousMinDate == this.minDate
				&& this.prevousMaxDate == this.maxDate && this.previousSpinDataType == this.spinDataType;
	}

	private void clearPreviousValues() {
		if (!getReady())
			return;

		this.previousDateType = null;
		this.previousMinDate = "";
		this.prevousMaxDate = "";
		this.previousSpinDataType = null;
	}

	private void refreshDates() {
		if (!getReady())
			return;

		switch (dateType) {
		case Last24:
			Query query = getApp().getQuery();
			this.maxDate = query.queryNowDate();
			this.minDate = query.queryNowDate("-24 hour");
			break;

		case Today:
			this.minDate = Utility.toSqlDate();
			this.maxDate = this.minDate;
			return;

		case Yesterday:
			this.minDate = Utility.toSqlDate(-1);
			this.maxDate = this.minDate;
			return;

		case ThisWeek:
			this.minDate = Utility.toSqlDate(-7);
			this.maxDate = Utility.toSqlDate();
			return;

		case ThisMonth:
			this.minDate = Utility.toSqlDate(-30);
			this.maxDate = Utility.toSqlDate();
			return;

		case AllDates:
			this.minDate = getApp().getQuery().queryMinDate();
			this.maxDate = getApp().getQuery().queryMaxDate();
			return;
		}
	}

	private void handleDateType(DateTypes dateType) {
		if (!getReady())
			return;

		String date = "";
		cachePreviousValues();
		this.dateType = dateType;

		switch (dateType) {
		case Last24:
			this.maxDate = getApp().getQuery().queryNowDate();
			this.minDate = getApp().getQuery().queryNowDate("-24 hour");
			onDateSelectionChange();
			break;

		case Today:
			this.minDate = Utility.toSqlDate();
			this.maxDate = this.minDate;
			onDateSelectionChange();
			return;

		case Yesterday:
			this.minDate = Utility.toSqlDate(-1);
			this.maxDate = this.minDate;
			onDateSelectionChange();
			return;

		case ThisWeek:
			this.minDate = Utility.toSqlDate(-7);
			this.maxDate = Utility.toSqlDate();
			onDateSelectionChange();
			return;

		case ThisMonth:
			this.minDate = Utility.toSqlDate(-30);
			this.maxDate = Utility.toSqlDate();
			onDateSelectionChange();
			return;

		case AllDates:
			this.minDate = getApp().getQuery().queryMinDate();
			this.maxDate = getApp().getQuery().queryMaxDate();
			onDateSelectionChange();
			return;

		case SelectDate:
			date = this.minDate;
			this.maxDate = this.minDate;
			break;

		case StartDate:
			date = this.minDate;
			break;

		case EndDate:
			date = this.maxDate;
			break;
		}

		if (!Utility.IsValid(date)) {
			return;
		}

		try {
			Utility.Log("Parsing date: {0}", date);
			String[] dateOnly = date.split(" ");
			if (dateOnly.length > 1)
				date = dateOnly[0];
			String[] values = date.split("-");
			if (values.length != 3) {
				Utility.LogError("Unexpected date format: {0}", date);
			}

			int year = Integer.parseInt(values[0]);
			int month = Integer.parseInt(values[1]) - 1;
			int day = Integer.parseInt(values[2]);

			ShowDialog.showDatePickerDialog(getFragment(), month, day, year, this);
		} catch (Exception ex) {
			Utility.Error(getString(R.string.error_parsing_date), date.toString(), ex.getMessage());
		}
	}

	private void handleSpinDataType(SpinDataTypes type) {
		if (!getReady())
			return;

		cachePreviousValues();
		this.spinDataType = type;
		onDateSelectionChange();
	}

	@Override
	public void onDateChanged(int year, int monthOfYear, int dayOfMonth) {
		if (!getReady())
			return;

		LogMethod();
		cachePreviousValues();

		String date = Utility.toSqlDate(year, monthOfYear, dayOfMonth);
		switch (this.dateType) {
		case SelectDate:
			this.minDate = date;
			this.maxDate = date;
			break;
		case StartDate:
			this.minDate = date;
			break;

		case EndDate:
			this.maxDate = date;
			break;
		}

		setSpinnerPosition(this.dateSpinner, DateTypes.SelectDate.ordinal());
		onDateSelectionChange();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		if (!getReady())
			return;

		// LogMethod();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (!getReady())
			return;

		if (!this.dateSpinnerInitialized) {
			this.dateSpinnerInitialized = true;
			return;
		}

		LogMethod();

		if (parent == this.dateSpinner) {
			handleDateType(DateTypes.values()[position]);
		} else if (parent == this.typeSpinner) {
			handleSpinDataType(SpinDataTypes.values()[position]);
		}
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		LogMethod();

		if (view == this.textEndDate) {
			handleDateType(DateTypes.EndDate);
		} else if (view == this.textStartDate) {
			handleDateType(DateTypes.StartDate);
		}
	}

	private void setSpinnerPosition(Spinner spinner, int position) {
		if (!getReady())
			return;

		if (spinner.getSelectedItemPosition() != position) {
			spinner.setOnItemSelectedListener(null);
			spinner.setSelection(position, true);
			spinner.setOnItemSelectedListener(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!getReady())
			return;

		LogMethod();
		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		if (!getActive())
			return;

		LogMethod();

		// Refresh min & maxDates since they may have changed due to data
		// entries
		refreshDates();
		clearPreviousValues();
		onDateSelectionChange();
	}

	@Override
	public void restoreFromSettings() {
		super.restoreFromSettings();
		if (!getReady())
			return;

		LogMethod();
		cachePreviousValues();
		String tag = getName() + "_";

		this.minDate = getApp().getQuery().queryMinDate();
		this.maxDate = getApp().getQuery().queryMaxDate();

		this.dateType = DateTypes.values()[getAppSettings().getInt(tag + "DateSpinner_dateType", 0)];
		if (this.dateSpinner != null) {
			setSpinnerPosition(this.dateSpinner, getAppSettings().getInt(tag + "DateSpinner_dateSpinner_position", 0));
		}

		this.spinDataType = SpinDataTypes.values()[getAppSettings().getInt(tag + "DateSpinner_spinDataType", 0)];
		if (this.typeSpinner != null) {
			setSpinnerPosition(this.typeSpinner, getAppSettings().getInt(tag + "DateSpinner_typeSpinner_position", 0));
		}

		this.minDate = getAppSettings().getString(tag + "DateSpinner_minDate", this.minDate);
		if (!Utility.IsValid(this.minDate))
			this.minDate = Utility.toSqlDate();
		this.textStartDate.setText(toDate(this.minDate));

		this.maxDate = getAppSettings().getString(tag + "DateSpinner_maxDate", this.maxDate);
		if (!Utility.IsValid(this.maxDate))
			this.maxDate = Utility.toSqlDate();
		this.textEndDate.setText(toDate(this.maxDate));
	}

	@Override
	public void saveToSettings(Editor editor) {
		super.saveToSettings(editor);
		if (!canSave())
			return;

		LogMethod();
		String tag = getName() + "_";
		editor.putInt(tag + "DateSpinner_dateType", this.dateType.ordinal());
		editor.putInt(tag + "DateSpinner_spinDataType", this.spinDataType.ordinal());
		editor.putString(tag + "DateSpinner_minDate", this.minDate);
		editor.putString(tag + "DateSpinner_maxDate", this.maxDate);
		if (this.dateSpinner != null) {
			editor.putInt(tag + "DateSpinner_dateSpinner_position", this.dateSpinner.getSelectedItemPosition());
		}
		if (this.typeSpinner != null) {
			editor.putInt(tag + "DateSpinner_typeSpinner_position", this.typeSpinner.getSelectedItemPosition());
		}
	}
} // DateSpinner
