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
	.module('qbe.controller', ['configuration','directive','services', 'exportModule'])
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
		"exportService",
		"filterFilter",
		qbeFunction]);


function qbeFunction($scope,$rootScope,$filter,entity_service,query_service,filters_service,formulaService,save_service,sbiModule_inputParams,sbiModule_translate,sbiModule_config,sbiModule_action,sbiModule_action_builder,sbiModule_restServices,sbiModule_messaging, sbiModule_user,windowCommunicationService, $mdDialog ,$mdPanel,$q,byNotExistingMembersFilter,selectedEntitiesRelationshipsService,queryEntitiesService,expression_service, exportService,filterFilter){
	$scope.translate = sbiModule_translate;
	$scope.sbiModule_action_builder = sbiModule_action_builder;
	var entityService = entity_service;
	var inputParamService = sbiModule_inputParams;
	$scope.filterFilter = filterFilter;
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
	$scope.show = true;
	$scope.fromDataset = false;
	var consoleHandler = {}
	consoleHandler.handleMessage = function(message){
		if(message === 'close'){
			var saveObj = {};
			saveObj.message = message
			saveObj.qbeQuery = {catalogue: {queries: [$scope.editQueryObj]}};
			saveObj.pars = $scope.pars;
			comunicator.sendMessage(saveObj);
		}
		if(message === 'saveDS'){
			var saveObj = {};
			saveObj.name = "workspace"
			saveObj.qbeQuery = {catalogue: {queries: [$scope.editQueryObj]}};
			saveObj.meta = $scope.meta;
			saveObj.pars = $scope.pars;
			comunicator.sendMessage(saveObj);
		}
		if(message.qbeJSONQuery){
	    	var qbeJsonObj = angular.fromJson(message.qbeJSONQuery)
	    	$scope.fromDataset = true
	    	$scope.editQueryObj = qbeJsonObj.catalogue.queries[0];
		}
		if(message.smartView != undefined) {
			query_service.smartView = message.smartView;
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

	exportService.getExportLimitation().then(function(response) {
		if(response.data){
			exportService.setExportLimit(response.data);
		};
	});

	var queryHandler = function(newCatalogue,oldCatalogue){
		$scope.meta.length = 0;
		exportService.setQuery(newCatalogue);
		for (var i = 0; i < newCatalogue.fields.length; i++) {
			var meta = {
					"displayedName":newCatalogue.fields[i].alias,
					"name":newCatalogue.fields[i].alias,
					"fieldType":newCatalogue.fields[i].fieldType.toUpperCase(),
					"dataType":newCatalogue.fields[i].dataType,
					"format":newCatalogue.fields[i].format,
					"type":newCatalogue.fields[i].type
			}
			$scope.meta.push(meta);
		}
		if($scope.fromDataset){
			$scope.bodySend.catalogue = [];
			$scope.bodySend.catalogue.push($scope.editQueryObj);
		}
		$scope.filters = $scope.editQueryObj.filters;
		$scope.havings = $scope.editQueryObj.havings;

		if(query_service.smartView){
			query_service.count++;
			var finalPromise = 	$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel, false);
			if(finalPromise) {
				finalPromise.then(function(response){
					$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
					exportService.setBody($scope.bodySend);
					window.parent.queryCatalogue = {catalogue: {queries: [$scope.editQueryObj]}};
				},function(){
					angular.copy(oldCatalogue,$scope.editQueryObj);
					for(var i in $scope.previousVersionRelations){
						var relationship = $scope.previousVersionRelations[i]
						$scope.filteredRelations[i].isConsidered = relationship.isConsidered;

					}

					if(query_service.count > 1){
						query_service.setSmartView(false);
						$rootScope.$emit('smartView');
						query_service.count = 0;
					}
				});
			}else{
				$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
				exportService.setBody($scope.bodySend);
				window.parent.queryCatalogue = {catalogue: {queries: [$scope.editQueryObj]}};
			}
		} else {
			$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
			exportService.setBody($scope.bodySend);
			window.parent.queryCatalogue = {catalogue: {queries: [$scope.editQueryObj]}};
		}


	}
	$scope.$watch('bodySend',function(newValue,oldValue){
		if(angular.equals(newValue.catalogue,oldValue.catalogue) && !angular.equals(newValue.pars,oldValue.pars)){
			queryHandler(newValue.catalogue[0],oldValue.catalogue[0])
		}


	},true)

	$scope.$watch('editQueryObj',function(newValue,oldValue){
		queryHandler(newValue,oldValue);
	},true)

	$scope.$watch('queryModel',function(newValue,oldValue){
		toEditQueryObj(newValue,$scope.editQueryObj)
	},true)

	$scope.executeQuery = function ( query, bodySend, queryModel, isCompleteResult, start, itemsPerPage) {

		if(query.fields.length>0){
			return query_service.executeQuery( query, bodySend, queryModel, true, start, itemsPerPage);
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
				templateUrl: sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/saveTemplate.html',
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

				var currField = query.fields[i];

				var queryObject = {
						"id":currField.id,
						"key": "column_" + (i+1),
						"name":currField.field,
						"alias":currField.alias,
						"entity":currField.entity,
						"color":currField.color,
						"type":currField.type,
						"funct":currField.funct,
						"fieldType" : currField.fieldType,
						"inUse":currField.inUse,
						"visible":currField.visible,
						"distinct":$scope.editQueryObj.distinct,
						"iconCls":currField.iconCls,
						"group":currField.group,
						"order":i+1,
						"ordering":currField.order,
						"filters": [],
						"havings": [],
						"dataType": currField.dataType,
						"format": currField.format
					}

				if(currField.temporal){
					queryObject.temporal = currField.temporal;
				}
				queryModel.push(queryObject);
			}
		}else{
			queryModel.length = 0;
		}
	}

	$scope.onDropComplete=function(field,evt){
		if(field.connector) return;
		if(field.children){
			for(var i in field.children){
				$scope.addField(field.children[i])
			}
		}else{
			$scope.addField(field);
		}


    };

    $rootScope.$on('modelChanged' , function(event,data){
    	toEditQueryObj(data,$scope.editQueryObj)
    })

    $rootScope.$on('addFieldOnClick', function (event, data) {
    	if(data.connector) return;
    	$scope.addField(data);
    });

	$rootScope.$on('applyFunction', function (event, data) {
		var indexOfEntity = findWithAttr($scope.entityModel.entities,'text', data.entity);
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
	});

	$rootScope.$on('applyFunctionForParams', function (event, data) {
		if(data.pars!= undefined && data.pars!= null ) {
			$scope.pars = data.pars;

		}
	});

	var toEditQueryObj = function(customTableModel,editQueryObj){
		for (var i = 0; i < customTableModel.length; i++) {

			var currEditQueryObjField = editQueryObj.fields[i];
			var currCustomTableModel = customTableModel[i];

			currEditQueryObjField.group = currCustomTableModel.group;
			currEditQueryObjField.funct = currCustomTableModel.funct;
			currEditQueryObjField.visible = currCustomTableModel.visible;
			currEditQueryObjField.distinct = currCustomTableModel.distinct;
			currEditQueryObjField.order = currCustomTableModel.ordering;
			currEditQueryObjField.alias = currCustomTableModel.alias;
			currEditQueryObjField.inUse = currCustomTableModel.inUse;
		}
	}

	$rootScope.$on('smartView', function (event, data) {

		if(query_service.smartView){
			$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel);
		} else {
			$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
		}
	});

	$scope.$on('executeQuery', function (event, data) {
		$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel, true, data.start, data.itemsPerPage);
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

	$scope.$on('move',function(event,data){
		console.log(data)
		console.log($scope.editQueryObj.fields[data.index])
		var temp = $scope.editQueryObj.fields[data.index];
		$scope.editQueryObj.fields[data.index] = $scope.editQueryObj.fields[data.index+data.direction]
		$scope.editQueryObj.fields[data.index+data.direction] = temp;
	})

	$scope.$on('setOrder',function(event,data){


		var colIndex = 0;
		var orderMap = {};
		data.forEach(function(e) {
			orderMap[e] = colIndex++;
		});

		$scope.editQueryObj
			.fields
			.sort(function(a, b) {
				return orderMap[a.id] - orderMap[b.id];
			});

	})

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
		item.pars = JSON.stringify($scope.pars);
		sbiModule_action.promisePost('SET_CATALOGUE_ACTION',queryParam,item, conf).then(function(response){
			$scope.getSQL();
		}, function(response){
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load("kn.qbe.general.error"));
		});
	});

	$scope.getSQL = function () {
		item = {};
    	item.replaceParametersWithQuestion=true;
    	item.queryId = $scope.editQueryObj.id

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


	$rootScope.$on('removeColumns', function (event, data) {
		for(var i in data){
			// Remove columns
			var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data[i].id);
			var indexOfFieldInModel = findWithAttr($scope.queryModel,'id', data[i].id);
			if (indexOfFieldInQuery > -1 && indexOfFieldInModel > -1) {
				$scope.editQueryObj.fields.splice(indexOfFieldInQuery, 1);
				$scope.queryModel.splice(indexOfFieldInModel, 1);
			}
			// Remove having conditions
			var indexOfHavingCondInQuery = -1;
			while ((indexOfHavingCondInQuery = findWithAttr($scope.editQueryObj.havings,'leftOperandValue', data[i].id)) != -1) {
				$scope.editQueryObj.havings.splice(indexOfHavingCondInQuery, 1);
			}
			// Remove related filters
			var indexOfFilter = -1;
			while ((indexOfFilter = findWithAttr($scope.editQueryObj.filters,'leftOperandValue', data[i].id)) != -1) {
				$scope.editQueryObj.filters.splice(indexOfFilter, 1);
			}
			// Recreate $scope.editQueryObj.expression
			expression_service.generateExpressions($scope.editQueryObj.filters, $scope.editQueryObj.expression, $scope.advancedFilters);
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
				$scope.editQueryObj.fields[i].inUse = $scope.queryModel[i].inUse;
			}
			$scope.editQueryObj.fields[i].alias = $scope.queryModel[i].alias;
		}

		if(!calcField){
			$scope.isSpatial(field,$scope.entityModel);
			var newField  = {
					"id":field.attributes.type === "inLineCalculatedField" ? field.attributes.formState : field.id,
					"alias":field.attributes.field,
					"type":field.attributes.type === "inLineCalculatedField" ? "inline.calculated.field" : "datamartField",
					"fieldType":field.attributes.iconCls,
					"entity":field.attributes.entity,
					"field":field.attributes.field,
					"funct":getFunct(field,"measure"),
					"color":field.color,
					"group":getGroup(field),
					"order":"NONE",
					"include":true,
					"inUse":field.hasOwnProperty("inUse") ? field.inUse : true,
					"visible":true,
					"iconCls":field.iconCls,
					"dataType":field.dataType,
					"format":field.format,
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

	var getFunct =function(field){

		 if(isColumnType(field,"measure") && field.aggtype){
			return field.aggtype
		}else if(isColumnType(field,"measure")){
			return "SUM"
		}
			return "NONE";
	}

	var getGroup = function(field){
		return isColumnType(field,"attribute")&&!isDataType(field,'com.vividsolutions.jts.geom.Geometry')
	}

	var isDataType = function(field,dataType){
		return field.dataType == dataType
	}

	var isColumnType = function(field,columnType){
		return field.iconCls==columnType || isCalculatedFieldColumnType(field,columnType)
	}

	$scope.isSpatial = function(field){
		var filtered = this.filterFilter(this.entityModel.entities,{children:{id:field.id}})
		return filtered.length>0 && filtered[0].iconCls == "geographic_dimension";

	}

	$scope.hasSpatialField = function(fields){

		if(fields){
			for(var i =0;i<fields.length;i++){
				if($scope.isSpatial(fields[i])){
					return true;
				}
			}
		}

		return false;

	}

	$scope.getFilteredFormulas = function(formulas,fields){
		if(!$scope.hasSpatialField(fields)){
			return $filter('filter')(formulas,{type:"!space"})
		}

		return formulas;
	}

	var isInLineCalculatedField = function(field){
		return field.attributes.type === "inLineCalculatedField"
	}

	var isCalculatedFieldColumnType = function(inLineCalculatedField,columnType){
		return isInLineCalculatedField(inLineCalculatedField) && inLineCalculatedField.attributes.formState.nature === columnType
	}

	$scope.colors = ['#D7263D', '#F46036', '#2E294E', '#1B998B', '#C5D86D', '#3F51B5', '#8BC34A', '#009688', '#F44336'];

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
        	  $scope.stopEditingSubqueries(false);
        }
    }];

    $scope.fieldsFunctions = [

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
                templateUrl:  sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/joinDefinitionsDialog.html',

                clickOutsideToClose:true
            })

    	})

    }

	$scope.openDialogForParams = function(pars,filters,expression,advancedFilters){
    	var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/parameterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
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
            templateUrl:  sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/temporalOperand.html',

            clickOutsideToClose:true
        })
	})

    $scope.showVisualization = function (filters, advancedFilters, expression) {
		var finishEdit=$q.defer();
		//$scope.show = false;
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/filterVisualizationTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				controller: function($scope,mdPanelRef){
					$scope.model = {"filters":filters,"advancedFilters":advancedFilters,"expression":expression,"mdPanelRef":mdPanelRef};
				},
				locals: {filters:filters,advancedFilters:advancedFilters, expression:expression},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				onCloseSuccess:function(){$scope.show = true},
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
			field.dataType = field_copy.dataType;
			field.longDescription = field_copy.attributes.longDescription;
			field.format = field_copy.format;
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
				templateUrl: sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/filterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				controller: function($scope,mdPanelRef){
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
			templateUrl : sbiModule_config.dynamicResourcesEnginePath
					+ '/qbe/templates/havingTemplate.html',
			position : $mdPanel.newPanelPosition().absolute().center(),
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
	$scope.stopEditingSubqueries = function(check){
		if (typeof check == "undefined") {
			check = true;
		}
		if (check && $scope.editQueryObj.fields.length != 1) {
			sbiModule_messaging.showErrorMessage($scope.translate.load("kn.qbe.subquery.onefielderror"), $scope.translate.load("kn.qbe.general.error"));
		} else {
			$scope.editQueryObj = $scope.query;
		}
	}
	comunicator.sendMessage("qbeJSONQuery");

    $scope.relationsListColumns = [
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.relation.name"), "name": "relationName"},
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.source.fields"), "name": "sourceFields"},
    	{"label": $scope.translate.load("kn.qbe.dialog.table.column.target.entity"), "name": "targetEntityLabel"},
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
	            templateUrl:  sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/relations.html',

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
		$scope.filteredFormulas = $scope.getFilteredFormulas($scope.formulas,$scope.editQueryObj.fields)
		$mdDialog.show({
				controller: function ($scope, $mdDialog) {

					function cleanExpression(expression) {
						// Replace non-breaking space
						expression = expression.replaceAll(/\u00a0/g, " ");

						return expression;
					}

					$scope.calculatedFieldOutput = new Object;
					if($scope.cfSelectedField){
						$scope.modifyCF = true;
						$scope.originalCFname = angular.copy($scope.cfSelectedField.name);
						$scope.calculatedFieldOutput.alias = $scope.cfSelectedField.name;
						angular.copy($scope.cfSelectedField.id.expression,$scope.calculatedFieldOutput.formula) ;
						$scope.calculatedFieldOutput.expression = $scope.cfSelectedField.id.expressionSimple;
						$scope.calculatedFieldOutput.type =$scope.cfSelectedField.id.type;
						$scope.calculatedFieldOutput.format =$scope.cfSelectedField.id.format;
						$scope.calculatedFieldOutput.nature= $scope.cfSelectedField.id.nature;
						$scope.calculatedFieldOutput.formula = $scope.cfSelectedField.id.expression;
					}

					$scope.hide = function() {
						if($scope.originalCFname!=""){
							for (var i = 0; i < $scope.editQueryObj.fields.length; i++) {
								if($scope.editQueryObj.fields[i].alias==$scope.originalCFname){
									$scope.editQueryObj.fields.splice(i,1);
									$scope.queryModel.splice(i,1);
								}
							}
						}
						//parameters to add in the calculatedFieldOutput object to prepare it for the sending
						$scope.addedParameters = {
							"alias":$scope.calculatedFieldOutput.alias,
							"type":angular.copy($scope.calculatedFieldOutput.type),
							"nature":angular.copy($scope.calculatedFieldOutput.nature),
							"expression":$scope.calculatedFieldOutput.formula,
							"expressionSimple":$scope.calculatedFieldOutput.expression,
							"format": $scope.calculatedFieldOutput.format
						}

						$scope.calculatedFieldOutput.id = $scope.addedParameters;
						$scope.calculatedFieldOutput.type = $scope.calculatedFieldOutput.fieldType;
						$scope.calculatedFieldOutput.format = $scope.calculatedFieldOutput.format;
						$scope.calculatedFieldOutput.fieldType = $scope.calculatedFieldOutput.nature.toLowerCase();
						$scope.calculatedFieldOutput.entity = $scope.calculatedFieldOutput.alias;
						$scope.calculatedFieldOutput.field = $scope.calculatedFieldOutput.alias;
						$scope.calculatedFieldOutput.funct = $scope.calculatedFieldOutput.nature=="MEASURE" ? "SUM" : "";
						$scope.calculatedFieldOutput.group = $scope.calculatedFieldOutput.nature=="ATTRIBUTE" ? true : false;
						$scope.calculatedFieldOutput.order = "";
						$scope.calculatedFieldOutput.include = true;
						$scope.calculatedFieldOutput.inUse = true;
						$scope.calculatedFieldOutput.visible = true;
						$scope.calculatedFieldOutput.id.expression = cleanExpression($scope.calculatedFieldOutput.id.expression);
						$scope.calculatedFieldOutput.id.expressionSimple = cleanExpression($scope.calculatedFieldOutput.id.expressionSimple);
						$scope.calculatedFieldOutput.formula = cleanExpression($scope.calculatedFieldOutput.formula);
						$scope.calculatedFieldOutput.expression = cleanExpression($scope.calculatedFieldOutput.expression);
						$scope.calculatedFieldOutput.longDescription = cleanExpression($scope.addedParameters.expression);
						$scope.editQueryObj.fields.push($scope.calculatedFieldOutput);
						$scope.addField($scope.calculatedFieldOutput, true);

						$mdDialog.hide()
					};
					$scope.cancel = function() {$mdDialog.cancel()};
				},
				templateUrl: sbiModule_config.dynamicResourcesEnginePath +'/qbe/templates/calculatedFieldsDialog.html',
				targetEvent: ev,
				clickOutsideToClose: true,
				scope: $scope,
				preserveScope: true
			})
		.then(function() {},
			function() {
				$scope.calculatedFieldOutput={};
			});
	};

    $scope.getEntityTree = function(){
    	entityService.getEntitiyTree(inputParamService.modelName).then(function(response){
   			$scope.entityModel = response.data;

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
