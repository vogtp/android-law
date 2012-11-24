package ch.bedesign.android.law.access;

import java.io.IOException;

public interface IParser {

	public abstract void parse() throws IOException;

	public abstract String getLawVersion();

}