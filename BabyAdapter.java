package com.joyofplaying.babytracker;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.joyofplaying.babytracker.Constants.FragmentTypes;

public class BabyAdapter extends FragmentPagerAdapter implements OnPageChangeListener {
	private BaseFragment[] fragmentArray = new BaseFragment[FragmentTypes.values().length];
	private ViewPager pager = null;
	private FragmentTypes current = FragmentTypes.EntryPage;

	private BabyTrackerActivity activity = null;

	public BabyAdapter(BabyTrackerActivity activity, ViewPager pager) {
		super(activity.getFragmentManager());
		this.activity = activity;
		this.pager = pager;
		this.pager.setAdapter(this);
		this.pager.setOnPageChangeListener(this);
	}

	@Override
	public int getCount() {
		return Constants.FragmentCount;
	}

	@Override
	public Fragment getItem(int position) {
		return getItem(FragmentTypes.values()[position]);
	}

	public Fragment getItem(FragmentTypes type) {
		switch (type) {
		default:
		case EntryPage:
			return getEntryPage();
		case StatsPage:
			return getStatsPage();
		case DataGridPage:
			return getDataGridPage();
		}
	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		Utility.LogMethod("{0} to {1}", this.current, position);
		BaseFragment page = tryCurrentPage();
		if (page != null) {
			page.saveToSettings();
		}

		this.current = FragmentTypes.values()[position];

		page = getCurrentPage();
		page.onActivate(); // will only have an effect on already resumed pages
	}

	public void onPageCreated(BaseFragment page) {
		Utility.LogMethod("{0}", page.getClass().getName());
		this.fragmentArray[page.getType().ordinal()] = page;
	}

	public BaseFragment tryItem(FragmentTypes type) {
		return this.fragmentArray[type.ordinal()];
	}

	public BaseFragment tryCurrentPage() {
		return tryItem(this.current);
	}

	public BaseFragment getBaseFragment(FragmentTypes type) {
		BaseFragment page = fragmentArray[type.ordinal()];
		if (page != null) {
			return page;
		}

		switch (type) {
		case EntryPage:
			return new EntryPage();
		case StatsPage:
			return new StatsPage();
		case DataGridPage:
			return new DataGridPage();
		}

		Utility.LogError("Invalid FragmentType {0}", type);
		return null;
	}

	public EntryPage getEntryPage() {
		return (EntryPage) getBaseFragment(FragmentTypes.EntryPage);
	}

	public StatsPage getStatsPage() {
		return (StatsPage) getBaseFragment(FragmentTypes.StatsPage);
	}

	public DataGridPage getDataGridPage() {
		return (DataGridPage) getBaseFragment(FragmentTypes.DataGridPage);
	}

	public void showPage(FragmentTypes type) {
		Utility.LogMethod(type.toString());
		this.pager.setCurrentItem(type.ordinal());
	}

	public FragmentTypes getCurrent() {
		return this.current;
	}

	public BaseFragment getCurrentPage() {
		return (BaseFragment) getItem(this.current);
	}

	private BabyTrackerActivity getActivity() {
		return this.activity;
	}

	private SharedPreferences getAppSettings() {
		return getActivity().getAppSettings();
	}

	@SuppressWarnings("unused")
	private Context getAppContext() {
		return getActivity().getAppContext();
	}

	public void saveToSettings() {
		Utility.LogMethod();
		SharedPreferences.Editor editor = getAppSettings().edit();
		saveToSettings(editor);
		editor.commit();
	}

	public void saveToSettings(Editor editor) {
		if (this.current == null) {
			return;
		}

		editor.putInt("BabyAdapter_current", this.current.ordinal());
	}

	public void restoreFromSettings() {
		Utility.LogMethod();
		FragmentTypes saved = FragmentTypes.values()[getAppSettings().getInt("BabyAdapter_current", 0)];
		showPage(saved);
	}

} // BabyAdapter