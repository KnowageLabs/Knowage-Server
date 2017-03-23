angular.module("cockpitModule").service("cockpitModule_utilstServices",function(cockpitModule_analyticalDrivers){
	var ut=this;
	
	this.getParameterValue=function(textVal){
		if(textVal!=undefined){
			var adVal=angular.copy(textVal);
			if(angular.isString(adVal)){
				angular.forEach(cockpitModule_analyticalDrivers,function(val,item){
					
					var valRegExp = new RegExp('\\{;\\{(.*)\\}(.*)\\}');
					// if it is multivalue
					if(angular.isString(val) && valRegExp.test(val)){
					
						var toParse;
						var type = "STRING";
						//if there is description take description
						var description = cockpitModule_analyticalDrivers[item+'_description'];
						if(description != undefined){
							toParse = description;
						}
						else{
							matches = val.match(valRegExp);
							toParse = matches[1];
							type = matches[2];
						}			
						var elements = toParse.split(";");
						var delimiter = (type == "STRING") ? "'" : ""; 
						var strings = [];
						angular.forEach(elements,function(value){
							this.push(delimiter + value + delimiter);
						},strings);
						val = strings.join(",");
					}
					else{
						// if it is single value take description if present
						var description = cockpitModule_analyticalDrivers[item+'_description'];
						if(description != undefined){
							val = description;
						}
					}
					var reg = new RegExp('\\$P\\{('+item+')\\}','g');
					adVal=adVal.replace(reg, val);
				})
				var reg2 = new RegExp('\\$P\\{[^\\}]*\\}','g');
				adVal=adVal.replace(reg2, "");
			}
			
			return adVal;
		} 
	}
});

