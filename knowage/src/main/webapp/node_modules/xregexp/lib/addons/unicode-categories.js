'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _categories = require('../../tools/output/categories');

var _categories2 = _interopRequireDefault(_categories);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = function (XRegExp) {

  /**
   * Adds support for Unicode's general categories. E.g., `\p{Lu}` or `\p{Uppercase Letter}`. See
   * category descriptions in UAX #44 <http://unicode.org/reports/tr44/#GC_Values_Table>. Token
   * names are case insensitive, and any spaces, hyphens, and underscores are ignored.
   *
   * Uses Unicode 10.0.0.
   *
   * @requires XRegExp, Unicode Base
   */

  if (!XRegExp.addUnicodeData) {
    throw new ReferenceError('Unicode Base must be loaded before Unicode Categories');
  }

  XRegExp.addUnicodeData(_categories2.default);
}; /*!
    * XRegExp Unicode Categories 4.1.1
    * <xregexp.com>
    * Steven Levithan (c) 2010-present MIT License
    * Unicode data by Mathias Bynens <mathiasbynens.be>
    */

module.exports = exports['default'];