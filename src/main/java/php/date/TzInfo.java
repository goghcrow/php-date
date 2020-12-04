package php.date;

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自动生成代码，请勿修改
 * @author xiaofeng
 * @version 2020d
 * @since 2020-12-04 12:35:50
 */
@SuppressWarnings("SpellCheckingInspection")
class TzInfo {

    @SuppressWarnings("SpellCheckingInspection")
    static class TInfo {
        final int offset;
        final int isdst;
        final long abbr_idx;

        final long isstdcnt;
        final long isgmtcnt;

        TInfo(int offset, int isdst, long abbr_idx, long isstdcnt, long isgmtcnt) {
            this.offset = offset;
            this.isdst = isdst;
            this.abbr_idx = abbr_idx;
            this.isstdcnt = isstdcnt;
            this.isgmtcnt = isgmtcnt;
        }
    }

    static class LInfo {
        final long trans;
        final int offset;

        LInfo(long trans, int offset) {
            this.trans = trans;
            this.offset = offset;
        }
    }

    static class Loc {
        final String countryCode;
        final double latitude;
        final double longitude;
        final String comments;

        Loc(String countryCode, double latitude, double longitude, String comments) {
            this.countryCode = countryCode;
            this.latitude = latitude;
            this.longitude = longitude;
            this.comments = comments;
        }
    }


    final String name;

    final long ttisgmtcnt;
    final long ttisstdcnt;
    final long leapcnt;
    final long timecnt;
    final long typecnt;
    final long charcnt;

    final long[] trans;
    final int[] trans_idx;

    final TInfo[] type;
    final String[] timezone_abbr;

    final LInfo[] leap_times;
    final int bc;
    final Loc location;

    TzInfo(String name,
           long ttisgmtcnt, long ttisstdcnt, long leapcnt, long timecnt, long typecnt, long charcnt,
           long[] trans, int[] trans_idx, TInfo[] type, String[] timezone_abbr, LInfo[] leap_times,
           int bc, Loc location) {
        this.name = name;
        this.ttisgmtcnt = ttisgmtcnt;
        this.ttisstdcnt = ttisstdcnt;
        this.leapcnt = leapcnt;
        this.timecnt = timecnt;
        this.typecnt = typecnt;
        this.charcnt = charcnt;
        this.trans = trans;
        this.trans_idx = trans_idx;
        this.type = type;
        this.timezone_abbr = timezone_abbr;
        this.leap_times = leap_times;
        this.bc = bc;
        this.location = location;
    }

    @Override
    public TzInfo clone() {
        // todo
        return this;
    }

    final static Map<String, TzInfo> cache = new ConcurrentHashMap<>();

    static TzInfo of(String name)  {
        return cache.computeIfAbsent(name, key -> of1(name));
    }

    static TzInfo of1(String name)  {
        try {
            Objects.requireNonNull(name);
            Long val = table.get(name);
            if (val == null) {
                throw new TimeException("不支持的时区 " + name);
            }
            int pos = (int) (val >>> 32);
            int sz = (int) (val & 0xffffffffL);
            return parse_tz_from_db(name, pos, sz);
        } catch (IOException e) {
            throw new TimeException(e);
        }
    }

//    final static Path tzDbPath;
//    static {
//        try {
//            URI path = Objects.requireNonNull(TzInfo.class.getClassLoader().getResource("php/timezonedb.dta")).toURI();
//            tzDbPath = Paths.get(path);
//        } catch (NullPointerException | URISyntaxException e) {
//            throw new TimeException(e);
//        }
//    }
    final static SeekableByteChannel tzDbCh;
    static {
        InputStream is = TzInfo.class.getClassLoader().getResourceAsStream("php/timezonedb.dta");
        tzDbCh = new MemoryChannel(is);
    }

    static TzInfo parse_tz_from_db(String name, int pos, int sz) throws IOException {
        // try (SeekableByteChannel tzDbCh = Files.newByteChannel(tzDbPath, StandardOpenOption.READ)) {
        // todo reuse buf
        ByteBuffer buf = ByteBuffer.allocateDirect(sz);
        synchronized (TzInfo.class) {
            tzDbCh.position(pos);
            int read = tzDbCh.read(buf);
            if (read != sz) {
                throw new AssertionError();
            }
        }
        buf.rewind();
        TzInfo tzInfo = parse_tz(name, Bytes.of(buf));
        buf.flip();
        return tzInfo;
        // }
    }


    // !!! 参照 timelib 自己做的二进制格式解析 !!!
    static TzInfo parse_tz(String name, Bytes bytes) {
        // read_php_preamble
        /* read ID */
        String version = new String(bytes.read(4));
        if (!version.equals("PHP2")) {
            throw new AssertionError();
        }

        /* read BC flag */
        int bc = bytes.s1();
        /* read country code */
        String countryCode = new String(bytes.read(2));
        /* skip rest of preamble */
        bytes.skip(13);

        // read_32bit_header
        int _bit32_ttisgmtcnt = (int) bytes.u4();
        int _bit32_ttisstdcnt = (int) bytes.u4();
        int _bit32_leapcnt = (int) bytes.u4();
        int _bit32_timecnt = (int) bytes.u4();
        int _bit32_typecnt = (int) bytes.u4();
        int _bit32_charcnt = (int) bytes.u4();

        // skip_32bit_transitions
        if (_bit32_timecnt > 0) {
            bytes.skip(4 * _bit32_timecnt);
            bytes.skip(_bit32_timecnt);
        }

        // skip_32bit_types
        /* Offset Types */
        bytes.skip(6 * _bit32_typecnt);
        /* Abbreviations */
        bytes.skip(_bit32_charcnt);
        /* Leap seconds (only use in 'right/') format */
        if (_bit32_leapcnt > 0) {
            bytes.skip(4 * _bit32_leapcnt * 2);
        }
        /* Standard/Wall Indicators (unused) */
        if (_bit32_ttisstdcnt > 0) {
            bytes.skip(_bit32_ttisstdcnt);
        }
        /* UT/Local Time Indicators (unused) */
        if (_bit32_ttisgmtcnt > 0) {
            bytes.skip(_bit32_ttisgmtcnt);
        }

        // skip_64bit_preamble
        String TZif = new String(bytes.read(5));
        if ("TZif2".equals(TZif) || "TZif3".equals(TZif)) {
            bytes.skip(15);
        } else {
            throw new AssertionError();
        }

        // read_64bit_header
        long ttisgmtcnt = bytes.u4();
        long ttisstdcnt = bytes.u4();
        long leapcnt = bytes.u4();
        long timecnt = bytes.u4();
        long typecnt = bytes.u4();
        long charcnt = bytes.u4();

        // read_64bit_transitions
        long[] trans = new long[(int) timecnt];
        int[] trans_idx = new int[(int) timecnt];
        if (timecnt > 0) {
            for (int i = 0; i < timecnt; i++) {
                trans[i] = bytes.s8(); // todo u8
            }
            for (int i = 0; i < timecnt; i++) {
                trans_idx[i] = bytes.s1();
            }
        }

        // read_64bit_types
        /* Offset Types */
        int[] offset = new int[(int) typecnt];
        int[] isdst = new int[(int) typecnt];
        long[] abbr_idx = new long[(int) typecnt];
        for (int i = 0; i < typecnt; i++) {
            offset[i] = (int) bytes.u4();
            isdst[i] = bytes.u1();
            abbr_idx[i] = bytes.u1();
        }
        /* Abbreviations */
        byte[] abbrs = bytes.read((int) charcnt);
        List<String> abbr_lst = new ArrayList<>();
        int from = 0;
        for (int i = 0; i < abbrs.length; i++) {
            if (abbrs[i] == '\0') {
                abbr_lst.add(new String(Arrays.copyOfRange(abbrs, from, i)));
                from = i + 1;
            }
        }
        String[] timezone_abbr = abbr_lst.toArray(new String[0]);
        /* Leap seconds (only use in 'right/') format */
        LInfo[] leap_times = new LInfo[(int) leapcnt];
        if (leapcnt > 0) {
            throw new AssertionError();
        }
        /* Standard/Wall Indicators (unused) */
        long[] ttisstdcnts = new long[(int) typecnt];
        if (ttisstdcnt > 0) {
            for (int i = 0; i < ttisstdcnt; i++) {
                ttisstdcnts[i] = bytes.s1();
            }
        }
        /* UT/Local Time Indicators (unused) */
        long[] ttisgmtcnts = new long[(int) typecnt];
        if (ttisgmtcnt > 0) {
            for (int i = 0; i < ttisgmtcnt; i++) {
                ttisgmtcnts[i] = bytes.s1();
            }
        }

        TInfo[] type = new TInfo[(int) typecnt];
        for (int i = 0; i < typecnt; i++) {
            type[i] = new TInfo(offset[i], isdst[i], abbr_idx[i], ttisstdcnts[i], ttisgmtcnts[i]);
        }

        // skip_posix_string
        int n_count = 0;
        do {
            if (bytes.s1() == '\n') {
                n_count++;
            }
        } while (n_count < 2);

        // read_location
        double latitude = bytes.u4();
        latitude = (latitude / 100000) - 90;
        double longitude = bytes.u4();
        longitude = (longitude / 100000) - 180;

        int comments_len = (int) bytes.u4();
        String comments = new String(bytes.read(comments_len));

        if (bytes.buf.remaining() > 0) {
            throw new AssertionError();
        }

        return new TzInfo(name,
                ttisgmtcnt, ttisstdcnt, leapcnt, timecnt, typecnt, charcnt,
                trans, trans_idx, type, timezone_abbr, leap_times,
                bc, new Loc(countryCode, latitude, longitude, comments));
    }


