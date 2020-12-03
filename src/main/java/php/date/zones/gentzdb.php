<?php
date_default_timezone_set('Asia/Shanghai');
$created_at = date("Y-m-d H:i:s");
$version = trim(file(__DIR__ . "/version-info.txt")[0]);


$files = array_merge(
    glob("code/data/*"),
    glob("code/data/*/*"),
    glob("code/data/*/*/*")
);
usort($files, "strcasecmp");

$file = __DIR__ . "/../TzInfo.java";
file_put_contents($file, <<<JAVA
package php;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自动生成代码，请勿修改
 * @author xiaofeng
 * @version $version
 * @since $created_at
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

    /*final */static Map<String, TzInfo> tbl;
    
    static TzInfo of(String name) {
        Objects.requireNonNull(name);
        TzInfo timezoneInfo = tbl.get(name);
        if (timezoneInfo == null) {
            throw new YaTime.TimeException("不支持的时区 " + name);
        }
        return timezoneInfo;
    }

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
 
    static {
        // Map<String, TzInfo> tbl = new HashMap<>();
        // this.table = Collections.unmodifiableMap(tbl);
    }


JAVA
);
$fd = fopen($file, "a");

// 分块生成的单个方法还是太大，且文件太长了 javac 拒绝编译...
$chunks = array_chunk($files, 10);
foreach($chunks as $chunk_files) {
    fwrite($fd, "    static {\n");
    foreach($chunk_files as $fileName) {
        if (is_dir($fileName)) {
            continue;
        }
        $fileName = preg_replace( '@code/data/@', '', $fileName );
        $entry = create_entry($fileName);

        $name = $entry["name"];
        $b = $entry["bit64"];
        $args = [];
        $args[] = '"' . $name . '"';
        $args[] = $b["ttisgmtcnt"];
        $args[] = $b["ttisstdcnt"];
        $args[] = $b["leapcnt"];
        $args[] = $b["timecnt"];
        $args[] = $b["typecnt"];
        $args[] = $b["charcnt"];
        $args[] = "new long[]{" . implode("L,", $entry["trans"]) . ($entry["trans"] ? "L" : "")  . "}";
        $args[] = "new int[]{" . implode(",", $entry["trans_idx"]) . "}";
        $args[] = "new TInfo[] {" . implode(", ", array_map(fn($it) => "new TInfo(${it["offset"]}, ${it["isdst"]}, ${it["abbr_idx"]}, ${it["isstdcnt"]}, ${it["isgmtcnt"]})", $entry["type"])) . "}";
        $args[] = "new String[] { \"" . implode("\", \"", $entry["timezone_abbr"]) . "\"}";
        $args[] = "new LInfo[] {" . implode(", ", array_map(fn($it) => "new LInfo(${$it["trans"]}, ${$it["offset"]})", $entry["leap_times"])) . "}";
        $args[] = $entry["bc"];
        $loc = $entry["location"];
        $args[] = "new Loc(\"${loc["country_code"]}\", ${loc["latitude"]}, ${loc["longitude"]}, \"${loc["comments"]}\")";
        fwrite($fd, "\t\ttbl.put(\"$name\", new TzInfo(" . implode(", ", $args) . "));\n");
        echo $fileName, "\n";
    }
    fwrite($fd, "    }\n\n");
}

fwrite($fd, <<<JAVA

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
JAVA
);
fclose($fd);
echo "Done\n";





