## 移植 PHP 日期时间扩展库

接受一个包含美国英语日期格式的字符串并尝试将其解析为 Unix 时间戳（自 January 1 1970 00:00:00 UTC 起的秒数），
其值相对于 now 参数给出的时间，如果没有提供 now 参数则用系统当前时间，
没有提供 ZoneId 默认使用 ZoneId.systemDefault()。

```java
public static Time parse(@NotNull String timeStr);
public static Time parse(@NotNull String timeStr, long ts);
public static Time parse(@NotNull String timeStr, @NotNull ZoneId zoneId, long ts);
```

移植自

https://github.com/php/php-src/blob/master/ext/date/lib/parse_date.re
https://github.com/derickr/timelib

## 文档

https://www.php.net/manual/zh/function.strtotime.php
https://www.php.net/manual/zh/datetime.formats.php
https://www.php.net/manual/zh/datetime.formats.time.php
https://www.php.net/manual/zh/datetime.formats.date.php
https://www.php.net/manual/zh/datetime.formats.compound.php
https://www.php.net/manual/zh/datetime.formats.relative.php



## 支持解析的格式 - regex

支持以下所有格式组合使用，注意相同元素不能重复设置，比如时区不能设置两次

```java
reAgo       = "^ago";

reHour24    = "(2[0-4]|[01]?[0-9])";
reHour24Lz  = "([01][0-9]|2[0-4])";
reHour12    = "(1[0-2]|0?[1-9])";
reMinute    = "([0-5]?[0-9])";
reMinuteLz  = "([0-5][0-9])";
reSecond    = "([0-5]?[0-9]|60)";
reSecondLz  = "([0-5][0-9]|60)";
reFrac      = "(?:\\.([0-9]+))";

reMeridian      = "(?:([ap])\\.?m\\.?(?:[ \\t]|$))";

reYear          = "([0-9]{1,4})";
reYear2         = "([0-9]{2})";
reYear4         = "([0-9]{4})";
reYear4WithSign = "([+-]?[0-9]{4})";

reMonth         = "(1[0-2]|0?[0-9])";
reMonthLz       = "(0[0-9]|1[0-2])";

reMonthFull     = "january|february|march|april|may|june|july|august|september|october|november|december";
reMonthAbbr     = "jan|feb|mar|apr|may|jun|jul|aug|sept?|oct|nov|dec";
reMonthRoman    = "i{1,3}|i[vx]|vi{0,3}|xi{0,2}";
reMonthText     = '(' + reMonthFull + '|' + reMonthAbbr + '|' + reMonthRoman + ')';

reDay   = "(?:([0-2]?[0-9]|3[01])(?:st|nd|rd|th)?)";
reDayLz = "(0[0-9]|[1-2][0-9]|3[01])";

reDayFull = "sunday|monday|tuesday|wednesday|thursday|friday|saturday";
reDayAbbr = "sun|mon|tue|wed|thu|fri|sat";
reDayText = reDayFull + '|' + reDayAbbr + '|' + "weekdays?";

reDayOfYear = "(00[1-9]|0[1-9][0-9]|[12][0-9][0-9]|3[0-5][0-9]|36[0-6])";
reWeekOfYear = "(0[1-9]|[1-4][0-9]|5[0-3])";

reTzCorrection = "((?:GMT)?([+-])" + reHour24 + ":?" + reMinute + "?)";
reTzAbbr = "\\(?([a-zA-Z]{1,6})\\)?";
reTz = "[A-Z][a-z]+([_/-][A-Za-z_]+)+|" + reTzAbbr;


/* Time formats */
reTimeTiny12  = '^' + reHour12                                           + reSpaceOpt + reMeridian;
reTimeShort12 = '^' + reHour12 + "[:.]" + reMinuteLz + reSpaceOpt + reMeridian;
reTimeLong12  = '^' + reHour12 + "[:.]" + reMinute + "[:.]" + reSecondLz + reSpaceOpt + reMeridian;

reTimeShort24 = "^t?" + reHour24 + "[:.]" + reMinute;
reTimeLong24  = "^t?" + reHour24 + "[:.]" + reMinute + "[:.]" + reSecond;
reISO8601Long = "^t?" + reHour24 + "[:.]" + reMinute + "[:.]" + reSecond + reFrac;

reTzText = '(' + reTzCorrection + '|' + reTz + ')';

reISO8601NormTz = "^t?" + reHour24 + "[:.]" + reMinute + "[:.]" + reSecondLz + reSpaceOpt + reTzText;

/* gnu */
reGNUNoColon = "^t?" + reHour24Lz + reMinuteLz;
reISO8601NoColon = "^t?" + reHour24Lz + reMinuteLz + reSecondLz;

/* Date formats */
reAmericanShort     = '^' + reMonth + '/' + reDay;
reAmerican          = '^' + reMonth + '/' + reDay + '/' + reYear;
reISO8601DateSlash  = '^' + reYear4 + '/' + reMonthLz + '/' + reDayLz + "/?";
reDateSlash         = '^' + reYear4 + '/' + reMonth + '/' + reDay;
reISO8601Date4      = '^' + reYear4WithSign + '-' + reMonthLz + '-' + reDayLz;
reISO8601Date2      = '^' + reYear2 + '-' + reMonthLz + '-' + reDayLz;
reGNUDateShorter    = '^' + reYear4 + '-' + reMonth;
reGNUDateShort      = '^' + reYear + '-' + reMonth + '-' + reDay;
rePointedDate4      = '^' + reDay + "[.\\t-]" + reMonth + "[.-]" + reYear4;
rePointedDate2      = '^' + reDay + "[.\\t]" +  reMonth + "\\." + reYear2;
reDateFull          = '^' + reDay + "[ \\t.-]*" + reMonthText + "[ \\t.-]*" + reYear;
reDateNoDay         = '^' + reMonthText + "[ .\\t-]*" + reYear4;
reDateNoDayRev      = '^' + reYear4 + "[ .\\t-]*" + reMonthText;
reDateTextual       = '^' + reMonthText + "[ .\\t-]*" + reDay + "[,.stndrh\\t ]+" + reYear;
reDateNoYear        = '^' + reMonthText + "[ .\\t-]*" + reDay + "[,.stndrh\\t ]*";
reDateNoYearRev     = '^' + reDay + "[ .\\t-]*" + reMonthText;
reDateNoColon       = '^' + reYear4 + reMonthLz + reDayLz;

/* Special formats */
// 参见 https://www.php.net/manual/en/datetime.formats.compound.php
// 木有遵守这个：The "T" in the SOAP, XMRPC and WDDX formats is case-sensitive, you can only use the upper case "T".
reSoap              = '^' + reYear4 + '-' + reMonthLz + '-' + reDayLz + 'T'    + reHour24Lz + ':' + reMinuteLz + ':' + reSecondLz + reFrac + reTzCorrection + '?';
reXML_RPC           = '^' + reYear4       + reMonthLz + reDayLz + 'T'    + reHour24    + ':' + reMinuteLz + ':' + reSecondLz;
reXML_RPCNoColon    = '^' + reYear4       + reMonthLz + reDayLz + "[Tt]" + reHour24          + reMinuteLz + reSecondLz;
reWDDX              = '^' + reYear4 + '-' + reMonth   + '-' + reDay   + 'T'    + reHour24    + ':' + reMinute   + ':' + reSecond;
reEXIF              = '^' + reYear4 + ':' + reMonthLz + ':' + reDayLz + ' '    + reHour24Lz + ':' + reMinuteLz + ':' + reSecondLz;

rePgYearDotDay      = '^' + reYear4 + "\\.?" + reDayOfYear;
rePgTextShort       = "^(" + reMonthAbbr + ")-" + reDayLz + '-' + reYear;
rePgTextReverse     = '^' + "(\\d{3,4}|[4-9]\\d|3[2-9])"/*reYear*/ + "-(" + reMonthAbbr + ")-" + reDayLz;
reMssqlTime         = '^' + reHour12 + ":" + reMinuteLz + ":" + reSecondLz + "[:.]([0-9]+)" + reMeridian;
reISOWeekday        = '^' + reYear4 + "-?W" + reWeekOfYear + "-?([0-7])";
reISOWeek           = '^' + reYear4 + "-?W" + reWeekOfYear;

reFirstOrLastDay    = "^(first|last) day of";
reBackOrFrontOf    = "^(back|front) of " + reHour24 + reSpaceOpt + reMeridian + '?';
reYesterday        = "^yesterday";
reNow              = "^now";
reNoon             = "^noon";
reMidnightOrToday  = "^(midnight|today)";
reTomorrow         = "^tomorrow";

/* Common Log Format: 10/Oct/2000:13:55:36 -0700 */
reCLF               = '^' + reDay + "/(" + reMonthAbbr + ")/" + reYear4 + ':' + reHour24Lz + ':' + reMinuteLz + ':' + reSecondLz + reSpace + reTzCorrection;

/* Timestamp format: @1126396800 */
reTimestamp        = "^@(-?\\d+)";
reTimestampMs      = "^@(-?\\d+)\\.(\\d{0,6})"; // timestamp microsec

/* To fix some ambiguities */
reDateShortWithTimeShort12  = '^' + reDateNoYear + reTimeShort12.substring(1);
reDateShortWithTimeLong12   = '^' + reDateNoYear + reTimeLong12.substring(1);
reDateShortWithTimeShort    = '^' + reDateNoYear + reTimeShort24.substring(1);
reDateShortWithTimeLong     = '^' + reDateNoYear + reTimeLong24.substring(1);
reDateShortWithTimeLongTz   = '^' + reDateNoYear + reISO8601NormTz.substring(1);

/* Relative regexps */
reRelTextNumber = "first|second|third|fourth|fifth|sixth|seventh|eighth?|ninth|tenth|eleventh|twelfth";
reRelTextText   = "next|last|previous|this";
reRelTextUnit   = "(?:msec|millisecond|µsec|microsecond|usec|second|sec|minute|min|hour|day|fortnight|forthnight|month|year)s?|(?:ms)|(?:µs)|weeks|" + reDayText;

reRelNumber         = "([+-]*)[ \\t]*([0-9]{1,13})";
reRelative          = '^' + reRelNumber + reSpaceOpt + '(' + reRelTextUnit + "|week)";
reRelativeText      = "^(" + reRelTextNumber + '|' + reRelTextText + ')' + reSpace + '(' + reRelTextUnit + ')';
reRelativeTextWeek  = "^(" + reRelTextText + ')' + reSpace + "(week)";

reWeekdayOf = "^(" + reRelTextNumber + '|' + reRelTextText + ')' + reSpace + '(' + reDayFull + '|' + reDayAbbr + ')' + reSpace + "of";
```

