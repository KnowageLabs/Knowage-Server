if (!Array.prototype.includes) {
  Array.prototype.includes = function(searchElement /*, fromIndex*/) {
    'use strict';
    if (this == null) {
      throw new TypeError('Array.prototype.includes called on null or undefined');
    }

    var O = Object(this);
    var len = parseInt(O.length, 10) || 0;
    if (len === 0) {
      return false;
    }
    var n = parseInt(arguments[1], 10) || 0;
    var k;
    if (n >= 0) {
      k = n;
    } else {
      k = len + n;
      if (k < 0) {k = 0;}
    }
    var currentElement;
    while (k < len) {
      currentElement = O[k];
      if (searchElement === currentElement ||
         (searchElement !== searchElement && currentElement !== currentElement)) { // NaN !== NaN
        return true;
      }
      k++;
    }
    return false;
  };

}

if (!Array.prototype.find) {
  Object.defineProperty(Array.prototype, 'find', {
    value: function(predicate) {
     'use strict';
     if (this == null) {
       throw new TypeError('Array.prototype.find called on null or undefined');
     }
     if (typeof predicate !== 'function') {
       throw new TypeError('predicate must be a function');
     }
     var list = Object(this);
     var length = list.length >>> 0;
     var thisArg = arguments[1];

     for (var i = 0; i !== length; i++) {
       if (predicate.call(thisArg, this[i], i, list)) {
         return this[i];
       }
     }
     return undefined;
    }
  });
}

if (!String.prototype.includes) {
	String.prototype.includes = function(search, start) {
	    'use strict';
	    if (typeof start !== 'number') {
	    	start = 0;
	    }

	    if (start + search.length > this.length) {
	    	return false;
	    } else {
	    	return this.indexOf(search, start) !== -1;
		}
	};
}