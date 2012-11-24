package ch.bedesign.android.law.access;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.TimingLogger;
import ch.bedesign.android.law.log.Logger;

public class Parser implements IParser {

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

	private final Context context;
	private final String urlText;
	ArrayList<LineInfo> data = new ArrayList<Parser.LineInfo>();

	public Parser(Context context, String urlText) {
		super();
		this.context = context;
		this.urlText = urlText;
	}


	/* (non-Javadoc)
	 * @see ch.bedesign.android.law.access.IParser#parse()
	 */
	public void parse() throws IOException {
		Logger.v("Parse law from " + urlText);
		TimingLogger timingLogger = new TimingLogger(Logger.TAG, "Parser");
		UrlLoader loader = new UrlLoader(context);

		try {
			InputStream in = new BufferedInputStream(loader.getLawStream(urlText));
			timingLogger.addSplit("Load");
			if (urlText.indexOf("index") > 0) {
				readStream(in);
			}
			else {
				readArticleStream(in);
			}
		} finally {
			loader.finish();
		}
		timingLogger.addSplit("parse");
		timingLogger.dumpToLog();

	}

	private String readArticleStream(InputStream is) throws IOException {
		BufferedReader reader = null;
		try {
			//-BufferReader mit InputStream erstellen 
			reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"));

			//-StringBuilder erstellen 
			StringBuilder sbDetail = new StringBuilder();

			//-Abbruchbedingung 
			String line = null;
			int ArtTextRecord = 0;

			//-While-Schleife ausführen, bis man in der letzten Zeile des BufferReader angekommen ist 
			while ((line = reader.readLine()) != null) {

				if (line.contains("<div id=\"spalteContentPlus\">")) {
					ArtTextRecord = 1;
				} else if (ArtTextRecord == 1) {
					if (line.contains("<div")) {
						sbDetail.append(line);
						ArtTextRecord = 0;
					} else {
						sbDetail.append(line + "\n");
					}
				}
			}

			//clean up string
			String article = sbDetail.toString();
			int idx = article.indexOf("<H5>");
			if (idx > 0 && idx < article.length()) {
				article = article.substring(idx);
			}

			data.add(new LineInfo("ArtikelText", "", article, ""));

			return article;

		} finally {
			try {
				if (is == null) {
					is.close();
				}
				if (reader == null) {
					reader.close();
				}

				//-Exception abfangen        
			} catch (IOException e) {
				// interssiert niemand
				//e.printStackTrace();

			}
		}
	}

	private String readStream(InputStream is) throws IOException {
		BufferedReader reader = null;
		try {
			//-BufferReader mit InputStream erstellen 
			reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"));

			//-StringBuilder erstellen 
			StringBuilder sbDetail = new StringBuilder();

			//-Abbruchbedingung 
			String line = null;
			String cleanLineText = null;
			String cleanLineShortText = null;
			String cleanLineID = null;
			String cleanLineLink = null;

			//-While-Schleife ausführen, bis man in der letzten Zeile des BufferReader angekommen ist 
			while ((line = reader.readLine()) != null) {

				if (line.contains("<a name=\"id")) {
					cleanLineText = line.substring(line.indexOf("M(this)\">") + 9, line.lastIndexOf("</a>"));
					/*		if (cleanLineText.contains("<b>")) {
								cleanLineShortText = cleanLineText.substring(cleanLineText.indexOf("<b>") + 3, cleanLineText.indexOf("</b>"));
								cleanLineText = cleanLineText.substring(cleanLineText.indexOf("</b>") + 4);
							} else if (cleanLineText.contains("<B>")) {
								cleanLineShortText = cleanLineText.substring(cleanLineText.indexOf("<B>") + 3, cleanLineText.indexOf("</B>"));
								cleanLineText = cleanLineText.substring(cleanLineText.indexOf("</B>") + 4);
							}*/
					cleanLineID = line.substring(line.indexOf("name=") + 6, line.indexOf("href") - 2);
					cleanLineLink = line.substring(line.indexOf("href=") + 8, line.indexOf("html") + 4);
					data.add(new LineInfo(cleanLineID, cleanLineLink, "", /*cleanLineShortText*/cleanLineText));
				}
			}

			//-Exception abfangen     
			//-String zurückgeben 
			return sbDetail.toString();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				// interssiert niemand
			}
		}
	}

	/* (non-Javadoc)
	 * @see ch.bedesign.android.law.access.IParser#getLawVersion()
	 */
	public String getLawVersion() {
		UrlLoader loader = new UrlLoader(context);
		InputStream is = null;
		BufferedReader reader = null;
		try {
			is = new BufferedInputStream(loader.getLawStream(urlText, false));
			reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"));

			String line = null;
			String lawVersion = null;

			while ((line = reader.readLine()) != null) {
				if (line.contains("<div class='Stand'>")) {
					lawVersion = line.substring(line.indexOf("<i>") + 3, line.indexOf("</i>"));
					return lawVersion;
				}
			}

		} catch (IOException e) {
			Logger.e("error parsing", e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (reader != null) {
					reader.close();
				}
				if (loader != null) {
					loader.finish();
				}
			} catch (IOException e) {
				// interssiert niemand
			}
		}
		return DateFormat.getLongDateFormat(context).format(System.currentTimeMillis());
	}
}