## Demo

```
now
yesterday
today
tomorrow
noon
midnight

yesterday 08:15pm
yesterday noon
yesterday midnight
tomorrow 18:00
tomorrow moon

+1 week 2 days 4 hours 2 seconds

saturday this week

next year
next month

last day
last wed

this week
next week
last week
previous week

monday
mon
tuesday
tue
wednesday
wed
thursday
thu
friday
fri
saturday
sat
sunday
sun

first day
first day next month
first day of next month
last day next month
last day of next month
last day of april

third Monday December 2020
second Friday Nov 2022
+3 week Thursday Nov 2020
last wednesday of march 2020

2020W30

2020W101T05:00+0

10/22/1990
10/22
01/01

Sun 2020-01-01
Mon 2020-01-02

19970523091528
20001231185859
20800410101010


Fri 2020-01-06

2020-06-25 14:18:48.543728 America/New_York

2020-10-22 13:00:00 Asia/Shanghai
2022-01-01 13:00:00 UTC
2020-01-01 00:00:00 Europe/Rome

2020-11-26T18:51:44+01:00
Thursday, 26-Nov-2020 18:51:44 CET
2020-11-26T18:51:44+0100
Thu, 26 Nov 20 18:51:44 +0100
Thursday, 26-Nov-20 18:51:44 CET
Thu, 26 Nov 2020 18:51:44 +0100

May 18th 2020 5:05pm
2005-8-12
Sat 26th Nov 2020 18:18
26th Nov
Dec. 4th, 2020
December 4th, 2020
Sun, 13 Nov 2020 22:56:10 -0800 (PST)
May 18th 5:05pm
```

