package ch.bedesign.android.law.access;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import ch.bedesign.android.law.access.LawUpdater.LoadResult;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.EntriesModel;
import ch.bedesign.android.law.model.LawModel;

public class LawUpdater extends AsyncTask<Long, Object, LoadResult> {

	private static final long UPDATE_INTERVALL_MILLIES = 1000 * 60 * 60 * 24 * 7; // once a week

	//	public interface LawUpdateCallback {
	//		public Context getContext();
	//	}

	public class LoadResult {
		private boolean ok = false;

		public LoadResult(boolean ok) {
			super();
			this.ok = ok;
		}

	}

	private final Context ctx;

	//	private final LawUpdateCallback callback;

	public LawUpdater(Context ctx) {
		super();
		this.ctx = ctx.getApplicationContext();
	}

	@Override
	protected LoadResult doInBackground(Long... lawIds) {
		if (lawIds.length < 1) {
			return new LoadResult(false);
		}
		for (Long lawId : lawIds) {
			if (lawId != null) {
				LawModel law = null;
				ContentResolver resolver = ctx.getContentResolver();
				try {
					Cursor c = resolver.query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, new String[] { Long.toString(lawId) },
							Laws.SORTORDER_DEFAULT);
					if (c != null && c.moveToFirst()) {
						law = new LawModel(c);
						law.setIsUpdating(System.currentTimeMillis());
						resolver.update(Laws.CONTENT_URI, law.getValues(), DB.SELECTION_BY_ID, new String[] { Long.toString(lawId) });
						updateLaw(resolver, law);
					}
				} catch (Exception e) {
					Logger.w("Cannot update law", e);
				} finally {
					if (law != null) {
						law.setIsUpdating(-1);
						resolver.update(Laws.CONTENT_URI, law.getValues(), DB.SELECTION_BY_ID, new String[] { Long.toString(lawId) });
					}
				}
			}
		}
		return new LoadResult(true);
	}

	private void insertLawText(Parser lawData, ContentResolver resolver, LawModel law) throws ClientProtocolException, IOException {
		long lawId = law.getId();
		String SrNr = law.getCode();
		int i = 0;
		Logger.i("Loading law " + law.getCode());
		while (i < lawData.data.size()) {
			long p = insert(resolver, new EntriesModel(lawId, -1, lawData.data.get(i).getShortText(), lawData.data.get(i).getText(), null, 0));
			Parser lawDataSecondLevel = new Parser(ctx, "http://www.admin.ch/ch/d/sr/" + SrNr + "/" + lawData.data.get(i).getLink());
			lawDataSecondLevel.parse();
			int x = 0;

			while (x < lawDataSecondLevel.data.size()) {
				if (lawDataSecondLevel.data.get(x).getId() == "ArtikelText") {
					insert(resolver, new EntriesModel(lawId, p, lawData.data.get(i).getShortText(), lawData.data.get(i).getText(), lawDataSecondLevel.data.get(x).getText(), 0));
				}
				{
					long l2 = insert(resolver,
							new EntriesModel(lawId, p, lawDataSecondLevel.data.get(x).getShortText(), lawDataSecondLevel.data.get(x).getShortText(), null, 1));
					Parser lawDataThirdLevel = new Parser(ctx, "http://www.admin.ch/ch/d/sr/" + SrNr + "/" + lawDataSecondLevel.data.get(x).getLink());
					lawDataThirdLevel.parse();
					int y = 0;
					while (y < lawDataThirdLevel.data.size()) {
						insert(resolver, new EntriesModel(lawId, l2, lawDataSecondLevel.data.get(x).getShortText(), lawDataSecondLevel.data.get(x).getShortText(),
								lawDataThirdLevel.data.get(y).getText(), 0));
						y++;
					}
				}
				x++;
			}
			i++;
		}
	}

	private void updateLaw(ContentResolver resolver, LawModel law) throws ClientProtocolException, IOException {

		Parser lawData = new Parser(ctx, law.getUrl());
		try {
			if (System.currentTimeMillis() < law.getLastCheck() + UPDATE_INTERVALL_MILLIES) {
				Logger.i("Not updating since the law is too new");
				return;
			}
			if (law.getVersion() != null && law.getVersion().equals(lawData.getLawVersion())) {
				Logger.i("Not updating since the law it has not changed");
				return;
			}
		} catch (Exception e) {
			// we do not care
		}

		lawData.parse();
		resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
		insertLawText(lawData, resolver, law);

		//FIXME only update if update successful
		law.setLastCheck(System.currentTimeMillis());
		law.setVersion(lawData.getLawVersion());
		resolver.update(Laws.CONTENT_URI, law.getValues(), Laws.SELECTION_CODE, new String[] { law.getCode() });

	}

	private long insert(ContentResolver resolver, EntriesModel entriesModel) {
		Uri uri = resolver.insert(Entries.CONTENT_URI, entriesModel.getValues());
		return ContentUris.parseId(uri);
	}

	public static void loadLaw(Context ctx, Long... lawIds) {
		LawUpdater task = new LawUpdater(ctx);
		task.execute(lawIds);
	}

}
