var app = angular.module('kpiDefinitionManager', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror','color.picker','angular_list']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('kpiDefinitionMasterController', ['$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout',kpiDefinitionMasterControllerFunction ]);

function kpiDefinitionMasterControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout){
	$scope.translate=sbiModule_translate;
	//variables formula
	$scope.checkFormula = false;
	$scope.emptyKpi = {"name":"","definition":"",'id':undefined,cardinality:"{\"measureList\":[],\"checkedAttribute\":{}}",threshold:{description:"",thresholdValues:[]},placeholder:""};
	$scope.kpi = {"name":"","definition":"",'id':undefined};
	$scope.activeSave = "";
	$scope.AttributeCategoryList=[];
	$scope.showGUI=false;
	$scope.formulaModified={"value":false};
	$scope.kpiList=[];
	$scope.kpiListOriginal=[];
	$scope.selectedTab={'tab':0};
	$scope.thresholdTypeList=[];

	//variables placeholder
	$scope.placeHolderObjectList = {};
	$scope.placeHolderList=[];

	//variables cardinality
	$scope.cardinality={"measureList":[],"checkedAttribute":{}};



	//methods formula
	sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_KPI_CATEGORY")
	.then(function(response){ 
		angular.copy(response.data,$scope.AttributeCategoryList);
	},function(response){

	});

	$scope.parseFormula = function(){
		$scope.$broadcast ('parseEvent');

		if($scope.showGUI){
			$scope.showSaveGUI().then(function(response){{}
			if($scope.activeSave=="add"){
				$scope.saveKPI();
			}else{
				$scope.saveKPI();
			}

			});

		}


	}
	$scope.flagActivateBrother= function(event){
		$scope.$broadcast (event);
	}



	$scope.cancelMeasureFunction=function(){
		if(!angular.equals($scope.originalRule,$scope.currentRule)){
			var confirm = $mdDialog.confirm()
			.title($scope.translate.load("sbi.layer.modify.progress"))
			.content($scope.translate.load("sbi.layer.modify.progress.message.modify"))
			.ariaLabel('cancel metadata') 
			.ok($scope.translate.load("sbi.general.ok"))
			.cancel($scope.translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm).then(function() {

				$angularListDetail.goToList();
			}, function() {
				return;
			});
		}else{

			$angularListDetail.goToList();
		} 
	};
	$scope.cancel = function(){
		if($scope.formulaModified.value){
			var confirm = $mdDialog.confirm()
			.title($scope.translate.load("sbi.layer.modify.progress"))
			.content($scope.translate.load("sbi.layer.modify.progress.message.modify"))
			.ariaLabel('cancel metadata') 
			.ok($scope.translate.load("sbi.general.ok"))
			.cancel($scope.translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm).then(function() {
				$scope.formulaModified.value=false;
				$scope.cardinality.measureList=[];
				$scope.cardinality.checkedAttribute={"attributeUnion":{},"attributeIntersection":{}};
				$scope.$broadcast ('cancelEvent');
			}, function() {
				return;
			});
		}else{
			$scope.formulaModified.value=false;
			angular.copy({"measureList":[],"checkedAttribute":{}},$scope.cardinality);
			$scope.$broadcast('clearAllEvent');
			$scope.$broadcast ('cancelEvent');
		}




	}
	$scope.showSaveGUI= function(){
		var deferred = $q.defer();
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templatesaveKPI.html',
			clickOutsideToClose:true,
			preserveScope:true,
			locals: {items: deferred,AttributeCategoryList: $scope.AttributeCategoryList,kpi:$scope.kpi }
		})
		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
			return deferred.resolve($scope.selectedFunctionalities);
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
		return deferred.promise;
	}


	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	}
	$scope.parsePlaceholder = function(placeholder){
		for(var key in Object.keys( placeholder)){
			if( placeholder[Object.keys( placeholder)[key]]==""){
				delete  placeholder[Object.keys( placeholder)[key]];
			}
		}

	}


	$scope.saveKPI = function(){
		var tmpKpiToSave={};
		angular.copy($scope.kpi,tmpKpiToSave);

		if(angular.isObject(tmpKpiToSave.definition)){
			tmpKpiToSave.definition = JSON.stringify($scope.kpi.definition);
		}
		if(Object.keys(tmpKpiToSave.cardinality).length>0){
			tmpKpiToSave.cardinality=JSON.stringify($scope.cardinality);
		}
		if(angular.isObject(tmpKpiToSave.placeholder)){
			$scope.parsePlaceholder(tmpKpiToSave.placeholder);
			tmpKpiToSave.placeholder=JSON.stringify(tmpKpiToSave.placeholder);
		}
		$scope.convertThresholdToCorrectObject(tmpKpiToSave);
		sbiModule_restServices.post("1.0/kpi", 'saveKpi',tmpKpiToSave).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						$scope.showAction(data);
					} else {
						$scope.$broadcast ('savedEvent');
						$scope.formulaModified.value=false;
					}

				}).error(function(data, status, headers, config) {
					$scope.showAction(data);
				})


	}

	$scope.convertThresholdToCorrectObject=function(kpi){
		for(var i=0;i<kpi.threshold.thresholdValues.length;i++){
			delete kpi.threshold.thresholdValues[i].move;
			delete kpi.threshold.thresholdValues[i].inputLable;
			delete kpi.threshold.thresholdValues[i].includeNumericInputMin;
			delete kpi.threshold.thresholdValues[i].includeNumericInputMax;
			delete kpi.threshold.thresholdValues[i].includeMinCheck;
			delete kpi.threshold.thresholdValues[i].includeMaxCheck;
			delete kpi.threshold.thresholdValues[i].selectColor;
			delete kpi.threshold.thresholdValues[i].comboSeverity;
		}
	}

	$scope.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,	 
		action : function(item,event) {
			$scope.deleteMeasure(item,event);
		}

	}];

	$scope.deleteMeasure=function(item,event){
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.kpi.measure.delete.title"))
		.content($scope.translate.load("sbi.kpi.measure.delete.content"))
		.ariaLabel('delete measure') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
		$mdDialog.show(confirm).then(function() {


			sbiModule_restServices.promiseDelete("1.0/kpi",item.id+"/deleteKpi").then(
					function(response){
						$scope.$broadcast("deleteKpiEvent");
					},
					function(response){
						$scope.errorHandler(response.data,""); 
					});




		}, function() {
		});
	}
	$scope.setTab = function(Tab){
		$scope.selectedTab = Tab;
	}
	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}
	//methods cardinality
	$scope.setCardinality = function(){
		var emptyobj={measureList:[],checkedAttribute:{"attributeUnion":{},"attributeIntersection":{}}}
		angular.copy(emptyobj,$scope.cardinality);

		$scope.$broadcast ('parseEvent');
		if($scope.kpi.cardinality!=undefined && Object.keys($scope.kpi.cardinality).length!=0){
			var obj=$scope.kpi.cardinality
			if(!angular.isObject($scope.kpi.cardinality)){ 
				obj = JSON.parse($scope.kpi.cardinality);
			}
			if(obj.measureList.length!=0 && !$scope.formulaModified.value){
				angular.copy(obj,$scope.cardinality); 
				$scope.$broadcast ('activateCardinalityEvent');
			}else if($scope.formulaModified.value){
				var flag = true;
				var obj2=$scope.kpi.definition.measures;
				For1:for(var i=0;i<obj.length;i++){
					if(obj.measureList[i].measureName!=obj2[i]){
						$scope.resetMatrix();
						flag=false;
						break For1;
					}
				}
				if(obj2.length!=obj.measureList.length){
					$scope.resetMatrix();
					flag=false;
				}else if(flag){
					angular.copy(obj,$scope.cardinality); 
					$scope.$broadcast ('activateCardinalityEvent');
				}
			}else {
				$scope.resetMatrix();
			}
		}else{

			$scope.$broadcast ('nullCardinalityEvent');
			$scope.resetMatrix();
		}

	}

	$scope.resetMatrix = function(){
		$scope.cardinality.measureList=[];
		if(Object.keys($scope.kpi.definition).length!=0){
			var definition = $scope.kpi.definition;

			sbiModule_restServices.post("1.0/kpi", 'buildCardinalityMatrix',$scope.kpi.definition.measures).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							$scope.showAction(data);
						} else {
							angular.copy(data,$scope.cardinality.measureList);
							$scope.cardinality.checkedAttribute={"attributeUnion":{},"attributeIntersection":{}};
							$scope.$broadcast ('activateCardinalityEvent');
						}

					}).error(function(data, status, headers, config) {
						$scope.showAction(data);
					})

		}
	}
