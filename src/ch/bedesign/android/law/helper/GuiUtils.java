package ch.bedesign.android.law.helper;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class GuiUtils {

	public static String milliesToString(long millies) {
		long h = (long) Math.floor(millies / (60 * 60 * 1000));
		long m = (long) Math.floor((millies - h * 60 * 60 * 1000) / (60 * 1000));
		long s = (long) Math.floor((millies - h * 60 * 60 * 1000 - m * 60 * 1000) / 1000);
		long mi = millies % 1000;
		return String.format("%02d:%02d:%02d.%03d", h, m, s, mi);
	}

	public static int setSpinner(Spinner spinner, long dbId) {
		if (spinner == null) {
			return Integer.MIN_VALUE;
		}
		SpinnerAdapter adapter = spinner.getAdapter();
		if (adapter == null) {
			return Integer.MIN_VALUE;
		}
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItemId(i) == dbId) {
				spinner.setSelection(i);
				return i;
			}
		}
		return Integer.MIN_VALUE;
	}

	public static int setSpinner(Spinner spinner, String text) {
		SpinnerAdapter adapter = spinner.getAdapter();
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i) == text) {
				spinner.setSelection(i);
				return i;
			}
		}
		return Integer.MIN_VALUE;
	}


	public static void setLanguage(Context ctx) {
		String lang = SettingsLaw.getInstance(ctx).getLanguage();
		if (!"".equals(lang)) {
			GuiUtils.setLanguage(ctx, lang);
		}
	}

	public static void setLanguage(Context ctx, String lang) {
		Configuration config = new Configuration();
		config.locale = new Locale(lang);
		ctx.getResources().updateConfiguration(config, ctx.getResources().getDisplayMetrics());
	}

	public static void showDialog(Context ctx, int titleId, int messageId) {
		try {
			new AlertDialog.Builder(ctx)
					//	.setIconAttribute(android.R.attr.alertDialogIcon)
					.setTitle(titleId)
					.setMessage(messageId)
					.setPositiveButton(android.R.string.ok, null).create().show();
		} catch (Throwable e) {
			Toast.makeText(ctx, messageId, Toast.LENGTH_LONG).show();
		}
	}

}
