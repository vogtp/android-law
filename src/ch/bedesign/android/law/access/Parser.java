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
		Logger.i("Parse law from " + urlText);
		// Entries Model (ID (auto increment), Gesetz ID, Parent Id, url, Name , Kurztext, Fullname, Text(Artikel selbst), sequence (long))
		try {
			Document doc = Jsoup.connect(urlText).timeout(10000).get();
			Elements links = doc.select("A[NAME]");
			long parentIdFirstLevel = -1;
			for (Element link : links) {
				if (link.attr("name").contains("id")) {
					if (link.attr("abs:href") == "") {
						EntriesModel em = new EntriesModel(lawId, -1);
						//em.setUrl(link.nextElementSibling().toString());
						em.setName(link.nextElementSibling().toString());
						em.setFullName(law.getShortName());//TODO fix me
						parentIdFirstLevel = insert(em);
					} else if (link.attr("abs:href").contains("index")) {
						EntriesModel em = new EntriesModel(lawId, parentIdFirstLevel);
						em.setUrl(link.attr("abs:href"));
						em.setName(link.text());
						em.setFullName(law.getShortName());
						long parentIdSecondLevel = insert(em);
						Document subdoc = Jsoup.connect(link.attr("abs:href")).timeout(10000).get();
						Elements subLinks = subdoc.select("A[Name]");
						for (Element subLink : subLinks) {
							if (subLink.attr("abs:href") == "") {
								// Entries Model (Gesetz ID, Parent Id,url, Name Gesetz???, Kurztext, Fullname, Text(Artikel selbst), sequence (long))
								EntriesModel emThirdLevel = new EntriesModel(lawId, parentIdSecondLevel);
								emThirdLevel.setName(subLink.nextElementSibling().toString());
								emThirdLevel.setFullName(law.getShortName() + "/" + link.text());
								long parentIdThirdLevel = insert(emThirdLevel);
							} else {
								EntriesModel emThirdLevel = new EntriesModel(lawId, parentIdSecondLevel);
								emThirdLevel.setUrl(subLink.attr("abs:href"));
								String name = subLink.text().substring(subLink.select("B").text().length());
								emThirdLevel.setName(name);
								emThirdLevel.setShortName(subLink.select("B").text());
								emThirdLevel.setFullName(law.getShortName() + "/" + link.text());
								long parentIdThirdLevel = insert(emThirdLevel);
								EntriesModel entrie = parseArticleText(subLink.attr("abs:href"), parentIdThirdLevel);
								if (entrie != null) {
									entrie.setFullName(law.getShortName() + "/" + link.text() + "/" + name);
									insert(entrie);
								}
							}
						}
					} else {
						EntriesModel em = new EntriesModel(lawId, parentIdFirstLevel);
						em.setUrl(link.attr("abs:href"));
						String name = link.text().substring(link.select("B").text().length());
						em.setName(name);
						em.setShortName(link.select("B").text());
						em.setFullName(law.getShortName());
						long ParentIdSecondLevel = insert(em);
						EntriesModel entrie = parseArticleText(link.attr("abs:href"), ParentIdSecondLevel);
						if (entrie != null) {
							entrie.setFullName(law.getShortName() + "/" + name);
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

	public EntriesModel parseArticleText(String urlText, long parentId) throws IOException {
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
				entrie = new EntriesModel(lawId, parentId, "", name, "", "", text, 0);
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
