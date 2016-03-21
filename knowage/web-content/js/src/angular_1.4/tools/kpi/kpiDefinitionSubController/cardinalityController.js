app.controller('kpiDefinitionCardinalityController', ['$scope','sbiModule_translate', kpiDefinitionCardinalityControllerFunction ]);


function kpiDefinitionCardinalityControllerFunction($scope,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.attributesList=[];
	$scope.indexOfMeasure=-1;
	$scope.currentCell={};


	$scope.createFormulaToShow = function(){
		var string = $scope.kpi.definition.formulaSimple.split(" ");
		var count =0;
		for(var i=0;i<string.length;i++){
			if(string[i].trim()=="+" ||string[i].trim()=="-" || string[i].trim()=="/" || string[i].trim()=="*" || string[i].trim()=="(" ||
					string[i].trim()==")" || string[i].trim()=="" || !isNaN(string[i])){
				var span = "<span class='showFormula'>"+" "+string[i]+" "+"</span>"
				angular.element(document.getElementsByClassName("formula")).append(span);
			}else{

				var span = "<span ng-class='{classBold:currentCell.row=="+i+"}' class='showFormula "+$scope.kpi.definition.functions[count]+"' id=M"+count+">"+" "+string[i]+" "+"</span>"
				angular.element(document.getElementsByClassName("formula")).append(span);
				count++;
			}

		}

	}
	
	$scope.$on('activateCardinalityEvent',function(e){
		$scope.attributesList=[];
		$scope.getAllMeasure();
		$scope.clearFormulaToShow();
		if($scope.kpi.cardinality.measureList.length>0){
			$scope.createFormulaToShow();
		}

	})


	$scope.$on('nullCardinalityEvent',function(e){
		$scope.clearFormulaToShow();
		$scope.attributesList=[];
		$scope.indexOfMeasure=-1;
		$scope.currentCell={};
	})
	$scope.clearFormulaToShow = function(){
		angular.element(document.getElementsByClassName("showFormula")).remove();


	}

	$scope.getAllMeasure=function(){
		if($scope.kpi.cardinality!=undefined && $scope.kpi.cardinality!=null){
			if(Object.keys($scope.kpi.cardinality).length!=0){
				if(!angular.isObject($scope.kpi.cardinality)){
					$scope.kpi.cardinality=JSON.parse($scope.kpi.cardinality);
				}
				
				for(var i=0; i<$scope.kpi.cardinality.measureList.length;i++){
					for(var tmpAttr in $scope.kpi.cardinality.measureList[i].attributes){

						if($scope.attributesList.indexOf(tmpAttr)==-1){
							$scope.attributesList.push(tmpAttr);
						}
					}
				}
			}
		}
		

	};
	$scope.getAllMeasure();

	$scope.toggleCell=function(attr,measure){

		if(!measure.attributes[attr] && !$scope.isEnabled(attr,measure)){
			return;
		}
		if(measure.attributes[attr] && !$scope.canDisable(attr,measure) ){
			return;
		}

		//toggle the value
		measure.attributes[attr]=!measure.attributes[attr];

		if(measure.attributes[attr]){
			//update union 
			if($scope.kpi.cardinality.checkedAttribute.attributeUnion[attr]){
				$scope.kpi.cardinality.checkedAttribute.attributeUnion[attr]++; 
			}else{
				$scope.kpi.cardinality.checkedAttribute.attributeUnion[attr]=1;
			}
		}else{ 
			if($scope.kpi.cardinality.checkedAttribute.attributeUnion[attr]==1){
				delete $scope.kpi.cardinality.checkedAttribute.attributeUnion[attr]; 
			}else{
				$scope.kpi.cardinality.checkedAttribute.attributeUnion[attr]--;
			}
		}


		//update intersection
		angular.copy({},$scope.kpi.cardinality.checkedAttribute.attributeIntersection); // reset intersection
		var maxAttrNum=$scope.getMaxAttributeNumber($scope.kpi.cardinality.checkedAttribute.attributeUnion);
		for(var key in $scope.kpi.cardinality.checkedAttribute.attributeUnion){
			if($scope.kpi.cardinality.checkedAttribute.attributeUnion[key]==maxAttrNum){
				$scope.kpi.cardinality.checkedAttribute.attributeIntersection[key]=true;
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


	//hide box if measure not has attribute
	$scope.measureHaveAttribute=function(attr,measure){
		return measure.attributes.hasOwnProperty(attr);
	}

	$scope.isEnabled=function(attr,measure){
		var checkMs=$scope.checkMeasure(measure);
		return (checkMs.status || $scope.isContainedByUpperSet(attr,measure,checkMs.itemNumber) );
	}

	$scope.canDisable=function(attr,measure){
		return !$scope.isContainedByUnderSet(attr,measure);
	}

	$scope.checkMeasure=function(measure){
		var tot=0;
		for( var attr in measure.attributes){
			if(measure.attributes[attr]){
				tot++;
			}
		}
		var resp={
				status:( Object.keys($scope.kpi.cardinality.checkedAttribute.attributeUnion).length==tot),
				itemNumber:tot
		};

		return resp;

	}

	$scope.isContainedByUpperSet=function(attr,measure,measureItemNumber){
		var upperSetAttributeNumber=99999999;
		var upperSet;
		for(var i=0;i<$scope.kpi.cardinality.measureList.length;i++){
			var tmpMeas=$scope.kpi.cardinality.measureList[i];
			if(tmpMeas==measure){
				continue
			}
			var tmpTot=0;
			for( var tmpAttr in tmpMeas.attributes){
				if(tmpMeas.attributes[tmpAttr]){
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
		if(upperSet==undefined || upperSet.attributes[attr] ){
			return true;
		}else{
			return false;
		}

	}

	$scope.isContainedByUnderSet=function(attr,measure){
		var measureItemNumber=0;
		for( var tmpattr in measure.attributes){
			if(measure.attributes[tmpattr]){
				measureItemNumber++;
			}
		}

		var underSetAttributeNumber=0;
		var underSet;
		for(var i=0;i<$scope.kpi.cardinality.measureList.length;i++){
			var tmpMeas=$scope.kpi.cardinality.measureList[i];
			if(tmpMeas==measure){
				continue
			}
			var tmpTot=0;
			for( var tmpAttr in tmpMeas.attributes){
				if(tmpMeas.attributes[tmpAttr]){
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

		if(underSet==undefined || !underSet.attributes[attr]){
			return false;
		}else{
			return true;
		}
	}



	$scope.blinkMeasure=function(event,attr,index){
		//blink measure in formula 
		$scope.currentCell.row = attr;
		$scope.currentCell.column = index;
		$scope.indexOfMeasure  = index;
		string="M"+$scope.indexOfMeasure ;
		angular.element(document.getElementById(string)).css('background','#eceff1 ');


	}

	$scope.removeblinkMeasure=function(){
		string="M"+$scope.indexOfMeasure;
		angular.element(document.getElementById(string)).css('background','transparent');

	}
}