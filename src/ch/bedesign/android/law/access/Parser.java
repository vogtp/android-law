package ch.bedesign.android.law.access;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.EntriesModel;
import ch.bedesign.android.law.model.LawModel;

public class Parser {

	private final ContentResolver resolver;
	private final long lawId;
	private final LawModel law;

	public Parser(ContentResolver resolver, LawModel law) {
		super();
		this.resolver = resolver;
		this.lawId = law.getId();
		this.law = law;
	}

	private long insert(EntriesModel entriesModel) {
		Uri uri = resolver.insert(Entries.CONTENT_URI, entriesModel.getValues());
		return ContentUris.parseId(uri);
	}

	public void parse() throws IOException {
		String urlText = law.getUrl();
		Logger.v("Parse law from " + urlText);
		// Entries Model (ID (auto increment), Gesetz ID, Parent Id, url, Name , Kurztext, Fullname, Text(Artikel selbst), sequence (long))
		try {
			Document doc = Jsoup.connect(urlText).timeout(10000).get();
			Elements links = doc.select("A[NAME]");
			long pt = -1;
			for (Element link : links) {
				if (link.attr("name").contains("id")) {
					if (link.attr("abs:href") == "") {
						EntriesModel em = new EntriesModel(lawId, -1);
						em.setUrl(link.nextElementSibling().toString());
						em.setName(link.nextElementSibling().toString());
						em.setFullName("PFAD");//TODO fix me
						pt = insert(em);
					} else if (link.attr("abs:href").contains("index")) {
						long p = insert(new EntriesModel(lawId, pt, link.attr("abs:href"), link.text(), "", link.text(), "", 0));
						Document subdoc = Jsoup.connect(link.attr("abs:href")).timeout(10000).get();
						Elements subLinks = subdoc.select("A[Name]");
						for (Element subLink : subLinks) {
							if (subLink.attr("abs:href") == "") {
								// Entries Model (Gesetz ID, Parent Id, Name Gesetz???, Kurztext, Fullname, Text(Artikel selbst), sequence (long))
								long p2 = insert(new EntriesModel(lawId, p, "", subLink.nextElementSibling().toString(), "", subLink.nextElementSibling().toString(), "",
										0));
							} else {
								long p2 = insert(new EntriesModel(lawId, p, subLink.attr("abs:href"), subLink.text().substring(
										subLink.select("B").text().length()), subLink.select("B").text(), "PFAD", "", 0));
								EntriesModel entrie = parseArticleText(subLink.attr("abs:href"), p2);
								if (entrie != null) {
									insert(entrie);
								}
							}
						}
					} else {
						EntriesModel e = new EntriesModel(lawId, -1, link.attr("abs:href"), "", link.text(), "", null, 0);
						long p = insert(e);
						EntriesModel entrie = parseArticleText(link.attr("abs:href"), p);
						if (entrie != null) {
							insert(entrie);
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

	public EntriesModel parseArticleText(String urlText, long p2) throws IOException {
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

	public String getLawVersion() {
		String urlText = law.getUrl();
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
