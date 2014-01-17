package com.joyofplaying.babytracker;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joyofplaying.babytracker.Constants.DataTypes;
import com.joyofplaying.babytracker.Constants.FragmentTypes;
import com.joyofplaying.babytracker.Constants.OnDateSelectionChange;

public class StatsPage extends BaseFragment implements OnClickListener, OnDateSelectionChange {

	private DateSpinner dateSpinner = null;
	private TextView nurseRow = null;
	private TextView bottleRow = null;
	private TextView pumpRow = null;
	private TextView wetRow = null;
	private TextView poopyRow = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Utility.LogMethod();

		View v = inflater.inflate(R.layout.stats_page, container, false);
		super.onCreateView(FragmentTypes.StatsPage, v);

		this.dateSpinner = new DateSpinner(this, v, this);
		this.nurseRow = (TextView) v.findViewById(R.id.nurseStatsRow);
		this.bottleRow = (TextView) v.findViewById(R.id.bottleStatsRow);
		this.pumpRow = (TextView) v.findViewById(R.id.pumpStatsRow);
		this.wetRow = (TextView) v.findViewById(R.id.wetStatsRow);
		this.poopyRow = (TextView) v.findViewById(R.id.poopyStatsRow);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!getReady())
			return;

		Utility.LogMethod();

		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		if (!getActive())
			return;

		Utility.LogMethod();

		this.dateSpinner.onActivate();
		// fillStatsPage();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!getReady())
			return false;

		Utility.LogMethod();
		return false;
	}

	/* Other methods */
	@SuppressWarnings("unused")
	private void fillStatsPage(int delay) {
		Utility.runDelayed(new Runnable() {
			@Override
			public void run() {
				fillStatsPage();
			}
		}, delay);

	}

	private void fillStatsPage() {
		if (!getReady())
			return;

		Utility.LogMethod();

		NumberFormat formatter = new DecimalFormat("#,##0.##");

		LocalData.Query query = getApp().getQuery();
		query.setDateRange(String.format("'%s'", this.dateSpinner.getMinDate()),
				String.format("date('%s','+1 day')", this.dateSpinner.getMaxDate()));

		int value;

		query.setType(DataTypes.Nurse);
		value = query.filteredQueryValue("count(*)", 0);
		if (value > 0) {
			int amount = query.filteredQueryValue("sum(amount)", 0);
			fillStatsRow(this.nurseRow, R.string.nurses_for_minutes, value, amount);
		} else {
			this.nurseRow.setText(this.getString(R.string.zero_nursings));
		}

		query.setType(DataTypes.Bottle);
		value = query.filteredQueryValue("count(*)", 0);
		if (value > 0) {
			double dblAmount = query.filteredQueryValue("sum(amount_oz)", 0.0);
			fillStatsRow(this.bottleRow, R.string.bottle_feedings_for_ounces, value, formatter.format(dblAmount));
		} else {
			this.bottleRow.setText(this.getString(R.string.zero_bottle_feedings));
		}

		query.setType(DataTypes.Pump);
		value = query.filteredQueryValue("count(*)", 0);
		if (value > 0) {
			double dblAmount = query.filteredQueryValue("sum(amount_oz)", 0.0);
			fillStatsRow(this.pumpRow, R.string.pumps_for_ounces, value, formatter.format(dblAmount));
		} else {
			this.pumpRow.setText(this.getString(R.string.zero_pumps));
		}

		query.setType(DataTypes.Wet);
		value = query.filteredQueryValue("count(*)", 0);
		fillStatsRow(this.wetRow, R.string.wet_diapers, value);

		query.setType(DataTypes.Poop);
		value = query.filteredQueryValue("count(*)", 0);
		fillStatsRow(this.poopyRow, R.string.poopy_diapers, value);
	}

	private void fillStatsRow(TextView view, int id, Object... args) {
		String str = MessageFormat.format(getActivityEx().getString(id), args);
		Utility.LogMethod(str);
		view.setText(str);
	}

	@Override
	public void restoreFromSettings() {
		super.restoreFromSettings();
		if (!getReady())
			return;

		Utility.LogMethod();
		if (this.dateSpinner != null) {
			this.dateSpinner.restoreFromSettings();
		}
	}

	@Override
	public void saveToSettings(Editor editor) {
		super.saveToSettings(editor);
		if (!canSave())
			return;

		Utility.LogMethod();
		if (this.dateSpinner != null) {
			this.dateSpinner.saveToSettings(editor);
		}
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		Utility.LogMethod();
	}

	@Override
	public void onDateSelectionChange() {
		if (!getReady())
			return;

		fillStatsPage();
	}

} // StatsPage
