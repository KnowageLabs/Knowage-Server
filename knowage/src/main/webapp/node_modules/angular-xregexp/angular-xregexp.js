/* angular-xregexp.js / v1.0.0 / (c) 2016 Cosimo Meli / MIT Licence */

'format amd';
/* global define */

(function () {
    'use strict';

    function requireXRegExp() {
        try {
            return require('xregexp'); // Using nw.js or browserify?
        } catch (e) {
            throw new Error('Please install XRegExp via npm. Please reference to: https://github.com/cosimomeli/angular-xregexp');
        }
    }

    function angularXRegExp(angular, XRegExp) {

        if (typeof XRegExp === 'undefined') {
            if (typeof require === 'function') {
                XRegExp = requireXRegExp();
            } else {
                throw new Error('XRegExp cannot be found by angular-xregexp! Please reference to: https://github.com/cosimomeli/angular-xregexp');
            }
        }

        var NODE_TYPE_TEXT = 3;

        /**
         * @returns {string} Returns the string representation of the element.
         */
        function startingTag(element) {
            element = angular.element(element).clone();
            try {
                // turns out IE does not let you set .html() on elements which
                // are not allowed to have children. So we just ignore it.
                element.empty();
            } catch (e) {
            }
            var elemHtml = angular.element('<div>').append(element).html();
            try {
                return element[0].nodeType === NODE_TYPE_TEXT ? angular.lowercase(elemHtml) :
                    elemHtml.match(/^(<[^>]+>)/)[1].replace(/^<([\w\-]+)/, function (match, nodeName) {
                        return '<' + angular.lowercase(nodeName);
                    });
            } catch (e) {
                return angular.lowercase(elemHtml);
            }

        }

        /**
         * New link function to replace the ngPattern one
         */
        function newLink(scope, elm, attr, ctrl) {
            if (!ctrl) {
                return;
            }

            function toXRegExp(regex) {
                var REGEX_STRING_REGEXP = /^\/(.+)\/([a-z]*)$/;
                if (regex instanceof RegExp) {
                    regex = regex.toString();
                }
                var match = regex.match(REGEX_STRING_REGEXP);
                if (match) {
                    return new XRegExp(match[1], match[2]);
                }

            }

            var regexp, patternExp = toXRegExp(attr.ngPattern || attr.pattern);

            attr.$observe('pattern', function (regex) {
                if (angular.isString(regex) && regex.length > 0) {
                    regex = new XRegExp('^' + regex + '$');
                } else if (regex instanceof RegExp) {
                    regex = toXRegExp(regex);
                }

                if (regex && !regex.test) {
                    throw angular.$$minErr('ngPattern')('noregexp',
                        'Expected {0} to be a RegExp but was {1}. Element: {2}', patternExp,
                        regex, startingTag(elm));
                }

                regexp = regex || undefined;
                ctrl.$validate();
            });

            ctrl.$validators.pattern = function (modelValue, viewValue) {
                // HTML5 pattern constraint validates the input value, so we validate the viewValue
                return ctrl.$isEmpty(viewValue) || angular.isUndefined(regexp) || regexp.test(viewValue);
            };
        }

        /**
         * @ngdoc overview
         * @name angularXRegExp
         *
         * @description
         * angularXRegExp module provides XRegExp functionality for angular.js apps.
         */
        angular.module('angularXRegExp', [])

        /**
         * @ngdoc object
         * @name angularXRegExp.config
         */
            .config(['$provide', function ($provide) {
                $provide.decorator('ngPatternDirective', ['$delegate', function ($delegate) {
                    //Replace ngPattern's link function
                    $delegate[0].compile = function () {
                        return function () {
                            newLink.apply(this, arguments);
                        };
                    };
                    return $delegate;
                }]);
            }]);

        return 'angularXRegExp';
    }

    var isElectron = window && window.process && window.process.type;
    if (typeof define === 'function' && define.amd) {
        define(['angular', 'xregexp'], angularXRegExp);
    } else if (typeof module !== 'undefined' && module && module.exports && (typeof require === 'function') && !isElectron) {
        module.exports = angularXRegExp(require('angular'), require('xregexp'));
    } else {
        angularXRegExp(angular, (typeof global !== 'undefined' ? global : window).XRegExp);
    }
})();
