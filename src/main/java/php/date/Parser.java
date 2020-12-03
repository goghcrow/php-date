package php.date;

import php.date.Time.RelativeUnit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static php.date.Time.*;
import static php.date.Time.TimeUnit.*;


public class Parser {

    final static int NONE             = 0x00;
    final static int OVERRIDE_TIME    = 0x01;
    final static int NO_CLONE         = 0x02;

    final static int SPECIAL_WEEKDAY = 0x01;
    final static int SPECIAL_DAY_OF_WEEK_IN_MONTH = 0x02;
    final static int SPECIAL_LAST_DAY_OF_WEEK_IN_MONTH = 0x03;

    final static int SPECIAL_FIRST_DAY_OF_MONTH = 0x01;
    final static int SPECIAL_LAST_DAY_OF_MONTH = 0x02;

    final static long SECS_PER_ERA   = 12622780800L;
    final static int  SECS_PER_DAY   = 86400;
    final static int  DAYS_PER_YEAR  = 365;
    final static int  DAYS_PER_LYEAR = 366;

    /* 400*365 days + 97 leap days */
    final static int DAYS_PER_LYEAR_PERIOD = 146097;
    final static int YEARS_PER_LYEAR_PERIOD = 400;

    // lz  leading zero

    final static String reAny = ".*"; // ""[\\u000-\\u377]";
    final static String reSpace = "[ \\t]+";
    final static String reSpaceOpt = "[ \\t]*";

    final static String reAgo = "^ago";

    final static String reHour24    = "(2[0-4]|[01]?[0-9])";
    final static String reHour24Lz  = "([01][0-9]|2[0-4])";
    final static String reHour12    = "(1[0-2]|0?[1-9])";
    final static String reMinute    = "([0-5]?[0-9])";
    final static String reMinuteLz  = "([0-5][0-9])";
    final static String reSecond    = "([0-5]?[0-9]|60)";
    final static String reSecondLz  = "([0-5][0-9]|60)";
    final static String reFrac      = "(?:\\.([0-9]+))";

    final static String reMeridian      = "(?:([ap])\\.?m\\.?(?:[ \\t]|$))";

    final static String reYear          = "([0-9]{1,4})";
    final static String reYear2         = "([0-9]{2})";
    final static String reYear4         = "([0-9]{4})";
    final static String reYear4WithSign = "([+-]?[0-9]{4})";
    // final static String reYearx         = "([+-][0-9]{5,19})";

    final static String reMonth         = "(1[0-2]|0?[0-9])";
    final static String reMonthLz       = "(0[0-9]|1[0-2])";

    final static String reMonthFull     = "january|february|march|april|may|june|july|august|september|october|november|december";
    final static String reMonthAbbr     = "jan|feb|mar|apr|may|jun|jul|aug|sept?|oct|nov|dec";
    final static String reMonthRoman    = "i{1,3}|i[vx]|vi{0,3}|xi{0,2}";
    final static String reMonthText     = '(' + reMonthFull + '|' + reMonthAbbr + '|' + reMonthRoman + ')';

    // final static String reDaysuf = "st|nd|rd|th";
    final static String reDay   = "(?:([0-2]?[0-9]|3[01])(?:st|nd|rd|th)?)";
    final static String reDayLz = "(0[0-9]|[1-2][0-9]|3[01])";

    final static String reDayFull = "sunday|monday|tuesday|wednesday|thursday|friday|saturday";
    final static String reDayAbbr = "sun|mon|tue|wed|thu|fri|sat";
    final static String reDayText = reDayFull + '|' + reDayAbbr + '|' + "weekdays?";

    final static String reDayOfYear = "(00[1-9]|0[1-9][0-9]|[12][0-9][0-9]|3[0-5][0-9]|36[0-6])";
    final static String reWeekOfYear = "(0[1-9]|[1-4][0-9]|5[0-3])";

    // matcher.groupCount() == 4   matcher.group(1) 是 tzCorr 整体
    final static String reTzCorrection  = "((?:GMT)?([+-])" + reHour24 + ":?" + reMinute + "?)";
    final static String reTzAbbr        = "\\(?([a-zA-Z]{1,6})\\)?";
    final static String reTz            = "[A-Z][a-z]+([_/-][A-Za-z_]+)+|" + reTzAbbr;
    final static Pattern reTzCorrectionPtn = Pattern.compile(reTzCorrection);


    /* Time formats */
    final static String reTimeTiny12  = '^' + reHour12                                           + reSpaceOpt + reMeridian;
    final static String reTimeShort12 = '^' + reHour12 + "[:.]" + reMinuteLz + reSpaceOpt + reMeridian;
    final static String reTimeLong12  = '^' + reHour12 + "[:.]" + reMinute + "[:.]" + reSecondLz + reSpaceOpt + reMeridian;

    final static String reTimeShort24 = "^t?" + reHour24 + "[:.]" + reMinute;
    final static String reTimeLong24  = "^t?" + reHour24 + "[:.]" + reMinute + "[:.]" + reSecond;
    final static String reISO8601Long = "^t?" + reHour24 + "[:.]" + reMinute + "[:.]" + reSecond + reFrac;
    // 还是遵守 ISO8601 标准 不随便兼容了
    // final static String reISO8601Long = "^t?" + reHour24 + "[:.]" + reMinute + "[:.]" + reSecond + "(?:[:.]([0-9]+))";

