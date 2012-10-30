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

	private void updateLaw(LawModel law) throws ClientProtocolException, IOException {
		ContentResolver resolver = callback.getContext().getContentResolver();
		long lawId = law.getId();
		if (DbInitaliser.CODE_VERFASSUNG.equals(law.getCode())) {
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			long p = insert(resolver, new EntriesModel(lawId, -1, "Pr�ambel", "Pr�ambel", null, 0));
			insert(resolver, new EntriesModel(lawId, p, "Pr�ambel", "Pr�ambel", "Im Namen Gottes des Allm�chtigen!\n" +
					"\n" +
					"Das Schweizervolk und die Kantone,\n" +
					"\n" +
					"in der Verantwortung gegen�ber der Sch�pfung,\n" +
					"\n" +
					"im Bestreben, den Bund zu erneuern, um Freiheit und Demokratie, Unabh�ngigkeit und Frieden in Solidarit�t und Offenheit gegen�ber der Welt zu st�rken,\n" +
					"\n" +
					"im Willen, in gegenseitiger R�cksichtnahme und Achtung ihre Vielfalt in der Einheit zu leben,\n" +
					"\n" +
					"im Bewusstsein der gemeinsamen Errungenschaften und der Verantwortung gegen�ber den k�nftigen Generationen,\n" +
					"\n" +
					"gewiss, dass frei nur ist, wer seine Freiheit gebraucht, und dass die St�rke des Volkes sich misst am Wohl der Schwachen,\n" +
					"\n" +
					"geben sich folgende Verfassung:", 0));
			long t1 = insert(resolver, new EntriesModel(lawId, -1, "1. Titel", "1. Titel: Allgemeine Bestimmungen", null, 1));
			long t2 = insert(resolver, new EntriesModel(lawId, -1, "2. Titel",
					"2. Titel: Grundrechte, B�rgerrechte und Sozialziele",
					"ein text", 2));
			long t2_k1 = insert(resolver, new EntriesModel(lawId, t2, "2. Titel 1. Kapitel", "1. Kapitel: Grundrechte", null, 3));
			long t2_k2 = insert(resolver, new EntriesModel(lawId, t2, "2. Titel 2. Kapitel", "2. Kapitel: B�rgerrecht und politische Rechte",
					null, 4));
			long t2_k3 = insert(resolver, new EntriesModel(lawId, t2, "2. Titel 3. Kapitel", "3. Kapitel: Sozialziele", null, 5));
			long t3 = insert(resolver, new EntriesModel(lawId, -1, "3. Titel", "3. Titel: Bund, Kantone und Gemeinden", "text", 6));
			long t3_k1 = insert(resolver, new EntriesModel(lawId, t3, "3. Titel 1. Kapitel", "1. Kapitel: Verh�ltnis von Bund und Kantonen",
					null, 7));
			long t3_k2 = insert(resolver, new EntriesModel(lawId, t3, "3. Titel 2. Kapitel", "2. Kapitel: Zust�ndigkeiten", null, 8));
		}

		if (DbInitaliser.CODE_OR.equals(law.getCode())) {
			//Daten Auslesen TODO Dynmaisch Programmieren.
			WebParser lawData = new WebParser();
			lawData.getText("http://www.admin.ch/ch/d/sr/220/index.html");
			resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
			int i = 0;
			while (i < lawData.data.size()) {
				long p = insert(resolver, new EntriesModel(lawId, -1, lawData.data.get(i).getShortText(), lawData.data.get(i).getText(), null, 0));
				WebParser lawDataSecondLevel = new WebParser();
				lawDataSecondLevel.getText("http://www.admin.ch/ch/d/sr/220/" + lawData.data.get(i).getLink());
				int x = 0;

				while (x < lawDataSecondLevel.data.size()) {
					if (lawDataSecondLevel.data.get(x).getId() == "ArtikelText") {
						insert(resolver, new EntriesModel(lawId, p, lawData.data.get(i).getShortText(), lawData.data.get(i).getText(), lawDataSecondLevel.data.get(x).getText(), 0));
					}
					{
						long l2 = insert(resolver,
								new EntriesModel(lawId, p, lawDataSecondLevel.data.get(x).getShortText(), lawDataSecondLevel.data.get(x).getShortText(), null, 1));
						WebParser lawDataThirdLevel = new WebParser();
						lawDataThirdLevel.getText("http://www.admin.ch/ch/d/sr/220/" + lawDataSecondLevel.data.get(x).getLink());
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
