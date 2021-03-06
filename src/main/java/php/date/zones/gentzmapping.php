<?php
date_default_timezone_set('Asia/Shanghai');
$created_at = date("Y-m-d H:i:s");
$version = trim(file(__DIR__ . "/version-info.txt")[0]);

$zones = timezone_identifiers_list( DateTimeZone::ALL_WITH_BC );
$priorityA = array(
    'acst'   => 'America/Porto_Acre',
    'act'    => 'America/Porto_Acre',
    'acdt'   => 'Australia/Adelaide',
    'acst'   => 'Australia/Adelaide',
    'addt'   => 'America/Goose_Bay',
    'adt'    => 'America/Halifax',
    'aedt'   => 'Australia/Melbourne',
    'aest'   => 'Australia/Melbourne',
    'aft'    => 'Asia/Kabul',
    'ahdt'   => 'America/Anchorage',
    'ahst'   => 'America/Anchorage',
    'akdt'   => 'America/Anchorage',
    'akst'   => 'America/Anchorage',
    'aktst'  => 'Asia/Aqtobe',
    'aktt'   => 'Asia/Aqtobe',
    'almst'  => 'Asia/Almaty',
    'almt'   => 'Asia/Almaty',
    'amst'   => 'Asia/Yerevan',
    'amt'    => 'Asia/Yerevan',
    'anast'  => 'Asia/Anadyr',
    'anat'   => 'Asia/Anadyr',
    'ant'    => 'America/Curacao',
    'apt'    => 'America/Halifax',
    'aqtst'  => 'Asia/Aqtau',
    'aqtt'   => 'Asia/Aqtau',
    'arst'   => 'America/Buenos_Aires',
    'art'    => 'America/Buenos_Aires',
    'ashst'  => 'Asia/Ashkhabad',
    'asht'   => 'Asia/Ashkhabad',
    'ast'    => 'Asia/Riyadh',
    'awt'    => 'America/Halifax',
    'awdt'   => 'Australia/Perth',
    'awst'   => 'Australia/Perth',
    'azomt'  => 'Atlantic/Azores',
    'azost'  => 'Atlantic/Azores',
    'azot'   => 'Atlantic/Azores',
    'azst'   => 'Asia/Baku',
    'azt'    => 'Asia/Baku',
    'bakst'  => 'Asia/Baku',
    'bakt'   => 'Asia/Baku',
    'bdst'   => 'Europe/London',
    'bdt'    => 'America/Adak',
    'beat'   => 'Africa/Mogadishu',
    'beaut'  => 'Africa/Nairobi',
    'bmt'    => 'America/Barbados',
    'bnt'    => 'Asia/Brunei',
    'bortst'  => 'Asia/Kuching',
    'bort'   => 'Asia/Kuching',
    'bost'   => 'America/La_Paz',
    'bot'    => 'America/La_Paz',
    'brst'   => 'America/Sao_Paulo',
    'brt'    => 'America/Sao_Paulo',
    'bst'    => 'Europe/London',
    'btt'    => 'Asia/Thimbu',
    'burt'   => 'Asia/Kolkata',
    'cadt'   => 'Australia/Adelaide',
    'cant'   => 'Atlantic/Canary',
    'cast'   => 'Australia/Adelaide',
    'cat'    => 'America/Anchorage',
    'cawt'   => 'America/Anchorage',
    'cddt'   => 'America/Rankin_Inlet',
    'cdt'    => 'America/Chicago',
    'cemt'   => 'Europe/Berlin',
    'cest'   => 'Europe/Berlin',
    'cet'    => 'Europe/Berlin',
    'cgst'   => 'America/Scoresbysund',
    'cgt'    => 'America/Scoresbysund',
    'chadt'  => 'Pacific/Chatham',
    'chast'  => 'Pacific/Chatham',
    'chat'   => 'Asia/Harbin',
    'chdt'   => 'America/Belize',
    'chost'  => 'Asia/Choibalsan',
    'chot'   => 'Asia/Choibalsan',
    'cit'    => 'Asia/Dili',
    'cjt'    => 'Asia/Sakhalin',
    'ckhst'  => 'Pacific/Rarotonga',
    'ckt'    => 'Pacific/Rarotonga',
    'clst'   => 'America/Santiago',
    'clt'    => 'America/Santiago',
    'cost'   => 'America/Bogota',
    'cot'    => 'America/Bogota',
    'cpt'    => 'America/Chicago',
    'cst'    => 'America/Chicago',
    'cvst'   => 'Atlantic/Cape_Verde',
    'cvt'    => 'Atlantic/Cape_Verde',
    'cwt'    => 'America/Chicago',
    'chst'   => 'Pacific/Guam',
    'dact'   => 'Asia/Dacca',
    'davt'   => 'Antarctica/Davis',
    'ddut'   => 'Antarctica/DumontDUrville',
    'dusst'  => 'Asia/Dushanbe',
    'dust'   => 'Asia/Dushanbe',
    'easst'  => 'Chile/EasterIsland',
    'east'   => 'Chile/EasterIsland',
    'eat'    => 'Africa/Khartoum',
    'ect'    => 'America/Guayaquil',
    'eddt'   => 'America/Iqaluit',
    'edt'    => 'America/New_York',
    'eest'   => 'Europe/Helsinki',
    'eet'    => 'Europe/Helsinki',
    'egst'   => 'America/Scoresbysund',
    'egt'    => 'America/Scoresbysund',
    'ehdt'   => 'America/Santo_Domingo',
    'eit'    => 'Asia/Jayapura',
    'ept'    => 'America/New_York',
    'est'    => 'America/New_York',
    'ewt'    => 'America/New_York',
    'fjst'   => 'Pacific/Fiji',
    'fjt'    => 'Pacific/Fiji',
    'fkst'   => 'Atlantic/Stanley',
    'fkt'    => 'Atlantic/Stanley',
    'fnst'   => 'America/Noronha',
    'fnt'    => 'America/Noronha',
    'fort'   => 'Asia/Aqtau',
    'frust'  => 'Asia/Bishkek',
    'frut'   => 'Asia/Bishkek',
    'galt'   => 'Pacific/Galapagos',
    'gamt'   => 'Pacific/Gambier',
    'gbgt'   => 'America/Guyana',
    'gest'   => 'Asia/Tbilisi',
    'get'    => 'Asia/Tbilisi',
    'gft'    => 'America/Cayenne',
    'ghst'   => 'Africa/Accra',
    'gst'    => 'Asia/Dubai',
    'gyt'    => 'America/Guyana',
    'hadt'   => 'America/Adak',
    'hast'   => 'America/Adak',
    'hdt'    => 'Pacific/Honolulu',
    'hkst'   => 'Asia/Hong_Kong',
    'hkt'    => 'Asia/Hong_Kong',
    'hovst'  => 'Asia/Hovd',
    'hovt'   => 'Asia/Hovd',
    'hpt'    => 'Pacific/Honolulu',
    'hst'    => 'Pacific/Honolulu',
    'hwt'    => 'Pacific/Honolulu',
    'ict'    => 'Asia/Bangkok',
    'iddt'   => 'Asia/Jerusalem',
    'idt'    => 'Asia/Jerusalem',
    'ihst'   => 'Asia/Colombo',
    'iot'    => 'Indian/Chagos',
    'irdt'   => 'Asia/Tehran',
    'irkst'  => 'Asia/Irkutsk',
    'irkt'   => 'Asia/Irkutsk',
    'irst'   => 'Asia/Tehran',
    'isst'   => 'Atlantic/Reykjavik',
    'ist'    => 'Asia/Jerusalem',
    'javt'   => 'Asia/Jakarta',
    'jdt'    => 'Asia/Tokyo',
    'jst'    => 'Asia/Tokyo',
    'kart'   => 'Asia/Karachi',
    'kast'   => 'Asia/Kashgar',
    'kdt'    => 'Asia/Seoul',
    'kgst'   => 'Asia/Bishkek',
    'kgt'    => 'Asia/Bishkek',
    'kizst'  => 'Asia/Qyzylorda',
    'kizt'   => 'Asia/Qyzylorda',
    'kmt'    => 'Europe/Vilnius',
    'kost'   => 'Pacific/Kosrae',
    'krast'  => 'Asia/Krasnoyarsk',
    'krat'   => 'Asia/Krasnoyarsk',
    'kst'    => 'Asia/Seoul',
    'kuyst'  => 'Europe/Samara',
    'kuyt'   => 'Europe/Samara',
    'kwat'   => 'Kwajalein',
    'kwat'   => 'Pacific/Kwajalein',
    'lhst'   => 'Australia/Lord_Howe',
    'lint'   => 'Pacific/Kiritimati',
    'lkt'    => 'Asia/Colombo',
    'lont'   => 'Asia/Chongqing',
    'lrt'    => 'Africa/Monrovia',
    'lst'    => 'Europe/Riga',
    'madmt'  => 'Atlantic/Madeira',
    'madst'  => 'Atlantic/Madeira',
    'madt'   => 'Atlantic/Madeira',
    'magst'  => 'Asia/Magadan',
    'magt'   => 'Asia/Magadan',
    'malst'  => 'Asia/Singapore',
    'malt'   => 'Asia/Singapore',
    'mart'   => 'Pacific/Marquesas',
    'mawt'   => 'Antarctica/Mawson',
    'mddt'   => 'America/Cambridge_Bay',
    'mdst'   => 'Europe/Moscow',
    'mdt'    => 'America/Denver',
    'mht'    => 'Pacific/Kwajalein',
    'mmt'    => 'Europe/Moscow',
    'most'   => 'Asia/Macao',
    'mot'    => 'Asia/Macao',
    'mpt'    => 'America/Denver',
    'msd'    => 'Europe/Moscow',
    'msk'    => 'Europe/Moscow',
    'mst'    => 'America/Denver',
    'mut'    => 'Indian/Mauritius',
    'mvt'    => 'Indian/Maldives',
    'mwt'    => 'America/Denver',
    'myt'    => 'Asia/Kuala_Lumpur',
    'ncst'   => 'Pacific/Noumea',
    'nct'    => 'Pacific/Noumea',
    'nddt'   => 'America/St_Johns',
    'ndt'    => 'America/St_Johns',
    'negt'   => 'America/Paramaribo',
    'nest'   => 'Europe/Amsterdam',
    'net'    => 'Europe/Amsterdam',
    'nft'    => 'Pacific/Norfolk',
    'novst'  => 'Asia/Novosibirsk',
    'novt'   => 'Asia/Novosibirsk',
    'npt'    => 'America/St_Johns',
    'nrt'    => 'Pacific/Nauru',
    'nst'    => 'America/St_Johns',
    'nut'    => 'Pacific/Niue',
    'nwt'    => 'America/St_Johns',
    'nzdt'   => 'Pacific/Auckland',
    'nzmt'   => 'Pacific/Auckland',
    'nzst'   => 'Pacific/Auckland',
    'omsst'  => 'Asia/Omsk',
    'omst'   => 'Asia/Omsk',
    'orast'  => 'Asia/Oral',
    'orat'   => 'Asia/Oral',
    'pddt'   => 'America/Inuvik',
    'pdt'    => 'America/Los_Angeles',
    'pest'   => 'America/Lima',
    'petst'  => 'Asia/Kamchatka',
    'pett'   => 'Asia/Kamchatka',
    'pet'    => 'America/Lima',
    'phot'   => 'Pacific/Enderbury',
    'phst'   => 'Asia/Manila',
    'pht'    => 'Asia/Manila',
    'pkst'   => 'Asia/Karachi',
    'pkt'    => 'Asia/Karachi',
    'pmdt'   => 'America/Miquelon',
    'pmst'   => 'America/Miquelon',
    'pmt'    => 'America/Paramaribo',
    'ppt'    => 'America/Los_Angeles',
    'pst'    => 'America/Los_Angeles',
    'pwt'    => 'America/Los_Angeles',
    'pyst'   => 'America/Asuncion',
    'pyt'    => 'America/Asuncion',
    'qyzst'  => 'Asia/Qyzylorda',
    'qyzt'   => 'Asia/Qyzylorda',
    'ret'    => 'Indian/Reunion',
    'rmt'    => 'Europe/Riga',
    'rott'   => 'Antarctica/Rothera',
    'sakst'  => 'Asia/Sakhalin',
    'sakt'   => 'Asia/Sakhalin',
    'samst'  => 'Asia/Samarkand',
    'samt'   => 'Asia/Samarkand',
    'sast'   => 'Africa/Johannesburg',
    'sbt'    => 'Pacific/Guadalcanal',
    'sct'    => 'Indian/Mahe',
    'sgt'    => 'Asia/Singapore',
    'shest'  => 'Asia/Aqtau',
    'shet'   => 'Asia/Aqtau',
    'slst'   => 'Africa/Freetown',
    'smt'    => 'Asia/Saigon',
    'srt'    => 'America/Paramaribo',
    'sst'    => 'Pacific/Samoa',
    'svest'  => 'Asia/Yekaterinburg',
    'svet'   => 'Asia/Yekaterinburg',
    'syot'   => 'Antarctica/Syowa',
    'taht'   => 'Pacific/Tahiti',
    'tasst'  => 'Asia/Samarkand',
    'tast'   => 'Asia/Samarkand',
    'tbist'  => 'Asia/Tbilisi',
    'tbit'   => 'Asia/Tbilisi',
    'tft'    => 'Indian/Kerguelen',
    'tjt'    => 'Asia/Dushanbe',
    'tlt'    => 'Asia/Dili',
    'tlt'    => 'Asia/Dili',
    'tmt'    => 'Asia/Tehran',
    'tost'   => 'Pacific/Tongatapu',
    'tot'    => 'Pacific/Tongatapu',
    'trst'   => 'Europe/Istanbul',
    'trt'    => 'Europe/Istanbul',
    'ulast'  => 'Asia/Ulaanbaatar',
    'ulat'   => 'Asia/Ulaanbaatar',
    'urast'  => 'Asia/Oral',
    'urat'   => 'Asia/Oral',
    'urut'   => 'Asia/Urumqi',
    'uyhst'  => 'America/Montevideo',
    'uyst'   => 'America/Montevideo',
    'uyt'    => 'America/Montevideo',
    'uzst'   => 'Asia/Samarkand',
    'uzt'    => 'Asia/Samarkand',
    'vet'    => 'America/Caracas',
    'vlasst'  => 'Asia/Vladivostok',
    'vlast'  => 'Asia/Vladivostok',
    'vlat'   => 'Asia/Vladivostok',
    'vost'   => 'Antarctica/Vostok',
    'vust'   => 'Pacific/Efate',
    'vut'    => 'Pacific/Efate',
    'warst'  => 'America/Mendoza',
    'wart'   => 'America/Mendoza',
    'wast'   => 'Africa/Windhoek',
    'wat'    => 'Africa/Dakar',
    'wemt'   => 'Europe/Lisbon',
    'west'   => 'Europe/Paris',
    'wet'    => 'Europe/Paris',
    'wgst'   => 'America/Godthab',
    'wgt'    => 'America/Godthab',
    'wit'    => 'Asia/Jakarta',
    'wst'    => 'Australia/Perth',
    'yakst'  => 'Asia/Yakutsk',
    'yakt'   => 'Asia/Yakutsk',
    'yddt'   => 'America/Dawson',
    'ydt'    => 'America/Dawson',
    'yekst'  => 'Asia/Yekaterinburg',
    'yekt'   => 'Asia/Yekaterinburg',
    'yerst'  => 'Asia/Yerevan',
    'yert'   => 'Asia/Yerevan',
    'ypt'    => 'America/Dawson',
    'yst'    => 'America/Anchorage',
    'ywt'    => 'America/Dawson',
    'zzz'    => 'Antarctica/Davis',
);
$priorityB = array(
    'acst'   => array(1, -14400, 'America/Porto_Acre'),
    'act'    => array(0, -18000, 'America/Porto_Acre'),
    'addt'   => array(1,  -7200, 'America/Goose_Bay'),
    'adt'    => array(1, -10800, 'America/Halifax'),
    'aft'    => array(0,  16200, 'Asia/Kabul'),
    'ahdt'   => array(1, -32400, 'America/Anchorage'),
    'ahst'   => array(0, -36000, 'America/Anchorage'),
    'akdt'   => array(1, -28800, 'America/Anchorage'),
    'akst'   => array(0, -32400, 'America/Anchorage'),
    'aktst'  => array(1,  21600, 'Asia/Aqtobe'),
    'aktt'   => array(0,  14400, 'Asia/Aqtobe'),
    'aktt'   => array(0,  18000, 'Asia/Aqtobe'),
    'aktt'   => array(0,  21600, 'Asia/Aqtobe'),
    'almst'  => array(1,  25200, 'Asia/Almaty'),
    'almt'   => array(0,  18000, 'Asia/Almaty'),
    'almt'   => array(0,  21600, 'Asia/Almaty'),
    'amst'   => array(1, -10800, 'America/Manaus'),
    'amst'   => array(1,  14400, 'Asia/Yerevan'),
    'amst'   => array(1,  18000, 'Asia/Yerevan'),
    'amt'    => array(0, -14400, 'America/Manaus'),
    'amt'    => array(0,  10800, 'Asia/Yerevan'),
    'amt'    => array(0,   1172, 'Europe/Amsterdam'),
    'amt'    => array(0,  14400, 'Asia/Yerevan'),
    'anast'  => array(1,  43200, 'Asia/Anadyr'),
    'anast'  => array(1,  46800, 'Asia/Anadyr'),
    'anast'  => array(1,  50400, 'Asia/Anadyr'),
    'anat'   => array(0,  39600, 'Asia/Anadyr'),
    'anat'   => array(0,  43200, 'Asia/Anadyr'),
    'anat'   => array(0,  46800, 'Asia/Anadyr'),
    'ant'    => array(0, -16200, 'America/Curacao'),
    'apt'    => array(1, -10800, 'America/Halifax'),
    'aqtst'  => array(1,  18000, 'Asia/Aqtau'),
    'aqtst'  => array(1,  21600, 'Asia/Aqtau'),
    'aqtt'   => array(0,  14400, 'Asia/Aqtau'),
    'aqtt'   => array(0,  18000, 'Asia/Aqtau'),
    'arst'   => array(1,  -7200, 'America/Buenos_Aires'),
    'art'    => array(0, -10800, 'America/Buenos_Aires'),
    'ashst'  => array(1,  18000, 'Asia/Ashkhabad'),
    'ashst'  => array(1,  21600, 'Asia/Ashkhabad'),
    'asht'   => array(0,  14400, 'Asia/Ashkhabad'),
    'asht'   => array(0,  18000, 'Asia/Ashkhabad'),
    'ast'    => array(0, -14400, 'America/Curacao'),
    'ast'    => array(0,  10800, 'Asia/Riyadh'),
    'awt'    => array(1, -10800, 'America/Halifax'),
    'azomt'  => array(1,      0, 'Atlantic/Azores'),
    'azost'  => array(1,      0, 'Atlantic/Azores'),
    'azot'   => array(0,  -3600, 'Atlantic/Azores'),
    'azst'   => array(1,  14400, 'Asia/Baku'),
    'azst'   => array(1,  18000, 'Asia/Baku'),
    'azt'    => array(0,  10800, 'Asia/Baku'),
    'azt'    => array(0,  14400, 'Asia/Baku'),
    'bakst'  => array(1,  14400, 'Asia/Baku'),
    'bakst'  => array(1,  18000, 'Asia/Baku'),
    'bakt'   => array(0,  10800, 'Asia/Baku'),
    'bakt'   => array(0,  14400, 'Asia/Baku'),
    'bdst'   => array(1,   7200, 'Europe/London'),
    'bdt'    => array(1, -36000, 'America/Adak'),
    'bdt'    => array(0,  21600, 'Asia/Dacca'),
    'beat'   => array(0,   9000, 'Africa/Mogadishu'),
    'beaut'  => array(0,   9885, 'Africa/Nairobi'),
    'bmt'    => array(0, -14308, 'America/Barbados'),
    'bmt'    => array(0,  -3996, 'Africa/Banjul'),
    'bmt'    => array(0,   6264, 'Europe/Tiraspol'),
    'bnt'    => array(0,  27000, 'Asia/Brunei'),
    'bnt'    => array(0,  28800, 'Asia/Brunei'),
    'bortst'  => array(1,  30000, 'Asia/Kuching'),
    'bort'   => array(0,  27000, 'Asia/Kuching'),
    'bort'   => array(0,  28800, 'Asia/Kuching'),
    'bost'   => array(1, -12756, 'America/La_Paz'),
    'bot'    => array(0, -14400, 'America/La_Paz'),
    'brst'   => array(1,  -7200, 'America/Sao_Paulo'),
    'brt'    => array(0, -10800, 'America/Sao_Paulo'),
    'bst'    => array(0, -39600, 'America/Adak'),
    'bst'    => array(0,   3600, 'Europe/London'),
    'bst'    => array(1,   3600, 'Europe/London'),
    'btt'    => array(0,  21600, 'Asia/Thimbu'),
    'burt'   => array(0,  23400, 'Asia/Kolkata'),
    'cant'   => array(0,  -3600, 'Atlantic/Canary'),
    'cast'   => array(0,  34200, 'Australia/Adelaide'),
    'cat'    => array(0, -36000, 'America/Anchorage'),
    'cat'    => array(0,   7200, 'Africa/Khartoum'),
    'cawt'   => array(1, -32400, 'America/Anchorage'),
    'cddt'   => array(1, -14400, 'America/Rankin_Inlet'),
    'cdt'    => array(1, -14400, 'America/Havana'),
    'cdt'    => array(1, -18000, 'America/Chicago'),
    'cdt'    => array(1,  32400, 'Asia/Shanghai'),
    'cemt'   => array(1,  10800, 'Europe/Berlin'),
    'cest'   => array(1,  10800, 'Europe/Kaliningrad'),
    'cest'   => array(1,   7200, 'Europe/Berlin'),
    'cet'    => array(0,   3600, 'Europe/Berlin'),
    'cet'    => array(0,   7200, 'Europe/Kaliningrad'),
    'cgst'   => array(1,  -3600, 'America/Scoresbysund'),
    'cgt'    => array(0,  -7200, 'America/Scoresbysund'),
    'chadt'  => array(1,  49500, 'Pacific/Chatham'),
    'chast'  => array(0,  45900, 'Pacific/Chatham'),
    'chat'   => array(0,  30600, 'Asia/Harbin'),
    'chat'   => array(0,  32400, 'Asia/Harbin'),
    'chdt'   => array(1, -19800, 'America/Belize'),
    'chost'  => array(1,  36000, 'Asia/Choibalsan'),
    'chot'   => array(0,  32400, 'Asia/Choibalsan'),
    'cit'    => array(0,  28800, 'Asia/Dili'),
    'cjt'    => array(0,  32400, 'Asia/Sakhalin'),
    'ckhst'  => array(1, -34200, 'Pacific/Rarotonga'),
    'ckt'    => array(0, -36000, 'Pacific/Rarotonga'),
    'clst'   => array(1, -10800, 'America/Santiago'),
    'clt'    => array(0, -14400, 'America/Santiago'),
    'cost'   => array(1, -14400, 'America/Bogota'),
    'cot'    => array(0, -18000, 'America/Bogota'),
    'cpt'    => array(1, -18000, 'America/Chicago'),
    'cst'    => array(0, -18000, 'America/Havana'),
    'cst'    => array(0, -21600, 'America/Chicago'),
    'cst'    => array(0,  28800, 'Asia/Shanghai'),
    'cst'    => array(0,  34200, 'Australia/Adelaide'),
    'cst'    => array(1,  37800, 'Australia/Adelaide'),
    'cvst'   => array(1,  -3600, 'Atlantic/Cape_Verde'),
    'cvt'    => array(0,  -3600, 'Atlantic/Cape_Verde'),
    'cvt'    => array(0,  -7200, 'Atlantic/Cape_Verde'),
    'cwt'    => array(1, -18000, 'America/Chicago'),
    'chst'   => array(0,  36000, 'Pacific/Guam'),
    'dact'   => array(0,  21600, 'Asia/Dacca'),
    'davt'   => array(0,  25200, 'Antarctica/Davis'),
    'ddut'   => array(0,  36000, 'Antarctica/DumontDUrville'),
    'dusst'  => array(1,  21600, 'Asia/Dushanbe'),
    'dusst'  => array(1,  25200, 'Asia/Dushanbe'),
    'dust'   => array(0,  18000, 'Asia/Dushanbe'),
    'dust'   => array(0,  21600, 'Asia/Dushanbe'),
    'easst'  => array(1, -18000, 'Chile/EasterIsland'),
    'easst'  => array(1, -21600, 'Chile/EasterIsland'),
    'east'   => array(0, -21600, 'Chile/EasterIsland'),
    'east'   => array(0, -25200, 'Chile/EasterIsland'),
    'east'   => array(1,  14400, 'Indian/Antananarivo'),
    'eat'    => array(0,  10800, 'Africa/Khartoum'),
    'ect'    => array(0, -18000, 'America/Guayaquil'),
    'eddt'   => array(1, -10800, 'America/Iqaluit'),
    'edt'    => array(1, -14400, 'America/New_York'),
    'eest'   => array(1,  10800, 'Europe/Helsinki'),
    'eet'    => array(0,   7200, 'Europe/Helsinki'),
    'egst'   => array(1,      0, 'America/Scoresbysund'),
    'egt'    => array(0,  -3600, 'America/Scoresbysund'),
    'ehdt'   => array(1, -16200, 'America/Santo_Domingo'),
    'eit'    => array(0,  32400, 'Asia/Jayapura'),
    'ept'    => array(1, -14400, 'America/New_York'),
    'est'    => array(0, -18000, 'America/New_York'),
    'est'    => array(0,  36000, 'Australia/Melbourne'),
    'est'    => array(1,  39600, 'Australia/Melbourne'),
    'ewt'    => array(1, -14400, 'America/New_York'),
    'fjst'   => array(1,  46800, 'Pacific/Fiji'),
    'fjt'    => array(0,  43200, 'Pacific/Fiji'),
    'fkst'   => array(1, -10800, 'Atlantic/Stanley'),
    'fkst'   => array(1,  -7200, 'Atlantic/Stanley'),
    'fkt'    => array(0, -10800, 'Atlantic/Stanley'),
    'fkt'    => array(0, -14400, 'Atlantic/Stanley'),
    'fnst'   => array(1,  -3600, 'America/Noronha'),
    'fnt'    => array(0,  -7200, 'America/Noronha'),
    'fort'   => array(0,  14400, 'Asia/Aqtau'),
    'fort'   => array(0,  18000, 'Asia/Aqtau'),
    'frust'  => array(1,  21600, 'Asia/Bishkek'),
    'frust'  => array(1,  25200, 'Asia/Bishkek'),
    'frut'   => array(0,  18000, 'Asia/Bishkek'),
    'frut'   => array(0,  21600, 'Asia/Bishkek'),
    'galt'   => array(0, -21600, 'Pacific/Galapagos'),
    'gamt'   => array(0, -32400, 'Pacific/Gambier'),
    'gbgt'   => array(0, -13500, 'America/Guyana'),
    'gest'   => array(1,  14400, 'Asia/Tbilisi'),
    'get'    => array(0,  10800, 'Asia/Tbilisi'),
    'get'    => array(0,  14400, 'Asia/Tbilisi'),
    'gft'    => array(0, -10800, 'America/Cayenne'),
    'gft'    => array(0, -14400, 'America/Cayenne'),
    'ghst'   => array(1,   1200, 'Africa/Accra'),
    'gst'    => array(0,  14400, 'Asia/Dubai'),
    'gyt'    => array(0, -14400, 'America/Guyana'),
    'hadt'   => array(1, -32400, 'America/Adak'),
    'hast'   => array(0, -36000, 'America/Adak'),
    'hdt'    => array(1, -34200, 'Pacific/Honolulu'),
    'hkst'   => array(1,  32400, 'Asia/Hong_Kong'),
    'hkt'    => array(0,  28800, 'Asia/Hong_Kong'),
    'hovst'  => array(1,  28800, 'Asia/Hovd'),
    'hovt'   => array(0,  21600, 'Asia/Hovd'),
    'hovt'   => array(0,  25200, 'Asia/Hovd'),
    'hpt'    => array(1, -34200, 'Pacific/Honolulu'),
    'hst'    => array(0, -36000, 'Pacific/Honolulu'),
    'hwt'    => array(1, -34200, 'Pacific/Honolulu'),
    'ict'    => array(0,  25200, 'Asia/Bangkok'),
    'iddt'   => array(1,  14400, 'Asia/Jerusalem'),
    'idt'    => array(1,  10800, 'Asia/Jerusalem'),
    'ihst'   => array(1,  21600, 'Asia/Colombo'),
    'iot'    => array(0,  18000, 'Indian/Chagos'),
    'iot'    => array(0,  21600, 'Indian/Chagos'),
    'irdt'   => array(1,  16200, 'Asia/Tehran'),
    'irkst'  => array(1,  28800, 'Asia/Irkutsk'),
    'irkst'  => array(1,  32400, 'Asia/Irkutsk'),
    'irkt'   => array(0,  25200, 'Asia/Irkutsk'),
    'irkt'   => array(0,  28800, 'Asia/Irkutsk'),
    'irst'   => array(0,  12600, 'Asia/Tehran'),
    'isst'   => array(1,      0, 'Atlantic/Reykjavik'),
    'ist'    => array(0,  -3600, 'Atlantic/Reykjavik'),
    'ist'    => array(0,  19800, 'Asia/Kolkata'),
    'ist'    => array(1,   2079, 'Europe/Dublin'),
    'ist'    => array(1,  23400, 'Asia/Kolkata'),
    'ist'    => array(0,   3600, 'Europe/Dublin'),
    'ist'    => array(1,   3600, 'Europe/Dublin'),
    'ist'    => array(0,   7200, 'Asia/Jerusalem'),
    'javt'   => array(0,  26400, 'Asia/Jakarta'),
    'jdt'    => array(1,  36000, 'Asia/Tokyo'),
    'jst'    => array(0,  32400, 'Asia/Tokyo'),
    'kart'   => array(0,  18000, 'Asia/Karachi'),
    'kast'   => array(0,  18000, 'Asia/Kashgar'),
    'kast'   => array(0,  19800, 'Asia/Kashgar'),
    'kdt'    => array(1,  36000, 'Asia/Seoul'),
    'kgst'   => array(1,  21600, 'Asia/Bishkek'),
    'kgt'    => array(0,  18000, 'Asia/Bishkek'),
    'kizst'  => array(1,  21600, 'Asia/Qyzylorda'),
    'kizt'   => array(0,  14400, 'Asia/Qyzylorda'),
    'kizt'   => array(0,  18000, 'Asia/Qyzylorda'),
    'kizt'   => array(0,  21600, 'Asia/Qyzylorda'),
    'kmt'    => array(0,   5736, 'Europe/Vilnius'),
    'kost'   => array(0,  39600, 'Pacific/Kosrae'),
    'kost'   => array(0,  43200, 'Pacific/Kosrae'),
    'krast'  => array(1,  25200, 'Asia/Krasnoyarsk'),
    'krast'  => array(1,  28800, 'Asia/Krasnoyarsk'),
    'krat'   => array(0,  21600, 'Asia/Krasnoyarsk'),
    'krat'   => array(0,  25200, 'Asia/Krasnoyarsk'),
    'kst'    => array(0,  32400, 'Asia/Seoul'),
    'kst'    => array(0,  30600, 'Asia/Pyongyang'),
    'kst'    => array(0,  32400, 'Asia/Pyongyang'),
    'kuyst'  => array(1,  10800, 'Europe/Samara'),
    'kuyst'  => array(1,  14400, 'Europe/Samara'),
    'kuyst'  => array(1,  18000, 'Europe/Samara'),
    'kuyt'   => array(0,  10800, 'Europe/Samara'),
    'kuyt'   => array(0,  14400, 'Europe/Samara'),
    'kwat'   => array(0, -43200, 'Kwajalein'),
    'kwat'   => array(0, -43200, 'Pacific/Kwajalein'),
    'lhst'   => array(0,  37800, 'Australia/Lord_Howe'),
    'lhst'   => array(1,  39600, 'Australia/Lord_Howe'),
    'lhst'   => array(1,  41400, 'Australia/Lord_Howe'),
    'lint'   => array(0, -36000, 'Pacific/Kiritimati'),
    'lint'   => array(0,  50400, 'Pacific/Kiritimati'),
    'lkt'    => array(0,  21600, 'Asia/Colombo'),
    'lkt'    => array(0,  23400, 'Asia/Colombo'),
    'lont'   => array(0,  25200, 'Asia/Chongqing'),
    'lrt'    => array(0,  -2670, 'Africa/Monrovia'),
    'lst'    => array(1,   9384, 'Europe/Riga'),
    'madmt'  => array(1,   3600, 'Atlantic/Madeira'),
    'madst'  => array(1,      0, 'Atlantic/Madeira'),
    'madt'   => array(0,  -3600, 'Atlantic/Madeira'),
    'magst'  => array(1,  43200, 'Asia/Magadan'),
    'magt'   => array(0,  36000, 'Asia/Magadan'),
    'malst'  => array(1,  26400, 'Asia/Singapore'),
    'malt'   => array(0,  25200, 'Asia/Singapore'),
    'malt'   => array(0,  26400, 'Asia/Singapore'),
    'malt'   => array(0,  27000, 'Asia/Singapore'),
    'mart'   => array(0, -34200, 'Pacific/Marquesas'),
    'mawt'   => array(0,  21600, 'Antarctica/Mawson'),
    'mddt'   => array(1, -18000, 'America/Cambridge_Bay'),
    'mdst'   => array(1,  16248, 'Europe/Moscow'),
    'mdt'    => array(1, -21600, 'America/Denver'),
    'mht'    => array(0,  43200, 'Pacific/Kwajalein'),
    'mmt'    => array(0,  28656, 'Asia/Makassar'),
    'mmt'    => array(0,   9048, 'Europe/Moscow'),
    'most'   => array(1,  32400, 'Asia/Macao'),
    'mot'    => array(0,  28800, 'Asia/Macao'),
    'mpt'    => array(1, -21600, 'America/Denver'),
    'mpt'    => array(0,  36000, 'Pacific/Saipan'),
    'msd'    => array(1,  14400, 'Europe/Moscow'),
    'msk'    => array(0,  10800, 'Europe/Moscow'),
    'mst'    => array(0, -25200, 'America/Denver'),
    'mst'    => array(1,  12648, 'Europe/Moscow'),
    'mut'    => array(0,  14400, 'Indian/Mauritius'),
    'mvt'    => array(0,  18000, 'Indian/Maldives'),
    'mwt'    => array(1, -21600, 'America/Denver'),
    'myt'    => array(0,  28800, 'Asia/Kuala_Lumpur'),
    'ncst'   => array(1,  43200, 'Pacific/Noumea'),
    'nct'    => array(0,  39600, 'Pacific/Noumea'),
    'nddt'   => array(1,  -5400, 'America/St_Johns'),
    'ndt'    => array(1, -36000, 'Pacific/Midway'),
    'ndt'    => array(1,  -9000, 'America/St_Johns'),
    'ndt'    => array(1,  -9052, 'America/St_Johns'),
    'negt'   => array(0, -12600, 'America/Paramaribo'),
    'nest'   => array(1,   4800, 'Europe/Amsterdam'),
    'net'    => array(0,   1200, 'Europe/Amsterdam'),
    'nft'    => array(0,  41400, 'Pacific/Norfolk'),
    'novst'  => array(1,  25200, 'Asia/Novosibirsk'),
    'novt'   => array(0,  21600, 'Asia/Novosibirsk'),
    'npt'    => array(1, -36000, 'America/Adak'),
    'npt'    => array(1,  -9000, 'America/St_Johns'),
    'npt'    => array(0,  20700, 'Asia/Katmandu'),
    'nrt'    => array(0,  41400, 'Pacific/Nauru'),
    'nrt'    => array(0,  43200, 'Pacific/Nauru'),
    'nst'    => array(0, -12600, 'America/St_Johns'),
    'nst'    => array(0, -12652, 'America/St_Johns'),
    'nst'    => array(0, -39600, 'America/Adak'),
    'nst'    => array(1,   4772, 'Europe/Amsterdam'),
    'nut'    => array(0, -39600, 'Pacific/Niue'),
    'nwt'    => array(1,  -9000, 'America/St_Johns'),
    'nzdt'   => array(1,  46800, 'Pacific/Auckland'),
    'nzmt'   => array(0,  41400, 'Pacific/Auckland'),
    'nzst'   => array(0,  43200, 'Pacific/Auckland'),
    'omsst'  => array(1,  21600, 'Asia/Omsk'),
    'omsst'  => array(1,  25200, 'Asia/Omsk'),
    'omst'   => array(0,  18000, 'Asia/Omsk'),
    'omst'   => array(0,  21600, 'Asia/Omsk'),
    'orast'  => array(1,  18000, 'Asia/Oral'),
    'orat'   => array(0,  14400, 'Asia/Oral'),
    'orat'   => array(0,  18000, 'Asia/Oral'),
    'pddt'   => array(1, -21600, 'America/Inuvik'),
    'pdt'    => array(1, -25200, 'America/Los_Angeles'),
    'pest'   => array(1, -14400, 'America/Lima'),
    'petst'  => array(1,  43200, 'Asia/Kamchatka'),
    'petst'  => array(1,  46800, 'Asia/Kamchatka'),
    'pett'   => array(0,  39600, 'Asia/Kamchatka'),
    'pett'   => array(0,  43200, 'Asia/Kamchatka'),
    'pet'    => array(0, -18000, 'America/Lima'),
    'phot'   => array(0, -39600, 'Pacific/Enderbury'),
    'phot'   => array(0,  46800, 'Pacific/Enderbury'),
    'phst'   => array(1,  32400, 'Asia/Manila'),
    'pht'    => array(0,  28800, 'Asia/Manila'),
    'pkst'   => array(1,  21600, 'Asia/Karachi'),
    'pkt'    => array(0,  18000, 'Asia/Karachi'),
    'pmdt'   => array(1,  -7200, 'America/Miquelon'),
    'pmst'   => array(0, -10800, 'America/Miquelon'),
    'pmt'    => array(0, -13236, 'America/Paramaribo'),
    'pmt'    => array(0, -13252, 'America/Paramaribo'),
    'pmt'    => array(0,  26240, 'Asia/Pontianak'),
    'pmt'    => array(0,  36000, 'Antarctica/DumontDUrville'),
    'ppt'    => array(1, -25200, 'America/Los_Angeles'),
    'pst'    => array(0, -28800, 'America/Los_Angeles'),
    'pwt'    => array(1, -25200, 'America/Los_Angeles'),
    'pyst'   => array(1, -10800, 'America/Asuncion'),
    'pyt'    => array(0, -10800, 'America/Asuncion'),
    'pyt'    => array(0, -14400, 'America/Asuncion'),
    'qyzst'  => array(1,  25200, 'Asia/Qyzylorda'),
    'qyzt'   => array(0,  18000, 'Asia/Qyzylorda'),
    'qyzt'   => array(0,  21600, 'Asia/Qyzylorda'),
    'ret'    => array(0,  14400, 'Indian/Reunion'),
    'rmt'    => array(0,   5784, 'Europe/Riga'),
    'rott'   => array(0, -10800, 'Antarctica/Rothera'),
    'sakst'  => array(1,  39600, 'Asia/Sakhalin'),
    'sakt'   => array(0,  36000, 'Asia/Sakhalin'),
    'samst'  => array(1,  18000, 'Europe/Samara'),
    'samst'  => array(1,  21600, 'Asia/Samarkand'),
    'samt'   => array(0, -41400, 'Pacific/Samoa'),
    'samt'   => array(0,  14400, 'Asia/Samarkand'),
    'samt'   => array(0,  18000, 'Asia/Samarkand'),
    'sast'   => array(1,  10800, 'Africa/Johannesburg'),
    'sast'   => array(0,   7200, 'Africa/Johannesburg'),
    'sbt'    => array(0,  39600, 'Pacific/Guadalcanal'),
    'sct'    => array(0,  14400, 'Indian/Mahe'),
    'sgt'    => array(0,  28800, 'Asia/Singapore'),
    'shest'  => array(1,  21600, 'Asia/Aqtau'),
    'shet'   => array(0,  18000, 'Asia/Aqtau'),
    'shet'   => array(0,  21600, 'Asia/Aqtau'),
    'slst'   => array(1,  -1200, 'Africa/Freetown'),
    'slst'   => array(1,   3600, 'Africa/Freetown'),
    'smt'    => array(0,  25580, 'Asia/Saigon'),
    'srt'    => array(0, -10800, 'America/Paramaribo'),
    'sst'    => array(0, -39600, 'Pacific/Samoa'),
    'svest'  => array(1,  18000, 'Asia/Yekaterinburg'),
    'svest'  => array(1,  21600, 'Asia/Yekaterinburg'),
    'svet'   => array(0,  14400, 'Asia/Yekaterinburg'),
    'svet'   => array(0,  18000, 'Asia/Yekaterinburg'),
    'syot'   => array(0,  10800, 'Antarctica/Syowa'),
    'taht'   => array(0, -36000, 'Pacific/Tahiti'),
    'tasst'  => array(1,  21600, 'Asia/Samarkand'),
    'tasst'  => array(1,  25200, 'Asia/Samarkand'),
    'tast'   => array(0,  18000, 'Asia/Tashkent'),
    'tast'   => array(0,  21600, 'Asia/Samarkand'),
    'tbist'  => array(1,  14400, 'Asia/Tbilisi'),
    'tbist'  => array(1,  18000, 'Asia/Tbilisi'),
    'tbit'   => array(0,  10800, 'Asia/Tbilisi'),
    'tbit'   => array(0,  14400, 'Asia/Tbilisi'),
    'tft'    => array(0,  18000, 'Indian/Kerguelen'),
    'tjt'    => array(0,  18000, 'Asia/Dushanbe'),
    'tlt'    => array(0,  28800, 'Asia/Dili'),
    'tlt'    => array(0,  32400, 'Asia/Dili'),
    'tmt'    => array(0,  12344, 'Asia/Tehran'),
    'tmt'    => array(0,  14400, 'Asia/Ashgabat'),
    'tmt'    => array(0,  18000, 'Asia/Ashgabat'),
    'tmt'    => array(0,   5940, 'Europe/Tallinn'),
    'tost'   => array(1,  50400, 'Pacific/Tongatapu'),
    'tot'    => array(0,  46800, 'Pacific/Tongatapu'),
    'trst'   => array(1,  14400, 'Europe/Istanbul'),
    'trt'    => array(0,  10800, 'Europe/Istanbul'),
    'ulast'  => array(1,  32400, 'Asia/Ulaanbaatar'),
    'ulat'   => array(0,  25200, 'Asia/Ulaanbaatar'),
    'ulat'   => array(0,  28800, 'Asia/Ulaanbaatar'),
    'urast'  => array(1,  18000, 'Asia/Oral'),
    'urast'  => array(1,  21600, 'Asia/Oral'),
    'urat'   => array(0,  14400, 'Asia/Oral'),
    'urat'   => array(0,  18000, 'Asia/Oral'),
    'urat'   => array(0,  21600, 'Asia/Oral'),
    'urut'   => array(0,  21600, 'Asia/Urumqi'),
    'uyhst'  => array(1, -10800, 'America/Montevideo'),
    'uyhst'  => array(1,  -9000, 'America/Montevideo'),
    'uyst'   => array(1,  -7200, 'America/Montevideo'),
    'uyt'    => array(0, -10800, 'America/Montevideo'),
    'uzst'   => array(1,  21600, 'Asia/Samarkand'),
    'uzt'    => array(0,  18000, 'Asia/Samarkand'),
    'vet'    => array(0, -14400, 'America/Caracas'),
    'vet'    => array(0, -16200, 'America/Caracas'),
    'vlasst'  => array(1,  36000, 'Asia/Vladivostok'),
    'vlast'  => array(1,  39600, 'Asia/Vladivostok'),
    'vlat'   => array(0,  36000, 'Asia/Vladivostok'),
    'vost'   => array(0,  21600, 'Antarctica/Vostok'),
    'vust'   => array(1,  43200, 'Pacific/Efate'),
    'vut'    => array(0,  39600, 'Pacific/Efate'),
    'warst'  => array(1, -10800, 'America/Mendoza'),
    'wart'   => array(0, -14400, 'America/Mendoza'),
    'wast'   => array(1,   7200, 'Africa/Windhoek'),
    'wat'    => array(0,  -3600, 'Africa/Dakar'),
    'wat'    => array(0,      0, 'Africa/Freetown'),
    'wat'    => array(0,   3600, 'Africa/Brazzaville'),
    'wemt'   => array(1,   7200, 'Europe/Lisbon'),
    'west'   => array(1,   3600, 'Europe/Paris'),
    'west'   => array(1,   7200, 'Europe/Luxembourg'),
    'wet'    => array(0,      0, 'Europe/Paris'),
    'wet'    => array(0,   3600, 'Europe/Luxembourg'),
    'wgst'   => array(1,  -7200, 'America/Godthab'),
    'wgt'    => array(0, -10800, 'America/Godthab'),
    'wit'    => array(0,  25200, 'Asia/Jakarta'),
    'wit'    => array(0,  27000, 'Asia/Jakarta'),
    'wit'    => array(0,  28800, 'Asia/Jakarta'),
    'wst'    => array(0, -39600, 'Pacific/Apia'),
    'wst'    => array(0,  28800, 'Australia/Perth'),
    'wst'    => array(1,  32400, 'Australia/Perth'),
    'yakst'  => array(1,  32400, 'Asia/Yakutsk'),
    'yakst'  => array(1,  36000, 'Asia/Yakutsk'),
    'yakt'   => array(0,  28800, 'Asia/Yakutsk'),
    'yakt'   => array(0,  32400, 'Asia/Yakutsk'),
    'yddt'   => array(1, -25200, 'America/Dawson'),
    'ydt'    => array(1, -28800, 'America/Dawson'),
    'yekst'  => array(1,  21600, 'Asia/Yekaterinburg'),
    'yekt'   => array(0,  18000, 'Asia/Yekaterinburg'),
    'yerst'  => array(1,  14400, 'Asia/Yerevan'),
    'yerst'  => array(1,  18000, 'Asia/Yerevan'),
    'yert'   => array(0,  10800, 'Asia/Yerevan'),
    'yert'   => array(0,  14400, 'Asia/Yerevan'),
    'ypt'    => array(1, -28800, 'America/Dawson'),
    'yst'    => array(0, -32400, 'America/Anchorage'),
    'ywt'    => array(1, -28800, 'America/Dawson'),
    'zzz'    => array(0,      0, 'Antarctica/Davis'),
);

