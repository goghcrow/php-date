package php.date;

import static php.date.Time.is_leap;

class DayOfWeek {

    public static long day_of_week(long y, long m, long d) {
        return day_of_week_ex(y, m, d, false);
    }

    public  static long iso_day_of_week(long y, long m, long d) {
        return day_of_week_ex(y, m, d, true);
    }

    final static int[] m_table_common = { -1, 0, 3, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5 }; /* 1 = jan */
    final static int[] m_table_leap   = { -1, 6, 2, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5 }; /* 1 = jan */

    // 只支持 Gregorian calendar 不支持 Julian calendar
    static long day_of_week_ex(long y, long m, long d, boolean iso) {
        long c1 = century_value(y / 100);
        //noinspection SuspiciousNameCombination
        long y1 = positive_mod(y, 100);
        long m1 = is_leap(y) ? m_table_leap[(int) m] : m_table_common[(int) m];
        long dow = positive_mod((c1 + y1 + m1 + (y1 / 4) + d), 7);
        if (iso) {
            if (dow == 0) {
                dow = 7;
            }
        }
        return dow;
    }

    static long positive_mod(long x, long y) {
        long tmp = x % y;
        if (tmp < 0) {
            tmp += y;
        }
        return tmp;
    }

    static long century_value(long j) {
        return 6 - positive_mod(j, 4) * 2;
    }


    /*                                         jan  feb  mar  apr  may  jun  jul  aug  sep  oct  nov  dec */
    final static int[] d_table_common  = {  0,   0,  31,  59,  90, 120, 151, 181, 212, 243, 273, 304, 334 };
    final static int[] d_table_leap    = {  0,   0,  31,  60,  91, 121, 152, 182, 213, 244, 274, 305, 335 };
    final static int[] ml_table_common = {  0,  31,  28,  31,  30,  31,  30,  31,  31,  30,  31,  30,  31 };
    final static int[] ml_table_leap   = {  0,  31,  29,  31,  30,  31,  30,  31,  31,  30,  31,  30,  31 };

    static long day_of_year(long y, long m, long d) {
        return (is_leap(y) ? d_table_leap[(int) m] : d_table_common[(int) m]) + d - 1;
    }

    static long days_in_month(long y, long m) {
        return is_leap(y) ? ml_table_leap[(int) m] : ml_table_common[(int) m];
    }

    static void iso_week_from_date(long y, long m, long d, long[] iw_iy /*long *iw, long *iy*/) {
        boolean y_leap = is_leap(y);
        boolean prev_y_leap = is_leap(y - 1);
        long doy = day_of_year(y, m, d) + 1;
        if (y_leap && m > 2) {
            doy++;
        }
        long jan1weekday = day_of_week(y, 1, 1);
        long weekday = day_of_week(y, m, d);
        if (weekday == 0) {
            weekday = 7;
        }
        if (jan1weekday == 0) {
            jan1weekday = 7;
        }

        /* Find if Y M D falls in YearNumber Y-1, WeekNumber 52 or 53 */
        if (doy <= (8 - jan1weekday) && jan1weekday > 4) {
            iw_iy[1] = y - 1;
            if (jan1weekday == 5 || (jan1weekday == 6 && prev_y_leap)) {
                iw_iy[0] = 53;
            } else {
                iw_iy[0] = 52;
            }
        } else {
            iw_iy[1] = y;
        }

        /* 8. Find if Y M D falls in YearNumber Y+1, WeekNumber 1 */
        if (iw_iy[1] == y) {
            long i = y_leap ? 366 : 365;
            if ((i - (doy - (y_leap ? 1 : 0))) < (4 - weekday)) {
                iw_iy[1] = y + 1;
                iw_iy[0] = 1;
                return;
            }
        }

        /* 9. Find if Y M D falls in YearNumber Y, WeekNumber 1 through 53 */
        if (iw_iy[1] == y) {
            long j = doy + (7 - weekday) + (jan1weekday - 1);
            iw_iy[0] = j / 7;
            if (jan1weekday > 4) {
                iw_iy[0] -= 1;
            }
        }
    }

    // 注意这里参数顺序
    static long iso_date_from_date(long y, long m, long d, long[] iw_iy /*long *iy, long *iw, long *id*/) {
        iso_week_from_date(y, m, d, iw_iy);
        return day_of_week_ex(y, m, d, true);
    }

    static long daynr_from_weeknr(long iy, long iw, long id) {
        /* Figure out the dayofweek for y-1-1 */
        long dow = day_of_week(iy, 1, 1);
        /* then use that to figure out the offset for day 1 of week 1 */
        //noinspection PointlessArithmeticExpression
        long day = 0 - (dow > 4 ? dow - 7 : dow);
        /* Add weeks and days */
        return day + ((iw - 1) * 7) + id;
    }

    static void date_from_iso_date(long iy, long iw, long id, long[] ymd/*long *y, long *m, long *d*/) {
        long daynr = daynr_from_weeknr(iy, iw, id) + 1;

        // Invariant: is_leap_year == is_leap(ymd[0])
        ymd[0] = iy;
        boolean is_leap_year = is_leap(ymd[0]);

        // Establish invariant that daynr >= 0
        while (daynr <= 0) {
            ymd[0] -= 1;
            daynr += (is_leap_year = is_leap(ymd[0])) ? 366 : 365;
        }

        // Establish invariant that daynr <= number of days in *yr
        while (daynr > (is_leap_year ? 366 : 365)) {
            daynr -= is_leap_year ? 366 : 365;
            ymd[0] += 1;
            is_leap_year = is_leap(ymd[0]);
        }

        int[] table = is_leap_year ? ml_table_leap : ml_table_common;

        // Establish invariant that daynr <= number of days in *m
        ymd[1] = 1;
        while (daynr > table[(int) ymd[1]]) {
            daynr -= table[(int) ymd[1]];
            ymd[1] += 1;
        }

        ymd[2] = daynr;
    }

    static boolean valid_date(long y, long m, long d) {
        return m >= 1 && m <= 12 && d >= 1 && d <= days_in_month(y, m);
    }

    static boolean valid_time(long h, long i, long s) {
        return h >= 0 && h <= 23 && i >= 0 && i <= 59 && s >= 0 && s <= 59;
    }
}
