package ch.bedesign.android.law.view.adapter;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.fragment.ILawFragment;
import ch.bedesign.android.law.view.fragment.LawDisplayFragment;
import ch.bedesign.android.law.view.fragment.LawsOverviewFragment;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

	private final ArrayList<Fragment> pages = new ArrayList<Fragment>();
	private final ViewPager viewPager;

	public SectionsPagerAdapter(ViewPager viewPager, FragmentManager fm) {
		super(fm);
		this.viewPager = viewPager;
		pages.add(0, new LawsOverviewFragment());
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

	public void addLawDisplay(LawModel lawModel) {
		Bundle args = new Bundle();
		Fragment fragment = new LawDisplayFragment();
		args.putLong(LawDisplayFragment.ARG_LAW_ID, lawModel.getId());
		args.putString(LawDisplayFragment.ARG_LAW_NAME, lawModel.getName());
		fragment.setArguments(args);
		pages.add(fragment);
		notifyDataSetChanged();
		viewPager.setCurrentItem(pages.size(), true);
	}

	public void addFromBundle(Bundle args) {
		Fragment fragment = new LawDisplayFragment();
		fragment.setArguments(args);
		pages.add(fragment);
		notifyDataSetChanged();
		viewPager.setCurrentItem(pages.size(), true);
	}

	public boolean onBackPressed(int position) {
		return ((ILawFragment) getItem(position)).onBackPressed();
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

}