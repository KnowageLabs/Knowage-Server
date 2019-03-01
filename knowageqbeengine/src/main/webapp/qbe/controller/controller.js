/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular
	.module('qbe.controller', ['configuration','directive','services'])
	.controller('qbeController',

		["$scope",
		"$rootScope",
		"$filter",
		"entity_service",
		"query_service",
		"filters_service",
		"formulaService",
		"save_service",
		"sbiModule_inputParams",
		"sbiModule_translate",
		"sbiModule_config",
		"sbiModule_action",
		"sbiModule_action_builder",
		"sbiModule_restServices",
		"sbiModule_messaging",
		"sbiModule_user",
		"windowCommunicationService",
		"$mdDialog",
		"$mdPanel",
		"$q",
		"byNotExistingMembersFilter",
		"selectedEntitiesRelationshipsService",
		"queryEntitiesService",
		"expression_service",
		qbeFunction]);


function qbeFunction($scope,$rootScope,$filter,entity_service,query_service,filters_service,formulaService,save_service,sbiModule_inputParams,sbiModule_translate,sbiModule_config,sbiModule_action,sbiModule_action_builder,sbiModule_restServices,sbiModule_messaging, sbiModule_user,windowCommunicationService, $mdDialog ,$mdPanel,$q,byNotExistingMembersFilter,selectedEntitiesRelationshipsService,queryEntitiesService,expression_service){
	$scope.translate = sbiModule_translate;
	$scope.sbiModule_action_builder = sbiModule_action_builder;
	var entityService = entity_service;
	var inputParamService = sbiModule_inputParams;
	$scope.queryModel = [];
	$scope.pars = inputParamService.params;
	$scope.meta = [];
	$scope.editQueryObj = new Query("");
	$scope.advancedFilters = [];
	$scope.customTransformedFormulas = [];
	$scope.entityModel = {};
	$scope.subqueriesModel = {};
	$scope.formulas = formulaService.getFormulasFromXml();
	$scope.selectedRelationsService = selectedEntitiesRelationshipsService;
	$scope.queryEntitiesService = queryEntitiesService;
	var comunicator = windowCommunicationService;

	var consoleHandler = {}
	consoleHandler.handleMessage = function(message){
		if(message === 'close'){
			var saveObj = {};
			saveObj.qbeQuery = {catalogue: {queries: [$scope.editQueryObj]}};
			saveObj.pars = $scope.pars;
			comunicator.sendMessage(saveObj);
		}
		if(message === 'saveDS'){
			var saveObj = {};
			saveObj.name = "workspace"
			saveObj.qbeQuery = {catalogue: {queries: [$scope.editQueryObj]}};
			saveObj.meta = $scope.meta;
			comunicator.sendMessage(saveObj);
		}

		console.log(message)
	}
	comunicator.addMessageHandler(consoleHandler);


	formulaService.getCustomFormulas().then(function(response) {
		$scope.customFormulas = [];
		if(response.data.data){
			$scope.customFormulas = response.data.data;
		};
		for (var i = 0; i < $scope.customFormulas.length; i++) {
			$scope.customTransformedFormulas.push(formulaService.createFormula($scope.customFormulas[i]))
		}
		Array.prototype.push.apply($scope.formulas, $scope.customTransformedFormulas);
	});
	var queryHandler = function(newCatalogue,oldCatalogue){
		$scope.meta.length = 0;
		$rootScope.$broadcast('editQueryObj',newCatalogue);
		for (var i = 0; i < newCatalogue.fields.length; i++) {
			var meta = {
					"displayedName":newCatalogue.fields[i].alias,
					"name":newCatalogue.fields[i].alias,
					"fieldType":newCatalogue.fields[i].fieldType.toUpperCase(),
					"type":""
			}
			$scope.meta.push(meta);
		}
		if(parent.globalQbeJson){
			$scope.bodySend.catalogue = [];
			$scope.bodySend.catalogue.push($scope.editQueryObj);
		}
		$scope.filters = $scope.editQueryObj.filters;
		$scope.havings = $scope.editQueryObj.havings;

		if(query_service.smartView){

			var finalPromise = 	$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel, false);
			if(finalPromise) {
				finalPromise.then(function(){},function(){
					angular.copy(oldCatalogue,$scope.editQueryObj);
					for(var i =0;i<$scope.previousVersionRelations.length;i++){
						var relationship = $scope.previousVersionRelations[i]
						$scope.filteredRelations[i].isConsidered = relationship.isConsidered;

					}
				});
			}
		} else {
			$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
		}
		$rootScope.$broadcast('bodySend', $scope.bodySend);
		window.parent.queryCatalogue = {catalogue: {queries: [$scope.editQueryObj]}};

	}
	$scope.$watch('bodySend',function(newValue,oldValue){
		if(angular.equals(newValue.catalogue,oldValue.catalogue) && !angular.equals(newValue.pars,oldValue.pars)){
			queryHandler(newValue.catalogue[0],oldValue.catalogue[0])
		}


	},true)

	$scope.$watch('editQueryObj',function(newValue,oldValue){
		queryHandler(newValue,oldValue);
	},true)

	$scope.executeQuery = function ( query, bodySend, queryModel, isCompleteResult, start, itemsPerPage) {

		if(query.fields.length>0){
			return query_service.executeQuery( query, bodySend, queryModel, isCompleteResult, start, itemsPerPage);
		}else{
			queryModel.length = 0;
		}
	}
	function checkForDuplicatedAliases() {
	    var aliases = [];
	    for (var i = 0; i < $scope.editQueryObj.fields.length; ++i) {
	        var value = $scope.editQueryObj.fields[i].alias;
	        if (aliases.indexOf(value) !== -1) {
	            return true;
	        }
	        aliases.push(value);
	    }
	    return false;
	}
	window.parent.openPanelForSavingQbeDataset = function (){
		if(checkForDuplicatedAliases()){
			sbiModule_messaging.showWarningMessage("Your query contains dupicate aliases", "Worning")
			return;
		}
		var bodySend = angular.copy($scope.bodySend);
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/saveTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,mdPanelRef){
					$scope.model ={ bodySend:bodySend,"mdPanelRef":mdPanelRef};
				},
				locals: {bodySend: bodySend},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	}

	window.qbe ={};
	window.qbe.openPanelForSavingQbeDataset = $scope.openPanelForSavingQbeDataset;

	$scope.addToQueryModelWithoutExecutingQuery = function (query, queryModel) {
		if(query.fields.length>0){
			queryModel.length = 0;
     		for (var i = 0; i < query.fields.length; i++) {
     			var key = "column_"+(i+1);
     			var queryObject = {
         		    	"id":query.fields[i].id,
         		    	"name":query.fields[i].field,
         		    	"alias":query.fields[i].alias,
         		    	"entity":query.fields[i].entity,
         		    	"color":query.fields[i].color,
         		    	"data":[],
         		    	"funct":query.fields[i].funct,
         		    	"fieldType" : query.fields[i].fieldType,
         		    	"visible":query.fields[i].visible,
         		    	"distinct":$scope.editQueryObj.distinct,
         		    	"group":query.fields[i].group,
         		    	"order":i+1,
         		    	"ordering":query.fields[i].order,
         		    	"filters": [],
         		    	"havings": []
         		    }

     			if(query.fields[i].temporal){
     				queryObject.temporal = query.fields[i].temporal;
     			}
     			queryModel.push(queryObject);
			}
		}else{
			queryModel.length = 0;
		}
	}

	$scope.onDropComplete=function(field,evt){
		if(field.connector) return;
		$scope.addField(field);

    };

    $rootScope.$on('addFieldOnClick', function (event, data) {
    	if(data.connector) return;
    	$scope.addField(data);
    });

	$rootScope.$on('applyFunction', function (event, data) {
		var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
		var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
		var indexOfFieldInQuery = findWithAttr($scope.query.fields,'id', data.fieldId);
		if(data.funct!= undefined && data.funct !=null && data.funct!="") {
			if(data.funct=="YTD"||
					data.funct=="LAST_YEAR"||
					data.funct=="PARALLEL_YEAR"||
					data.funct=="MTD"||
					data.funct=="LAST_MONTH") {
				$scope.query.fields[indexOfFieldInQuery].temporalOperand = data.funct.toUpperCase();
			} else {
				$scope.query.fields[indexOfFieldInQuery].funct = data.funct.toUpperCase();
			}
		}
		if(data.filters!= undefined && data.filters != null ) {
			$scope.query.filters = data.filters;
			$scope.query.expression = data.expression;
		}
		if(data.pars!= undefined && data.pars != null ) {
			$scope.bodySend.pars = data.pars;
		}
		$scope.query.fields[indexOfFieldInQuery].group = false;
		//$scope.executeQuery($scope.entityModel.entities[indexOfEntity].children[indexOfFieldInEntity], $scope.query, $scope.bodySend, $scope.queryModel);
	});

	$rootScope.$on('applyFunctionForParams', function (event, data) {
		if(data.pars!= undefined && data.pars!= null ) {
			$scope.pars = data.pars;

		}
	});

	$rootScope.$on('smartView', function (event, data) {
		var temp = []

			for (var i = 0; i < data.length; i++) {
				for(var j =0;j < $scope.editQueryObj.fields.length;j++){
					if($scope.editQueryObj.fields[j].id === data[i].id){
						$scope.editQueryObj.fields[j].group = data[i].group;
						$scope.editQueryObj.fields[j].funct = data[i].funct;
						$scope.editQueryObj.fields[j].visible = data[i].visible;
						$scope.editQueryObj.fields[j].distinct = data[i].distinct;
						$scope.editQueryObj.fields[j].order = data[i].ordering;
						$scope.editQueryObj.fields[j].alias = data[i].alias;
						temp.push($scope.editQueryObj.fields[j]);
					}
				}

			}
			$scope.editQueryObj.fields = temp;




		if(query_service.smartView){
			$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel);
		} else {
			$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
		}
	});

	$scope.$on('executeQuery', function (event, data) {
		if(data.fields != undefined  && !query_service.smartView && data.fields.length>0){
			for (var i = 0; i < data.fields.length; i++) {
				$scope.editQueryObj.fields[i].group = data.fields[i].group;
				$scope.editQueryObj.fields[i].funct = data.fields[i].funct;
				$scope.editQueryObj.fields[i].visible = data.fields[i].visible;
				$scope.editQueryObj.fields[i].distinct = data.fields[i].distinct;
				$scope.editQueryObj.fields[i].order = data.fields[i].ordering;
			}
		}

		$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel, true, data.start, data.itemsPerPage);
	});

	$scope.$on('setVisible', function (event, data) {
		 var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
		  var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
		  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.fieldId);
		  $scope.isNotChangedFromHidingField = false;
		  $scope.editQueryObj.fields[indexOfFieldInQuery].visible = data.visible;
	});

	$scope.$on('showHiddenColumns', function (event, data) {
		 for (var i = 0; i < $scope.editQueryObj.fields.length; i++) {
			  $scope.editQueryObj.fields[i].visible = true;
		}
	});

	$scope.$on('orderField', function (event, data) {
		 for (var i = 0; i < $scope.editQueryObj.fields.length; i++) {
			 if($scope.editQueryObj.fields[i].id==data.id){

				 $scope.editQueryObj.fields[i].order = data.order;
			 } else {
				 $scope.editQueryObj.fields[i].order = "NONE";
			 }
		}
	});

	$scope.$on('showCalculatedField', function (event, data) {
		$scope.showCalculatedField(data);
	});

	$scope.$on('showSQLQuery', function (event, data) {

		item = {};
    	item.catalogue=JSON.stringify($scope.bodySend.catalogue);
    	item.currentQueryId = "q1";
    	item.ambiguousFieldsPaths = [];
    	item.ambiguousRoles = [];

    	queryParam = {};

    	conf = {};
		conf.headers = {'Content-Type': 'application/x-www-form-urlencoded'},

		conf.transformRequest = function(obj) {

			var str = [];

			for(var p in obj)
				str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));

			return str.join("&");

		}
		sbiModule_action.promisePost('SET_CATALOGUE_ACTION',queryParam,item, conf).then(function(response){
			$scope.getSQL();
		}, function(response){
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load("kn.qbe.general.error"));
		});
	});

	$scope.getSQL = function () {
		item = {};
    	item.replaceParametersWithQuestion=true;

    	queryParam = {};

    	conf = {};
		conf.headers = {'Content-Type': 'application/x-www-form-urlencoded'},

		conf.transformRequest = function(obj) {

			var str = [];

			for(var p in obj)
				str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));

			return str.join("&");

		}
		sbiModule_action.promisePost('GET_SQL_QUERY_ACTION',queryParam,item, conf).then(function(response){
			$mdDialog.show(
	        		$mdDialog.alert()
	        		     .clickOutsideToClose(true)
	        		     .title($scope.translate.load("kn.generic.query.SQL"))
	        		     .htmlContent(response.data.sqlFormatted)
	        		     .ok($scope.translate.load("kn.qbe.general.ok"))
	            );
		}, function(response){
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load("kn.qbe.general.error"));
		});
	}


	$rootScope.$on('removeColumn', function (event, data) {
	  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.id);
	  var indexOfFieldInModel = findWithAttr($scope.queryModel,'id', data.id);
	  if (indexOfFieldInQuery > -1 && indexOfFieldInModel > -1) {
		  $scope.editQueryObj.fields.splice(indexOfFieldInQuery, 1);
		  $scope.queryModel.splice(indexOfFieldInModel, 1);
		}




	});

	$rootScope.$on('group', function (event, data) {

	  var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
	  var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
	  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.fieldId);
	  $scope.editQueryObj.fields[indexOfFieldInQuery].group = data.group;
	  $scope.editQueryObj.fields[indexOfFieldInQuery].funct = "";
	  if(query_service.smartView){
		  //$scope.executeQuery( $scope.editQueryObj, $scope.bodySend, $scope.queryModel, false);
	  }

	});

	var findWithAttr = function(array, attr, value) {
	    for(var i = 0; i < array.length; i += 1) {
	        if(array[i][attr] === value) {
	            return i;
	        }
	    }
	    return -1;
	}

	$scope.addField = function (field,calcField) {
		for (var i = 0; i < $scope.queryModel.length; i++) {
			if($scope.queryModel != undefined  && !query_service.smartView && $scope.queryModel.length>0){
				$scope.editQueryObj.fields[i].group = $scope.queryModel[i].group;
				$scope.editQueryObj.fields[i].funct = $scope.queryModel[i].funct;
				$scope.editQueryObj.fields[i].visible = $scope.queryModel[i].visible;
				$scope.editQueryObj.fields[i].distinct = $scope.queryModel[i].distinct;
				$scope.editQueryObj.fields[i].iconCls = $scope.queryModel[i].visible;
			}
			$scope.editQueryObj.fields[i].alias = $scope.queryModel[i].alias;
		}

		if(!calcField){
			var newField  = {
				   "id":field.attributes.type === "inLineCalculatedField" ? field.attributes.formState : field.id,
				   "alias":field.attributes.field,
				   "type":field.attributes.type === "inLineCalculatedField" ? "inline.calculated.field" : "datamartField",
				   "fieldType":field.attributes.iconCls,
				   "entity":field.attributes.entity,
				   "field":field.attributes.field,
				   "funct":isColumnType(field,"measure")? "SUM":"",
				   "color":field.color,
				   "group":isColumnType(field,"attribute"),
				   "order":"NONE",
				   "include":true,
				   "visible":true,
				   "iconCls":field.iconCls,
				   "longDescription":field.attributes.longDescription,
				   "distinct":$scope.editQueryObj.distinct,
				   "leaf":field.leaf
				}
		}
		if(!field.hasOwnProperty('id')){
			newField.id=field.alias;
			newField.alias=field.text;
			newField.field=field.text;
			newField.temporal=field.temporal;
		}

		if(!calcField){
			$scope.editQueryObj.fields.push(newField);
		}

	}

	var isColumnType = function(field,columnType){
		return field.iconCls==columnType || isCalculatedFieldColumnType(field,columnType)
	}

	var isInLineCalculatedField = function(field){
		return field.attributes.type === "inLineCalculatedField"
	}

	var isCalculatedFieldColumnType = function(inLineCalculatedField,columnType){
		return isInLineCalculatedField(inLineCalculatedField) && inLineCalculatedField.attributes.formState.nature === columnType
	}

	$scope.colors = ['#F44336', '#673AB7', '#03A9F4', '#4CAF50', '#FFEB3B', '#3F51B5', '#8BC34A', '#009688', '#F44336'];

    $scope.droppedFunction = function(data) {
        console.log(data)
    };

    $scope.entitiesFunctions = [{
        "label": "show information",
        "icon": "fa fa-info",
        "action": function(item, event) {
        	$scope.showInfo(item, event);
        }
    }];

    $scope.queryFunctions = [{
        "label": "start subquery",
        "icon": " fa fa-pencil-square-o",
        "action": function(item, event) {
        	$scope.editQueryObj = item;
        }
    },
    {
        "label": "remove subquery",
        "icon": "fa fa-trash",
        "action": function(item, $event) {
        	var index = $scope.subqueriesModel.subqueries.indexOf(item);
        	  $scope.subqueriesModel.subqueries.splice(index, 1);
        	  $scope.stopEditingSubqueries();
        }
    }];

    $scope.fieldsFunctions = [

//        {
//        	"label": "havings",
//        	"icon": "fa fa-check-square-o",
//        	"visible": function (item){
//        		return true;
//        	},
//        	"action": function(item, event) {
//            	$scope.openHavings(item, $scope.editQueryObj.havings,$scope.entityModel, $scope.editQueryObj.subqueries);
//        	}
//        },
    	{
        	"label": "filters",
        	"icon": "fa fa-filter",
        	"visible": function (item){
        		return true;
        	},
        	"action": function(item, event) {
        		$scope.openFilters(item,$scope.entityModel,$scope.pars, $scope.editQueryObj.filters,$scope.editQueryObj.subqueries, $scope.editQueryObj.expression, $scope.advancedFilters);
        	}
        }
    ];

    $scope.query = new Query(1);
    $scope.query.name = $scope.translate.load("kn.qbe.custom.table.toolbar.main");

    $scope.catalogue = [$scope.query];

    $scope.editQueryObj = $scope.query;
    $scope.subqueriesModel.subqueries = $scope.query.subqueries;

    $scope.bodySend = {
    		"catalogue":$scope.catalogue,
    		"qbeJSONQuery":{},
        	"pars": $scope.pars,
        	"meta": $scope.meta,
        	"schedulingCronLine":"0 * * * * ?",
    };


    $scope.$on('openHavings',function(event,field){
		$scope.openHavings(field);
	});

    $scope.$on('openFilters',function(event,field){
		$scope.openFilters(field,$scope.entityModel,$scope.pars, $scope.editQueryObj.filters,$scope.editQueryObj.subqueries, $scope.editQueryObj.expression, $scope.advancedFilters);
	})

	$scope.$on('distinctSelected',function(){
    	 $scope.editQueryObj.distinct =  !$scope.editQueryObj.distinct;
    })

	$scope.$on('openDialogForParams',function(event){
		$scope.openDialogForParams($scope.pars,$scope.editQueryObj.filters, $scope.editQueryObj.expression,$scope.advancedFilters);
	})

	$scope.$on('openDialogJoinDefinitions',function(event){
		$scope.openDialogJoinDefinitions($scope.pars);
	})


	$scope.openDialogJoinDefinitions = function(){

    	queryEntitiesService.getQueryEntitiesUniqueNames($scope.query.id,$scope.bodySend).then(function(response){
    		var selectedEntities = response.data;

    		$mdDialog.show({
                controller: function ($scope, $mdDialog) {

                	$scope.filteredRelations = $scope.selectedRelationsService.getRelationships(selectedEntities,$scope.entityModel.entities)
                	$scope.previousVersionRelations = angular.copy($scope.filteredRelations);
                    $scope.ok= function(){
                		$scope.editQueryObj.graph = angular.copy($scope.filteredRelations);

                        $mdDialog.hide();
                    }

                	$scope.cancel= function(){
                		console.log($scope.joinForm.FormController)
                		//$scope.joinForm.$setPristine();
                        $mdDialog.hide();
                    }
                },

                scope: $scope,
                preserveScope:true,
                templateUrl:  sbiModule_config.contextName +'/qbe/templates/joinDefinitionsDialog.html',

                clickOutsideToClose:true
            })

    	})

    }

	$scope.openDialogForParams = function(pars,filters,expression,advancedFilters){
    	var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/parameterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,mdPanelRef){
					$scope.model ={
							"pars": pars,
							"filters":filters,
							"mdPanelRef":mdPanelRef,
							"expression":expression,
							"advancedFilters":advancedFilters};
				},
				locals: {pars: pars},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
    }

    $scope.$on('openFiltersAdvanced',function(event,field){
		$scope.showVisualization($scope.editQueryObj.filters, $scope.advancedFilters,$scope.editQueryObj.expression);
	})

	$scope.$on('addTemporalParameter',function(event,field){
		$scope.tmpOperandFieldId = field.id;
		$scope.tmpOperand={};
		for (var i = 0; i < $scope.editQueryObj.fields.length; i++) {
			  if($scope.editQueryObj.fields[i].id == field.id){
				  if($scope.editQueryObj.fields[i].hasOwnProperty("temporalOperandParameter") && $scope.editQueryObj.fields[i].temporalOperandParameter!=undefined) {
					  $scope.tmpOperand.param = $scope.editQueryObj.fields[i].temporalOperandParameter;
				  } else {
					  $scope.tmpOperand.param = "";
				  }

			  }
    	}

		$mdDialog.show({
            controller: function ($scope, $mdDialog) {

                $scope.applyTmpOperandParam = function(){
                	for (var i = 0; i < $scope.editQueryObj.fields.length; i++) {
            			  if($scope.editQueryObj.fields[i].id == $scope.tmpOperandFieldId){
            				$scope.editQueryObj.fields[i].temporalOperandParameter = $scope.tmpOperand.param;
            			  }
      	      		}
                    $mdDialog.hide();
                }
            },
            scope: $scope,
            preserveScope:true,
            templateUrl:  sbiModule_config.contextName +'/qbe/templates/temporalOperand.html',

            clickOutsideToClose:true
        })
	})

    $scope.showVisualization = function (filters, advancedFilters, expression) {
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/filterVisualizationTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,mdPanelRef){
					$scope.model = {"filters":filters,"advancedFilters":advancedFilters,"expression":expression,"mdPanelRef":mdPanelRef};
				},
				locals: {filters:filters,advancedFilters:advancedFilters, expression:expression},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	}

	$scope.openFilters = function(field, tree, pars, queryFilters, subqueries, expression, advancedFilters) {
		field_copy = angular.copy(field);
		field = {};
		if(field_copy.hasOwnProperty('attributes')){
			field.id = field_copy.id;
			field.name = field_copy.text;
			field.entity = field_copy.attributes.entity;
			field.iconCls = field_copy.attributes.iconCls;
			field.color = field_copy.color;
			field.visible= true;
			field.group= false;
			field.order= 1;
			field.filters= [];
		} else {
			field = field_copy.field
		}
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/filterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,field,mdPanelRef){
					$scope.model ={ "field": field, "tree": tree, "pars": pars,"mdPanelRef":mdPanelRef, "queryFilters":queryFilters, "subqueries":subqueries, "expression":expression, "advancedFilters":advancedFilters};
				},
				locals: {field: field, tree: tree, pars: pars, queryFilters:queryFilters, subqueries: subqueries, expression : expression, advancedFilters : advancedFilters},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	};

	$scope.openHavings = function(field) {

		if (field.hasOwnProperty('attributes')) {
			field_copy = angular.copy(field);
			field = {};
			field.id = field_copy.id;
			field.name = field_copy.text;
			field.entity = field_copy.attributes.entity;
			field.iconCls = field_copy.attributes.iconCls;
			field.color = field_copy.color;
			field.visible = true;
			field.group = false;
			field.order = 1;
		}
		var finishEdit = $q.defer();
		var config = {
			attachTo : angular.element(document.body),
			templateUrl : sbiModule_config.contextName
					+ '/qbe/templates/havingTemplate.html',
			position : $mdPanel.newPanelPosition().absolute().center(),
			fullscreen : true,
			controller : function($scope, havings, tree, subqueries, selectedFields, mdPanelRef) {
				$scope.model = {
					"havings" : havings,
					"field" : field,
					"tree" : tree,
					"mdPanelRef" : mdPanelRef,
					"subqueries" : subqueries,
					"selectedFields" : selectedFields
				};
			},
			locals : {
				"havings" : $scope.editQueryObj.havings,
				"field" : field,
				"tree" : $scope.entityModel,
				"subqueries" : $scope.editQueryObj.subqueries,
				"selectedFields" : $scope.editQueryObj.fields
			},
			hasBackdrop : true,
			clickOutsideToClose : true,
			escapeToClose : true,
			focusOnOpen : true,
			preserveScope : true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	};

    $scope.openMenu = function($mdMenu, ev) {
        originatorEv = ev;
        $mdMenu.open(ev);
    };

    $scope.createQueryName = function(){
    	var lastcount = 0;
    	var lastIndex = $scope.subqueriesModel.subqueries.length-1;
    	if(lastIndex!=-1){
    		var lastQueryId = $scope.subqueriesModel.subqueries[lastIndex].id;
    		lastcount = parseInt(lastQueryId.substr(1));
    	}else{
    		lastcount = 1;
    	}

    	return lastcount +1;
    }
    $scope.createSubquery = function(){
    	var subquery = new Query($scope.createQueryName());
    	$scope.query.subqueries.push(subquery);
    	$scope.editQueryObj = subquery;
    }
    $scope.stopEditingSubqueries = function(){
    	$scope.editQueryObj = $scope.query;
    }

    if(parent.globalQbeJson){
    	var qbeJsonObj = angular.fromJson(parent.globalQbeJson)
    	$scope.editQueryObj = qbeJsonObj.catalogue.queries[0];
    }

    $scope.relationsListColumns = [
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.relation.name"), "name": "relationName"},
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.source.fields"), "name": "sourceFields"},
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.target.entity"), "name": "targetEntity"},
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.target.fields"), "name": "targetFields"},
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.target.fields"), "name": "joinType"}
    ]

    $scope.showInfo = function(item, ev) {

        $scope.entityItem = item;

        if($scope.entityItem.relation.length > 0) {
	    	$mdDialog.show({
	            controller: function ($scope, $mdDialog) {

	                $scope.ok= function(){
	                	console.log($scope)
	                    $mdDialog.hide();
	                }
	            },
	            scope: $scope,
	            preserveScope:true,
	            templateUrl:  sbiModule_config.contextName +'/qbe/templates/relations.html',

	            clickOutsideToClose:true
	        })
        } else {
        	$mdDialog.show(
        		$mdDialog.alert()
        		     .clickOutsideToClose(true)
        		     .title($scope.entityItem.text)
        		     .textContent($scope.translate.load("kn.qbe.alert.norelations"))
        		     .ok($scope.translate.load("kn.qbe.general.ok"))
            );
        }
    };

    $scope.deleteCalculatedField = function (selectedField){

    	var field = {};
    	field.id = selectedField.attributes.formState;
    	field.alias = selectedField.text;
    	field.type =selectedField.attributes.formState.type;
    	field.expression = selectedField.attributes.formState.expressionSimple;
    	field.calculationDescriptor= field.id;

    	var deleteCalclatedFieldAction  = $scope.sbiModule_action_builder.getActionBuilder("POST");
    	deleteCalclatedFieldAction.actionName = "DELETE_CALCULATED_FIELD_ACTION";
    	deleteCalclatedFieldAction.formParams.entityId = selectedField.id.substring(0,selectedField.id.indexOf(":"))+"::"+selectedField.attributes.entity;
    	deleteCalclatedFieldAction.formParams.field = field ;
    	deleteCalclatedFieldAction.executeAction().then(function(){
    		 $scope.getEntityTree();
    		 sbiModule_messaging.showSuccessMessage($scope.translate.load("kn.qbe.calculatedFields.delete"), $scope.translate.load("kn.qbe.general.success"));
    	},
    	 function(response){
    		sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load("kn.qbe.general.error"));
		})
	}


	$scope.showCalculatedField = function(cf,ev){
    	$scope.cfSelectedField= angular.copy(cf);
    	$scope.originalCFname = "";
    	$mdDialog.show({
            controller: function ($scope, $mdDialog) {

            	$scope.calculatedFieldOutput = new Object;
            	if($scope.cfSelectedField){
            		$scope.modifyCF = true;
            		$scope.originalCFname = angular.copy($scope.cfSelectedField.name);
            		$scope.calculatedFieldOutput.alias = $scope.cfSelectedField.name;
                   	$scope.calculatedFieldOutput.formula = $scope.cfSelectedField.id.expression;
                   	$scope.calculatedFieldOutput.expression = $scope.cfSelectedField.id.expressionSimple;
                	$scope.calculatedFieldOutput.type =$scope.cfSelectedField.id.type;
                	$scope.calculatedFieldOutput.nature= $scope.cfSelectedField.id.nature;
            	}
                $scope.hide = function() {
                	if($scope.originalCFname!=""){
                		for (var i = 0; i < $scope.editQueryObj.fields.length; i++) {
							if($scope.editQueryObj.fields[i].alias==$scope.originalCFname) $scope.editQueryObj.fields.splice(i);
						}
                	}
                	//parameters to add in the calculatedFieldOutput object to prepare it for the sending
                	$scope.addedParameters = {
            			"alias":$scope.calculatedFieldOutput.alias,
            			"type":angular.copy($scope.calculatedFieldOutput.type),
            			"nature":angular.copy($scope.calculatedFieldOutput.nature),
            			"expression":$scope.calculatedFieldOutput.formula,
            			"expressionSimple":$scope.calculatedFieldOutput.expression,
                	}

                	$scope.calculatedFieldOutput.id = $scope.addedParameters;
                	$scope.calculatedFieldOutput.type = $scope.calculatedFieldOutput.fieldType;
                	$scope.calculatedFieldOutput.fieldType = $scope.calculatedFieldOutput.nature.toLowerCase();
                	$scope.calculatedFieldOutput.entity = $scope.calculatedFieldOutput.alias;
                	$scope.calculatedFieldOutput.field = $scope.calculatedFieldOutput.alias;
                	$scope.calculatedFieldOutput.funct = "";
                	$scope.calculatedFieldOutput.group = false;
                	$scope.calculatedFieldOutput.order = "";
                	$scope.calculatedFieldOutput.include = true;
                	$scope.calculatedFieldOutput.visible = true;
                	$scope.calculatedFieldOutput.longDescription = $scope.addedParameters.expression;
                	$scope.editQueryObj.fields.push($scope.calculatedFieldOutput);
                	$scope.addField($scope.calculatedFieldOutput, true);

                	$mdDialog.hide()};
                $scope.cancel = function() {$mdDialog.cancel()};
            },
            templateUrl: sbiModule_config.contextName +'/qbe/templates/calculatedFieldsDialog.html',
            targetEvent: ev,
            clickOutsideToClose: true,
            scope: $scope,
            preserveScope: true
        })
        .then(function() {

        },function() {
        	$scope.calculatedFieldOutput={};
        });
    };

    $scope.getEntityTree = function(){
    	entityService.getEntitiyTree(inputParamService.modelName).then(function(response){
   			$scope.entityModel = response.data;
   			$scope.filteredFormulas = byNotExistingMembersFilter($scope.formulas,'type',['space'],{space:'geographic_dimension'},$scope.entityModel.entities,'iconCls')
   		}, function(response){
   			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load("kn.qbe.general.error"));
   		});
    }

    $scope.saveCC = function(selectedEntity,cc){
    	var addCalclatedFieldAction  = $scope.sbiModule_action_builder.getActionBuilder("POST");

    	addCalclatedFieldAction.actionName = "ADD_CALCULATED_FIELD_ACTION";
    	addCalclatedFieldAction.queryParams.datamartName = inputParamService.modelName;
    	addCalclatedFieldAction.formParams.field = cc;
    	addCalclatedFieldAction.formParams.editingMode = "create";
    	addCalclatedFieldAction.formParams.fieldId = "";
    	if($scope.modifyCF){
    		addCalclatedFieldAction.formParams.editingMode = "modify";
        	addCalclatedFieldAction.formParams.fieldId = $scope.originalCFname;
    	}
    	addCalclatedFieldAction.formParams.entityId = selectedEntity;

    	addCalclatedFieldAction.executeAction().then(function(){
    		$scope.getEntityTree();
     		sbiModule_messaging.showSuccessMessage("Calculated field is added", $scope.translate.load("kn.qbe.general.success"));
    	},function(response){
    		sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load("kn.qbe.general.error"));
		})
    }

    $scope.saveEntityTree = function(){
    	var saveTreeAction  = $scope.sbiModule_action_builder.getActionBuilder("GET");
    	saveTreeAction.actionName = "SAVE_TREE_ACTION";
    	saveTreeAction.executeAction().then(function(response){
    		sbiModule_messaging.showSuccessMessage($scope.translate.load("kn.qbe.expander.list.entities.tree.saved"), $scope.translate.load("kn.qbe.expander.list.entities.tree.saving"));
		}, function(response){
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load("kn.qbe.expander.list.entities.tree.saving"));
		});

    }

    $scope.getEntityTree();
}