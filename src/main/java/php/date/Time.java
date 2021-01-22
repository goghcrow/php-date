package php.date;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

import static php.date.Time.ZoneType.*;

/**
 * PHP 时期时间扩展
 *
 * 移植自
 *  https://github.com/php/php-src/tree/master/ext/date/lib
 *  https://github.com/derickr/timelib
 *
 * 文档
 *  https://www.php.net/manual/zh/function.strtotime.php
 *  https://www.php.net/manual/zh/datetime.formats.php
 *  https://www.php.net/manual/zh/datetime.formats.time.php
 *  https://www.php.net/manual/zh/datetime.formats.date.php
 *  https://www.php.net/manual/zh/datetime.formats.compound.php
 *  https://www.php.net/manual/zh/datetime.formats.relative.php
 */
@SuppressWarnings("UnusedReturnValue")
public class Time {
    /**
     * php strtotime
     * @link https://www.php.net/manual/zh/function.strtotime.php
     */
    public static Time parse(@NotNull String time_str) {
        return parse(time_str, Instant.now().getEpochSecond());
    }

    /**
     * php strtotime
     * @link https://www.php.net/manual/zh/function.strtotime.php
     */
    public static Time parse(@NotNull String time_str, long utc_ts) {
        return parse(time_str, ZoneId.systemDefault().getId(), utc_ts);
    }

    /**
     * php strtotime
     * @link https://www.php.net/manual/zh/function.strtotime.php
     */
    public static Time parse(@NotNull String time_str, @NotNull ZonedDateTime zdt) {
        return parse(time_str, zdt.getZone().getId(), Timestamp.valueOf(zdt.toLocalDateTime()).getTime());
    }

    /**
     * php strtotime
     * @link https://www.php.net/manual/zh/function.strtotime.php
     */
    public static Time parse(@NotNull String time_str, @NotNull String zone_id, long zoned_ts) {
        Objects.requireNonNull(time_str);
        TimeException.setLastTimeStr(time_str);

        Time now = new Time();

        TzInfo tz = TzInfo.of(zone_id);
        now.tz_info = tz;
        now.zone_type = ZoneType.ID;

        UnixTime2Time.unixtime2local(now, zoned_ts);

        Time time = new Time();
        String str = time_str;
        while (str.length() > 0) {
            String matched = Parser.match(time, str);
            str = str.substring(matched.length());
        }

        if (time.have_time && time.h != UNSET && time.i != UNSET && time.s != UNSET) {
            if (!DayOfWeek.valid_time(time.h, time.i, time.s)) {
                time.warnings.add(new Warning(Warning.WARN_INVALID_TIME, "错误的时间 " + time_str));
            }
        }
        if (time.have_date && time.y != UNSET && time.m != UNSET && time.d != UNSET) {
            if (!DayOfWeek.valid_date(time.y, time.m, time.d)) {
                time.warnings.add(new Warning(Warning.WARN_INVALID_DATE, "错误的日期 " + time_str));
            }
        }

        time.fill_holes(now, Parser.NO_CLONE);

        Time2UnixTime.update_ts(time, tz);

        return time;
    }

    /**
     * php mktime
     * @link https://www.php.net/manual/en/function.mktime.php
     */
    public static Time make(int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
        return make(ZoneId.systemDefault().getId(), hour, min, sec, month, day, year);
    }

    /**
     * php mktime
     * @link https://www.php.net/manual/en/function.mktime.php
     */
    public static Time make(@NotNull String zone_id, int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
        Time now = new Time();
        TzInfo timeZone = TzInfo.of(zone_id);
        now.tz_info = timeZone;
        now.zone_type = ZoneType.ID;
        UnixTime2Time.unixtime2local(now, Instant.now().getEpochSecond());

        now.h = hour;
        if (min != null) {
            now.i = min;
        }
        if (sec != null) {
            now.s = sec;
        }
        if (month != null) {
            now.m = month;
        }
        if (day != null) {
            now.d = day;
        }
        if (year != null) {
            now.year(year);
        }

        Time2UnixTime.update_ts(now, timeZone);
        return now;
    }

