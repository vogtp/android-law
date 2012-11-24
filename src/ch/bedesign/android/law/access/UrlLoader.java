package ch.bedesign.android.law.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import ch.bedesign.android.law.log.Logger;

public class UrlLoader {

	private HttpURLConnection urlConnection = null;
	private final Context context;

	//FIXME remove
	public boolean useUrlConnection = true;

	public UrlLoader(Context context) {
		super();
		this.context = context;
	}

	public InputStream getLawStream(String urlStr) throws IOException {
		return getLawStream(urlStr, true);
	}

	public InputStream getLawStream(String urlStr, boolean useCache) throws IOException {
		if (!useCache) {
			return getStreamFromUrl(urlStr);
		}
		if (!cacheExists(urlStr)) {
			Logger.v("Load url " + urlStr);
			cacheStream(urlStr);
		}
		return getStreamFromCache(urlStr);
	}

	private InputStream getStreamFromCache(String urlStr) throws IOException {
		return new FileInputStream(getCacheFile(urlStr));
	}

	private void cacheStream(String urlStr) {
		InputStream stream;
		FileOutputStream out = null;
		try {
			stream = getStreamFromUrl(urlStr);
			out = new FileOutputStream(getCacheFile(urlStr));
			byte buf[] = new byte[16384];
			do {
				int numread = stream.read(buf);
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
			} while (true);
		} catch (IOException e) {
			Logger.w("Can not cache file", e);
			getCacheFile(urlStr).delete();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private File getCacheFile(String urlStr) {
		File cacheDir = context.getCacheDir();
		return new File(cacheDir, Integer.toString(urlStr.hashCode()));
	}

	private boolean cacheExists(String urlStr) {
		return getCacheFile(urlStr).exists();
	}

	private InputStream getStreamFromUrl(String urlStr) throws IOException {
		if (useUrlConnection) {
			return getStreamFromUrlConnection(urlStr);
		} else {
			return getStreamFromHttpClient(urlStr);
		}
	}

	private InputStream getStreamFromUrlConnection(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		urlConnection = (HttpURLConnection) url.openConnection();
		return urlConnection.getInputStream();
	}

	private InputStream getStreamFromHttpClient(String urlStr) throws IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(urlStr);
		HttpResponse response;
		response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			return entity.getContent();
		}
		throw new IOException("No data for " + urlStr);
	}

	public void finish() {
		if (urlConnection != null) {
			urlConnection.disconnect();
		}
		// close the file here
	}

	public File getFile(String urlStr) {
		if (!cacheExists(urlStr)) {
			Logger.v("Load url " + urlStr);
			cacheStream(urlStr);
		}
		return getCacheFile(urlStr);
	}

}
