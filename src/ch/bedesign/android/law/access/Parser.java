package ch.bedesign.android.law.access;

import java.io.IOException;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import ch.bedesign.android.law.db.DB.Entries;
import ch.bedesign.android.law.helper.SettingsLaw;
import ch.bedesign.android.law.log.Logger;
import ch.bedesign.android.law.model.EntriesModel;
import ch.bedesign.android.law.model.LawModel;

public class Parser {

	private final ContentResolver resolver;
	private final long lawId;
	private final LawModel law;
	private final UrlLoader urlLoader;
	private final String lang;
	private final String basicUrl;
	private String langPartUrl;

	public Parser(Context ctx, LawModel law) {
		super();
		this.resolver = ctx.getContentResolver();
		this.lawId = law.getId();
		this.law = law;
		this.lang = new SettingsLaw(ctx).getLanguage();
		this.basicUrl = "http://www.admin.ch/ch/";
		this.langPartUrl = "d/sr";
		urlLoader = new UrlLoader(ctx, law.getCode());
	}

	private Document getDocument(String urlText) throws IOException {
		Logger.i("Parsing Document: " + urlText);
		return Jsoup.parse(urlLoader.getFile(urlText), null, urlText);
	}

	private long insert(EntriesModel entriesModel) {
		Uri uri = resolver.insert(Entries.CONTENT_URI, entriesModel.getValues());
		return ContentUris.parseId(uri);
	}

	private String getUrlText() {
		String urlText = law.getUrl();
		Locale langLocale = Locale.getDefault();
		String systemLang = langLocale.getLanguage();
		String userLang = "";

		if (systemLang == lang) {
			userLang = lang;
		} else {
			if (systemLang.contains("de") || systemLang == "it" || systemLang == "fr") {
				userLang = systemLang;
			} else {
				userLang = lang;
			}
		}

		if (userLang.contains("de")) {
			langPartUrl = "d/sr";
		} else if (userLang.contains("fr")) {
			langPartUrl = "f/rs";
		} else if (userLang.contains("it")) {
			langPartUrl = "i/rs";
		}

		String urlTextnew = basicUrl + langPartUrl + urlText;

		return urlTextnew;
	}

