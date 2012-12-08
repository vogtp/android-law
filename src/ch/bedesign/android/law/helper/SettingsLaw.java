package ch.bedesign.android.law.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.log.Logger;

/**
 * A front end for android preferences
 * 
 * @author vogtp
 * 
 */
public class SettingsLaw {

	private static SettingsLaw instance = null;
	private final Context ctx;

	/**
	 * Get the singleton instance
	 * 
	 * @param ctx
	 *            as from <code>getContext()</code>
	 * @return the singleton instance of {@link SettingsLaw}
	 */
	public static SettingsLaw getInstance(Context ctx) {
		if (instance == null) {
			instance = new SettingsLaw(ctx);
		}
		return instance;
	}

	/**
	 * Do not use if you can acess a {@link Context}<br>
	 * Use {@link getInstance(Context ctx)} instead
	 * 
	 * @return
	 */
	public static SettingsLaw getInstance() {
		return instance;
	}

	// hide constructor use getinstance
	private SettingsLaw(Context context) {
		super();
		this.ctx = context.getApplicationContext();
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	//	private SharedPreferences getLocalPreferences() {
	//		return context.getSharedPreferences(PREF_STORE_LOCAL, 0);
	//	}

	/**
	 * Get the version of the App
	 * 
	 * @return <code>versionName</code> from {@link PackageInfo}
	 */
	public String getVersionName() {
		try {
			PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			Logger.i("Cannot get cpu tuner version", e);
		}
		return "";
	}

	private String getStringPreference(int prefKey, String defValue) {
		return getPreferences().getString(ctx.getString(prefKey), defValue);
	}

	private boolean getBooleanPreference(int prefKey, boolean defValue) {
		return getPreferences().getBoolean(ctx.getString(prefKey), defValue);
	}

	/**
	 * Check if the holo theme is available
	 * 
	 * @return if {@link Build}<code>.VERSION.SDK_INT</code> is bigger or equal
	 *         as {@link Build}<code>.VERSION_CODES.HONEYCOMB</code>
	 */
	public boolean hasHoloTheme() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	/**
	 * Should updates continue after app restart
	 * 
	 * @return <code>true</code> as default if nothing is set
	 */
	public boolean isContinueUpdatesAtStartup() {
		return getBooleanPreference(R.string.prefKeyContinueUpdates, true);
	}

	/**
	 * Should a new page be inserted at the end
	 * 
	 * @return <code>true</code> as default if nothing is set
	 */

	public boolean isInsertPageAtEnd() {
		return getBooleanPreference(R.string.prefKeyInsertPageAtEnd, true);
	}

	/**
	 * Get the language iso2 code
	 * 
	 * @return the iso2 code of the language or empty string
	 */
	public String getLanguage() {
		return getStringPreference(R.string.prefKeyLanguage, "");
	}

	/**
	 * Get the language dependent url part
	 * 
	 * @return f/rs, i/rs or d/rs the last is the default
	 */
	public String getLanguageUrlPart() {
		StringBuilder url = new StringBuilder();
		String lang = getStringPreference(R.string.prefKeyLanguage, "");
		boolean sr = false;
		if ("fr".equals(lang) || "it".equals(lang)) {
			url.append(lang.substring(0, 1)).append("/");
		} else {
			url.append("d");
			sr = true;
		}
		url.append("/");
		url.append(sr ? "sr" : "rs");
		return url.toString();
	}
}