    /**
     * php gmmktime
     * @link https://www.php.net/manual/en/function.gmmktime.php
     */
    public static Time makeGMT(int hour, Integer min, Integer sec, Integer month, Integer day, Integer year) {
        Time now = new Time();
        UnixTime2Time.unixtime2gmt(now, Instant.now().getEpochSecond());

        now.h = hour;
        if (min != null) {
            now.i = min;
        }
        if (sec != null) {
            now.s = sec;
        }
        if (month != null) {
            now.m = month;
        }
        if (day != null) {
            now.d = day;
        }
        if (year != null) {
            now.year(year);
        }

        Time2UnixTime.update_ts(now, null);
        return now;
    }


    static class Relative {
        long y, m, d; /* Years, Months and Days */
        long h, i, s; /* Hours, mInutes and Seconds */
        long us;      /* Microseconds 微秒 */

        int weekday;          /* Stores the day in 'next monday' */
        int weekday_behavior; /* 0: the current day should *not* be counted when advancing forwards;
                                 1: the current day *should* be counted
                                 2:  */

        int first_last_day_of;
        boolean invert;       /* Whether the difference should be inverted */
        long days;            /* Contains the number of *days*, instead of Y-M-D differences */

        // struct {
            int special_type;
            long special_amount;
        // } special;

        boolean have_weekday_relative;
        boolean have_special_relative;
    }


    // 时区表示法 https://zh.wikipedia.org/wiki/时区
    /*  1 time offset,
     *  2 TimeZone abbreviation
     *  3 TimeZone identifier, */
    enum ZoneType {
        OFFSET, // UTC 偏移量 ,  @see java.time.ZoneOffset
        ABBR, // 不是标准，有问题
        ID, // @see java.time.ZoneRegion
    }

    static class Abbreviation {
        long utc_offset;
        String abbr;
        int dst; // Daylight Saving Time
    }