    final static String reTzText = '(' + reTzCorrection + '|' + reTz + ')';

    //final static String reISO8601ShortTz= reHour24 + "[:.]" + reMinutelz + reSpaceOpt + reTzText;
    final static String reISO8601NormTz = "^t?" + reHour24 + "[:.]" + reMinute + "[:.]" + reSecondLz + reSpaceOpt + reTzText;
    //final static String reISO8601LongTz = reHour24 + "[:.]" + reMinute + "[:.]" + reSecondlz + reFrac + reSpaceOpt + reTzText;

    /* gnu */
    final static String reGNUNoColon = "^t?" + reHour24Lz + reMinuteLz;
    // final static String reGNUNoColonTz = reHour24 + reMinutelz + reSpaceOpt + reTzText;
    final static String reISO8601NoColon = "^t?" + reHour24Lz + reMinuteLz + reSecondLz;
    // final static String reISO8601NoColonTz = reHour24lz + reMinutelz + reSecondlz + reSpaceOpt + reTzText;

    /* Date formats */
    final static String reAmericanShort     = '^' + reMonth + '/' + reDay;
    final static String reAmerican          = '^' + reMonth + '/' + reDay + '/' + reYear;
    final static String reISO8601DateSlash  = '^' + reYear4 + '/' + reMonthLz + '/' + reDayLz + "/?";
    final static String reDateSlash         = '^' + reYear4 + '/' + reMonth + '/' + reDay;
    final static String reISO8601Date4      = '^' + reYear4WithSign + '-' + reMonthLz + '-' + reDayLz;
    final static String reISO8601Date2      = '^' + reYear2 + '-' + reMonthLz + '-' + reDayLz;
    // final static String reISO8601Datex      = '^' + reYearx + '-' + reMonthlz + '-' + reDaylz;
    final static String reGNUDateShorter    = '^' + reYear4 + '-' + reMonth;
    final static String reGNUDateShort      = '^' + reYear + '-' + reMonth + '-' + reDay;
    final static String rePointedDate4      = '^' + reDay + "[.\\t-]" + reMonth + "[.-]" + reYear4;
    final static String rePointedDate2      = '^' + reDay + "[.\\t]" +  reMonth + "\\." + reYear2;
    final static String reDateFull          = '^' + reDay + "[ \\t.-]*" + reMonthText + "[ \\t.-]*" + reYear;
    final static String reDateNoDay         = '^' + reMonthText + "[ .\\t-]*" + reYear4;
    final static String reDateNoDayRev      = '^' + reYear4 + "[ .\\t-]*" + reMonthText;
    final static String reDateTextual       = '^' + reMonthText + "[ .\\t-]*" + reDay + "[,.stndrh\\t ]+" + reYear;
    final static String reDateNoYear        = '^' + reMonthText + "[ .\\t-]*" + reDay + "[,.stndrh\\t ]*";
    final static String reDateNoYearRev     = '^' + reDay + "[ .\\t-]*" + reMonthText;
    final static String reDateNoColon       = '^' + reYear4 + reMonthLz + reDayLz;

    /* Special formats */
    // 参见 https://www.php.net/manual/en/datetime.formats.compound.php
    // 木有遵守这个：The "T" in the SOAP, XMRPC and WDDX formats is case-sensitive, you can only use the upper case "T".
    final static String reSoap              = '^' + reYear4 + '-' + reMonthLz + '-' + reDayLz + 'T'    + reHour24Lz + ':' + reMinuteLz + ':' + reSecondLz + reFrac + reTzCorrection + '?';
    final static String reXML_RPC           = '^' + reYear4       + reMonthLz + reDayLz + 'T'    + reHour24    + ':' + reMinuteLz + ':' + reSecondLz;
    final static String reXML_RPCNoColon    = '^' + reYear4       + reMonthLz + reDayLz + "[Tt]" + reHour24          + reMinuteLz + reSecondLz;
    final static String reWDDX              = '^' + reYear4 + '-' + reMonth   + '-' + reDay   + 'T'    + reHour24    + ':' + reMinute   + ':' + reSecond;
    final static String reEXIF              = '^' + reYear4 + ':' + reMonthLz + ':' + reDayLz + ' '    + reHour24Lz + ':' + reMinuteLz + ':' + reSecondLz;

    final static String rePgYearDotDay      = '^' + reYear4 + "\\.?" + reDayOfYear;
    final static String rePgTextShort       = "^(" + reMonthAbbr + ")-" + reDayLz + '-' + reYear;
    final static String rePgTextReverse     = '^' + "(\\d{3,4}|[4-9]\\d|3[2-9])"/*reYear*/ + "-(" + reMonthAbbr + ")-" + reDayLz;
    final static String reMssqlTime         = '^' + reHour12 + ":" + reMinuteLz + ":" + reSecondLz + "[:.]([0-9]+)" + reMeridian;
    final static String reISOWeekday        = '^' + reYear4 + "-?W" + reWeekOfYear + "-?([0-7])";
    final static String reISOWeek           = '^' + reYear4 + "-?W" + reWeekOfYear;

