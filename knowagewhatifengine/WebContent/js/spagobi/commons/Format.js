/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
  * Object name
  *
  * [description]
  *
  *
  * Public Functions
  *
  *  [list]
  *
  *
  * Authors
  *
  * - Andrea Gioia (adrea.gioia@eng.it)
  */

Ext.ns("Sbi.whatif.commons");

Sbi.whatif.commons.Format = function(){

	var specials = [
        // order matters for these
          "-"
        , "["
        , "]"
        // order doesn't matter for any of these
        , "/"
        , "{"
        , "}"
        , "("
        , ")"
        , "*"
        , "+"
        , "?"
        , "."
        , "\\"
        , "^"
        , "$"
        , "|"
      ]

      // I choose to escape every character with '\'
      // even though only some strictly require it when inside of []
    , regex = RegExp('[' + specials.join('\\') + ']', 'g')
    ;


	return {
		/**
         * Cut and paste from Ext.util.Format
         */
        date : function(v, format){




			format = format || "m/d/Y";

			if(typeof format === 'string') {
				format = {
					dateFormat: format,
			    	nullValue: ''
				};
			}


            if(!v){
                return format.nullValue;
            }

            if(!(v instanceof Date)){
                v = new Date(Date.parse(v));
            }


            v = v.dateFormat(format.dateFormat);

            return v;
        }

        /**
         * Cut and paste from Ext.util.Format
         */
        , dateRenderer : function(format){
            return function(v){
                return Sbi.qbe.commons.Format.date(v, format);
            };
        }


        /**
         * thanks to Condor: http://www.extjs.com/forum/showthread.php?t=48600
         */
        , number : function(v, format)  {

        	format = Ext.apply({}, format || {}, {
	    		decimalSeparator: '.',
	    		decimalPrecision: 2,
	    		groupingSeparator: ',',
	    		groupingSize: 3,
	    		currencySymbol: '',
	    		nullValue: ''

    		});

        	if(v === undefined || v === null) {
        		 return format.nullValue;
        	}

        	if (typeof v !== 'number') {
    			v = String(v);
    			if (format.currencySymbol) {
    				v = v.replace(format.currencySymbol, '');
    			}
    			if (format.groupingSeparator) {
    				v = v.replace(new RegExp(Sbi.whatif.commons.Format.escapeRegExp(format.groupingSeparator), 'g'), '');
    			}
    			if (format.decimalSeparator !== '.') {
    				v = v.replace(format.decimalSeparator, '.');
    			}
    			v = parseFloat(v);
    		}
    		var neg = v < 0;
    		v = Math.abs(v).toFixed(format.decimalPrecision);
    		var i = v.indexOf('.');
    		if (i >= 0) {
    			if (format.decimalSeparator !== '.') {
    				v = v.slice(0, i) + format.decimalSeparator + v.slice(i + 1);
    			}
    		} else {
    			i = v.length;
    		}
    		if (format.groupingSeparator) {
    			while (i > format.groupingSize) {
    				i -= format.groupingSize;
    				v = v.slice(0, i) + format.groupingSeparator + v.slice(i);
    			}
    		}
    		if (format.currencySymbol) {
    			v = format.currencySymbol + v;
    		}
    		if (neg) {
    			v = '-' + v;
    		}
    		return v;
        }

        , cleanFormattedNumber : function(v, format)  {

        	format = Ext.apply({}, format || {}, {
	    		decimalSeparator: '.',
	    		decimalPrecision: 2,
	    		groupingSeparator: ',',
	    		groupingSize: 3,
	    		currencySymbol: '',
	    		nullValue: ''

    		});

        	if(v === undefined || v === null) {
        		 return format.nullValue;
        	}

        	if (typeof v !== 'number') {
    			v = String(v);
    			if (format.currencySymbol) {
    				v = v.replace(format.currencySymbol, '');
    			}
    			if (format.groupingSeparator != undefined && format.groupingSeparator != null) {
    				v = v.replace(new RegExp(Sbi.whatif.commons.Format.escapeRegExp(format.groupingSeparator), 'g'), '');
    			}
    			if (format.decimalSeparator !== '.') {
    				v = v.replace(format.decimalSeparator, '.');
    			}
    			v = parseFloat(v);

            	v = v+"";


    			if (format.decimalSeparator !== '.') {
    				v = v.replace('.', format.decimalSeparator);
    			}
    		}

    		return v;
        }

        , formatInJavaDouble : function(v, format)  {

        	format = Ext.apply({}, format || {}, {
	    		decimalSeparator: '.',
	    		decimalPrecision: 2,
	    		groupingSeparator: ',',
	    		groupingSize: 3,
	    		currencySymbol: '',
	    		nullValue: ''

    		});

        	if(v === undefined || v === null) {
        		 return format.nullValue;
        	}

        	if (typeof v !== 'number') {
    			v = String(v);
    			if (format.currencySymbol) {
    				v = v.replace(format.currencySymbol, '');
    			}
    			if (format.groupingSeparator!=undefined && format.groupingSeparator!=null) {
    				v = v.replace(format.groupingSeparator, '');
    			}
    			if (format.decimalSeparator !== '.') {
    				v = v.replace(format.decimalSeparator, '.');
    			}
    		}

    		return v;
        }

        , numberRenderer : function(format){
            return function(v){
            	return '<div style=\'text-align: right;\'>' + Sbi.qbe.commons.Format.number(v, format) + '</div>';
            };
        }
        , floatRenderer : function(format){

            return function(v){
            	if( typeof v === 'string'){
            		v = v.replace(',', '.');
            	}
            	var fl = parseFloat(v);
            	return '<div style=\'text-align: right;\'>' + Sbi.qbe.commons.Format.number(fl, format) + '</div>';
            };
        }
        , string : function(v, format) {
        	format = Ext.apply({}, format || {}, {
	    		trim: true,
	    		maxLength: null,
	    		ellipsis: true,
	    		changeCase: null, // null | 'capitalize' | 'uppercase' | 'lowercase'
	    		prefix: '',
	    		suffix: '',
	    		nullValue: ''
    		});

        	if(!v){
                return format.nullValue;
            }

        	if(format.trim){v = Ext.util.Format.trim(v);}
        	if(format.maxLength) {
        		if(format.ellipsis === true) {
        			v = Ext.util.Format.ellipsis(v, format.maxLength);
        		} else {
        			v = Ext.util.Format.substr(v, 0, format.maxLength);
        		}
        	}
        	if(format.changeCase){
        		if(format.changeCase === 'capitalize') {
        			v = Ext.util.Format.capitalize(v);
        		} else if(format.changeCase === 'uppercase') {
        			v = Ext.util.Format.uppercase(v);
        		} else if(format.changeCase === 'lowercase') {
        			v = Ext.util.Format.lowercase(v);
        		}
        	}
        	if(format.prefix){v = format.prefix+ v;}
        	if(format.suffix){v =  v + format.suffix;}

        	return v;
        }

        , stringRenderer : function(format){
            return function(v){
                return Sbi.qbe.commons.Format.string(v, format);
            };
        }

        , 'boolean' : function(v, format) {

        	if (typeof v === 'string') {
        		v = Sbi.qbe.commons.Format.stringToBoolean(v);
        	}

        	format = Ext.apply({}, format || {}, {
	    		trueSymbol: 'true',
	    		falseSymbol: 'false',
	    		nullValue: ''
    		});

        	if(v === true){
        		 v = format.trueSymbol;
            } else if(v === false){
            	 v = format.falseSymbol;
            } else {
            	 v = format.nullValue;
            }

        	return v;
        }

        , booleanRenderer : function(format){
            return function(v){
            	return Sbi.qbe.commons.Format['boolean'](v, format);
            };
        }

        , html : function(v, format) {
        	// format is not used yet but it is reserve for future use
        	// ex. format.cls, format.style
        	v = Ext.util.Format.htmlDecode(v);
        	return v;
        }

        , htmlRenderer : function(format){
            return function(v){
                return Sbi.qbe.commons.Format.html(v, format);
            };
        }

        , getFormatFromJavaPattern: function(pattern) {
        	var toReturn = {};
        	if (pattern === undefined || pattern === null || pattern.trim() === '') {
        		return toReturn;
        	}

        	var decimalSeparatorIndex = pattern.indexOf(".");
        	if (decimalSeparatorIndex !== -1) {
        		toReturn.decimalPrecision = (pattern.length - decimalSeparatorIndex) - 1;
        	} else {
        		toReturn.decimalPrecision = 0;
        		decimalSeparatorIndex = pattern.length;
        	}

        	var groupingSeparatorIndex = pattern.lastIndexOf(",");
        	if (groupingSeparatorIndex !== -1) {
        		toReturn.groupingSize = (decimalSeparatorIndex - 1) - groupingSeparatorIndex;
        	} else {
        		toReturn.groupingSize = Number.MAX_VALUE;
        	}

        	return toReturn;

        }


        , stringToBoolean : function(string) {
			switch (string.toLowerCase()) {
				case "true":
				case "yes":
				case "1":
					return true;
				case "false":
				case "no":
				case "0":
				case null:
					return false;
				default:
					return Boolean(string);
			}
        }

		,
		escapeRegExp : function (str) {
			return str.replace(regex, "\\$&");
		}

	};

}();




