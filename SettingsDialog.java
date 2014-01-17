package com.joyofplaying.babytracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsDialog extends BaseDialogFragment implements OnClickListener {
	private Test test = null;

	static SettingsDialog newInstance(BabyTrackerActivity activity, int stackLevel) {
		Utility.LogMethod();
		SettingsDialog dlg = new SettingsDialog();
		dlg.InitDialog(activity, null);
		return dlg;
	}

	@Override
	protected void InitDialog(BabyTrackerActivity activity, BaseFragment parent) {
		super.InitDialog(activity, parent);
		setArguments(new Bundle());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		Utility.LogMethod();
		getDialog().setTitle("Options");
		View v = inflater.inflate(R.layout.options_dialog, container, false);

		Button btn = (Button) v.findViewById(R.id.btnOptionsDone);
		btn.setOnClickListener(this);

		btn = (Button) v.findViewById(R.id.btnOptionsReset);
		btn.setOnClickListener(this);

		btn = (Button) v.findViewById(R.id.btnOptionsExportData);
		btn.setOnClickListener(this);

		CheckBox check = (CheckBox) v.findViewById(R.id.checkOptionsUseDatePicker);
		check.setOnClickListener(this);

		check = (CheckBox) v.findViewById(R.id.checkShowButtonCaptions);
		check.setChecked(getSettings().getShowButtonCaptions());
		check.setOnClickListener(this);

		check = (CheckBox) v.findViewById(R.id.checkUseAcra);
		check.setChecked(getSettings().getUseAcra());
		check.setOnClickListener(this);

		check = (CheckBox) v.findViewById(R.id.checkOptionsDebug);
		check.setChecked(getSettings().getDebugMode());
		check.setOnClickListener(this);

		check = (CheckBox) v.findViewById(R.id.checkSelectDateAfterSubmit);
		check.setChecked(getSettings().getSelectDateAfterSubmit());
		check.setOnClickListener(this);

		if (Constants.DebugMode) {
			this.test = new Test(null, v);
		}

		return v;
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		Utility.LogMethod();

		switch (view.getId()) {
		case R.id.btnOptionsReset:
			showComfirmationMesageBox();
			dismiss();
			break;

		case R.id.checkSelectDateAfterSubmit:
			CheckBox check = (CheckBox) view;
			getSettings().setSelectDateAfterSubmit(check.isChecked());
			break;

		case R.id.checkOptionsDebug:
			check = (CheckBox) view;
			getSettings().setDebugMode(check.isChecked());
			break;

		case R.id.checkShowButtonCaptions:
			check = (CheckBox) view;
			getSettings().setShowButtonCaptions(check.isChecked());
			break;

		case R.id.checkUseAcra:
			check = (CheckBox) view;
			getSettings().setUseAcra(check.isChecked());
			break;

		case R.id.btnOptionsDone:
			dismiss();
			getActivityEx().onActivate();
			break;

		case R.id.btnOptionsExportData:
			dismiss();
			getActivityEx().exportData();
			break;

		default:
			dismiss();
			break;
		}
	}

	private void showComfirmationMesageBox() {
		if (!getReady())
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(getString(R.string.warning));
		builder.setMessage(getString(R.string.comfirm_erase));

		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BabyTrackerActivity.getActivity().resetBabyData();
				dialog.dismiss();
			}
		});

		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();

	}
} // OptionsDialog
