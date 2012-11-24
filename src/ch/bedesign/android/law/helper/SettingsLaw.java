package ch.bedesign.android.law.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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

	public boolean isInsertPageAtEnd() {
		// FIXME not yet working
		return true;
	}

	public String getVersionName() {
		try {
			PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			return pInfo.versionName;
		} catch (NameNotFoundException e) {
			Logger.i("Cannot get cpu tuner version", e);
		}
		return "";
	}

	public boolean isContinueUpdatesAtStartup() {
		// TODO Auto-generated method stub
		return true;
	}
}
