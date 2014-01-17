package com.joyofplaying.babytracker;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.joyofplaying.babytracker.Constants.ActivityRequestType;
import com.joyofplaying.babytracker.Constants.OnActivityRequest;
import com.joyofplaying.babytracker.Constants.OnPhotoUrlChange;

public class LogoImage extends BaseHelper implements OnClickListener, OnLongClickListener, OnActivityRequest,
		OnPhotoUrlChange {

	private ImageButton logoImage = null;
	private TextView logoCaption = null;
	private Uri selectionUri = null;
	private final String photoUrl = null;
	private OnPhotoUrlChange callback = null;
	private long photoLastModifiedTime = 0;

	public LogoImage(BaseFragment fragment, View parent, OnPhotoUrlChange callback) {
		super(fragment);

		this.callback = null;
		this.logoImage = (ImageButton) parent.findViewById(R.id.btnLogo);
		// this.logoImage.setPadding(0, 0, 0, 0);
		this.logoCaption = (TextView) parent.findViewById(R.id.btnLogoCaption);
		if (fragment != null) {
			// Setup dialog doesn't have a BaseFragment
			this.logoImage.setOnClickListener(this);
			this.logoImage.setOnLongClickListener(this);
		}
		applyImage();
	}

	@Override
	public void onPhotoUrlChange(String photoUrl) {
		// When fragment is shutdown can't update the image reliably
		persistPhotoUrl(photoUrl);

		if (!getReady())
			return;

		LogMethod("Setting photoUrl: {0}", photoUrl);
		applyImage();
	}

	private void persistPhotoUrl(String photoUrl) {
		if (Utility.IsValid(photoUrl)) {
			try {
				if (getSettings() != null) {
					getSettings().setPhotoUrl(photoUrl);
				}
			} catch (Exception e) {
				Utility.LogWarning("{0}", e.getMessage());
			}
			try {
				if (this.callback != null) {
					this.callback.onPhotoUrlChange(photoUrl);
				}
			} catch (Exception e) {
				Utility.LogWarning("{0}", e.getMessage());
			}
		}
	}

	public void showChangeChildPhotoDialog() {
		if (!getReady())
			return;

		showSelectPhotoDialog(getActivity().getChildName());
	}

	public void showSelectPhotoDialog(String PictureName) {
		if (!getReady())
			return;

		File file = Utility.makeExternalFile(PictureName, ".jpg");
		this.selectionUri = Uri.fromFile(file);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		// intent.putExtra("aspectX", aspectX);
		// intent.putExtra("aspectY", aspectY);
		// intent.putExtra("outputX", outputX);
		// intent.putExtra("outputY", outputY);
		// intent.putExtra("scale", scale);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, this.selectionUri);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", false);
		intent.putExtra("circleCrop", true);
		getActivity().startActivityForResult(
				Intent.createChooser(intent, getActivity().getString(R.string.select_picture)),
				ActivityRequestType.GetPhoto.ordinal());
		getActivity().setActivityResultCallback(ActivityRequestType.GetPhoto, this);
	}

	// @Override
	@Override
	public void onActivityRequestRecieved(int requestType, boolean succeeded, Intent data) {
		if (!succeeded || data == null) {
			Utility.showToast("No Image is selected.");
			this.selectionUri = Uri.fromFile(new File(getSettings().getPhotoUrl()));
		}
		// Use existing photoUri on success
		// this.photoUri = data.getData();
		onPhotoUrlChange(this.selectionUri.getEncodedPath());
	}

	@Override
	public void onClick(View view) {
		if (!getReady())
			return;

		switch (view.getId()) {
		case R.id.btnLogo:
			getActivity().openOptionsMenu();
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (!getReady())
			return false;

		if (v.getId() == R.id.btnLogo) {
			showSelectPhotoDialog(getActivity().getChildName());
			return true;
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!getReady())
			return;

		restoreFromSettings();
		applyImage();

		super.postResume();
	}

	@Override
	public void onActivate() {
		super.onActivate();
		if (!getActive())
			return;

		applyImage();

		String name = getActivity().getChildName();
		if (this.logoCaption != null) {
			this.logoCaption.setText(name);
			updateButtonCaptionState(this.logoCaption);
		}
	}

	private String getPhotoUrl() {
		return this.photoUrl;
	}

	public static String lookupPhotoUrl() {
		return BabyTrackerActivity.getActivity().getSettings().getPhotoUrl();
	}

	private long getPhotoLastModifiedTime() {
		String photoUrl = getSettings().getPhotoUrl();
		if (Utility.IsValid(photoUrl)) {
			File f = new File(photoUrl);
			if (f != null) {
				return f.lastModified();
			}
		}
		return 0;
	}

	public void applyImage() {

		if (getActivity() != null) {
			String photoUrl = getSettings().getPhotoUrl();
			if (Utility.IsValid(photoUrl)) {
				long lastModifiedTime = getPhotoLastModifiedTime();
				if ((this.photoLastModifiedTime != lastModifiedTime) || (lastModifiedTime == 0)) {
					applyPhotoUrl(getSettings().getPhotoUrl());
				}
			} else if (this.photoLastModifiedTime != -1) {
				applyDefaultImage();
			}
		}
	}

	private void applyDefaultImage() {
		this.logoImage.setImageDrawable(getResources().getDrawable(R.drawable.logo));
		this.photoLastModifiedTime = -1;
	}

	public void applyPhotoUrl(String photoUrl) {
		LogMethod("{0}", photoUrl);

		BitmapFactory.Options options = new BitmapFactory.Options();
		Bitmap bm = BitmapFactory.decodeFile(photoUrl, options);
		applyBitmap(bm);

		this.photoLastModifiedTime = getPhotoLastModifiedTime();
	}

	private void applyBitmap(Bitmap bm) {
		if (bm != null) {
			int width = getResources().getDimensionPixelSize(R.dimen.btn_type_width);
			int height = getResources().getDimensionPixelSize(R.dimen.btn_type_height);
			Bitmap bmScaled = Bitmap.createScaledBitmap(bm, width, height, false);
			this.logoImage.setImageBitmap(bmScaled);
		}
	}

	@Override
	public void saveToSettings(SharedPreferences.Editor editor) {
		super.saveToSettings(editor);
		if (!canSave())
			return;

		getActivity().saveChildToSettings(editor);
	}
}
