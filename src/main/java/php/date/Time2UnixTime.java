package php.date;

import org.jetbrains.annotations.Nullable;
import php.date.Time.Relative;

class Time2UnixTime {

    // 截止上个月累计过去多少天, 闰年 -1
    /*                                        jan  feb  mrt  apr  may  jun  jul  aug  sep  oct  nov  dec */
    final static int[] month_tab_leap     = {  -1,  30,  59,  90, 120, 151, 181, 212, 243, 273, 304, 334 };
    final static int[] month_tab          = {   0,  31,  59,  90, 120, 151, 181, 212, 243, 273, 304, 334 };

    /*                                        dec  jan  feb  mrt  apr  may  jun  jul  aug  sep  oct  nov  dec */
    final static int[] days_in_month_leap = {  31,  31,  29,  31,  30,  31,  30,  31,  31,  30,  31,  30,  31 };
    final static int[] days_in_month      = {  31,  31,  28,  31,  30,  31,  30,  31,  31,  30,  31,  30,  31 };

    static long days_in_month(long year, long month) {
        return Time.is_leap(year) ? days_in_month_leap[(int) month] : days_in_month[(int) month];
    }

    static long[] do_range_limit(long start, long end, long adj, long a, long b) {
        if (a < start) {
            b -= (start - a - 1) / adj + 1;
            a += adj * ((start - a - 1) / adj + 1);
        }
        if (a >= end) {
            b += a / adj;
            a -= adj * (a / adj);
        }
        return new long[]{ a, b };
    }

    static void inc_month(long[] ym/*long *y, long *m*/) {
        ym[1]++;
        if (ym[1] > 12) {
            ym[1] -= 12;
            ym[0]++;
        }
    }

    static void dec_month(long[] ym/*long *y, long *m*/) {
        ym[1]--;
        if (ym[1] < 1) {
            ym[1] += 12;
            ym[0]--;
        }
    }

    static void do_range_limit_days_relative(Time base, Relative rt) {
        long[] tmp = do_range_limit(1, 13, 12, base.m, base.y);
        base.m = tmp[0];
        base.y = tmp[1];

        long[] ym = {tmp[1], tmp[0]};
        if (rt.invert) {
            while (rt.d < 0) {
                long days = days_in_month(ym[0], ym[1]);
                rt.d += days;
                rt.m--;
                inc_month(ym);
            }
        } else {
            while (rt.d < 0) {
                dec_month(ym);
                long days = days_in_month(ym[0], ym[1]);
                rt.d += days;
                rt.m--;
            }
        }
    }

    static boolean do_range_limit_days(Time t) {
        /* can jump an entire leap year period quickly */
        if (t.d >= Parser.DAYS_PER_LYEAR_PERIOD || t.d <= -Parser.DAYS_PER_LYEAR_PERIOD) {
            t.y += Parser.YEARS_PER_LYEAR_PERIOD * (t.d / Parser.DAYS_PER_LYEAR_PERIOD);
            t.d -= Parser.DAYS_PER_LYEAR_PERIOD * (t.d / Parser.DAYS_PER_LYEAR_PERIOD);
        }

        long[] tmp = do_range_limit(1, 13, 12, t.m, t.y);
        t.m = tmp[0];
        t.y = tmp[1];

        long days_this_month = days_in_month(t.y, t.m);

        long last_month = t.m - 1;
        long last_year;
        if (last_month < 1) {
            last_month += 12;
            last_year = t.y - 1;
        } else {
            last_year = t.y;
        }

        long days_last_month = days_in_month(last_year, last_month);
        if (t.d <= 0) {
            t.d += days_last_month;
            t.m--;
            return true;
        }
        if (t.d > days_this_month) {
            t.d -= days_this_month;
            t.m++;
            return true;
        }

        return false;
    }

    static void do_adjust_for_weekday(Time time) {
        long current_dow = DayOfWeek.day_of_week(time.y, time.m, time.d);
        if (time.relative.weekday_behavior == 2) {
            /* To make "this week" work, where the current DOW is a "sunday" */
            if (current_dow == 0 && time.relative.weekday != 0) {
                time.relative.weekday -= 7;
            }

            /* To make "sunday this week" work, where the current DOW is not a
             * "sunday" */
            if (time.relative.weekday == 0 && current_dow != 0) {
                time.relative.weekday = 7;
            }

            time.d -= current_dow;
            time.d += time.relative.weekday;
            return;
        }

        long difference = time.relative.weekday - current_dow;
        if ((time.relative.d < 0 && difference < 0) || (time.relative.d >= 0 && difference <= -time.relative.weekday_behavior)) {
            difference += 7;
        }
        if (time.relative.weekday >= 0) {
            time.d += difference;
        } else {
            time.d -= (7 - (Math.abs(time.relative.weekday) - current_dow));
        }
        time.relative.have_weekday_relative = false;
    }

