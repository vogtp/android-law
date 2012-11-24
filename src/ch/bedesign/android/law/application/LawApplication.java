package ch.bedesign.android.law.application;

import android.app.Application;
import ch.bedesign.android.law.db.DbInitaliser;
import ch.bedesign.android.law.helper.SettingsLaw;

public class LawApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		DbInitaliser.initDb(this);
		//initalise settings
		SettingsLaw.getInstance(this);
	}

}
