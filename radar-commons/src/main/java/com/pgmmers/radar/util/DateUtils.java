package com.pgmmers.radar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;


/**
 * <p>DateUtils class.</p>
 *
 * @author feihu.wang
 */
public class DateUtils {

    /** Constant <code>dateFormat</code> */
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /** Constant <code>dateTimeFormat</code> */
    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
	 * <p>parseDate.</p>
	 *
	 * @param str a {@link String} object.
	 * @return a {@link Date} object.
	 * @throws ParseException if any.
	 */
	public static Date parseDate(String str) throws ParseException {
		if (str == null || str.trim().equals("")) {
		    return null;
		}
		return dateFormat.parse(str);
	}

    public static Date parseDateTime(String str) throws ParseException {
        if (str == null || str.trim().equals("")) {
            return null;
        }
        return dateTimeFormat.parse(str);
    }

    /**
     * <p>getStartCurrentDate.</p>
     *
     * @return a {@link Date} object.
     */
    public static Date getStartCurrentDate() {
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        try {
//            return dateTimeFormat.parse(dateTimeFormat.format(cal.getTime()));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
        return getCurrentDate();
    }

    /**
     * <p>getEndCurrentDate.</p>
     *
     * @return a {@link Date} object.
     */
    public static Date getEndCurrentDate() {
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, 23);
//        cal.set(Calendar.MINUTE, 59);
//        cal.set(Calendar.SECOND, 59);
//
//        try {
//            return dateTimeFormat.parse(dateTimeFormat.format(cal.getTime()));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
        return addDate(1,getCurrentDate());
    }

    /**
     * <p>getDateTime.</p>
     *
     * @return a long.
     */
    public static long getDateTime() {
        return new Date().getTime();
    }

    /**
     * <p>getCurrentDate.</p>
     *
     * @return a {@link Date} object.
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * <p>getCurrentGMTDate.</p>
     *
     * @return a {@link Date} object.
     */
    public static Date getCurrentGMTDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
        try {
            return dateTimeFormat.parse(dateFormat.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <p>formatDate.</p>
     *
     * @param date a {@link Date} object.
     * @return a {@link String} object.
     */
    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    /**
     * <p>formatDate.</p>
     *
     * @param date a {@link Date} object.
     * @param format a {@link String} object.
     * @return a {@link String} object.
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 两个日期相减 得到秒
     *
     * @param startDate a {@link Date} object.
     * @param endDate a {@link Date} object.
     * @return a long.
     */
    public static long dateSubtract(Date startDate,Date endDate) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        //System.out.println(startCal.getTimeInMillis());
        //System.out.println(endCal.getTimeInMillis());
        return (startCal.getTimeInMillis() - endCal.getTimeInMillis()) / (1000);
    }

    /**
     * 日期加指定天数
     *
     * @param day a int.
     * @param date a {@link Date} object.
     * @return a {@link Date} object.
     */
    public static Date addDate(int day,Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, day);
        return cal.getTime();
    }

    /**
     * <p>parse.</p>
     *
     * @param source a {@link String} object.
     * @param format a {@link String} object.
     * @return a {@link Calendar} object.
     */
    public static Calendar parse(String source, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance();
        try {
           Date date = dateFormat.parse(source);
           c.setTime(date);
        } catch (ParseException e) {
            c = null;
        }
        return c;

    }

    public static Calendar addDate(Date d, int field, int value) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(field, value);
        return c;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link String} objects.
     */
    public static void main(String[] args) {
        System.out.println(getStartCurrentDate());
        System.out.println(getEndCurrentDate());
        
        System.out.println(formatDate(new Date(), "yyyy-MM-ww-dd"));
        
    }
}