    static void do_rel_normalize(Time base, Relative rt) {
        {
            long[] tmp = do_range_limit(0, 1000000, 1000000, rt.us, rt.s);
            rt.us = tmp[0];
            rt.s = tmp[1];
        }
        {
            long[] tmp = do_range_limit(0, 60, 60, rt.s, rt.i);
            rt.s = tmp[0];
            rt.i = tmp[1];
        }
        {
            long[] tmp = do_range_limit(0, 60, 60, rt.i, rt.h);
            rt.i = tmp[0];
            rt.h = tmp[1];
        }
        {
            long[] tmp = do_range_limit(0, 24, 24, rt.h, rt.d);
            rt.h = tmp[0];
            rt.d = tmp[1];
        }
        {
            long[] tmp = do_range_limit(0, 12, 12, rt.m, rt.y);
            rt.m = tmp[0];
            rt.y = tmp[1];
        }

        do_range_limit_days_relative(base, rt);

        {
            long[] tmp = do_range_limit(0, 12, 12, rt.m, rt.y);
            rt.m = tmp[0];
            rt.y = tmp[1];
        }
    }

    final static long EPOCH_DAY = 719468;

    static void magic_date_calc(Time time) {
        /* The algorithm doesn't work before the year 1 */
        if (time.d < -719498) {
            return;
        }

        long g = time.d + EPOCH_DAY - 1;

        long y = (10000 * g + 14780) / 3652425;
        long ddd = g - ((365 * y) + (y / 4) - (y / 100) + (y / 400));
        if (ddd < 0) {
            y--;
            ddd = g - ((365 * y) + (y / 4) - (y / 100) + (y / 400));
        }
        long mi = (100 * ddd + 52) / 3060;
        long mm = ((mi + 2) % 12) + 1;
        y = y + (mi + 2) / 12;
        long dd = ddd - ((mi * 306 + 5) / 10) + 1;
        time.y = y;
        time.m = mm;
        time.d = dd;
    }

    static void do_normalize(Time time) {
        if (time.us != Time.UNSET) {
            long[] tmp = do_range_limit(0, 1000000, 1000000, time.us, time.s);
            time.us = tmp[0];
            time.s = tmp[1];
        }
        if (time.s != Time.UNSET) {
            long[] tmp = do_range_limit(0, 60, 60, time.s, time.i);
            time.s = tmp[0];
            time.i = tmp[1];
        }
        if (time.s != Time.UNSET) {
            long[] tmp = do_range_limit(0, 60, 60, time.i, time.h);
            time.i = tmp[0];
            time.h = tmp[1];
        }
        if (time.s != Time.UNSET) {
            long[] tmp = do_range_limit(0, 24, 24, time.h, time.d);
            time.h = tmp[0];
            time.d = tmp[1];
        }
        {
            long[] tmp = do_range_limit(1, 13, 12, time.m, time.y);
            time.m = tmp[0];
            time.y = tmp[1];
        }

        /* Short cut if we're doing things against the Epoch */
        if (time.y == 1970 && time.m == 1 && time.d != 1) {
            magic_date_calc(time);
        }

        //noinspection StatementWithEmptyBody
        while (do_range_limit_days(time)) {
        }

        {
            long[] tmp = do_range_limit(1, 13, 12, time.m, time.y);
            time.m = tmp[0];
            time.y = tmp[1];
        }
    }

    static void do_adjust_relative(Time time) {
        if (time.relative.have_weekday_relative) {
            do_adjust_for_weekday(time);
        }
        do_normalize(time);

        if (time.have_relative) {
            time.us += time.relative.us;

            time.s += time.relative.s;
            time.i += time.relative.i;
            time.h += time.relative.h;

            time.d += time.relative.d;
            time.m += time.relative.m;
            time.y += time.relative.y;
        }

        //noinspection DuplicatedCode
        switch (time.relative.first_last_day_of) {
            case Parser.SPECIAL_FIRST_DAY_OF_MONTH: /* first */
                time.d = 1;
                break;
            case Parser.SPECIAL_LAST_DAY_OF_MONTH: /* last */
                time.d = 0;
                time.m++;
                break;
        }

        do_normalize(time);
    }

    static void do_adjust_special_weekday(Time time) {
        long count, dow, rem;

        count = time.relative.special_amount;
        dow = DayOfWeek.day_of_week(time.y, time.m, time.d);

        /* Add increments of 5 weekdays as a week, leaving the DOW unchanged. */
        time.d += (count / 5) * 7;

        /* Deal with the remainder. */
        rem = (count % 5);

        if (count > 0) {
            if (rem == 0) {
                /* Head back to Friday if we stop on the weekend. */
                if (dow == 0) {
                    time.d -= 2;
                } else if (dow == 6) {
                    time.d -= 1;
                }
            } else if (dow == 6) {
                /* We ended up on Saturday, but there's still work to do, so move
                 * to Sunday and continue from there. */
                time.d += 1;
            } else if (dow + rem > 5) {
                /* We're on a weekday, but we're going past Friday, so skip right
                 * over the weekend. */
                time.d += 2;
            }
        } else {
            /* Completely mirror the forward direction. This also covers the 0
             * case, since if we start on the weekend, we want to move forward as
             * if we stopped there while going backwards. */
            if (rem == 0) {
                if (dow == 6) {
                    time.d += 2;
                } else if (dow == 0) {
                    time.d += 1;
                }
            } else if (dow == 0) {
                time.d -= 1;
            } else if (dow + rem < 1) {
                time.d -= 2;
            }
        }

        time.d += rem;
    }

