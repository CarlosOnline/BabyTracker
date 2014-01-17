package com.joyofplaying.babytracker;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.joyofplaying.babytracker.Constants.DataTypes;
import com.joyofplaying.babytracker.Constants.FragmentTypes;
import com.joyofplaying.babytracker.Constants.OnDateSelectionChange;
import com.joyofplaying.babytracker.Constants.OnEditChildData;
import com.joyofplaying.babytracker.Constants.OnTypeButtonClick;
import com.joyofplaying.babytracker.Constants.SpinDataTypes;
import com.joyofplaying.babytracker.Data.ChildData;
import com.joyofplaying.babytracker.LocalData.Query;

public class DataGridPage extends BaseFragment implements OnClickListener, OnTypeButtonClick, OnDateSelectionChange,
		OnEditChildData {

	private DataGrid dataGrid = null;
	private final ButtonTypes types = null;
	private DateSpinner dateSpinner = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.data_grid_page, container, false);
		super.onCreateView(FragmentTypes.DataGridPage, v);

		this.dataGrid = new DataGrid(this, v, this, Constants.MaxDataGridRows, true);
		this.dateSpinner = new DateSpinner(this, v, this);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!getReady())
			return;

		if (this.types != null) {
			this.types.setHandler(this);
			this.types.onResume();
		}
		if (this.dateSpinner != null) {
			this.dateSpinner.onResume();
		}

		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		if (!getActive())
			return;

		Utility.LogMethod();
		this.dateSpinner.onActivate(); // will cause an onDateSelectionChange
										// event if something different
		this.dataGrid.onActivate();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!getReady())
			return false;

		Utility.LogMethod();
		if (this.dataGrid != null) {
			return this.dataGrid.onContextItemSelected(item);
		}
		return false;
	}

	/* Other methods */

	@SuppressWarnings("unused")
	private void fillTable() {
		if (!getReady())
			return;

		Utility.LogMethod();
		this.dataGrid.display();
	}

	@SuppressWarnings("unused")
	private void fillTable(int delay) {
		if (!getReady())
			return;

		Utility.LogMethod();
		this.dataGrid.display(delay);
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		Utility.LogMethod();
	}

	@Override
	public void onTypeButtonClick(DataTypes type) {
		if (!getReady())
			return;
	}

	@Override
	public void onDateSelectionChange() {
		if (!getReady())
			return;

		Query query = getApp().getQuery();

		query.setDateRange(String.format("'%s'", this.dateSpinner.getMinDate()),
				String.format("date('%s','+1 day')", this.dateSpinner.getMaxDate()));
		if (this.dateSpinner.getSpinDataType() == SpinDataTypes.All) {
			query.clearType();
		} else {
			query.setType(DataTypes.values()[this.dateSpinner.getSpinDataType().ordinal() - 1]);
		}
		this.dataGrid.display(query.getFilteredWhereClause());
	}

	@Override
	public void onEditChild(ChildData data) {
		if (!getReady())
			return;

		Utility.LogMethod();
		getActivityEx().onEditChild(data);
	}

	@Override
	public boolean onDeleteChild(ChildData data) {
		if (!getReady())
			return false;

		return getActivityEx().deleteData(data);
	}

	@Override
	public void onUnEditChild(ChildData data) {
		if (!getReady())
			return;
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
		if (this.dataGrid != null) {
			this.dataGrid.restoreFromSettings();
		}
	}

	@Override
	public void saveToSettings(Editor editor) {
		super.saveToSettings(editor);
		if (!canSave())
			return;

		Utility.LogMethod();

		if (this.dataGrid != null) {
			this.dataGrid.saveToSettings(editor);
		}
		if (this.dateSpinner != null) {
			this.dateSpinner.saveToSettings(editor);
		}
	}

} // DataGridPage