function create_entry($zone) {
	$dataf = __DIR__ . "/code/data/$zone";

	// obtain data from tz file
	$fdata = file_get_contents( $dataf, false, NULL, 20 );


	$cc = "??";
	$lat = 0.0;
	$long = 0.0;
	$desc = "";

    // process extra info
    $f = file(__DIR__ .  "/code/zone.tab");
	foreach ($f as $line) {
		$line = trim( $line );
		if ( strlen( $line ) < 5 || $line[0] == '#' ) {
			continue;
		}

		$parts = explode( "\t", $line );
		if ($parts[2] == $zone) {
			// format lang/lat
			if ( strlen( $parts[1] ) == 11 ) {
				sscanf( $parts[1], '%c%2d%2d%c%3d%2d', $xSign, $xH, $xM, $ySign, $yH, $yM );
				$xS = $yS = 0;
			} else {
				sscanf( $parts[1], '%c%2d%2d%2d%c%3d%2d%2d', $xSign, $xH, $xM, $xS, $ySign, $yH, $yM, $yS );
			}
			$lat = $xH + ( $xM / 60 ) + ( $xS / 3600 );
			$long = $yH + ( $yM / 60 ) + ( $yS / 3600 );
			$lat = $xSign == '+' ? $lat : -$lat;
			$long = $ySign == '+' ? $long : -$long;

			$cc   = $parts[0];
			$desc = isset( $parts[3] ) ? $parts[3] : '';
			break;
		}
	}

	assert(PHP_INT_SIZE > 4);

    $tzinfo = [
        "name" => $zone,
        "_bit32" => [
            "ttisgmtcnt" => 0,
            "ttisstdcnt" => 0,
            "leapcnt" => 0,
            "timecnt" => 0,
            "typecnt" => 0,
            "charcnt" => 0,
        ],
        "bit64" => [
            "ttisgmtcnt" => 0,
            "ttisstdcnt" => 0,
            "leapcnt" => 0,
            "timecnt" => 0,
            "typecnt" => 0,
            "charcnt" => 0,
        ],
        "trans" => [],
        "trans_idx" => [],
        "type" => [
            [
                "offset" => 0,
                "isdst" => 0,
                "abbr_idx" => 0,
                "isstdcnt" => 0,
                "isgmtcnt" => 0,
            ],
        ],
        "timezone_abbr" => [],
        "leap_times" => [
            [
                "trans" => 0,
                "offset" => 0,
            ],
        ],
        "bc" => intval($cc != '??' || $zone == 'UTC'),
        "location" => [
            "country_code" => $cc,
            "latitude" => (($lat + 90) * 100000) / 100000 - 90, // todo
            "longitude" => (($long + 180) * 100000) / 100000 - 180, // todo
            "comments" => $desc,
        ]
    ];

    $offset = 0;

    // read_32bit_header
    $bit32 = unpack("N6", $fdata, $offset);
    $tzinfo["_bit32"] = [
        "ttisgmtcnt" => $bit32[1],
        "ttisstdcnt" => $bit32[2],
        "leapcnt" => $bit32[3],
        "timecnt" => $bit32[4],
        "typecnt" => $bit32[5],
        "charcnt" => $bit32[6],
    ];
    $offset += 4 * 6;
    // skip_32bit_transitions
    if (($timecnt = $tzinfo["_bit32"]["timecnt"]) > 0) {
        $offset += 4 * $timecnt;
        $offset += 1 * $timecnt;
    }

    // skip_32bit_types
    /* Offset Types */
    $offset += 1 * 6 * $tzinfo["_bit32"]["typecnt"];
    /* Abbreviations */
    $offset += 1 * $tzinfo["_bit32"]["charcnt"];
    /* Leap seconds (only use in 'right/') format */
    if (($leapcnt = $tzinfo["_bit32"]["leapcnt"]) > 0) {
        $offset += 4 * $leapcnt * 2;
    }
    /* Standard/Wall Indicators (unused) */
    if (($ttisstdcnt = $tzinfo["_bit32"]["ttisstdcnt"]) > 0) {
        $offset += 1 * $ttisstdcnt;
    }
    /* UT/Local Time Indicators (unused) */
    if (($ttisgmtcnt = $tzinfo["_bit32"]["ttisgmtcnt"]) > 0) {
        $offset += 1 * $ttisgmtcnt;
    }

    if (substr_compare($fdata, "TZif2", $offset, 5) === 0) {
        $offset += 20;
    } else if (substr_compare($fdata, "TZif3", $offset, 5) === 0) {
        $offset += 20;
    } else {
        throw new RuntimeException();
    }

    // read_64bit_header
    $bit32 = unpack("N6", $fdata, $offset);
    $tzinfo["bit64"] = [
        "ttisgmtcnt" => $bit32[1],
        "ttisstdcnt" => $bit32[2],
        "leapcnt" => $bit32[3],
        "timecnt" => $bit32[4],
        "typecnt" => $bit32[5],
        "charcnt" => $bit32[6],
    ];
    $offset += 4 * 6;

    // read_64bit_transitions
    if (($timecnt = $tzinfo["bit64"]["timecnt"]) > 0) {
        $tzinfo["trans"] = unpack("J$timecnt", $fdata, $offset);
        $offset += 8 * $timecnt;
        $tzinfo["trans_idx"] = unpack("C$timecnt", $fdata, $offset);
        $offset += 1 * $timecnt;
    }

    // read_64bit_types
    $typecnt = $tzinfo["bit64"]["typecnt"];
    /* Offset Types */
    $tzinfo["type"] = [];
    for ($i = 0; $i < $typecnt; $i++) {
        $type = unpack("Noffset/Cisdst/Cabbr_idx", $fdata, $offset);
        $offset += 1 * 6;
        $tzinfo["type"][$i] = [
            "offset" => ($type["offset"] << 32) >> 32, // uint32_t => int32_t
            "isdst" => $type["isdst"],
            "abbr_idx" => $type["abbr_idx"],
            "isstdcnt" => 0,
            "isgmtcnt" => 0,
        ];
    }
    /* Abbreviations */
    $charcnt = $tzinfo["bit64"]["charcnt"];
    $tz_abbr = substr($fdata, $offset, $charcnt);
    $tzinfo["timezone_abbr"] = explode("\0", trim($tz_abbr, "\0"));
    $offset += 1 * $charcnt;
    /* Leap seconds (only use in 'right/') format */
    $tzinfo["leap_times"] = [];
    if (($leapcnt = $tzinfo["bit64"]["leapcnt"]) > 0) {
        throw new RuntimeException();
        for ($i = 0; $i < $leapcnt; $i++) {
            // tz->leap_times[i].trans = timelib_conv_int64_signed(leap_buffer[i * 3 + 1] * 4294967296 + leap_buffer[i * 3]);
			// tz->leap_times[i].offset = timelib_conv_int_signed(leap_buffer[i * 3 + 2]);
            $tzinfo["leap_times"][$i] = [
                "trans" => 0,
                "offset" => 0,
            ];
        }
        $offset += $leapcnt * (8 + 4);
    }
    /* Standard/Wall Indicators (unused) */
    if (($ttisstdcnt = $tzinfo["bit64"]["ttisstdcnt"]) > 0) {
        $chars = unpack("C$ttisstdcnt", $fdata, $offset);
        for ($i = 0; $i < $ttisstdcnt; $i++) {
            $tzinfo["type"][$i]["isstdcnt"] = $chars[$i + 1];
        }
        $offset += 1 * $ttisstdcnt;
    }
    /* UT/Local Time Indicators (unused) */
    if (($ttisgmtcnt = $tzinfo["bit64"]["ttisgmtcnt"]) > 0) {
        $chars = unpack("C$ttisgmtcnt", $fdata, $offset);
        for ($i = 0; $i < $ttisgmtcnt; $i++) {
            $tzinfo["type"][$i]["isgmtcnt"] = $chars[$i + 1];
        }
        $offset += 1 * $ttisgmtcnt;
    }

    // skip_posix_string
    $n_count = 0;
    do {
        if ($fdata[$offset] === "\n") {
            $n_count++;
        }
		$offset++;
	} while ($n_count < 2);


    return $tzinfo;
}
