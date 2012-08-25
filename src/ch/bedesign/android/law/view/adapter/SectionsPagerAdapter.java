package ch.bedesign.android.law.view.adapter;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.fragment.ILawFragment;
import ch.bedesign.android.law.view.fragment.LawDisplayFragment;
import ch.bedesign.android.law.view.fragment.LawsOverviewFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

	private final ArrayList<Fragment> pages = new ArrayList<Fragment>();
	private final ViewPager viewPager;

	public SectionsPagerAdapter(ViewPager viewPager, FragmentManager fm) {
		super(fm);
		this.viewPager = viewPager;
		pages.add(0, new LawsOverviewFragment(this));
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
		return ((ILawFragment) getItem(position)).getName();
	}

	public void addLayDisplay(LawModel lawModel) {
		Bundle args = new Bundle();
		Fragment fragment = new LawDisplayFragment(this, lawModel);
		//		args.putLong(LawDisplayFragment.ARG_LAW_ID, lawModel);
		//		fragment.setArguments(args);
		pages.add(fragment);
		notifyDataSetChanged();
		viewPager.setCurrentItem(pages.size(), true);
	}

	public boolean onBackPressed(int position) {
		return ((ILawFragment) getItem(position)).onBackPressed();
	}

}