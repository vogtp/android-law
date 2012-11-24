package ch.bedesign.android.law.view.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.access.LawUpdater;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.activity.MainActivity;

public class LawsOverviewFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;

	//	private final SectionsPagerAdapter pagerAdapter;

	//	public LawsOverviewFragment(SectionsPagerAdapter pagerAdapter) {
	//		this.pagerAdapter = pagerAdapter;
	//	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null,
				new String[] { DB.Laws.NAME_NAME, DB.Laws.NAME_VERSION },
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		adapter.setViewBinder(new ViewBinder() {

			public boolean setViewValue(View v, Cursor c, int colIdx) {
				if (colIdx == DB.Laws.INDEX_VERSION) {
					LawModel law = new LawModel(c);
					if (law.isUpdating()) {
						((TextView) v).setText(R.string.msg_updating);
						// FIXME move to application
						if (SettingsLaw.getInstance(getActivity()).isContinueUpdatesAtStartup()) {
							LawUpdater.loadLaw(getActivity(), law.getId());
						}
						return true;
					} else if (!law.isLoaded()) {
						((TextView) v).setText(R.string.msg_law_not_yet_loaded);
					}
				}
				return false;
			}
		});
		setListAdapter(adapter);
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = adapter.getCursor();
		if (cursor.moveToPosition(position)) {
			Activity act = getActivity();
			if (act != null && act instanceof MainActivity) {
				((MainActivity) act).addLawDisplay(new LawModel(cursor));
			}

		}
	}

	public String getName() {
		return "Gesetze";
	}

	
	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		return new CursorLoader(getActivity(), DB.Laws.CONTENT_URI, DB.Laws.PROJECTION_DEFAULT, null, null, DB.Laws.SORTORDER_DEFAULT);
	}

	
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		adapter.swapCursor(c);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	
	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.overview_list_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Logger.e("bad menuInfo", e);
			return false;
		}

		//		final Uri uri = ContentUris.withAppendedId(DB.VirtualGovernor.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.itemClearCache:
			getActivity().getContentResolver().delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(info.id) });
			return true;
		}

		return super.onContextItemSelected(item);
	}
}
