(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');
	
	documentExecutionModule.directive('documentOlap', ['sbiModule_config', function(sbiModule_config){
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.contextName 
				+ '/js/src/angular_1.4/tools/documentexecution/documentOlap/documentOlapTemplate.jsp',
			controller: documentOlapCtrl
		};
	}]);
	
	
	var documentOlapCtrl = function($scope, sbiModule_config, sbiModule_translate, documentExecuteServices, $mdDialog,
			sbiModule_restServices,	docExecute_urlViewPointService,execProperties,docExecute_paramRolePanelService,$filter,
			sbiModule_download) {

		$scope.column = [
                        {label:sbiModule_translate.load("sbi.generic.name"),name:"name"},
                        {label:sbiModule_translate.load("sbi.generic.descr"),name:"description"},
                        {label:sbiModule_translate.load("sbi.generic.scope"),name:"isPublic"}];

		$scope.closeFilter = function(){
			$mdDialog.cancel();
		}

		
		$scope.gvpCtrlOlapMenuOpt = [ 			 		               	
			 { // Fill Form
				 label: sbiModule_translate.load("sbi.generic.apply"),
				 icon:"fa fa-file-text-o",
				 color:'#222222',
				 action : function(item) { 
					 var para = {
						 "subobj_id":item.id,
						 "subobj_name":item.name,
						 "subobj_description":item.description,
						 "subobj_visibility":item.isPublic,
					 }
					 docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name, para);
					 docExecute_paramRolePanelService.returnToDocument();
					 
				}
			 }
			 ,{   //Delete Action
				 label: sbiModule_translate.load("sbi.generic.delete"),
				 icon:"fa fa-trash-o",
				 //backgroundColor:'red',
				 color:'#222222',
				 action : function(item, $index) {
					 sbiModule_restServices.promiseDelete("1.0/olapsubobjects", 'removeOlapSubObject', 
								"idObj=" + item.id)
						.then(function(response){  
							docExecute_urlViewPointService.olapList.splice($index,1);
						},function(response){
							sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.alert.load.error"));
						});
				 }
			 } 	
		 ];

	};
	
	
})();