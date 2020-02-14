/**
 *
 */

function addBusinessClassController($scope,$mdDialog,sbiModule_translate,businessModel,physicalModel,businessClassesGrid,metaModelServices,sbiModule_restServices){
	$scope.translate=sbiModule_translate;
	$scope.businessClassesGrid = businessClassesGrid;
	$scope.physicalModel=physicalModel;
	$scope.businessModel=businessModel;
	$scope.tmpBnssModel={physicalModel:{columns:[]},selectedColumns:[]};
	$scope.changePhYModel=function(){
		$scope.tmpBnssModel.selectedColumns=[];
	}
	$scope.bmTableColumns=[{label:sbiModule_translate.load("sbi.generic.name"),name:"name"}];


	 $scope.create = function() {
			var send = metaModelServices.createRequestRest($scope.tmpBnssModel);
			var obj2snd = {};
			obj2snd.data = {};

			obj2snd.data.physicalModel = send.data.physicalModel.name;
			obj2snd.data.selectedColumns = [];
			send.data.selectedColumns.forEach(function(entry) {
				obj2snd.data.selectedColumns.push(entry.name);
    		  });

			if (send.data.description !== undefined)
				obj2snd.data.description = send.data.description;
			else
				obj2snd.data.description = "";
			obj2snd.diff = send.diff;
			obj2snd.data.name = send.data.name;

			sbiModule_restServices.promisePost("1.0/metaWeb","addBusinessClass",obj2snd)
			.then(function(response){
				metaModelServices.applyPatch(response.data);
			    $mdDialog.hide();
			}
			,function(response){
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
			})
		};

		$scope.cancel = function() {
	    $mdDialog.cancel();
	  };
}