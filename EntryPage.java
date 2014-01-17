package com.joyofplaying.babytracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joyofplaying.babytracker.Constants.FragmentTypes;
import com.joyofplaying.babytracker.Constants.OnEditChildData;
import com.joyofplaying.babytracker.Data.ChildData;

public class EntryPage extends BaseFragment implements OnEditChildData {

	private ButtonTypes types = null;
	private EditTypes edits = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Utility.LogMethod();
		View v = inflater.inflate(R.layout.data_entry_page, container, false);
		super.onCreateView(FragmentTypes.EntryPage, v);
		this.types = new ButtonTypes(this, v);
		this.edits = new EditTypes(this, v, this.types);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!getReady())
			return;

		this.types.setHandler(this.edits);
		this.edits.onResume();
		this.types.onResume();

		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();

		if (!getActive())
			return;

		Utility.LogMethod();

		this.edits.onActivate();
		this.types.onActivate();
	}

	public void onEditChild(ChildData data) {
		if (!getReady())
			return;

		if (this.edits != null) {
			this.edits.onEditChild(data);
		}
	}

	public boolean onDeleteChild(ChildData data) {
		if (!getReady())
			return false;

		if (this.edits != null) {
			return this.edits.onDeleteChild(data);
		}
		return false;
	}

	public void onUnEditChild(ChildData data) {
		if (!getReady())
			return;

		if (this.edits != null) {
			this.edits.onUnEditChild(data);
		}
	}

	@Override
	public void restoreFromSettings() {
		super.restoreFromSettings();
		if (!getReady())
			return;

		Utility.LogMethod();
		if (this.types != null) {
			this.types.restoreFromSettings();
		}
		if (this.edits != null) {
			this.edits.restoreFromSettings();
		}
	}

	@Override
	public void saveToSettings(SharedPreferences.Editor editor) {
		super.saveToSettings(editor);
		if (!canSave())
			return;

		Utility.LogMethod();
		if (this.types != null) {
			this.types.saveToSettings(editor);
		}
		if (this.edits != null) {
			this.edits.saveToSettings(editor);
		}
	}

} // UI
