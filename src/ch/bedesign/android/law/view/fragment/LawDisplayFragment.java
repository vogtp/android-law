package ch.bedesign.android.law.view.fragment;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.access.LawUpdater;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.LawModel;
import ch.bedesign.android.law.view.widget.DbUpdateProgressBar;

public class LawDisplayFragment extends ListFragment implements ILawFragment, LoaderCallbacks<Cursor> {

	public static final String ARG_LAW = "lawParcel";
	private static final String ARG_PARENT_ID = "parentId";

	private static final int LOADER_ENTRIES_LIST = 1;
	private static final int LOADER_GET_PARTENTID = 2;

	private SimpleCursorAdapter adapter;
	private LawModel law = LawModel.DUMMY;
	private long parentId = -1;
	private long oldParentId = -1;
	private DbUpdateProgressBar pbWait;
	private TextView tvTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		Bundle args = getArguments();
		law = (LawModel) args.getParcelable(ARG_LAW);
		if (savedInstanceState != null && law == null) {
			args = savedInstanceState;
			law = (LawModel) args.getParcelable(ARG_LAW);
		}
		if (law == null) {
			law = LawModel.DUMMY;
		}
		if (args.containsKey(ARG_PARENT_ID)) {
			parentId = args.getLong(ARG_PARENT_ID);
			Logger.v("got parentID=" + parentId);
		} else if (savedInstanceState != null && savedInstanceState.containsKey(ARG_PARENT_ID)) {
			parentId = savedInstanceState.getLong(ARG_PARENT_ID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.law_display_list, container, false);
		pbWait = (DbUpdateProgressBar) v.findViewById(R.id.pbWait);
		tvTitle = (TextView) v.findViewById(R.id.tvTitle);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		getListView().setVisibility(View.INVISIBLE);
		pbWait.setVisibility(View.VISIBLE);

		LawUpdater.loadLaw(getActivity(), getLawId());

		adapter = new SimpleCursorAdapter(getActivity(), R.layout.law_display_list_item, null,
				new String[] { DB.Entries.NAME_SHORT_NAME, DB.Entries.NAME_TEXT },
				new int[] { R.id.tvLawTitle, R.id.tvLawText }, 0);
		ViewBinder binder = new ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int idx) {
				if (DB.Entries.INDEX_TEXT == idx) {
					if (view instanceof TextView) {
						String string = cursor.getString(idx);
						if (string != null) {
							Spanned fromHtml = Html.fromHtml(string);
							TextView tv = (TextView) view;
							tv.setText(fromHtml);
							view.setVisibility(View.VISIBLE);
							((View) view.getParent()).findViewById(R.id.tvLawTitle).setVisibility(View.GONE);
							return true;
						}
					}
				} else if (DB.Entries.INDEX_SHORT_NAME == idx) {
					if (view instanceof TextView) {
						String string = cursor.getString(idx);
						if (string != null) {
							Spanned fromHtml = Html.fromHtml(string);
							TextView tv = (TextView) view;
							tv.setText(fromHtml);
							view.setVisibility(View.VISIBLE);
							((View) view.getParent()).findViewById(R.id.tvLawText).setVisibility(View.GONE);
							return true;
						}
					}
				}
				return false;
			}

		};
		adapter.setViewBinder(binder);
		setListAdapter(adapter);
		getLoaderManager().initLoader(LOADER_ENTRIES_LIST, null, this);
		pbWait.listenForChange(law.getId());
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (parentId != id) {
			oldParentId = parentId;
			parentId = id;
		}
		getLoaderManager().restartLoader(LOADER_ENTRIES_LIST, null, this);
	}

	public String getName() {
		return getLawName();
	}

	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		switch (loader) {
		case LOADER_GET_PARTENTID:
			return new CursorLoader(getActivity(), DB.Entries.CONTENT_URI, DB.Entries.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, new String[] { Long.toString(parentId) },
					DB.Entries.SORTORDER_DEFAULT);
		case LOADER_ENTRIES_LIST:
		default:
			return new CursorLoader(getActivity(), DB.Entries.CONTENT_URI, DB.Entries.PROJECTION_DEFAULT, DB.Entries.SELECTION_LAW_PARENT, new String[] {
					Long.toString(getLawId()), Long.toString(parentId) }, DB.Entries.SORTORDER_DEFAULT);
		}
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch (loader.getId()) {
		case LOADER_GET_PARTENTID:
			if (c.moveToFirst()) {
				parentId = c.getLong(DB.Entries.INDEX_PARENT_ID);
				getLoaderManager().restartLoader(LOADER_ENTRIES_LIST, null, this);
			}
			break;

		case LOADER_ENTRIES_LIST:
		default:
			if (c == null || c.getCount() < 1) {
				parentId = oldParentId;
				getLoaderManager().restartLoader(LOADER_ENTRIES_LIST, null, this);
				return;
			}
			adapter.swapCursor(c);

			getListView().setVisibility(View.VISIBLE);
			pbWait.setVisibility(View.INVISIBLE);
			break;
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	public boolean onBackPressed() {
		if (parentId == -1) {
			return false;
		}
		getLoaderManager().restartLoader(LOADER_GET_PARTENTID, null, this);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(ARG_LAW, law);
		outState.putLong(ARG_PARENT_ID, parentId);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.list_option, menu);
	};

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + getLawName();
	}

	private Long getLawId() {
		return law.getId();
	}

	private String getLawName() {
		return law.getName();
	}

	@Override
	public void onResume() {
		super.onResume();
		tvTitle.setText(getLawName());
	}

}
