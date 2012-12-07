package ch.bedesign.android.law.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import ch.bedesign.android.law.R;
import ch.bedesign.android.law.log.Logger;

public class SettingsLaw {

	private static SettingsLaw instance = null;
	private final Context ctx;

	public static SettingsLaw getInstance(Context ctx) {
		if (instance == null) {
			instance = new SettingsLaw(ctx);
		}
		return instance;
	}

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

	public boolean hasHoloTheme() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public boolean isContinueUpdatesAtStartup() {
		return getBooleanPreference(R.string.prefKeyContinueUpdates, true);
	}

	public boolean isInsertPageAtEnd() {
		return getBooleanPreference(R.string.prefKeyInsertPageAtEnd, true);
	}

	public String getLanguage() {
		return getStringPreference(R.string.prefKeyLanguage, "");
	}

	public String getLanguageUrlPart() {
		StringBuilder url = new StringBuilder();
		String lang = getStringPreference(R.string.prefKeyLanguage, "");
		boolean sr = true;
		if ("fr".equals(lang) || "it".equals(lang)) {
			url.append(lang.substring(0, 1)).append("/");
			sr = false;
		} else {
			url.append("d");
		}
		url.append("/");
		url.append(sr ? "sr" : "rs");
		return url.toString();
	}
}
