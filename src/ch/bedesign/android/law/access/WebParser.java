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

	public static void main(String args[]){
		
		WebParser r = new WebParser();
		r.getNewLaw(220);
		
		
	}
	
	//TODO  Class HTML ansehen! evt relevant f�r Parser
	
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
	
	ArrayList<LineInfo> data = new ArrayList<WebParser.LineInfo>();

	public void getNewLaw(int SrNr){
		String FixUrlGerman = "http://www.admin.ch/ch/d/sr/";
		String url = FixUrlGerman+SrNr+"/index.html";
		
		try {
			WebParser result = new WebParser();
			
			String r = result.getText(url);
			int i = 0;
			String s = "";
			
			while(i < result.data.size()){
				/*
				 * s = s + result.data.get(i).getId() + ", " + result.data.get(i).getLink() + ", " + result.data.get(i).getText()+"\n" ;
				
				url = FixUrlGerman+SrNr+ "/" + result.data.get(i).getLink();
				WebParser secondResult = new WebParser();
				secondResult.getText(url);
				int t = 0;
				String s2= "";
				while(t < secondResult.data.size()){
					s2 = s2 + secondResult.data.get(t).getId() + ", " + secondResult.data.get(t).getLink() + ", " + secondResult.data.get(t).getText()+"\n" ;
					t++;
				}
				s = s + "\n ----- \n" + s2  + "\n ----- \n";*/
				
				s = s+result.data.get(i).getShortText() + ", " + result.data.get(i).getText();
				url = FixUrlGerman+SrNr+ "/" + result.data.get(i).getLink();
				WebParser secondResult = new WebParser();
				secondResult.getText(url);
				int t = 0;
				String s2= "";
				while(t < secondResult.data.size()){
					s2 = s2 + secondResult.data.get(t).getShortText() + ", " + secondResult.data.get(t).getText()+"\n" ;
					t++;
				}
				s = s + "\n ----- \n" + s2  + "\n ----- \n";
				i++;
			}
			 
			System.out.println(s);
			//System.out.println(r);
		} catch (ClientProtocolException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

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
			if(url.indexOf("index") > 0){
				result = convertStreamToString(instream);
			}else{
				result = convertArticleStreamToString(instream);
			}

			//-InputStream schließen 
			instream.close();

		}

		return result;

	}

	//-InputStream in String convertieren 
	private String convertStreamToString(InputStream is) {

		BufferedReader reader = null;
		try {
		//-BufferReader mit InputStream erstellen 
		 reader = new BufferedReader(new InputStreamReader(is));

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
					if(cleanLineText.contains("<b>")){
						cleanLineShortText = cleanLineText.substring(cleanLineText.indexOf("<b>")+3, cleanLineText.indexOf("</b>"));
						cleanLineText = cleanLineText.substring(cleanLineText.indexOf("</b>")+4);
					}else if(cleanLineText.contains("<B>")){
						cleanLineShortText = cleanLineText.substring(cleanLineText.indexOf("<B>")+3, cleanLineText.indexOf("</B>"));
						cleanLineText = cleanLineText.substring(cleanLineText.indexOf("</B>")+4);
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
	
	//-Article InputStream in String convertieren 
	private String convertArticleStreamToString(InputStream is) {

		BufferedReader reader = null;
		try {
		//-BufferReader mit InputStream erstellen 
		 reader = new BufferedReader(new InputStreamReader(is));

		//-StringBuilder erstellen 
		StringBuilder sbDetail = new StringBuilder();

		//-Abbruchbedingung 
		String line = null;
		int ArtTextRecord = 0;

			//-While-Schleife ausführen, bis man in der letzten Zeile des BufferReader angekommen ist 
			while ((line = reader.readLine()) != null) {

				if (line.contains("<div id=\"spalteContentPlus\">")){
					ArtTextRecord = 1;
				}else if(ArtTextRecord == 1){
					if(line.contains("<div")){
						sbDetail.append(line);
						ArtTextRecord = 0;
					}else{
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

	public String getLawVersion() {
		// FIXME Muriel: get the version of the law
		return null;
	}

}
