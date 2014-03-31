package utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

public class DateUtil {
	public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE__TIME_PATTERN = "yyyy-MM-dd HH:mm";
	public static final String DATE_SHORT_PATTERN = "yyyy-MM-dd";
	public static final String TIME_PATTERN = "HH:mm:ss";
	public static final String TIME_SHORT_PALLTTRN = "HH:mm";
	public static final String TIME_PATTERN_EN = "ddMMMyyyy";
	private static final String TIME_PATTERN_EN2 = "dd/MM/yyyy";
	public static final String EXCEL_EXPORT_PATTERN = "yyyyMMdd_HHmm";

	public static String dateToStringEn2(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_PATTERN_EN2);
		return sdf.format(date);
	}

	/**
	 * 获得当前日期
	 * @return
	 */
	public static Timestamp newDate() {
		SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
		String timesampDate = format.format(new Date());
		return Timestamp.valueOf(timesampDate);
	}

	/**
	 * 获得当前系统时间
	 * @return
	 */
	public static Timestamp currentTimestamp() {
		String sql = "SELECT NOW() as now";
		SqlRow row = Ebean.createSqlQuery(sql).findUnique();
		return row.getTimestamp("now");
	}

	/**
	 * 将时间格式时间转换为字符串 HH:mm:ss
	 * @param dateDate
	 * @return
	 */
	public static String dateToTime(DateTime dateDate) {
		return dateDate.toString(TIME_PATTERN);
	}

	/**
	 * 将时间格式时间转换为字符串 HH:mm
	 * @param dateDate
	 * @return
	 */
	public static String dateToShortTime(Date date) {
		SimpleDateFormat format = new SimpleDateFormat(TIME_SHORT_PALLTTRN);
		String timesampDate = format.format(date);
		return timesampDate;
	}

	/**
	 * 将时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
	 * @param dateDate
	 * @return
	 */
	public static String dateTimeToStrLong(DateTime dateDate) {
		return dateDate.toString(DATE_PATTERN);
	}

	/**
	 * 将时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
	 * @param date
	 * @return
	 */
	public static String dateToStrLong(Date date) {
		DateTime dateDate = DateUtil.dateToDateTime(date);
		return dateDate.toString(DATE_PATTERN);
	}

	/**
	 * 将时间格式时间转换为字符串 yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public static String dateToStrShort(Date date) {
		DateTime dateDate = DateUtil.dateToDateTime(date);
		return dateDate.toString(DATE_SHORT_PATTERN);
	}

	/**
	 * 将时间格式时间转换为字符串 yyyy-MM-dd HH:mm
	 * @param date
	 * @return
	 */
	public static String dateToStrLongShot(Date date) {
		DateTime dateDate = DateUtil.dateToDateTime(date);
		return dateDate.toString(DATE__TIME_PATTERN);
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static Date strShortToDate(String str) {
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_SHORT_PATTERN);
		try {
			date = sdf.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			date = new Date();
		}
		return date;
	}

	/**
	 * 将时间格式时间转换为字符串 ddMMMyyyy（英文格式）
	 * @param date
	 * @return
	 */
	public static String dateToStrShortEn(Date date) {
		DateTime dateDate = DateUtil.dateToDateTime(date);
		return dateDate.toString(TIME_PATTERN_EN, Locale.ENGLISH);
	}

	/**
	 * Date转DateTime
	 * @param dateDate
	 * @return
	 */
	public static DateTime dateToDateTime(Date dateDate) {
		return new DateTime(dateDate);
	}

	/**
	 * 将timestamp转换成date
	 * @param timestamp
	 * @return
	 */

	public static Date timestampToDate(Timestamp timestamp) {
		return new Date(timestamp.getTime());
	}

	public static String setFullDateFormat(Date date) {
		DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.FULL);
		return dateInstance.format(date);
	}

	public static Date getFullDateFormat(String date) {
		DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.FULL);
		try {
			return dateInstance.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Date strFormatDate(String date) {
		DateFormat dateInstance = new SimpleDateFormat(DATE__TIME_PATTERN);
		try {
			return dateInstance.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 日期加一天
	 * @param date
	 * @return
	 */
	public static Date addOneDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, 1);
		return c.getTime();
	}

	public static Date setDay(Date date, int Day) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, Day);
		return c.getTime();
	}

	/**
	 * 日期减一天
	 * @param date
	 * @return
	 */
	public static Date minusOneDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, -1);
		return c.getTime();
	}

	/**
	 * <p>Description: 日期减一年减一天</p>
	 * @param date
	 * @return
	 */
	public static Date minusYearAddDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, -1);
		c.add(Calendar.DATE, -1);
		return c.getTime();
	}

	/**
	 * <p>Description: 日期减N年减N月减N天</p>
	 * <p>不增不减参数传递0</p>
	 * <p>例如：减一天传递-1，增加一天传递1</p>
	 * @param date
	 * @return
	 */
	public static Date adjustYearAndMonthAndDay(Date date, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, year);
		c.add(Calendar.MONTH, month);
		c.add(Calendar.DATE, day);
		return c.getTime();
	}

	/**
	 * 将字符串转换为日期
	 * @param strDate
	 * @param strInPattern
	 * @return
	 */
	public static Date strToDate(String strDate, String strInPattern) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(strInPattern);
		DateTime result = formatter.parseDateTime(strDate);
		return result.toDate();
	}

	/**
	 * <p>Description: 日期减一个月</p>
	 * @param date
	 * @return
	 */
	public static Date minusMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, -1);
		return c.getTime();
	}

	/**
	 * 增加多少个小时后的日期
	 * @param date
	 * @param hours
	 * @return
	 */
	public static Date addHoursDate(Date date, int hours) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR, hours);
		return c.getTime();
	}

	public static Date StringToDate(String date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		Date s_date = null;
		try {
			if (date == "" || date == null)
				return null;
			s_date = (Date) format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return s_date;
	}

	/**
	 * 获取JSON中DateTime格式时间
	 */
	public static Date getDate(String Time) {
		if (StringUtils.isNotBlank(Time)) {
			Time = Time.replaceAll("T", " ");
			return utils.DateUtil.strToDate(Time, "yyyy-MM-dd HH:mm");
		} else {
			return null;
		}
	}

	public static String dateToExcelPattern(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(EXCEL_EXPORT_PATTERN);
		return sdf.format(date);
	}
}