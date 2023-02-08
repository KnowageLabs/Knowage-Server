import * as deepEqual from 'deep-equal'

export function filter() {
    return function (array, expression, comparator?, anyPropertyKey?) {
        if (!isArrayLike(array)) {
            if (array == null) {
                return array
            } else {
                console.error('error filtering')
            }
        }

        anyPropertyKey = anyPropertyKey || '$'
        var expressionType = getTypeForFilter(expression)
        var predicateFn
        var matchAgainstAnyProp

        switch (expressionType) {
            case 'function':
                predicateFn = expression
                break
            case 'boolean':
            case 'null':
            case 'number':
            case 'string':
                matchAgainstAnyProp = true
            // falls through
            case 'object':
                predicateFn = createPredicateFn(expression, comparator, anyPropertyKey, matchAgainstAnyProp)
                break
            default:
                return array
        }

        return Array.prototype.filter.call(array, predicateFn)
    }
}

// Helper functions for `filterFilter`
function createPredicateFn(expression, comparator, anyPropertyKey, matchAgainstAnyProp) {
    var shouldMatchPrimitives = isObject(expression) && anyPropertyKey in expression
    var predicateFn

    if (comparator === true) {
        comparator = deepEqual
    } else if (!isFunction(comparator)) {
        comparator = function (actual, expected) {
            if (isUndefined(actual)) {
                // No substring matching against `undefined`
                return false
            }
            if (actual === null || expected === null) {
                // No substring matching against `null`; only match against `null`
                return actual === expected
            }
            if (isObject(expected) || (isObject(actual) && !hasCustomToString(actual))) {
                // Should not compare primitives against objects, unless they have custom `toString` method
                return false
            }

            actual = lowercase('' + actual)
            expected = lowercase('' + expected)
            return actual.indexOf(expected) !== -1
        }
    }

    predicateFn = function (item) {
        if (shouldMatchPrimitives && !isObject(item)) {
            return deepCompare(item, expression[anyPropertyKey], comparator, anyPropertyKey, false)
        }
        return deepCompare(item, expression, comparator, anyPropertyKey, matchAgainstAnyProp)
    }

    return predicateFn
}

function deepCompare(actual, expected, comparator, anyPropertyKey, matchAgainstAnyProp, dontMatchWholeObject?) {
    var actualType = getTypeForFilter(actual)
    var expectedType = getTypeForFilter(expected)

    if (expectedType === 'string' && expected.charAt(0) === '!') {
        return !deepCompare(actual, expected.substring(1), comparator, anyPropertyKey, matchAgainstAnyProp)
    } else if (isArray(actual)) {
        // In case `actual` is an array, consider it a match
        // if ANY of it's items matches `expected`
        return actual.some(function (item) {
            return deepCompare(item, expected, comparator, anyPropertyKey, matchAgainstAnyProp)
        })
    }

    switch (actualType) {
        case 'object':
            var key
            if (matchAgainstAnyProp) {
                for (key in actual) {
                    // Under certain, rare, circumstances, key may not be a string and `charAt` will be undefined
                    // See: https://github.com/angular/angular.js/issues/15644
                    if (key.charAt && key.charAt(0) !== '$' && deepCompare(actual[key], expected, comparator, anyPropertyKey, true)) {
                        return true
                    }
                }
                return dontMatchWholeObject ? false : deepCompare(actual, expected, comparator, anyPropertyKey, false)
            } else if (expectedType === 'object') {
                for (key in expected) {
                    var expectedVal = expected[key]
                    if (isFunction(expectedVal) || isUndefined(expectedVal)) {
                        continue
                    }

                    var matchAnyProperty = key === anyPropertyKey
                    var actualVal = matchAnyProperty ? actual : actual[key]
                    if (!deepCompare(actualVal, expectedVal, comparator, anyPropertyKey, matchAnyProperty, matchAnyProperty)) {
                        return false
                    }
                }
                return true
            } else {
                return comparator(actual, expected)
            }
        case 'function':
            return false
        default:
            return comparator(actual, expected)
    }
}

// Used for easily differentiating between `null` and actual `object`
function getTypeForFilter(val) {
    return val === null ? 'null' : typeof val
}

function isArrayLike(obj) {
    // `null`, `undefined` and `window` are not array-like
    if (obj == null || isWindow(obj)) return false

    // arrays, strings and jQuery/jqLite objects are array like
    // * jqLite is either the jQuery or jqLite constructor function
    // * we have to check the existence of jqLite first as this method is called
    //   via the forEach method when constructing the jqLite object in the first place
    if (isArray(obj) || isString(obj)) return true

    // Support: iOS 8.2 (not reproducible in simulator)
    // "length" in obj used to prevent JIT error (gh-11508)
    var length = 'length' in Object(obj) && obj.length

    // NodeList objects (with `item` method) and
    // other objects with suitable length characteristics are array-like
    return isNumber(length) && ((length >= 0 && length - 1 in obj) || typeof obj.item === 'function')
}

function isWindow(obj) {
    return obj && obj.window === obj
}
function isArray(arr) {
    return Array.isArray(arr) || arr instanceof Array
}
function isString(value) {
    return typeof value === 'string'
}
function isNumber(value) {
    return typeof value === 'number'
}
function isObject(value) {
    return value !== null && typeof value === 'object'
}
function isFunction(value) {
    return typeof value === 'function'
}
function isUndefined(value) {
    return typeof value === 'undefined'
}
function hasCustomToString(obj) {
    return isFunction(obj.toString) && obj.toString !== toString
}
var lowercase = function (string) {
    return isString(string) ? string.toLowerCase() : string
}
