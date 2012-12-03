package ch.bedesign.android.law.access;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import ch.bedesign.android.law.db.DB;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.db.DB.Laws;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.EntriesModel;
import ch.bedesign.android.law.model.LawModel;

public class LawUpdater extends AsyncTask<Long, Object, Object> {
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
	private static final long UPDATE_INTERVALL_MILLIES = 10; //1000 * 60 * 60 * 24 * 7; Klein gehalten um zu testen SoupParser funktioniert ansonsten nicht (Da LastCheck noch nicht richtig implementiert ist todo Muriel) // once a week

	private final Context ctx;

	public LawUpdater(Context ctx) {
		super();
		this.ctx = ctx.getApplicationContext();
	}

	@Override
	protected Object doInBackground(Long... lawIds) {
		if (lawIds.length < 1) {
			return null;
		}
		for (Long lawId : lawIds) {
			if (lawId != null) {
				ContentResolver resolver = ctx.getContentResolver();
				Cursor c = null;
				try {
					c = resolver.query(Laws.CONTENT_URI, Laws.PROJECTION_DEFAULT, DB.SELECTION_BY_ID, new String[] { Long.toString(lawId) },
							Laws.SORTORDER_DEFAULT);
					if (c != null && c.moveToFirst()) {
						updateLaw(resolver, c);
					}
				} catch (Exception e) {
					Logger.w("Cannot update law", e);
				} finally {
					if (c != null && !c.isClosed()) {
						c.close();
					}
				}
			}
		}
		return null;
	}

	private void updateLaw(ContentResolver resolver, Cursor c) throws ClientProtocolException, IOException {
		LawModel law = new LawModel(c);
		String urlText = law.getUrl();
		String lawVersion = getLawVersion(urlText);
		long now = System.currentTimeMillis();
		try {
			if (now < law.getLastCheck() + UPDATE_INTERVALL_MILLIES) {
				Logger.i("Not updating since the law is too new");
				return;
			}

		} catch (Exception e) {
			// we do not care
		}
		try {
			if (law.getVersion() != null && !law.getVersion().equals(lawVersion)) {
				law.setIsUpdating(now);
				resolver.update(Laws.CONTENT_URI, law.getValues(), DB.SELECTION_BY_ID, new String[] { Long.toString(law.getId()) });

				Logger.i("Parsing law " + law.getName() + " to version " + lawVersion);
				long start = now;
				resolver.delete(Entries.CONTENT_URI, Entries.SELECTION_LAW, new String[] { Long.toString(law.getId()) });
				insertLawText(urlText, resolver, law);

				Logger.i("Finished parsing law " + law.getName() + " to version " + lawVersion + " in " + sdf.format(now - start));
				law.setVersion(lawVersion);
			}
			law.setLastCheck(now);
		} finally {
			law.setIsUpdating(-1);
		}
		resolver.update(Laws.CONTENT_URI, law.getValues(), Laws.SELECTION_CODE, new String[] { law.getCode() });
	}


	private void insertLawText(String urlText, ContentResolver resolver, LawModel law) throws ClientProtocolException, IOException {
		long lawId = law.getId();
		String SrNr = law.getCode();
		Logger.i("Loading law " + law.getCode());
		parse(urlText, lawId, resolver);
	}

	private long insert(ContentResolver resolver, EntriesModel entriesModel) {
		Uri uri = resolver.insert(Entries.CONTENT_URI, entriesModel.getValues());
		return ContentUris.parseId(uri);
	}

	public static void loadLaw(Context ctx, Long... lawIds) {
		LawUpdater task = new LawUpdater(ctx);
		task.execute(lawIds);
	}

	public void parse(String urlText, long lawId, ContentResolver resolver) throws IOException {
		Logger.v("Parse law from " + urlText);
		// Entries Model (ID (auto increment), Gesetz ID, Parent Id, url, Name , Kurztext, Fullname, Text(Artikel selbst), sequence (long))
		try {
			Document doc = Jsoup.connect(urlText).timeout(10000).get();
			Elements links = doc.select("A[NAME]");
			long pt = -1;
			for (Element link : links) {
				if (link.attr("name").contains("id")) {
					if (link.attr("abs:href") == "") {
						pt = insert(resolver, new EntriesModel(lawId, -1, "", link.nextElementSibling().toString(), "", link.nextElementSibling().toString(), "", 0));
					} else if (link.attr("abs:href").contains("index")) {
						long p = insert(resolver, new EntriesModel(lawId, pt, link.attr("abs:href"), link.text(), "", link.text(), "", 0));
							Document subdoc = Jsoup.connect(link.attr("abs:href")).timeout(10000).get();
							Elements subLinks = subdoc.select("A[Name]");
							for (Element subLink : subLinks) {
							if (subLink.attr("abs:href") == "") {
									// Entries Model (Gesetz ID, Parent Id, Name Gesetz???, Kurztext, Fullname, Text(Artikel selbst), sequence (long))
								long p2 = insert(resolver, new EntriesModel(lawId, p, "", subLink.nextElementSibling().toString(), "", subLink.nextElementSibling().toString(), "",
										0));
								} else {
								long p2 = insert(resolver, new EntriesModel(lawId, p, subLink.attr("abs:href"), "", subLink.select("B").text(), subLink.text(), "", 0));
									EntriesModel entrie = parseArticleText(subLink.attr("abs:href"), lawId, p2);
									if (entrie != null) {
										insert(resolver, entrie);
									}
								}
							}
					} else {
						EntriesModel e = new EntriesModel(lawId, -1, link.attr("abs:href"), "", link.text(), "", null, 0);
						long p = insert(resolver, e);
						EntriesModel entrie = parseArticleText(link.attr("abs:href"), lawId, p);
						if (entrie != null) {
							insert(resolver, entrie);
						}
					}

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.w("Can not parse site", e);
		}
		//timingLogger.addSplit("parse");
		//timingLogger.dumpToLog();
	}

	public EntriesModel parseArticleText(String urlText, long lawId, long p2) throws IOException {
		Logger.v("Parse law from " + urlText);
		EntriesModel entrie = null;
		try {
			Document doc = Jsoup.connect(urlText).timeout(10000).get();
			Elements artikels = doc.select("div[id$=spalteContentPlus]");
			for (Element artikel : artikels) {
				String name = artikel.select("h5").text();
				String text = "";
				Elements arts = artikel.select("p");
				for (Element art : arts) {
					text = text + "<br>" + art.text();
				}
				text = text.substring(4);
				entrie = new EntriesModel(lawId, p2, "", name, "", name, text, 0);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.w("Can not parse site", e);
		}
		//timingLogger.addSplit("parse");
		//timingLogger.dumpToLog();
		return entrie;
	}
	
	private String getLawVersion(String urlText) {
		String lawVersion = null;
		try {
			Document doc = Jsoup.connect(urlText).timeout(10000).get();
			Elements Versions = doc.select("div[class$=Stand]");
			for (Element Version : Versions) {
				Elements vs = Version.select("i");
				for (Element v : vs) {
					lawVersion = v.text();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			Logger.w("Can not parse site", e);
		}

		return lawVersion;
	}

}
