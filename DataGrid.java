package com.joyofplaying.babytracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.joyofplaying.babytracker.Constants.Anim;
import com.joyofplaying.babytracker.Constants.DataTypes;
import com.joyofplaying.babytracker.Constants.OnContextItemSelected;
import com.joyofplaying.babytracker.Constants.OnEditChildData;
import com.joyofplaying.babytracker.Constants.OnScrollViewChanged;
import com.joyofplaying.babytracker.Data.ChildData;
import com.joyofplaying.babytracker.LocalData.ChildDataTable;
import com.joyofplaying.babytracker.Utility.Formats;

public class DataGrid extends BaseHelper implements OnClickListener, OnLongClickListener, OnCreateContextMenuListener,
		ContextMenuInfo, OnContextItemSelected, OnScrollViewChanged {

	private class ScrollToSelected implements Runnable {
		ScrollView scrollView = null;
		TableLayout table = null;
		TableRow child = null;

		ScrollToSelected(ScrollView scrollView, TableLayout table, TableRow child) {
			this.scrollView = scrollView;
			this.table = table;
			this.child = child;
		}

		@Override
		public void run() {
			try {
				if (this.table != null && this.scrollView != null && this.child != null) {
					if (this.table.indexOfChild(this.child) == 0) {
						this.scrollView.scrollTo(0, this.child.getTop());

					} else {
						this.scrollView.scrollTo(0, this.child.getTop());
					}
				}
			} catch (Exception e) {
				LogMethod(e.toString());
			}
		}
	}

	private TableLayout tableView = null;
	private TableRow selected = null;
	private TableRow moreRow = null;
	private TableRow endRow = null;
	private TableLayoutScrollView scrollView = null;
	private List<ChildData> list = new ArrayList<ChildData>();
	private OnEditChildData recordHandler = null;
	private String whereClause = "";
	private int count = 0;
	private int limit = 0;
	private int limitStart = 0;
	private int limitEnd = 0;
	private final AtomicBoolean inQuery = new AtomicBoolean();
	private boolean reversed = false;
	private boolean contextMenuOnClick = true;
	private ChildData selectedChild = null;

	private boolean resultsPaged = false;

	DataGrid(BaseFragment fragment, View parent, int limit, boolean resultsPaged) {
		super(fragment);
		this.limit = limit;
		this.resultsPaged = resultsPaged;
		constructDataGrid(parent, null);
	}

	DataGrid(BaseFragment fragment, View parent, OnEditChildData recordHandler) {
		super(fragment);
		constructDataGrid(parent, recordHandler);
	}

	DataGrid(BaseFragment fragment, View parent, OnEditChildData recordHandler, int limit, boolean resultsPaged) {
		super(fragment);
		this.limit = limit;
		this.resultsPaged = resultsPaged;
		constructDataGrid(parent, recordHandler);
	}

	private void addDataRows() {
		if (!getReady())
			return;

		if (this.list.isEmpty()) {
			showTableView(false);
			return;
		}
		for (ChildData data : this.list) {
			addTableRow(data, false, false);
		}
	}

	private void addLastRow() {
		if (!getReady())
			return;

		removeLastRow();
		if (this.resultsPaged) {
			if (this.limit > 0) {
				if (this.list.size() == 0 || this.list.size() < this.limit) {
					LogMethod("Adding End Row");
					addTableRow(this.endRow, false);
				} else {
					LogMethod("Adding More Row");
					addTableRow(this.moreRow, false);
				}
			}
		}
	}

	private TableRow addTableRow(ChildData data, boolean atStart, boolean animate) {
		if (!getReady())
			return null;

		if (data.type >= DataTypes.values().length) {
			Utility.LogError("Invalid ChildData {0}", data.toString());
			return null;
		}

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.data_grid_table_row, this.tableView, false);

		TableRow tr = (TableRow) v.findViewById(R.id.data_grid_row);
		tr.setId(data.id);
		tr.setTag(data);
		tr.setOnClickListener(this);
		tr.setOnLongClickListener(this);
		tr.setOnCreateContextMenuListener(this);
		// tr.setBackgroundDrawable(getAppContext().getResources().getDrawable(R.drawable.table_row));

		TextView text = (TextView) v.findViewById(R.id.colType);
		text.setText(DataTypes.values()[data.type].toString());

		text = (TextView) v.findViewById(R.id.colDate);
		text.setText(Utility.toString(data.datetime, Formats.DateShort));

		text = (TextView) v.findViewById(R.id.colTime);
		text.setText(Utility.toString(data.datetime, Formats.TimeAmPm));

		text = (TextView) v.findViewById(R.id.colAmount);
		text.setText(getAmountString(data));

		text = (TextView) v.findViewById(R.id.colNote);
		text.setText(data.description);

		if (animate) {
			tr.setVisibility(View.VISIBLE);
			tr.startAnimation(AnimationUtils.loadAnimation(getAppContext(), R.anim.bounce));
		}
		addTableRow(tr, atStart);

		return tr;
	}

	private void addTableRow(TableRow tr, boolean atStart) {
		if (!getReady())
			return;

		if (this.reversed || atStart) {
			this.tableView.addView(tr, 0);
		} else {
			this.tableView.addView(tr);
		}
		this.count++;
		showTableView(true);
	}

	private void clear() {
		if (!getReady())
			return;

		LogMethod();
		this.whereClause = "";
		this.count = 0;
		this.tableView.removeAllViews();
		this.list.clear();
		this.limitStart = 0;
		this.limitEnd = 0;
	}

	private void constructDataGrid(View parent, OnEditChildData recordHandler) {
		this.recordHandler = recordHandler;
		this.tableView = (TableLayout) parent.findViewById(R.id.tableLayoutData);
		this.scrollView = (TableLayoutScrollView) parent.findViewById(R.id.tableLayoutDataScrollView);
		if (this.resultsPaged && this.scrollView != null) {
			this.scrollView.setOnScrollViewChanged(this);
		}

		this.moreRow = new TableRow(getActivity());
		createTextView(this.moreRow, "", true);
		createTextView(this.moreRow, "More", true);
		createTextView(this.moreRow, "...", true);
		createTextView(this.moreRow, "", true);
		createTextView(this.moreRow, "", false);
		this.moreRow.setOnClickListener(this);

		this.endRow = new TableRow(getActivity());
		createTextView(this.endRow, "", true);
		createTextView(this.endRow, "", true);
		createTextView(this.endRow, "", true);
		createTextView(this.endRow, "", true);
		createTextView(this.endRow, "", false);
	}

	private TextView createTextView(TableRow row, String value, boolean stretch) {
		TextView tv = new TextView(getActivity());
		tv.setText(value);
		tv.setFocusable(true);
		tv.setBackgroundDrawable(getAppContext().getResources().getDrawable(R.drawable.table_row));

		row.addView(tv);
		return tv;
	}

	private void deleteChild() {
		if (!getReady())
			return;

		LogMethod();
		if (this.selected == null)
			return;
		deleteChild(this.selected);
		this.selected = null;
	}

	private void deleteChild(TableRow row) {
		if (!getReady())
			return;

		LogMethod();
		if (row == null)
			return;
		this.count--;
		Animation anim = AnimationUtils.loadAnimation(getAppContext(), android.R.anim.fade_out);

		anim.setDuration(Anim.RemoveDataRow);
		row.startAnimation(anim);
		row.setVisibility(View.INVISIBLE);
		row.setSelected(false);
		removeTableRow(this.tableView, row, Anim.RemoveDataRow);
		if (this.count <= 0) {
			showTableView(false);
			this.count = 0;
		}
	}

	public void display() {
		if (!getReady())
			return;

		display("");
	}

	public void display(int delay) {
		if (!getReady())
			return;

		Utility.runDelayed(new Runnable() {
			@Override
			public void run() {
				display();
			}
		}, delay);
	}

	public void display(String whereClause) {
		if (!getReady())
			return;

		LogMethod("reversed: {0}", this.reversed);
		clear();
		this.whereClause = whereClause;

		if (getSettings().getRegistered()) {
			ChildDataTable query = getChildQuery();
			setQueryLimit(query);
			this.list = query.select(this.whereClause);
			addDataRows();
			addLastRow();

			if (this.selectedChild != null) {
				this.editChildData(this.selectedChild);
				this.selectedChild = null;
			}

			scrollToSelectedChild();

			showTableView(this.list.size() > 0);
		} else {
			showTableView(false);
		}
	}

	private void displayMoreData() {
		if (!getReady())
			return;

		LogMethod();

		removeLastRow();
		if (this.list.size() == 0) {
			this.inQuery.set(false);
			return;
		}

		if (getSettings().getRegistered()) {
			this.limitStart += this.limit;
			this.limitEnd = this.limitStart + this.limit;
			LocalData.ChildDataTable query = getActivity().getChildQuery();
			setQueryLimit(query);
			this.list = query.select(this.whereClause);
			if (!this.list.isEmpty()) {
				addDataRows();
			}
			addLastRow();
		}

		this.inQuery.set(false);
	}

	@SuppressWarnings("unused")
	private void displayMoreData(int delay) {
		if (!getReady())
			return;

		onClick(this.moreRow);
		Utility.runDelayed(new Runnable() {
			@Override
			public void run() {
				displayMoreData();
			}
		}, delay);
	}

	private ChildData editChildData(ChildData data) {
		if (!getReady())
			return null;

		LogMethod();

		if (getChildData() == data) {
			return data;
		}

		this.selected = null;
		for (int idx = 0; idx < this.tableView.getChildCount(); idx++) {
			TableRow row = (TableRow) this.tableView.getChildAt(idx);
			if (row.getId() == data.id && row.getTag() != null) {
				this.selected = row;
				data = (ChildData) row.getTag();
				break;
			}
		}
		if (this.selected == null) {
			this.selected = onAddChild(data, true);
		}
		this.selected.setSelected(true);
		return data;
	}

	private String getAmountString(ChildData data) {
		if (!getReady())
			return "";

		switch (DataTypes.values()[data.type]) {
		case Wet:
		case Poop:
			return "";
		case Nurse:
		case Bottle:
		case Pump:
			return Utility.AmountToString(data.amount) + " " + data.ozml;
		}
		return "";
	}

	private ChildData getChildData() {
		if (this.selected != null) {
			return (ChildData) this.selected.getTag();
		}

		return null;
	}

	public TableRow onAddChild(ChildData data, boolean atStart) {
		if (!getReady())
			return null;

		LogMethod();
		// this.list.add(data);
		TableRow row = addTableRow(data, atStart, true);

		if (this.resultsPaged && this.limit > 0) {
			for (int idx = this.limit; idx < this.tableView.getChildCount(); idx++) {
				deleteChild((TableRow) this.tableView.getChildAt(idx));
			}
		}
		return row;
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		LogMethod();

		if (this.selected != null & this.selected != view) {
			this.selected.setSelected(false);
			this.selected = null;
		}

		if (view == this.moreRow) {
			this.moreRow.setSelected(true);
			onScrolledToBottom();
		} else if (view.getTag() != null) {
			TableRow row = (TableRow) view;
			row.setSelected(true);
			this.selected = row;

			if (this.contextMenuOnClick) {
				getActivity().openContextMenu(view, this);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!getReady())
			return false;

		LogMethod();
		Utility.Log("context menu for selected item {0}", this.selected);
		ChildData data = getChildData();
		switch (item.getItemId()) {
		case R.id.menu_edit:
			if (data != null) {
				this.recordHandler.onEditChild(data);
			}
			return true;

		case R.id.menu_delete:
			if (data != null) {
				this.recordHandler.onUnEditChild(data);
				if (this.recordHandler.onDeleteChild(data)) {
					deleteChild();
				} else {
					Utility.Error(getActivity().getString(R.string.failed_to_delete_the_record));
				}
			}
			break;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if (!getReady())
			return;

		LogMethod();
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.layout.table_row_menu, menu);
	}

	@Override
	public boolean onLongClick(View view) {
		if (!getReady())
			return false;

		LogMethod();
		onClick(view);
		if (this.contextMenuOnClick == false) {
			getActivity().openContextMenu(view, this);
		}
		return true;
	}

	@Override
	public void onScrollChanged(TableLayoutScrollView scrollView, int x, int y, int oldx, int oldy) {
		if (!getReady())
			return;

	}

	@Override
	public void onScrolledToBottom() {
		if (!getReady())
			return;

		if (this.inQuery.compareAndSet(false, true)) {
			displayMoreData();
		}
	}

	public void onUpdateChild(ChildData data) {
		if (!getReady())
			return;

		LogMethod();
		if (this.selected == null)
			return;

		updateTextView(0, DataTypes.values()[data.type].toString());
		updateTextView(1, Utility.toString(data.datetime, Formats.DateShort));
		updateTextView(2, Utility.toString(data.datetime, Formats.TimeAmPm));
		updateTextView(3, getAmountString(data));
		updateTextView(4, data.description);

		this.selected.setTag(data);
	}

	private void removeLastRow() {
		if (!getReady())
			return;

		this.moreRow.setSelected(false);
		this.tableView.removeView(this.moreRow);
		this.tableView.removeView(this.endRow);
	}

	private void removeTableRow(final TableLayout source, final View target, int delay) {
		if (!getReady())
			return;

		Utility.runDelayed(new Runnable() {
			@Override
			public void run() {
				source.removeView(target);
			}
		}, delay);
	}

	@Override
	public void restoreFromSettings() {
		// restoring the limit overrides the limit set by constructor unless it
		// was already saved

		// Only the caller should cause rows to be displayed
		// this.whereClause = getAppSettings().getString("DataGrid_whereClause",
		// "");
		// this.limit = getAppSettings().getInt("DataGrid_limit", this.limit);
	}

	@Override
	public void saveToSettings(Editor editor) {
		// editor.putString("DataGrid_whereClause", this.whereClause);
		// editor.putInt("DataGrid_limit", this.limit);
	}

	private final void scrollToSelectedChild() {
		if (!getReady())
			return;

		if (this.selected != null) {
			new Handler().post(new ScrollToSelected(this.scrollView, this.tableView, this.selected));
		}
	}

	public void setContextMenuOnClick(boolean contextMenuOnClick) {
		this.contextMenuOnClick = contextMenuOnClick;
	}

	public void setHandler(OnEditChildData recordHandler) {
		this.recordHandler = recordHandler;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	private void setQueryLimit(LocalData.ChildDataTable query) {
		if (this.limitStart != 0 && this.limitEnd != 0) {
			query.setLimit(String.format("%d, %d", this.limitStart, this.limitEnd));
		} else if (this.limit != 0) {
			query.setLimit(String.format("%d", this.limit));
		}
	}

	public void setReversed(boolean reversed) {
		this.reversed = reversed;
	}

	public void setSelectedChild(ChildData data) {
		this.selectedChild = data;
	}

	private void showTableView(boolean show) {
		if (!getReady())
			return;

		if (show) {
			this.tableView.setVisibility(View.VISIBLE);
			if (this.scrollView != null) {
				this.scrollView.setBackgroundDrawable(getActivity().getDrawable(R.drawable.data_grid_border));
			}
		} else {
			this.tableView.setVisibility(View.GONE);
			if (this.scrollView != null) {
				this.scrollView.setBackgroundDrawable(null);
			}
		}
	}

	private void updateTextView(int idx, String value) {
		if (!getReady())
			return;

		LogMethod();
		TableRow row = this.selected;
		TextView tv = (TextView) row.getChildAt(idx);
		tv.setText(value);
	}

} // DataGrid