    // final static String reFirstDayOf       = "^first day of";
    // final static String reLastDayOf        = "^last day of";
    final static String reFirstOrLastDay    = "^(first|last) day of";
    // final static String reBackOf           = "^back of " + reHour24 + reSpaceOpt + reMeridian + '?';
    // final static String reFrontOf          = "^front of " + reHour24 + reSpaceOpt + reMeridian + '?';
    final static String reBackOrFrontOf    = "^(back|front) of " + reHour24 + reSpaceOpt + reMeridian + '?';
    final static String reYesterday        = "^yesterday";
    final static String reNow              = "^now";
    final static String reNoon             = "^noon";
    final static String reMidnightOrToday  = "^(midnight|today)";
    final static String reTomorrow         = "^tomorrow";

    /* Common Log Format: 10/Oct/2000:13:55:36 -0700 */
    final static String reCLF               = '^' + reDay + "/(" + reMonthAbbr + ")/" + reYear4 + ':' + reHour24Lz + ':' + reMinuteLz + ':' + reSecondLz + reSpace + reTzCorrection;

    /* Timestamp format: @1126396800 */
    final static String reTimestamp        = "^@(-?\\d+)";
    final static String reTimestampMs      = "^@(-?\\d+)\\.(\\d{0,6})"; // timestamp microsec

    /* To fix some ambiguities */
    final static String reDateShortWithTimeShort12  = '^' + reDateNoYear + reTimeShort12.substring(1);
    final static String reDateShortWithTimeLong12   = '^' + reDateNoYear + reTimeLong12.substring(1);
    final static String reDateShortWithTimeShort    = '^' + reDateNoYear + reTimeShort24.substring(1);
    final static String reDateShortWithTimeLong     = '^' + reDateNoYear + reTimeLong24.substring(1);
    final static String reDateShortWithTimeLongTz   = '^' + reDateNoYear + reISO8601NormTz.substring(1);

    /* Relative regexps */
    final static String reRelTextNumber = "first|second|third|fourth|fifth|sixth|seventh|eighth?|ninth|tenth|eleventh|twelfth";
    final static String reRelTextText   = "next|last|previous|this";
    final static String reRelTextUnit   = "(?:msec|millisecond|µsec|microsecond|usec|second|sec|minute|min|hour|day|fortnight|forthnight|month|year)s?|(?:ms)|(?:µs)|weeks|" + reDayText;

    final static String reRelNumber         = "([+-]*)[ \\t]*([0-9]{1,13})";
    final static String reRelative          = '^' + reRelNumber + reSpaceOpt + '(' + reRelTextUnit + "|week)";
    final static String reRelativeText      = "^(" + reRelTextNumber + '|' + reRelTextText + ')' + reSpace + '(' + reRelTextUnit + ')';
    final static String reRelativeTextWeek  = "^(" + reRelTextText + ')' + reSpace + "(week)";

    final static String reWeekdayOf = "^(" + reRelTextNumber + '|' + reRelTextText + ')' + reSpace + '(' + reDayFull + '|' + reDayAbbr + ')' + reSpace + "of";

    final static String reWhitespace = "^[ .,\\t\\r\\n]+";

    final static Map<String, Integer> tblMouth;
    static {
        Map<String, Integer> tbl = new HashMap<>();
        tbl.put("jan", 1); tbl.put("january", 1); tbl.put("i", 1);
        tbl.put("feb", 2); tbl.put("february", 2); tbl.put("ii", 2);
        tbl.put("mar", 3); tbl.put("march", 3); tbl.put("iii", 3);
        tbl.put("apr", 4); tbl.put("april", 4); tbl.put("iv", 4);
                           tbl.put("may", 5); tbl.put("v", 5);
        tbl.put("jun", 6); tbl.put("june", 6); tbl.put("vi", 6);
        tbl.put("jul", 7); tbl.put("july", 7); tbl.put("vii", 7);
        tbl.put("aug", 8); tbl.put("august", 8); tbl.put("viii", 8);
        tbl.put("sep", 9); tbl.put("sept", 9); tbl.put("september", 9); tbl.put("ix", 9);
        tbl.put("oct", 10); tbl.put("october", 10); tbl.put("x", 10);
        tbl.put("nov", 11); tbl.put("november", 11); tbl.put("xi", 11);
        tbl.put("dec", 12); tbl.put("december", 12); tbl.put("xii", 12);
        tblMouth = Collections.unmodifiableMap(tbl);
    }

    final static Map<String, Integer> tblWeekday;
    static {
        Map<String, Integer> tbl = new HashMap<>();
        tbl.put("mon", 1); tbl.put("monday", 1);
        tbl.put("tue", 2); tbl.put("tuesday", 2);
        tbl.put("wed", 3); tbl.put("wednesday", 3);
        tbl.put("thu", 4); tbl.put("thursday", 4);
        tbl.put("fri", 5); tbl.put("friday", 5);
        tbl.put("sat", 6); tbl.put("saturday", 6);
        tbl.put("sun", 0); tbl.put("sunday", 0);
        tblWeekday = Collections.unmodifiableMap(tbl);
    }

