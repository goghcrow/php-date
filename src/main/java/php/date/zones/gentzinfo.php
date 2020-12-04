<?php
date_default_timezone_set('Asia/Shanghai');
$created_at = date("Y-m-d H:i:s");
$version = trim(file(__DIR__ . "/version-info.txt")[0]);

$genFileName = __DIR__ . "/../TzInfo.java";
$dataFileName = __DIR__ . "/../../../../resources/php/timezonedb.dta";


file_put_contents( $dataFileName, "" ); // 清空文件

$idx_java = <<<JAVA
package php.date;

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动生成代码，请勿修改
 * @author xiaofeng
 * @version $version
 * @since $created_at
 */

JAVA;

$idx_java .= <<<'JAVA'
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
        Objects.requireNonNull(name);
        Long val = table.get(name);
        if (val == null) {
            throw new TimeException("不支持的时区 " + name);
        }
        int pos = (int) (val >>> 32);
        int sz = (int) (val & 0xffffffffL);
        return parse_tz_from_db(name, pos, sz);
    }

    final static byte[] tzdb;
    static {
        try {
            InputStream is = TzInfo.class.getClassLoader().getResourceAsStream("php/timezonedb.dta");
            tzdb = IOUtils.readFully(is, -1, false);
        } catch (IOException e) {
            throw new TimeException(e);
        }
    }

    static TzInfo parse_tz_from_db(String name, int pos, int sz) {
        return parse_tz(name, Bytes.of(ByteBuffer.wrap(tzdb, pos, sz).asReadOnlyBuffer()));
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

    static Map<String, Long> table;

    static {
        Map<String, Long> tbl = new HashMap<>();
        

JAVA;

$missing = 0;

$files = array_merge(
    glob( "code/data/*" ),
    glob("code/data/*/*" ),
    glob("code/data/*/*/*" )
);
usort($files, "strcasecmp");


$dataFileFd = fopen($dataFileName, "a");

foreach($files as $fileName) {
	if (is_dir($fileName)) {
		continue;
	}

	$l = filesize($dataFileName);
	$fileName = preg_replace('@code/data/@', '', $fileName);

	list($fdataSize, $v2Start, $v2Size) = create_entry($fileName, $dataFileFd);

	$idx_java .= sprintf("\t\ttbl.put(%-35s (%-7s << 32) | %-5s);\n", "\"{$fileName}\",", "${l}L", "${fdataSize}L");

	$missing += $v2Size;
	printf("- %s (%d, %d)\n", $fileName, $l, $missing);
}

fclose($dataFileFd);

$idx_java .= <<<'JAVA'

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
JAVA;

file_put_contents($genFileName, $idx_java);

echo "Done\n";




function create_entry(string $zone, $dataFileFd) : array {
	$dataf = __DIR__ . "/code/data/$zone";

	// obtain data from tz file
	$fdata = file_get_contents($dataf, false, NULL, 20);

	$v2 = strpos($fdata, "TZif");

	// process extra info
	$f = file(__DIR__ . "/code/zone.tab");

	$cc = "??";
	$lat = $long = 0;
	$desc = '';

	foreach ($f as $line) {
		$line = trim($line);
		if (strlen($line) < 5 || $line[0] == '#') {
			continue;
		}

		$parts = explode("\t", $line);
		if ($parts[2] == $zone) {
			// format lang/lat
			if (strlen($parts[1]) == 11) {
				sscanf($parts[1], '%c%2d%2d%c%3d%2d', $xSign, $xH, $xM, $ySign, $yH, $yM);
				$xS = $yS = 0;
			} else {
				sscanf($parts[1], '%c%2d%2d%2d%c%3d%2d%2d', $xSign, $xH, $xM, $xS, $ySign, $yH, $yM, $yS);
			}
			$lat = $xH + ($xM / 60) + ($xS / 3600);
			$long = $yH + ($yM / 60) + ($yS / 3600);
			$lat = $xSign == '+' ? $lat : -$lat;
			$long = $ySign == '+' ? $long : -$long;

			$cc = $parts[0];
			$desc = isset($parts[3]) ? $parts[3] : '';

			break;
		}
	}
	//  printf( '{ "%2s", %d, %d, "%s" },' . "\n",
	//	$cc, $lat * 100000, $long * 100000, addslashes( $desc ) );
	$data = pack('a4ca2a13a*NNNa*', "PHP2", $cc != '??' || $zone == 'UTC', $cc, '', $fdata, ($lat + 90) * 100000, ($long + 180) * 100000, strlen($desc), $desc);
	fwrite($dataFileFd, $data);
	fflush($dataFileFd);

	// list( $fdataSize, $v2Start, $v2Size )
	return [strlen($data), $v2 + 20, strlen($fdata) - $v2];
}
