package ch.bedesign.android.law.access;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ch.bedesign.android.law.log.Logger;

public class parser {

	public static void main(String args[]) {

		parser r = new parser();

	}

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

	ArrayList<LineInfo> data = new ArrayList<parser.LineInfo>();

	public void getText(String UrlText) throws IOException {
		URL url;
		url = new URL(UrlText);

		   HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		   try {
		     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				if(UrlText.indexOf("index") > 0){
					readStream(in);
				}else{
					readArticleStream(in);
				}    
		} finally {
		     urlConnection.disconnect();
		   }
		
	}

	private String readArticleStream(InputStream is) {
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
			article = article.substring(article.indexOf("<H5>"));

			String cleanLineText = article;
			data.add(new LineInfo("ArtikelText", "", cleanLineText, ""));

			//-Exception abfangen     
			//-String zurückgeben 
			return article;

		} catch (IOException e) {
			Logger.e("error parsing", e);
			//	e.printStackTrace();

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
		return null;

	}

	private String readStream(InputStream is) {
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
					if (cleanLineText.contains("<b>")) {
						cleanLineShortText = cleanLineText.substring(cleanLineText.indexOf("<b>") + 3, cleanLineText.indexOf("</b>"));
						cleanLineText = cleanLineText.substring(cleanLineText.indexOf("</b>") + 4);
					} else if (cleanLineText.contains("<B>")) {
						cleanLineShortText = cleanLineText.substring(cleanLineText.indexOf("<B>") + 3, cleanLineText.indexOf("</B>"));
						cleanLineText = cleanLineText.substring(cleanLineText.indexOf("</B>") + 4);
					}
					cleanLineID = line.substring(line.indexOf("name=") + 6, line.indexOf("href") - 2);
					cleanLineLink = line.substring(line.indexOf("href=") + 8, line.indexOf("html") + 4);
					data.add(new LineInfo(cleanLineID, cleanLineLink, cleanLineText, cleanLineShortText));
				}
			}

			//-Exception abfangen     
			//-String zurückgeben 
			return sbDetail.toString();
		} catch (IOException e) {
			Logger.e("error parsing", e);
			//	e.printStackTrace();

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
		return null;
	}

	public String getLawVersion() {
		// TODO Auto-generated method stub
		return null;
	}
}