$mapping = array();

foreach ($zones as $zone) {
    fprintf(STDERR, "Checking $zone: ");
    if (!(preg_match( '@^([A-Z][a-z]+([_/-][A-Za-z]+)+)$@', $zone) || (preg_match( '@^[A-Z]{1,6}$@', $zone)))) {
        fprintf(STDERR, "skipped.\n");
        continue;
    }
    $tz = @timezone_open($zone);
    if (!$tz) {
        fprintf(STDERR, "skipped.\n");
        continue;
    }
    $transistions = timezone_transitions_get($tz);
    if ($transistions === FALSE) {
        $transistions = array();
    }
    foreach ($transistions as $trans) {
        if (preg_match( '/^[+-][0-9]{2}$/', $trans['abbr'] ) ) {
            continue;
        }
        if (preg_match( '/^[+-][0-9]{4}$/', $trans['abbr'] ) ) {
            continue;
        }
        if ($trans['abbr'] == 'LMT') {
            continue;
        }
        $key = $trans['abbr'] . '|' . $trans['offset']. '|' . ($trans['isdst'] ? '1' : '0');

        if (isset($mapping[$key])) {
            if (!in_array($zone, $mapping[$key])) {
                $mapping[$key][] = $zone;
            }
        } else {
            $mapping[$key] = array($zone);
        }
    }
    fprintf(STDERR, "ok.\n");
}

