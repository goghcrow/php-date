package php.date;


class UnixTime2Time {

    final static int[] month_tab_leap = { -1, 30, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
    final static int[] month_tab =      { 0,  31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };

    /* Converts the time stored in the struct to localtime if localtime = true,
     * otherwise it converts it to gmttime. This is only done when necessary
     * of course. */
    static boolean apply_localtime(Time t, long localtime) {
        if (localtime > 0) {
            /* Converting from GMT time to local time */
            /* Check if TZ is set */
            if (t.tz_info == null) {
                return false;
            }
            unixtime2local(t, t.sse);
        } else {
            /* Converting from local time to GMT time */
            unixtime2gmt(t, t.sse);
        }
        return true;
    }

    /* Converts a Unix timestamp value into broken down time, in GMT */
    static void unixtime2gmt(Time t, long ts) {
        long days = ts / Parser.SECS_PER_DAY;
        long remainder = ts - (days * Parser.SECS_PER_DAY);
        if (ts < 0 && remainder == 0) {
            days++;
            remainder -= Parser.SECS_PER_DAY;
        }

        long tmp_days;
        if (ts >= 0) {
            tmp_days = days + 1;
        } else {
            tmp_days = days;
        }

        long cur_year = 1970;
        if (tmp_days > Parser.DAYS_PER_LYEAR_PERIOD || tmp_days <= -Parser.DAYS_PER_LYEAR_PERIOD) {
            cur_year += Parser.YEARS_PER_LYEAR_PERIOD * (tmp_days / Parser.DAYS_PER_LYEAR_PERIOD);
            tmp_days -= Parser.DAYS_PER_LYEAR_PERIOD * (tmp_days / Parser.DAYS_PER_LYEAR_PERIOD);
        }

        if (ts >= 0) {
            while (tmp_days >= Parser.DAYS_PER_LYEAR) {
                cur_year++;
                if (Time.is_leap(cur_year)) {
                    tmp_days -= Parser.DAYS_PER_LYEAR;
                } else {
                    tmp_days -= Parser.DAYS_PER_YEAR;
                }
            }
        } else {
            while (tmp_days <= 0) {
                cur_year--;
                if (Time.is_leap(cur_year)) {
                    tmp_days += Parser.DAYS_PER_LYEAR;
                } else {
                    tmp_days += Parser.DAYS_PER_YEAR;
                }
            }
            remainder += Parser.SECS_PER_DAY;
        }

        int[] months = Time.is_leap(cur_year) ? month_tab_leap : month_tab;
        if (Time.is_leap(cur_year) && cur_year < 1970) {
            tmp_days--;
        }
        int i = 11;
        while (i > 0) {
            if (tmp_days > months[i]) {
                break;
            }
            i--;
        }

        /* That was the date, now we do the time */
        long hours = remainder / 3600;
        long minutes = (remainder - hours * 3600) / 60;
        long seconds = remainder % 60;

        t.y = cur_year;
        t.m = i + 1;
        t.d = tmp_days - months[i];
        t.h = hours;
        t.i = minutes;
        t.s = seconds;
        t.z = 0;
        t.dst = 0;
        t.sse = ts;
        t.sse_uptodate = 1;
        t.tim_uptodate = 1;
        t.is_localtime = false;
    }

    static void unixtime2local(Time t, long ts) {
        TzInfo tz = t.tz_info;

        switch (t.zone_type) {
            case ABBR:
            case OFFSET:
                long z = t.z;
                int dst = t.dst;

                unixtime2gmt(t, ts + t.z + t.dst * 3600);

                t.sse = ts;
                t.z = z;
                t.dst = dst;
                break;

            case ID:
                TimeOffset gmt_offset = TimeOffset.of(ts, tz);
                unixtime2gmt(t, ts + gmt_offset.offset);

                /* we need to reset the sse here as unixtime2gmt modifies it */
                t.sse = ts;
                t.dst = gmt_offset.is_dst;
                t.z = gmt_offset.offset;
                t.tz_info = tz;
                t.tz_abbr = gmt_offset.abbr.toUpperCase();
                break;

            default:
                t.is_localtime = false;
                t.have_zone = 0;
                return;
        }

        t.is_localtime = true;
        t.have_zone = 1;
    }

    static void update_from_sse(Time t) {
        long z = t.z;
        int dst = t.dst;

        long sse = t.sse;

        switch (t.zone_type) {
            case ABBR:
            case OFFSET:
                unixtime2gmt(t, t.sse + t.z + t.dst * 3600);
                break;

            case ID:
                TimeOffset gmt_offset = TimeOffset.of(t.sse, t.tz_info);
                unixtime2gmt(t, t.sse + gmt_offset.offset);
                break;

            default:
                unixtime2gmt(t, t.sse);
        }

        t.sse = sse;
        t.is_localtime = true;
        t.have_zone = 1;
        t.z = z;
        t.dst = dst;
    }
}
