package de.tblsoft.solr.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	public static String date2String(Date date) {
		SimpleDateFormat sdfPipeline = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		return sdfPipeline.format(date);

	}
	
	public static Date getDate(String date) {
		SimpleDateFormat sdfPipeline = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		try {
			return sdfPipeline.parse(date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String date2String(Date date, String format) {
		SimpleDateFormat sdfPipeline = new SimpleDateFormat(
				format);
		return sdfPipeline.format(date);

	}
	
	public static Date getDate(String date, String format) {
		SimpleDateFormat sdfPipeline = new SimpleDateFormat(
				format);
		try {
			return sdfPipeline.parse(date);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		
	}

	public static String toSolrDate(String date) {
		DateTime jodaDate = new DateTime(date);
		DateTimeFormatter formatter = DateTimeFormat.forPattern(SOLR_DATE_FORMAT).withZone(DateTimeZone.UTC);
		return jodaDate.toString(formatter);
	}

}