    static class Bytes {
        ByteBuffer buf;
        Bytes(ByteBuffer buf) {  this.buf = buf;  }
        static Bytes of(ByteBuffer buf) {  return new Bytes(buf);  }
        int pos() { return buf.position(); }
        void pos(int pos) { buf.position(pos); }
        float f()  { return buf.getFloat(); }
        double d() { return buf.getDouble(); }
        long l()   { return buf.getLong(); }
        byte s1()  { return buf.get(); }
        short s2() { return buf.getShort(); }
        int s4()   { return buf.getInt(); }
        long s8()  { return buf.getLong(); }
        int u1()   { return (short) (buf.get() & 0xff); }
        int u2()   { return buf.getShort() & 0xffff; }
        long u4()  { return (long) buf.getInt() & 0xffffffffL; }
        void skip(int n) { pos(pos() + n); }
        byte[] read(int n) {  byte[] bytes = new byte[n]; buf.get(bytes); return bytes;  }
    }
    static class MemoryChannel implements SeekableByteChannel {
        final AtomicBoolean closed = new AtomicBoolean();
        byte[] data;
        int position, size;
        MemoryChannel(final byte[] data) {
            this.data = data;
            size = data.length;
        }
        MemoryChannel(InputStream is) {
            this(readFully(is));
        }
        static byte[] readFully(InputStream is) {
            try {
                return IOUtils.readFully(is, -1, false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override public long position() { return position; }
        @Override public long size() { return size; }
        @Override public void close() { closed.set(true); }
        @Override public boolean isOpen() { return !closed.get(); }
        @Override public int write(final ByteBuffer b) { throw new UnsupportedOperationException(); }
        @Override
        public SeekableByteChannel position(final long newPosition) throws IOException {
            ensureOpen();
            assert newPosition >= 0L && newPosition <= Integer.MAX_VALUE;
            position = (int) newPosition;
            return this;
        }
        @Override
        public SeekableByteChannel truncate(final long newSize) {
            assert newSize >= 0L && newSize <= Integer.MAX_VALUE;
            if (size > newSize) {
                size = (int) newSize;
            }
            if (position > newSize) {
                position = (int) newSize;
            }
            return this;
        }
        @Override
        public int read(final ByteBuffer buf) throws IOException {
            ensureOpen();
            int wanted = buf.remaining();
            final int possible = size - position;
            if (possible <= 0) {
                return -1;
            }
            if (wanted > possible) {
                wanted = possible;
            }
            buf.put(data, position, wanted);
            position += wanted;
            return wanted;
        }
        void ensureOpen() throws ClosedChannelException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
        }
    }

    static Map<String, Long> table;

    static {
        Map<String, Long> tbl = new HashMap<>();
        
		tbl.put("Africa/Abidjan",                   (0L      << 32) | 160L );
		tbl.put("Africa/Accra",                     (160L    << 32) | 828L );
		tbl.put("Africa/Addis_Ababa",               (988L    << 32) | 263L );
		tbl.put("Africa/Algiers",                   (1251L   << 32) | 747L );
		tbl.put("Africa/Asmara",                    (1998L   << 32) | 263L );
		tbl.put("Africa/Asmera",                    (2261L   << 32) | 263L );
		tbl.put("Africa/Bamako",                    (2524L   << 32) | 160L );
		tbl.put("Africa/Bangui",                    (2684L   << 32) | 161L );
		tbl.put("Africa/Banjul",                    (2845L   << 32) | 160L );
		tbl.put("Africa/Bissau",                    (3005L   << 32) | 206L );
		tbl.put("Africa/Blantyre",                  (3211L   << 32) | 161L );
		tbl.put("Africa/Brazzaville",               (3372L   << 32) | 161L );
		tbl.put("Africa/Bujumbura",                 (3533L   << 32) | 161L );
		tbl.put("Africa/Cairo",                     (3694L   << 32) | 1967L);
		tbl.put("Africa/Casablanca",                (5661L   << 32) | 2441L);
		tbl.put("Africa/Ceuta",                     (8102L   << 32) | 2062L);
		tbl.put("Africa/Conakry",                   (10164L  << 32) | 160L );
		tbl.put("Africa/Dakar",                     (10324L  << 32) | 160L );
		tbl.put("Africa/Dar_es_Salaam",             (10484L  << 32) | 263L );
		tbl.put("Africa/Djibouti",                  (10747L  << 32) | 263L );
		tbl.put("Africa/Douala",                    (11010L  << 32) | 161L );
		tbl.put("Africa/El_Aaiun",                  (11171L  << 32) | 2307L);
		tbl.put("Africa/Freetown",                  (13478L  << 32) | 160L );
		tbl.put("Africa/Gaborone",                  (13638L  << 32) | 161L );
		tbl.put("Africa/Harare",                    (13799L  << 32) | 161L );
		tbl.put("Africa/Johannesburg",              (13960L  << 32) | 258L );
		tbl.put("Africa/Juba",                      (14218L  << 32) | 665L );
		tbl.put("Africa/Kampala",                   (14883L  << 32) | 263L );
		tbl.put("Africa/Khartoum",                  (15146L  << 32) | 691L );
		tbl.put("Africa/Kigali",                    (15837L  << 32) | 161L );
		tbl.put("Africa/Kinshasa",                  (15998L  << 32) | 186L );
		tbl.put("Africa/Lagos",                     (16184L  << 32) | 161L );
		tbl.put("Africa/Libreville",                (16345L  << 32) | 161L );
		tbl.put("Africa/Lome",                      (16506L  << 32) | 160L );
		tbl.put("Africa/Luanda",                    (16666L  << 32) | 161L );
		tbl.put("Africa/Lubumbashi",                (16827L  << 32) | 186L );
		tbl.put("Africa/Lusaka",                    (17013L  << 32) | 161L );
		tbl.put("Africa/Malabo",                    (17174L  << 32) | 161L );
		tbl.put("Africa/Maputo",                    (17335L  << 32) | 161L );
		tbl.put("Africa/Maseru",                    (17496L  << 32) | 258L );
		tbl.put("Africa/Mbabane",                   (17754L  << 32) | 258L );
		tbl.put("Africa/Mogadishu",                 (18012L  << 32) | 263L );
		tbl.put("Africa/Monrovia",                  (18275L  << 32) | 220L );
		tbl.put("Africa/Nairobi",                   (18495L  << 32) | 263L );
		tbl.put("Africa/Ndjamena",                  (18758L  << 32) | 211L );
		tbl.put("Africa/Niamey",                    (18969L  << 32) | 161L );
		tbl.put("Africa/Nouakchott",                (19130L  << 32) | 160L );
		tbl.put("Africa/Ouagadougou",               (19290L  << 32) | 160L );
		tbl.put("Africa/Porto-Novo",                (19450L  << 32) | 161L );
		tbl.put("Africa/Sao_Tome",                  (19611L  << 32) | 266L );
		tbl.put("Africa/Timbuktu",                  (19877L  << 32) | 160L );
		tbl.put("Africa/Tripoli",                   (20037L  << 32) | 637L );
		tbl.put("Africa/Tunis",                     (20674L  << 32) | 701L );
		tbl.put("Africa/Windhoek",                  (21375L  << 32) | 967L );
		tbl.put("America/Adak",                     (22342L  << 32) | 2384L);
		tbl.put("America/Anchorage",                (24726L  << 32) | 2402L);
		tbl.put("America/Anguilla",                 (27128L  << 32) | 160L );
		tbl.put("America/Antigua",                  (27288L  << 32) | 160L );
		tbl.put("America/Araguaina",                (27448L  << 32) | 905L );
		tbl.put("America/Argentina/Buenos_Aires",   (28353L  << 32) | 1109L);
		tbl.put("America/Argentina/Catamarca",      (29462L  << 32) | 1115L);
		tbl.put("America/Argentina/ComodRivadavia", (30577L  << 32) | 1088L);
		tbl.put("America/Argentina/Cordoba",        (31665L  << 32) | 1142L);
		tbl.put("America/Argentina/Jujuy",          (32807L  << 32) | 1070L);
		tbl.put("America/Argentina/La_Rioja",       (33877L  << 32) | 1115L);
		tbl.put("America/Argentina/Mendoza",        (34992L  << 32) | 1100L);
		tbl.put("America/Argentina/Rio_Gallegos",   (36092L  << 32) | 1103L);
		tbl.put("America/Argentina/Salta",          (37195L  << 32) | 1082L);
		tbl.put("America/Argentina/San_Juan",       (38277L  << 32) | 1115L);
		tbl.put("America/Argentina/San_Luis",       (39392L  << 32) | 1127L);
		tbl.put("America/Argentina/Tucuman",        (40519L  << 32) | 1128L);
		tbl.put("America/Argentina/Ushuaia",        (41647L  << 32) | 1109L);
		tbl.put("America/Aruba",                    (42756L  << 32) | 198L );
		tbl.put("America/Asuncion",                 (42954L  << 32) | 2056L);
		tbl.put("America/Atikokan",                 (45010L  << 32) | 381L );
		tbl.put("America/Atka",                     (45391L  << 32) | 2368L);
		tbl.put("America/Bahia",                    (47759L  << 32) | 1041L);
		tbl.put("America/Bahia_Banderas",           (48800L  << 32) | 1590L);
		tbl.put("America/Barbados",                 (50390L  << 32) | 326L );
		tbl.put("America/Belem",                    (50716L  << 32) | 606L );
		tbl.put("America/Belize",                   (51322L  << 32) | 960L );
		tbl.put("America/Blanc-Sablon",             (52282L  << 32) | 338L );
		tbl.put("America/Boa_Vista",                (52620L  << 32) | 651L );
		tbl.put("America/Bogota",                   (53271L  << 32) | 258L );
		tbl.put("America/Boise",                    (53529L  << 32) | 2438L);
		tbl.put("America/Buenos_Aires",             (55967L  << 32) | 1088L);
		tbl.put("America/Cambridge_Bay",            (57055L  << 32) | 2116L);
		tbl.put("America/Campo_Grande",             (59171L  << 32) | 1474L);
		tbl.put("America/Cancun",                   (60645L  << 32) | 830L );
		tbl.put("America/Caracas",                  (61475L  << 32) | 276L );
		tbl.put("America/Catamarca",                (61751L  << 32) | 1088L);
		tbl.put("America/Cayenne",                  (62839L  << 32) | 210L );
		tbl.put("America/Cayman",                   (63049L  << 32) | 194L );
		tbl.put("America/Chicago",                  (63243L  << 32) | 3608L);
		tbl.put("America/Chihuahua",                (66851L  << 32) | 1534L);
		tbl.put("America/Coral_Harbour",            (68385L  << 32) | 348L );
		tbl.put("America/Cordoba",                  (68733L  << 32) | 1088L);
		tbl.put("America/Costa_Rica",               (69821L  << 32) | 328L );
		tbl.put("America/Creston",                  (70149L  << 32) | 238L );
		tbl.put("America/Cuiaba",                   (70387L  << 32) | 1439L);
		tbl.put("America/Curacao",                  (71826L  << 32) | 198L );
		tbl.put("America/Danmarkshavn",             (72024L  << 32) | 736L );
		tbl.put("America/Dawson",                   (72760L  << 32) | 1648L);
		tbl.put("America/Dawson_Creek",             (74408L  << 32) | 1094L);
		tbl.put("America/Denver",                   (75502L  << 32) | 2477L);
		tbl.put("America/Detroit",                  (77979L  << 32) | 2267L);
		tbl.put("America/Dominica",                 (80246L  << 32) | 160L );
		tbl.put("America/Edmonton",                 (80406L  << 32) | 2373L);
		tbl.put("America/Eirunepe",                 (82779L  << 32) | 683L );
		tbl.put("America/El_Salvador",              (83462L  << 32) | 236L );
		tbl.put("America/Ensenada",                 (83698L  << 32) | 2354L);
		tbl.put("America/Fort_Nelson",              (86052L  << 32) | 2272L);
		tbl.put("America/Fort_Wayne",               (88324L  << 32) | 1678L);
		tbl.put("America/Fortaleza",                (90002L  << 32) | 766L );
		tbl.put("America/Glace_Bay",                (90768L  << 32) | 2231L);
		tbl.put("America/Godthab",                  (92999L  << 32) | 1890L);
		tbl.put("America/Goose_Bay",                (94889L  << 32) | 3254L);
		tbl.put("America/Grand_Turk",               (98143L  << 32) | 1860L);
		tbl.put("America/Grenada",                  (100003L << 32) | 160L );
		tbl.put("America/Guadeloupe",               (100163L << 32) | 160L );
		tbl.put("America/Guatemala",                (100323L << 32) | 292L );
		tbl.put("America/Guayaquil",                (100615L << 32) | 276L );
		tbl.put("America/Guyana",                   (100891L << 32) | 248L );
		tbl.put("America/Halifax",                  (101139L << 32) | 3466L);
		tbl.put("America/Havana",                   (104605L << 32) | 2428L);
		tbl.put("America/Hermosillo",               (107033L << 32) | 459L );
		tbl.put("America/Indiana/Indianapolis",     (107492L << 32) | 1703L);
		tbl.put("America/Indiana/Knox",             (109195L << 32) | 2461L);
		tbl.put("America/Indiana/Marengo",          (111656L << 32) | 1757L);
		tbl.put("America/Indiana/Petersburg",       (113413L << 32) | 1935L);
		tbl.put("America/Indiana/Tell_City",        (115348L << 32) | 1716L);
		tbl.put("America/Indiana/Vevay",            (117064L << 32) | 1452L);
		tbl.put("America/Indiana/Vincennes",        (118516L << 32) | 1734L);
		tbl.put("America/Indiana/Winamac",          (120250L << 32) | 1812L);
		tbl.put("America/Indianapolis",             (122062L << 32) | 1678L);
		tbl.put("America/Inuvik",                   (123740L << 32) | 1926L);
		tbl.put("America/Iqaluit",                  (125666L << 32) | 2074L);
		tbl.put("America/Jamaica",                  (127740L << 32) | 494L );
		tbl.put("America/Jujuy",                    (128234L << 32) | 1060L);
		tbl.put("America/Juneau",                   (129294L << 32) | 2385L);
		tbl.put("America/Kentucky/Louisville",      (131679L << 32) | 2814L);
		tbl.put("America/Kentucky/Monticello",      (134493L << 32) | 2384L);
		tbl.put("America/Knox_IN",                  (136877L << 32) | 2440L);
		tbl.put("America/Kralendijk",               (139317L << 32) | 198L );
		tbl.put("America/La_Paz",                   (139515L << 32) | 244L );
		tbl.put("America/Lima",                     (139759L << 32) | 418L );
		tbl.put("America/Los_Angeles",              (140177L << 32) | 2855L);
		tbl.put("America/Louisville",               (143032L << 32) | 2784L);
		tbl.put("America/Lower_Princes",            (145816L << 32) | 198L );
		tbl.put("America/Maceio",                   (146014L << 32) | 772L );
		tbl.put("America/Managua",                  (146786L << 32) | 442L );
		tbl.put("America/Manaus",                   (147228L << 32) | 631L );
		tbl.put("America/Marigot",                  (147859L << 32) | 160L );
		tbl.put("America/Martinique",               (148019L << 32) | 244L );
		tbl.put("America/Matamoros",                (148263L << 32) | 1464L);
		tbl.put("America/Mazatlan",                 (149727L << 32) | 1591L);
		tbl.put("America/Mendoza",                  (151318L << 32) | 1088L);
		tbl.put("America/Menominee",                (152406L << 32) | 2317L);
		tbl.put("America/Merida",                   (154723L << 32) | 1466L);
		tbl.put("America/Metlakatla",               (156189L << 32) | 1458L);
		tbl.put("America/Mexico_City",              (157647L << 32) | 1608L);
		tbl.put("America/Miquelon",                 (159255L << 32) | 1678L);
		tbl.put("America/Moncton",                  (160933L << 32) | 3190L);
		tbl.put("America/Monterrey",                (164123L << 32) | 1471L);
		tbl.put("America/Montevideo",               (165594L << 32) | 1522L);
		tbl.put("America/Montreal",                 (167116L << 32) | 3506L);
		tbl.put("America/Montserrat",               (170622L << 32) | 160L );
		tbl.put("America/Nassau",                   (170782L << 32) | 2270L);
		tbl.put("America/New_York",                 (173052L << 32) | 3568L);
		tbl.put("America/Nipigon",                  (176620L << 32) | 2167L);
		tbl.put("America/Nome",                     (178787L << 32) | 2392L);
		tbl.put("America/Noronha",                  (181179L << 32) | 744L );
		tbl.put("America/North_Dakota/Beulah",      (181923L << 32) | 2413L);
		tbl.put("America/North_Dakota/Center",      (184336L << 32) | 2413L);
		tbl.put("America/North_Dakota/New_Salem",   (186749L << 32) | 2419L);
		tbl.put("America/Nuuk",                     (189168L << 32) | 1912L);
		tbl.put("America/Ojinaga",                  (191080L << 32) | 1536L);
		tbl.put("America/Panama",                   (192616L << 32) | 194L );
		tbl.put("America/Pangnirtung",              (192810L << 32) | 2132L);
		tbl.put("America/Paramaribo",               (194942L << 32) | 274L );
		tbl.put("America/Phoenix",                  (195216L << 32) | 369L );
		tbl.put("America/Port-au-Prince",           (195585L << 32) | 1446L);
		tbl.put("America/Port_of_Spain",            (197031L << 32) | 160L );
		tbl.put("America/Porto_Acre",               (197191L << 32) | 640L );
		tbl.put("America/Porto_Velho",              (197831L << 32) | 596L );
		tbl.put("America/Puerto_Rico",              (198427L << 32) | 258L );
		tbl.put("America/Punta_Arenas",             (198685L << 32) | 1934L);
		tbl.put("America/Rainy_River",              (200619L << 32) | 2168L);
		tbl.put("America/Rankin_Inlet",             (202787L << 32) | 1926L);
		tbl.put("America/Recife",                   (204713L << 32) | 738L );
		tbl.put("America/Regina",                   (205451L << 32) | 1013L);
		tbl.put("America/Resolute",                 (206464L << 32) | 1927L);
		tbl.put("America/Rio_Branco",               (208391L << 32) | 644L );
		tbl.put("America/Rosario",                  (209035L << 32) | 1088L);
		tbl.put("America/Santa_Isabel",             (210123L << 32) | 2354L);
		tbl.put("America/Santarem",                 (212477L << 32) | 625L );
		tbl.put("America/Santiago",                 (213102L << 32) | 2559L);
		tbl.put("America/Santo_Domingo",            (215661L << 32) | 470L );
		tbl.put("America/Sao_Paulo",                (216131L << 32) | 1510L);
		tbl.put("America/Scoresbysund",             (217641L << 32) | 1957L);
		tbl.put("America/Shiprock",                 (219598L << 32) | 2456L);
		tbl.put("America/Sitka",                    (222054L << 32) | 2360L);
		tbl.put("America/St_Barthelemy",            (224414L << 32) | 160L );
		tbl.put("America/St_Johns",                 (224574L << 32) | 3701L);
		tbl.put("America/St_Kitts",                 (228275L << 32) | 160L );
		tbl.put("America/St_Lucia",                 (228435L << 32) | 160L );
		tbl.put("America/St_Thomas",                (228595L << 32) | 160L );
		tbl.put("America/St_Vincent",               (228755L << 32) | 160L );
		tbl.put("America/Swift_Current",            (228915L << 32) | 590L );
		tbl.put("America/Tegucigalpa",              (229505L << 32) | 264L );
		tbl.put("America/Thule",                    (229769L << 32) | 1528L);
		tbl.put("America/Thunder_Bay",              (231297L << 32) | 2240L);
		tbl.put("America/Tijuana",                  (233537L << 32) | 2387L);
		tbl.put("America/Toronto",                  (235924L << 32) | 3535L);
		tbl.put("America/Tortola",                  (239459L << 32) | 160L );
		tbl.put("America/Vancouver",                (239619L << 32) | 2929L);
		tbl.put("America/Virgin",                   (242548L << 32) | 160L );
		tbl.put("America/Whitehorse",               (242708L << 32) | 1648L);
		tbl.put("America/Winnipeg",                 (244356L << 32) | 2909L);
		tbl.put("America/Yakutat",                  (247265L << 32) | 2333L);
		tbl.put("America/Yellowknife",              (249598L << 32) | 2001L);
		tbl.put("Antarctica/Casey",                 (251599L << 32) | 401L );
		tbl.put("Antarctica/Davis",                 (252000L << 32) | 314L );
		tbl.put("Antarctica/DumontDUrville",        (252314L << 32) | 222L );
		tbl.put("Antarctica/Macquarie",             (252536L << 32) | 2274L);
		tbl.put("Antarctica/Mawson",                (254810L << 32) | 217L );
		tbl.put("Antarctica/McMurdo",               (255027L << 32) | 2487L);
		tbl.put("Antarctica/Palmer",                (257514L << 32) | 1436L);
		tbl.put("Antarctica/Rothera",               (258950L << 32) | 183L );
		tbl.put("Antarctica/South_Pole",            (259133L << 32) | 2449L);
		tbl.put("Antarctica/Syowa",                 (261582L << 32) | 182L );
		tbl.put("Antarctica/Troll",                 (261764L << 32) | 1179L);
		tbl.put("Antarctica/Vostok",                (262943L << 32) | 183L );
		tbl.put("Arctic/Longyearbyen",              (263126L << 32) | 2240L);
		tbl.put("Asia/Aden",                        (265366L << 32) | 177L );
		tbl.put("Asia/Almaty",                      (265543L << 32) | 1032L);
		tbl.put("Asia/Amman",                       (266575L << 32) | 1865L);
		tbl.put("Asia/Anadyr",                      (268440L << 32) | 1219L);
		tbl.put("Asia/Aqtau",                       (269659L << 32) | 1016L);
		tbl.put("Asia/Aqtobe",                      (270675L << 32) | 1036L);
		tbl.put("Asia/Ashgabat",                    (271711L << 32) | 631L );
		tbl.put("Asia/Ashkhabad",                   (272342L << 32) | 631L );
		tbl.put("Asia/Atyrau",                      (272973L << 32) | 1024L);
		tbl.put("Asia/Baghdad",                     (273997L << 32) | 995L );
		tbl.put("Asia/Bahrain",                     (274992L << 32) | 211L );
		tbl.put("Asia/Baku",                        (275203L << 32) | 1239L);
		tbl.put("Asia/Bangkok",                     (276442L << 32) | 211L );
		tbl.put("Asia/Barnaul",                     (276653L << 32) | 1247L);
		tbl.put("Asia/Beirut",                      (277900L << 32) | 2166L);
		tbl.put("Asia/Bishkek",                     (280066L << 32) | 995L );
		tbl.put("Asia/Brunei",                      (281061L << 32) | 215L );
		tbl.put("Asia/Calcutta",                    (281276L << 32) | 297L );
		tbl.put("Asia/Chita",                       (281573L << 32) | 1253L);
		tbl.put("Asia/Choibalsan",                  (282826L << 32) | 979L );
		tbl.put("Asia/Chongqing",                   (283805L << 32) | 573L );
		tbl.put("Asia/Chungking",                   (284378L << 32) | 573L );
		tbl.put("Asia/Colombo",                     (284951L << 32) | 384L );
		tbl.put("Asia/Dacca",                       (285335L << 32) | 349L );
		tbl.put("Asia/Damascus",                    (285684L << 32) | 2306L);
		tbl.put("Asia/Dhaka",                       (287990L << 32) | 349L );
		tbl.put("Asia/Dili",                        (288339L << 32) | 239L );
		tbl.put("Asia/Dubai",                       (288578L << 32) | 177L );
		tbl.put("Asia/Dushanbe",                    (288755L << 32) | 603L );
		tbl.put("Asia/Famagusta",                   (289358L << 32) | 2055L);
		tbl.put("Asia/Gaza",                        (291413L << 32) | 2340L);
		tbl.put("Asia/Harbin",                      (293753L << 32) | 573L );
		tbl.put("Asia/Hebron",                      (294326L << 32) | 2367L);
		tbl.put("Asia/Ho_Chi_Minh",                 (296693L << 32) | 363L );
		tbl.put("Asia/Hong_Kong",                   (297056L << 32) | 1215L);
		tbl.put("Asia/Hovd",                        (298271L << 32) | 946L );
		tbl.put("Asia/Irkutsk",                     (299217L << 32) | 1281L);
		tbl.put("Asia/Istanbul",                    (300498L << 32) | 1959L);
		tbl.put("Asia/Jakarta",                     (302457L << 32) | 380L );
		tbl.put("Asia/Jayapura",                    (302837L << 32) | 287L );
		tbl.put("Asia/Jerusalem",                   (303124L << 32) | 2300L);
		tbl.put("Asia/Kabul",                       (305424L << 32) | 220L );
		tbl.put("Asia/Kamchatka",                   (305644L << 32) | 1196L);
		tbl.put("Asia/Karachi",                     (306840L << 32) | 391L );
		tbl.put("Asia/Kashgar",                     (307231L << 32) | 177L );
		tbl.put("Asia/Kathmandu",                   (307408L << 32) | 224L );
		tbl.put("Asia/Katmandu",                    (307632L << 32) | 224L );
		tbl.put("Asia/Khandyga",                    (307856L << 32) | 1313L);
		tbl.put("Asia/Kolkata",                     (309169L << 32) | 297L );
		tbl.put("Asia/Krasnoyarsk",                 (309466L << 32) | 1244L);
		tbl.put("Asia/Kuala_Lumpur",                (310710L << 32) | 415L );
		tbl.put("Asia/Kuching",                     (311125L << 32) | 509L );
		tbl.put("Asia/Kuwait",                      (311634L << 32) | 177L );
		tbl.put("Asia/Macao",                       (311811L << 32) | 1239L);
		tbl.put("Asia/Macau",                       (313050L << 32) | 1239L);
		tbl.put("Asia/Magadan",                     (314289L << 32) | 1250L);
		tbl.put("Asia/Makassar",                    (315539L << 32) | 339L );
		tbl.put("Asia/Manila",                      (315878L << 32) | 340L );
		tbl.put("Asia/Muscat",                      (316218L << 32) | 177L );
		tbl.put("Asia/Nicosia",                     (316395L << 32) | 2033L);
		tbl.put("Asia/Novokuznetsk",                (318428L << 32) | 1194L);
		tbl.put("Asia/Novosibirsk",                 (319622L << 32) | 1253L);
		tbl.put("Asia/Omsk",                        (320875L << 32) | 1232L);
		tbl.put("Asia/Oral",                        (322107L << 32) | 1032L);
		tbl.put("Asia/Phnom_Penh",                  (323139L << 32) | 211L );
		tbl.put("Asia/Pontianak",                   (323350L << 32) | 387L );
		tbl.put("Asia/Pyongyang",                   (323737L << 32) | 249L );
		tbl.put("Asia/Qatar",                       (323986L << 32) | 211L );
		tbl.put("Asia/Qostanay",                    (324197L << 32) | 1049L);
		tbl.put("Asia/Qyzylorda",                   (325246L << 32) | 1066L);
		tbl.put("Asia/Rangoon",                     (326312L << 32) | 280L );
		tbl.put("Asia/Riyadh",                      (326592L << 32) | 177L );
		tbl.put("Asia/Saigon",                      (326769L << 32) | 363L );
		tbl.put("Asia/Sakhalin",                    (327132L << 32) | 1238L);
		tbl.put("Asia/Samarkand",                   (328370L << 32) | 606L );
		tbl.put("Asia/Seoul",                       (328976L << 32) | 629L );
		tbl.put("Asia/Shanghai",                    (329605L << 32) | 585L );
		tbl.put("Asia/Singapore",                   (330190L << 32) | 395L );
		tbl.put("Asia/Srednekolymsk",               (330585L << 32) | 1254L);
		tbl.put("Asia/Taipei",                      (331839L << 32) | 773L );
		tbl.put("Asia/Tashkent",                    (332612L << 32) | 620L );
		tbl.put("Asia/Tbilisi",                     (333232L << 32) | 1047L);
		tbl.put("Asia/Tehran",                      (334279L << 32) | 2594L);
		tbl.put("Asia/Tel_Aviv",                    (336873L << 32) | 2300L);
		tbl.put("Asia/Thimbu",                      (339173L << 32) | 215L );
		tbl.put("Asia/Thimphu",                     (339388L << 32) | 215L );
		tbl.put("Asia/Tokyo",                       (339603L << 32) | 321L );
		tbl.put("Asia/Tomsk",                       (339924L << 32) | 1247L);
		tbl.put("Asia/Ujung_Pandang",               (341171L << 32) | 266L );
		tbl.put("Asia/Ulaanbaatar",                 (341437L << 32) | 924L );
		tbl.put("Asia/Ulan_Bator",                  (342361L << 32) | 903L );
		tbl.put("Asia/Urumqi",                      (343264L << 32) | 190L );
		tbl.put("Asia/Ust-Nera",                    (343454L << 32) | 1284L);
		tbl.put("Asia/Vientiane",                   (344738L << 32) | 211L );
		tbl.put("Asia/Vladivostok",                 (344949L << 32) | 1239L);
		tbl.put("Asia/Yakutsk",                     (346188L << 32) | 1238L);
		tbl.put("Asia/Yangon",                      (347426L << 32) | 280L );
		tbl.put("Asia/Yekaterinburg",               (347706L << 32) | 1269L);
		tbl.put("Asia/Yerevan",                     (348975L << 32) | 1163L);
		tbl.put("Atlantic/Azores",                  (350138L << 32) | 3502L);
		tbl.put("Atlantic/Bermuda",                 (353640L << 32) | 1990L);
		tbl.put("Atlantic/Canary",                  (355630L << 32) | 1923L);
		tbl.put("Atlantic/Cape_Verde",              (357553L << 32) | 282L );
		tbl.put("Atlantic/Faeroe",                  (357835L << 32) | 1827L);
		tbl.put("Atlantic/Faroe",                   (359662L << 32) | 1827L);
		tbl.put("Atlantic/Jan_Mayen",               (361489L << 32) | 2240L);
		tbl.put("Atlantic/Madeira",                 (363729L << 32) | 3502L);
		tbl.put("Atlantic/Reykjavik",               (367231L << 32) | 1174L);
		tbl.put("Atlantic/South_Georgia",           (368405L << 32) | 176L );
		tbl.put("Atlantic/St_Helena",               (368581L << 32) | 160L );
		tbl.put("Atlantic/Stanley",                 (368741L << 32) | 1226L);
		tbl.put("Australia/ACT",                    (369967L << 32) | 2216L);
		tbl.put("Australia/Adelaide",               (372183L << 32) | 2249L);
		tbl.put("Australia/Brisbane",               (374432L << 32) | 468L );
		tbl.put("Australia/Broken_Hill",            (374900L << 32) | 2283L);
		tbl.put("Australia/Canberra",               (377183L << 32) | 2216L);
		tbl.put("Australia/Currie",                 (379399L << 32) | 2238L);
		tbl.put("Australia/Darwin",                 (381637L << 32) | 334L );
		tbl.put("Australia/Eucla",                  (381971L << 32) | 521L );
		tbl.put("Australia/Hobart",                 (382492L << 32) | 2349L);
		tbl.put("Australia/LHI",                    (384841L << 32) | 1872L);
		tbl.put("Australia/Lindeman",               (386713L << 32) | 532L );
		tbl.put("Australia/Lord_Howe",              (387245L << 32) | 1888L);
		tbl.put("Australia/Melbourne",              (389133L << 32) | 2224L);
		tbl.put("Australia/North",                  (391357L << 32) | 316L );
		tbl.put("Australia/NSW",                    (391673L << 32) | 2216L);
		tbl.put("Australia/Perth",                  (393889L << 32) | 502L );
		tbl.put("Australia/Queensland",             (394391L << 32) | 445L );
		tbl.put("Australia/South",                  (394836L << 32) | 2234L);
		tbl.put("Australia/Sydney",                 (397070L << 32) | 2244L);
		tbl.put("Australia/Tasmania",               (399314L << 32) | 2328L);
		tbl.put("Australia/Victoria",               (401642L << 32) | 2216L);
		tbl.put("Australia/West",                   (403858L << 32) | 472L );
		tbl.put("Australia/Yancowinna",             (404330L << 32) | 2255L);
		tbl.put("Brazil/Acre",                      (406585L << 32) | 640L );
		tbl.put("Brazil/DeNoronha",                 (407225L << 32) | 728L );
		tbl.put("Brazil/East",                      (407953L << 32) | 1456L);
		tbl.put("Brazil/West",                      (409409L << 32) | 616L );
		tbl.put("Canada/Atlantic",                  (410025L << 32) | 3436L);
		tbl.put("Canada/Central",                   (413461L << 32) | 2880L);
		tbl.put("Canada/Eastern",                   (416341L << 32) | 3506L);
		tbl.put("Canada/Mountain",                  (419847L << 32) | 2344L);
		tbl.put("Canada/Newfoundland",              (422191L << 32) | 3667L);
		tbl.put("Canada/Pacific",                   (425858L << 32) | 2904L);
		tbl.put("Canada/Saskatchewan",              (428762L << 32) | 992L );
		tbl.put("Canada/Yukon",                     (429754L << 32) | 1626L);
		tbl.put("CET",                              (431380L << 32) | 2106L);
		tbl.put("Chile/Continental",                (433486L << 32) | 2541L);
		tbl.put("Chile/EasterIsland",               (436027L << 32) | 2245L);
		tbl.put("CST6CDT",                          (438272L << 32) | 2322L);
		tbl.put("Cuba",                             (440594L << 32) | 2428L);
		tbl.put("EET",                              (443022L << 32) | 1920L);
		tbl.put("Egypt",                            (444942L << 32) | 1967L);
		tbl.put("Eire",                             (446909L << 32) | 3504L);
		tbl.put("EST",                              (450413L << 32) | 126L );
		tbl.put("EST5EDT",                          (450539L << 32) | 2322L);
		tbl.put("Etc/GMT",                          (452861L << 32) | 126L );
		tbl.put("Etc/GMT+0",                        (452987L << 32) | 126L );
		tbl.put("Etc/GMT+1",                        (453113L << 32) | 128L );
		tbl.put("Etc/GMT+10",                       (453241L << 32) | 129L );
		tbl.put("Etc/GMT+11",                       (453370L << 32) | 129L );
		tbl.put("Etc/GMT+12",                       (453499L << 32) | 129L );
		tbl.put("Etc/GMT+2",                        (453628L << 32) | 128L );
		tbl.put("Etc/GMT+3",                        (453756L << 32) | 128L );
		tbl.put("Etc/GMT+4",                        (453884L << 32) | 128L );
		tbl.put("Etc/GMT+5",                        (454012L << 32) | 128L );
		tbl.put("Etc/GMT+6",                        (454140L << 32) | 128L );
		tbl.put("Etc/GMT+7",                        (454268L << 32) | 128L );
		tbl.put("Etc/GMT+8",                        (454396L << 32) | 128L );
		tbl.put("Etc/GMT+9",                        (454524L << 32) | 128L );
		tbl.put("Etc/GMT-0",                        (454652L << 32) | 126L );
		tbl.put("Etc/GMT-1",                        (454778L << 32) | 129L );
		tbl.put("Etc/GMT-10",                       (454907L << 32) | 130L );
		tbl.put("Etc/GMT-11",                       (455037L << 32) | 130L );
		tbl.put("Etc/GMT-12",                       (455167L << 32) | 130L );
		tbl.put("Etc/GMT-13",                       (455297L << 32) | 130L );
		tbl.put("Etc/GMT-14",                       (455427L << 32) | 130L );
		tbl.put("Etc/GMT-2",                        (455557L << 32) | 129L );
		tbl.put("Etc/GMT-3",                        (455686L << 32) | 129L );
		tbl.put("Etc/GMT-4",                        (455815L << 32) | 129L );
		tbl.put("Etc/GMT-5",                        (455944L << 32) | 129L );
		tbl.put("Etc/GMT-6",                        (456073L << 32) | 129L );
		tbl.put("Etc/GMT-7",                        (456202L << 32) | 129L );
		tbl.put("Etc/GMT-8",                        (456331L << 32) | 129L );
		tbl.put("Etc/GMT-9",                        (456460L << 32) | 129L );
		tbl.put("Etc/GMT0",                         (456589L << 32) | 126L );
		tbl.put("Etc/Greenwich",                    (456715L << 32) | 126L );
		tbl.put("Etc/UCT",                          (456841L << 32) | 126L );
		tbl.put("Etc/Universal",                    (456967L << 32) | 126L );
		tbl.put("Etc/UTC",                          (457093L << 32) | 126L );
		tbl.put("Etc/Zulu",                         (457219L << 32) | 126L );
		tbl.put("Europe/Amsterdam",                 (457345L << 32) | 2922L);
		tbl.put("Europe/Andorra",                   (460267L << 32) | 1754L);
		tbl.put("Europe/Astrakhan",                 (462021L << 32) | 1195L);
		tbl.put("Europe/Athens",                    (463216L << 32) | 2274L);
		tbl.put("Europe/Belfast",                   (465490L << 32) | 3660L);
		tbl.put("Europe/Belgrade",                  (469150L << 32) | 1932L);
		tbl.put("Europe/Berlin",                    (471082L << 32) | 2330L);
		tbl.put("Europe/Bratislava",                (473412L << 32) | 2313L);
		tbl.put("Europe/Brussels",                  (475725L << 32) | 2945L);
		tbl.put("Europe/Bucharest",                 (478670L << 32) | 2196L);
		tbl.put("Europe/Budapest",                  (480866L << 32) | 2380L);
		tbl.put("Europe/Busingen",                  (483246L << 32) | 1929L);
		tbl.put("Europe/Chisinau",                  (485175L << 32) | 2402L);
		tbl.put("Europe/Copenhagen",                (487577L << 32) | 2149L);
		tbl.put("Europe/Dublin",                    (489726L << 32) | 3504L);
		tbl.put("Europe/Gibraltar",                 (493230L << 32) | 3064L);
		tbl.put("Europe/Guernsey",                  (496294L << 32) | 3660L);
		tbl.put("Europe/Helsinki",                  (499954L << 32) | 1912L);
		tbl.put("Europe/Isle_of_Man",               (501866L << 32) | 3660L);
		tbl.put("Europe/Istanbul",                  (505526L << 32) | 1959L);
		tbl.put("Europe/Jersey",                    (507485L << 32) | 3660L);
		tbl.put("Europe/Kaliningrad",               (511145L << 32) | 1525L);
		tbl.put("Europe/Kiev",                      (512670L << 32) | 2120L);
		tbl.put("Europe/Kirov",                     (514790L << 32) | 1179L);
		tbl.put("Europe/Lisbon",                    (515969L << 32) | 3500L);
		tbl.put("Europe/Ljubljana",                 (519469L << 32) | 1932L);
		tbl.put("Europe/London",                    (521401L << 32) | 3660L);
		tbl.put("Europe/Luxembourg",                (525061L << 32) | 2958L);
		tbl.put("Europe/Madrid",                    (528019L << 32) | 2642L);
		tbl.put("Europe/Malta",                     (530661L << 32) | 2632L);
		tbl.put("Europe/Mariehamn",                 (533293L << 32) | 1912L);
		tbl.put("Europe/Minsk",                     (535205L << 32) | 1333L);
		tbl.put("Europe/Monaco",                    (536538L << 32) | 2956L);
		tbl.put("Europe/Moscow",                    (539494L << 32) | 1567L);
		tbl.put("Europe/Nicosia",                   (541061L << 32) | 2014L);
		tbl.put("Europe/Oslo",                      (543075L << 32) | 2240L);
		tbl.put("Europe/Paris",                     (545315L << 32) | 2974L);
		tbl.put("Europe/Podgorica",                 (548289L << 32) | 1932L);
		tbl.put("Europe/Prague",                    (550221L << 32) | 2313L);
		tbl.put("Europe/Riga",                      (552534L << 32) | 2210L);
		tbl.put("Europe/Rome",                      (554744L << 32) | 2653L);
		tbl.put("Europe/Samara",                    (557397L << 32) | 1252L);
		tbl.put("Europe/San_Marino",                (558649L << 32) | 2653L);
		tbl.put("Europe/Sarajevo",                  (561302L << 32) | 1932L);
		tbl.put("Europe/Saratov",                   (563234L << 32) | 1211L);
		tbl.put("Europe/Simferopol",                (564445L << 32) | 1471L);
		tbl.put("Europe/Skopje",                    (565916L << 32) | 1932L);
		tbl.put("Europe/Sofia",                     (567848L << 32) | 2089L);
		tbl.put("Europe/Stockholm",                 (569937L << 32) | 1921L);
		tbl.put("Europe/Tallinn",                   (571858L << 32) | 2160L);
		tbl.put("Europe/Tirane",                    (574018L << 32) | 2096L);
		tbl.put("Europe/Tiraspol",                  (576114L << 32) | 2402L);
		tbl.put("Europe/Ulyanovsk",                 (578516L << 32) | 1297L);
		tbl.put("Europe/Uzhgorod",                  (579813L << 32) | 2076L);
		tbl.put("Europe/Vaduz",                     (581889L << 32) | 1921L);
		tbl.put("Europe/Vatican",                   (583810L << 32) | 2653L);
		tbl.put("Europe/Vienna",                    (586463L << 32) | 2212L);
		tbl.put("Europe/Vilnius",                   (588675L << 32) | 2174L);
		tbl.put("Europe/Volgograd",                 (590849L << 32) | 1195L);
		tbl.put("Europe/Warsaw",                    (592044L << 32) | 2666L);
		tbl.put("Europe/Zagreb",                    (594710L << 32) | 1932L);
		tbl.put("Europe/Zaporozhye",                (596642L << 32) | 2145L);
		tbl.put("Europe/Zurich",                    (598787L << 32) | 1921L);
		tbl.put("Factory",                          (600708L << 32) | 128L );
		tbl.put("GB",                               (600836L << 32) | 3660L);
		tbl.put("GB-Eire",                          (604496L << 32) | 3660L);
		tbl.put("GMT",                              (608156L << 32) | 126L );
		tbl.put("GMT+0",                            (608282L << 32) | 126L );
		tbl.put("GMT-0",                            (608408L << 32) | 126L );
		tbl.put("GMT0",                             (608534L << 32) | 126L );
		tbl.put("Greenwich",                        (608660L << 32) | 126L );
		tbl.put("Hongkong",                         (608786L << 32) | 1215L);
		tbl.put("HST",                              (610001L << 32) | 127L );
		tbl.put("Iceland",                          (610128L << 32) | 1174L);
		tbl.put("Indian/Antananarivo",              (611302L << 32) | 263L );
		tbl.put("Indian/Chagos",                    (611565L << 32) | 211L );
		tbl.put("Indian/Christmas",                 (611776L << 32) | 177L );
		tbl.put("Indian/Cocos",                     (611953L << 32) | 186L );
		tbl.put("Indian/Comoro",                    (612139L << 32) | 263L );
		tbl.put("Indian/Kerguelen",                 (612402L << 32) | 177L );
		tbl.put("Indian/Mahe",                      (612579L << 32) | 177L );
		tbl.put("Indian/Maldives",                  (612756L << 32) | 211L );
		tbl.put("Indian/Mauritius",                 (612967L << 32) | 253L );
		tbl.put("Indian/Mayotte",                   (613220L << 32) | 263L );
		tbl.put("Indian/Reunion",                   (613483L << 32) | 177L );
		tbl.put("Iran",                             (613660L << 32) | 2594L);
		tbl.put("Israel",                           (616254L << 32) | 2300L);
		tbl.put("Jamaica",                          (618554L << 32) | 494L );
		tbl.put("Japan",                            (619048L << 32) | 321L );
		tbl.put("Kwajalein",                        (619369L << 32) | 328L );
		tbl.put("Libya",                            (619697L << 32) | 637L );
		tbl.put("MET",                              (620334L << 32) | 2106L);
		tbl.put("Mexico/BajaNorte",                 (622440L << 32) | 2354L);
		tbl.put("Mexico/BajaSur",                   (624794L << 32) | 1538L);
		tbl.put("Mexico/General",                   (626332L << 32) | 1596L);
		tbl.put("MST",                              (627928L << 32) | 126L );
		tbl.put("MST7MDT",                          (628054L << 32) | 2322L);
		tbl.put("Navajo",                           (630376L << 32) | 2456L);
		tbl.put("NZ",                               (632832L << 32) | 2449L);
		tbl.put("NZ-CHAT",                          (635281L << 32) | 2080L);
		tbl.put("Pacific/Apia",                     (637361L << 32) | 1109L);
		tbl.put("Pacific/Auckland",                 (638470L << 32) | 2473L);
		tbl.put("Pacific/Bougainville",             (640943L << 32) | 292L );
		tbl.put("Pacific/Chatham",                  (641235L << 32) | 2095L);
		tbl.put("Pacific/Chuuk",                    (643330L << 32) | 296L );
		tbl.put("Pacific/Easter",                   (643626L << 32) | 2258L);
		tbl.put("Pacific/Efate",                    (645884L << 32) | 478L );
		tbl.put("Pacific/Enderbury",                (646362L << 32) | 261L );
		tbl.put("Pacific/Fakaofo",                  (646623L << 32) | 212L );
		tbl.put("Pacific/Fiji",                     (646835L << 32) | 1089L);
		tbl.put("Pacific/Funafuti",                 (647924L << 32) | 178L );
		tbl.put("Pacific/Galapagos",                (648102L << 32) | 267L );
		tbl.put("Pacific/Gambier",                  (648369L << 32) | 191L );
		tbl.put("Pacific/Guadalcanal",              (648560L << 32) | 178L );
		tbl.put("Pacific/Guam",                     (648738L << 32) | 506L );
		tbl.put("Pacific/Honolulu",                 (649244L << 32) | 347L );
		tbl.put("Pacific/Johnston",                 (649591L << 32) | 341L );
		tbl.put("Pacific/Kiritimati",               (649932L << 32) | 262L );
		tbl.put("Pacific/Kosrae",                   (650194L << 32) | 369L );
		tbl.put("Pacific/Kwajalein",                (650563L << 32) | 337L );
		tbl.put("Pacific/Majuro",                   (650900L << 32) | 351L );
		tbl.put("Pacific/Marquesas",                (651251L << 32) | 202L );
		tbl.put("Pacific/Midway",                   (651453L << 32) | 201L );
		tbl.put("Pacific/Nauru",                    (651654L << 32) | 264L );
		tbl.put("Pacific/Niue",                     (651918L << 32) | 253L );
		tbl.put("Pacific/Norfolk",                  (652171L << 32) | 892L );
		tbl.put("Pacific/Noumea",                   (653063L << 32) | 316L );
		tbl.put("Pacific/Pago_Pago",                (653379L << 32) | 187L );
		tbl.put("Pacific/Palau",                    (653566L << 32) | 192L );
		tbl.put("Pacific/Pitcairn",                 (653758L << 32) | 214L );
		tbl.put("Pacific/Pohnpei",                  (653972L << 32) | 329L );
		tbl.put("Pacific/Ponape",                   (654301L << 32) | 315L );
		tbl.put("Pacific/Port_Moresby",             (654616L << 32) | 227L );
		tbl.put("Pacific/Rarotonga",                (654843L << 32) | 589L );
		tbl.put("Pacific/Saipan",                   (655432L << 32) | 506L );
		tbl.put("Pacific/Samoa",                    (655938L << 32) | 187L );
		tbl.put("Pacific/Tahiti",                   (656125L << 32) | 192L );
		tbl.put("Pacific/Tarawa",                   (656317L << 32) | 193L );
		tbl.put("Pacific/Tongatapu",                (656510L << 32) | 384L );
		tbl.put("Pacific/Truk",                     (656894L << 32) | 281L );
		tbl.put("Pacific/Wake",                     (657175L << 32) | 189L );
		tbl.put("Pacific/Wallis",                   (657364L << 32) | 178L );
		tbl.put("Pacific/Yap",                      (657542L << 32) | 281L );
		tbl.put("Poland",                           (657823L << 32) | 2666L);
		tbl.put("Portugal",                         (660489L << 32) | 3481L);
		tbl.put("PRC",                              (663970L << 32) | 573L );
		tbl.put("PST8PDT",                          (664543L << 32) | 2322L);
		tbl.put("ROC",                              (666865L << 32) | 773L );
		tbl.put("ROK",                              (667638L << 32) | 629L );
		tbl.put("Singapore",                        (668267L << 32) | 395L );
		tbl.put("Turkey",                           (668662L << 32) | 1959L);
		tbl.put("UCT",                              (670621L << 32) | 126L );
		tbl.put("Universal",                        (670747L << 32) | 126L );
		tbl.put("US/Alaska",                        (670873L << 32) | 2383L);
		tbl.put("US/Aleutian",                      (673256L << 32) | 2368L);
		tbl.put("US/Arizona",                       (675624L << 32) | 340L );
		tbl.put("US/Central",                       (675964L << 32) | 3588L);
		tbl.put("US/East-Indiana",                  (679552L << 32) | 1678L);
		tbl.put("US/Eastern",                       (681230L << 32) | 3548L);
		tbl.put("US/Hawaii",                        (684778L << 32) | 341L );
		tbl.put("US/Indiana-Starke",                (685119L << 32) | 2440L);
		tbl.put("US/Michigan",                      (687559L << 32) | 2242L);
		tbl.put("US/Mountain",                      (689801L << 32) | 2456L);
		tbl.put("US/Pacific",                       (692257L << 32) | 2848L);
		tbl.put("US/Samoa",                         (695105L << 32) | 187L );
		tbl.put("UTC",                              (695292L << 32) | 126L );
		tbl.put("W-SU",                             (695418L << 32) | 1547L);
		tbl.put("WET",                              (696965L << 32) | 1917L);
		tbl.put("Zulu",                             (698882L << 32) | 126L );

        table = Collections.unmodifiableMap(tbl);
    }
}

/*
typedef struct _timelib_tzinfo
{
    char    *name;
    struct {
        uint32_t ttisgmtcnt;
        uint32_t ttisstdcnt;
        uint32_t leapcnt;
        uint32_t timecnt;
        uint32_t typecnt;
        uint32_t charcnt;
    } _bit32;
    struct {
        uint64_t ttisgmtcnt;
        uint64_t ttisstdcnt;
        uint64_t leapcnt;
        uint64_t timecnt;
        uint64_t typecnt;
        uint64_t charcnt;
    } bit64;

    int64_t *trans;
    unsigned char *trans_idx;

    ttinfo  *type;
    char    *timezone_abbr;

    tlinfo  *leap_times;
    unsigned char bc;
    tlocinfo location;
} timelib_tzinfo;

struct _ttinfo {
    int32_t      offset;
    int          isdst;
    unsigned int abbr_idx;

    unsigned int isstdcnt;
    unsigned int isgmtcnt;
};

struct _tlinfo {
    int64_t  trans;
    int32_t  offset;
};

typedef struct _tlocinfo {
    char country_code[3];
    double latitude;
    double longitude;
    char *comments;
} tlocinfo;
*/