'use strict';
angular.module("ngLocale", [], ["$provide", function($provide) {
var PLURAL_CATEGORY = {ZERO: "zero", ONE: "one", TWO: "two", FEW: "few", MANY: "many", OTHER: "other"};
function getDecimals(n) {
  n = n + '';
  var i = n.indexOf('.');
  return (i == -1) ? 0 : n.length - i - 1;
}

function getVF(n, opt_precision) {
  var v = opt_precision;

  if (undefined === v) {
    v = Math.min(getDecimals(n), 3);
  }

  var base = Math.pow(10, v);
  var f = ((n * base) | 0) % base;
  return {v: v, f: f};
}

$provide.value("$locale", {
  "DATETIME_FORMATS": {
    "AMPMS": [
      "AM",
      "PM"
    ],
    "DAY": [
      "nedeľa",
      "pondelok",
      "utorok",
      "streda",
      "štvrtok",
      "piatok",
      "sobota"
    ],
    "ERANAMES": [
      "pred Kristom",
      "po Kristovi"
    ],
    "ERAS": [
      "pred Kr.",
      "po Kr."
    ],
    "FIRSTDAYOFWEEK": 1,
    "MONTH": [
      "január",
      "február",
      "marec",
      "apríl",
      "máj",
      "jún",
      "júl",
      "august",
      "september",
      "október",
      "november",
      "december"
    ],
    "SHORTDAY": [
      "ne",
      "po",
      "ut",
      "st",
      "št",
      "pi",
      "so"
    ],
    "SHORTMONTH": [
      "jan",
      "feb",
      "mar",
      "apr",
      "máj",
      "jún",
      "júl",
      "aug",
      "sep",
      "okt",
      "nov",
      "dec"
    ],
    "STANDALONEMONTH": [
      "január",
      "február",
      "marec",
      "apríl",
      "máj",
      "jún",
      "júl",
      "august",
      "september",
      "október",
      "november",
      "december"
    ],
    "WEEKENDRANGE": [
      5,
      6
    ],
    "fullDate": "EEEE d MMMM y",
    "longDate": "d MMMM y",
    "medium": "dd MMM y HH:mm:ss",
    "mediumDate": "dd MMM y",
    "mediumTime": "HH:mm:ss",
    "short": "dd/MM/yy HH:mm",
    "shortDate": "dd/MM/yy",
    "shortTime": "HH:mm"
  },
  "NUMBER_FORMATS": {
    "CURRENCY_SYM": "€",
    "DECIMAL_SEP": ",",
    "GROUP_SEP": " ",
    "PATTERNS": [
      {
        "gSize": 3,
        "lgSize": 3,
        "maxFrac": 3,
        "minFrac": 0,
        "minInt": 1,
        "negPre": "-",
        "negSuf": "",
        "posPre": "",
        "posSuf": ""
      },
      {
        "gSize": 3,
        "lgSize": 3,
        "maxFrac": 2,
        "minFrac": 2,
        "minInt": 1,
        "negPre": "-",
        "negSuf": " €",
        "posPre": "",
        "posSuf": " €"
      }
    ]
  },
  "id": "sk-sk",
  "localeID": "sk_SK",
  "pluralCat": function(n, opt_precision) {  var i = n | 0;  var vf = getVF(n, opt_precision);  if (i == 1 && vf.v == 0) {    return PLURAL_CATEGORY.ONE;  }  return PLURAL_CATEGORY.OTHER;}
});
}]);
