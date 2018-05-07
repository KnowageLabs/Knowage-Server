'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _scripts = require('../../tools/output/scripts');

var _scripts2 = _interopRequireDefault(_scripts);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = function (XRegExp) {

  /**
   * Adds support for all Unicode scripts. E.g., `\p{Latin}`. Token names are case insensitive,
   * and any spaces, hyphens, and underscores are ignored.
   *
   * Uses Unicode 10.0.0.
   *
   * @requires XRegExp, Unicode Base
   */

  if (!XRegExp.addUnicodeData) {
    throw new ReferenceError('Unicode Base must be loaded before Unicode Scripts');
  }

  XRegExp.addUnicodeData(_scripts2.default);
}; /*!
    * XRegExp Unicode Scripts 4.1.1
    * <xregexp.com>
    * Steven Levithan (c) 2010-present MIT License
    * Unicode data by Mathias Bynens <mathiasbynens.be>
    */

module.exports = exports['default'];