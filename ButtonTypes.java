package com.joyofplaying.babytracker;

import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.joyofplaying.babytracker.Constants.DataTypes;
import com.joyofplaying.babytracker.Constants.OnTypeButtonClick;

public class ButtonTypes extends BaseHelper implements OnClickListener {

	private final ImageButton[] imageBtns = new ImageButton[DataTypes.values().length];
	private final TextView[] imageCaptions = new TextView[DataTypes.values().length];
	private ImageButton current = null;
	private DataTypes type = DataTypes.Nurse;
	private OnTypeButtonClick handler = null;

	ButtonTypes(BaseFragment fragment, View parent) {
		super(fragment);

		setupButton(parent, DataTypes.Nurse, R.id.btnNurse, R.id.btnNurseCaption);
		setupButton(parent, DataTypes.Bottle, R.id.btnBottle, R.id.btnBottleCaption);
		setupButton(parent, DataTypes.Pump, R.id.btnPump, R.id.btnPumpCaption);
		setupButton(parent, DataTypes.Wet, R.id.btnWetDiaper, R.id.btnWetDiaperCaption);
		setupButton(parent, DataTypes.Poop, R.id.btnPoopyDiaper, R.id.btnPoopyDiaperCaption);
	}

	private void setupButton(View parent, DataTypes type, int id, int idCaption) {
		ImageButton btn = (ImageButton) parent.findViewById(id);
		// btn.setPadding(0, 0, 0, 0);
		btn.setOnClickListener(this);
		btn.setTag(type);
		this.imageBtns[type.ordinal()] = btn;

		View view = parent.findViewById(idCaption);
		if (view != null) {
			this.imageCaptions[type.ordinal()] = (TextView) view;
		}
	}

	public void setHandler(OnTypeButtonClick handler) {
		this.handler = handler;
	}

	public void onTypeBtnClick(DataTypes type) {
		if (!getReady())
			return;

		LogMethod();

		onClick(getImageButton(type));
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		LogMethod();

		ImageButton imageBtn = (ImageButton) view;
		if (this.current != null && this.current != imageBtn) {
			this.current.setSelected(false);
			View parent = (View) this.current.getParent();
			parent.setSelected(false);
		}

		this.current = imageBtn;
		if (this.current != null) {
			this.type = getType(this.current);
			this.current.setSelected(true);
			View parent = (View) this.current.getParent();
			parent.setSelected(true);
			// this.imageBtn.setBackgroundColor(Utility.lightenColor(R.color.dark_goldenrod3,
			// (float) 0.75));
			if (this.handler != null) {
				this.handler.onTypeButtonClick(this.type);
			}
		}
	}

	public void setType(DataTypes type) {
		this.type = type;
		this.current = getImageButton(this.type);
	}

	public DataTypes getType() {
		return this.type;
	}

	private DataTypes getType(ImageButton btn) {
		return (DataTypes) btn.getTag();
	}

	public ImageButton getImageButton() {
		return this.current;
	}

	private ImageButton getImageButton(DataTypes type) {
		return this.imageBtns[type.ordinal()];
	}

	public static void adjustButtonWidth(ImageButton view) {
		int screenWidth = BabyTrackerActivity.getActivity().getWindowManager().getDefaultDisplay().getWidth();
		int width = (screenWidth / Constants.ButtonBarLength);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, width);
		view.setLayoutParams(params);
		view.setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

	private void updateButtonCaptions() {
		for (View view : this.imageCaptions) {
			updateButtonCaptionState(view);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!getReady())
			return;

		LogMethod();
		onTypeBtnClick(this.type);

		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		if (!getActive())
			return;

		// LogMethod();
		updateButtonCaptions();
	}

	@Override
	public void restoreFromSettings() {
		super.restoreFromSettings();
		if (!getReady())
			return;

		String type = getAppSettings().getString("ButtonTypes_type", DataTypes.Nurse.toString());
		this.type = DataTypes.valueOf(type);
	}

	@Override
	public void saveToSettings(Editor editor) {
		super.saveToSettings(editor);
		if (!canSave())
			return;

		editor.putString("ButtonTypes_type", this.type.toString());
	}

} // ButtonTypes

