(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');
	
	documentExecutionModule.directive('parameterViewPointHandler', ['sbiModule_config', function(sbiModule_config){
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.contextName 
				+ '/js/src/angular_1.4/tools/documentexecution/parameterViewPointHandler/parameterViewPointHandlerTemplate.jsp',
			controller: documentParamenterViewPointCtrl,
		};
	}]);
	
	
	var documentParamenterViewPointCtrl = function($scope, sbiModule_config, sbiModule_translate, documentExecuteServices, $mdDialog
			,sbiModule_restServices,docExecute_urlViewPointService,execProperties,docExecute_paramRolePanelService) {
						
		
		$scope.gvpCtrlVpSpeedMenuOpt = 
			[ 			 		               	
			 { // Fill Form
				 label: sbiModule_translate.load("sbi.execution.parametersselection.executionbutton.fill.tooltip"),
				 icon:"fa fa-check",
				 color:'#222222',
				 action : function(item) { 
					 //var params = documentExecuteServices.decodeRequestStringToJson(decodeURIComponent(item.vpValueParams));
					 //console.log('item ' , item);
					 var params = documentExecuteServices.decodeRequestStringToJson(item.vpValueParams);
					 fillParametersPanel(params);
					 docExecute_paramRolePanelService.returnToDocument();
				 }	
			 },
			 { //Execute Url
				 label: sbiModule_translate.load("sbi.execution.parametersselection.executionbutton.message"),
				 icon:"fa fa-play",
				 color:'#222222',
				 action : function(item) {
					 //decodeURIComponent						 		               		
					 //var params = documentExecuteServices.decodeRequestStringToJson(decodeURIComponent(item.vpValueParams));
					 var params = documentExecuteServices.decodeRequestStringToJson(item.vpValueParams);
					 fillParametersPanel(params);
					 docExecute_urlViewPointService.executionProcesRestV1(execProperties.selectedRole.name, JSON.stringify(params));
					 docExecute_paramRolePanelService.returnToDocument();
				 }	
			 }
			 ,{   //Delete Action
				 label: sbiModule_translate.load("sbi.generic.delete"),
				 icon:"fa fa-trash-o",
				 //backgroundColor:'red',
				 color:'#222222',
				 action : function(item) {
					 var confirm = $mdDialog
						.confirm()
						.title(sbiModule_translate.load("sbi.execution.parametersselection.delete.filters.title"))
						.content(
							sbiModule_translate
							.load("sbi.execution.parametersselection.delete.filters.message"))
							.ok(sbiModule_translate.load("sbi.general.continue"))
							.cancel(sbiModule_translate.load("sbi.general.cancel")
						);
					$mdDialog.show(confirm).then(function() {
						var index =docExecute_urlViewPointService.gvpCtrlViewpoints.indexOf(item);
						 var objViewpoint = JSON.parse('{ "VIEWPOINT" : "'+ item.vpId +'"}');
							sbiModule_restServices.post(
									"1.0/documentviewpoint",
									"deleteViewpoint", objViewpoint)
							   .success(function(data, status, headers, config) {
								   if(data.errors && data.errors.length > 0 ){
									   documentExecuteServices.showToast(data.errors[0].message);
									 }else{
										 docExecute_urlViewPointService.gvpCtrlViewpoints.splice(index, 1);
											 //message success 
									 }
								   //gvpctl.selectedParametersFilter = [];
							})
							.error(function(data, status, headers, config) {});
//							$scope.getViewpoints();
					}, function() {
						console.log('Annulla');
						//docExecute_urlViewPointService.getViewpoints();
					});	
				 }
			 } 	
		 ];
		
		
		/*
		 * Fill Parameters Panel 
		 */
		function fillParametersPanel(params){
			console.log('Load filter params : ' , params);
			if(execProperties.parametersData.documentParameters.length > 0){
				for(var i = 0; i < execProperties.parametersData.documentParameters.length; i++){
					var parameter = execProperties.parametersData.documentParameters[i];
					
					if(!params[parameter.urlName]) {
						documentExecuteServices.resetParameter(parameter);
					} else {
						//Type params
						if(parameter.selectionType.toLowerCase() == 'tree'){
							//TREE
							console.log('Param ' , parameter.parameterValue);
							console.log('param to set ' , execProperties.parametersData.documentParameters[i]); 
							//parameter.parameterValue = params[parameter.urlName];
							//TODO FOR Benedetto 											
//							execProperties.parametersData.documentParameters[i].parameterValue = toReturn;							
						}else{	
							if(parameter.type=='NUM'){
								parameter.parameterValue = parseFloat(params[parameter.urlName],10);
							}else if(parameter.type=='STRING'){
								parameter.parameterValue = params[parameter.urlName];
								if(parameter.defaultValues && parameter.defaultValues.length > 0) {
									var parameterValues = parameter.parameterValue;
									//console.log('param to set .... ' , parameterValues);
									var parArr = parameterValues.split(';');
									//console.log('parArr ' , parArr);
									for(var j = 0; j < parameter.defaultValues.length; j++) {
										var defaultValue = parameter.defaultValues[j];
										for(var k = 0; k < parArr.length; k++) {
											if(defaultValue.value == parArr[k]) {
												//TODO SET PARAMETERS !!
												defaultValue.isSelected = true;
												break;
											} else {
												defaultValue.isSelected = false;
											}											
										}
									}
								}
							}	
						}						
					}
				}
			}			
		};
	};
})();