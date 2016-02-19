app.controller('kpiDefinitionCardinalityController', ['$scope','sbiModule_translate', kpiDefinitionCardinalityControllerFunction ]);

function kpiDefinitionCardinalityControllerFunction($scope,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.attributesList=[];
	$scope.checkedAttribute={"attributeUnion":{},"attributeIntersection":{}};
	$scope.measureList=[
	                	{	"nomeRegola": "regola1",
	                		"nomeMisura": "numScuole",
	                		"attributi": {"Regione":false,"Provincia":false,"Comune":false,"Tipologia":false
	                		}
	                	},
	                		{	"nomeRegola": "regola2",
	                		"nomeMisura": "numAbitanti",
	                		"attributi": {"Regione":false,"Provincia":false,"Comune":false,"FasciaEta":false,"CatLavoratore":false}
	                	},
	                		{	"nomeRegola": "regola3",
	                		"nomeMisura": "pilProCapite",
	                		"attributi": {"Regione":false,"FasciaEta":false,"CatLavoratore":false}
	                	},
	                		{	"nomeRegola": "regola4",
	                			"nomeMisura": "densPopolazione",
	                			"attributi": {"AreaGeografice":false}
	                	}
	                ];
	

	
	$scope.getAllMeasure=function(){
		for(var i=0; i<$scope.measureList.length;i++){
			for(var tmpAttr in $scope.measureList[i].attributi){
				
				if($scope.attributesList.indexOf(tmpAttr)==-1){
					$scope.attributesList.push(tmpAttr);
				}
			}
		}
	};
	$scope.getAllMeasure();
	
	$scope.toggleCell=function(attr,measure){
		
		if(!measure.attributi[attr] && !$scope.isEnabled(attr,measure)){
			return;
		}
		
		if(measure.attributi[attr] && !$scope.canDisable(attr,measure) ){
			return;
		}
		
		//toggle the value
		measure.attributi[attr]=!measure.attributi[attr];
		
		if(measure.attributi[attr]){
			//update union 
			if($scope.checkedAttribute.attributeUnion[attr]){
				$scope.checkedAttribute.attributeUnion[attr]++; 
			}else{
				$scope.checkedAttribute.attributeUnion[attr]=1;
			}
		}else{ 
			if($scope.checkedAttribute.attributeUnion[attr]==1){
				delete $scope.checkedAttribute.attributeUnion[attr]; 
			}else{
				$scope.checkedAttribute.attributeUnion[attr]--;
			}
		}
		
		
		//update intersection
		angular.copy({},$scope.checkedAttribute.attributeIntersection); // reset intersection
		var maxAttrNum=$scope.getMaxAttributeNumber($scope.checkedAttribute.attributeUnion);
		for(var key in $scope.checkedAttribute.attributeUnion){
			if($scope.checkedAttribute.attributeUnion[key]==maxAttrNum){
				$scope.checkedAttribute.attributeIntersection[key]=true;
			}
		}
		
	}
	$scope.getMaxAttributeNumber=function(data){
		var max=0;
		for(var key in data ){
			if(data[key]>=max){
				max=data[key];
			}
		}
		return max;
	}
	
	
	//hide box if measure not have attribute
	$scope.measureHaveAttribute=function(attr,measure){
		return measure.attributi.hasOwnProperty(attr);
	}
	
	$scope.isEnabled=function(attr,measure){
		var checkMs=$scope.checkMeasure(measure);
		return (checkMs.status || $scope.isContainedByUpperSet(attr,measure,checkMs.itemNumber) );
	}
	
	$scope.canDisable=function(attr,measure){
		return !$scope.isContainedByUnderSet(attr,measure);
	}
	
	//la mia misura contiene (selezionati) tutti gli ATTRIBUTI della cardinalità  (gli attributi del campo union)
	//IL MUMERO DI ATTRIBUTI SELEZIONATI DELLA MISURA è UGUALE AL NUMERO DI ELEMENTI  DELL' UNIONE
	$scope.checkMeasure=function(measure){
		var tot=0;
		for( var attr in measure.attributi){
			if(measure.attributi[attr]){
				tot++;
			}
		}
		var resp={
				status:( Object.keys($scope.checkedAttribute.attributeUnion).length==tot),
				itemNumber:tot
				};
		
		return resp;
		
	}
	
	$scope.isContainedByUpperSet=function(attr,measure,measureItemNumber){
		var upperSetAttributeNumber=99999999;
		var upperSet;
		for(var i=0;i<$scope.measureList.length;i++){
			var tmpMeas=$scope.measureList[i];
			if(tmpMeas==measure){
				continue
			}
			var tmpTot=0;
			for( var tmpAttr in tmpMeas.attributi){
				if(tmpMeas.attributi[tmpAttr]){
					tmpTot++;
				}
			}
			if(tmpTot-1==measureItemNumber){
				upperSet=tmpMeas;
				break;
			}
			if(tmpTot<upperSetAttributeNumber && tmpTot>measureItemNumber ){
				upperSetAttributeNumber=tmpTot;
				upperSet=tmpMeas;
			}
		}
		if(upperSet==undefined || upperSet.attributi[attr] ){
			return true;
		}else{
			return false;
		}
		
	}
	
	$scope.isContainedByUnderSet=function(attr,measure){
		var measureItemNumber=0;
		for( var tmpattr in measure.attributi){
			if(measure.attributi[tmpattr]){
				measureItemNumber++;
			}
		}
		
		var underSetAttributeNumber=0;
		var underSet;
		for(var i=0;i<$scope.measureList.length;i++){
			var tmpMeas=$scope.measureList[i];
			if(tmpMeas==measure){
				continue
			}
			var tmpTot=0;
			for( var tmpAttr in tmpMeas.attributi){
				if(tmpMeas.attributi[tmpAttr]){
					tmpTot++;
				}
			}
			if(tmpTot+1==measureItemNumber){
				underSet=tmpMeas;
				break;
			}
			if(tmpTot>underSetAttributeNumber && tmpTot<measureItemNumber ){
				underSetAttributeNumber=tmpTot;
				underSet=tmpMeas;
			}
		}
		
		if(underSet==undefined || !underSet.attributi[attr]){
			return false;
		}else{
			return true;
		}
	}
	
	
}