    final static Map<String, Integer> tblRelative;
    static {
        Map<String, Integer> tbl = new HashMap<>();
        tbl.put("last", -1);
        tbl.put("previous", -1);
        tbl.put("this", 0);
        tbl.put("first", 1);
        tbl.put("next", 1);
        tbl.put("second", 2);
        tbl.put("third", 3);
        tbl.put("fourth", 4);
        tbl.put("fifth", 5);
        tbl.put("sixth", 6);
        tbl.put("seventh", 7);
        tbl.put("eight", 8);
        tbl.put("eighth", 8);
        tbl.put("ninth", 9);
        tbl.put("tenth", 10);
        tbl.put("eleventh", 11);
        tbl.put("twelfth", 1);
        tblRelative = Collections.unmodifiableMap(tbl);
    }

    final static Map<String, RelativeUnit> tblRelativeUnit;
    static {
        RelativeUnit[] relativeUnits = {
                new RelativeUnit( "ms",           MICROSEC, 1000 ),
                new RelativeUnit( "msec",         TimeUnit.MICROSEC, 1000 ),
                new RelativeUnit( "msecs",        TimeUnit.MICROSEC, 1000 ),
                new RelativeUnit( "millisecond",  TimeUnit.MICROSEC, 1000 ),
                new RelativeUnit( "milliseconds", TimeUnit.MICROSEC, 1000 ),
                new RelativeUnit( "µs",           TimeUnit.MICROSEC,    1 ),
                new RelativeUnit( "usec",         TimeUnit.MICROSEC,    1 ),
                new RelativeUnit( "usecs",        TimeUnit.MICROSEC,    1 ),
                new RelativeUnit( "µsec",         TimeUnit.MICROSEC,    1 ),
                new RelativeUnit( "µsecs",        TimeUnit.MICROSEC,    1 ),
                new RelativeUnit( "microsecond",  TimeUnit.MICROSEC,    1 ),
                new RelativeUnit( "microseconds", TimeUnit.MICROSEC,    1 ),
                new RelativeUnit( "sec",         TimeUnit.SECOND,  1 ),
                new RelativeUnit( "secs",        TimeUnit.SECOND,  1 ),
                new RelativeUnit( "second",      TimeUnit.SECOND,  1 ),
                new RelativeUnit( "seconds",     TimeUnit.SECOND,  1 ),
                new RelativeUnit( "min",         TimeUnit.MINUTE,  1 ),
                new RelativeUnit( "mins",        TimeUnit.MINUTE,  1 ),
                new RelativeUnit( "minute",      TimeUnit.MINUTE,  1 ),
                new RelativeUnit( "minutes",     TimeUnit.MINUTE,  1 ),
                new RelativeUnit( "hour",        TimeUnit.HOUR,    1 ),
                new RelativeUnit( "hours",       TimeUnit.HOUR,    1 ),
                new RelativeUnit( "day",         TimeUnit.DAY,     1 ),
                new RelativeUnit( "days",        TimeUnit.DAY,     1 ),
                new RelativeUnit( "week",        TimeUnit.DAY,     7 ),
                new RelativeUnit( "weeks",       TimeUnit.DAY,     7 ),
                new RelativeUnit( "fortnight",   TimeUnit.DAY,    14 ),
                new RelativeUnit( "fortnights",  TimeUnit.DAY,    14 ),
                new RelativeUnit( "forthnight",  TimeUnit.DAY,    14 ),
                new RelativeUnit( "forthnights", TimeUnit.DAY,    14 ),
                new RelativeUnit( "month",       TimeUnit.MONTH,   1 ),
                new RelativeUnit( "months",      TimeUnit.MONTH,   1 ),
                new RelativeUnit( "year",        TimeUnit.YEAR,    1 ),
                new RelativeUnit( "years",       TimeUnit.YEAR,    1 ),

                new RelativeUnit( "monday",      TimeUnit.WEEKDAY, 1 ),
                new RelativeUnit( "mon",         TimeUnit.WEEKDAY, 1 ),
                new RelativeUnit( "tuesday",     TimeUnit.WEEKDAY, 2 ),
                new RelativeUnit( "tue",         TimeUnit.WEEKDAY, 2 ),
                new RelativeUnit( "wednesday",   TimeUnit.WEEKDAY, 3 ),
                new RelativeUnit( "wed",         TimeUnit.WEEKDAY, 3 ),
                new RelativeUnit( "thursday",    TimeUnit.WEEKDAY, 4 ),
                new RelativeUnit( "thu",         TimeUnit.WEEKDAY, 4 ),
                new RelativeUnit( "friday",      TimeUnit.WEEKDAY, 5 ),
                new RelativeUnit( "fri",         TimeUnit.WEEKDAY, 5 ),
                new RelativeUnit( "saturday",    TimeUnit.WEEKDAY, 6 ),
                new RelativeUnit( "sat",         TimeUnit.WEEKDAY, 6 ),
                new RelativeUnit( "sunday",      TimeUnit.WEEKDAY, 0 ),
                new RelativeUnit( "sun",         TimeUnit.WEEKDAY, 0 ),

                new RelativeUnit( "weekday",     TimeUnit.SPECIAL, SPECIAL_WEEKDAY ),
                new RelativeUnit( "weekdays",    TimeUnit.SPECIAL, SPECIAL_WEEKDAY ),
        };
        Map<String, RelativeUnit> tbl = new HashMap<>();
        for (RelativeUnit relativeUnit : relativeUnits) {
            tbl.put(relativeUnit.name, relativeUnit);
        }
        tblRelativeUnit = Collections.unmodifiableMap(tbl);
    }




