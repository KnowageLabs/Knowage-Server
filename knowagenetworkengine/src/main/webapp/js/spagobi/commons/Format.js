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
  * Public Functions
  * 
  *  [list]
  * 
  * 
  * Authors
  * 
  * - Antonella Giachino (antonella.giachino@eng.it)
  */


Ext.define('Sbi.network.commons.Format', {
	statics: {
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
                return Sbi.network.commons.Format.date(v, format);
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

        	if(!v) {
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
                return Sbi.network.commons.Format.number(v, format);
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
                return Sbi.network.commons.Format.string(v, format);
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
            } else if(v === true){
            	 v = format.falseSymbol;
            } else {
            	 v = format.nullValue;
            }
        	
        	return v;
        }
        
        
        , booleanRenderer : function(format){
            return function(v){
                return Sbi.network.commons.Format['boolean'](v, format);
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
                return Sbi.network.commons.Format.html(v, format);
            };
        }
        
	} 
});

	







	