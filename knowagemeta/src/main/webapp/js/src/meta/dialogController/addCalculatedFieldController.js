function addCalculatedFieldController($scope, $mdDialog,sbiModule_translate,sbiModule_restServices, selectedBusinessModel,metaModelServices,editMode,currentCF){
	//synchronize model before creating the calculated field
	var send = metaModelServices.createRequestRest();
	sbiModule_restServices.promisePost("1.0/metaWeb","updateModel",send)
	.then(function(response){
		metaModelServices.applyPatch(response.data);
	}
	,function(response){
		sbiModule_restServices.errorHandler(response.data,"");
	})



	$scope.translate=sbiModule_translate;
	$scope.selectedBusinessModel=selectedBusinessModel;
	$scope.type=[{label:sbiModule_translate.load("sbi.lookup.asString"),name:"STRING"},{label:sbiModule_translate.load("sbi.lookup.asNumber"),name:"NUMBER"}]

	$scope.calcField={expression:"",dataType:$scope.type[0].name};
	if(editMode){
		$scope.calcField.name=currentCF.name

		for(var i=0;i<currentCF.properties.length;i++){
			var key = Object.keys(currentCF.properties[i])[0];
			if(angular.equals(key,"structural.datatype")){
				$scope.calcField.dataType=currentCF.properties[i][key].value
			}
			if(angular.equals(key,"structural.expression")){
				$scope.calcField.expression=currentCF.properties[i][key].value
			}
		}
	}

	$scope.cancel = function(){
		$mdDialog.cancel();
	};
	$scope.createCalculatedField=function(){
		 var dataToSend=metaModelServices.createRequestRest($scope.calcField);
		 dataToSend.data.sourceTableName=selectedBusinessModel.uniqueName;
		 dataToSend.data.editMode=editMode;
		 if(editMode){
			 dataToSend.data.uniquename=currentCF.uniqueName;
		 }

		sbiModule_restServices.promisePost("1.0/metaWeb","setCalculatedField",dataToSend)
		.then(function(response){
			metaModelServices.applyPatch(response.data);
			$mdDialog.hide();
		}
		,function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.meta.business.calculatedField.create.error"))
		})
	}

	$scope.functions=[
		                  {
		                	  label:"+",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.sum"),
		                	  value:"+"
		                  },
		                  {
		                	  label:"-",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.subtraction"),
		                	  value:"-"
		                  },
		                  {
		                	  label:"*",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.multiplication"),
		                	  value:"*"
		                  },
		                  {
		                	  label:"/",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.division"),
		                	  value:"/"
		                  },
		                  {
		                	  label:"||",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.or"),
		                	  value:"||"
		                  }
	                  ];
	$scope.dateFunctions=[
		                  {
		                	  label:"< GG <",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.GG_between_dates"),
		                	  value:"GG_between_dates"
		                  },
		                  {
		                	  label:"< MM <",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.MM_between_dates"),
		                	  value:"MM_between_dates"
		                  },
		                  {
		                	  label:"< AA <",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.AA_between_dates"),
		                	  value:"AA_between_dates"
		                  },
		                  {
		                	  label:"GG++",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.GG_up_today"),
		                	  value:"GG_up_today"
		                  },
		                  {
		                	  label:"MM++",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.MM_up_today"),
		                	  value:"MM_up_today"
		                  },
		                  {
		                	  label:"AA++",
		                	  name:sbiModule_translate.load("sbi.meta.business.calculatedField.AA_up_today"),
		                	  value:"AA_up_today"
		                  }
	                  ];


	$scope.addCol=function(col){
		$scope.calcField.expression+=col.name;
	};
	$scope.addFunc=function(func){
		$scope.calcField.expression+=func.value;
	}
}