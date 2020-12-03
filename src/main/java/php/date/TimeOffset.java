package php.date;

import php.date.TzInfo.LInfo;
import php.date.TzInfo.TInfo;

class TimeOffset {

    final int offset; // ZoneOffset.totalSeconds     UTC offset
    final long leap_secs;
    final int is_dst;
    final String abbr;
    final long transition_time;

    TimeOffset(int offset, long leap_secs, int is_dst, String abbr, long transition_time) {
        this.offset = offset;
        this.leap_secs = leap_secs;
        this.is_dst = is_dst;
        this.abbr = abbr;
        this.transition_time = transition_time;
    }

    // timelib_get_time_zone_info
    static TimeOffset of(long ts, TzInfo tz) {
        TInfo to = fetch_timezone_offset(tz, ts);
        Long transition_time = fetch_timezone_transition(tz, ts);

        int offset;
        String abbr;
        int is_dst;
        if (to != null) {
            offset = to.offset;
            // 这里可能不对
            // timeOffset.abbr = tz.timezone_abbr[(int) to.abbr_idx];
            if (to.abbr_idx >= tz.timezone_abbr.length) {
                abbr = "GMT";
            } else {
                abbr = tz.timezone_abbr[(int) to.abbr_idx];
            }

            is_dst = to.isdst;
        } else {
            offset = 0;
            abbr = tz.timezone_abbr[0];
            is_dst = 0;
            transition_time = 0L;
        }

        long leap_secs;
        LInfo tl = fetch_leaptime_offset(tz, ts);
        if (tl == null) {
            leap_secs = 0L;
        } else {
            leap_secs = -tl.offset;
        }

        assert transition_time != null;
        return new TimeOffset(offset, leap_secs, is_dst, abbr, transition_time);
    }

    static TInfo fetch_timezone_offset(TzInfo tz, long ts) {
        /* If there is no transition time, we pick the first one, if that doesn't
         * exist we return NULL */
        if (tz.timecnt == 0 || tz.trans.length == 0) {
            if (tz.typecnt == 1) {
			    return tz.type[0];
            }
            return null;
        }

        /* If the TS is lower than the first transition time, then we scan over
         * all the transition times to find the first non-DST one, or the first
         * one in case there are only DST entries. Not sure which smartass came up
         * with this idea in the first though :) */
        if (ts < tz.trans[0]) {
            return tz.type[0];
        }

        /* In all other cases we loop through the available transition times to find
         * the correct entry */
        for (int i = 0; i < tz.timecnt; i++) {
            if (ts < tz.trans[i]) {
                return tz.type[tz.trans_idx[i - 1]];
            }
        }
        return tz.type[tz.trans_idx[(int) (tz.timecnt - 1)]];
    }

    static Long fetch_timezone_transition(TzInfo tz, long ts) {
        if (tz.timecnt == 0 || tz.trans.length == 0) {
            if (tz.typecnt == 1) {
                return Long.MIN_VALUE;
            }
            return null;
        }
        if (ts < tz.trans[0]) {
            return Long.MIN_VALUE;
        }
        for (int i = 0; i < tz.timecnt; i++) {
            if (ts < tz.trans[i]) {
                return tz.trans[i - 1];
            }
        }
        return tz.trans[(int) (tz.timecnt - 1)];
    }

    static LInfo fetch_leaptime_offset(TzInfo tz, long ts)  {
        if (tz.leapcnt == 0 || tz.leap_times.length == 0) {
            return null;
        }

        for (int i = (int) (tz.leapcnt - 1); i > 0; i--) {
            if (ts > tz.leap_times[i].trans) {
                return tz.leap_times[i];
            }
        }
        return null;
    }
}
