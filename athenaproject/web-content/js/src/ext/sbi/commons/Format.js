/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Properties
  * 
  * [list]
  * 
  * 
  * Public Methods
  * 
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * -  Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.commons");

Sbi.commons.Format = function(){
    //var trimRe = /^\s+|\s+$/g;
    
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
                return Sbi.commons.Format.date(v, format);
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
    				v = v.replace(new RegExp(format.groupingSeparator, 'g'), '');
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
        
        , numberRenderer : function(format){
            return function(v){
            	return '<div style=\'text-align: right;\'>' + Sbi.commons.Format.number(v, format) + '</div>';
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
        	
        	if(format.trim) v = Ext.util.Format.trim(v);
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
        	if(format.prefix) v = format.prefix+ v;
        	if(format.suffix) v =  v + format.suffix;
        	
        	return v;
        }
        
        , stringRenderer : function(format){
            return function(v){
                return Sbi.commons.Format.string(v, format);
            };
        }
        
        , 'boolean' : function(v, format) {
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
            	return Sbi.commons.Format['boolean'](v, format);
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
                return Sbi.commons.Format.html(v, format);
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
        
		/*
		 *  deprecated: why not to use the lib function Ext.util.JSON.encode(o)?
		 */
        , toString : function(o){
			var str = '{';
			for (p in o) {
				var obj = o[p];
				if (typeof obj == 'object') {
					str += p + ': ['
					for (count in obj) {
						var temp = obj[count];
						if (typeof temp == 'function') {
							continue;
						}
						if (typeof temp == 'string') {
							// the String.escape function escapes the passed string for ' and \
							temp = String.escape(temp);
							str += '\'' + temp + '\', ';
						} else if (typeof temp == 'date') {
							temp = Ext.util.Format.date(temp, Sbi.config.clientServerDateFormat);
							str += p + ': \'' +  temp + '\', ';
						} else {
							str += temp + ', ';
						}
					}
					// removing last ', ' string
					if (str.length > 1 && str.substring(str.length - 3, str.length - 1) == ', ') {
						str = str.substring(0, str.length - 3);
					}
					str += '], ';
				} else if (typeof obj == 'string') {
					// the String.escape function escapes the passed string for ' and \
					obj = String.escape(obj);
					str += p + ': \'' +  obj + '\', ';
				} else if (typeof obj == 'date') {
					obj = Ext.util.Format.date(obj, Sbi.config.clientServerDateFormat);
					str += p + ': \'' +  obj + '\', ';
				} else {
					// case number or boolean
					str += p + ': ' +  obj + ', ';
				}
			}
			if (str.length > 1 && str.substring(str.length - 3, str.length - 1) == ', ') {
				str = str.substring(0, str.length - 3);
			}
			str += '}';
			
			return str;
        }
	
		
		, toStringOldSyntax: function(o) {
			var str = '';
			for (p in o) {
				var obj = o[p];
				if (typeof obj == 'object') {
					str += p + '='
					for (count in obj) {
						var temp = obj[count];
						if (typeof temp == 'function') {
							continue;
						}
						if (typeof obj == 'string') {
							// the String.escape function escapes the passed string for ' and \
							temp = String.escape(temp);
							str += temp + ';'; // using ';' as separator between values (TODO: change separator)
						} else {
							str += temp + ';'; // using ';' as separator between values (TODO: change separator)
						}
					}
					// removing last ';' string
					if (str.length > 1 && str.substring(str.length - 2, str.length - 1) == ';') {
						str = str.substring(0, str.length - 2);
					}
					str += '&';
				} else if (typeof obj == 'string') {
					// the String.escape function escapes the passed string for ' and \
					obj = String.escape(obj);
					str += p + '=' +  obj + '&';
				} else {
					// case number or boolean
					str += p + '=' +  obj + '&';
				}
			}
			if (str.length > 1 && str.substring(str.length - 2, str.length - 1) == '&') {
				str = str.substring(0, str.length - 2);
			}
			
			return str;
		}
	}

       
}();