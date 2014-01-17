package com.joyofplaying.babytracker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.joyofplaying.babytracker.Constants.OnScrollViewChanged;

public class TableLayoutScrollView extends ScrollView {

	private OnScrollViewChanged callback = null;

	public TableLayoutScrollView(Context context) {
		super(context);
		Utility.LogMethod();
	}

	public TableLayoutScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Utility.LogMethod();
	}

	public TableLayoutScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Utility.LogMethod();
	}

	public void setOnScrollViewChanged(OnScrollViewChanged callback) {
		this.callback = callback;
		Utility.LogMethod();
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		// Utility.LogMethod("{0} {1}", x, y, oldx, oldy);
		if (this.callback != null) {
			this.callback.onScrollChanged(this, x, y, oldx, oldy);
		}
		if (y == 0 && oldy == 0) {
			if (this.callback != null) {
				this.callback.onScrolledToBottom();
			}
		}

	}

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);

		// Utility.LogMethod("{0} {1}", scrollY, clampedY);
		if (clampedY) { // && scrollY > 0) {
			if (this.callback != null) {
				this.callback.onScrolledToBottom();
			}
		}
	}

}