    static void do_adjust_special(Time time) {
        if (time.relative.have_special_relative) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (time.relative.special_type) {
                case Parser.SPECIAL_WEEKDAY:
                    do_adjust_special_weekday(time);
                    break;
            }
        }
        do_normalize(time);
        time.relative.special_type = 0;
        time.relative.special_amount = 0;
    }

    static void do_adjust_special_early(Time time) {
        if (time.relative.have_special_relative) {
            switch (time.relative.special_type) {
                case Parser.SPECIAL_DAY_OF_WEEK_IN_MONTH:
                    time.d = 1;
                    time.m += time.relative.m;
                    time.relative.m = 0;
                    break;
                case Parser.SPECIAL_LAST_DAY_OF_WEEK_IN_MONTH:
                    time.d = 1;
                    time.m += time.relative.m + 1;
                    time.relative.m = 0;
                    break;
            }
        }

        //noinspection DuplicatedCode
        switch (time.relative.first_last_day_of) {
            case Parser.SPECIAL_FIRST_DAY_OF_MONTH: /* first */
                time.d = 1;
                break;
            case Parser.SPECIAL_LAST_DAY_OF_MONTH: /* last */
                time.d = 0;
                time.m++;
                break;
        }
        do_normalize(time);
    }

    static long do_years(long year) {
        long res = 0;
        long eras = (year - 1970) / 40000;

        if (eras < -1000000 || eras > 1000000) {
            throw new TimeException("无效年份 " + year);
        }

        if (eras != 0) {
            year = year - (eras * 40000);
            res += (Parser.SECS_PER_ERA * eras * 100);
        }

        if (year >= 1970) {
            for (long i = year - 1; i >= 1970; i--) {
                if (Time.is_leap(i)) {
                    res += (Parser.DAYS_PER_LYEAR * Parser.SECS_PER_DAY);
                } else {
                    res += (Parser.DAYS_PER_YEAR * Parser.SECS_PER_DAY);
                }
            }
        } else {
            for (long i = 1969; i >= year; i--) {
                if (Time.is_leap(i)) {
                    res -= (Parser.DAYS_PER_LYEAR * Parser.SECS_PER_DAY);
                } else {
                    res -= (Parser.DAYS_PER_YEAR * Parser.SECS_PER_DAY);
                }
            }
        }
        return res;
    }

    static long do_months(long month, long year) {
        int idx = (int) (month - 1);
        if (Time.is_leap(year)) {
            return (month_tab_leap[idx] + 1) * Parser.SECS_PER_DAY;
        } else {
            return month_tab[idx] * Parser.SECS_PER_DAY;
        }
    }

    static long do_days(long day) {
        return ((day - 1) * Parser.SECS_PER_DAY);
    }

    static long do_time(long hour, long minute, long second) {
        long res = 0;
        res += hour * 3600;
        res += minute * 60;
        res += second;
        return res;
    }

    static long do_adjust_timezone(Time t, TzInfo tz) {
        switch (t.zone_type) {
            case OFFSET:
                t.is_localtime = true;
                return -t.z;

            case ABBR: {
                t.is_localtime = true;
                return -t.z - t.dst * 3600;
            }

            case ID:
                tz = t.tz_info;
                /* Break intentionally missing */

            default:
                /* No timezone in struct, fallback to reference if possible */
                if (tz != null) {
                    t.is_localtime = true;
                    TimeOffset before = TimeOffset.of(t.sse, tz);
                    TimeOffset after = TimeOffset.of(t.sse - before.offset, tz);
                    t.zone(tz);

                    boolean in_transition = (
                            ((t.sse - after.offset) >= (after.transition_time + (before.offset - after.offset))) &&
                                    ((t.sse - after.offset) < after.transition_time)
                    );

                    long tmp;
                    if ((before.offset != after.offset) && !in_transition) {
                        tmp = -after.offset;
                    } else {
                        tmp = -t.z;
                    }

                    {
                        TimeOffset gmt_offset = TimeOffset.of(t.sse + tmp, tz);
                        t.z = gmt_offset.offset;

                        t.dst = gmt_offset.is_dst;
                        t.tz_abbr = gmt_offset.abbr;
                    }
                    return tmp;
                }
        }
        return 0;
    }

    public static void update_ts(Time time, @Nullable TzInfo tzi/*now_timezone*/) {
        long res = 0;
        do_adjust_special_early(time);
        do_adjust_relative(time);
        do_adjust_special(time);
        res += do_years(time.y);
        res += do_months(time.m, time.y);
        res += do_days(time.d);
        res += do_time(time.h, time.i, time.s);
        time.sse = res;
        res += do_adjust_timezone(time, tzi);
        time.sse = res;
        time.sse_uptodate = 1;
        time.have_relative = false;
        time.relative.have_weekday_relative = false;
        time.relative.have_special_relative = false;
        time.relative.first_last_day_of = 0;
    }
}