感恩节
```java
static String date(String fmt, long ts) {
    return DateTimeFormatter.ofPattern(fmt).format(LocalDateTime.ofEpochSecond(ts, 0, OffsetDateTime.now().getOffset()));
}

// 今年感恩节
System.out.println(date("yyyy-MM-dd", Time.parse("fourth thursday of november").epochSecond())); // 2020-11-26

// 2019感恩节
System.out.println(date("yyyy-MM-dd", Time.parse("fourth thursday of november 2019").epochSecond())); // 2019-11-28

// 2018今年感恩节
System.out.println(date("yyyy-MM-dd", Time.parse("fourth thursday of november 2018").epochSecond())); // 2018-11-22

```

每月最后一天
```java
String start = "2016-05";
long now = Instant.now().getEpochSecond();
long ts = Time.parse(start).epochSecond();
while (true) {
    String month = date("yyyy-MM", ts);
    String last_day_of_month = date("yyyyMMdd", Time.parse("last day of " + month).epochSecond());
    System.out.println(last_day_of_month);
    ts = Time.parse("+1 month", ts).epochSecond();
    if (ts > now) {
        break;
    }
}
```

"first day of" 和 "last day of"
```java
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
```

## 已知问题

- 没有百分之百兼容，做了一些改进，见代码注释
    - 时区 offset 表示省略分钟时小时不符合 24 小时格式, 按分钟处理
- php没有遵循ISO8601，实际使用的是毫秒级(timeval)的Posix Time Epoch(即Unix TimeStamp), 而ISO8601时间戳精度是 ms
    - Unix TimeStamp 与 UTC 基本一致
- 单测依赖本地 php 环境

## Roadmap

- 实现其他 php 日期函数 e.g. date()
- 继续移植 其他功能 e.g. interval
- php没有遵循ISO8601, 时间戳精确到 ms, 调整到 ms 精度 到 ns
