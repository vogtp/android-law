package ch.bedesign.android.law.access;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import ch.bedesign.android.law.log.Logger;

public class SoupParser {

	private final Context context;
	private final String urlText;
	ArrayList<LineInfo> data = new ArrayList<SoupParser.LineInfo>();

	public SoupParser(Context context, String urlText) {
		super();
		this.context = context;
		this.urlText = urlText;
	}

	//FIXME use EntityModel instead
	class LineInfo {
		private String id; //type long?
		private String link; // type URL?
		private String text;
		private String shortText;

		public LineInfo(String id, String link, String text, String shortText) {
			super();
			this.id = id;
			this.link = link;
			this.text = text;
			this.shortText = shortText;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getShortText() {
			return shortText;
		}

		public void setShortText(String shortText) {
			this.shortText = shortText;
		}

		public String getLink() {
			return link;
		}

		public void setLink(String link) {
			this.link = link;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	public void parseWithJsoup() throws IOException {
		Logger.v("Parse law from " + urlText);
		//TimingLogger timingLogger = new TimingLogger(Logger.TAG, "Parser");
		//UrlLoader loader = new UrlLoader(context);

		try {
			Document doc = Jsoup.connect(urlText).timeout(10000).get();
			Elements links = doc.select("A[NAME]");
			//timingLogger.addSplit("Load");
			for (Element link : links) {
				if (link.attr("name").contains("id")) {
					if (link.attr("abs:href") == "") {
						data.add(new LineInfo(link.attr("name"), "", link.nextElementSibling().toString(), ""));

					} else if (link.attr("abs:href").contains("index")) {
						data.add(new LineInfo(link.attr("name"), link.attr("abs:href"), link.text(), ""));
						Document subDoc = Jsoup.connect(link.attr("abs:href")).timeout(10000).get();
						Elements subLinks = subDoc.select("A[NAME]");
						for (Element subLink : subLinks) {
							data.add(new LineInfo(subLink.attr("name"), subLink.attr("abs:href"), subLink.text(), ""));
							Document subSubDoc = Jsoup.connect(link.attr("abs:href")).get();
							Elements subSubLinks = subSubDoc.select("div[id$=spalteContentPlus]");
							for (Element subSubLink : subSubLinks) {
								data.add(new LineInfo("ArtikelText", "", subSubLink.text(), ""));
							}
						}
					}
					else {
						data.add(new LineInfo(link.attr("name"), link.attr("abs:href"), link.text(), ""));

						Document subDoc = Jsoup.connect(link.attr("abs:href")).timeout(10000).get();
						Elements subLinks = subDoc.select("div[id$=spalteContentPlus]");
						for (Element subLink : subLinks) {
							data.add(new LineInfo("ArtikelText", "", subLink.text(), ""));
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

}

