function addCalculatedFieldController($scope, $mdDialog,sbiModule_translate,sbiModule_restServices, selectedBusinessModel,metaModelServices){
	$scope.translate=sbiModule_translate;
	$scope.selectedBusinessModel=selectedBusinessModel;
	$scope.type=[{label:"String",name:"String"},{label:"Number",name:"Number"}]
	$scope.calcField={expression:"",dataType:$scope.type[0].name};
	$scope.cancel = function(){
		$mdDialog.cancel();
	};
	$scope.createCalculatedField=function(){
		 var dataToSend=metaModelServices.createRequestRest($scope.calcField);
		 dataToSend.data.sourceTableName=selectedBusinessModel.uniqueName;
		sbiModule_restServices.promisePost("1.0/metaWeb","setCalculatedField",dataToSend)
		.then(function(response){
			metaModelServices.applyPatch(response.data);
			$mdDialog.hide();
		}
		,function(response){
			sbiModule_restServices.errorHandler(response.data,"Error while try to create calculated field")
		})
	}

	$scope.functions=[
		                  {
		                	  label:"+",
		                	  name:"sum",
		                	  value:"+"
		                  },
		                  {
		                	  label:"-",
		                	  name:"sum",
		                	  value:"-"
		                  },
		                  {
		                	  label:"*",
		                	  name:"sum",
		                	  value:"*"
		                  },
		                  {
		                	  label:"/",
		                	  name:"sum",
		                	  value:"/"
		                  },
		                  {
		                	  label:"||",
		                	  name:"sum",
		                	  value:"||"
		                  }
	                  ];
	$scope.dateFunctions=[
		                  {
		                	  label:"< GG <",
		                	  name:"GG_between_dates",
		                	  value:"GG_between_dates"
		                  },
		                  {
		                	  label:"< MM <",
		                	  name:"MM_between_dates",
		                	  value:"MM_between_dates"
		                  },
		                  {
		                	  label:"< AA <",
		                	  name:"AA_between_dates",
		                	  value:"AA_between_dates"
		                  },
		                  {
		                	  label:"GG++",
		                	  name:"GG_up_today",
		                	  value:"GG_up_today"
		                  },
		                  {
		                	  label:"MM++",
		                	  name:"MM_up_today",
		                	  value:"MM_up_today"
		                  },
		                  {
		                	  label:"AA++",
		                	  name:"AA_up_today",
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