//Militairy zones
$ls = array_merge(range('a', 'i'), range('k', 'm'));
$h = 1;
foreach ($ls as $l) {
    $offset = $h * 3600;
    ++$h;
    $mapping["$l|$offset|0"] = array(null);
}
$ls = range('n', 'y');
$h = 1;
foreach ($ls as $l) {
    $offset = -$h * 3600;
    ++$h;
    $mapping["$l|$offset|0"] = array(null);
}
$mapping["z|0|0"] = array(null);

//Special BC overrides
$mapping['cast|34200|0'] = array('Australia/Adelaide');

//Add UTC
$mapping['utc|0|0'] = array('UTC');

ksort($mapping);
//var_dump($mapping);
$mapping2 = array();
foreach ($mapping as $key => $test) {
    list($abbr, $offset, $isdst) = explode('|', $key);
    $abbr = strtolower($abbr);
    if (isset($priorityB[$abbr]) && $offset == $priorityB[$abbr][1] && $isdst == $priorityB[$abbr][0]) {
        $fullelem = array($isdst, $offset, $priorityB[$abbr][2]);
        $keys = array_flip($test);
        unset($keys[$priorityB[$abbr][2]]);
        $test = array_flip($keys);
        if (isset($priorityA[$abbr]) && $priorityA[$abbr] == $priorityB[$abbr][2]) {
            $mapping2[$abbr][0][] = $fullelem;
        } else {
            $mapping2[$abbr][1][] = $fullelem;
        }
//			var_dump($abbr, $priorityB[$abbr], $mapping2[$abbr]);
    }
    foreach ($test as $id) {
        $fullelem = array($isdst, $offset, $id);
        if (isset($priorityA[$abbr]) && $priorityA[$abbr] == $id) {
            $mapping2[$abbr][1][] = $fullelem;
        } else {
            $mapping2[$abbr][2][] = $fullelem;
        }
    }
}
//var_dump($mapping2);