    static int lookup_month(String str) {
        Integer m = tblMouth.get(str.toLowerCase());
        if (m == null) {
            throw new RuntimeException("不支持的月份描述 " + str);
        } else {
            return m;
        }
    }

    static int lookup_weekday(String str) {
        Integer w = tblWeekday.get(str.toLowerCase());
        if (w == null) {
            throw new RuntimeException("不支持的星期描述 " + str);
        } else {
            return w;
        }
    }

    static int lookup_relative(String relStr) {
        Integer rel = tblRelative.get(relStr.toLowerCase());
        if (rel == null) {
            throw new TimeException("不支持的相对数字描述 " + relStr);
        } else {
            return rel;
        }
    }

    static int lookup_relativeBehavior(String relStr) {
        if (relStr.equals("this")) {
            return 1;
        } else {
            return 0;
        }
    }

    static RelativeUnit lookup_relativeUnit(String relStr) {
        RelativeUnit relativeUnit = tblRelativeUnit.get(relStr.toLowerCase());
        if (relativeUnit == null) {
            throw new TimeException("不支持的单位 " + relStr);
        }
        return relativeUnit;
    }



    interface TimeMatcher {
        void match(Time time, Matcher matcher);
    }

    static class Rule {
        String name;
        Pattern format;
        TimeMatcher matcher;
        Rule(String name, Pattern format, TimeMatcher matcher) {
            this.name = name;
            this.format = format;
            this.matcher = matcher;
        }
    }

    final static Map<String, Rule> rules = new LinkedHashMap<>();

    static void register(String name, String regex, TimeMatcher matcher) {
        rules.put(name, new Rule(name, compile(regex, Pattern.CASE_INSENSITIVE), matcher));
    }

    static void registerCaseSensitive(String name, String regex, TimeMatcher matcher) {
        rules.put(name, new Rule(name, compile(regex), matcher));
    }

    static {
        register("yesterday", reYesterday, (time, matcher) -> {
            time.unhave_time().have_relative();
            time.relative.y = 0;
            time.relative.m = 0;
            time.relative.d = -1;
        });

        register("now", reNow, (time, matcher) -> { });

        register("noon", reNoon, (time, matcher) -> {
            time.unhave_time().have_time();
            time.h = 12;
        });

        register("midnightOrToday", reMidnightOrToday, (time, matcher) -> {
            time.unhave_time();
        });

        register("tomorrow", reTomorrow, (time, matcher) -> {
            time.unhave_time().have_relative();
            time.relative.y = 0;
            time.relative.m = 0;
            time.relative.d = 1;
        });

        register("timestamp", reTimestamp, (time, matcher) -> {
            assert matcher.groupCount() == 1;
            time.have_relative().unhave_date().unhave_time().have_tz();

            time.y = 1970;
            time.m = 1;
            time.d = 1;
            time.relative.s += longval(matcher.group(1));
            time.is_localtime = true;
            time.zone_type = ZoneType.OFFSET;
            time.z = 0;
            time.dst = 0;
        });

        register("timestampms", reTimestampMs, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_relative().unhave_date().unhave_time().have_tz();

            time.y = 1970;
            time.m = 1;
            time.d = 1;
            time.relative.s += longval(matcher.group(1));
            time.relative.us += frac(matcher.group(2));
            time.is_localtime = true;
            time.zone_type = ZoneType.OFFSET;
            time.z = 0;
            time.dst = 0;
        });

        register("first | LastDay", reFirstOrLastDay, (time, matcher) -> {
            assert matcher.groupCount() == 1;
            time.have_relative();

            char c = matcher.group(1).charAt(0);
            if (c == 'l' || c == 'L') {
                time.relative.first_last_day_of = SPECIAL_LAST_DAY_OF_MONTH;
            } else {
                time.relative.first_last_day_of = SPECIAL_FIRST_DAY_OF_MONTH;
            }
        });

