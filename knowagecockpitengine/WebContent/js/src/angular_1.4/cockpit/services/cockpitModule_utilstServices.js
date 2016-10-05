angular.module("cockpitModule").service("cockpitModule_utilstServices",function(cockpitModule_analyticalDrivers){
	var ut=this;
	
	this.getParameterValue=function(textVal){
		if(textVal!=undefined){
			var adVal=angular.copy(textVal);
			angular.forEach(cockpitModule_analyticalDrivers,function(val,item){
				var reg = new RegExp('\\$P\\{('+item+')\\}','g')
				adVal=adVal.replace(reg, val);
			})
			var reg2 = new RegExp('\\$P\\{[^\\}]*\\}','g');
			adVal=adVal.replace(reg2, "");
			
			return adVal;
		} 
	}
});

