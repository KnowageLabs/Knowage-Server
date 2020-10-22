(function() {
	var documentExecutionModule = angular.module('driversExecutionModule');

	documentExecutionModule.directive('parameterViewPointHandler', ['sbiModule_config', function(sbiModule_config){
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.dynamicResourcesBasePath
				+ '/angular_1.4/tools/documentexecution/parameterViewPointHandler/parameterViewPointHandlerTemplate.jsp',
			controller: documentParamenterViewPointCtrl,
			scope: {
				execproperties: '=',
				execute: '='
			}
		};
	}]);


	var documentParamenterViewPointCtrl = function($scope, sbiModule_config, sbiModule_translate, $mdDialog,
			sbiModule_restServices, sbiModule_dateServices, sbiModule_messaging, driversExecutionService, driversDependencyService) {

		$scope.driversExecutionService = driversExecutionService;
		$scope.execProperties = $scope.execproperties;
		$scope.translate = sbiModule_translate;

		$scope.$watchCollection('driversExecutionService.gvpCtrlViewpoints', function(newValue, oldValue) {
			if (newValue && $scope.savedParametersGrid.api) {
				$scope.savedParametersGrid.api.setRowData(newValue);
			}
		})

		$scope.columns = [
			{"headerName":sbiModule_translate.load('sbi.generic.name'),"field":"vpName"},
			{"headerName":sbiModule_translate.load('sbi.generic.descr'),"field":"vpDesc"},
			{"headerName":sbiModule_translate.load('sbi.generic.visibility'),"field":"vpScope"},
			{"headerName":"",cellRenderer: buttonRenderer,"field":"valueId","cellClass":"singlePinnedButton","cellStyle":{"border":"none !important","text-align": "right","display":"inline-flex","justify-content":"center"},
				sortable:false,filter:false,width: 150,suppressSizeToFit:true,suppressMovable:true,pinned: 'right',resizable: false}];

		function buttonRenderer(params){
			return '<md-button class="md-icon-button" ng-click="fillParams(\''+params.data.vpId+'\')">'+
			'	<md-tooltip md-delay="500">'+sbiModule_translate.load("sbi.execution.parametersselection.executionbutton.fill.tooltip")+'</md-tooltip>'+
			'	<md-icon md-font-icon="fas fa-file-signature"></md-icon>'+
			'</md-button>'+
			'<md-button class="md-icon-button" ng-click="executeParams(\''+params.data.vpId+'\')">'+
			'	<md-tooltip md-delay="500">'+sbiModule_translate.load("sbi.execution.parametersselection.executionbutton.message")+'</md-tooltip>'+
			'	<md-icon md-font-icon="fa fa-play-circle"></md-icon>'+
			'</md-button>'+
			'<md-button class="md-icon-button" ng-click="deleteParams(\''+params.data.vpId+'\')">'+
			'	<md-tooltip md-delay="500">'+sbiModule_translate.load("sbi.generic.delete")+'</md-tooltip>'+
			'	<md-icon md-font-icon="fa fa-trash-o"></md-icon>'+
			'</md-button>';
		}

		$scope.savedParametersGrid = {
				angularCompileRows: true,
				pagination : true,
				paginationAutoPageSize: true,
		        onGridSizeChanged: resizeColumns,
		        defaultColDef: {
		        	sortable: true,
		        	filter: true,
		        	resizable: false
		        },
				columnDefs: $scope.columns,
				rowData: driversExecutionService.gvpCtrlViewpoints
		}

		function resizeColumns(){
			$scope.savedParametersGrid.api.sizeColumnsToFit();
		}

		function getItemFromId(id){
			for(var k in $scope.driversExecutionService.gvpCtrlViewpoints){
				if($scope.driversExecutionService.gvpCtrlViewpoints[k].vpId == id) return $scope.driversExecutionService.gvpCtrlViewpoints[k];
			}
		}

		$scope.fillParams = function(id){
			var item = getItemFromId(id);
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

		$scope.executeParams = function(id){
			var item = getItemFromId(id);
			var params = decodeRequestStringToJson(item.vpValueParams);
			//disable visual correlation
			 $scope.execProperties.initResetFunctionVisualDependency.status=false;
			 $scope.execProperties.initResetFunctionDataDependency.status=false;
			 $scope.execProperties.initResetFunctionLovDependency.status=false;
			 $scope.execProperties.returnFromDataDepenViewpoint.status = true;
			 $scope.execProperties.returnFromLovDepenViewpoint.status = true;
			 fillParametersPanel(params);
//			 docExecute_urlViewPointService.frameLoaded = false;
			 $scope.execute($scope.execProperties.selectedRole.name, stringfyFromGetUrlParameters(params));
			 $scope.returnToDocument();
		}

		$scope.deleteParams = function(id){
			var item = getItemFromId(id);
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
				if($scope.execProperties.executionInstance.OBJECT_TYPE_CODE){
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
								 $scope.savedParametersGrid.api.setRowData(driversExecutionService.gvpCtrlViewpoints)
							 }
					})
					.error(function(data, status, headers, config) {});
			}, function() {});
		}

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
			if($scope.execProperties.hasOwnProperty('documentUrl')) {
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