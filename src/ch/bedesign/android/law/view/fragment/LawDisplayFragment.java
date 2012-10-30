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
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import ch.bedesign.android.law.R;
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

		adapter = new SimpleCursorAdapter(getActivity(), R.layout.law_display_list_item, null,
				new String[] { DB.Entries.NAME_FULL_NAME, DB.Entries.NAME_TEXT },
				new int[] { R.id.tvLawTitle, R.id.tvLawText }, 0);
		ViewBinder binder = new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int idx) {
				if (DB.Entries.INDEX_TEXT == idx) {
					if (view instanceof TextView) {
						String string = cursor.getString(idx);
						if (string != null) {
							Spanned fromHtml = Html.fromHtml(string);
							((TextView) view).setText(fromHtml);
							return true;
						}
					}
				}
				return false;
			}

		};
		adapter.setViewBinder(binder);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		parents.push(parentId);
		parentId = id;
		getLoaderManager().restartLoader(0, null, this);
	}

	public String getName() {
		return lawModel.getName();
	}

	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		String[] args = new String[] { Long.toString(lawModel.getId()), Long.toString(parentId) };
		return new CursorLoader(getActivity(), DB.Entries.CONTENT_URI, DB.Entries.PROJECTION_DEFAULT, DB.Entries.SELECTION_LAW_PARENT, args, DB.Entries.SORTORDER_DEFAULT);
	}

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

	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	public Context getContext() {
		return getActivity().getApplicationContext();
	}

	public boolean onBackPressed() {
		if (parentId == -1) {
			return false;
		}
		parentId = getLastParent();
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

}