        register("backof | frontOf", reBackOrFrontOf, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.unhave_time().have_time();

            char c = matcher.group(1).charAt(0);
            long hour = longval(matcher.group(2));
            if (c == 'b' || c == 'B') {
                time.h = hour;
                time.i = 15;
            } else {
                time.h = hour - 1;
                time.i = 45;
            }
            String meridian = matcher.group(3);
            if (meridian != null) {
                time.h = meridian(time.h, meridian);
            }
        });

        register("weekdayof", reWeekdayOf, (time, matcher) -> {
            time.have_relative().have_special_relative();

            String rel_str = matcher.group(1);
            int amount = lookup_relative(rel_str);
            int relativeBehavior = lookup_relativeBehavior(rel_str);
            RelativeUnit relativeUnit = lookup_relativeUnit(matcher.group(2));

            if (amount > 0) { /* first, second... etc */
                time.relative.special_type = SPECIAL_DAY_OF_WEEK_IN_MONTH;
                time.set_relative(amount, 1, relativeUnit);
            } else { /* last */ // previous this
                time.relative.special_type = SPECIAL_LAST_DAY_OF_WEEK_IN_MONTH;
                time.set_relative(amount, relativeBehavior, relativeUnit);
            }
        });

        register("reTimeTiny12", reTimeTiny12, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.h = meridian(time.h, matcher.group(2));
        });

        register("timeShort12", reTimeShort12, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.i = intval(matcher.group(2));
            time.h = meridian(time.h, matcher.group(3));
        });

        register("timeLong12", reTimeLong12, (time, matcher) -> {
            assert matcher.groupCount() == 4;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.i = intval(matcher.group(2));
            time.s = intval(matcher.group(3));
            time.h = meridian(time.h, matcher.group(4));
        });

        register("mssqltime", reMssqlTime, (time, matcher) -> {
            assert matcher.groupCount() == 5;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.i = intval(matcher.group(2));
            time.s = intval(matcher.group(3));
            time.us = frac(matcher.group(4));
            time.h = meridian(time.h, matcher.group(5));
        });

        register("timeshort24", reTimeShort24, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.i = intval(matcher.group(2));
            // todo if (matcher.group(?) != null) { time.z = parse_zone(time, matcher.group(?)); }
        });

        register("timelong24", reTimeLong24, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.i = intval(matcher.group(2));
            time.s = intval(matcher.group(3));
            // todo if (matcher.group(?) != null) { time.z = parse_zone(time, matcher.group(?)); }
        });

        register("iso8601long", reISO8601Long, (time, matcher) -> {
            assert matcher.groupCount() == 4;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.i = intval(matcher.group(2));
            time.s = intval(matcher.group(3));
            time.us = frac(matcher.group(4));
            // todo if (matcher.group(?) != null) { time.z = parse_zone(time, matcher.group(?)); }
        });

        register("gnunocolon", reGNUNoColon, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            if (time.have_time) {
                time.y = intval(matcher.group(1) + matcher.group(2));
            } else {
                time.h = intval(matcher.group(1));
                time.i = intval(matcher.group(2));
                time.s = 0;
            }
            time.have_time = true;
        });

        register("iso8601nocolon", reISO8601NoColon, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_time();
            time.h = intval(matcher.group(1));
            time.i = intval(matcher.group(2));
            time.s = intval(matcher.group(3));
            // todo if (matcher.group(?) != null) { time.z = parse_zone(time, matcher.group(?)); }
        });

        register("americanshort", reAmericanShort, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date();
            time.m = intval(matcher.group(1));
            time.d = intval(matcher.group(2));
        });

        register("american", reAmerican, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.m = intval(matcher.group(1));
            time.d = intval(matcher.group(2));
            time.year(matcher.group(3));
        });

        register("iso8601date4", reISO8601Date4, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
        });

        register("iso8601dateSlash", reISO8601DateSlash, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
        });

        register("dateSlash", reDateSlash, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
        });

        register("iso8601date2", reISO8601Date2, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.year(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
        });

