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
import ch.bedesign.android.law.db.DbInitaliser;
import ch.bedesign.android.law.model.EntriesModel;
import ch.bedesign.android.law.model.LawModel;

public class LawUpdater extends AsyncTask<LawModel, LawUpdateCallback, LoadResult> {

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
		ContentResolver resolver = callback.getContext().getContentResolver();
		if (DbInitaliser.CODE_VERFASSUNG.equals(law.getCode())) {
			parser lawData = new parser();
			lawData.getText("http://www.admin.ch/ch/d/sr/101/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData, resolver, law);
		}

		if (DbInitaliser.CODE_OR.equals(law.getCode())) {
			parser lawData = new parser();
			lawData.getText("http://www.admin.ch/ch/d/sr/220/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData, resolver, law);
		}

		if (DbInitaliser.CODE_ZGB.equals(law.getCode())) {
			parser lawData = new parser();
			lawData.getText("http://www.admin.ch/ch/d/sr/210/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData, resolver, law);
		}

		if (DbInitaliser.CODE_STGB.equals(law.getCode())) {
			parser lawData = new parser();
			lawData.getText("http://www.admin.ch/ch/d/sr/311_0/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			insertLawText(lawData, resolver, law);
		}

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
