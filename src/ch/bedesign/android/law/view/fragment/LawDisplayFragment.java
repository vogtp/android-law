package ch.bedesign.android.law.view.fragment;

import java.util.Stack;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import ch.bedesign.android.law.access.LawUpdater;
import ch.bedesign.android.law.access.LawUpdater.LawUpdateCallback;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.adapter.SectionsPagerAdapter;

public class LawDisplayFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor>, LawUpdateCallback {

	public static final String ARG_LAW_ID = "lawId";
	private SimpleCursorAdapter adapter;
	private final SectionsPagerAdapter pagerAdapter;
	//	private long lawId;
	private long parentId = -1;
	private final LawModel lawModel;
	private final Stack<Long> parents = new Stack<Long>();

	public LawDisplayFragment(SectionsPagerAdapter pagerAdapter, LawModel lawModel) {
		this.pagerAdapter = pagerAdapter;
		this.lawModel = lawModel;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
		//		Bundle args = getArguments();
		//		lawId = args.getLong(ARG_LAW_ID);
		LawUpdater.loadLaw(this, lawModel);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null,
				new String[] { DB.Entries.NAME_FULL_NAME, DB.Entries.NAME_TEXT },
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		parents.push(parentId);
		parentId = id;
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public String getName() {
		return lawModel.getName();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		String[] args = new String[] { Long.toString(lawModel.getId()), Long.toString(parentId) };
		return new CursorLoader(getActivity(), DB.Entries.CONTENT_URI, DB.Entries.PROJECTION_DEFAULT, DB.Entries.SELECTION_LAW_PARENT, args, DB.Entries.SORTORDER_DEFAULT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		if (c == null || c.getCount() < 1) {
			parentId = getLastParent();
			getLoaderManager().restartLoader(0, null, this);
			return;
		}
		adapter.swapCursor(c);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	private long getLastParent() {
		if (parents.size() < 1) {
			return parentId;
		}
		return parents.pop();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public Context getContext() {
		return getActivity().getApplicationContext();
	}

	@Override
	public boolean onBackPressed() {
		if (parentId == -1) {
			return false;
		}
		parentId = getLastParent();
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

}