//        register("iso8601datex", reISO8601Datex, (time, matcher) -> {
//            assert matcher.groupCount() ==3;
//            time.have_date();
//            time.y = longval(matcher.group(1));
//            time.m = intval(matcher.group(2));
//            time.d = intval(matcher.group(3));
//        });

        register("gnudateshorter", reGNUDateShorter, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date();
            time.year(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = 1;
        });

        register("gnudateshort", reGNUDateShort, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.year(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));;
        });

        register("datefull", reDateFull, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.d = intval(matcher.group(1));
            time.m = lookup_month(matcher.group(2));
            time.year(matcher.group(3));
        });

        register("pointeddate4", rePointedDate4,(time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.d = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.y = intval(matcher.group(3));
        });

        register("pointeddate2", rePointedDate2, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.d = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.year(matcher.group(3));
        });

        register("datenoday", reDateNoDay, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date();
            time.year(matcher.group(2));
            time.month(matcher.group(1));
            time.d = 1;
        });

        register("datenodayrev", reDateNoDayRev, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date();
            time.month(matcher.group(2));
            time.year(matcher.group(1));
            time.d = 1;
        });

        register("datetextual", reDateTextual, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.month(matcher.group(1));
            time.year(matcher.group(3));
            time.d = intval(matcher.group(2));
        });

        register("datenoyear", reDateNoYear, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date();
            time.month(matcher.group(1));
            time.d = intval(matcher.group(2));
        });

        register("datenoyearrev", reDateNoYearRev, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date();
            time.d = intval(matcher.group(1));
            time.month(matcher.group(2));
        });

        register("datenocolon", reDateNoColon, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
        });

        register("xmlrpc", reXML_RPC, (time, matcher) -> {
            assert matcher.groupCount() == 6;
            time.have_date().have_time();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
            time.h = intval(matcher.group(4));
            time.i = intval(matcher.group(5));
            time.s = intval(matcher.group(6));
        });

        register("xmlrpcnocolon", reXML_RPCNoColon, (time, matcher) -> {
            assert matcher.groupCount() == 6;
            time.have_date().have_time();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
            time.h = intval(matcher.group(4));
            time.i = intval(matcher.group(5));
            time.s = intval(matcher.group(6));
        });

        register("soap", reSoap, (time, matcher) -> {
            assert matcher.groupCount() == 11;
            time.have_date().have_time();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
            time.h = intval(matcher.group(4));
            time.i = intval(matcher.group(5));
            time.s = intval(matcher.group(6));
            time.us = frac(matcher.group(7));
            String tz = matcher.group(8);
            if (tz != null) {
                time.zone(tz);
            }
        });

        register("wddx", reWDDX, (time, matcher) -> {
            assert matcher.groupCount() == 6;
            time.have_date().have_time();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
            time.h = intval(matcher.group(4));
            time.i = intval(matcher.group(5));
            time.s = intval(matcher.group(6));
        });

        register("exif", reEXIF, (time, matcher) -> {
            assert matcher.groupCount() == 6;
            time.have_date().have_time();
            time.y = intval(matcher.group(1));
            time.m = intval(matcher.group(2));
            time.d = intval(matcher.group(3));
            time.h = intval(matcher.group(4));
            time.i = intval(matcher.group(5));
            time.s = intval(matcher.group(6));
        });

        register("pgydotd", rePgYearDotDay, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date();
            time.year(matcher.group(1));
            time.m = 1;
            time.d = intval(matcher.group(2));
        });

        registerCaseSensitive("isoweekday", reISOWeekday, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date().have_relative();
            time.y = intval(matcher.group(1));
            time.m = 1;
            time.d = 1;
            long w = longval(matcher.group(2));
            long d = longval(matcher.group(3));
            time.relative.d = DayOfWeek.daynr_from_weeknr(time.y, w, d);
        });

        registerCaseSensitive("isoweek", reISOWeek, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_date().have_relative();
            time.y = intval(matcher.group(1));
            time.m = 1;
            time.d = 1;
            long w = longval(matcher.group(2));
            long d = 1;
            time.relative.d = DayOfWeek.daynr_from_weeknr(time.y, w, d);
        });

        register("pgtextshort", rePgTextShort, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.month(matcher.group(1));
            time.d = intval(matcher.group(2));
            time.year(matcher.group(3));
        });

        register("pgtextreverse", rePgTextReverse,(time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_date();
            time.year(matcher.group(1));
            time.month(matcher.group(2));
            time.d = intval(matcher.group(3));
        });

        register("clf", reCLF, (time, matcher) -> {
            assert matcher.groupCount() == 10;
            time.have_time().have_date();
            time.d = intval(matcher.group(1));
            time.month(matcher.group(2));
            time.y = intval(matcher.group(3));
            time.h = intval(matcher.group(4));
            time.i = intval(matcher.group(5));
            time.s = intval(matcher.group(6));
            time.zone(matcher.group(7));
        });

        register("year4", "^" + reYear4, (time, matcher) -> { // TODO ^
            assert matcher.groupCount() == 1;
            time.y = intval(matcher.group(1));
        });

        register("ago", reAgo, (time, matcher) -> {
            time.relative.y = -time.relative.y;
            time.relative.m = -time.relative.m;
            time.relative.d = -time.relative.d;
            time.relative.h = -time.relative.h;
            time.relative.i = -time.relative.i;
            time.relative.s = -time.relative.s;
            time.relative.us = -time.relative.us; // 貌似没什么用
            time.relative.weekday = -time.relative.weekday;
            if (time.relative.weekday == 0) {
                time.relative.weekday = -7;
            }
            if (time.relative.have_special_relative && time.relative.special_type == SPECIAL_WEEKDAY) {
                time.relative.special_amount = -time.relative.special_amount;
            }
        });

        register("daytext", "^" + reDayText, (time, matcher) -> {
            time.have_relative().have_weekday_relative().unhave_time();
            RelativeUnit relativeUnit = lookup_relativeUnit(matcher.group(0));
            time.relative.weekday = relativeUnit.multiplier;
            if (time.relative.weekday_behavior != 2) {
                time.relative.weekday_behavior = 1;
            }
        });

        register("relativetextweek", reRelativeTextWeek, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_relative();
            String rel_str = matcher.group(1);
            int amount = lookup_relative(rel_str);
            int relativeBehavior = lookup_relativeBehavior(rel_str);
            RelativeUnit relativeUnit = lookup_relativeUnit(matcher.group(2));
            time.set_relative(amount, relativeBehavior, relativeUnit);
            time.relative.weekday_behavior = 2;

            /* to handle the format weekday + last/this/next week */
            if (!time.relative.have_weekday_relative) {
                time.have_weekday_relative();
                time.relative.weekday = 1;
            }
        });

        register("relativetext", reRelativeText, (time, matcher) -> {
            assert matcher.groupCount() == 2;
            time.have_relative();
            String rel_str = matcher.group(1);
            int amount = lookup_relative(rel_str);
            int relativeBehavior = lookup_relativeBehavior(rel_str);
            RelativeUnit relativeUnit = lookup_relativeUnit(matcher.group(2));
            time.set_relative(amount, relativeBehavior, relativeUnit);
        });

        register("monthfull", "^" + reMonthFull, (time, matcher) -> {
            assert matcher.groupCount() == 0;
            time.have_date();
            time.month(matcher.group(0));
        });

        register("monthabbr", "^" + reMonthAbbr, (time, matcher) -> {
            assert matcher.groupCount() == 0;
            time.have_date();
            time.month(matcher.group(0));
        });

        register("tzcorrection", "^" + reTzCorrection, (time, matcher) -> {
            time.have_tz();
            if (time.have_zone > 1) {
                return;
            }
            time.zone(matcher.group(0));
        });

        register("tz", "^" + reTz, (time, matcher) -> {
            time.have_tz();
            if (time.have_zone > 1) {
                return;
            }
            String tz = matcher.group(0);
            if (tz.charAt(0) == '(' && tz.charAt(tz.length() - 1) == ')') {
                tz = tz.substring(1, tz.length() - 1);
            }
            time.zone(tz);
        });

        register("dateshortwithtimeshort12", reDateShortWithTimeShort12, (time, matcher) -> {
            assert matcher.groupCount() == 5;
            time.have_date();
            time.month(matcher.group(1));
            time.d = intval(matcher.group(2));

            time.have_time();
            time.h = intval(matcher.group(3));
            time.i = intval(matcher.group(4));
            time.h = meridian(time.h, matcher.group(5));
        });

        register("dateshortwithtimelong12", reDateShortWithTimeLong12, (time, matcher) -> {
            assert matcher.groupCount() == 6;
            time.have_date();
            time.month(matcher.group(1));
            time.d = intval(matcher.group(2));

            time.have_time();
            time.h = intval(matcher.group(3));
            time.i = intval(matcher.group(4));
            time.s = intval(matcher.group(5));
            time.h = meridian(time.h, matcher.group(6));
        });

        register("dateshortwithtimeshort", reDateShortWithTimeShort, (time, matcher) -> {
            assert matcher.groupCount() == 4;
            time.have_date();
            time.month(matcher.group(1));
            time.d = intval(matcher.group(2));

            time.have_time();
            time.h = intval(matcher.group(3));
            time.i = intval(matcher.group(4));
        });

        register("dateshortwithtimelong", reDateShortWithTimeLong, (time, matcher) -> {
            assert matcher.groupCount() == 5;
            time.have_date();
            time.month(matcher.group(1));
            time.d = intval(matcher.group(2));

            time.have_time();
            time.h = intval(matcher.group(3));
            time.i = intval(matcher.group(4));
            time.s = intval(matcher.group(5));
        });

        register("dateshortwithtimelongtz", reDateShortWithTimeLongTz, (time, matcher) -> {
            assert matcher.groupCount() == 12;
            time.have_date();
            time.month(matcher.group(1));
            time.d = intval(matcher.group(2));

            time.have_time();
            time.h = intval(matcher.group(3));
            time.i = intval(matcher.group(4));
            time.s = intval(matcher.group(5));
            // time.us
            time.zone(matcher.group(6));
        });

        register("relative", reRelative, (time, matcher) -> {
            assert matcher.groupCount() == 3;
            time.have_relative();

            String signs = matcher.group(1);
            String rel_val = matcher.group(2);
            int minus_n = signs.length() - signs.replace("-", "").length();
            long amount = (long) (longval(rel_val) * Math.pow(-1, minus_n));
            RelativeUnit relativeUnit = lookup_relativeUnit(matcher.group(3));
            time.set_relative(amount, 1, relativeUnit);
        });

        register("whitespace", reWhitespace, (time, matcher) -> { });
    }

    public static String match(Time time, String str) {
        Matcher longestMatcher = null;
        Rule finalRule = null;

        for (Map.Entry<String, Rule> entry : rules.entrySet()) {
            Rule rule = entry.getValue();
            Matcher matcher = rule.format.matcher(str);
            if (matcher.lookingAt()) {
                if (longestMatcher == null || matcher.group(0).length() > longestMatcher.group(0).length()) {
                    longestMatcher = matcher;
                    finalRule = rule;
                }
            }
        }

        if (finalRule == null) {
            throw new TimeException("不支持解析 " + str);
        }
        finalRule.matcher.match(time, longestMatcher);

        return longestMatcher.group(0);
    }


    static long meridian(long hour, String meridian) {
        meridian = meridian.toLowerCase();
        if (meridian.equals("a")) {
            hour += hour == 12 ? -12 : 0;
        } else if (meridian.equals("p")) {
            hour += hour != 12 ? 12 : 0;
        }
        return hour;
    }

    // php 目前在使用的版本有问题
    // https://github.com/php/php-src/blob/php-8.0.0/ext/date/lib/parse_date.re#L1245
    //      加上 .最长只读 8 个, timelib_get_frac_nr(&ptr, 8);   WTF, 所以就有下面这个奇怪的逻辑....
    // php -r 'date_default_timezone_set("Asia/Shanghai");  var_dump(date_parse("2010-03-06 16:07:25.123456"));'
    //      'fraction' => double(0.123456)
    // php -r 'date_default_timezone_set("Asia/Shanghai");  var_dump(date_parse("2010-03-06 16:07:25.1234567"));'
    //      'fraction' => double(0.123456)
    // php -r 'date_default_timezone_set("Asia/Shanghai");  var_dump(date_parse("2010-03-06 16:07:25.12345678"));'
    //      "The timezone could not be found in the database"
    //  备注: date_parse 就是用 strtotime 实现的
    // 用这个版本的，估计 php 可能不会修这个逻辑，会造成兼容性问题 !!!
    // https://github.com/derickr/timelib/commit/c45bb94ebefd6ad92653f2dbf0ea6aef0e07b463
    static long frac(String str) {
        long frac = str.isEmpty() ? 0 : longval(str);
        // 兼容 php 的破逻辑, 多余 6 位的 ms 抛弃掉
        // todo 将 us 变成 纳秒...
        return (long) (frac * Math.pow(10, (6 - str.length())));
    }

    static int intval(String s) {
        return Integer.parseInt(s);
    }

    static long longval(String s) {
        return Long.parseLong(s);
    }

}