package ch.bedesign.android.law.view.fragment;

import java.io.Serializable;
import java.util.Stack;

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
import android.widget.Toast;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.access.LawUpdater;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.log.Logger;

public class LawDisplayFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	public static final String ARG_LAW_ID = "lawId";
	public static final String ARG_LAW_NAME = "lawName";
	private static final String ARG_PARENT_ID = "parentId";
	private static final String ARG_PARENT_ID_STACK = "parentIdStack";
	private SimpleCursorAdapter adapter;
	private long lawId;
	private String lawName;
	private long parentId = -1;
	private Stack<Long> parents = new Stack<Long>();

	//	public LawDisplayFragment(SectionsPagerAdapter pagerAdapter, LawModel lawModel) {
	//		this.pagerAdapter = pagerAdapter;
	//		this.lawModel = lawModel;
	//	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
		Bundle args;
		if (savedInstanceState != null) {
			args = savedInstanceState;
		} else {
			args = getArguments();
		}
		lawId = args.getLong(ARG_LAW_ID);
		lawName = args.getString(ARG_LAW_NAME);
		if (args.containsKey(ARG_PARENT_ID)) {
			parentId = args.getLong(ARG_PARENT_ID);
		}
		if (args.containsKey(ARG_PARENT_ID_STACK)) {
			Serializable serializable = args.getSerializable(ARG_PARENT_ID_STACK);
			if (serializable instanceof Stack<?>) {
				parents = (Stack<Long>) serializable;
			} else {
				String msg = "Something went wrong with the parent IDs";
				Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
				Logger.e(msg);
			}
		}
		LawUpdater.loadLaw(getActivity().getApplicationContext(), lawId);

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
		return lawName;
	}

	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		String[] args = new String[] { Long.toString(lawId), Long.toString(parentId) };
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

	public boolean onBackPressed() {
		if (parentId == -1) {
			return false;
		}
		parentId = getLastParent();
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(ARG_LAW_ID, lawId);
		outState.putLong(ARG_PARENT_ID, parentId);
		outState.putString(ARG_LAW_NAME, lawName);
		outState.putSerializable(ARG_PARENT_ID_STACK, parents);
	}


}