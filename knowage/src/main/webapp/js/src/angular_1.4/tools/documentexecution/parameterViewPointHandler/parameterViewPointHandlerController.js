(function() {
	var documentExecutionModule = angular.module('driversExecutionModule');

	documentExecutionModule.directive('parameterViewPointHandler', ['sbiModule_config', function(sbiModule_config){
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.contextName
				+ '/js/src/angular_1.4/tools/documentexecution/parameterViewPointHandler/parameterViewPointHandlerTemplate.jsp',
			controller: documentParamenterViewPointCtrl,
			scope: {
				execproperties: '=',
				execute: '='
			}
		};
	}]);


	var documentParamenterViewPointCtrl = function($scope, sbiModule_config, sbiModule_translate, $mdDialog,
			sbiModule_restServices, sbiModule_messaging, driversExecutionService, driversDependencyService) {

		$scope.driversExecutionService = driversExecutionService;
		$scope.execProperties = $scope.execproperties;
		$scope.translate = sbiModule_translate;

		$scope.gvpCtrlVpSpeedMenuOpt =
			[
			 { // Fill Form
				 label: sbiModule_translate.load("sbi.execution.parametersselection.executionbutton.fill.tooltip"),
				 icon:"fa fa-check",
				 color:'#222222',
				 action : function(item) {
					 //var params = documentExecuteServices.decodeRequestStringToJson(decodeURIComponent(item.vpValueParams));
					 //console.log('item ' , item);
					 var params = decodeRequestStringToJson(item.vpValueParams);
					 //disable visual correlation
					 $scope.execProperties.initResetFunctionVisualDependency.status=false;
					 $scope.execProperties.initResetFunctionDataDependency.status=false;
					 $scope.execProperties.initResetFunctionLovDependency.status=false;
					 $scope.execProperties.returnFromDataDepenViewpoint.status = true;
					 $scope.execProperties.returnFromLovDepenViewpoint.status = true;
					 fillParametersPanel(params);
					 $scope.returnToDocument();
				 }
			 },
			 { //Execute Url
				 label: sbiModule_translate.load("sbi.execution.parametersselection.executionbutton.message"),
				 icon:"fa fa-play",
				 color:'#222222',
				 action : function(item) {
					 //decodeURIComponent
					 //var params = documentExecuteServices.decodeRequestStringToJson(decodeURIComponent(item.vpValueParams));
					 var params = decodeRequestStringToJson(item.vpValueParams);
					//disable visual correlation
					 $scope.execProperties.initResetFunctionVisualDependency.status=false;
					 $scope.execProperties.initResetFunctionDataDependency.status=false;
					 $scope.execProperties.initResetFunctionLovDependency.status=false;
					 $scope.execProperties.returnFromDataDepenViewpoint.status = true;
					 $scope.execProperties.returnFromLovDepenViewpoint.status = true;
					 fillParametersPanel(params);
//					 docExecute_urlViewPointService.frameLoaded = false;
					 $scope.execute($scope.execProperties.selectedRole.name, stringfyFromGetUrlParameters(params));
					 $scope.returnToDocument();
				 }
			 }
			 ,{   //Delete Action
				 label: sbiModule_translate.load("sbi.generic.delete"),
				 icon:"fa fa-trash-o",
				 color:'#222222',
				 action : function(item) {
					 var confirm = $mdDialog
						.confirm({skipHide: true})
						.title(sbiModule_translate.load("sbi.execution.parametersselection.delete.filters.title"))
						.content(
							sbiModule_translate
							.load("sbi.execution.parametersselection.delete.filters.message"))
							.ok(sbiModule_translate.load("sbi.general.continue"))
							.cancel(sbiModule_translate.load("sbi.general.cancel")
						);
					$mdDialog.show(confirm).then(function() {
						var index = driversExecutionService.gvpCtrlViewpoints.indexOf(item);
						if($scope.execProperties.documentUrl){
							var deletePath = "1.0/documentviewpoint";
						} else {
							var deletePath = "1.0/metamodelviewpoint";
						}

						 var objViewpoint = JSON.parse('{ "VIEWPOINT" : "'+ item.vpId +'"}');
							sbiModule_restServices.post(deletePath,	"deleteViewpoint", objViewpoint)
							   .success(function(data, status, headers, config) {
								   if(data.errors && data.errors.length > 0 ){
									   showToast(data.errors[0].message);
									 }else{
										 driversExecutionService.gvpCtrlViewpoints.splice(index, 1);
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


		function stringfyFromGetUrlParameters(params){
			 var prm = {};
			 for (key in params) {
				    if (params.hasOwnProperty(key)) {
				        try {
				        	prm[key]=JSON.parse(params[key]);
				        } catch (e) {
				        	prm[key]=params[key];
				        }
				    }
				}

			 return prm;
		};

		function decodeRequestStringToJson(str) {
			var parametersJson = {};

			var arrParam = str.split('%26');
			for(var i=0; i<arrParam.length; i++){
				var arrJsonElement = arrParam[i].split('%3D');
				parametersJson[arrJsonElement[0]]=arrJsonElement[1];
			}
			return parametersJson;
		};

		function showToast(text, time) {
			var timer = time == undefined ? 6000 : time;
			sbiModule_messaging.showInfoMessage(text,"");
		};

		$scope.returnToDocument = function() {
			if($scope.execProperties.documentUrl) {
				$scope.execProperties.currentView.status = 'DOCUMENT';
			} else {
				$scope.execProperties.currentView.status = 'BUSINESSMODEL';
			}
			$scope.execProperties.parameterView.status='';
			$scope.execProperties.isParameterRolePanelDisabled.status = checkParameterRolePanelDisabled();
			$scope.execProperties.returnFromVisualViewpoint.status = true;
		};

		function checkParameterRolePanelDisabled() {
			return ((!$scope.execProperties.parametersData.documentParameters || $scope.execProperties.parametersData.documentParameters.length == 0)
					&& ($scope.execProperties.roles.length==1));
		};

		/*
		 * Fill Parameters Panel
		 */
		function fillParametersPanel(params){
			if($scope.execProperties.parametersData.documentParameters.length > 0){
				for(var i = 0; i < $scope.execProperties.parametersData.documentParameters.length; i++){
					var parameter = $scope.execProperties.parametersData.documentParameters[i];
					if(!params[parameter.urlName] || params[parameter.urlName] == "[]") {
						driversExecutionService.resetParameter(parameter);
					} else {
						if(parameter.valueSelection=='lov') {
							if(parameter.selectionType.toLowerCase() == "tree" || parameter.selectionType.toLowerCase() == "lookup") {
								//TREE DESC FOR LABEL
								var ArrValue = JSON.parse(params[parameter.urlName]);
								if (typeof parameter.parameterDescription === 'undefined'){
									parameter.parameterDescription = {};
								}
								if(params[parameter.urlName+'_field_visible_description']!=undefined) {
									var ArrDesc = params[parameter.urlName+'_field_visible_description'].split(';');
									for(var w=0; w<ArrValue.length; w++){
										parameter.parameterDescription[ArrValue[w]] =ArrDesc[w];
									}
									parameter.parameterValue = ArrValue;
								}
							} else {
								//FROM VIEWPOINT : the lov value saved (multivalue or single value) matched  with the parameter
								parameter.parameterValue = parameter.multivalue ? JSON.parse(params[parameter.urlName])	: params[parameter.urlName];
							}
						} else if(parameter.valueSelection.toLowerCase() == 'map_in') {
							var valueToBeCleanedByQuotes = params[parameter.urlName].replace(/^'(.*)'$/g, '$1');
							var valueToBeSplitted = valueToBeCleanedByQuotes.split("','");
							parameter.parameterValue = (parameter.multivalue)? valueToBeSplitted : valueToBeCleanedByQuotes;
						} else {
							if(parameter.type=='NUM'){
								if (parameter.multivalue){
									var values = params[parameter.urlName].split(",");
									parameter.parameterValue = "";
									for (var v=0; v<values.length; v++){
										parameter.parameterValue += parseFloat(values[v],10);
										if (v < (values.length-1)) parameter.parameterValue += ",";
									}
								}else
									parameter.parameterValue = parseFloat(params[parameter.urlName],10);
							}else if(parameter.type=='DATE'){
								//set parameter date server
								if (parameter.multivalue){
									var values = params[parameter.urlName].split(",");
									parameter.parameterValue = "";
									for (var v=0; v<values.length; v++){
										var res = sbiModule_dateServices.getDateFromFormat(values[v], sbiModule_config.serverDateFormat);
										parameter.parameterValue += sbiModule_dateServices.formatDate(res, sbiModule_config.serverDateFormat); //convert to string
										if (v < (values.length-1)) parameter.parameterValue += ",";
									}
								}else
									parameter.parameterValue = sbiModule_dateServices.getDateFromFormat(params[parameter.urlName], sbiModule_config.serverDateFormat);
							}else if(parameter.type=='DATE_RANGE'){
								var dateRange = params[parameter.urlName];
								var dateRangeArr = dateRange.split('_');
								var range = dateRangeArr[1];
								dateRange = dateRangeArr[0];
								if (dateRange === parseInt(dateRange)){
									//FROM DEFAULT
									parameter.parameterValue= new Date(parseInt(dateRange));
								}else{
									//FROM VIEWPOINT
									parameter.parameterValue= sbiModule_dateServices.getDateFromFormat(dateRange, sbiModule_config.serverDateFormat);
								}
								if(typeof parameter.datarange ==='undefined'){
									parameter.datarange = {};
								}
								parameter.datarange.opt=serviceScope.convertDateRange(range);
							}
							else if(parameter.type=='STRING'){
								parameter.parameterValue = params[parameter.urlName];
								if(parameter.defaultValues && parameter.defaultValues.length > 0) {
									var parameterValues = parameter.parameterValue;
									var parArr = parameterValues.split(';');
									for(var j = 0; j < parameter.defaultValues.length; j++) {
										var defaultValue = parameter.defaultValues[j];
										for(var k = 0; k < parArr.length; k++) {
											if(defaultValue.value == parArr[k]) {
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
					driversDependencyService.updateVisualDependency(parameter,$scope.execProperties);
				}
			}
		};

	};
})();