	public void parse() throws IOException {
		String urlText = getUrlText();
		Logger.i("Parse law from " + urlText);
		// Entries Model (ID (auto increment), Gesetz ID, Parent Id, url, Name , Kurztext, Fullname, Text(Artikel selbst), sequence (long))
		Document doc = getDocument(urlText);
		Elements links = doc.select("A[NAME]");
		long parentIdFirstLevel = -1;

		long level1 = -1;
		long level2 = 0;
		long level3 = 0;
		long level4 = 0;

		long subLevel1 = 0;
		long subLevel2 = 0;
		long subLevel3 = 0;
		long subLevel4 = 0;

		for (Element link : links) {
			if (link.attr("name").contains("id")) {
				if (link.attr("abs:href") == "") {
					EntriesModel em = new EntriesModel(lawId, -1);
					em.setName(link.nextElementSibling().text().trim());
					em.setShortName("");
					em.setFullName(law.getShortName());
					// find right Parent_id
					String elementName = link.attr("Name");
					elementName = elementName.replace("id-", "");

					int matches = countMatches(elementName, "-");

					if (matches == 0) {
						em.setParentId(level1);
						parentIdFirstLevel = insert(em);
						level2 = parentIdFirstLevel;
					} else if (matches == 1) {
						em.setParentId(level2);
						parentIdFirstLevel = insert(em);
						level3 = parentIdFirstLevel;
					} else if (matches == 2) {
						em.setParentId(level3);
						parentIdFirstLevel = insert(em);
						level4 = parentIdFirstLevel;
					} else if (matches == 3) {
						em.setParentId(level4);
						parentIdFirstLevel = insert(em);
						//level4 = parentIdFirstLevel;
					}

					if (link.attr("name").contains("id-final")) {
						Element end = link.parent();
						String endText = end.html();
						int start = endText.indexOf("/h4");
						endText = endText.substring(start + 5);
						EntriesModel emEnd = new EntriesModel(lawId, parentIdFirstLevel);
						emEnd.setName(link.nextElementSibling().text().trim());
						emEnd.setShortName("");
						emEnd.setFullName(law.getShortName());
						emEnd.setText(endText);
						insert(emEnd);
					}

				} else if (link.attr("abs:href").contains("index")) {
					EntriesModel em = new EntriesModel(lawId, parentIdFirstLevel);
					em.setUrl(link.attr("abs:href"));
					em.setName(link.text());
					em.setShortName("");
					em.setFullName(law.getShortName());
					long parentIdSecondLevel = insert(em);
					Document subdoc = getDocument(link.attr("abs:href"));
					Elements subLinks = subdoc.select("A[Name]");
					long parentIdThirdLevel = parentIdSecondLevel;
					subLevel1 = parentIdSecondLevel;
					for (Element subLink : subLinks) {
						if (subLink.attr("name").contains("id")) {
							if (subLink.attr("abs:href") == "") {
								// Entries Model (Gesetz ID, Parent Id,url, Name Gesetz???, Kurztext, Fullname, Text(Artikel selbst), sequence (long))
								EntriesModel emThirdLevel = new EntriesModel(lawId, parentIdSecondLevel);
								emThirdLevel.setName(subLink.nextElementSibling().text().trim());
								emThirdLevel.setShortName("");
								emThirdLevel.setFullName(law.getShortName() + "/" + link.text());

								// find right Parent_id
								String subElementName = subLink.attr("Name");
								subElementName = subElementName.replace("id-", "");

								int matches = countMatches(subElementName, "-");

								if (matches == 1) {
									emThirdLevel.setParentId(subLevel1);
									parentIdThirdLevel = insert(emThirdLevel);
									subLevel2 = parentIdThirdLevel;
								} else if (matches == 2) {
									emThirdLevel.setParentId(subLevel2);
									parentIdThirdLevel = insert(emThirdLevel);
									subLevel3 = parentIdThirdLevel;
								} else if (matches == 3) {
									emThirdLevel.setParentId(subLevel3);
									parentIdThirdLevel = insert(emThirdLevel);
									subLevel4 = parentIdThirdLevel;
								}

							} else {
								EntriesModel emThirdLevel = new EntriesModel(lawId, parentIdThirdLevel);
								emThirdLevel.setUrl(subLink.attr("abs:href"));
								String name = subLink.text().substring(subLink.select("B").text().length());
								emThirdLevel.setName(name);
								emThirdLevel.setShortName(subLink.select("B").text());
								emThirdLevel.setFullName(law.getShortName() + "/" + link.text());
								long parentIdFourthLevel = insert(emThirdLevel);
								EntriesModel entrie = parseArticleText(subLink.attr("abs:href"), parentIdFourthLevel);
								if (entrie != null) {
									entrie.setFullName(law.getShortName() + "/" + link.text() + "/" + name);
									entrie.setShortName("");
									insert(entrie);
								}
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
						entrie.setShortName("");
						insert(entrie);
					}
				}
			}
		}

	}

	public EntriesModel parseArticleText(String urlText, long parentId) throws IOException {
		Logger.v("Parse law from " + urlText);
		EntriesModel entrie = null;
		Document doc = getDocument(urlText);
			Elements artikels = doc.select("div[id$=spalteContentPlus]");
			for (Element artikel : artikels) {
				String name = artikel.select("h5").text();
				String text = "";
			//Elements arts = artikel.select("p");
			//for (Element art : arts) {
			//text = text + "<br><br>" + art.html();
			//}
			//text = text.substring(8);
			text = artikel.html();
			int posStart = text.indexOf("</h5>");
			text = text.substring(posStart + 5, text.length() - 6);
			text = text.replaceAll("<dl compact='compact'>", "");
			text = text.replaceAll("</dl>", "");
			text = text.replaceAll("</dt>", "");
			text = text.replaceAll("<dt>", "");
			text = text.replaceAll("<dd>", "");
			text = text.replaceAll("</dd>", "<br><br>");

				entrie = new EntriesModel(lawId, parentId, "", name, "", "", text, 0);
			}
		return entrie;
	}

	public String getLawVersion() {
		String urlText = getUrlText();
		String lawVersion = null;
		try {
			Document doc = getDocument(urlText);
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

	private static int countMatches(String source, String part)
	{
		if (source == null || part == null) {
			return 0;
		}
		int count = 0;

		for (int pos = 0; (pos = source.indexOf(part, pos)) != -1; count++)
			pos += part.length();

		return count;
	}

}