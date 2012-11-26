package ch.bedesign.android.law.view.adapter;

import java.util.LinkedList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.fragment.ILawFragment;
import ch.bedesign.android.law.view.fragment.LawDisplayFragment;
import ch.bedesign.android.law.view.fragment.LawsOverviewFragment;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

	private final LinkedList<Fragment> pages = new LinkedList<Fragment>();
	private final LinkedList<String> names = new LinkedList<String>();
	private final ViewPager viewPager;
	private final FragmentManager fragmentManager;

	public SectionsPagerAdapter(ViewPager viewPager, FragmentManager fm) {
		super(fm);
		this.viewPager = viewPager;
		this.fragmentManager = fm;
		LawsOverviewFragment lawsOverviewFragment = new LawsOverviewFragment();
		pages.add(0, lawsOverviewFragment);
		names.add(0, lawsOverviewFragment.getName());
	}

	@Override
	public Fragment getItem(int i) {
		return pages.get(i);
	}

	@Override
	public int getCount() {
		return pages.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return names.get(position);
	}

	public void addLawDisplay(LawModel lawModel) {
		Bundle args = new Bundle();
		Fragment fragment = new LawDisplayFragment();
		args.putParcelable(LawDisplayFragment.ARG_LAW, lawModel);
		fragment.setArguments(args);
		addFragment(fragment, lawModel.getName());
	}

	public void addFragment(Fragment fragment, String name) {
		int pos = 1;
		if (SettingsLaw.getInstance().isInsertPageAtEnd()) {
			pos = pages.size();
		}
		pages.add(pos, fragment);
		names.add(pos, name);
		viewPager.setCurrentItem(pos, true);
		notifyDataSetChanged();
	}



	public boolean onBackPressed() {
		ILawFragment fragment = (ILawFragment) getCurrentFragment();
		if (fragment != null) {
			return fragment.onBackPressed();
		}
		return false;
	}

	public void removePage(int pos) {
		if (pos > 0 && pos < pages.size()) {
			Fragment fragment = pages.remove(pos);
			names.remove(pos);
			destroyItem(viewPager, pos, fragment);
			notifyDataSetChanged();
		}
	}

	public Fragment getCurrentFragment() {
		return getFragmentAtPos(viewPager.getCurrentItem());
	}

	public Fragment getFragmentAtPos(int pos) {
		Fragment fragment = fragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":" + pos);
		return fragment;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

}