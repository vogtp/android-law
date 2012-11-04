package ch.bedesign.android.law.access;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import ch.bedesign.android.law.access.LawUpdater.LawUpdateCallback;
import ch.bedesign.android.law.access.LawUpdater.LoadResult;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.db.DbInitaliser;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.EntriesModel;
import ch.bedesign.android.law.model.LawModel;

public class LawUpdater extends AsyncTask<LawModel, LawUpdateCallback, LoadResult> {

	private static final long UPDATE_INTERVALL_MILLIES = 1000 * 60 * 60 * 24 * 7; // once a week

	public interface LawUpdateCallback {
		public Context getContext();
	}

	public class LoadResult {
		private boolean ok = false;

		public LoadResult(boolean ok) {
			super();
			this.ok = ok;
		}

	}


	private final LawUpdateCallback callback;

	public LawUpdater(LawUpdateCallback callback) {
		super();
		this.callback = callback;
	}

	@Override
	protected LoadResult doInBackground(LawModel... params) {
		if (params.length < 1) {
			return new LoadResult(false);
		}
		for (LawModel param : params) {
			if (param != null) {
				try {
					updateLaw(param);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return new LoadResult(true);
	}

	private void insertLawText(parser lawData, ContentResolver resolver, LawModel law) throws ClientProtocolException, IOException {
		long lawId = law.getId();
		String SrNr = law.getCode();
		int i = 0;
		Logger.i("Loading law " + law.getCode());
		while (i < lawData.data.size()) {
			long p = insert(resolver, new EntriesModel(lawId, -1, lawData.data.get(i).getShortText(), lawData.data.get(i).getText(), null, 0));
			parser lawDataSecondLevel = new parser();
			lawDataSecondLevel.getText("http://www.admin.ch/ch/d/sr/" + SrNr + "/" + lawData.data.get(i).getLink());
			int x = 0;

			while (x < lawDataSecondLevel.data.size()) {
				if (lawDataSecondLevel.data.get(x).getId() == "ArtikelText") {
					insert(resolver, new EntriesModel(lawId, p, lawData.data.get(i).getShortText(), lawData.data.get(i).getText(), lawDataSecondLevel.data.get(x).getText(), 0));
				}
				{
					long l2 = insert(resolver,
							new EntriesModel(lawId, p, lawDataSecondLevel.data.get(x).getShortText(), lawDataSecondLevel.data.get(x).getShortText(), null, 1));
					parser lawDataThirdLevel = new parser();
					lawDataThirdLevel.getText("http://www.admin.ch/ch/d/sr/" + SrNr + "/" + lawDataSecondLevel.data.get(x).getLink());
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

	private void updateLaw(LawModel law) throws ClientProtocolException, IOException {

		parser lawData = new parser();
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

		ContentResolver resolver = callback.getContext().getContentResolver();

		if (DbInitaliser.CODE_VERFASSUNG.equals(law.getCode())) {
			parser lawData1 = new parser();
			lawData1.getText("http://www.admin.ch/ch/d/sr/101/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData1, resolver, law);
		}

		if (DbInitaliser.CODE_OR.equals(law.getCode())) {
			parser lawData1 = new parser();
			lawData1.getText("http://www.admin.ch/ch/d/sr/220/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData1, resolver, law);
		}

		if (DbInitaliser.CODE_ZGB.equals(law.getCode())) {
			parser lawData1 = new parser();
			lawData1.getText("http://www.admin.ch/ch/d/sr/210/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData1, resolver, law);
		}
			
		if (DbInitaliser.CODE_STGB.equals(law.getCode())) {
			parser lawData1 = new parser();
			lawData1.getText("http://www.admin.ch/ch/d/sr/311_0/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData1, resolver, law);
		}
		//FIXME only update if update successful
		law.setLastCheck(System.currentTimeMillis());
		law.setVersion(lawData.getLawVersion());
		resolver.update(Laws.CONTENT_URI, law.getValues(), Laws.SELECTION_CODE, new String[] { law.getCode() });

		//FIXME only update if update successful
		law.setLastCheck(System.currentTimeMillis());
		law.setVersion(lawData.getLawVersion());
		resolver.update(Laws.CONTENT_URI, law.getValues(), Laws.SELECTION_CODE, new String[] { law.getCode() });
	}
		
		
	private long insert(ContentResolver resolver, EntriesModel entriesModel) {
		Uri uri = resolver.insert(Entries.CONTENT_URI, entriesModel.getValues());
		return ContentUris.parseId(uri);
	}

	public static void loadLaw(LawUpdateCallback callback, LawModel... laws) {
		LawUpdater task = new LawUpdater(callback);
		task.execute(laws);
	}

}
