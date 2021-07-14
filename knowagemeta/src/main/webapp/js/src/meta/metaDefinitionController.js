(function() {
	var app = angular.module('metaManager', [ 'ngMaterial', 'angular_table','sbiModule', 'componentTreeModule', 'expander-box','associator-directive','angular-list-detail','agGrid' ]);

	app.config(function($mdThemingProvider, $locationProvider ,$mdDateLocaleProvider) {
		$mdThemingProvider.theme('knowage')
		$mdThemingProvider.setDefaultTheme('knowage');
		$locationProvider.html5Mode(true);

		$mdDateLocaleProvider.parseDate = function(dateString) {
	        var m = moment(dateString, 'L', true);
	        return m.isValid() ? m.toDate() : new Date(NaN);
	    };
	    $mdDateLocaleProvider.formatDate = function(date,format) {


	    	if(!format) format = 'L';
	        return moment(date).locale(locale).format(format);
	    };
	});

	app.service("metaModelServices",function(sbiModule_jsonServices){
		var bms=this;
		this.metaModelObserver; //the observer on the original object without the $parent
		this.originalMetaModelObserver; //the observer on the original object
		this.observerObject;
		this.cleanedObserverObject={};
		this.dataSourceId;


		this.cleanObserverObject=function(){
			var data={};
			angular.copy(bms.observerObject,data);
			for(key in data){

				for(var i=0;i<data[key].length;i++){
					if(key!="olapModels"){
						for(var j=0;j<data[key][i].columns.length;j++){
							delete data[key][i].columns[j].$parent;
						}
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

		this.getDataSourceId=function(){
			return this.dataSourceId;
		}

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

	app.controller('checkModelController', [ '$scope', 'sbiModule_translate','sbiModule_restServices','sbiModule_config','metaModelServices','$interval','$angularListDetail','$mdDialog','$window','incorrectRelationships','dataToSend', checkModelControllerFunction ]);

	function checkModelControllerFunction($scope, sbiModule_translate,sbiModule_restServices,sbiModule_config,metaModelServices,$interval,$angularListDetail,$mdDialog,$window,incorrectRelationships,dataToSend) {
		$scope.incorrectRelationships=incorrectRelationships;
		$scope.translate=sbiModule_translate;
		$scope.dataToSend = dataToSend;

		//cancel action
		$scope.cancel = function(){
			$mdDialog.cancel();
		};

		//save model
		$scope.saveModel=function(){
			sbiModule_restServices.promisePost("1.0/metaWeb", "generateModel", metaModelServices.createRequestRest($scope.dataToSend))
			.then(
					function(response) {
						sbiModule_restServices.errorHandler(sbiModule_translate.load("sbi.catalogues.toast.updated"), "");

					},function(response) {
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.meta.model.generate.error"));
					});
		};

	}

	app.controller('metaDefinitionController', [ '$scope', 'sbiModule_translate','sbiModule_restServices','sbiModule_config','metaModelServices','$interval','$angularListDetail','$mdDialog','$window','sbiModule_config','sbiModule_user', metaDefinitionControllerFunction ]);



	function metaDefinitionControllerFunction($scope, sbiModule_translate,sbiModule_restServices,sbiModule_config,metaModelServices,$interval,$angularListDetail,$mdDialog,$window,sbiModule_user) {
		$scope.translate = sbiModule_translate;
		$scope.physicalModelTreeInterceptor = {};
		$scope.businessModelTreeInterceptor = {};
		$scope.businessViewTreeInterceptor = {};
		$scope.steps = {
			current : 0
		};
		$scope.datasourceId = datasourceId;
		$scope.bmName=bmName;
		$scope.meta={physicalModels:[],businessModels:[],businessViews:[]};
		$scope.physicalModels = []; // array of table to transform in physical model
		$scope.businessModels = []; // array of table to transform in business model

		metaModelServices.dataSourceId =datasourceId;

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
			$angularListDetail.goToDetail();
			$scope.loadSbiModel(translatedModel);
		}
		$window.parent.document.getElementById('loadMask').style.display='none';


		$scope.saveModel=function(){
			var dataToSend={};
			dataToSend.name=bmName;
			dataToSend.id=bmId;

			sbiModule_restServices.promisePost("1.0/metaWeb", "checkRelationships", metaModelServices.createRequestRest(dataToSend))
			.then(
					function(response) {
						//check if any error was found during the validation
						if (response.data.incorrectRelationships !== undefined && response.data.incorrectRelationships.length > 0) {
							//show the warning dialog
							$mdDialog.show({
								controller: 'checkModelController',
								preserveScope: true,
								locals: {incorrectRelationships:response.data.incorrectRelationships, dataToSend: dataToSend},
								templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/checkModel.jsp',
								clickOutsideToClose:false,
								escapeToClose :false,
								fullscreen: true
							});
						} else {
							//After the check if there aren't warnings, let's continue saving
							if(JSON.parse(response.data.patch).length>0){
								metaModelServices.applyPatch(JSON.parse(response.data.patch));
								$scope.$broadcast("updateBusinessClassesGrid");
								$scope.$broadcast("updateBusinessViewsGrid");
							}
							sbiModule_restServices.promisePost("1.0/metaWeb", "generateModel", metaModelServices.createRequestRest(dataToSend))
							.then(
									function(response) {
										sbiModule_restServices.errorHandler(sbiModule_translate.load("sbi.catalogues.toast.updated"), "");

									},function(response) {
										sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.meta.model.generate.error"));
									});
						}

					},function(response) {
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.meta.model.checkmodel.error"));
					});


		}

		$scope.closeMetaDefinition = function() {
			 var confirm = $mdDialog.confirm()
			 .title(sbiModule_translate.load("sbi.meta.meta.exit"))
			 .ariaLabel('exit meta')
			 .ok(sbiModule_translate.load("sbi.general.continue"))
			 .cancel(sbiModule_translate.load("sbi.general.cancel"));
			   $mdDialog.show(confirm).then(function() {
				   window.parent.postMessage({"action":"closeDialog"}, '*');
			   });


		}

		$scope.continueToMeta = function() {
				if ($scope.physicalModels.length == 0) {
					sbiModule_restServices.errorHandler(sbiModule_translate.load("sbi.meta.model.physical.select.required"), "");
				} else {
					$scope.createMeta();
					$angularListDetail.goToDetail();
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

	angular.module('metaManager').filter('filterByMainCategory', function(sbiModule_user) {
		var showDataProfiling = sbiModule_user.functionalities.indexOf("MetaModelDataProfiling")>-1;

		return function(items, prop) {
			var toReturn = [];
			angular.copy(items,toReturn);

			angular.forEach(toReturn, function(item) {
				if(item == "behavioural" && !showDataProfiling){
					//remove behavioural category
					var sdInd = toReturn.indexOf("behavioural");
					if(sdInd != -1){
						toReturn.splice(sdInd,1);
					}
				}
			});
			return toReturn;
		};
	});

	angular.module('metaManager').filter('filterByCategory', function(sbiModule_user) {
		var showDataProfiling = sbiModule_user.functionalities.indexOf("MetaModelDataProfiling")>-1;

		return function(items, categoryName) {
			var filtered = [];
			angular.forEach(items, function(item) {
				var key = Object.keys(item)[0];
				if (angular.equals(key.split(".")[0], categoryName)) {
					if (categoryName == "behavioural" || key == "structural.attribute"){
						if (showDataProfiling == true) {
							filtered.push(item);
						}
					} else {
						filtered.push(item);
					}
				}
			});
			return filtered;
		};
	});

	angular.module('metaManager').filter('filterByDataType', function($filter) {


		return function(items) {

			var filtered = [];

			var dataType = $filter('filter')(items,'structural.datatype')
			var dataTypeValue;
			if(dataType.length > 0){
				dataTypeValue = dataType[0]['structural.datatype'].value
			}



			var isOneOfDataTypes = function(dataType,types){
				for(var i =0;i < types.length ; i++){
					if(dataType && dataType.toLowerCase() == types[i]){
						return true;
					}
				}

				return false;
			}



			angular.forEach(items, function(item) {

				if(!isOneOfDataTypes(dataTypeValue,['date','timestamp']) && item['structural.dateformat']){

				}else if(!isOneOfDataTypes(dataTypeValue,['time']) && item['structural.timeformat']){

				}else if(isOneOfDataTypes(dataTypeValue,['date','time','timestamp']) && item['structural.format']){

				}else{
					filtered.push(item)
				}


			});

			return filtered;
		};
	});

	angular.module('metaManager').filter('format', function($mdDateLocale) {



		return function(item,prop) {


			var formatted = item;
			var date = new Date().toDateString();
			if(prop["structural.dateformat"]||prop["structural.timeformat"]){

				return $mdDateLocale.formatDate(new Date(date),item)
			}

			return formatted;
		};
	});

	angular.module('metaManager').filter('filterByProductType', function(sbiModule_config,sbiModule_user) {
		return function(items, prop) {

			var showSpatialDimension = sbiModule_user.functionalities.indexOf("SpatialDimension")>-1;
			var showTemporalDimension = sbiModule_user.functionalities.indexOf("TemporalDimension")>-1;
			var toReturn = [];
			angular.copy(items,toReturn);

			var key = Object.keys(prop)[0];
			if(angular.equals(prop[key].propertyType.name,"Type")){
				if(sbiModule_config.productTypes.indexOf("KnowageLI")==-1 || !showSpatialDimension){
					//remove spatial dimension
					var sdInd=toReturn.indexOf("geographic dimension");
					if(sdInd!=-1){
						toReturn.splice(sdInd,1);
					}
				}

				if(sbiModule_config.productTypes.indexOf("KnowageSI")==-1 || !showTemporalDimension){
					//remove temporal dimension
					var tdInd=toReturn.indexOf("temporal dimension");
					if(tdInd!=-1){
						toReturn.splice(tdInd,1);
					}

					var tdInd=toReturn.indexOf("time dimension");
					if(tdInd!=-1){
						toReturn.splice(tdInd,1);
					}

					var tdInd=toReturn.indexOf("calendar");
					if(tdInd!=-1){
						toReturn.splice(tdInd,1);
					}

				}
			}
			return toReturn;

		};
	});

	angular.module('metaManager').service("parametersBuilder", function() {
		this.extractCategories = function(properties) {
			var propertiesCat = [];
			for (var i = 0; i < properties.length; i++) {
				var tmpProp = properties[i];
				var key = Object.keys(tmpProp)[0];
				var struct = key.split(".");
				if (propertiesCat.indexOf(struct[0]) == -1) {
					propertiesCat.push(struct[0]);
				}
			}
			return propertiesCat;
		}

	})
})();