//	methods Filters
	$scope.setFilters = function(){
		$scope.$broadcast ('parseEvent');
		$scope.placeHolderList=[];
		if($scope.kpi.placeholder=="" || $scope.kpi.placeholder==null || Object.keys($scope.kpi.placeholder).length==0){
			$scope.kpi.placeholder={};
			if($scope.kpi.definition!=undefined && Object.keys($scope.kpi.definition).length!=0){
				$scope.$broadcast('activateFiltersEvent');
			}
		}else if($scope.formulaModified.value){
			//I keep only placeholder valid
			sbiModule_restServices.post("1.0/kpi", 'listPlaceholderByMeasures',$scope.kpi.definition.measures).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
						} else {
							var lista=data;
							if(!angular.isObject($scope.kpi.placeholder)){
								$scope.kpi.placeholder = JSON.parse($scope.kpi.placeholder);
							}

							for(key in Object.keys($scope.kpi.placeholder)){
								if(lista.indexOf(Object.keys($scope.kpi.placeholder)[key])==-1){
									//remove placeholder
									delete $scope.kpi.placeholder[Object.keys($scope.kpi.placeholder)[key]];
								}
							}
						}
						$scope.$broadcast('activateFiltersEvent');

					}).error(function(data, status, headers, config) {

					})



		}else{
			//placeholder presents
			if($scope.kpi.placeholder!=""){
				$scope.kpi.placeholder = JSON.parse($scope.kpi.placeholder);

			}else{
				$scope.kpi.placeholder={};
			}
			$scope.$broadcast('activateFiltersEvent');
		}


	}

	$scope.indexInList=function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.measureName==item){
				return i;
			}
		}
		return -1;
	};
	//methods Threshold
	$scope.loadThreshold=function(){

		for(var i=0;i<$scope.kpi.threshold.thresholdValues.length;i++){
			$scope.kpi.threshold.thresholdValues[i].move="<div layout=\"row\"> " 
				+"<md-button ng-click=\"scopeFunctions.moveUp($event,$parent.$parent.$index)\" class=\"md-icon-button h20 \" aria-label=\"up\">" 
				+"  <md-icon md-font-icon=\"fa fa-arrow-up\"></md-icon>" 
				+" </md-button>" 
				+" <md-button ng-click=\"scopeFunctions.moveDown($event,$parent.$parent.$index)\" class=\"md-icon-button h20\" aria-label=\"down\">" 
				+" <md-icon md-font-icon=\"fa fa-arrow-down\"></md-icon>" 
				+"</md-button>" 
				+"</div>";
			$scope.kpi.threshold.thresholdValues[i].inputLable=' <input  class="tableInput" ng-model="row.label"  ></input>'
				$scope.kpi.threshold.thresholdValues[i].includeNumericInputMin=' <input type="number" class="tableInput" ng-model="row.minValue" step="0,1"  ></input>'
					$scope.kpi.threshold.thresholdValues[i].includeNumericInputMax=' <input type="number" class="tableInput" ng-model="row.maxValue" step="0,1"  ></input>'
						$scope.kpi.threshold.thresholdValues[i].includeMinCheck="<md-checkbox ng-model='row.includeMin'  aria-label='Checkbox'></md-checkbox>";
			$scope.kpi.threshold.thresholdValues[i].includeMaxCheck='<md-checkbox ng-model="row.includeMax"  aria-label="Checkbox"></md-checkbox>';
			$scope.kpi.threshold.thresholdValues[i].selectColor='<color-picker class="tableColorPiker"  color-picker-alpha="true" color-picker-swatch="true" color-picker-format="\'hex\'" ng-model="row.color"></color-picker>';
			$scope.kpi.threshold.thresholdValues[i].comboSeverity=' <md-select ng-model="row.severityId" class="noMargin">'
				+'<md-option value=""></md-option>'
				+'<md-option ng-repeat="sev in scopeFunctions.severityType" value="{{sev.valueId}}">'
				+'	{{sev.translatedValueName}}'
				+' </md-option>'
				+'</md-select>';
		}
	};

	$scope.errorHandler=function(text,title){
		var titleFin=title || "";
		var textFin=text;
		if(angular.isObject(text)){
			if(text.hasOwnProperty("errors")){
				textFin="";
				for(var i=0;i<text.errors.length;i++){
					textFin+=text.errors[i].message+" <br> ";
				}
			}else{
				textFin=JSON.stringify(text)
			}
		}

		var confirm = $mdDialog.confirm()
		.title(titleFin)
		.content(textFin)
		.ariaLabel('error') 
		.ok($scope.translate.load("sbi.general.ok")) 
		return $mdDialog.show(confirm);
	}

};
function DialogControllerKPI($scope,$mdDialog,items,AttributeCategoryList,kpi){

	$scope.AttributeCategoryList=AttributeCategoryList;
	$scope.kpi=kpi;
	$scope.close = function(){
		$mdDialog.cancel();

	}
	$scope.apply = function(){
		$mdDialog.cancel();
		items.resolve($scope.kpi);
	}

	$scope.querySearchCategory=function(query){
		var results = query ? $scope.AttributeCategoryList.filter( createFilterFor(query) ) : [];
		results.push({valueCd:angular.uppercase(query)})
		return results;
	}
	function createFilterFor(query) {
		var lowercaseQuery = angular.lowercase(query);
		return function filterFn(state) {
			return (angular.lowercase(state.valueCd).indexOf(lowercaseQuery) === 0);
		};
	}



}






