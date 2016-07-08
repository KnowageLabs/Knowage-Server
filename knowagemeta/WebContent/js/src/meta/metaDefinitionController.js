var app = angular.module('metaManager', [ 'ngMaterial', 'angular_table','sbiModule', 'componentTreeModule', 'expander-box','associator-directive','angular-list-detail' ]);

app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.factory("dialogScope",function(){
	return window.parent.angular.element(window.frameElement).scope() || {};
})

app.service("metaModelServices",function(sbiModule_jsonServices){
	var bms=this;
	this.metaModelObserver; //the observer on the original object without the $parent
	this.originalMetaModelObserver; //the observer on the original object
	this.observerObject;
	this.cleanedObserverObject={};

	this.cleanObserverObject=function(){
		var data={};
		angular.copy(bms.observerObject,data);
		for(key in data){

			for(var i=0;i<data[key].length;i++){
				for(var j=0;j<data[key][i].columns.length;j++){
					delete data[key][i].columns[j].$parent;
				}
			}
		}
		 angular.copy(data,bms.cleanedObserverObject);
	}

	this.observe=function(observerObj){
		bms.observerObject=observerObj;
		bms.originalMetaModelObserver=sbiModule_jsonServices.observe(bms.observerObject);
		bms.cleanObserverObject();
		bms.metaModelObserve=sbiModule_jsonServices.observe(bms.cleanedObserverObject);
	};

	this.generateDiff=function(){
		bms.cleanObserverObject();
		return sbiModule_jsonServices.generate(bms.metaModelObserve);
	}

	this.applyPatch=function (patch,validate){
		sbiModule_jsonServices.apply(bms.observerObject,patch,validate);
		bms.generateDiff(); //create for avoid return data to backend
	}

	this.createRequestRest=function(myJson){
		var diff=bms.generateDiff();

		for(var i=0;i<diff.length;i++){
			var arrsp=diff[i].path.split("/");
			var split=false;

			switch(arrsp[arrsp.length-1]){
				case "checked":
					split=true;
					break;
				case "expanded":
					split=true;
					break;
				case "visible":
					split=true;
					break;
				case "$$hashKey":
					split=true;
					break;
				case "$parent":
					split=true;
					break;
				case "type":
					// TO-DO this value is added by component-tree... check if are some property with this value
					split=true;
					break;

			}

			if(split){
				diff.splice(i,1);
				i--;
			}
		}

		var resp={
				data : myJson,
				diff : diff
		};
		return resp;
	}
})

app.controller('metaDefinitionController', [ '$scope', 'sbiModule_translate','sbiModule_restServices','sbiModule_config','dialogScope','metaModelServices','$interval', metaDefinitionControllerFunction ]);



function metaDefinitionControllerFunction($scope, sbiModule_translate,sbiModule_restServices,sbiModule_config,dialogScope,metaModelServices,$interval) {
	$scope.translate = sbiModule_translate;
	$scope.physicalModelTreeInterceptor = {};
	$scope.businessModelTreeInterceptor = {};
	$scope.businessViewTreeInterceptor = {};
	$scope.steps = {
		current : 0
	};
	$scope.datasourceId = datasourceId;
	$scope.meta={physicalModels:[],businessModels:[],businessViews:[]};
	$scope.physicalModels = []; // array of table to transform in physical model
	$scope.businessModels = []; // array of table to transform in business model


	$scope.loadSbiModel=function(translatedModel){

		angular.copy(translatedModel,$scope.meta);
		metaModelServices.observe($scope.meta);

		var refreshPMT= $interval(function() {
	        if ($scope.physicalModelTreeInterceptor.refreshTree!=undefined) {
	        	  $interval.cancel(refreshPMT);
	        		$scope.physicalModelTreeInterceptor.refreshTree();
	          }
	        }, 500,10);

		var refreshBMT= $interval(function() {
	        if ($scope.businessModelTreeInterceptor.refreshTree!=undefined) {
	        	  $interval.cancel(refreshBMT);
	        		$scope.businessModelTreeInterceptor.refreshTree();
	          }
	        }, 500,10);

		var refreshBVT= $interval(function() {
	        if ($scope.businessViewTreeInterceptor.refreshTree!=undefined) {
	        	  $interval.cancel(refreshBVT);
	        		$scope.businessViewTreeInterceptor.refreshTree();
	          }
	        }, 500,10);

	}

	if(translatedModel!=undefined){
		$scope.steps.current=1;
		$scope.loadSbiModel(translatedModel);
	}



//	$scope.physicalModel = [];
//	$scope.businessModel = [];




	$scope.saveModel=function(){
		var dataToSend={};
		dataToSend.name=bmName;
		dataToSend.id=bmId;
		sbiModule_restServices.promisePost("1.0/metaWeb", "generateModel", metaModelServices.createRequestRest(dataToSend))
		.then(
				function(response) {
					sbiModule_restServices.errorHandler(sbiModule_translate.load("sbi.catalogues.toast.updated"), "");
				},function(response) {
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.meta.model.generate.error"));
				});
	}

	$scope.closeMetaDefinition = function() {
		//TO-DO chiedere conferma prima di chiudere
		dialogScope.closeMetaWeb();
	}

	$scope.continueToMeta = function() {
			if ($scope.physicalModels.length == 0) {
				sbiModule_restServices.errorHandler(sbiModule_translate.load("sbi.meta.model.physical.select.required"), "");
			} else {
				$scope.createMeta();
			}
	};

	$scope.gobackToMetaDefinition=function(){
		//TO-DO chiedere conferma prima di andare indietro
		$scope.steps.current = 0;
	}

	 $scope.removeCircularDependency=function(data){
		 for(var i=0;i<data.length;i++){
			 for(var j=0;j<data[i].columns.length;j++){
				 delete data[i].columns[j].$parent;
			 }
		 }
		 return data;
	 }

	$scope.createMeta = function() {
		var dataToSend = {};
		dataToSend.datasourceId = $scope.datasourceId;
		dataToSend.physicalModels = $scope.physicalModels;
		dataToSend.businessModels = $scope.businessModels;
		// TODO set model name here
		//dataToSend.modelName = 'test_model_hard_coded';
		dataToSend.modelName = bmName;

		sbiModule_restServices.promisePost("1.0/metaWeb", "create", dataToSend)
				.then(
						function(response) {
							$scope.steps.current = 1;
							angular.copy(response.data,$scope.meta);

							metaModelServices.observe($scope.meta);

						},
						function(response) {
							sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.kpi.rule.load.datasource.error"));
						});

	};


}


angular.module('metaManager').filter('filterByCategory', function() {
	return function(items, categoryName) {

		var filtered = [];
		angular.forEach(items, function(item) {
			if (angular.equals(item.key.split(".")[0], categoryName)) {
				filtered.push(item);
			}
		});
		return filtered;
	};
});

angular.module('metaManager').service("parametersBuilder", function() {
	this.extractCategories = function(properties) {
		var propertiesCat = [];
		for (var i = 0; i < properties.length; i++) {
			var tmpProp = properties[i];
			var struct = tmpProp.key.split(".");
			if (propertiesCat.indexOf(struct[0]) == -1) {
				propertiesCat.push(struct[0]);
			}
		}
		return propertiesCat;
	}

})