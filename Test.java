package com.joyofplaying.babytracker;

import java.util.Random;

import android.annotation.SuppressLint;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.joyofplaying.babytracker.Constants.DataTypes;
import com.joyofplaying.babytracker.Data.ChildData;

public class Test extends BaseHelper implements OnClickListener {
	private Random generator = null;

	@SuppressWarnings("unused")
	public Test(BaseFragment fragment, View view) {
		super(fragment);
		if (Constants.ReleaseMode)
			return;

		ViewGroup parent = (ViewGroup) view;

		this.generator = new Random();

		Button btn = (Button) parent.findViewById(R.id.btnTest1);
		btn.setVisibility(View.VISIBLE);
		btn.setOnClickListener(this);

		btn = (Button) parent.findViewById(R.id.btnTest2);
		btn.setVisibility(View.VISIBLE);
		btn.setOnClickListener(this);

		btn = (Button) parent.findViewById(R.id.btnDumpSettings);
		btn.setVisibility(View.VISIBLE);
		btn.setOnClickListener(this);

		btn = (Button) parent.findViewById(R.id.btnCrashToSendLogs);
		btn.setVisibility(View.VISIBLE);
		btn.setOnClickListener(this);
	}

	@SuppressWarnings("unused")
	@SuppressLint("DefaultLocale")
	private void registerChild() {
		if (Constants.ReleaseMode)
			return;

		String name = "Junior";
		String date = Utility.toSqlDate(-20);
		String dob = Utility.toUIDate(date);
		String userid = String.format("%s@parent.com", name);
		getActivity().registerChild(name, dob, userid, LogoImage.lookupPhotoUrl());
	}

	@SuppressWarnings("unused")
	@SuppressLint("DefaultLocale")
	private void addEntries(int startDateDelta, int dayCount, int perDayCount) {
		if (Constants.ReleaseMode)
			return;

		for (int idx = 0; idx < dayCount; idx++) {
			String date = Utility.toUIDate(Utility.toSqlDate(startDateDelta - idx));

			int reps = 10;
			for (; reps > 0; reps--) {

				int hour = generator.nextInt(12) + 1;
				int minute = generator.nextInt(59) + 1;
				String time = String.format("%d:%02d", hour, minute);
				boolean ampm = generator.nextInt(2) == 1 ? true : false;
				DataTypes type = DataTypes.values()[generator.nextInt(DataTypes.values().length)];
				double factor = generator.nextInt(10) == 1 ? 0.5 : 1.0;
				double amount = generator.nextInt(50) * factor;
				boolean ozml = generator.nextInt(2) == 1 ? true : false;
				String description = generator.nextInt(5) == 1 ? "abc" : "";

				ChildData data = createChildData(date, time, ampm, type, amount, ozml, description);
				Utility.Log("{0}-{1}: {2}", idx, reps, data.toStringFlat());
				submitChild(data);
			}
		}
	}

	@SuppressWarnings("unused")
	@SuppressLint("DefaultLocale")
	private void addEntries(int dayCount, int perDayCount) {
		if (Constants.ReleaseMode)
			return;

		int count = 1;
		for (int idx = dayCount; idx >= 0; idx--) {
			String date = Utility.toUIDate(Utility.toSqlDate(-(dayCount + idx)));
			for (int reps = 0; reps < perDayCount; reps++, count++) {
				int hour = 1;
				int minute = count;
				String time = String.format("%d:%02d", hour, minute);
				boolean ampm = true;
				DataTypes type = DataTypes.values()[generator.nextInt(DataTypes.values().length)];
				double factor = generator.nextInt(10) == 1 ? 0.5 : 1.0;
				double amount = generator.nextInt(50) * factor;
				boolean ozml = generator.nextInt(2) == 1 ? true : false;
				String description = String.format("#%d", count);

				ChildData data = createChildData(date, time, ampm, type, amount, ozml, description);
				Utility.Log("{0}-{1}: {2}", idx, reps, data.toStringFlat());
				submitChild(data);
			}
		}
	}

	public void updateChild() {
		if (Constants.ReleaseMode)
			return;

	}

	@SuppressWarnings("unused")
	private ChildData createChildData(String date, String time, boolean ampm, DataTypes type, Double amount,
			boolean ozml, String description) {
		if (Constants.ReleaseMode)
			return null;

		String ozmlStr = "";
		switch (type) {
		case Wet:
		case Poop:
			amount = 0.0;
			ozmlStr = getActivity().getString(R.string.oz);
			break;
		case Nurse:
			ozml = false;
			ozmlStr = getActivity().getString(R.string.min);
			break;
		case Bottle:
		case Pump:
			ozmlStr = getActivity().getString(ozml ? R.string.ml : R.string.oz);
			break;
		}
		ChildData data = new ChildData();
		data.date = date;
		data.time = time;
		data.ampm = getActivity().getString(ampm ? R.string.pm : R.string.am);
		data.type = type.ordinal();
		data.amount = amount;
		data.ozml = ozmlStr;
		data.description = description;

		return data;
	}

	@SuppressWarnings("unused")
	private ChildData submitChild(ChildData data) {
		if (Constants.ReleaseMode)
			return null;

		return getActivity().submitData(data);
	}

	@SuppressWarnings("unused")
	public void runTest1() {
		if (Constants.ReleaseMode)
			return;

		getActivity().resetBabyData();
		registerChild();
		addEntries(0, 12, 5);
		getActivity().reloadViews();
	}

	@SuppressWarnings("unused")
	public void runTest2() {
		if (Constants.ReleaseMode)
			return;

		getActivity().resetBabyData();
		registerChild();
		addEntries(0, 12, 5);
		getActivity().reloadViews();
	}

	@Override
	@SuppressWarnings("unused")
	public void onClick(View view) {
		if (Constants.ReleaseMode)
			return;

		switch (view.getId()) {
		case R.id.btnTest1:
			runTest1();
			break;
		case R.id.btnTest2:
			runTest2();
			break;
		case R.id.btnDumpSettings:
			getActivity().saveToSettings();
			getActivity().dumpSettings();
			break;
		case R.id.btnDumpDatabase:
			getApp().getDatabase().dumpDatabase();
			break;
		case R.id.btnCrashToSendLogs:
			view.getTag().notify(); // should be null and crash
			break;
		}
	}

	@Override
	public void restoreFromSettings() {
		if (Constants.ReleaseMode)
			return;
	}

	@Override
	public void saveToSettings(Editor editor) {
		if (Constants.ReleaseMode)
			return;
	}

} // Test
