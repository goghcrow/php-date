package php.date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static php.date.DayOfWeek.day_of_week;
import static php.date.Time.parse;


/**
 * todo  通过打印把测试用例输出成 json, 做成表驱动的测试
 */
public class TimeTest {

    @Test
    public void playground() {
//        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
//        java_strtotime("+99999-01-01T00:00:00");


        // https://www.laruence.com/2018/07/31/3207.html
        // 1. 先做-1 month, 那么当前是07-31, 减去一以后就是06-31.
        // 2. 再做日期规范化, 因为6月没有31号, 所以就好像2点60等于3点一样, 6月31就等于了7月1

        // 现象
        System.out.println(date("yyyy-MM-dd", Time.parse("2020-07-31 -1 month").epochSecond()));
        // output 2020-07-01

        // 原因
        System.out.println(date("yyyy-MM-dd", Time.parse("2020-06-31").epochSecond()));
        // output  2020-07-01

        // 解决方式
        // "first day of" 和 "last day of", 限定好不要让date自动"规范化":
        System.out.println(date("yyyy-MM-dd", Time.parse("2020-07-31 last day of -1 month").epochSecond()));
        // 2020-06-30
    }


    static String date(String fmt, long ts) {
        return DateTimeFormatter.ofPattern(fmt).format(LocalDateTime.ofEpochSecond(ts, 0, OffsetDateTime.now().getOffset()));
    }


    @Test
    public void test1() {
        System.out.println(date("yyyy-MM-dd", parse("fourth thursday of november").epochSecond()));  // 今年感恩节
        System.out.println(date("yyyy-MM-dd", parse("fourth thursday of november 2019").epochSecond()));  // 2019感恩节
        System.out.println(date("yyyy-MM-dd", parse("fourth thursday of november 2018").epochSecond()));  // 2018今年感恩节

        // 每月最后一天
        String start = "2016-05";
        long now = Instant.now().getEpochSecond();
        long ts = parse(start).epochSecond();
        while (true) {
            String month = date("yyyy-MM", ts);
            String last_day_of_month = date("yyyyMMdd", parse("last day of " + month).epochSecond());
            System.out.println(last_day_of_month);
            ts = parse("+1 month", ts).epochSecond();
            if (ts > now) {
                break;
            }
        }
    }

    @Test
    public void test_dow() {
        Assert.assertEquals(day_of_week(1978, 12, 22), 5);
        Assert.assertEquals(day_of_week(2005, 2, 19), 6);
    }


    @Test
    public void bug() {
        Assert.assertEquals(java_strtotime("10/22"), php_strtotime("10/22")); // AmericanShort


        {
            // work in DST  "America/Sao_Paulo"
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug32086.phpt

            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));

            Assert.assertEquals(java_strtotime("2004-11-01 +1 day"), php_strtotime("2004-11-01 +1 day"));
            Assert.assertEquals(java_strtotime("+1 day", java_strtotime("2004-11-01")), php_strtotime("+1 day", php_strtotime("2004-11-01")));
            Assert.assertEquals(java_strtotime("2004-11-02"), php_strtotime("2004-11-02"));
            Assert.assertEquals(java_strtotime("2004-11-03"), php_strtotime("2004-11-03"));
            Assert.assertEquals(java_strtotime("2005-02-19 +1 day"), php_strtotime("2005-02-19 +1 day"));
            Assert.assertEquals(java_strtotime("+1 day", java_strtotime("2005-02-19")), php_strtotime("+1 day", php_strtotime("2005-02-19")));
            Assert.assertEquals(java_strtotime("2005-02-20"), php_strtotime("2005-02-20"));
            Assert.assertEquals(java_strtotime("2005-02-21"), php_strtotime("2005-02-21"));

