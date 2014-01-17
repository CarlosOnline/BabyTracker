package com.joyofplaying.babytracker;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class CalendarDialog extends DialogFragment implements OnDateChangedListener, OnClickListener {
	public interface OnCalendarChangedListener {
		public void onDateChanged(int year, int monthOfYear, int dayOfMonth);
	}

	private DatePicker datePicker = null;
	private OnCalendarChangedListener callBack = null;
	private int year;
	private int month;
	private int day;

	static CalendarDialog newInstance(BabyTrackerActivity activity, int stackLevel, int month, int day, int year,
			OnCalendarChangedListener callBack) {
		Utility.LogMethod();
		CalendarDialog f = new CalendarDialog();
		f.callBack = callBack;

		f.year = year;
		f.month = month;
		f.day = day;

		Bundle args = new Bundle();
		args.putInt("month", month);
		args.putInt("day", day);
		args.putInt("year", year);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Utility.LogMethod();
		getDialog().setTitle("Choose Date");
		View v = inflater.inflate(R.layout.date_picker, container, false);

		Bundle args = this.getArguments();
		this.datePicker = (DatePicker) v.findViewById(R.id.datePicker);
		this.datePicker.init(args.getInt("year"), args.getInt("month"), args.getInt("day"), this);

		View btn = v.findViewById(R.id.btnDone);
		btn.setOnClickListener(this);

		btn = v.findViewById(R.id.btnCancel);
		btn.setOnClickListener(this);

		return v;
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		this.year = year;
		this.month = monthOfYear;
		this.day = dayOfMonth;
	}

	@Override
	public void onClick(View view) {
		Utility.LogMethod();
		switch (view.getId()) {
		case R.id.btnDone:
			if (this.callBack != null) {
				this.callBack.onDateChanged(this.year, this.month, this.day);
			}
			dismiss();
			break;
		case R.id.btnCancel:
			dismiss();
			break;
		}
	}

} // CalendarDialog