$file = __DIR__ . "/../TzMapping.java";
file_put_contents($file, <<<JAVA
package php.date;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动生成代码，请勿修改
 * @author xiaofeng
 * @version $version
 * @since $created_at
 */
@SuppressWarnings("SpellCheckingInspection")
class TzMapping {

    final String abbr;
    final int is_dst;
    final int gmt_offset;
    final String name;

    TzMapping(String abbr, int is_dst, int gmt_offset, String name) {
        this.abbr = abbr;
        this.is_dst = is_dst;
        this.gmt_offset = gmt_offset;
        this.name = name;
    }

    final static Map<String, TzMapping> cache = new ConcurrentHashMap<>();

    static TzMapping abbr_search(String abbr) {
        abbr = abbr.toLowerCase();
        return cache.computeIfAbsent(abbr, key -> {
			if ("utc".equals(key) || "gmt".equals(key)) {
				return UTC;
			} else {
				for (TzMapping tz : tblTimezones) {
					if (tz.abbr.equals(key)) {
						return tz;
					}
				}
				return null;
			}
		});
    }

    final static TzMapping UTC = new TzMapping("utc", 0, 0, "UTC");
    final static TzMapping[] tblTimezones = {

JAVA
);

$fd = fopen($file, "a");

foreach ($mapping2 as $abbr => $abbr_parts) {
    ksort($abbr_parts);
    foreach ($abbr_parts as $abbr_elements) {
        foreach ($abbr_elements as $abbr_elem) {
            list($isdst, $offset, $elem) = $abbr_elem;

            // 过滤掉军事时区
            if (strlen($abbr) === 1) {
                continue;
            }

            if ($elem === NULL) {
                // echo "\t{ ", sprintf('%-9s', '"'. $abbr. '", '), $isdst, ', ', sprintf("%7s", $offset . ','), sprintf("%-32s", " NULL"), " },\n";
                fwrite($fd, "\t\t\tnew TzMapping(" . sprintf('%-9s', '"'. $abbr. '", ') . $isdst . ', ' . sprintf("%7s", $offset . ',') . sprintf("%-32s", " null") . "),\n");
            } else {
                // echo "\t{ ", sprintf('%-9s', '"'. $abbr. '", '), $isdst, ', ', sprintf("%7s", $offset . ','), sprintf("%-32s", " \"$elem\""), " },\n";
                fwrite($fd, "\t\t\tnew TzMapping(" . sprintf('%-9s', '"'. $abbr. '", ') . $isdst . ', ' . sprintf("%7s", $offset . ',') . sprintf("%-32s", " \"$elem\"") . "),\n");
            }
        }
    }
}

fwrite($fd, <<<JAVA

            // 下面这些应该是军事时区
            // https://en.wikipedia.org/wiki/List_of_military_time_zones
            // https://militarybenefits.info/military-time/
            new TzMapping("a",     0,   3600, "UTC+01:00"                     ),
            new TzMapping("b",     0,   7200, "UTC+02:00"                     ),
            new TzMapping("c",     0,  10800, "UTC+03:00"                     ),
            new TzMapping("d",     0,  14400, "UTC+04:00"                     ),
            new TzMapping("e",     0,  18000, "UTC+05:00"                     ),
            new TzMapping("f",     0,  21600, "UTC+06:00"                     ),
            new TzMapping("g",     0,  25200, "UTC+07:00"                     ),
            new TzMapping("h",     0,  28800, "UTC+08:00"                     ),
            new TzMapping("i",     0,  32400, "UTC+09:00"                     ),
            new TzMapping("k",     0,  36000, "UTC+10:00"                     ),
            new TzMapping("l",     0,  39600, "UTC+11:00"                     ),
            new TzMapping("m",     0,  43200, "UTC+12:00"                     ),
            new TzMapping("n",     0,  -3600, "UTC-01:00"                     ),
            new TzMapping("o",     0,  -7200, "UTC-02:00"                     ),
            new TzMapping("p",     0, -10800, "UTC-03:00"                     ),
            new TzMapping("q",     0, -14400, "UTC-04:00"                     ),
            new TzMapping("r",     0, -18000, "UTC-05:00"                     ),
            new TzMapping("s",     0, -21600, "UTC-06:00"                     ),
            new TzMapping("t",     0, -25200, "UTC-07:00"                     ),
            new TzMapping("u",     0, -28800, "UTC-08:00"                     ),
            new TzMapping("v",     0, -32400, "UTC-09:00"                     ),
            new TzMapping("w",     0, -36000, "UTC-10:00"                     ),
            new TzMapping("x",     0, -39600, "UTC-11:00"                     ),
            new TzMapping("y",     0, -43200, "UTC-12:00"                     ),
            new TzMapping("z",     0,      0, "UTC"                           ),
    };
}

/*
typedef struct _timelib_tz_lookup_table {
	char       *name;
	int         type;
	float       gmtoffset;
	char       *full_tz_name;
} timelib_tz_lookup_table;
*/
JAVA
);

fclose($fd);