            TimeZone.setDefault(defaultZone);
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));

            Assert.assertEquals(java_strtotime("2005-07-14 22:30:41"), php_strtotime("2005-07-14 22:30:41"));
            Assert.assertEquals(java_strtotime("2005-07-14 22:30:41 GMT"), php_strtotime("2005-07-14 22:30:41 GMT"));
            Assert.assertEquals(java_strtotime("@1121373041"), php_strtotime("@1121373041"));
            Assert.assertEquals(java_strtotime("@1121373041 CEST"), php_strtotime("@1121373041 CEST")); // 重复时区声明

            TimeZone.setDefault(defaultZone);
        }

        {
            // Bug #32588 (strtotime() error for 'last xxx' DST problem)
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug32588.phpt
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

            Assert.assertEquals(java_strtotime("last saturday", 1112703348), php_strtotime("last saturday", 1112703348));
            Assert.assertEquals(java_strtotime("last monday", 1112703348), php_strtotime("last monday", 1112703348));
            Assert.assertEquals(java_strtotime("last sunday", 1112703348), php_strtotime("last sunday", 1112703348));

            TimeZone.setDefault(defaultZone);
        }

        {
            // Bug #30532 (strtotime - crossing daylight savings time)
            TimeZone defaultZone = TimeZone.getDefault();
            date_default_timezone_set("America/New_York");
            Assert.assertEquals(java_strtotime("2004-10-31 EDT +1 hour"), php_strtotime("2004-10-31 EDT +1 hour"));
            Assert.assertEquals(java_strtotime("2004-10-31 EDT +2 hours"), php_strtotime("2004-10-31 EDT +2 hours"));
            Assert.assertEquals(java_strtotime("2004-10-31 EDT +3 hours"), php_strtotime("2004-10-31 EDT +3 hours"));
            Assert.assertEquals(java_strtotime("2004-10-31 +1 hour"), php_strtotime("2004-10-31 +1 hour"));
            Assert.assertEquals(java_strtotime("2004-10-31 +2 hours"), php_strtotime("2004-10-31 +2 hours"));
            Assert.assertEquals(java_strtotime("2004-10-31 +3 hours"), php_strtotime("2004-10-31 +3 hours"));
            TimeZone.setDefault(defaultZone);
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug27780.phpt
            for (String zone : new String[]{
                    "America/Chicago",
                    "Europe/Amsterdam",
                    "Asia/Singapore",
                    "Asia/Jerusalem",
                    "America/Sao_Paulo"
            }) {
                System.out.println(zone);
                date_default_timezone_set(zone);
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 -2 months +7 days +23 hours +59 minutes +59 seconds"), php_strtotime("2004-04-07 00:00:00 -2 months +7 days +23 hours +59 minutes +59 seconds"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 -2 months +7 days +23 hours +59 minutes +60 seconds"), php_strtotime("2004-04-07 00:00:00 -2 months +7 days +23 hours +59 minutes +60 seconds"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 -2 months +7 days +23 hours +59 minutes +61 seconds"), php_strtotime("2004-04-07 00:00:00 -2 months +7 days +23 hours +59 minutes +61 seconds"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 -21 days"), php_strtotime("2004-04-07 00:00:00 -21 days"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 11 days ago"), php_strtotime("2004-04-07 00:00:00 11 days ago"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 -10 day +2 hours"), php_strtotime("2004-04-07 00:00:00 -10 day +2 hours"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 -1 day"), php_strtotime("2004-04-07 00:00:00 -1 day"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00"), php_strtotime("2004-04-07 00:00:00"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 +1 hour"), php_strtotime("2004-04-07 00:00:00 +1 hour"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 +2 hour"), php_strtotime("2004-04-07 00:00:00 +2 hour"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 +1 day"), php_strtotime("2004-04-07 00:00:00 +1 day"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 1 day"), php_strtotime("2004-04-07 00:00:00 1 day"));
                Assert.assertEquals(java_strtotime("2004-04-07 00:00:00 +21 days"), php_strtotime("2004-04-07 00:00:00 +21 days"));
            }
            TimeZone.setDefault(defaultZone);
        }

        {
            // Bug #33415 [1] (Possibly invalid non-one-hour DST or timezone shifts)
            TimeZone defaultZone = TimeZone.getDefault();

            date_default_timezone_set("America/Jujuy");
            Assert.assertEquals(java_strtotime("next Monday", java_mktime (17, 17, 17, 1, 7593, 1970)), php_strtotime("next Monday", php_mktime (17, 17, 17, 1, 7593, 1970)));

            date_default_timezone_set("Asia/Tbilisi");
            Assert.assertEquals(java_strtotime("next Sunday", java_mktime (17, 17, 17, 1, 12863, 1970)), php_strtotime("next Sunday", php_mktime (17, 17, 17, 1, 12863, 1970)));

            date_default_timezone_set("Africa/Bujumbura");
            Assert.assertEquals(java_strtotime("next Wednesday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Wednesday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Asia/Thimbu");
            Assert.assertEquals(java_strtotime("next Thursday", java_mktime (17, 17, 17, 1, 6476, 1970)), php_strtotime("next Thursday", php_mktime (17, 17, 17, 1, 6476, 1970)));

            date_default_timezone_set("Indian/Cocos");
            Assert.assertEquals(java_strtotime("next Thursday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Thursday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Africa/Lubumbashi");
            Assert.assertEquals(java_strtotime("next Saturday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Saturday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Asia/Kashgar");
            Assert.assertEquals(java_strtotime("next Thursday", java_mktime (17, 17, 17, 1, 3767, 1970)), php_strtotime("next Thursday", php_mktime (17, 17, 17, 1, 3767, 1970)));

            date_default_timezone_set("Indian/Christmas");
            Assert.assertEquals(java_strtotime("next Sunday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Sunday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("America/Santo_Domingo");
            Assert.assertEquals(java_strtotime("next Sunday", java_mktime (17, 17, 17, 1, 291, 1970)), php_strtotime("next Sunday", php_mktime (17, 17, 17, 1, 291, 1970)));

            date_default_timezone_set("Pacific/Truk");
            Assert.assertEquals(java_strtotime("next Tuesday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Tuesday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Pacific/Ponape");
            Assert.assertEquals(java_strtotime("next Monday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Monday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("America/Scoresbysund");
            Assert.assertEquals(java_strtotime("next Sunday", java_mktime (17, 17, 17, 1, 4099, 1970)), php_strtotime("next Sunday", php_mktime (17, 17, 17, 1, 4099, 1970)));

            date_default_timezone_set("America/Guyana");
            Assert.assertEquals(java_strtotime("next Thursday", java_mktime (17, 17, 17, 1, 2031, 1970)), php_strtotime("next Thursday", php_mktime (17, 17, 17, 1, 2031, 1970)));

            date_default_timezone_set("Asia/Tehran");
            Assert.assertEquals(java_strtotime("next Tuesday", java_mktime (17, 17, 17, 1, 2855, 1970)), php_strtotime("next Tuesday", php_mktime (17, 17, 17, 1, 2855, 1970)));

            date_default_timezone_set("Pacific/Tarawa");
            Assert.assertEquals(java_strtotime("next Monday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Monday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Africa/Monrovia");
            Assert.assertEquals(java_strtotime("next Friday", java_mktime (17, 17, 17, 1, 730, 1970)), php_strtotime("next Friday", php_mktime (17, 17, 17, 1, 730, 1970)));

            date_default_timezone_set("Asia/Katmandu");
            Assert.assertEquals(java_strtotime("next Wednesday", java_mktime (17, 17, 17, 1, 5838, 1970)), php_strtotime("next Wednesday", php_mktime (17, 17, 17, 1, 5838, 1970)));

            date_default_timezone_set("Pacific/Nauru");
            Assert.assertEquals(java_strtotime("next Saturday +2 hours", java_mktime (17, 17, 17, 2, 3, 1979)), php_strtotime("next Saturday +2 hours", php_mktime (17, 17, 17, 2, 3, 1979)));

            date_default_timezone_set("Pacific/Niue");
            Assert.assertEquals(java_strtotime("next Sunday", java_mktime (17, 17, 17, 1, 3189, 1970)), php_strtotime("next Sunday", php_mktime (17, 17, 17, 1, 3189, 1970)));

            date_default_timezone_set("Pacific/Port_Moresby");
            Assert.assertEquals(java_strtotime("next Thursday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Thursday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("America/Miquelon");
            Assert.assertEquals(java_strtotime("next Thursday", java_mktime (17, 17, 17, 1, 3767, 1970)), php_strtotime("next Thursday", php_mktime (17, 17, 17, 1, 3767, 1970)));

            date_default_timezone_set("Pacific/Palau");
            Assert.assertEquals(java_strtotime("next Saturday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Saturday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Pacific/Funafuti");
            Assert.assertEquals(java_strtotime("next Wednesday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Wednesday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Pacific/Wake");
            Assert.assertEquals(java_strtotime("next Tuesday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Tuesday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("Pacific/Wallis");
            Assert.assertEquals(java_strtotime("next Tuesday", java_mktime (17, 17, 17, 1, 1, 1970)), php_strtotime("next Tuesday", php_mktime (17, 17, 17, 1, 1, 1970)));

            date_default_timezone_set("America/Paramaribo");
            Assert.assertEquals(java_strtotime("next Monday", java_mktime (17, 17, 17, 1, 5381, 1970)), php_strtotime("next Monday", php_mktime (17, 17, 17, 1, 5381, 1970)));

            TimeZone.setDefault(defaultZone);
        }
    }

    @Test
    public void test() {
        LocalDateTime now = LocalDateTime.now();
        ZoneOffset offset = OffsetDateTime.now().getOffset();

        Instant now_instant = now.toInstant(ZoneOffset.UTC);
        long now_ts = now_instant.getEpochSecond(); // .toEpochMilli();
        long now_us = now_instant.getNano() / 1000_000;

        LocalDateTime today = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
        LocalDateTime yesterday = today.minusDays(1);
        LocalDateTime tomorrow = today.plusDays(1);
        LocalDateTime noon = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
        LocalDateTime yesterday_noon = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(12, 0));

        Assert.assertTrue(Math.abs(parse("now").epochSecond() - Instant.now().getEpochSecond()) <= 1);
        Assert.assertTrue((parse("now").epochMilli() - now.toInstant(ZonedDateTime.now().getOffset()).toEpochMilli()) < 1000);
        Assert.assertEquals(parse("@" + now_ts + "." + now_us).epochSecond(), now.toEpochSecond(ZoneOffset.UTC));
        Assert.assertEquals(parse("yesterday").epochSecond(), yesterday.toEpochSecond(offset));
        Assert.assertEquals(parse("tomorrow").epochSecond(), tomorrow.toEpochSecond(offset));
        Assert.assertEquals(parse("noon").epochSecond(), noon.toEpochSecond(offset));
        Assert.assertEquals(parse("yesterday noon").epochSecond(), yesterday_noon.toEpochSecond(offset));
    }

    @Test
    public void test_timelib() {
        TimeZone defaultZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        String[] test_cases = {
// american.parse
                "9/11", // -99999-09-11 -99999:-99999:-99999
                "09/11", // -99999-09-11 -99999:-99999:-99999
                "12/22/69", // 2069-12-22 -99999:-99999:-99999
                "12/22/70", // 1970-12-22 -99999:-99999:-99999
                "12/22/78", // 1978-12-22 -99999:-99999:-99999
                "12/22/1978", // 1978-12-22 -99999:-99999:-99999
                "12/22/2078", // 2078-12-22 -99999:-99999:-99999

// bug37017.parse
                "2006-05-12 12:59:59 America/New_York", // 2006-05-12 12:59:59 America/New_York
                "2006-05-12 13:00:00 America/New_York", // 2006-05-12 13:00:00 America/New_York
                "2006-05-12 13:00:01 America/New_York", // 2006-05-12 13:00:01 America/New_York
                "2006-05-12 12:59:59 GMT", // 2006-05-12 12:59:59 GMT 00000

// bug41523.parse
                "0000-00-00", // 0000-00-00 -99999:-99999:-99999
                "0001-00-00", // 0001-00-00 -99999:-99999:-99999
                "0002-00-00", // 0002-00-00 -99999:-99999:-99999
                "0003-00-00", // 0003-00-00 -99999:-99999:-99999
                "000-00-00", // 2000-00-00 -99999:-99999:-99999
                "001-00-00", // 2001-00-00 -99999:-99999:-99999
                "002-00-00", // 2002-00-00 -99999:-99999:-99999
                "003-00-00", // 2003-00-00 -99999:-99999:-99999
                "00-00-00", // 2000-00-00 -99999:-99999:-99999
                "01-00-00", // 2001-00-00 -99999:-99999:-99999
                "02-00-00", // 2002-00-00 -99999:-99999:-99999
                "03-00-00", // 2003-00-00 -99999:-99999:-99999

// bug41842.parse
                "-0001-06-28", // -0001-06-28 -99999:-99999:-99999
                "-2007-06-28", // -2007-06-28 -99999:-99999:-99999

// bug41964.parse
                "❌Ask the experts", // -99999--99999--99999 -99999:-99999:-99999
                "A", // -99999--99999--99999 -99999:-99999:-99999 A 03600
                "❌A Revolution in Development", // -99999--99999--99999 -99999:-99999:-99999 A 03600

// bug44426.parse
                "Aug 27 2007 12:00:00:000AM", // 2007-08-27 00:00:00
                "Aug 27 2007 12:00:00.000AM", // 2007-08-27 00:00:00
                "❌Aug 27 2007 12:00:00:000", // 2007-08-27 12:00:00
                "Aug 27 2007 12:00:00.000", // 2007-08-27 12:00:00
                "Aug 27 2007 12:00:00AM", // 2007-08-27 00:00:00
                "Aug 27 2007", // 2007-08-27 -99999:-99999:-99999
                "Aug 27 2007 12:00AM", // 2007-08-27 00:00:00

// bug50392.parse
                "2010-03-06 16:07:25", // 2010-03-06 16:07:25
                "2010-03-06 16:07:25.1", // 2010-03-06 16:07:25 0.100000
                "2010-03-06 16:07:25.12", // 2010-03-06 16:07:25 0.120000
                "2010-03-06 16:07:25.123", // 2010-03-06 16:07:25 0.123000
                "2010-03-06 16:07:25.1234", // 2010-03-06 16:07:25 0.123400
                "2010-03-06 16:07:25.12345", // 2010-03-06 16:07:25 0.123450
                "2010-03-06 16:07:25.123456", // 2010-03-06 16:07:25 0.123456
                "2010-03-06 16:07:25.1234567", // 2010-03-06 16:07:25 0.123456
                // 这个用例 php 当前(<=8.0)实现有问题
                // "2010-03-06 16:07:25.12345678", // 2010-03-06 16:07:25 0.123456

// bug51096.parse
                "first day", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   1D /   0H   0M   0S
                "last day", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -1D /   0H   0M   0S
                "next month", // -99999--99999--99999 -99999:-99999:-99999  0Y   1M   0D /   0H   0M   0S
                "first day next month", // -99999--99999--99999 -99999:-99999:-99999  0Y   1M   1D /   0H   0M   0S
                "first day of next month", // -99999--99999--99999 -99999:-99999:-99999  0Y   1M   0D /   0H   0M   0S / first day of
                "last day next month", // -99999--99999--99999 -99999:-99999:-99999  0Y   1M  -1D /   0H   0M   0S
                "last day of next month", // -99999--99999--99999 -99999:-99999:-99999  0Y   1M   0D /   0H   0M   0S / last day of

// bug54597.parse
                "January 0099", // 0099-01-01 -99999:-99999:-99999
                "January 1, 0099", // 0099-01-01 -99999:-99999:-99999
                "0099-1", // 0099-01-01 -99999:-99999:-99999
                "0099-January", // 0099-01-01 -99999:-99999:-99999
                "0099-Jan", // 0099-01-01 -99999:-99999:-99999
                "January 1099", // 1099-01-01 -99999:-99999:-99999
                "January 1, 1299", // 1299-01-01 -99999:-99999:-99999
                "1599-1", // 1599-01-01 -99999:-99999:-99999

// bug63470.parse
                "2015-07-12 00:00 this week", // 2015-07-12 00:00:00  0Y   0M   0D /   0H   0M   0S / 1.2
                "2015-07-12 00:00 sunday this week", // 2015-07-12 00:00:00  0Y   0M   0D /   0H   0M   0S / 0.2
                "2015-07-12 00:00 this week sunday", // 2015-07-12 00:00:00  0Y   0M   0D /   0H   0M   0S / 0.2
                "2015-07-12 00:00 sunday", // 2015-07-12 00:00:00  0Y   0M   0D /   0H   0M   0S / 0.1
                "2008-04-25 00:00 this week tuesday", // 2008-04-25 00:00:00  0Y   0M   0D /   0H   0M   0S / 2.2
                "2008-04-25 00:00 this week sunday", // 2008-04-25 00:00:00  0Y   0M   0D /   0H   0M   0S / 0.2
                "Sun 2017-01-01 00:00 saturday this week", // 2017-01-01 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.2
                "Mon 2017-01-02 00:00 saturday this week", // 2017-01-02 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.2
                "Tue 2017-01-03 00:00 saturday this week", // 2017-01-03 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.2

                "Sun 2017-01-02 00:00 saturday this week", // 2017-01-02 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.2

// bug74819.parse
                "❌I06.00am 0", // 2000-01-06 -99999:-99999:-99999

// bugs.parse
                "04/05/06 0045", // 2006-04-05 00:45:00
                "17:00 2004-01-03", // 2004-01-03 17:00:00
                "2004-03-10 16:33:17.11403+01", // 2004-03-10 16:33:17 0.114030 GMT 03600
                "2004-03-10 16:33:17+01", // 2004-03-10 16:33:17 GMT 03600
                "Sun, 21 Dec 2003 20:38:33 +0000 GMT", // 2003-12-21 20:38:33 GMT 00000  0Y   0M   0D /   0H   0M   0S / 0.1
                "2003-11-19 08:00:00 T", // 2003-11-19 08:00:00 T -25200
                "01-MAY-1982 00:00:00", // 1982-05-01 00:00:00
                "2040-06-12T04:32:12", // 2040-06-12 04:32:12
                "july 14th", // -99999-07-14 -99999:-99999:-99999

                // ❗️❗️❗️
                // php 这个有问题, 会按照  july 14t  解析, 然后把 H 解析成时区
                // php -r 'date_default_timezone_set("UTC");  print_r(date_parse("july 14tH"));'
                //  [tz_abbr] => H
                // "july 14tH", // -99999-07-14 -99999:-99999:-99999 H 28800

                "11Oct", // -99999-10-11 -99999:-99999:-99999
                "11 Oct", // -99999-10-11 -99999:-99999:-99999
                "2005/04/05/08:15:48 last saturday", // 2005-04-05 00:00:00  0Y   0M  -7D /   0H   0M   0S / 6.0
                "2005/04/05/08:15:48 last sunday", // 2005-04-05 00:00:00  0Y   0M  -7D /   0H   0M   0S / 0.0
                "2005/04/05/08:15:48 last monday", // 2005-04-05 00:00:00  0Y   0M  -7D /   0H   0M   0S / 1.0
                "2004-04-07 00:00:00 CET -10 day +1 hour", // 2004-04-07 00:00:00 CET 03600  0Y   0M -10D /   1H   0M   0S
                "Jan14, 2004", // 2004-01-14 -99999:-99999:-99999
                "Jan 14, 2004", // 2004-01-14 -99999:-99999:-99999
                "Jan.14, 2004", // 2004-01-14 -99999:-99999:-99999
                "1999-10-13", // 1999-10-13 -99999:-99999:-99999
                "Oct 13  1999", // 1999-10-13 -99999:-99999:-99999
                "2000-01-19", // 2000-01-19 -99999:-99999:-99999
                "Jan 19  2000", // 2000-01-19 -99999:-99999:-99999
                "2001-12-21", // 2001-12-21 -99999:-99999:-99999
                "Dec 21  2001", // 2001-12-21 -99999:-99999:-99999
                "2001-12-21 12:16", // 2001-12-21 12:16:00
                "Dec 21 2001 12:16", // 2001-12-21 12:16:00
                "Dec 21  12:16", // -99999-12-21 12:16:00
                "2001-10-22 21:19:58", // 2001-10-22 21:19:58
                "2001-10-22 21:19:58-02", // 2001-10-22 21:19:58 GMT -7200
                "2001-10-22 21:19:58-0213", // 2001-10-22 21:19:58 GMT -7980
                "2001-10-22 21:19:58+02", // 2001-10-22 21:19:58 GMT 07200
                "2001-10-22 21:19:58+0213", // 2001-10-22 21:19:58 GMT 07980
                "2001-10-22T21:20:58-03:40", // 2001-10-22 21:20:58 GMT -13200
                "2001-10-22T211958-2", // 2001-10-22 21:19:58 GMT -7200
                "20011022T211958+0213", // 2001-10-22 21:19:58 GMT 07980
                "20011022T21:20+0215", // 2001-10-22 21:20:00 GMT 08100
                "1997W011", // 1997-01-01 -99999:-99999:-99999  0Y   0M  -2D /   0H   0M   0S
                "2004W101T05:00+0", // 2004-01-01 05:00:00 GMT 00000  0Y   0M  60D /   0H   0M   0S
                "❌nextyear", // -99999--99999--99999 -99999:-99999:-99999
                "next year", // -99999--99999--99999 -99999:-99999:-99999  1Y   0M   0D /   0H   0M   0S

// combined.parse
                "Sat, 24 Apr 2004 21:48:40 +0200", // 2004-04-24 21:48:40 GMT 07200  0Y   0M   0D /   0H   0M   0S / 6.1
                "Sun Apr 25 01:05:41 CEST 2004", // 2004-04-25 01:05:41 CEST 03600 (DST)  0Y   0M   0D /   0H   0M   0S / 0.1
                "Sun Apr 18 18:36:57 2004", // 2004-04-18 18:36:57  0Y   0M   0D /   0H   0M   0S / 0.1
                "Sat, 24 Apr 2004	21:48:40	+0200", // 2004-04-24 21:48:40 GMT 07200  0Y   0M   0D /   0H   0M   0S / 6.1
                "20040425010541 CEST", // 2004-04-25 01:05:41 CEST 03600 (DST)
                "20040425010541", // 2004-04-25 01:05:41
                "19980717T14:08:55", // 1998-07-17 14:08:55
                "10/Oct/2000:13:55:36 -0700", // 2000-10-10 13:55:36 GMT -25200
                "2001-11-29T13:20:01.123", // 2001-11-29 13:20:01 0.123000
                "2001-11-29T13:20:01.123-05:00", // 2001-11-29 13:20:01 0.123000 GMT -18000
                "Fri Aug 20 11:59:59 1993 GMT", // 1993-08-20 11:59:59 GMT 00000  0Y   0M   0D /   0H   0M   0S / 5.1
                "Fri Aug 20 11:59:59 1993 UTC", // 1993-08-20 11:59:59 UTC UTC  0Y   0M   0D /   0H   0M   0S / 5.1
                "Fri	Aug	20	 11:59:59	 1993	UTC", // 1993-08-20 11:59:59 UTC UTC  0Y   0M   0D /   0H   0M   0S / 5.1
                "May 18th 5:05 UTC", // -99999-05-18 05:05:00 UTC UTC
                "May 18th 5:05pm UTC", // -99999-05-18 17:05:00 UTC UTC
                "May 18th 5:05 pm UTC", // -99999-05-18 17:05:00 UTC UTC
                "May 18th 5:05am UTC", // -99999-05-18 05:05:00 UTC UTC
                "May 18th 5:05 am UTC", // -99999-05-18 05:05:00 UTC UTC
                "May 18th 2006 5:05pm UTC", // 2006-05-18 17:05:00 UTC UTC

// common.parse
                "now", // -99999--99999--99999 -99999:-99999:-99999
                "NOW", // -99999--99999--99999 -99999:-99999:-99999
                "noW", // -99999--99999--99999 -99999:-99999:-99999
                "today", // -99999--99999--99999 00:00:00
                "midnight", // -99999--99999--99999 00:00:00
                "noon", // -99999--99999--99999 12:00:00
                "tomorrow", // -99999--99999--99999 00:00:00  0Y   0M   1D /   0H   0M   0S
                "yesterday 08:15pm", // -99999--99999--99999 20:15:00  0Y   0M  -1D /   0H   0M   0S
                "yesterday midnight", // -99999--99999--99999 00:00:00  0Y   0M  -1D /   0H   0M   0S
                "tomorrow 18:00", // -99999--99999--99999 18:00:00  0Y   0M   1D /   0H   0M   0S
                "tomorrow noon", // -99999--99999--99999 12:00:00  0Y   0M   1D /   0H   0M   0S
                "TODAY", // -99999--99999--99999 00:00:00
                "MIDNIGHT", // -99999--99999--99999 00:00:00
                "NOON", // -99999--99999--99999 12:00:00
                "TOMORROW", // -99999--99999--99999 00:00:00  0Y   0M   1D /   0H   0M   0S
                "YESTERDAY 08:15pm", // -99999--99999--99999 20:15:00  0Y   0M  -1D /   0H   0M   0S
                "YESTERDAY MIDNIGHT", // -99999--99999--99999 00:00:00  0Y   0M  -1D /   0H   0M   0S
                "TOMORROW 18:00", // -99999--99999--99999 18:00:00  0Y   0M   1D /   0H   0M   0S
                "TOMORROW NOON", // -99999--99999--99999 12:00:00  0Y   0M   1D /   0H   0M   0S
                "ToDaY", // -99999--99999--99999 00:00:00
                "mIdNiGhT", // -99999--99999--99999 00:00:00
                "NooN", // -99999--99999--99999 12:00:00
                "ToMoRRoW", // -99999--99999--99999 00:00:00  0Y   0M   1D /   0H   0M   0S
                "yEstErdAY 08:15pm", // -99999--99999--99999 20:15:00  0Y   0M  -1D /   0H   0M   0S
                "yEsTeRdAY mIdNiGht", // -99999--99999--99999 00:00:00  0Y   0M  -1D /   0H   0M   0S
                "toMOrrOW 18:00", // -99999--99999--99999 18:00:00  0Y   0M   1D /   0H   0M   0S
                "TOmoRRow nOOn", // -99999--99999--99999 12:00:00  0Y   0M   1D /   0H   0M   0S
                "TOmoRRow	nOOn", // -99999--99999--99999 12:00:00  0Y   0M   1D /   0H   0M   0S

// date.parse
                "31.01.2006", // 2006-01-31 -99999:-99999:-99999
                "❌32.01.2006", // 2006-01-02 -99999:-99999:-99999
                "28.01.2006", // 2006-01-28 -99999:-99999:-99999
                "29.01.2006", // 2006-01-29 -99999:-99999:-99999
                "30.01.2006", // 2006-01-30 -99999:-99999:-99999
                "31.01.2006", // 2006-01-31 -99999:-99999:-99999
                "❌32.01.2006", // 2006-01-02 -99999:-99999:-99999
                "31-01-2006", // 2006-01-31 -99999:-99999:-99999
                "❌32-01-2006", // 2032-01-20 -99999:-99999:-99999
                "28-01-2006", // 2006-01-28 -99999:-99999:-99999
                "29-01-2006", // 2006-01-29 -99999:-99999:-99999
                "30-01-2006", // 2006-01-30 -99999:-99999:-99999
                "31-01-2006", // 2006-01-31 -99999:-99999:-99999
                "❌32-01-2006", // 2032-01-20 -99999:-99999:-99999
                "29-02-2006", // 2006-02-29 -99999:-99999:-99999
                "30-02-2006", // 2006-02-30 -99999:-99999:-99999
                "31-02-2006", // 2006-02-31 -99999:-99999:-99999
                "01-01-2006", // 2006-01-01 -99999:-99999:-99999
                "31-12-2006", // 2006-12-31 -99999:-99999:-99999
                "❌31-13-2006", // -99999--99999--99999 -99999:-99999:-99999 GMT -46800
                "11/10/2006", // 2006-11-10 -99999:-99999:-99999
                "12/10/2006", // 2006-12-10 -99999:-99999:-99999
                "❌13/10/2006", // 2006-03-10 -99999:-99999:-99999
                "❌14/10/2006", // 2006-04-10 -99999:-99999:-99999

// datefull.parse
                "22 dec 1978", // 1978-12-22 -99999:-99999:-99999
                "22-dec-78", // 1978-12-22 -99999:-99999:-99999
                "22 Dec 1978", // 1978-12-22 -99999:-99999:-99999
                "22DEC78", // 1978-12-22 -99999:-99999:-99999
                "22 december 1978", // 1978-12-22 -99999:-99999:-99999
                "22-december-78", // 1978-12-22 -99999:-99999:-99999
                "22 December 1978", // 1978-12-22 -99999:-99999:-99999
                "22DECEMBER78", // 1978-12-22 -99999:-99999:-99999
                "22	dec	1978", // 1978-12-22 -99999:-99999:-99999
                "22	Dec	1978", // 1978-12-22 -99999:-99999:-99999
                "22	december	1978", // 1978-12-22 -99999:-99999:-99999
                "22	December	1978", // 1978-12-22 -99999:-99999:-99999

// datenocolon.parse
                "19781222", // 1978-12-22 -99999:-99999:-99999

// datenoday.parse
                "Oct 2003", // 2003-10-01 -99999:-99999:-99999
                "20 October 2003", // 2003-10-20 -99999:-99999:-99999
                "Oct 03", // -99999-10-03 -99999:-99999:-99999
                "Oct 2003 2045", // 2003-10-01 20:45:00
                "Oct 2003 20:45", // 2003-10-01 20:45:00
                "Oct 2003 20:45:37", // 2003-10-01 20:45:37
                "20 October 2003 00:00 CEST", // 2003-10-20 00:00:00 CEST 03600 (DST)
                "Oct 03 21:46m", // -99999-10-03 21:46:00 M 43200
                "Oct	2003	20:45", // 2003-10-01 20:45:00
                "Oct	03	21:46m", // -99999-10-03 21:46:00 M 43200

// dateroman.parse
                "22 I 1978", // 1978-01-22 -99999:-99999:-99999
                "22. II 1978", // 1978-02-22 -99999:-99999:-99999
                "22 III. 1978", // 1978-03-22 -99999:-99999:-99999
                "22- IV- 1978", // 1978-04-22 -99999:-99999:-99999
                "22 -V -1978", // 1978-05-22 -99999:-99999:-99999
                "22-VI-1978", // 1978-06-22 -99999:-99999:-99999
                "22.VII.1978", // 1978-07-22 -99999:-99999:-99999
                "22 VIII 1978", // 1978-08-22 -99999:-99999:-99999
                "22 IX 1978", // 1978-09-22 -99999:-99999:-99999
                "22 X 1978", // 1978-10-22 -99999:-99999:-99999
                "22 XI 1978", // 1978-11-22 -99999:-99999:-99999
                "22	XII	1978", // 1978-12-22 -99999:-99999:-99999

// dateslash.parse
                "2005/8/12", // 2005-08-12 -99999:-99999:-99999
                "2005/01/02", // 2005-01-02 -99999:-99999:-99999
                "2005/01/2", // 2005-01-02 -99999:-99999:-99999
                "2005/1/02", // 2005-01-02 -99999:-99999:-99999
                "2005/1/2", // 2005-01-02 -99999:-99999:-99999

// datetextual.parse
                "December 22, 1978", // 1978-12-22 -99999:-99999:-99999
                "DECEMBER 22nd 1978", // 1978-12-22 -99999:-99999:-99999
                "December 22. 1978", // 1978-12-22 -99999:-99999:-99999
                "December 22 1978", // 1978-12-22 -99999:-99999:-99999
                "Dec 22, 1978", // 1978-12-22 -99999:-99999:-99999
                "DEC 22nd 1978", // 1978-12-22 -99999:-99999:-99999
                "Dec 22. 1978", // 1978-12-22 -99999:-99999:-99999
                "Dec 22 1978", // 1978-12-22 -99999:-99999:-99999
                "December 22", // -99999-12-22 -99999:-99999:-99999
                "Dec 22", // -99999-12-22 -99999:-99999:-99999
                "DEC 22nd", // -99999-12-22 -99999:-99999:-99999
                "December	22	1978", // 1978-12-22 -99999:-99999:-99999
                "DEC	22nd", // -99999-12-22 -99999:-99999:-99999

// frontof.parse
                "❌frONt of 0 0", // -99999--99999--99999 -1:45:00
                "frONt of 4pm", // -99999--99999--99999 15:45:00
                "frONt of 4 pm", // -99999--99999--99999 15:45:00

// iso8601date.parse
                "1978-12-22", // 1978-12-22 -99999:-99999:-99999
                "0078-12-22", // 0078-12-22 -99999:-99999:-99999
                "078-12-22", // 1978-12-22 -99999:-99999:-99999
                "78-12-22", // 1978-12-22 -99999:-99999:-99999
                "4-4-25", // 2004-04-25 -99999:-99999:-99999
                "69-4-25", // 2069-04-25 -99999:-99999:-99999
                "70-4-25", // 1970-04-25 -99999:-99999:-99999
                "1978/12/22", // 1978-12-22 -99999:-99999:-99999
                "1978/02/02", // 1978-02-02 -99999:-99999:-99999
                "1978/12/02", // 1978-12-02 -99999:-99999:-99999
                "1978/02/22", // 1978-02-22 -99999:-99999:-99999

// iso8601long.parse
                "01:00:03.12345", // -99999--99999--99999 01:00:03 0.123450
                "13:03:12.45678", // -99999--99999--99999 13:03:12 0.456780

// iso8601longtz.parse
                "01:00:03.12345 CET", // -99999--99999--99999 01:00:03 0.123450 CET 03600
                "13:03:12.45678 CEST", // -99999--99999--99999 13:03:12 0.456780 CEST 03600 (DST)
                "15:57:41.0GMT", // -99999--99999--99999 15:57:41 GMT 00000
                "15:57:41.0 pdt", // -99999--99999--99999 15:57:41 PDT -28800 (DST)
                "23:41:00.0Z", // -99999--99999--99999 23:41:00 Z 00000
                "23:41:00.0 k", // -99999--99999--99999 23:41:00 K 36000
                "04:05:07.789cast", // -99999--99999--99999 04:05:07 0.789000 CAST 34200
                "01:00:03.12345  +1", // -99999--99999--99999 01:00:03 0.123450 GMT 03600
                "13:03:12.45678 +0100", // -99999--99999--99999 13:03:12 0.456780 GMT 03600
                "15:57:41.0-0", // -99999--99999--99999 15:57:41 GMT 00000
                "15:57:41.0-8", // -99999--99999--99999 15:57:41 GMT -28800
                "23:41:00.0 -0000", // -99999--99999--99999 23:41:00 GMT 00000
                "04:05:07.789 +0930", // -99999--99999--99999 04:05:07 0.789000 GMT 34200
                "01:00:03.12345 (CET)", // -99999--99999--99999 01:00:03 0.123450 CET 03600
                "13:03:12.45678 (CEST)", // -99999--99999--99999 13:03:12 0.456780 CEST 03600 (DST)
                "(CET) 01:00:03.12345", // -99999--99999--99999 01:00:03 0.123450 CET 03600
                "(CEST) 13:03:12.45678", // -99999--99999--99999 13:03:12 0.456780 CEST 03600 (DST)
                "13:03:12.45678	(CEST)", // -99999--99999--99999 13:03:12 0.456780 CEST 03600 (DST)
                "(CEST)	13:03:12.45678", // -99999--99999--99999 13:03:12 0.456780 CEST 03600 (DST)

// iso8601nocolon.parse
                "2314", // -99999--99999--99999 23:14:00
                "2314 2314", // 2314--99999--99999 23:14:00
                "2314 PST", // -99999--99999--99999 23:14:00 PST -28800
                "231431 CEST", // -99999--99999--99999 23:14:31 CEST 03600 (DST)
                "231431 CET", // -99999--99999--99999 23:14:31 CET 03600
                "231431", // -99999--99999--99999 23:14:31
                "231431 2314", // 2314--99999--99999 23:14:31
                "❌2314 231431", // -99999--99999--99999 23:14:00
                "2314	2314", // 2314--99999--99999 23:14:00
                "2314	PST", // -99999--99999--99999 23:14:00 PST -28800
                "231431	CEST", // -99999--99999--99999 23:14:31 CEST 03600 (DST)
                "231431	CET", // -99999--99999--99999 23:14:31 CET 03600
                "231431	2314", // 2314--99999--99999 23:14:31
                "❌2314	231431", // -99999--99999--99999 23:14:00

// iso8601normtz.parse
                "01:00:03 CET", // -99999--99999--99999 01:00:03 CET 03600
                "13:03:12 CEST", // -99999--99999--99999 13:03:12 CEST 03600 (DST)
                "15:57:41GMT", // -99999--99999--99999 15:57:41 GMT 00000
                "15:57:41 pdt", // -99999--99999--99999 15:57:41 PDT -28800 (DST)
                "23:41:02Y", // -99999--99999--99999 23:41:02 Y -43200
                "04:05:07cast", // -99999--99999--99999 04:05:07 CAST 34200
                "01:00:03  +1", // -99999--99999--99999 01:00:03 GMT 03600
                "13:03:12 +0100", // -99999--99999--99999 13:03:12 GMT 03600
                "15:57:41-0", // -99999--99999--99999 15:57:41 GMT 00000
                "15:57:41-8", // -99999--99999--99999 15:57:41 GMT -28800
                "23:41:01 -0000", // -99999--99999--99999 23:41:01 GMT 00000
                "04:05:07 +0930", // -99999--99999--99999 04:05:07 GMT 34200
                "13:03:12	CEST", // -99999--99999--99999 13:03:12 CEST 03600 (DST)
                "15:57:41	pdt", // -99999--99999--99999 15:57:41 PDT -28800 (DST)
                "01:00:03		+1", // -99999--99999--99999 01:00:03 GMT 03600
                "13:03:12	+0100", // -99999--99999--99999 13:03:12 GMT 03600

// iso8601shorttz.parse
                "01:00 CET", // -99999--99999--99999 01:00:00 CET 03600
                "13:03 CEST", // -99999--99999--99999 13:03:00 CEST 03600 (DST)
                "15:57GMT", // -99999--99999--99999 15:57:00 GMT 00000
                "15:57 pdt", // -99999--99999--99999 15:57:00 PDT -28800 (DST)
                "23:41F", // -99999--99999--99999 23:41:00 F 21600
                "04:05cast", // -99999--99999--99999 04:05:00 CAST 34200
                "01:00  +1", // -99999--99999--99999 01:00:00 GMT 03600
                "13:03 +0100", // -99999--99999--99999 13:03:00 GMT 03600
                "15:57-0", // -99999--99999--99999 15:57:00 GMT 00000
                "15:57-8", // -99999--99999--99999 15:57:00 GMT -28800
                "23:41 -0000", // -99999--99999--99999 23:41:00 GMT 00000
                "04:05 +0930", // -99999--99999--99999 04:05:00 GMT 34200

// last-day-of.parse
                "last saturday of feb 2008", // 2008-02-01 00:00:00  0Y   0M  -7D /   0H   0M   0S / 6.0 / last y of z month
                "last tue of 2008-11", // 2008-11-01 00:00:00  0Y   0M  -7D /   0H   0M   0S / 2.0 / last y of z month
                "last sunday of sept", // -99999-09--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 0.0 / last y of z month
                "last saturday of this month", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 6.0 / last y of z month
                "last thursday of last month", // -99999--99999--99999 00:00:00  0Y  -1M  -7D /   0H   0M   0S / 4.0 / last y of z month
                "last wed of fourth month", // -99999--99999--99999 00:00:00  0Y   4M  -7D /   0H   0M   0S / 3.0 / last y of z month

// microsecond.parse
                "+1 ms", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.001000
                "+3 msec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.003000
                "+4 msecs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.004000
                "+5 millisecond", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.005000
                "+6 milliseconds", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.006000
                "+1 µs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.000001
                "+3 usec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.000003
                "+4 usecs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.000004
                "+5 µsec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.000005
                "+6 µsecs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.000006
                "+7 microsecond", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.000007
                "+8 microseconds", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S 0.000008

// mysql.parse
                "19970523091528", // 1997-05-23 09:15:28
                "20001231185859", // 2000-12-31 18:58:59
                "20500410101010", // 2050-04-10 10:10:10
                "20050620091407", // 2005-06-20 09:14:07

// pgsql.parse
                "January 8, 1999", // 1999-01-08 -99999:-99999:-99999
                "January	8,	1999", // 1999-01-08 -99999:-99999:-99999
                "1999-01-08", // 1999-01-08 -99999:-99999:-99999
                "1/8/1999", // 1999-01-08 -99999:-99999:-99999
                "1/18/1999", // 1999-01-18 -99999:-99999:-99999
                "01/02/03", // 2003-01-02 -99999:-99999:-99999
                "1999-Jan-08", // 1999-01-08 -99999:-99999:-99999
                "Jan-08-1999", // 1999-01-08 -99999:-99999:-99999
                "08-Jan-1999", // 1999-01-08 -99999:-99999:-99999
                "99-Jan-08", // 1999-01-08 -99999:-99999:-99999
                "08-Jan-99", // 1999-01-08 -99999:-99999:-99999
                "Jan-08-99", // 1999-01-08 -99999:-99999:-99999
                "19990108", // 1999-01-08 -99999:-99999:-99999
                "1999.008", // 1999-01-08 -99999:-99999:-99999
                "1999.038", // 1999-01-38 -99999:-99999:-99999
                "1999.238", // 1999-01-238 -99999:-99999:-99999
                "1999.366", // 1999-01-366 -99999:-99999:-99999
                "1999008", // 1999-01-08 -99999:-99999:-99999
                "1999038", // 1999-01-38 -99999:-99999:-99999
                "1999238", // 1999-01-238 -99999:-99999:-99999
                "1999366", // 1999-01-366 -99999:-99999:-99999

// pointeddate.parse
                "22.12.1978", // 1978-12-22 -99999:-99999:-99999
                "22.7.1978", // 1978-07-22 -99999:-99999:-99999
                "22.12.78", // 1978-12-22 -99999:-99999:-99999
                "22.7.78", // 1978-07-22 -99999:-99999:-99999

// relative.parse
                "2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "+2 sec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "-2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "++2 sec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "+-2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "-+2 sec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "--2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "+++2 sec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "++-2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "+-+2 sec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "+--2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "-++2 sec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "-+-2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "--+2 sec", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "---2 secs", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "+2 sec ago", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "2 secs ago", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -2S
                "0 second", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S
                "first second", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   1S
                "next second", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   1S
                "second second", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   2S
                "third second", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   3S
                "-3 seconds", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -3S
                "+2 days", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   2D /   0H   0M   0S
                "+2 days ago", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -2D /   0H   0M   0S
                "-2 days", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -2D /   0H   0M   0S
                "-3 fortnight", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M -42D /   0H   0M   0S
                "+12 weeks", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  84D /   0H   0M   0S
                "- 3 seconds", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M  -3S
                "+ 2 days", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   2D /   0H   0M   0S
                "+ 2 days ago", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -2D /   0H   0M   0S
                "- 2 days", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -2D /   0H   0M   0S
                "- 3 fortnight", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M -42D /   0H   0M   0S
                "+ 12 weeks", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  84D /   0H   0M   0S
                "- 2	days", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -2D /   0H   0M   0S
                "-	3 fortnight", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M -42D /   0H   0M   0S
                "+	12	weeks", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  84D /   0H   0M   0S
                "6 month 2004-05-05 12:15:23 CEST", // 2004-05-05 12:15:23 CEST 03600 (DST)  0Y   6M   0D /   0H   0M   0S
                "2004-05-05 12:15:23 CEST 6 months", // 2004-05-05 12:15:23 CEST 03600 (DST)  0Y   6M   0D /   0H   0M   0S
                "2004-05-05 12:15:23 CEST 6 months ago", // 2004-05-05 12:15:23 CEST 03600 (DST)  0Y  -6M   0D /   0H   0M   0S
                "6 months ago 4 days", // -99999--99999--99999 -99999:-99999:-99999  0Y  -6M   4D /   0H   0M   0S
                "first month", // -99999--99999--99999 -99999:-99999:-99999  0Y   1M   0D /   0H   0M   0S
                "saturday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.1
                "saturday ago", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / -6.1
                "this saturday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.1
                "this saturday ago", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / -6.1
                "last saturday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 6.0
                "last saturday ago", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / -6.0
                "first saturday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.0
                "first saturday ago", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / -6.0
                "next saturday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.0
                "next saturday ago", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / -6.0
                "third saturday", // -99999--99999--99999 00:00:00  0Y   0M  14D /   0H   0M   0S / 6.0
                "third saturday ago", // -99999--99999--99999 00:00:00  0Y   0M -14D /   0H   0M   0S / -6.0
                "previous saturday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 6.0
                "this weekday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 0 weekday
                "last weekday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / -1 weekday
                "next weekday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 1 weekday
                "8 weekdays ago", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / -8 weekday
                "Sun, 21 Dec 2003 20:38:33 +0000 GMT", // 2003-12-21 20:38:33 GMT 00000  0Y   0M   0D /   0H   0M   0S / 0.1
                "Mon, 08 May 2006 13:06:44 -0400 +30 days", // 2006-05-08 13:06:44 GMT -14400  0Y   0M  30D /   0H   0M   0S / 1.1

// special.parse
                "1998-9-15T09:05:32+4:0", // 1998-09-15 09:05:32 GMT 14400
                "1998-09-15T09:05:32+04:00", // 1998-09-15 09:05:32 GMT 14400
                "1998-09-15T09:05:32.912+04:00", // 1998-09-15 09:05:32 0.912000 GMT 14400
                "1998-09-15T09:05:32", // 1998-09-15 09:05:32
                "19980915T09:05:32", // 1998-09-15 09:05:32
                "19980915t090532", // 1998-09-15 09:05:32
                "1998-09-15T09:05:32+4:9", // 1998-09-15 09:05:32 GMT 14940
                "1998-9-15T09:05:32+4:30", // 1998-09-15 09:05:32 GMT 16200
                "1998-09-15T09:05:32+04:9", // 1998-09-15 09:05:32 GMT 14940
                "1998-9-15T09:05:32+04:30", // 1998-09-15 09:05:32 GMT 16200

// timelong12.parse
                "01:00:03am", // -99999--99999--99999 01:00:03
                "01:03:12pm", // -99999--99999--99999 13:03:12
                "12:31:13 A.M.", // -99999--99999--99999 00:31:13
                "08:13:14 P.M.", // -99999--99999--99999 20:13:14
                "11:59:15 AM", // -99999--99999--99999 11:59:15
                "06:12:16 PM", // -99999--99999--99999 18:12:16
                "07:08:17 am", // -99999--99999--99999 07:08:17
                "08:09:18 p.m.", // -99999--99999--99999 20:09:18
                "01.00.03am", // -99999--99999--99999 01:00:03
                "01.03.12pm", // -99999--99999--99999 13:03:12
                "12.31.13 A.M.", // -99999--99999--99999 00:31:13
                "08.13.14 P.M.", // -99999--99999--99999 20:13:14
                "11.59.15 AM", // -99999--99999--99999 11:59:15
                "06.12.16 PM", // -99999--99999--99999 18:12:16
                "07.08.17 am", // -99999--99999--99999 07:08:17
                "08.09.18 p.m.", // -99999--99999--99999 20:09:18
                "07.08.17	am", // -99999--99999--99999 07:08:17
                "08.09.18	p.m.", // -99999--99999--99999 20:09:18

// timelong24.parse
                "01:00:03", // -99999--99999--99999 01:00:03
                "13:03:12", // -99999--99999--99999 13:03:12
                "24:03:12", // -99999--99999--99999 24:03:12
                "01.00.03", // -99999--99999--99999 01:00:03
                "13.03.12", // -99999--99999--99999 13:03:12
                "24.03.12", // -99999--99999--99999 24:03:12

// timeshort12.parse
                "01:00am", // -99999--99999--99999 01:00:00
                "01:03pm", // -99999--99999--99999 13:03:00
                "12:31 A.M.", // -99999--99999--99999 00:31:00
                "08:13 P.M.", // -99999--99999--99999 20:13:00
                "11:59 AM", // -99999--99999--99999 11:59:00
                "06:12 PM", // -99999--99999--99999 18:12:00
                "07:08 am", // -99999--99999--99999 07:08:00
                "08:09 p.m.", // -99999--99999--99999 20:09:00
                "01.00am", // -99999--99999--99999 01:00:00
                "01.03pm", // -99999--99999--99999 13:03:00
                "12.31 A.M.", // -99999--99999--99999 00:31:00
                "08.13 P.M.", // -99999--99999--99999 20:13:00
                "11.59 AM", // -99999--99999--99999 11:59:00
                "06.12 PM", // -99999--99999--99999 18:12:00
                "07.08 am", // -99999--99999--99999 07:08:00
                "08.09 p.m.", // -99999--99999--99999 20:09:00
                "07.08	am", // -99999--99999--99999 07:08:00
                "08.09	p.m.", // -99999--99999--99999 20:09:00

// timeshort24.parse
                "01:00", // -99999--99999--99999 01:00:00
                "13:03", // -99999--99999--99999 13:03:00
                "01.00", // -99999--99999--99999 01:00:00
                "13.03", // -99999--99999--99999 13:03:00

                // php 不支持 @ts\.\d+ 格式
// timestamp.parse
//                "@1508765076.3", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.300000
//                "@1508765076.34", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.340000
//                "@1508765076.347", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.347000
                // php 把 3470 解析成 year 了...
//                "@1508765076.3470", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.347000
//                "@1508765076.34700", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.347000
//                "@1508765076.347000", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.347000
//                "@1508765076.03", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.030000
//                "@1508765076.003", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.003000
//                "@1508765076.0003", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.000300 // php 把 0003 解析成 year 了
//                "@1508765076.00003", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.000030
//                "@1508765076.000003", // 1970-01-01 00:00:00 GMT 00000  0Y   0M   0D /   0H   0M 1508765076S 0.000003

// timetiny12.parse
                "01am", // -99999--99999--99999 01:00:00
                "01pm", // -99999--99999--99999 13:00:00
                "12 A.M.", // -99999--99999--99999 00:00:00
                "08 P.M.", // -99999--99999--99999 20:00:00
                "11 AM", // -99999--99999--99999 11:00:00
                "06 PM", // -99999--99999--99999 18:00:00
                "07 am", // -99999--99999--99999 07:00:00
                "08 p.m.", // -99999--99999--99999 20:00:00
                "09	am", // -99999--99999--99999 09:00:00
                "10	p.m.", // -99999--99999--99999 22:00:00

// tz-identifier.parse
                "01:00:03.12345 Europe/Amsterdam", // -99999--99999--99999 01:00:03 0.123450 Europe/Amsterdam
                "01:00:03.12345 America/Indiana/Knox", // -99999--99999--99999 01:00:03 0.123450 America/Indiana/Knox
                "2005-07-14 22:30:41 America/Los_Angeles", // 2005-07-14 22:30:41 America/Los_Angeles
                "2005-07-14	22:30:41	America/Los_Angeles", // 2005-07-14 22:30:41 America/Los_Angeles
                "Africa/Dar_es_Salaam", // -99999--99999--99999 -99999:-99999:-99999 Africa/Dar_es_Salaam
                "Africa/Porto-Novo", // -99999--99999--99999 -99999:-99999:-99999 Africa/Porto-Novo
                "America/Blanc-Sablon", // -99999--99999--99999 -99999:-99999:-99999 America/Blanc-Sablon
                "America/Port-au-Prince", // -99999--99999--99999 -99999:-99999:-99999 America/Port-au-Prince
                "America/Port_of_Spain", // -99999--99999--99999 -99999:-99999:-99999 America/Port_of_Spain
                "Antarctica/DumontDUrville", // -99999--99999--99999 -99999:-99999:-99999 Antarctica/DumontDUrville
                "Antarctica/McMurdo", // -99999--99999--99999 -99999:-99999:-99999 Antarctica/McMurdo

// tzcorrection.parse
                "+4:30", // -99999--99999--99999 -99999:-99999:-99999 GMT 16200
                "+4", // -99999--99999--99999 -99999:-99999:-99999 GMT 14400
                "+1", // -99999--99999--99999 -99999:-99999:-99999 GMT 03600
                "+14", // -99999--99999--99999 -99999:-99999:-99999 GMT 50400
                // !!!  php 处理成+42:00, 这里处理成 +04:02
                // "+42", // -99999--99999--99999 -99999:-99999:-99999 GMT 151200
                "+4:0", // -99999--99999--99999 -99999:-99999:-99999 GMT 14400
                "+4:01", // -99999--99999--99999 -99999:-99999:-99999 GMT 14460
                "+4:30", // -99999--99999--99999 -99999:-99999:-99999 GMT 16200
                "+401", // -99999--99999--99999 -99999:-99999:-99999 GMT 14460
                "+402", // -99999--99999--99999 -99999:-99999:-99999 GMT 14520
                "+430", // -99999--99999--99999 -99999:-99999:-99999 GMT 16200
                "+0430", // -99999--99999--99999 -99999:-99999:-99999 GMT 16200
                "+04:30", // -99999--99999--99999 -99999:-99999:-99999 GMT 16200
                "+04:9", // -99999--99999--99999 -99999:-99999:-99999 GMT 14940
                "+04:09", // -99999--99999--99999 -99999:-99999:-99999 GMT 14940

// week.parse
                "this week", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   0D /   0H   0M   0S / 1.2
                "this week monday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 1.2
                "this week tuesday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 2.2
                "this week wednesday", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 3.2
                "thursday this week", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 4.2
                "friday this week", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 5.2
                "saturday this week", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 6.2
                "sunday this week", // -99999--99999--99999 00:00:00  0Y   0M   0D /   0H   0M   0S / 0.2
                "last week", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -7D /   0H   0M   0S / 1.2
                "last week monday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 1.2
                "last week tuesday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 2.2
                "last week wednesday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 3.2
                "thursday last week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 4.2
                "friday last week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 5.2
                "saturday last week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 6.2
                "sunday last week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 0.2
                "previous week", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M  -7D /   0H   0M   0S / 1.2
                "previous week monday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 1.2
                "previous week tuesday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 2.2
                "previous week wednesday", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 3.2
                "thursday previous week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 4.2
                "friday previous week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 5.2
                "saturday previous week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 6.2
                "sunday previous week", // -99999--99999--99999 00:00:00  0Y   0M  -7D /   0H   0M   0S / 0.2
                "next week", // -99999--99999--99999 -99999:-99999:-99999  0Y   0M   7D /   0H   0M   0S / 1.2
                "next week monday", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / 1.2
                "next week tuesday", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / 2.2
                "next week wednesday", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / 3.2
                "thursday next week", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / 4.2
                "friday next week", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / 5.2
                "saturday next week", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / 6.2
                "sunday next week", // -99999--99999--99999 00:00:00  0Y   0M   7D /   0H   0M   0S / 0.2

// weeknr.parse
                "1995W051", // 1995-01-01 -99999:-99999:-99999  0Y   0M  29D /   0H   0M   0S
                "2004W30", // 2004-01-01 -99999:-99999:-99999  0Y   0M 200D /   0H   0M   0S
                "1995-W051", // 1995-01-01 -99999:-99999:-99999  0Y   0M  29D /   0H   0M   0S
                "2004-W30", // 2004-01-01 -99999:-99999:-99999  0Y   0M 200D /   0H   0M   0S
                "1995W05-1", // 1995-01-01 -99999:-99999:-99999  0Y   0M  29D /   0H   0M   0S
                "1995-W05-1", // 1995-01-01 -99999:-99999:-99999  0Y   0M  29D /   0H   0M   0S

// year-long.parse
                "+10000-01-01T00:00:00", // 10000-01-01 00:00:00
                // php 处理成 +99:00 999-01-01T00:00:00
                // java 处理成 +09:09 999-01-01T00:00:00
                // "+99999-01-01T00:00:00", // 99999-01-01 00:00:00
                "+100000-01-01T00:00:00", // 100000-01-01 00:00:00
                "❌+4294967296-01-01T00:00:00", // 4294967296-01-01 00:00:00
                "❌+9223372036854775807-01-01T00:00:00", // 9223372036854775807-01-01 00:00:00
                "-10000-01-01T00:00:00", // -10000-01-01 00:00:00
                // php 处理成 -99:00 999-01-01T00:00:00
                // java 处理成 -09:09 999-01-01T00:00:00
                // "-99999-01-01T00:00:00", // -99999-01-01 00:00:00
                "-100000-01-01T00:00:00", // -100000-01-01 00:00:00
                "❌-4294967296-01-01T00:00:00", // -4294967296-01-01 00:00:00
                "❌-9223372036854775807-01-01T00:00:00", // -9223372036854775807-01-01 00:00:00
        };

        for (String test_case : test_cases) {
            System.out.println(test_case);
            if (test_case.startsWith("❌")) {
                try {
                    java_strtotime(test_case.substring("❌".length()));
                    Assert.fail();
                } catch (TimeException ignored) {
                    Assert.assertTrue(true);
                }
            } else {
                Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
            }
        }

        TimeZone.setDefault(defaultZone);
    }

    @Test
    public void test_simple_cases() {
        TimeZone defaultZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        String[] test_cases = {
                "now",
                "NOW",
                "yesterday",
                "tomorrow",
                "noon",
                "midnight",
                "yesterday noon",
                "saturday this week",

                "next year",
                "next month",
                "last day",
                "last wed",

                "this week",
                "next week",
                "previous week",
                "last week",

                "monday","mon","tuesday","tue","wednesday","wed","thursday","thu","friday","fri","saturday","sat","sunday","sun",

                "Sun 2017-01-01",
                "Mon 2017-01-02",

                "19970523091528",
                "20001231185859",
                "20800410101010",

                "10:01:20 AM July 2 1963",

                "fourth thursday of november", // 今年感恩节
                "fourth thursday of november 2019", // 2019 感恩节
                "2019 fourth thursday of november", // 这个不对
                "2018 fourth thursday of november", // 这个不对


                "february first day of last month midnight",
                "february first day of this month midnight - 1 second",

                // invalid
                // "0000-00-00 00:00:00",

                // Support week numbers in strtotime()
                "2004W30",

                "March 1 eighth day 2009",

                "2004-08-09 14:48:27.304809+10", // postgres
                "-8:0",
                "2006-1-6T0:0:0-8:0", // wddx

                "20 VI. 2005",

                "12:59:59.1234567am", // mssqltime
                "10/22", // AmericanShort
                "20-10-22", // reISO8601Date2

                "+9999-01-01", //iso8601datex
                "-9999-01-01", //iso8601datex

                "22.10.20", // rePointedDate2
                "22\t10.20", // rePointedDate2

                "1990:10:22 20:15:00", // exif

                "jan 1th 12:12:12am", // dateshortwithtimelong12
                "jan 1th 12:12:12utc", // dateshortwithtimelongtz

                "+5 weekdays",
                "+5 weekdays ago",


                // "YYYYMMDDhhmmss [ZZZ]
                "20050620091407 GMT",

                "2006-12-12 10:00:00.5",
                "2006-12-12 10:00:00.5 +1 week +1 hour",

                "1 Jul 06 14:27:30 +0200",

                // Bug #35499 (strtotime() does not handle whitespace around the date string)
//                "11/20/2005 8:00 AM \r\n",
//                "  11/20/2005 8:00 AM \r\n",
                "11/20/2005 8:00 AM ",
                "  11/20/2005 8:00 AM ",

                "10:00:00 AM July 1 2005",

                "1 Jul 06 14:27:30 +0200",

                "2006-05-30T14:32:13+02:00",

                "2017-12-31 23:59:59.999999 +1 microsecond",
                "2017-12-31 23:59:59.999999 +2 microsecond",
        };
        for (String test_case : test_cases) {
            if (test_case.equalsIgnoreCase("now")) {
                Assert.assertTrue(Math.abs(java_strtotime(test_case) - php_strtotime(test_case)) <= 1);
            } else {
                Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
            }
        }


        TimeZone.setDefault(TimeZone.getTimeZone("US/Eastern"));
        String[] test_cases1 = {
                "Sep 04 16:39:45 2001",
                "Sep 04 2001 16:39:45",
        };
        for (String test_case : test_cases1) {
            // Assert.assertTrue(Math.abs(java_strtotime(test_case) - php_strtotime(test_case)) <= 1);
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }


        TimeZone.setDefault(TimeZone.getTimeZone("Atlantic/Azores"));
        String[] test_cases2 = {
                "2012-02-29 12:00:00 +0000",
        };
        for (String test_case : test_cases2) {
            // Assert.assertTrue(Math.abs(java_strtotime(test_case) - php_strtotime(test_case)) <= 1);
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }


        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Lisbon"));
        String[] test_cases3 = {
                "2012-01-01 00:00:00 Europe/Rome",
                "2012-01-01 00:00:00 PST",
                "2012-01-01 00:00:00 +03:40",
        };
        for (String test_case : test_cases3) {
            // Assert.assertTrue(Math.abs(java_strtotime(test_case) - php_strtotime(test_case)) <= 1);
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }



        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Lisbon"));
        String[] test_cases4 = {
                "22:49:12",
                "22.49.12.42GMT",
                "t0222",
                "t0222 t0222",
                "022233",
                "2-3-2004",
                "2.3.2004",
                "20060212T23:12:23UTC",
                "2006167", //pgydotd
                "Jan-15-2006", //pgtextshort
                "2006-Jan-15", //pgtextreverse
                "10/Oct/2000:13:55:36 +0100", //clf
                "2006",
                "1986",
                "JAN",
                "January",
        };
        for (String test_case : test_cases4) {
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }


        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
        String[] test_cases5 = {
                "2020-11-26T18:51:44+01:00", // DATE_ATOM
                "Thursday, 26-Nov-2020 18:51:44 CET", // DATE_COOKIE
                "2020-11-26T18:51:44+0100", // DATE_ISO8601
                "Thu, 26 Nov 20 18:51:44 +0100", // DATE_RFC822
                "Thursday, 26-Nov-20 18:51:44 CET", // DATE_RFC850
                "Thu, 26 Nov 20 18:51:44 +0100", // DATE_RFC1036
                "Thu, 26 Nov 2020 18:51:44 +0100", // DATE_RFC1123
                "Thu, 26 Nov 2020 18:51:44 +0100", // DATE_RFC2822
                "2020-11-26T18:51:44+01:00", // DATE_RFC3339
                "Thu, 26 Nov 2020 18:51:44 +0100", // DATE_RSS
                "2020-11-26T18:51:44+01:00", // DATE_W3C
        };
        for (String test_case : test_cases5) {
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }


        String[] test_cases6 = {
                "1999-10-13",
                "Oct 13  1999",
                "2000-01-19",
                "Jan 19  2000",
                "2001-12-21",
                "Dec 21  2001",
                "2001-12-21 12:16",
                "Dec 21 2001 12:16",
                "Dec 21  12:16",
                "2001-10-22 21:19:58",
                "2001-10-22 21:19:58-02",
                "2001-10-22 21:19:58-0213",
                "2001-10-22 21:19:58+02",
                "2001-10-22 21:19:58+0213",
                "2001-10-22T21:20:58-03:40",
                "2001-10-22T211958-2",
                "20011022T211958+0213",
                "20011022T21:20+0215",
                "1997W011",
                "2004W101T05:00+0",
        };
        TimeZone.setDefault(TimeZone.getTimeZone("GMT0"));
        for (String test_case : test_cases6) {
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }
        TimeZone.setDefault(TimeZone.getTimeZone("US/Eastern"));
        for (String test_case : test_cases6) {
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }



        String[] test_cases7 = {
                "12am", "1am", "1pm",
                "12a.m.", "1a.m.", "1p.m.",
                "12:00am", "1:00am", "1:00pm",
                "12:00a.m.", "1:00a.m.", "1:00p.m."
        };
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        for (String test_case : test_cases7) {
            Assert.assertEquals(java_strtotime("2005-12-22 " + test_case), php_strtotime("2005-12-22 " + test_case));
        }



        String[] test_cases8 = {
                // offset around a day
                "80412 seconds",
                "86399 seconds",
                "86400 seconds",
                "86401 seconds",
                "112913 seconds",

                // offset around 7 days
                "134 hours",
                "167 hours",
                "168 hours",
                "169 hours",
                "183 hours",

                // offset around 6 months
                "178 days",
                "179 days",
                "180 days",
                "183 days",
                "184 days",

                // offset around 10 years
                "115 months",
                "119 months",
                "120 months",
                "121 months",
                "128 months",

                // offset around 25 years (can"t do much more reliably with strtotime)
                "24 years",
                "25 years",
                "26 years",


                // around 10 leap year periods (4000 years) in days
                "1460000 days",
                "1460969 days",
                "1460970 days",
                "1460971 days",
                "1462970 days",

                // around 1 leap year period in years
                "398 years",
                "399 years",
                "400 years",
                "401 years",

                // around 40000 years
                "39755 years",
                "39999 years",
                "40000 years",
                "40001 years",
                "41010 years",

                // bigger than int (32-bit)
                "10000000000 seconds",
                "10000000000 minutes",
                "10000000000 hours",
                "10000000000 days",
                "10000000000 months",
                "10000000 years",  // "10000000000 years", php支持 java不支持 Instant.MAX_SECOND
        };
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        long base_time = 1204200000; // 28 Feb 2008 12:00:00
        for (String test_case : test_cases8) {
            Assert.assertEquals(java_strtotime("+" + test_case, base_time), php_strtotime("+" + test_case, base_time));
            Assert.assertEquals(java_strtotime("-" + test_case, base_time), php_strtotime("-" + test_case, base_time));
        }

        String[] test_cases9 = {
                "28 Feb 2008",
                "28 Feb 2008 12:00:00",
                "28 Feb 2008 12:00:00 +1 days",
                "28 Feb 2008 12:00:00 -1 days",

                "28 Feb 2008 12:00:00 +1460000 days",
                "28 Feb 2008 12:00:00 -1460000 days",
                "28 Feb 2008 12:00:00 +1460969 days",
                "28 Feb 2008 12:00:00 -1460969 days",
                "28 Feb 2008 12:00:00 +1460970 days",
                "28 Feb 2008 12:00:00 -1460970 days",
                "28 Feb 2008 12:00:00 +1460971 days",
                "28 Feb 2008 12:00:00 -1460971 days",
                "28 Feb 2008 12:00:00 +1462970 days",
                "28 Feb 2008 12:00:00 -1462970 days",
                "28 Feb 2008 12:00:00 +398 years",
                "28 Feb 2008 12:00:00 -398 years",
                "28 Feb 2008 12:00:00 +399 years",
                "28 Feb 2008 12:00:00 -399 years",
                "28 Feb 2008 12:00:00 +400 years",
                "28 Feb 2008 12:00:00 -400 years",
                "28 Feb 2008 12:00:00 +401 years",
                "28 Feb 2008 12:00:00 -401 years",
                "28 Feb 2008 12:00:00 +39755 years",
                "28 Feb 2008 12:00:00 -39755 years",
                "28 Feb 2008 12:00:00 +39999 years",
                "28 Feb 2008 12:00:00 -39999 years",
                "28 Feb 2008 12:00:00 +40000 years",
                "28 Feb 2008 12:00:00 -40000 years",
                "28 Feb 2008 12:00:00 +40001 years",
                "28 Feb 2008 12:00:00 -40001 years",
                "28 Feb 2008 12:00:00 +41010 years",
                "28 Feb 2008 12:00:00 -41010 years",
                "28 Feb 2008 12:00:00 +10000000000 seconds",
                "28 Feb 2008 12:00:00 -10000000000 seconds",
                "28 Feb 2008 12:00:00 +10000000000 minutes",
                "28 Feb 2008 12:00:00 -10000000000 minutes",
                "28 Feb 2008 12:00:00 +10000000000 hours",
                "28 Feb 2008 12:00:00 -10000000000 hours",
                "28 Feb 2008 12:00:00 +10000000000 days",
                "28 Feb 2008 12:00:00 -10000000000 days",
                "28 Feb 2008 12:00:00 +10000000000 months",
                "28 Feb 2008 12:00:00 -10000000000 months",
                "28 Feb 2008 12:00:00 +100000000 years",
                "28 Feb 2008 12:00:00 -100000000 years",

        };
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        for (String test_case : test_cases9) {
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }

        String[] test_cases10 = {
                "7.8.2010",
        };
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kiev"));
        for (String test_case : test_cases10) {
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }

        String[] test_cases11 = {
                "a"," a ",
                "A"," A ",
        };
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        for (String test_case : test_cases11) {
            Assert.assertEquals(java_strtotime(test_case), php_strtotime(test_case));
        }


        TimeZone.setDefault(defaultZone);
    }

    @Test(expected = TimeException.class)
    public void test_invalid() {
        parse("00.00.0000 - 00:00:00");
    }

    @Test
    public void test_php_bugs() {
        {
            // Bug #20382 [1] (strtotime ("Monday", $date) produces wrong result on DST changeover)
            TimeZone defaultZone = TimeZone.getDefault();
            date_default_timezone_set("Europe/Amsterdam");
            Assert.assertEquals(
                    java_strtotime("Monday", java_mktime(17, 17, 17, 10, 27, 2004)),
                    php_strtotime("Monday", php_mktime(17, 17, 17, 10, 27, 2004))
            );
            TimeZone.setDefault(defaultZone);
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();
            date_default_timezone_set("Europe/Andorra");
            Assert.assertEquals(java_strtotime("@" + java_strtotime("2012-08-22 00:00:00 CEST")), php_strtotime("@" + php_strtotime("2012-08-22 00:00:00 CEST")));
            TimeZone.setDefault(defaultZone);
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();

            // https://github.com/php/php-src/blob/master/ext/date/tests/bug20382-2.phpt
            // Bug #20382 [2] (strtotime ("Monday", $date) produces wrong result on DST changeover)
            date_default_timezone_set("Europe/Andorra");
            Assert.assertEquals(java_strtotime("first monday", 2139578237), php_strtotime("first monday", 2139578237));
            date_default_timezone_set("Asia/Dubai");
            Assert.assertEquals(java_strtotime("first monday", 47837), php_strtotime("first monday", 47837));
            date_default_timezone_set("Asia/Kabul");
            Assert.assertEquals(java_strtotime("first monday", 46037), php_strtotime("first monday", 46037));
            date_default_timezone_set("America/Antigua");
            Assert.assertEquals(java_strtotime("first monday", 76637), php_strtotime("first monday", 76637));
            date_default_timezone_set("America/Anguilla");
            Assert.assertEquals(java_strtotime("first monday", 76637), php_strtotime("first monday", 76637));
            date_default_timezone_set("Europe/Tirane");
            Assert.assertEquals(java_strtotime("first monday", 418925837), php_strtotime("first monday", 418925837));
            date_default_timezone_set("Asia/Yerevan");
            Assert.assertEquals(java_strtotime("first monday", 2139571037), php_strtotime("first monday", 2139571037));
            date_default_timezone_set("America/Curacao");
            Assert.assertEquals(java_strtotime("first monday", 76637), php_strtotime("first monday", 76637));
            date_default_timezone_set("Africa/Luanda");
            Assert.assertEquals(java_strtotime("first monday", 58637), php_strtotime("first monday", 58637));
            date_default_timezone_set("Antarctica/McMurdo");
            Assert.assertEquals(java_strtotime("first monday", 2137724237), php_strtotime("first monday", 2137724237));
            date_default_timezone_set("Australia/Adelaide");
            Assert.assertEquals(java_strtotime("first monday", 31564037), php_strtotime("first monday", 31564037));
            date_default_timezone_set("Australia/Darwin");
            Assert.assertEquals(java_strtotime("first monday", 39080837), php_strtotime("first monday", 39080837));
            date_default_timezone_set("Australia/Perth");
            Assert.assertEquals(java_strtotime("first monday", 31569437), php_strtotime("first monday", 31569437));
            date_default_timezone_set("America/Aruba");
            Assert.assertEquals(java_strtotime("first monday", 39129437), php_strtotime("first monday", 39129437));
            date_default_timezone_set("Asia/Baku");
            Assert.assertEquals(java_strtotime("first monday", 31583837), php_strtotime("first monday", 31583837));
            date_default_timezone_set("Europe/Sarajevo");
            Assert.assertEquals(java_strtotime("first monday", 31594637), php_strtotime("first monday", 31594637));
            date_default_timezone_set("America/Barbados");
            Assert.assertEquals(java_strtotime("first monday", 31612637), php_strtotime("first monday", 31612637));
            date_default_timezone_set("Asia/Dacca");
            Assert.assertEquals(java_strtotime("first monday", 31576637), php_strtotime("first monday", 31576637));
            date_default_timezone_set("Europe/Brussels");
            Assert.assertEquals(java_strtotime("first monday", 31594637), php_strtotime("first monday", 31594637));
            date_default_timezone_set("Africa/Ouagadougou");
            Assert.assertEquals(java_strtotime("first monday", 39115037), php_strtotime("first monday", 39115037));
            date_default_timezone_set("Europe/Tirane");
            Assert.assertEquals(java_strtotime("first monday", 418925837), php_strtotime("first monday", 418925837));
            date_default_timezone_set("America/Buenos_Aires");
            Assert.assertEquals(java_strtotime("first monday", 149804237), php_strtotime("first monday", 149804237));
            date_default_timezone_set("America/Rosario");
            Assert.assertEquals(java_strtotime("first monday", 149804237), php_strtotime("first monday", 149804237));
            date_default_timezone_set("Europe/Vienna");
            Assert.assertEquals(java_strtotime("first monday", 323367437), php_strtotime("first monday", 323367437));
            date_default_timezone_set("Asia/Baku");
            Assert.assertEquals(java_strtotime("first monday", 819897437), php_strtotime("first monday", 819897437));

            TimeZone.setDefault(defaultZone);
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("GMT0"));

            // Bug #26317 (military timezone offset signedness)
            Assert.assertEquals(java_strtotime("2003-11-19 16:20:42 Z"), php_strtotime("2003-11-19 16:20:42 Z"));
            Assert.assertEquals(java_strtotime("2003-11-19 09:20:42 T"), php_strtotime("2003-11-19 09:20:42 T"));
            Assert.assertEquals(java_strtotime("2003-11-19 19:20:42 C"), php_strtotime("2003-11-19 19:20:42 C"));

            // Bug #26320 (strtotime handling of XML Schema/ISO 8601 format)
            Assert.assertEquals(java_strtotime("2003-11-19T12:30:42"), php_strtotime("2003-11-19T12:30:42"));
            Assert.assertEquals(java_strtotime("2003-11-19T12:30:42Z"), php_strtotime("2003-11-19T12:30:42Z"));

            TimeZone.setDefault(defaultZone);
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();

            // Bug #14561 (strtotime() bug)
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug14561.phpt
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

            Assert.assertEquals(java_strtotime("19:30 Dec 17 2005"), php_strtotime("19:30 Dec 17 2005"));
            Assert.assertEquals(java_strtotime("Dec 17 19:30 2005"), php_strtotime("Dec 17 19:30 2005"));

            // Bug #33563 (strtotime('+1 month',$abc) cant get right time)
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug33563.phpt
            Assert.assertEquals(java_strtotime("2005-06-30 21:04:23"), php_strtotime("2005-06-30 21:04:23"));
            Assert.assertEquals(java_strtotime("+1 month", java_strtotime("2005-06-30 21:04:23")), php_strtotime("+1 month", php_strtotime("2005-06-30 21:04:23")));

            // Bug #26198 (strtotime handling of "M Y" and "Y M" format)
            Assert.assertEquals(java_strtotime("Oct 2001"), php_strtotime("Oct 2001"));
            Assert.assertEquals(java_strtotime("2001 Oct"), php_strtotime("2001 Oct"));

            // Bug #36510 (strtotime() fails to parse date strings with tabs)
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug36510.phpt
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Assert.assertEquals(java_strtotime("-2 hours", 1140973388), php_strtotime("-2 hours", 1140973388));
            Assert.assertEquals(java_strtotime("-2\thours", 1140973388), php_strtotime("-2\thours", 1140973388));

            // https://github.com/php/php-src/blob/master/ext/date/tests/bug45081.phpt
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Assert.assertEquals(java_strtotime("2008-05-23 00:00:00 +08"), php_strtotime("2008-05-23 00:00:00 +08"));
            Assert.assertEquals(java_strtotime("2008-05-23 00:00:00"), php_strtotime("2008-05-23 00:00:00"));

            // https://github.com/php/php-src/blob/master/ext/date/tests/bug28024.phpt
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
            Assert.assertEquals(java_strtotime("17:00 2004-01-01"), php_strtotime("17:00 2004-01-01"));
            Assert.assertEquals(java_strtotime("17:00 2004-01-01"), php_strtotime("17:00 2004-01-01"));

            // https://github.com/php/php-src/blob/master/ext/date/tests/bug26090.phpt
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            Assert.assertEquals(java_strtotime("2003-10-28 10:20:30-0800"), php_strtotime("2003-10-28 10:20:30-0800"));
            Assert.assertEquals(java_strtotime("2003-10-28 10:20:30-08:00"), php_strtotime("2003-10-28 10:20:30-08:00"));

            // https://github.com/php/php-src/blob/master/ext/date/tests/bug33536.phpt
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            Assert.assertEquals(java_strtotime("Sun, 13 Nov 2005 22:56:10 -0800 (PST)"), php_strtotime("Sun, 13 Nov 2005 22:56:10 -0800 (PST)"));
            Assert.assertEquals(java_strtotime("Sun, 13 Nov 2005 22:56:10 -0800"), php_strtotime("Sun, 13 Nov 2005 22:56:10 -0800"));

            Assert.assertEquals(java_strtotime("July 1, 2000 00:00:00 UTC"), php_strtotime("July 1, 2000 00:00:00 UTC"));
            Assert.assertEquals(java_strtotime("July 1, 2000 00:00:00 GMT"), php_strtotime("July 1, 2000 00:00:00 GMT"));

            Assert.assertEquals(java_strtotime("+ 1 day", 1133216119), php_strtotime("+ 1 day", 1133216119));
            Assert.assertEquals(java_strtotime("+ 1 month", 1133216119), php_strtotime("+ 1 month", 1133216119));
            Assert.assertEquals(java_strtotime("+ 1 week", 1133216119), php_strtotime("+ 1 week", 1133216119));

            // https://github.com/php/php-src/blob/49a4e695845bf55e059e7f88e54b1111fe284223/ext/date/tests/bug35699.phpt
            // Bug #35699 (date() can't handle leap years before 1970)
            Assert.assertEquals(java_strtotime("1964-06-06"), php_strtotime("1964-06-06"));
            Assert.assertEquals(java_strtotime("1963-06-06"), php_strtotime("1963-06-06"));
            Assert.assertEquals(java_strtotime("1964-01-06"), php_strtotime("1964-01-06"));

            // Bug #28088 (strtotime() cannot convert 00 hours")
            Assert.assertEquals(java_strtotime("04/04/04 2345"), php_strtotime("04/04/04 2345"));
            Assert.assertEquals(java_strtotime("04/04/04 0045"), php_strtotime("04/04/04 0045"));


            // Bug #28599 (strtotime fails with zero base time)
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"));
            Assert.assertEquals(java_strtotime("+30 minutes", 1100535573), php_strtotime("+30 minutes", 1100535573));

            // Bug #33452 (Support for year accompanying ISO week nr)
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            Assert.assertEquals(java_strtotime("2005-1-1"), php_strtotime("2005-1-1"));

            date_default_timezone_set("UTC");
            Assert.assertEquals(java_strtotime("2 January 2005"), php_strtotime("2 January 2005"));
            Assert.assertEquals(java_strtotime("9 January 2005"), php_strtotime("9 January 2005"));

            // Bug #35630 (strtotime() crashes on non-separated relative modifiers)
            Assert.assertEquals(java_strtotime("5 january 2006+3day+1day"), php_strtotime("5 january 2006+3day+1day"));

            // Bug #35705 (strtotime() fails to parse soap date format without TZ)
            Assert.assertEquals(java_strtotime("2000-10-10T10:12:30.000"), php_strtotime("2000-10-10T10:12:30.000"));
            Assert.assertEquals(java_strtotime("2000-10-10T10:12:30.000+12:34"), php_strtotime("2000-10-10T10:12:30.000+12:34"));

            // Bug #37368 (Incorrect timestamp returned for strtotime()).
            Assert.assertEquals(java_strtotime("Mon, 08 May 2006 13:06:44 -0400 +30 days"), php_strtotime("Mon, 08 May 2006 13:06:44 -0400 +30 days"));

            date_default_timezone_set("Europe/Oslo");
            Assert.assertEquals(java_strtotime("2006-01-31T19:23:56Z"), php_strtotime("2006-01-31T19:23:56Z"));
            Assert.assertEquals(java_strtotime("2006-01-31T19:23:56"), php_strtotime("2006-01-31T19:23:56"));

            // Bug #26694 (strtotime() request for "Sun, 21 Dec 2003 20:38:33 +0000 GMT")
            date_default_timezone_set("GMT");
            Assert.assertEquals(java_strtotime("Sun, 21 Dec 2003 20:38:33 +0000 GMT"), php_strtotime("Sun, 21 Dec 2003 20:38:33 +0000 GMT"));




            TimeZone.setDefault(defaultZone);
        }


        {
            // Checking whisky time
            // https://github.com/php/php-src/blob/master/ext/date/tests/strtotime_variation_scottish.phpt
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Assert.assertEquals(java_strtotime("back of 7"), php_strtotime("back of 7"));
            Assert.assertEquals(java_strtotime("front of 7"), php_strtotime("front of 7"));
            Assert.assertEquals(java_strtotime("back of 19"), php_strtotime("back of 19"));
            Assert.assertEquals(java_strtotime("front of 19"), php_strtotime("front of 19"));

            Assert.assertEquals(java_strtotime("back of 7 am"), php_strtotime("back of 7 am"));
            Assert.assertEquals(java_strtotime("back of 7 pm"), php_strtotime("back of 7 pm"));
            Assert.assertEquals(java_strtotime("front of 7 am"), php_strtotime("front of 7 am"));
            Assert.assertEquals(java_strtotime("front of 7 pm"), php_strtotime("front of 7 pm"));
            Assert.assertEquals(java_strtotime("back of 19 am"), php_strtotime("back of 19 am"));
            Assert.assertEquals(java_strtotime("back of 19 pm"), php_strtotime("back of 19 pm"));
            Assert.assertEquals(java_strtotime("front of 19 am"), php_strtotime("front of 19 am"));
            Assert.assertEquals(java_strtotime("front of 19 pm"), php_strtotime("front of 19 pm"));

            TimeZone.setDefault(defaultZone);
        }


        {
            // Bug #32270 (strtotime/date behavior)
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug32270.phpt
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));

            Assert.assertEquals(java_strtotime("Jan 1 1902"), php_strtotime("Jan 1 1902"));
            Assert.assertEquals(java_strtotime("Jan 1 1950"), php_strtotime("Jan 1 1950"));
            Assert.assertEquals(java_strtotime("Jan 1 2000"), php_strtotime("Jan 1 2000"));

            TimeZone.setDefault(defaultZone);
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

            Assert.assertEquals(java_strtotime("2006-1"), php_strtotime("2006-1"));
            Assert.assertEquals(java_strtotime("2006-03"), php_strtotime("2006-03"));
            Assert.assertEquals(java_strtotime("2006-12"), php_strtotime("2006-12"));

            TimeZone.setDefault(defaultZone);
        }

        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug33056.phpt
            // Bug #33056 (strtotime() does not parse 20050518t090000Z)

            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

            Assert.assertEquals(java_strtotime("20050518t090000Z"), php_strtotime("20050518t090000Z"));
            Assert.assertEquals(java_strtotime("20050518t091234Z"), php_strtotime("20050518t091234Z"));
            Assert.assertEquals(java_strtotime("20050518t191234Z"), php_strtotime("20050518t191234Z"));

            TimeZone.setDefault(defaultZone);
        }

        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug33414-1.phpt
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug33414-2.phpt
            // Bug #33414 [2] (Comprehensive list of incorrect days returned after strotime() / date() tests)

            long tStamp = java_mktime("America/Mendoza", 17, 17, 17, 1, 8327, 1970);
            Assert.assertEquals(java_strtotime("next Sunday", tStamp), php_strtotime("next Sunday", tStamp));


            tStamp = java_mktime("America/Catamarca", 17, 17, 17, 1, 7599, 1970);
            Assert.assertEquals(java_strtotime("next Sunday", tStamp), php_strtotime("next Sunday", tStamp));


            tStamp = java_mktime("America/Cordoba", 17, 17, 17, 1, 7599, 1970);
            Assert.assertEquals(java_strtotime("next Sunday", tStamp), php_strtotime("next Sunday", tStamp));


            tStamp = java_mktime("America/Rosario", 17, 17, 17, 1, 7958, 1970);
            Assert.assertEquals(java_strtotime("next Tuesday", tStamp), php_strtotime("next Tuesday", tStamp));


            tStamp = java_mktime("Europe/Vienna", 17, 17, 17, 1, 3746, 1970);
            Assert.assertEquals(java_strtotime("next Thursday", tStamp), php_strtotime("next Thursday", tStamp));


            tStamp = java_mktime("Asia/Baku", 17, 17, 17, 1, 8299, 1970);
            Assert.assertEquals(java_strtotime("second Monday", tStamp), php_strtotime("second Monday", tStamp));


            tStamp = java_mktime("America/Noronha", 17, 17, 17, 1, 10866, 1970);
            Assert.assertEquals(java_strtotime("next Friday", tStamp), php_strtotime("next Friday", tStamp));


            tStamp = java_mktime("America/Havana", 17, 17, 17, 1, 12720, 1970);
            Assert.assertEquals(java_strtotime("next Thursday", tStamp), php_strtotime("next Thursday", tStamp));


            tStamp = java_mktime("Europe/Tallinn", 17, 17, 17, 1, 11777, 1970);
            Assert.assertEquals(java_strtotime("next Saturday", tStamp), php_strtotime("next Saturday", tStamp));


            tStamp = java_mktime("Asia/Jerusalem", 17, 17, 17, 1, 13056, 1970);
            Assert.assertEquals(java_strtotime("next Thursday", tStamp), php_strtotime("next Thursday", tStamp));


            tStamp = java_mktime("Europe/Vilnius", 17, 17, 17, 1, 12140, 1970);
            Assert.assertEquals(java_strtotime("next Friday", tStamp), php_strtotime("next Friday", tStamp));


            tStamp = java_mktime("Pacific/Kwajalein", 17, 17, 17, 1, 8627, 1970);
            Assert.assertEquals(java_strtotime("next Saturday", tStamp), php_strtotime("next Saturday", tStamp));


            tStamp = java_mktime("Asia/Ulan_Bator", 17, 17, 17, 1, 11588, 1970);
            Assert.assertEquals(java_strtotime("next Saturday", tStamp), php_strtotime("next Saturday", tStamp));


            tStamp = java_mktime("America/Cancun", 17, 17, 17, 1, 11785, 1970);
            Assert.assertEquals(java_strtotime("next Sunday", tStamp), php_strtotime("next Sunday", tStamp));


            tStamp = java_mktime("America/Mexico_City", 17, 17, 17, 1, 11781, 1970);
            Assert.assertEquals(java_strtotime("next Wednesday", tStamp), php_strtotime("next Wednesday", tStamp));


            tStamp = java_mktime("America/Mazatlan", 17, 17, 17, 1, 11780, 1970);
            Assert.assertEquals(java_strtotime("next Tuesday", tStamp), php_strtotime("next Tuesday", tStamp));


            tStamp = java_mktime("America/Chihuahua", 17, 17, 17, 1, 11782, 1970);
            Assert.assertEquals(java_strtotime("next Thursday", tStamp), php_strtotime("next Thursday", tStamp));


            tStamp = java_mktime("Asia/Kuala_Lumpur", 17, 17, 17, 1, 4380, 1970);
            Assert.assertEquals(java_strtotime("next Monday", tStamp), php_strtotime("next Monday", tStamp));


            tStamp = java_mktime("Pacific/Chatham", 17, 17, 17, 1, 1762, 1970);
            Assert.assertEquals(java_strtotime("next Monday", tStamp), php_strtotime("next Monday", tStamp));


            tStamp = java_mktime("America/Lima", 17, 17, 17, 1, 5839, 1970);
            Assert.assertEquals(java_strtotime("next Thursday", tStamp), php_strtotime("next Thursday", tStamp));


            tStamp = java_mktime("Asia/Karachi", 17, 17, 17, 1, 11783, 1970);
            Assert.assertEquals(java_strtotime("next Friday", tStamp), php_strtotime("next Friday", tStamp));


            tStamp = java_mktime("America/Asuncion", 17, 17, 17, 1, 11746, 1970);
            Assert.assertEquals(java_strtotime("next Wednesday", tStamp), php_strtotime("next Wednesday", tStamp));


            tStamp = java_mktime("Asia/Singapore", 17, 17, 17, 1, 4383, 1970);
            Assert.assertEquals(java_strtotime("next Thursday", tStamp), php_strtotime("next Thursday", tStamp));


            tStamp = java_mktime("America/Montevideo", 17, 17, 17, 1, 12678, 1970);
            Assert.assertEquals(java_strtotime("next Thursday", tStamp), php_strtotime("next Thursday", tStamp));


            tStamp = java_mktime("Pacific/Rarotonga", 17, 17, 17, 1, 1, 1970);
            Assert.assertEquals(java_strtotime("next Tuesday", tStamp), php_strtotime("next Tuesday", tStamp));


            tStamp = java_mktime("Atlantic/South_Georgia", 17, 17, 17, 1, 1, 1970);
            Assert.assertEquals(java_strtotime("next Tuesday", tStamp), php_strtotime("next Tuesday", tStamp));


            tStamp = java_mktime("America/Port-au-Prince", 17, 17, 17, 1, 12871, 1970);
            Assert.assertEquals(java_strtotime("next Monday", tStamp), php_strtotime("next Monday", tStamp));


            tStamp = java_mktime("Pacific/Enderbury", 17, 17, 17, 1, 1, 1970);
            Assert.assertEquals(java_strtotime("next Monday", tStamp), php_strtotime("next Monday", tStamp));


            tStamp = java_mktime("Pacific/Kiritimati", 17, 17, 17, 1, 1, 1970);
            Assert.assertEquals(java_strtotime("next Monday", tStamp), php_strtotime("next Monday", tStamp));


            tStamp = java_mktime("America/Managua", 17, 17, 17, 1, 12879, 1970);
            Assert.assertEquals(java_strtotime("next Tuesday", tStamp), php_strtotime("next Tuesday", tStamp));


            tStamp = java_mktime("Pacific/Pitcairn", 17, 17, 17, 1, 1, 1970);
            Assert.assertEquals(java_strtotime("next Wednesday", tStamp), php_strtotime("next Wednesday", tStamp));


            tStamp = java_mktime("Pacific/Fakaofo", 17, 17, 17, 1, 1, 1970);
            Assert.assertEquals(java_strtotime("next Saturday", tStamp), php_strtotime("next Saturday", tStamp));


            tStamp = java_mktime("Pacific/Johnston", 17, 17, 17, 1, 1, 1970);
            Assert.assertEquals(java_strtotime("next Friday", tStamp), php_strtotime("next Friday", tStamp));
        }



        {
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));

            long stamp = 1112427000;
            Assert.assertEquals(java_strtotime("now",stamp), php_strtotime("now",stamp));
            Assert.assertEquals(java_strtotime("tomorrow",stamp), php_strtotime("tomorrow",stamp));
            Assert.assertEquals(java_strtotime("+1 day",stamp), php_strtotime("+1 day",stamp));
            Assert.assertEquals(java_strtotime("+2 day",stamp), php_strtotime("+2 day",stamp));

            TimeZone.setDefault(defaultZone);
        }


        {
            // Bug #37017 (strtotime fails before 13:00:00 with some time zones identifiers).
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug37017.phpt
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

            Assert.assertEquals(java_strtotime("2006-05-12 13:00:01 America/New_York"), php_strtotime("2006-05-12 13:00:01 America/New_York"));
            Assert.assertEquals(java_strtotime("2006-05-12 13:00:00 America/New_York"), php_strtotime("2006-05-12 13:00:00 America/New_York"));
            Assert.assertEquals(java_strtotime("2006-05-12 12:59:59 America/New_York"), php_strtotime("2006-05-12 12:59:59 America/New_York"));
            Assert.assertEquals(java_strtotime("2006-05-12 12:59:59 GMT"), php_strtotime("2006-05-12 12:59:59 GMT"));

            TimeZone.setDefault(defaultZone);
        }


        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug69336.phpt
            // Bug #69336 (Issues with "last day of <monthname>")
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Assert.assertEquals(java_strtotime("last day of april"), php_strtotime("last day of april"));
            Assert.assertEquals(java_strtotime("Last day of april"), php_strtotime("Last day of april"));
            Assert.assertEquals(java_strtotime("lAst Day of April"), php_strtotime("lAst Day of April"));
            Assert.assertEquals(java_strtotime("last tuesday of march 2015"), php_strtotime("last tuesday of march 2015"));
            Assert.assertEquals(java_strtotime("last wednesday of march 2015"), php_strtotime("last wednesday of march 2015"));
            Assert.assertEquals(java_strtotime("last wednesday of april 2015"), php_strtotime("last wednesday of april 2015"));
            Assert.assertEquals(java_strtotime("last wednesday of march 2014"), php_strtotime("last wednesday of march 2014"));
            Assert.assertEquals(java_strtotime("last wednesday of april 2014"), php_strtotime("last wednesday of april 2014"));

            TimeZone.setDefault(defaultZone);
        }


        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug37514.phpt
            // Bug #37514 (strtotime doesn't assume year correctly).
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Assert.assertEquals(java_strtotime("May 18th 5:05", 1168156376), php_strtotime("May 18th 5:05", 1168156376));
            Assert.assertEquals(java_strtotime("May 18th 5:05pm", 1168156376), php_strtotime("May 18th 5:05pm", 1168156376));
            Assert.assertEquals(java_strtotime("May 18th 5:05 pm", 1168156376), php_strtotime("May 18th 5:05 pm", 1168156376));
            Assert.assertEquals(java_strtotime("May 18th 5:05am", 1168156376), php_strtotime("May 18th 5:05am", 1168156376));
            Assert.assertEquals(java_strtotime("May 18th 5:05 am", 1168156376), php_strtotime("May 18th 5:05 am", 1168156376));
            Assert.assertEquals(java_strtotime("May 18th 2006 5:05pm", 1168156376), php_strtotime("May 18th 2006 5:05pm", 1168156376));

            TimeZone.setDefault(defaultZone);
        }


        {
            // Y/m/d
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug34087.phpt
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Assert.assertEquals(java_strtotime("2005/8/12"), php_strtotime("2005/8/12"));
            Assert.assertEquals(java_strtotime("2005-8-12"), php_strtotime("2005-8-12"));

            Assert.assertEquals(java_strtotime("2005/1/2"), php_strtotime("2005/1/2"));
            Assert.assertEquals(java_strtotime("2005/01/02"), php_strtotime("2005/01/02"));
            Assert.assertEquals(java_strtotime("2005/01/2"), php_strtotime("2005/01/2"));
            Assert.assertEquals(java_strtotime("2005/1/02"), php_strtotime("2005/1/02"));

            TimeZone.setDefault(defaultZone);
        }

        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug35414.phpt
            // Bug #35414 (strtotime() no longer works with ordinal suffix)
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            Assert.assertEquals(java_strtotime("Sat 26th Nov 2005 18:18"), php_strtotime("Sat 26th Nov 2005 18:18"));
            Assert.assertEquals(java_strtotime("26th Nov", 1134340285), php_strtotime("26th Nov", 1134340285));
            Assert.assertEquals(java_strtotime("Dec. 4th, 2005"), php_strtotime("Dec. 4th, 2005"));
            Assert.assertEquals(java_strtotime("December 4th, 2005"), php_strtotime("December 4th, 2005"));

            TimeZone.setDefault(defaultZone);
        }

        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug17988.phpt
            // postgresql timestamps
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728"), php_strtotime("2002-06-25 14:18:48.543728"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728 GMT"), php_strtotime("2002-06-25 14:18:48.543728 GMT"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728 EDT"), php_strtotime("2002-06-25 14:18:48.543728 EDT"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728 America/New_York"), php_strtotime("2002-06-25 14:18:48.543728 America/New_York"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728-00"), php_strtotime("2002-06-25 14:18:48.543728-00"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728+00"), php_strtotime("2002-06-25 14:18:48.543728+00"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728-04"), php_strtotime("2002-06-25 14:18:48.543728-04"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728+04"), php_strtotime("2002-06-25 14:18:48.543728+04"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728-0300"), php_strtotime("2002-06-25 14:18:48.543728-0300"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728+0300"), php_strtotime("2002-06-25 14:18:48.543728+0300"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728-0330"), php_strtotime("2002-06-25 14:18:48.543728-0330"));
            Assert.assertEquals(java_strtotime("2002-06-25 14:18:48.543728+0330"), php_strtotime("2002-06-25 14:18:48.543728+0330"));
        }

        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug33578.phpt
            // reDateNoYear
            Assert.assertEquals(java_strtotime("Oct 11"), php_strtotime("Oct 11"));
            Assert.assertEquals(java_strtotime("11 Oct"), php_strtotime("11 Oct"));
            Assert.assertEquals(java_strtotime("11 Oct 2005"), php_strtotime("11 Oct 2005"));
            Assert.assertEquals(java_strtotime("Oct11"), php_strtotime("Oct11"));
            Assert.assertEquals(java_strtotime("11Oct"), php_strtotime("11Oct"));
            Assert.assertEquals(java_strtotime("11Oct 2005"), php_strtotime("11Oct 2005"));
            Assert.assertEquals(java_strtotime("11Oct2005"), php_strtotime("11Oct2005"));
        }

        {
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Sun 2017-01-01")), php_strtotime("saturday this week", php_strtotime("Sun 2017-01-01")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Mon 2017-01-02")), php_strtotime("saturday this week", php_strtotime("Mon 2017-01-02")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Tue 2017-01-03")), php_strtotime("saturday this week", php_strtotime("Tue 2017-01-03")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Wed 2017-01-04")), php_strtotime("saturday this week", php_strtotime("Wed 2017-01-04")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Thu 2017-01-05")), php_strtotime("saturday this week", php_strtotime("Thu 2017-01-05")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Fri 2017-01-06")), php_strtotime("saturday this week", php_strtotime("Fri 2017-01-06")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Sat 2017-01-07")), php_strtotime("saturday this week", php_strtotime("Sat 2017-01-07")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Sun 2017-01-08")), php_strtotime("saturday this week", php_strtotime("Sun 2017-01-08")));
            Assert.assertEquals(java_strtotime("saturday this week", java_strtotime("Mon 2017-01-09")), php_strtotime("saturday this week", php_strtotime("Mon 2017-01-09")));
        }


        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug33869.phpt
            long tm = php_strtotime("2005-01-01 01:01:01");
            Assert.assertEquals(java_strtotime("+5days", tm), php_strtotime("+5days", tm));
            Assert.assertEquals(java_strtotime("+1month", tm), php_strtotime("+1month", tm));
            Assert.assertEquals(java_strtotime("+1year", tm), php_strtotime("+1year", tm));
            Assert.assertEquals(java_strtotime("+5 days", tm), php_strtotime("+5 days", tm));
            Assert.assertEquals(java_strtotime("+1 month", tm), php_strtotime("+1 month", tm));
            Assert.assertEquals(java_strtotime("+1 year", tm), php_strtotime("+1 year", tm));
        }

        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/bug40861.phpt
            Assert.assertEquals(java_strtotime("++60 minutes", java_strtotime("2000-01-01 12:00:00")), php_strtotime("++60 minutes", php_strtotime("2000-01-01 12:00:00")));
            Assert.assertEquals(java_strtotime("+-60 minutes", java_strtotime("2000-01-01 12:00:00")), php_strtotime("+-60 minutes", php_strtotime("2000-01-01 12:00:00")));
            Assert.assertEquals(java_strtotime("--60 minutes", java_strtotime("2000-01-01 12:00:00")), php_strtotime("--60 minutes", php_strtotime("2000-01-01 12:00:00")));
            Assert.assertEquals(java_strtotime("+60 minutes", java_strtotime("2000-01-01 12:00:00")), php_strtotime("+60 minutes", php_strtotime("2000-01-01 12:00:00")));
        }

        {
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));

            // <day> is equivalent to 1 <day> and will *not* forward if the current day
            // (November 1st) is the same day of week.
            Assert.assertEquals(java_strtotime( "Thursday Nov 2007" ), php_strtotime( "Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "1 Thursday Nov 2007" ), php_strtotime( "1 Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "2 Thursday Nov 2007" ), php_strtotime( "2 Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "3 Thursday Nov 2007" ), php_strtotime( "3 Thursday Nov 2007" ));

            // forward one week, then behaves like above for week days
            Assert.assertEquals(java_strtotime( "Thursday Nov 2007" ), php_strtotime( "Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "+1 week Thursday Nov 2007" ), php_strtotime( "+1 week Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "+2 week Thursday Nov 2007" ), php_strtotime( "+2 week Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "+3 week Thursday Nov 2007" ), php_strtotime( "+3 week Thursday Nov 2007" ));

            // First, second, etc skip to the first/second weekday *after* the current day.
            // This makes "first thursday" equivalent to "+1 week thursday" - but only
            // if the current day-of-week is the one mentioned in the phrase.
            Assert.assertEquals(java_strtotime( "Thursday Nov 2007" ), php_strtotime( "Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "first Thursday Nov 2007" ), php_strtotime( "first Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "second Thursday Nov 2007" ), php_strtotime( "second Thursday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "third Thursday Nov 2007" ), php_strtotime( "third Thursday Nov 2007" ));

            // Now the same where the current day-of-week does not match the one in the
            // phrase.
            Assert.assertEquals(java_strtotime( "Friday Nov 2007" ), php_strtotime( "Friday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "first Friday Nov 2007" ), php_strtotime( "first Friday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "second Friday Nov 2007" ), php_strtotime( "second Friday Nov 2007" ));
            Assert.assertEquals(java_strtotime( "third Friday Nov 2007" ), php_strtotime( "third Friday Nov 2007" ));

            TimeZone.setDefault(defaultZone);
        }

        {
            // https://github.com/php/php-src/blob/master/ext/date/tests/strtotime_basic.phpt
            // a test to show the difference in behaviour between 'first' and '1', "second" and "2"...
            TimeZone defaultZone = TimeZone.getDefault();
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            /*
             * The first of December 2008 is a Monday.
             * The term "Monday December 2008" will be parsed as the first Monday in December 2008.
             */

            /*
             * This is parsed as the "first following Monday OR the current day if it is a Monday"
             */
            Assert.assertEquals(java_strtotime("1 Monday December 2008"), php_strtotime("1 Monday December 2008"));
            /*
             * This is parsed as the "second following Monday OR the first following
             * Monday if the current day is a Monday"
             */
            Assert.assertEquals(java_strtotime("2 Monday December 2008"), php_strtotime("2 Monday December 2008"));
            /*
             * This is parsed as the "third following Monday OR the second following
             * Monday if the current day is a Monday"
             */
            Assert.assertEquals(java_strtotime("3 Monday December 2008"), php_strtotime("3 Monday December 2008"));
            /*
             * This is parsed as the "first following Monday after the first Monday in December"
             */
            Assert.assertEquals(java_strtotime("first Monday December 2008"), php_strtotime("first Monday December 2008"));
            /*
             * This is parsed as the "second following Monday after the first Monday in December"
             */
            Assert.assertEquals(java_strtotime("second Monday December 2008"), php_strtotime("second Monday December 2008"));
            /*
             * This is parsed as the "third following Monday after the first Monday in December"
             */
            Assert.assertEquals(java_strtotime("third Monday December 2008"), php_strtotime("third Monday December 2008"));

            TimeZone.setDefault(defaultZone);
        }
    }

    @Test
    public void test_timezone_id_list() {
        for (String timezone : timezones) {
            Assert.assertEquals(java_strtotime("2008-01-01 13:00:00 " + timezone), php_strtotime("2008-01-01 13:00:00 " + timezone));
        }
    }


    @Test
    public void test_mktime() {
        for (String timezone : timezones) {
            Assert.assertEquals(
                    java_mktime(timezone,17, 17, 17, 1, 8327, 1970),
                    php_mktime(timezone, 17, 17, 17, 1, 8327, 1970)
            );
        }
    }


    private static void date_default_timezone_set(String id) {
        TimeZone.setDefault(TimeZone.getTimeZone(id));
    }

    private static long java_mktime(int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
        return java_mktime(ZoneId.systemDefault().getId(), hour, min, sec, month, day, year);
    }
    private static long java_mktime(String zoneId, int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
        String line = zoneId  + "Π" + hour + "Π" + min + "Π" + sec + "Π" + month + "Π" + day + "Π" + year;
        System.out.print("[java] " + line + " => ");
        long r = Time.make(hour, min, sec, month, day, year).epochSecond();
        System.out.println(r);
        return r;
    }

    private static long php_mktime(int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
        return php_mktime(ZoneId.systemDefault().getId(), hour, min, sec, month, day, year);
    }
    private static long php_mktime(String zoneId, int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
        String line = zoneId  + "Π" + hour + "Π" + min + "Π" + sec + "Π" + month + "Π" + day + "Π" + year;
        System.out.print("[ php] " + line + " => ");
        long r = php_mktime_process.mktime(hour, min, sec, month, day, year);
        System.out.println(r);
        return r;
    }



    private static long java_strtotime(String str) {
        return java_strtotime(str, Instant.now().getEpochSecond());
    }
    private static long java_strtotime(String str, long ts) {
        return java_strtotime(str, ZoneId.systemDefault().getId(), ts);
    }
    private static long java_strtotime(String str, String zoneId) {
        return java_strtotime(str, zoneId, Instant.now().getEpochSecond());
    }
    private static long java_strtotime(String str, String zoneId, long ts) {
        System.out.print("[java][" + zoneId + "] " + str + " => ");
        Time saturday_this_week = parse(str, ZoneId.of(zoneId), ts);
        long r = saturday_this_week.epochSecond();
        System.out.println(r);
        return r;
    }
    private static long php_strtotime(String str) {
        return php_strtotime(str, Instant.now().getEpochSecond());
    }
    private static long php_strtotime(String str, String zoneId) {
        return php_strtotime(str, zoneId, Instant.now().getEpochSecond());
    }
    private static long php_strtotime(String str, long ts) {
        String zoneId = ZoneId.systemDefault().getId();
        return php_strtotime(str, zoneId, ts);
    }
    private static long php_strtotime(String str, String zoneId, long ts) {
        System.out.print("[ php][" + zoneId + "] " + str + " => ");
        long r = php_strtotime_process.strtotime(str, zoneId, ts);
        System.out.println(r);
        return r;
    }

    @AfterClass
    public static void destruct() {
        php_strtotime_process.kill();
        php_mktime_process.kill();
    }

    static class php_strtotime_process {
        final static OutputStream os;
        final static BufferedReader br;
        final static Process process;

        static {
            try {
                process = Runtime.getRuntime().exec(new String[]{
                        "php", "-r", "while (($line = fgets(STDIN)) !== false) { " +
                            "list($tz, $s, $ts) = explode(\"Π\", $line); " +
                            "date_default_timezone_set($tz); " +
                            "echo strtotime($s, intval($ts)), \"\\n\"; " +
                        "};"
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            os = process.getOutputStream();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
        }

        static void kill() {
            process.destroyForcibly();
        }

        static long strtotime(String str, String zoneId, long ts) {
            try {
                String line = zoneId + "Π" + str + "Π" + ts + "\n";
                os.write(line.getBytes());
                os.flush();
                line = br.readLine();
                return Parser.longval(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class php_mktime_process {
        final static OutputStream os;
        final static BufferedReader br;
        final static Process process;

        static {
            try {
                process = Runtime.getRuntime().exec(new String[]{
                        "php", "-r", "while (($line = fgets(STDIN)) !== false) { " +
                            "list($tz, $h, $i, $s, $m, $d, $y) = explode(\"Π\", $line); " +
                            "date_default_timezone_set($tz); " +
                            "echo @mktime($h,$i,$s,$m,$d,$y), \"\\n\"; " +
                        "};"
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            os = process.getOutputStream();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
        }

        static void kill() {
            process.destroyForcibly();
        }

        static long mktime(int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
            return mktime(ZoneId.systemDefault().getId(), hour, min, sec, month, day, year);
        }

        static long mktime(String zoneId, int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
            try {
                String line = zoneId  + "Π" + hour + "Π" + min + "Π" + sec + "Π" + month + "Π" + day + "Π" + year + "\n";
                os.write(line.getBytes());
                os.flush();
                line = br.readLine();
                return Parser.longval(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static void main(String[] args) {
            System.out.println(mktime(17, 17, 17, 1, 8327, 1970));;
        }
    }


    // https://github.com/php/php-src/blob/master/ext/date/tests/bug46111.phpt
    final static String[] timezones = {
            // 这一行 时区 java8 的时区库目前不支持
            "America/Nuuk","America/Punta_Arenas","Asia/Atyrau","Asia/Famagusta","Asia/Qostanay","Asia/Yangon","Europe/Saratov",

            "Africa/Abidjan", "Africa/Accra", "Africa/Addis_Ababa", "Africa/Algiers", "Africa/Asmara", "Africa/Bamako",
            "Africa/Bangui", "Africa/Banjul", "Africa/Bissau", "Africa/Blantyre", "Africa/Brazzaville", "Africa/Bujumbura",
            "Africa/Cairo", "Africa/Casablanca", "Africa/Ceuta", "Africa/Conakry", "Africa/Dakar", "Africa/Dar_es_Salaam",
            "Africa/Djibouti", "Africa/Douala", "Africa/El_Aaiun", "Africa/Freetown", "Africa/Gaborone", "Africa/Harare",
            "Africa/Johannesburg", "Africa/Juba", "Africa/Kampala", "Africa/Khartoum", "Africa/Kigali", "Africa/Kinshasa",
            "Africa/Lagos", "Africa/Libreville", "Africa/Lome", "Africa/Luanda", "Africa/Lubumbashi", "Africa/Lusaka",
            "Africa/Malabo", "Africa/Maputo", "Africa/Maseru", "Africa/Mbabane", "Africa/Mogadishu", "Africa/Monrovia",
            "Africa/Nairobi", "Africa/Ndjamena", "Africa/Niamey", "Africa/Nouakchott", "Africa/Ouagadougou",
            "Africa/Porto-Novo", "Africa/Sao_Tome", "Africa/Tripoli", "Africa/Tunis", "Africa/Windhoek", "America/Adak",
            "America/Anchorage", "America/Anguilla", "America/Antigua", "America/Araguaina", "America/Argentina/Buenos_Aires",
            "America/Argentina/Catamarca", "America/Argentina/Cordoba", "America/Argentina/Jujuy", "America/Argentina/La_Rioja",
            "America/Argentina/Mendoza", "America/Argentina/Rio_Gallegos", "America/Argentina/Salta", "America/Argentina/San_Juan",
            "America/Argentina/San_Luis", "America/Argentina/Tucuman", "America/Argentina/Ushuaia", "America/Aruba", "America/Asuncion",
            "America/Atikokan", "America/Bahia", "America/Bahia_Banderas", "America/Barbados", "America/Belem", "America/Belize",
            "America/Blanc-Sablon", "America/Boa_Vista", "America/Bogota", "America/Boise", "America/Cambridge_Bay",
            "America/Campo_Grande", "America/Cancun", "America/Caracas", "America/Cayenne", "America/Cayman", "America/Chicago",
            "America/Chihuahua", "America/Costa_Rica", "America/Creston", "America/Cuiaba", "America/Curacao", "America/Danmarkshavn",
            "America/Dawson", "America/Dawson_Creek", "America/Denver", "America/Detroit", "America/Dominica", "America/Edmonton",
            "America/Eirunepe", "America/El_Salvador", "America/Fort_Nelson", "America/Fortaleza", "America/Glace_Bay",
            "America/Goose_Bay", "America/Grand_Turk", "America/Grenada", "America/Guadeloupe", "America/Guatemala",
            "America/Guayaquil", "America/Guyana", "America/Halifax", "America/Havana", "America/Hermosillo",
            "America/Indiana/Indianapolis", "America/Indiana/Knox", "America/Indiana/Marengo", "America/Indiana/Petersburg",
            "America/Indiana/Tell_City", "America/Indiana/Vevay", "America/Indiana/Vincennes", "America/Indiana/Winamac",
            "America/Inuvik", "America/Iqaluit", "America/Jamaica", "America/Juneau", "America/Kentucky/Louisville",
            "America/Kentucky/Monticello", "America/Kralendijk", "America/La_Paz", "America/Lima", "America/Los_Angeles",
            "America/Lower_Princes", "America/Maceio", "America/Managua", "America/Manaus", "America/Marigot", "America/Martinique",
            "America/Matamoros", "America/Mazatlan", "America/Menominee", "America/Merida", "America/Metlakatla", "America/Mexico_City",
            "America/Miquelon", "America/Moncton", "America/Monterrey", "America/Montevideo", "America/Montserrat", "America/Nassau",
            "America/New_York", "America/Nipigon", "America/Nome", "America/Noronha", "America/North_Dakota/Beulah",
            "America/North_Dakota/Center", "America/North_Dakota/New_Salem", "America/Ojinaga", "America/Panama", "America/Pangnirtung",
            "America/Paramaribo", "America/Phoenix", "America/Port-au-Prince", "America/Port_of_Spain", "America/Porto_Velho",
            "America/Puerto_Rico", "America/Rainy_River", "America/Rankin_Inlet", "America/Recife", "America/Regina",
            "America/Resolute", "America/Rio_Branco", "America/Santarem", "America/Santiago", "America/Santo_Domingo",
            "America/Sao_Paulo", "America/Scoresbysund", "America/Sitka", "America/St_Barthelemy", "America/St_Johns",
            "America/St_Kitts", "America/St_Lucia", "America/St_Thomas", "America/St_Vincent", "America/Swift_Current",
            "America/Tegucigalpa", "America/Thule", "America/Thunder_Bay", "America/Tijuana", "America/Toronto",
            "America/Tortola", "America/Vancouver", "America/Whitehorse", "America/Winnipeg", "America/Yakutat",
            "America/Yellowknife", "Antarctica/Casey", "Antarctica/Davis", "Antarctica/DumontDUrville", "Antarctica/Macquarie",
            "Antarctica/Mawson", "Antarctica/McMurdo", "Antarctica/Palmer", "Antarctica/Rothera", "Antarctica/Syowa",
            "Antarctica/Troll", "Antarctica/Vostok", "Arctic/Longyearbyen", "Asia/Aden", "Asia/Almaty", "Asia/Amman",
            "Asia/Anadyr", "Asia/Aqtau", "Asia/Aqtobe", "Asia/Ashgabat", "Asia/Baghdad", "Asia/Bahrain", "Asia/Baku",
            "Asia/Bangkok", "Asia/Barnaul", "Asia/Beirut", "Asia/Bishkek", "Asia/Brunei", "Asia/Chita", "Asia/Choibalsan",
            "Asia/Colombo", "Asia/Damascus", "Asia/Dhaka", "Asia/Dili", "Asia/Dubai", "Asia/Dushanbe", "Asia/Gaza",
            "Asia/Hebron", "Asia/Ho_Chi_Minh", "Asia/Hong_Kong", "Asia/Hovd", "Asia/Irkutsk", "Asia/Jakarta", "Asia/Jayapura",
            "Asia/Jerusalem", "Asia/Kabul", "Asia/Kamchatka", "Asia/Karachi", "Asia/Kathmandu", "Asia/Khandyga", "Asia/Kolkata",
            "Asia/Krasnoyarsk", "Asia/Kuala_Lumpur", "Asia/Kuching", "Asia/Kuwait", "Asia/Macau", "Asia/Magadan", "Asia/Makassar",
            "Asia/Manila", "Asia/Muscat", "Asia/Nicosia", "Asia/Novokuznetsk", "Asia/Novosibirsk", "Asia/Omsk", "Asia/Oral",
            "Asia/Phnom_Penh", "Asia/Pontianak", "Asia/Pyongyang", "Asia/Qatar", "Asia/Qyzylorda", "Asia/Riyadh", "Asia/Sakhalin",
            "Asia/Samarkand", "Asia/Seoul", "Asia/Shanghai", "Asia/Singapore", "Asia/Srednekolymsk", "Asia/Taipei", "Asia/Tashkent",
            "Asia/Tbilisi", "Asia/Tehran", "Asia/Thimphu", "Asia/Tokyo", "Asia/Tomsk", "Asia/Ulaanbaatar", "Asia/Urumqi", "Asia/Ust-Nera",
            "Asia/Vientiane", "Asia/Vladivostok", "Asia/Yakutsk", "Asia/Yekaterinburg", "Asia/Yerevan", "Atlantic/Azores",
            "Atlantic/Bermuda", "Atlantic/Canary", "Atlantic/Cape_Verde", "Atlantic/Faroe", "Atlantic/Madeira", "Atlantic/Reykjavik",
            "Atlantic/South_Georgia", "Atlantic/St_Helena", "Atlantic/Stanley", "Australia/Adelaide", "Australia/Brisbane",
            "Australia/Broken_Hill", "Australia/Currie", "Australia/Darwin", "Australia/Eucla", "Australia/Hobart", "Australia/Lindeman",
            "Australia/Lord_Howe", "Australia/Melbourne", "Australia/Perth", "Australia/Sydney", "Europe/Amsterdam", "Europe/Andorra",
            "Europe/Astrakhan", "Europe/Athens", "Europe/Belgrade", "Europe/Berlin", "Europe/Bratislava", "Europe/Brussels",
            "Europe/Bucharest", "Europe/Budapest", "Europe/Busingen", "Europe/Chisinau", "Europe/Copenhagen", "Europe/Dublin",
            "Europe/Gibraltar", "Europe/Guernsey", "Europe/Helsinki", "Europe/Isle_of_Man", "Europe/Istanbul", "Europe/Jersey",
            "Europe/Kaliningrad", "Europe/Kiev", "Europe/Kirov", "Europe/Lisbon", "Europe/Ljubljana", "Europe/London",
            "Europe/Luxembourg", "Europe/Madrid", "Europe/Malta", "Europe/Mariehamn", "Europe/Minsk", "Europe/Monaco",
            "Europe/Moscow", "Europe/Oslo", "Europe/Paris", "Europe/Podgorica", "Europe/Prague", "Europe/Riga", "Europe/Rome",
            "Europe/Samara", "Europe/San_Marino", "Europe/Sarajevo", "Europe/Simferopol", "Europe/Skopje", "Europe/Sofia",
            "Europe/Stockholm", "Europe/Tallinn", "Europe/Tirane", "Europe/Ulyanovsk", "Europe/Uzhgorod", "Europe/Vaduz",
            "Europe/Vatican", "Europe/Vienna", "Europe/Vilnius", "Europe/Volgograd", "Europe/Warsaw", "Europe/Zagreb",
            "Europe/Zaporozhye", "Europe/Zurich", "Indian/Antananarivo", "Indian/Chagos", "Indian/Christmas", "Indian/Cocos",
            "Indian/Comoro", "Indian/Kerguelen", "Indian/Mahe", "Indian/Maldives", "Indian/Mauritius", "Indian/Mayotte",
            "Indian/Reunion", "Pacific/Apia", "Pacific/Auckland", "Pacific/Bougainville", "Pacific/Chatham", "Pacific/Chuuk",
            "Pacific/Easter", "Pacific/Efate", "Pacific/Enderbury", "Pacific/Fakaofo", "Pacific/Fiji", "Pacific/Funafuti",
            "Pacific/Galapagos", "Pacific/Gambier", "Pacific/Guadalcanal", "Pacific/Guam", "Pacific/Honolulu", "Pacific/Kiritimati",
            "Pacific/Kosrae", "Pacific/Kwajalein", "Pacific/Majuro", "Pacific/Marquesas", "Pacific/Midway", "Pacific/Nauru",
            "Pacific/Niue", "Pacific/Norfolk", "Pacific/Noumea", "Pacific/Pago_Pago", "Pacific/Palau", "Pacific/Pitcairn",
            "Pacific/Pohnpei", "Pacific/Port_Moresby", "Pacific/Rarotonga", "Pacific/Saipan", "Pacific/Tahiti", "Pacific/Tarawa",
            "Pacific/Tongatapu", "Pacific/Wake", "Pacific/Wallis", "UTC"
    };
}
