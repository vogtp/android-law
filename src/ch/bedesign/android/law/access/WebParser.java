package ch.bedesign.android.law.access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import ch.bedesign.android.law.log.Logger;

public class WebParser {

	class LineInfo {
		private String id; //type long?
		private String link; // type URL?
		private String text;

		public LineInfo(String id, String link, String text) {
			super();
			this.id = id;
			this.link = link;
			this.text = text;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
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

	ArrayList<LineInfo> data = new ArrayList<WebParser.LineInfo>();


	//-URL laden 
	public String getText(String url) throws ClientProtocolException, IOException {

		//-String für Rückgabe 
		String result = null;

		//-HTTPClient erstellen 
		HttpClient httpclient = new DefaultHttpClient();

		//-HHTPGet erstellen 
		HttpGet httpget = new HttpGet(url);

		//-HTTPResponse erstellen 
		HttpResponse response;

		//-Anforderung starten 

		//-Antwort von HTTPClient mit HTTPGet 
		response = httpclient.execute(httpget);

		//-HTTPEntity auf Antwort ausführen 
		HttpEntity entity = response.getEntity();

		//-Wenn es eine antwort gibt 
		if (entity != null) {

			//-Antwort in InputStream speichern 
			InputStream instream = entity.getContent();

			//-InputStream in String umwandeln 
			result = convertStreamToString(instream);

			//-InputStream schließen 
			instream.close();

		}

		return result;

	}

	//-InputStream in String convertieren 
	private String convertStreamToString(InputStream is) {

		//-BufferReader mit InputStream erstellen 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		//-StringBuilder erstellen 
		StringBuilder sb = new StringBuilder();
		StringBuilder sbDetail = new StringBuilder();

		//-Abbruchbedingung 
		String line = null;
		String cleanLineText = null;
		String cleanLineID = null;
		String cleanLineLink = null;

		try {

			//-While-Schleife ausführen, bis man in der letzten Zeile des BufferReader angekommen ist 
			while ((line = reader.readLine()) != null) {

				if (line.contains("<a name=\"id")) {
					cleanLineText = line.substring(line.indexOf("M(this)\">") + 9, line.lastIndexOf("</a>"));
					cleanLineID = line.substring(line.indexOf("name=") + 6, line.indexOf("href") - 2);
					cleanLineLink = line.substring(line.indexOf("href=") + 8, line.indexOf("html") + 4);
					data.add(new LineInfo(cleanLineID, cleanLineLink, cleanLineText));

					sbDetail.append(cleanLineLink + "\n");
				}
				//-Neue Zeile an String anhängen 
				sb.append(line + "\n");

			}

			//-Exception abfangen     
		} catch (IOException e) {
			Logger.e("error parsing", e);
			//	e.printStackTrace();

		} finally {
			try {
				is.close();

				//-Exception abfangen        
			} catch (IOException e) {

				e.printStackTrace();

			}
		}

		//-String zurückgeben 
		return sbDetail.toString();
	}

}
