Utils = {};

Utils.version = "1.0.0";

Utils.getVersion = function(){
  return this.version;
}

Utils.svgNS = 'http://www.w3.org/2000/svg';
Utils.xlinkNS = 'http://www.w3.org/1999/xlink';

Utils.find = function(a, pname, pvalue) {
  var result;
  for(i = 0; i < a.length; i++) {
    if(a[i][pname] === pvalue) {
      result = a[i];
      break;
    }
  }
  
  return result;
};


Utils.apply = function(o, c, defaults){
    if(defaults){
        // no "this" reference for friendly out of scope calls
        Utils.apply(o, defaults);
    }
    
    if(o && c && typeof c == 'object'){
        for(var p in c){
            o[p] = c[p];
        }
    }
    
    return o;
};

Utils.applyAttributes = function(o, c, defaults){
    if(defaults){
        Utils.applyAttributes(o, defaults);
    }
    
    if(o && c && typeof c == 'object'){
        for(var p in c){
            o.setAttributeNS(null, p, c[p]);
        }
    }
    
    return o;
};

Utils.toStr = function(o) {
    var str = "";
			
		if(o === 'undefined') return 'undefined';
			
		str += "Type: [" + typeof(o) + "]\n------------------------\n";
			
	  for(p in o) {
	   str += p + ": " +  o[p] + "\n";
	  }
  
    return str;
};
		
Utils.dump = function(o) {
		alert(this.toStr(o));
};


Utils.roundDecimals = function (originalNumber, decimals) {  
    var result1 = originalNumber * Math.pow(10, decimals);
	  var result2 = Math.round(result1);
    var result3 = result2 / Math.pow(10, decimals);
	
    return Utils.padWithZeros(result3, decimals);
};

Utils.padWithZeros = function(rounded_value, decimal_places) {
		
    // Convert the number to a string
		var value_string = rounded_value.toString();
		   
		// Locate the decimal point
		var decimal_location = value_string.indexOf(".");
		
    // Is there a decimal point?
		if (decimal_location == -1) {
  		// If no, then all decimal places will be padded with 0s
  		decimal_part_length = 0;
  		
      // If decimal_places is greater than zero, tack on a decimal point
  		value_string += decimal_places > 0 ? "." : "";
		} else {		
		  // If yes, then only the extra decimal places will be padded with 0s
		  decimal_part_length = value_string.length - decimal_location - 1;
		}
		   
		// Calculate the number of decimal places that need to be padded with 0s
		var pad_total = decimal_places - decimal_part_length;
		if (pad_total > 0) {
		 		// Pad the string with 0s
		 		for (var counter = 1; counter <= pad_total; counter++) value_string += "0";
		}
		
    return value_string;
};

Utils.addSeparators = function(numberStr) {
    
    var chunk = numberStr.split('.');
    var s = '';
    for(var i = chunk[0].length; i > 0; i = i - 3 ) {
        var separator = s.length === 0? '': sbi.geo.conf.locale.groupingSeparator;
        var lb = i-3>0? i-3 : 0;
        s = chunk[0].substring(lb, i) + separator + s;
    }
    
    if(chunk.length > 1)  {
        s = s + sbi.geo.conf.locale.decimalSeparator + chunk[1];
    }
    
   return s;
};

Utils.numberToString = function(rounded_value, decimals) {
  	var result = null;
  	var value_string = rounded_value.toString();
  	var decimal_location = value_string.indexOf(".");		
  	
    if (decimal_location != -1) {
        var value_float = parseFloat(value_string);
       	result = Utils.roundDecimals(value_float, decimals);
    } else {                       	
        result = value_string;
    }
    
    result = Utils.addSeparators(result);
             
    return result;
};