    enum TimeUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        MONTH,
        YEAR,
        WEEKDAY,
        SPECIAL,
        MICROSEC
    }

    static class Warning {
        final static int WARN_DOUBLE_TZ     = 0x101;
        final static int WARN_INVALID_TIME  = 0x102;
        final static int WARN_INVALID_DATE  = 0x103;
        final static int WARN_TRAILING_DATA = 0x11a;
        final int code;
        final String msg;
        Warning(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    static class RelativeUnit {
        final String name;
        final TimeUnit unit;
        final int multiplier;
        RelativeUnit(String name, TimeUnit unit, int multiplier) {
            this.name = name;
            this.unit = unit;
            this.multiplier = multiplier;
        }
    }

    final static int UNSET = -99999;

    long      y, m, d;     /* Year, Month, Day */
    long      h, i, s;     /* Hour, mInute, Second */
    long      us;          /* Microseconds */
    long      z;           /* UTC offset in seconds */
    String    tz_abbr;     /* Timezone abbreviation (display only) */
    TzInfo    tz_info;     /* Timezone structure */
    int       dst;         /* Flag if we were parsing a DST zone */
    Relative relative;

    long      sse;         /* Seconds since epoch */

    boolean   have_time;
    boolean   have_date;
    int       have_zone;
    boolean   have_relative;
    // boolean   have_weeknr_day;

    long     sse_uptodate; /* !0 if the sse member is up to date with the date/time members */
    long     tim_uptodate; /* !0 if the date/time members are up to date with the sse member */
    boolean  is_localtime; /*  1 if the current struct represents localtime, 0 if it is in GMT */
    ZoneType zone_type;

    List<Warning> warnings;

    @Override
    public String toString() {
        return zonedDateTime().toString();
    }

    Time() {
        y = m = d = h = i = s = us = z = UNSET;
        dst = UNSET;
        is_localtime = false;
        zone_type = null;
        relative = new Relative();
        relative.days = UNSET;
        warnings = new ArrayList<>(0);
    }

    // https://www.php.net/manual/en/datetime.formats.compound.php
    // The "Unix Timestamp" format sets the timezone to UTC.
    public long epochSecond() {
        return sse;
    }

    public long epochMilli() {
        return sse * 1000 + us / 1000;
    }

    public Instant instant() {
        return Instant.ofEpochSecond(sse, us * 1000);
    }

    public ZonedDateTime zonedDateTime() {
        return instant().atZone(ZoneId.systemDefault());
    }

    public LocalDateTime localDateTime() {
        return zonedDateTime().toLocalDateTime();
    }



    Time have_time() {
        if (have_time) {
            throw new TimeException("重复的时间声明");
        } else {
            have_time = true;
            h = 0;
            i = 0;
            s = 0;
            us = 0;
            return this;
        }
    }

    Time unhave_time() {
        have_time = false;
        h = 0;
        i = 0;
        s = 0;
        us = 0;
        return this;
    }

    Time have_date() {
        if (have_date) {
            throw new TimeException("重复的日期声明");
        } else {
            have_date = true;
            return this;
        }
    }

    Time unhave_date() {
        have_date = false;
        d = 0;
        m = 0;
        y = 0;
        return this;
    }

    Time have_relative() {
        have_relative = true;
        return this;
    }

    Time have_weekday_relative() {
        have_relative = true;
        relative.have_weekday_relative = true;
        return this;
    }

    Time have_special_relative() {
        have_relative = true;
        relative.have_special_relative = true;
        return this;
    }

    Time have_tz() {
        if (have_zone > 0) {
            if (have_zone > 1) {
                throw new TimeException("重复的时区声明");
            } else {
                warnings.add(new Warning(Warning.WARN_DOUBLE_TZ, "声明两次时区"));
                have_zone++;
                return this;
            }
        } else {
            have_zone++;
            return this;
        }
    }

    Time set_relative(long amount, int behavior, RelativeUnit relativeUnit) {
        switch (relativeUnit.unit) {
            case MICROSEC: relative.us += amount * relativeUnit.multiplier; break;
            case SECOND:   relative.s += amount * relativeUnit.multiplier; break;
            case MINUTE:   relative.i += amount * relativeUnit.multiplier; break;
            case HOUR:     relative.h += amount * relativeUnit.multiplier; break;
            case DAY:      relative.d += amount * relativeUnit.multiplier; break;
            case MONTH:    relative.m += amount * relativeUnit.multiplier; break;
            case YEAR:     relative.y += amount * relativeUnit.multiplier; break;

            case WEEKDAY:
                have_weekday_relative().unhave_time();
                relative.d += (amount > 0 ? amount - 1 : amount) * 7;
                relative.weekday = relativeUnit.multiplier;
                relative.weekday_behavior = behavior;
                break;

            case SPECIAL:
                have_special_relative().unhave_time();
                relative.special_type = relativeUnit.multiplier;
                relative.special_amount = amount;
        }
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    void fill_holes(Time now, int options) {
        if ((options & Parser.OVERRIDE_TIME) == 0 && have_date && !have_time) {
            h = 0;
            i = 0;
            s = 0;
            us = 0;
        }

        if (y != UNSET || m != UNSET || d != UNSET || h != UNSET || i != UNSET || s != UNSET) {
            if (us == UNSET) us = 0;
        } else {
            if (us == UNSET) us = now.us != UNSET ? now.us : 0;
        }

        if (y == UNSET) y = now.y != UNSET ? now.y : 0;
        if (m == UNSET) m = now.m != UNSET ? now.m : 0;
        if (d == UNSET) d = now.d != UNSET ? now.d : 0;
        if (h == UNSET) h = now.h != UNSET ? now.h : 0;
        if (i == UNSET) i = now.i != UNSET ? now.i : 0;
        if (s == UNSET) s = now.s != UNSET ? now.s : 0;
        if (z == UNSET) z = now.z != UNSET ? now.z : 0;
        if (dst == UNSET) dst = now.dst != UNSET ? now.dst : 0;

        if (tz_abbr == null) {
            tz_abbr = now.tz_abbr;
        }
        if (tz_info == null) {
            tz_info = now.tz_info != null ? ((options & Parser.NO_CLONE) == 0 ? now.tz_info.clone() : now.tz_info) : null;
        }

        if (zone_type == null && now.zone_type != null) {
            zone_type = now.zone_type;
            // tz_abbr = now.tz_abbr ? timelib_strdup(now.tz_abbr) : NULL;
            // tz_info = now.tz_info ? timelib_tzinfo_clone(now.tz_info) : NULL;
            is_localtime = true;
        }
    }

    Time zone(long utc_offset) {
        tz_abbr = null;
        z = utc_offset;
        have_zone = 1;
        zone_type = OFFSET;
        dst = 0;
        tz_info = null;
        return this;
    }

    Time zone(Abbreviation abbr) {
        tz_abbr = abbr.abbr;
        z = abbr.utc_offset;
        have_zone = 1;
        zone_type = ABBR;
        dst = abbr.dst;
        tz_info = null;
        return this;
    }

    Time zone(TzInfo tz) {
        TimeOffset gmt_offset = TimeOffset.of(sse, tz);
        z = gmt_offset.offset;
        // assert dst == gmt_offset.is_dst
        dst = gmt_offset.is_dst;
        tz_info = tz;
        tz_abbr = gmt_offset.abbr;

        have_zone = 1;
        zone_type = ID;
        return this;
    }

    // timelib_parse_zone
    Time zone(String str) {
        long offset = 0;

        Matcher matcher = Parser.reTzCorrectionPtn.matcher(str);
        if (matcher.lookingAt()) {
            String sign = matcher.group(2);
            int hours = Parser.intval(matcher.group(3));
            int minutes = 0;
            String minutesStr = matcher.group(4);
            if (minutesStr != null) {
                minutes = Parser.intval(minutesStr);
            }
            offset = hours * 3600 + minutes * 60;
            if ("-".equals(sign)) {
                offset = -offset;
            }

            dst = 0;
            is_localtime = true;
            zone_type = ZoneType.OFFSET;
        } else {
            TzMapping tz = TzMapping.abbr_search(str);
            if (tz != null) {
                zone_type = ZoneType.ABBR;
                dst = tz.is_dst;
                tz_abbr = str.toUpperCase();
                offset = tz.gmt_offset;
                offset -= tz.is_dst * 3600;
            }

            if (tz == null || "UTC".equalsIgnoreCase(str)) {
                if ("UTC".equalsIgnoreCase(str)) {
                    str = "UTC";
                }
                zone_type = ZoneType.ID;
                tz_info = TzInfo.of(str);
            }
        }
        z = offset;
        return this;
    }

    Time month(String month_str) {
        m = Parser.lookup_month(month_str);
        return this;
    }

    // libtime 和 php 源码上下这两个year 对于 100 的逻辑不统一 ...
    // parse_date.re 的逻辑，不包括 100
    Time year(String year_str) {
        y = Parser.intval(year_str);
        if (year_str.length() < 4 && y < 100) {
            y += y < 70 ? 2000 : 1900;
        }
        return this;
    }

    // https://github.com/php/php-src/blob/41b096b392e551df51c80d29f6f30c163a0630ea/ext/date/php_date.c#L1065
    // 的逻辑，包括 100
    Time year(int year) {
        if (year >= 0 && year < 70) {
            year += 2000;
        } else if (year >= 70 && year <= 100) {
            year += 1900;
        }
        y = year;
        return this;
    }

    // https://zh.wikipedia.org/wiki/闰年
    static boolean is_leap(long y) {
        return (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0));
    }
}
