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

var datasetModule = angular.module('datasetModule', ['ngMaterial', 'angular-list-detail', 'sbiModule', 'angular_table', 'file_upload', 'ui.codemirror','expander-box']);

datasetModule.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

datasetModule
	.controller('datasetController', ["$scope", "$log", "$http", "sbiModule_config", "sbiModule_translate", "sbiModule_restServices", "sbiModule_messaging", "$mdDialog", "multipartForm", "$timeout", datasetFunction])
	.service('multipartForm',['$http',function($http){
			
			this.post = function(uploadUrl,data){
				
				var formData = new FormData();
	    		for(var key in data){
	    				formData.append(key,data[key]);
	    			}
				return	$http.post(uploadUrl,formData,{
						transformRequest:angular.identity,
						headers:{'Content-Type': undefined}
					})
			}
			
		}]);


function datasetFunction($scope, $log, $http, sbiModule_config, sbiModule_translate, sbiModule_restServices, sbiModule_messaging, $mdDialog, multipartForm, $timeout){
	
	$scope.translate = sbiModule_translate;
	$scope.codeMirror = null;
	$scope.isSomething = false;
	
	$scope.dataSetListColumns = [
	    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
	    {"label":$scope.translate.load("sbi.generic.label"),"name":"label"},
	    {"label":$scope.translate.load("sbi.generic.type"), "name":"dsTypeCd"},
	    {"label":$scope.translate.load("sbi.ds.numDocs"), "name":"usedByNDocs"}
    ];
	
	$scope.selectedDatasetVersion = null;
	
	$scope.dataSourceList = [];
	$scope.datamartList = [];
	
	/**
	 * The 'datasetsListPersisted' is the initial and DB aligned dataset list (collection) is needed when dealing with not
	 * yet saved dataset items (when cloning existing or creating new datasets, as well as when performing their removal
	 * from the AT). The 'datasetsListTemp' contains the current AT state.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.datasetsListTemp = [];
	$scope.datasetsListPersisted = [];
	
	// Initialization of scope variables that will show their AT the value of the number of its last page (when adding new items)
	$scope.datasetTableLastPage = 1;
	$scope.parametersTableLastPage = 1;
	$scope.restDsRequestHeaderTableLastPage = 1;
	$scope.restDsJsonPathAttribTableLastPage = 1;
	
	$scope.fileObj={};
	$scope.selectedTab = 0;	// Initially, the first tab is selected.
	$scope.tempScope = {};
	$scope.showSaveAndCancelButtons = false;
		
	$scope.schedulingMonths = function(item) {
		
		switch(item) {
			case 1: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.january"); break;
			case 2: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.february"); break;
			case 3: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.march"); break;
			case 4: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.april"); break;
			case 5: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.may"); break;
			case 6: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.june"); break;
			case 7: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.july"); break;
			case 8: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.august"); break;			
			case 9: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.september"); break;
			case 10: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.october"); break;
			case 11: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.november"); break;
			case 12: $scope.schedulingMonthsMap = $scope.translate.load("sbi.ds.persist.cron.month.december"); break;
		}
		
	}
	
	// Setting for minutes for Scheduling
	$scope.minutes = new Array(60);
	
	// TODO: 
	$scope.minutesClearSelections = function() {
		console.log("Clear minutes (TODO)");
		$scope.minutesSelected = [];
	}
	
	// Setting for hours for Scheduling
	$scope.hours = new Array(24);
	
	// TODO: 
	$scope.hoursClearSelections = function() {
		console.log("Clear hours (TODO)");
		$scope.hoursSelected = [];
	}
	
	$scope.days = new Array();
	
	// Setting for hours for Scheduling
	var populateDays = function() {		
		
		for (i=1; i<=31; i++) {
			$scope.days.push(i);
		}
		
	}
		
	populateDays();
		
	// TODO: 
	$scope.daysClearSelections = function() {
		console.log("Clear days (TODO)");
		$scope.daysSelected = [];
	}
	
	// Setting for month for Scheduling
	$scope.months = new Array();
	
	var populateMonths = function() {
		
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.january")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.february")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.march")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.april")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.may")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.june")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.july")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.august")); 			
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.september")); 
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.october"));
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.november"));
		$scope.months.push($scope.translate.load("sbi.ds.persist.cron.month.december"));
		
	}
	
	populateMonths();
	
	// TODO: 
	$scope.monthsClearSelections = function() {
		console.log("Clear months (TODO)");
		$scope.monthsSelected = [];
	}	
	
	// Setting for month for Scheduling
	$scope.weekdays = new Array();
	
	var populateWeekdays = function() {
		
		$scope.weekdays.push($scope.translate.load("sbi.ds.persist.cron.weekday.monday")); 
		$scope.weekdays.push($scope.translate.load("sbi.ds.persist.cron.weekday.tuesday")); 
		$scope.weekdays.push($scope.translate.load("sbi.ds.persist.cron.weekday.wednesday")); 
		$scope.weekdays.push($scope.translate.load("sbi.ds.persist.cron.weekday.thursday")); 
		$scope.weekdays.push($scope.translate.load("sbi.ds.persist.cron.weekday.friday")); 
		$scope.weekdays.push($scope.translate.load("sbi.ds.persist.cron.weekday.saturday")); 
		$scope.weekdays.push($scope.translate.load("sbi.ds.persist.cron.weekday.sunday")); 
		
	}
	
	populateWeekdays();
	
	// TODO: 
	$scope.weekdaysClearSelections = function() {
		console.log("Clear weekdays (TODO)");
		$scope.weekdaysSelected = [];
	}	
	
	// Flag that indicates if the Dataset form is dirty (changed)
	$scope.dirtyForm = false;
	
	// Functions for setting the indicator 
	$scope.setFormDirty = function() {
		console.log("set dirty");
		$scope.dirtyForm = true;
	}
	
	// Functions for resetting the indicator 
	$scope.setFormNotDirty = function() {
		$scope.dirtyForm = false;
	}
	
	// CKAN DATASET CONFIG
	$scope.ckanFileType = 
	[ 
	 	{value:"xls",name:"XLS"},
	 	{value:"csv",name:"CSV"}
 	];
	
	/**
	 * Static (fixed) values for three comboboxes that appear when the CSV file is uploaded.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
	 */
	$scope.csvEncodingTypes = 
	[ 
	 	{value:"windows-1252",name:"windows-1252"},
	 	{value:"UTF-8",name:"UTF-8"},
	 	{value:"UTF-16",name:"UTF-16"},
	 	{value:"US-ASCII",name:"US-ASCII"},
	 	{value:"ISO-8859-1",name:"ISO-8859-1"}
 	];
	
	$scope.csvDelimiterCharacterTypes = 
	[ 
	 	{value:",",name:","}, 
	 	{value:";",name:";"},	 	
	 	{value:"\\t",name:"\\t"},	
	 	{value:"\|",name:"\|"}
 	];
	
	$scope.csvQuoteCharacterTypes = 
	[ 
	 	{value:"\"",name:"\""}, 
	 	{value:"\'",name:"\'"}
 	];
	
	// Dataset preview
	$scope.previewDatasetModel=[];
    $scope.previewDatasetColumns=[];
    $scope.startPreviewIndex=0;
    $scope.endPreviewIndex=0;
    $scope.totalItemsInPreview=-1;	// modified by: danristo
    $scope.previewPaginationEnabled=true;     
    $scope.paginationDisabled = null;
    $scope.itemsPerPage=15;
    $scope.datasetInPreview=undefined;
	
	/**
	 * Keep and change the values for three comboboxes that appear when user uploads a CSV file when creating a new Dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
	 */
	$scope.chooseDelimiterCharacter = function(delimiterCharacterObj) {
		$scope.selectedDataSet.csvDelimiter = delimiterCharacterObj;
	}
	
	$scope.chooseQuoteCharacter = function(quoteCharacterObj) {
		$scope.selectedDataSet.csvQuote = quoteCharacterObj;
	}
	
	$scope.chooseEncoding = function(encodingObj) {
		$scope.selectedDataSet.csvEncoding = encodingObj;
	}
	
	/**
	 * Scope variables (properties) for the REST dataset.
	 * REQUEST HEADERS
	 * (danristo)
	 */
	
	/**
	 * HTTP methods collection needed for the REST dataset Type tab
	 * (danristo)
	 */
	$scope.httpMethods = [ 
	 	{value:"Get",name:"Get"},
	 	{value:"Post",name:"Post"},
	 	{value:"Put",name:"Put"},
	 	{value:"Delete",name:"Delete"}
 	];
	
	$scope.requestHeadersTableColumns = [
	     {
	    	 name:"name", 
	    	 label:"Name",
	    	 hideTooltip:true,
	    	 
	    	 transformer: function() {
	    		 return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.name" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
	    	 }
	     },
	     
	     {
	         name:"value",
	         label:"Value",
	         hideTooltip:true,
	         
	         transformer: function() {
	    		 return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.value" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
	    	 }	         
	     }
     ];	
	
	$scope.requestHeaderNameValues = [
		{value:"Accept",name:"Accept"},
		{value:"Content-Type",name:"Content-Type"}
	 ];
	
	$scope.requestHeaderValueValues =  [
		{value:"application/json",name:"application/json"},
		{value:"text/plain",name:"text/plain"}
	 ];
	
	$scope.changeDatasetScope = function() {
		
		/**
		 * If the dataset scope is changed and the new value is ENTERPRISE or TECHNICAL, while the dataset category
		 * is not yet picked, we need to set the indicator that will serve to inform user about the necessity to pick
		 * the category for one of those two dataset scopes.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (($scope.selectedDataSet.scopeCd.toUpperCase()=="ENTERPRISE" 
				|| $scope.selectedDataSet.scopeCd.toUpperCase()=="TECHNICAL") 
					&& (!$scope.selectedDataSet.catTypeVn 
							|| $scope.selectedDataSet.catTypeVn=="")) {
			$scope.isCategoryRequired = true;
		}
		else {
			$scope.isCategoryRequired = false;
		}
		
	}
	
	// Initial list for REST request headers for a new REST dataset
	$scope.restRequestHeaders = [];
	
	$scope.requestHeadersScopeFunctions = {
		setFormDirty: $scope.setFormDirty
	};
	
	$scope.requestHeaderAddItem = function() {
		
		$scope.restRequestHeaders.push({"name":"","value":"","index":$scope.counterRequestHeaders++});
		
		$timeout(			
				function() { 
					var page = $scope.tableLastPage("requestHeadersTable");	
					console.log(page);
					console.log($scope.restDsRequestHeaderTableLastPage);
					// If the page that is returned is less than the current one, that means that we are already on that page, so keep it (danristo)
					//console.log(page<$scope.restDsRequestHeaderTableLastPage);
					$scope.restDsRequestHeaderTableLastPage = (page<=$scope.restDsRequestHeaderTableLastPage)
						? $scope.restDsRequestHeaderTableLastPage : page;
					
					//console.log($scope.restDsRequestHeaderTableLastPage);
				}, 
				
				300
			);
		
	}	
	
	// Provide unique IDs for elements in the Request header grid, so we can remove them easily
	$scope.counterRequestHeaders = 0;
	
	$scope.requestHeadersDelete = 
	[
		{
			label: $scope.translate.load("sbi.generic.delete"),
		 	icon:'fa fa-trash' ,
		 	backgroundColor:'transparent',
		
		 	action: function(item) {
		 		
		 		console.log(item);
		 		
		 		// TODO: translate
		 		var confirm = $mdDialog.confirm()
	 	          .title("Delete of REST dataset request header")
	 	          .targetEvent(event)	 	          
	 	          .textContent("Are you sure you want to delete request header")
	 	          .ariaLabel("Delete of REST dataset request header")
	 	          .ok($scope.translate.load("sbi.general.yes"))
	 	          .cancel($scope.translate.load("sbi.general.No"));
		 		
		 		$mdDialog
	 				.show(confirm)
	 				.then(
	 						
	 						function() {
	 							
				 	        	for (i=0; i<$scope.restRequestHeaders.length; i++) {
						 							 	        		
						 			if ($scope.restRequestHeaders[i].index == item.index) {
						 				$scope.setFormDirty();
						 				$scope.restRequestHeaders.splice(i,1);
						 				break;
						 			}
						 			
						 		}
				 	        	
					 		}
						);	
		 		
	 		}
					
	 	}
	 ];
	
	/**
	 * Scope variables (properties) for the REST dataset.
	 * JSON PATH ATTRIBUTES
	 * (danristo)
	 */
	$scope.restJsonPathAttributesList = [ 
 	 	{value:"string",name:"string"},
 	 	{value:"int",name:"int"},
 	 	{value:"double",name:"double"},
 	 	{value:"date yyyy-MM-dd",name:"date yyyy-MM-dd"},
 	 	{value:"timestamp yyyy-MM-dd HH:mm:ss",name:"timestamp yyyy-MM-dd HH:mm:ss"},
 	 	{value:"time HH:mm:ss",name:"time HH:mm:ss"},
 	 	{value:"boolean",name:"boolean"}
  	];
	
	$scope.restJsonPathAttributesTableColumns = [
	     {
	    	 name:"name", 
	    	 label:"Name",
	    	 hideTooltip:true,
	    	 
	    	 transformer: function() {
	    		 return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.name" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
	    	 }
	     },
	     
	     {
	         name:"jsonPathValue",
	         label:"JSON path value",
	         hideTooltip:true,
	         
	         transformer: function() {
	    		 return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.jsonPathValue" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
	    	 }
	     },
	     
	     {
	         name:"typeOrJsonPathValue",
	         label:"Type or JSON path type",
	         hideTooltip:true,
	         
	         transformer: function() {
	    		 return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.jsonPathType" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
	    	 }
	     }
     ];
	
	$scope.restJsonPathAttributes = [];
	$scope.counterJsonAttributes = 0;
	
	$scope.jsonPathAttrScopeFunctions = {
		setFormDirty: $scope.setFormDirty
	};
	
	$scope.restJsonPathAttributesAddItem = function() {
		
		$scope.restJsonPathAttributes.push({"name":"","jsonPathValue":"","typeOrJsonPathValue":"", "index":$scope.counterJsonAttributes++});
		
		$timeout(			
				function() { 
					var page = $scope.tableLastPage("jsonPathAttrTable");						
					// If the page that is returned is less than the current one, that means that we are already on that page, so keep it (danristo)
					$scope.restDsJsonPathAttribTableLastPage = page<$scope.restDsJsonPathAttribTableLastPage ? $scope.restDsJsonPathAttribTableLastPage : page; 
				}, 
				
				300
			);
		
	}
		
	$scope.restJsonPathAttributesDelete = 
	[
	 	//Delete the REST request JSON path attribute
		{
			label: $scope.translate.load("sbi.generic.delete"),
		 	icon:'fa fa-trash' ,
		 	backgroundColor:'transparent',
		
		 	action: function(item) {
		 		console.log(item);
		 		
		 		// TODO: translate
		 		var confirm = $mdDialog.confirm()
	 	          .title("Delete of REST JSON path attribute")
	 	          .targetEvent(event)	 	          
	 	          .textContent("Are you sure you want to delete JSON path attribute")
	 	          .ariaLabel("Delete of REST JSON path attribute")
	 	          .ok($scope.translate.load("sbi.general.yes"))
	 	          .cancel($scope.translate.load("sbi.general.No"));
		 		
		 		$mdDialog
	 				.show(confirm)
	 				.then(
	 						
	 						function() {
	 							
	 							for (i=0; i<$scope.restJsonPathAttributes.length; i++) {
	 					 			
	 					 			if ($scope.restJsonPathAttributes[i].index == item.index) {
	 					 				$scope.setFormDirty();
	 					 				$scope.restJsonPathAttributes.splice(i,1);
	 					 				break;
	 					 			}
	 					 		}
				 	        	
					 		}
						);
		 		
	 		}
					
	 	}
	 ];
	
	
	/*
	 * Dataset parameters table.
	 * 
	 * */
	$scope.datasetParameterTypes = [
        {
        	name:"String", 
        	value:"String"
		},
		
        {
			name:"Number", 
			value:"Number"
		},
        
        {
			name:"Raw", 
			value:"Raw"
		},
        
        {
			name:"Generic", 
			value: "Generic"
		}
        
    ];
	
	$scope.paramScopeFunctions = {
		datasetParameterTypes: $scope.datasetParameterTypes,
		setFormDirty: $scope.setFormDirty
	};

	$scope.parametersColumns = [
	                            
        {
        	"label":$scope.translate.load("sbi.generic.name"), 
        	"name":"name", 
        	hideTooltip:true,
        	
        	transformer: function() {
        		return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.name" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
        	}
    	},
        
    	{
    		"label":$scope.translate.load("sbi.generic.type"), 
    		"name":"type", 
    		hideTooltip:true,
        	
        	transformer: function() {
        		return '<md-select ng-model=row.type class="noMargin" ng-change="scopeFunctions.setFormDirty()"><md-option ng-repeat="col in scopeFunctions.datasetParameterTypes" value="{{col.name}}">{{col.name}}</md-option></md-select>';
        	}
		},
        
    	{
			"label":$scope.translate.load("sbi.generic.defaultValue"), 
			"name":"defaultValue", 
			hideTooltip:true,
        	
        	transformer: function() {
        		return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.defaultValue" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
        	}
		}
		
    ];
	
	$scope.parameterItems = [];
	$scope.parametersCounter = 0;
	
	$scope.parametersAddItem = function() {
		
		$scope.parameterItems.push({"name":"","type":"", "defaultValue":"","index":$scope.parametersCounter++});
		
		$timeout(			
					function() { 
						var page = $scope.tableLastPage("datasetParametersTable");						
						// If the page that is returned is less than the current one, that means that we are already on that page, so keep it (danristo)
						$scope.parametersTableLastPage = page<$scope.parametersTableLastPage ? $scope.parametersTableLastPage : page; 
					}, 
					
					300
				);
		
	}
	
	$scope.parameterDelete = 
	[
	 	//Delete the parameter.
		{
			label: $scope.translate.load("sbi.generic.delete"),
		 	icon:'fa fa-trash' ,
		 	backgroundColor:'transparent',
		
		 	action: function(item) {

			 	// TODO: translate
		    	var confirm = $mdDialog.confirm()
			         .title("Delete dataset parameter")
			         .targetEvent(event)	 	          
			         .textContent("Are you sure you want to delete the dataset parameter?")
			         .ariaLabel("Delete dataset parameter")
			         .ok($scope.translate.load("sbi.general.yes"))
			         .cancel($scope.translate.load("sbi.general.No"));
				
				$mdDialog
					.show(confirm)
					.then(					
							function() {
								
								for (i=0; i<$scope.parameterItems.length; i++) {
						 			
						 			if ($scope.parameterItems[i].index == item.index) {
						 				$scope.setFormDirty();
						 				$scope.parameterItems.splice(i,1);
						 				break;
						 			}
						 		}	
								
					 		}
						);	
		 				 		
	 		}
					
	 	}
	 ];
	
	$scope.deleteAllParameters = function() {		
		
		// TODO: translate
    	var confirm = $mdDialog.confirm()
	         .title("Clear all dataset parameters")
	         .targetEvent(event)	 	          
	         .textContent("Are you sure you want to delete all dataset parameters")
	         .ariaLabel("Clear all dataset parameters")
	         .ok($scope.translate.load("sbi.general.yes"))
	         .cancel($scope.translate.load("sbi.general.No"));
		
		$mdDialog
			.show(confirm)
			.then(					
					function() {
						$scope.setFormDirty();
						$scope.parameterItems = [];	 	        	
			 		}
				);	
		
	}
	
	/**
	 * Provide bindings for the Custom attributes of the Custom dataset. (danristo)
	 */
	$scope.customAttributes = [];
	
	// Customization of the table columns (headers). The label is visible, whilst the name serves for binding.
	$scope.customAttributesTableColumns = 
	[
	 	{
	 		label:"Name",
	 		name:"name",
	 		hideTooltip:true,
	 		
	 		transformer: function() {
	 			return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.name"></md-input-container>';
	 		}
 		},
 		
	 	{
 			label:"Value",
 			name:"value",
 			hideTooltip:true,
 			
 			transformer: function() {
 				return '<md-input-container class="md-block" style="margin:0"><input ng-model="row.value"></md-input-container>';
 			}
		},
	 ];
	
	$scope.customAttrScopeFunctions = {
		dataset: $scope.selectedDataSet
	};
	
//	$scope.customAttributesNameItem = '<md-input-container class="md-block" style="margin:0"><input ng-model="_xxx2_"></md-input-container>';
//	$scope.customAttributesValueItem = '<md-input-container class="md-block" style="margin:0"><input ng-model="_xxx4_"></md-input-container>';
	
	$scope.customAttributesCounter = 0;
	
    $scope.addCustomAttributes = function() {
//    	$log.info("ADDING THE CUSTOM ATTRIBUTES");    	
    	$scope.customAttributes.push({"name":"","value":"","index":$scope.customAttributesCounter++});
    	
    }
    
    // TODO: Test if works well (delete some middle item)
    $scope.customAttributesDelete = 
	[
	 	//Delete the custom attribute.
		{
			label: $scope.translate.load("sbi.generic.delete"),
		 	icon:'fa fa-trash' ,
		 	backgroundColor:'transparent',
		
		 	action: function(item) {
		 		
		 		for (i=0; i<$scope.customAttributes.length; i++) {		 			
		 			if ($scope.customAttributes[i].index == item.index) {
		 				$scope.customAttributes.splice(i,1);
		 				break;
		 			}
		 		}
		 		
		 		
	 		}
					
	 	}
	 ];
    
    $scope.deleteAllCustomAttributes = function() {
    	$scope.customAttributes = [];
    }
	
	/*
	 * 	service that loads all datasets
	 *   																	
	 */
	$scope.loadAllDatasets = function(){
		
		// If you want to use server-side pagination of the Dataset list, use this commented line. (danristo)
//		sbiModule_restServices.promiseGet("1.0/datasets","pagopt","offset=0&fetchSize=5",null)
		
		sbiModule_restServices.promiseGet("1.0/datasets","pagopt")
			.then(function(response) {
				$scope.datasetsListTemp = angular.copy(response.data.root);
				$scope.datasetsListPersisted = angular.copy($scope.datasetsListTemp);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
		
	}
	
	$scope.loadAllDatasets();	
	
	/**
	 * Speed-menu option configuration for deleting of a dataset.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.manageDataset = 
	[
	 	// Delete the dataset.
	 	{
	 		label: $scope.translate.load("sbi.generic.delete"),
		 	icon:'fa fa-trash' ,
		 	backgroundColor:'transparent',
	   
		 	action: function(item,event) {
		 		
		 		// Deleting an existing (persisted) dataset 
		 		if (item.id && item.id != "") {
		 			
		 			var confirm = $mdDialog.confirm()
		 	          .title($scope.translate.load('sbi.ds.deletedataset'))
		 	          .targetEvent(event)	 	          
		 	          .textContent($scope.translate.format($scope.translate.load('sbi.ds.deletedataset.msg'), item.label))
		 	          .ariaLabel("Delete dataset")
		 	          .ok($scope.translate.load("sbi.general.yes"))
		 	          .cancel($scope.translate.load("sbi.general.No"));
		 			
		 			$mdDialog
		 				.show(confirm)
		 				.then(
		 						
		 						function() {
		 							
		 							sbiModule_restServices.promiseDelete("1.0/datasets", item.label, "/")
			 							.then(
			 									function(response) {
			 					
			 						   				sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.ds.deletedataset.success'), item.label));
			 						   				
			 						   				// If the dataset that is deleted is selected, deselect it and hence close its details.
			 						   				if ($scope.selectedDataSet && $scope.selectedDataSet.label == item.label) {
			 						   					$scope.selectedDataSet = null;			   					
			 						   				}
			 						   				
			 						   				// Find the dataset, that is deleted on the server-side, in the array of all datasets and remove it from the array.
			 						   				for (var i=0; i<$scope.datasetsListTemp.length; i++) {			   					
			 						   					if ($scope.datasetsListTemp[i].label == item.label) {
//			 						   						console.log($scope.dirtyForm);
//			 						   						console.log($scope.selectedDataSet.label == item.label);
			 						   						$scope.datasetsListTemp.splice(i,1);
			 						   						$scope.showSaveAndCancelButtons = false;
			 						   						break;
			 						   					}			   					
			 						   				}
			 						
			 						   			}, 
			 						   			
			 						   			function(response) {	
			 						   				
			 						   				// If there is a message that is provided when attempting to delete a dataset that is used in some documents.
				 					   				if (response.data && response.data.errors && Array.isArray(response.data.errors)) {
				 					   					sbiModule_messaging.showErrorMessage(response.data.errors[0].message);
				 					   				}
				 					   				else {
				 					   					sbiModule_messaging.showErrorMessage($scope.translate.format($scope.translate.load('sbi.ds.deletedataset.error'), item.label));
				 					   				}
			 						   				
			 						   			}
			 								);
		 							
		 						},
		 						
		 						function() {
		 							
		 						}
		 						
	 						);
			 		}
		 			// Deleting an unsaved (unpersisted) dataset (the new one or the cloned one)
		 			else {
		 						 				
		 				var confirm = $mdDialog.confirm()
			 	          .title($scope.translate.load('sbi.ds.deletedataset'))
			 	          .targetEvent(event)	 	          
			 	          .textContent($scope.translate.load('sbi.ds.deletedataset.notsaved.msg'))
			 	          .ariaLabel("Delete dataset")
			 	          .ok($scope.translate.load("sbi.general.yes"))
			 	          .cancel($scope.translate.load("sbi.general.No"));
			 			
			 			$mdDialog
			 				.show(confirm)
			 				.then(		 						
			 						function() { 	
			 							
			 							$scope.datasetsListTemp.splice($scope.datasetsListTemp.length-1,1);		
			 							
			 							$scope.selectedDataSetInit = null; // Reset the selection (none dataset item will be selected) (danristo)
			 							$scope.selectedDataSet = null;
			 							$scope.showSaveAndCancelButtons = false;	
			 							
			 						}, 
			 						
			 						function(){} 
		 						);
		 				
		 			}
		 		}
	 			
		 	},
		 	
		 	// Clone the dataset.
		 	{
		 		label: $scope.translate.load("sbi.ds.clone.tooltip"),
			 	icon:'fa fa-files-o' , // alternative: 'fa fa-clone'
			 	backgroundColor:'transparent',
		   
			 	action: function(item,event) {
	
					if (item.id) {
						
						if ($scope.datasetsListTemp.length==$scope.datasetsListPersisted.length+1) {
							
							if ($scope.dirtyForm) {
					 				
					 				var confirm = $mdDialog.confirm()
									        .title($scope.translate.load("sbi.catalogues.generic.modify"))
									        .targetEvent(event)	 	          
									        .textContent($scope.translate.load("sbi.catalogues.generic.modify.msg"))
									        .ariaLabel("Losing the changed and not saved data")
									        .ok($scope.translate.load("sbi.general.yes"))
									        .cancel($scope.translate.load("sbi.general.No"));
									
									$mdDialog
										.show(confirm)
										.then(					
												function() {
													$scope.setFormNotDirty();
													$scope.datasetsListTemp = angular.copy($scope.datasetsListPersisted);
													cloningDataset(item);
//													console.log("prosao clone 3");
										 		},
										 		
										 		function() {
										 			
										 			console.log("keep changes");
										 			
					//					 			if ($scope.selectedDataSet.id) {
					//					 				$scope.selectedDataSetInit = $scope.datasetsListPersisted[$scope.selectedDSIndex];
					//					 			}
					//					 			else {
					//					 				$scope.selectedDataSetInit = $scope.datasetsListTemp[$scope.datasetsListTemp.length-1];
					//					 			}
										 			
										 		}
											);
					 				
					 			}
					 			else {
					 				$scope.setFormNotDirty();
					 				$scope.datasetsListTemp = angular.copy($scope.datasetsListPersisted);
					 				cloningDataset(item);	
					 			}
						 		
					 		}
							
						else {
							$scope.setFormNotDirty();
							$scope.datasetsListTemp = angular.copy($scope.datasetsListPersisted);
							cloningDataset(item);
						}
						
					}
					

//					if ($scope.datasetsListTemp.length == $scope.datasetsListPersisted.length + 1) {
////			 			sbiModule_messaging.showErrorMessage($scope.translate.load('sbi.ds.clone.warning.onlyonenewdataset.msg'));
//			 			
//			 			$mdDialog
//						.show(
//								$mdDialog.alert()
//							        .clickOutsideToClose(true)
//							        .title('Cannot clone a new dataset')
//							        .textContent("You cannot clone dataset that is not saved yet. Please, save before cloning it")
//							        .ariaLabel('Cannot clone a new dataset')
//							        .ok('Ok')
//						    );
//			 			
//			 		}
		 			
			 	} 
		 	}
 		 	
	 ];
	
	var cloningDataset = function(item) {
		
		var datasetClone = angular.copy(item);	
 		
		datasetClone.id = "";
 		datasetClone.label = "...";
 		datasetClone.dsVersions = [];
 		datasetClone.usedByNDocs = 0;
 		
 		// Convert string values for these two properties when clining, since the Angular will not let us map them otherwise
 		datasetClone.xslSheetNumber ? datasetClone.xslSheetNumber = Number(datasetClone.xslSheetNumber) : null;
 		datasetClone.skipRows ? datasetClone.skipRows = Number(datasetClone.skipRows) : null;
 		
 		$scope.datasetsListTemp.push(datasetClone);
 		$scope.selectedDataSet = angular.copy($scope.datasetsListTemp[$scope.datasetsListTemp.length-1]);
 		$scope.selectedDataSetInit = angular.copy($scope.datasetsListTemp[$scope.datasetsListTemp.length-1]);
 		
 		$scope.setFormNotDirty();
 		
 		$scope.showSaveAndCancelButtons = true;
 		
 		$timeout(
 					function() {
// 						console.log(page);
// 						console.log($scope.datasetTableLastPage);
// 						console.log(page<$scope.datasetTableLastPage);
 						var page = $scope.tableLastPage("datasetList_id");
 						// If the page that is returned is less than the current one, that means that we are already on that page, so keep it (danristo)
						$scope.datasetTableLastPage = page<$scope.datasetTableLastPage ? $scope.datasetTableLastPage : page; 
 					},
 					
 					300
 				);
	}
	
	/**
	 * Speed-menu option configuration for deleting of a dataset version.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.manageVersion = 
	[ 
	 	// Speed-menu option for deleting a dataset version.
     	{	           
     		label: $scope.translate.load("sbi.generic.delete"),
	 		icon:'fa fa-trash' ,
	 		backgroundColor:'transparent',
	           
	 		action: function(item,event) {
	 				 			
	 			var confirm = $mdDialog.confirm()
	 	          .title($scope.translate.load('sbi.ds.version.delete.title'))
	 	          .targetEvent(event)	 	          
	 	          .textContent($scope.translate.format($scope.translate.load('sbi.ds.version.delete.msg'), item.versNum))
	 	          .ariaLabel("Delete dataset version")
	 	          .ok($scope.translate.load("sbi.general.yes"))
	 	          .cancel($scope.translate.load("sbi.general.No"));
	 			
	 			$mdDialog
	 				.show(confirm)
	 				.then(
	 						
	 					function() {
	 						
	 						sbiModule_restServices.promiseDelete("1.0/datasets", $scope.selectedDataSet.id+"/version/"+item.versNum,"/")
		 		   				.then(
		 		   						function(response) {
		 		   						
		 					   				sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.ds.version.delete.success'), item.versNum));
		 					   				
		 					   				for (var j=0; j<$scope.datasetsListTemp.length; j++) {
		 					   					
		 					   					if ($scope.selectedDataSet.id == $scope.datasetsListTemp[j].id) {
		 					
		 					   						for (var i=0; i<$scope.selectedDataSet.dsVersions.length; i++) {
		 					   							
		 					   							if (item.versNum == $scope.selectedDataSet.dsVersions[i].versNum) {		 					   								
		 					   								// Remove the dataset's version from the collection of all datasets (the array in the left angular-table).
		 					   								$scope.datasetsListTemp[j].dsVersions.splice(i,1);
		 					   								// Remove the version from the currently selected dataset (the item in the left angular-table).
		 					   								$scope.selectedDataSet.dsVersions.splice(i,1);//			 					   								
		 					   								break;
		 					   								
		 					   							}
		 					   								
		 					   						}
		 					   						
		 					   					}
		 					   				}   				
		 	   
		 					   			}, 
		 					   			
		 					   			function(response) {	
		 					   				sbiModule_messaging.showErrorMessage($scope.translate.format($scope.translate.load('sbi.ds.version.delete.error'), item.versNum));
		 					   			}
		 			   			);
	 						
	 					},
	 					
	 					function() {
	 					}
	 				
	 				);
	 			
           }
     	
     	},
     	
     	// Speed-menu option for restoring a dataset version.
     	{
     		label: $scope.translate.load('sbi.ds.restore'),
     		icon:'fa fa-retweet' ,
	 		backgroundColor:'transparent',     		
     		
     		action: function(item,event) {
     			
     			var confirm = $mdDialog.confirm()
	 	          .title($scope.translate.load('sbi.ds.version.restore.title'))
	 	          .targetEvent(event)	 	          
	 	          .textContent($scope.translate.format($scope.translate.load('sbi.ds.version.restore.msg'), item.versNum))
	 	          .ariaLabel("Restore dataset version")
	 	          .ok($scope.translate.load("sbi.general.yes"))
	 	          .cancel($scope.translate.load("sbi.general.No"));
	 			
	 			$mdDialog
	 				.show(confirm)
	 				.then(	 						
		 					function() {
		 						
		 						sbiModule_restServices.promiseGet("1.0/datasets", $scope.selectedDataSet.id + "/restore", "versionId=" + item.versNum)
			 		   				.then(
			 		   						function(response) {
			 		   						
			 					   				sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.ds.version.restore.success'), item.versNum),500);
			 					   				
			 					   				for (var i=0; i<$scope.datasetsListTemp.length; i++) {
			 					   					
			 					   					if ($scope.selectedDataSet.id == $scope.datasetsListTemp[i].id) {			 			
			 					   						
			 					   						($scope.selectedDataSet.dsTypeCd.toLowerCase()=="file") ? $scope.refactorFileDatasetConfig(response.data[0]) : null;
			 					   						
			 					   						// Remove the dataset's version from the collection of all datasets (the array in the left angular-table).
	 					   								$scope.datasetsListTemp[i] = angular.copy(response.data[0]);
	 					   								// Remove the version from the currently selected dataset (the item in the left angular-table).
	 					   								$scope.selectedDataSet = angular.copy(response.data[0]);	
	 					   								// Needed in order to have a copy of the selected dataset that will not influence the selected dataset in the AT while performing changes on it
	 					   								$scope.selectedDataSetInit = angular.copy(response.data[0]);	
	 					   								// Call the scope function that is responsible for transformation of configuration data of the File dataset.
	 					   								
	 					   								
	 					   								break;
	 					   								
			 					   					}
			 					   					
			 					   				}   				
			 	   
			 					   			}, 
			 					   			
			 					   			function(response) {
			 					   				//sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			 					   				sbiModule_messaging.showErrorMessage($scope.translate.format($scope.translate.load('sbi.ds.version.restore.error'), item.versNum));
			 					   			}
			 			   			);
		 					},
		 					
		 					function() {
		 						
		 					}		 					
	 					);
     			
     		}
     	
     	}
     	
   ];
	
	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	$scope.deleteAllDatasetVersions = function() {
				
		if ($scope.selectedDataSet.dsVersions && Array.isArray($scope.selectedDataSet.dsVersions) && $scope.selectedDataSet.dsVersions.length>0) {
						
			var confirm = $mdDialog.confirm()
	          .title($scope.translate.load('sbi.ds.allversions.delete.title')) 	          
	          .textContent($scope.translate.load('sbi.ds.allversions.delete.msg'))
	          .ariaLabel("Delete all dataset versions")
	          .ok($scope.translate.load("sbi.general.yes"))
	          .cancel($scope.translate.load("sbi.general.No"));
			
			$mdDialog
				.show(confirm)
				.then(	 						
						function() {
			
							sbiModule_restServices.promiseDelete("1.0/datasets", $scope.selectedDataSet.id+"/allversions","/")
								.then(
										function(response) {
										
							   				sbiModule_messaging.showSuccessMessage($scope.translate.load('sbi.ds.allversions.delete.success'));
							   				
							   				for (var j=0; j<$scope.datasetsListTemp.length; j++) {
							   					
							   					if ($scope.selectedDataSet && $scope.selectedDataSet.id == $scope.datasetsListTemp[j].id) {	
							   						$scope.datasetsListTemp[j].dsVersions.splice(0,$scope.selectedDataSet.dsVersions.length);
													$scope.selectedDataSet.dsVersions.splice(0,$scope.selectedDataSet.dsVersions.length);	 					   								// Needed in order to have a copy of the selected dataset that will not influence the selected dataset in the AT while performing changes on it
 					   								// Needed in order to have a copy of the selected dataset that will not influence the selected dataset in the AT while performing changes on it
													$scope.selectedDataSetInit = angular.copy($scope.selectedDataSet);
													break;
							   					}
							   				}	
					
							   			}, 
							   			
							   			function(response) {							   				
							   				sbiModule_messaging.showErrorMessage($scope.translate.load('sbi.ds.allversions.delete.error'));
							   			}
								);
						},
						
						function() {
							
						}
					);
			
		}
		else {
			
			$mdDialog
				.show(
						$mdDialog.alert()
					        .clickOutsideToClose(true)
					        .title('Dataset has no versions to delete')
					        .textContent('There are not dataset versions to delete for the selected dataset')
					        .ariaLabel('Dataset versions do not exist')
					        .ok('Ok')
				    );
			
		}
		
	}
	
	// @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	$scope.selectDatasetVersion = function(item) {			
		$scope.selectedDatasetVersion = item;
	}
	
	/*
	 * 	service that loads all data sources
	 *   																	
	 */
	$scope.loadAllDataSources = function(){
		
		sbiModule_restServices.promiseGet("2.0","datasources")
			.then(
					function(response) {
						$scope.dataSourceList = response.data;
					}, 
					
					function(response) {
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					}
				);
		
	}
	
	$scope.loadAllDataSources();
	
	$scope.loadAllDatamarts = function() {
		 
		sbiModule_restServices.promiseGet("2.0","businessmodels")
			.then(
					function(response) {
						$scope.datamartList = angular.copy(response.data);
					}, 
					
					function(response) {
						sbiModule_messaging.showErrorMessage(response.data);
					}
				);	
		
	}
	
	$scope.loadAllDatamarts();
	
	/*
	 * 	@GET service that gets domain types for
	 *  scope 																	
	 */
	$scope.getDomainTypeScope = function(){	
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=DS_SCOPE")
		.then(function(response) {
			$scope.scopeList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}
	
	$scope.getDomainTypeScope();
	
	/*
	 * 	@GET service that gets domain types for
	 *  category 																	
	 */
	$scope.getDomainTypeCategory = function(){	
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=CATEGORY_TYPE")
		.then(function(response) {
			$scope.categoryList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message,'Error');
		});
	}
	
	$scope.getDomainTypeCategory();
	
	/*
	 * 	@GET service that gets domain types for
	 *  dataset types 																	
	 */
	$scope.getDomainTypeDataset = function(){	
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=DATA_SET_TYPE")
		.then(function(response) {
			$scope.datasetTypeList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}
	
	$scope.getDomainTypeDataset();
	
	/**
	 * Get transformation types of the dataset. (danristo)
	 */
	
	$scope.transformationDataset = null;
	
	$scope.getDomainTypeScope = function() {	
		
		sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=TRANSFORMER_TYPE")
			.then(function(response) {
				$scope.transformationDataset = response.data[0];
			}, function(response) {
				alert("ERROR");
//				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
		
	}
	
	$scope.getDomainTypeScope();
	
	 /**
	  * Adds variable map(name,value) object to scenario property array variable.
	  */
	 $scope.addParameters = function(){ 
		
		 var param={};
		
		if($scope.selectedDataSet.parameters==undefined){
			$scope.selectedDataSet.parameters = [];
		}
		
		$scope.selectedDataSet.parameters.push(param);
		console.log($scope.selectedDataSet.parameters);
		return param;
		
	 }
	 
	 /**
	  * Removes selected variable from array property of $scope.scenario object.
	  */
	 $scope.removeParameter=function(param){
			var index=$scope.selectedDataSet.parameters.indexOf(param);		
			$scope.selectedDataSet.parameters.splice(index, 1);
			console.log($scope.selectedDataSet.parameters);
	 }
	
	 // SELECT DATASET
	 $scope.loadDataSet = function(item,index) {
//		 console.log("A8");
//		 console.log(item);
//		 console.log($scope.dirtyForm);
//		 console.log($scope.selectedDataSet);
		 //$scope.selectedDataSet ? console.log("id: ",$scope.selectedDataSet.id) : console.log("UNDEFINED");
		 // DS not yet selected
		 if (!$scope.selectedDataSet) {
			 //console.log("a2");
			 $scope.setFormNotDirty();
			 selectDataset(item,index);
		 }
		 // Moving from selected new DS to existing DS
		 else if (!$scope.selectedDataSet.id) {
			 //console.log("b2");
			 //console.log(item.id);
			 //console.log($scope.dirtyForm);
//			 if ($scope.selectedDataSet.id!=item.id && $scope.dirtyForm) {
			 // If we move to the already existing DS and not clicking on the new DS again
			 if (item.id) {
				 
				 if ($scope.dirtyForm) {
					// TODO: translate
					var confirm = $mdDialog.confirm()
					        .title($scope.translate.load("sbi.catalogues.generic.modify"))
					        .targetEvent(event)	 	          
					        .textContent($scope.translate.load("sbi.catalogues.generic.modify.msg"))
					        .ariaLabel("Losing the changed and not saved data")
					        .ok($scope.translate.load("sbi.general.yes"))
					        .cancel($scope.translate.load("sbi.general.No"));
			
					$mdDialog
						.show(confirm)
						.then(					
								function() {
										
									$scope.datasetsListTemp.splice($scope.datasetsListTemp.length-1,1);											
									selectDataset(item,index);
									$scope.setFormNotDirty();
						 		},
						 		
						 		function() {		
						 			
						 			$scope.selectedDataSetInit = $scope.datasetsListTemp[$scope.datasetsListTemp.length-1];
						 			// Move to the AT page where the new DS has been created (since we decided not to move to the previously clicked dataset).
						 			$timeout(function() { $scope.datasetTableLastPage = $scope.tableLastPage("datasetList_id") }, 100);
						 		}
							);
				 }
				 else {
					
					 selectDataset(item,index);
					 $scope.setFormNotDirty();
				 }
				 
			 }			 
			
		 }
		 // Moving from an existing DS to another one
//		 else if ($scope.selectedDataSet && $scope.selectedDataSet.id!=item.id) {
		 else if ($scope.selectedDataSet.id!=item.id) {
			 //console.log("c4");
			if ($scope.dirtyForm) {
			
				// TODO: translate
				var confirm = $mdDialog.confirm()
				        .title($scope.translate.load("sbi.catalogues.generic.modify"))
				        .targetEvent(event)	 	          
				        .textContent($scope.translate.load("sbi.catalogues.generic.modify.msg"))
				        .ariaLabel("Losing the changed and not saved data")
				        .ok($scope.translate.load("sbi.general.yes"))
				        .cancel($scope.translate.load("sbi.general.No"));
		
				$mdDialog
					.show(confirm)
					.then(					
							function() {
								
								//console.log($scope.selectedDataSet);
								selectDataset(item,index);
								$scope.setFormNotDirty();
					 		},
					 		
					 		function() {

					 			var indexOfExistingDSInAT = -1;
//					 			
								for (i=0; i<$scope.datasetsListTemp.length; i++) {
									if ($scope.datasetsListTemp[i].id == $scope.selectedDataSet.id) {
										indexOfExistingDSInAT = i;
									}
								}
							
				 				$scope.selectedDataSetInit = $scope.datasetsListTemp[indexOfExistingDSInAT];					 			
					 			
					 		}
						);
				
			}
			else {
				//console.log("d1");
				selectDataset(item,index);	
				$scope.setFormNotDirty();
			}
		}
		
		
	};
	
	var selectDataset = function(item,index) {
		
		$log.info(item);
		
		$scope.selectedDataSetInit = angular.copy(item);
		$scope.selectedDataSet = angular.copy(item);
		$scope.showSaveAndCancelButtons = true;
		
		// DATASET PARAMETERS
		var parameterItemsTemp = [];			
		
		var parameterItems = $scope.selectedDataSet.pars;
		var parameterItemsLength = parameterItems.length;
		
		for (j=0; j<parameterItemsLength; j++) {
			
			var parameterItemTemp = {};
			
			parameterItemTemp["name"] = parameterItems[j]["name"];
			parameterItemTemp["type"] = parameterItems[j]["type"];
			parameterItemTemp["defaultValue"] = parameterItems[j]["defaultValue"];
			parameterItemTemp["index"] = $scope.parametersCounter++;
			
			parameterItemsTemp.push(parameterItemTemp);
			  
		}
		
		$scope.parameterItems = parameterItemsTemp;
		
		if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="rest") {
			
			// Cast the REST NGSI (transform from the String)
			$scope.selectedDataSet.restNGSI = JSON.parse($scope.selectedDataSet.restNGSI);
			
			// Cast the REST directly JSON attributes (transform from the String)
			$scope.selectedDataSet.restDirectlyJSONAttributes = JSON.parse($scope.selectedDataSet.restDirectlyJSONAttributes);
			
			// REST REQUEST HEADERS
			var restRequestHeadersTemp = [];
			//var counter = 0;
			
			for (var key in JSON.parse($scope.selectedDataSet.restRequestHeaders)) {
				
				var restRequestHeaderTemp = {};			
				
				  if (JSON.parse($scope.selectedDataSet.restRequestHeaders).hasOwnProperty(key)) {				  
					  restRequestHeaderTemp["name"] = key;
					  restRequestHeaderTemp["value"] = JSON.parse($scope.selectedDataSet.restRequestHeaders)[key];
					  restRequestHeaderTemp["index"] = $scope.counterRequestHeaders;			    	
				  }
				  
				  $scope.counterRequestHeaders++;
				  restRequestHeadersTemp.push(restRequestHeaderTemp);
				  
			}
			
			$scope.restRequestHeaders = restRequestHeadersTemp;
			
			// REST JSON PATH
			var restJsonPathAttributesTemp = [];	
			
			var restJsonPathAttributes = JSON.parse($scope.selectedDataSet.restJsonPathAttributes);
			var restJsonPathAttributesLength = restJsonPathAttributes.length;
			
			for (j=0; j<restJsonPathAttributesLength; j++) {
				
				var restJsonPathAttributeTemp = {};
				
				restJsonPathAttributeTemp["name"] = restJsonPathAttributes[j]["name"];
				restJsonPathAttributeTemp["jsonPathValue"] = restJsonPathAttributes[j]["jsonPathValue"];
				restJsonPathAttributeTemp["jsonPathType"] = restJsonPathAttributes[j]["jsonPathType"];
				restJsonPathAttributeTemp["index"] = $scope.counterJsonAttributes++;
				
				restJsonPathAttributesTemp.push(restJsonPathAttributeTemp);
				  
			}
			
			$scope.restJsonPathAttributes = restJsonPathAttributesTemp;
			
		}
		else if($scope.selectedDataSet.dsTypeCd.toLowerCase()=="custom") {
			
			// REST REQUEST HEADERS
			var customAttributesTemp = [];
		
			if ($scope.selectedDataSet.Custom) {
				
				for (var key in $scope.selectedDataSet.Custom) {
					
					var customAttributeTemp = {};			
					
					  if ($scope.selectedDataSet.Custom.hasOwnProperty(key)) {				  
						  customAttributeTemp["name"] = key;
						  customAttributeTemp["value"] = $scope.selectedDataSet.Custom[key];
						  customAttributeTemp["index"] = $scope.customAttributesCounter;			    	
					  }
					  
					  $scope.customAttributesCounter++;
					  customAttributesTemp.push(customAttributeTemp);
					  
				}
				
			}
			
			$scope.customAttributes = customAttributesTemp;
			
		}
		
				
		// Call the scope function that is responsible for transformation of configuration data of the File dataset.
		($scope.selectedDataSet.dsTypeCd.toLowerCase()=="file") ? $scope.refactorFileDatasetConfig(item) : null;
				
		if ($scope.selectedDataSet.trasfTypeCd) {
			$scope.transformDatasetState = $scope.selectedDataSet.trasfTypeCd==$scope.transformationDataset.VALUE_CD;
		}
		else {
			$scope.transformDatasetState = false;
		}
		
	}
	
	$scope.refactorFileDatasetConfig = function(item) {
						
		$scope.selectedDataSet.fileType = item!=undefined ? item.fileType : "";
		$scope.selectedDataSet.fileName = item!=undefined ? item.fileName : "";
		$scope.selectedDataSetInitialFileName = $scope.selectedDataSet.fileName;
		
		$scope.limitPreviewChecked = false;
		
		$scope.selectedDataSet.csvEncoding = item!=undefined ? item.csvEncoding : $scope.csvEncodingDefault; 
		$scope.selectedDataSet.csvDelimiter = item!=undefined ? item.csvDelimiter : $scope.csvDelimiterDefault; 
		$scope.selectedDataSet.csvQuote = item!=undefined ? item.csvQuote : $scope.csvQuoteDefault; 
		
		/**
		 * Handle the limitRows property value deserialization (special case: it can be of a value NULL).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (item!=undefined) {
		
			if (item.limitRows!=null && item.limitRows!="") {
				$scope.selectedDataSet.limitRows = Number(item.limitRows);
			}
			else {
				$scope.selectedDataSet.limitRows = item.limitRows;
			}
		}
		else {			
			$scope.selectedDataSet.limitRows = $scope.limitRowsDefault;			
		}
				
		$scope.selectedDataSet.catTypeVn = item!=undefined ? item.catTypeVn : "";
		
		if (item!=undefined) {
			$scope.selectedDataSet.catTypeId =  Number(item.catTypeId);	
			$scope.selectedDataSet.xslSheetNumber = Number($scope.xslSheetNumberDefault);
			$scope.selectedDataSet.skipRows = Number(item.skipRows);
			$scope.selectedDataSet.limitRows  = Number($scope.limitRowsDefault);	
		}
		else {
			$scope.selectedDataSet.catTypeId = null;
			$scope.selectedDataSet.xslSheetNumber = null;
			$scope.selectedDataSet.skipRows = null;
			$scope.selectedDataSet.limitRows  = null;
		}
		
		
		$scope.selectedDataSet.id = item!=undefined ? item.id : "";
		$scope.selectedDataSet.label = item!=undefined ? item.label : "";
		$scope.selectedDataSet.name = item!=undefined ? item.name : "";
		$scope.selectedDataSet.description = item!=undefined ? item.description : ""; 
		$scope.selectedDataSet.meta = item!=undefined ? item.meta : [];
		
		$scope.selectedDataSet.fileUploaded = false;
		
		$scope.setFormNotDirty();
				
	}
	
	/** 
	 * The function that will check for the current last AT page, so it can set this one to active one when new items are 
	 * added to the AT witht the "tableId" ID.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.tableLastPage = function(tableId) {
				
		var datasetTable = document.getElementById(tableId);		
		var listOfPages = datasetTable.getElementsByTagName("dir-pagination-controls")[0].innerText;
		listOfPages = listOfPages.replace(/\s/g, "");
//		console.log(listOfPages);
//		console.log(listOfPages.charAt(listOfPages.indexOf(">")-1));
		// If the AT is empty, set the current page on the table's first one
		if (listOfPages!="") {
			return parseInt(listOfPages.charAt(listOfPages.indexOf(">")-1));
		}
		else {
			return 1;
		}

	}
	
	$scope.createNewDataSet = function() {
				
//		if ($scope.datasetsListTemp.length < $scope.datasetsListPersisted.length + 1) {
		
		// There is a new (not saved) DS that is already dirty
//		var blankDSDirty = $scope.dirtyForm && ;
//		var blankDSNotDirty = !$scope.dirtyForm && $scope.datasetsListTemp.length == $scope.datasetsListPersisted.length + 1;

		if ($scope.datasetsListTemp.length == $scope.datasetsListPersisted.length + 1) {
		
			if ($scope.dirtyForm) {
				
				var confirm = $mdDialog.confirm()
				        .title($scope.translate.load("sbi.catalogues.generic.modify"))
				        .targetEvent(event)	 	          
				        .textContent($scope.translate.load("sbi.catalogues.generic.modify.msg"))
				        .ariaLabel("Losing the changed and not saved data")
				        .ok($scope.translate.load("sbi.general.yes"))
				        .cancel($scope.translate.load("sbi.general.No"));
				
				$mdDialog
					.show(confirm)
					.then(					
							function() {
								$scope.setFormNotDirty();
								//console.log("menjano 3");
								$scope.datasetsListTemp = angular.copy($scope.datasetsListPersisted);
								makeNewDataset();
					 		},
					 		
					 		function() {
					 			console.log("keep changes");					 			
					 		}
						);
				
			}
			else {		
				$scope.setFormNotDirty();
				$scope.datasetsListTemp = angular.copy($scope.datasetsListPersisted);
				makeNewDataset();		
			}
			
		}
		// There is already a new or cloned DS that is not changed, so just replace it
		else {
			$scope.setFormNotDirty();
			makeNewDataset();	
		}
			
//		}
//		else {
////			sbiModule_messaging.showErrorMessage($scope.translate.load("sbi.ds.add.warning.onlyonenewdataset.msg"));
//			
//			$mdDialog
//			.show(
//					$mdDialog.alert()
//				        .clickOutsideToClose(true)
//				        .title('Cannot add new dataset')
//				        .textContent($scope.translate.load('sbi.ds.add.warning.onlyonenewdataset.msg'))
//				        .ariaLabel('Cannot add new dataset')
//				        .ok('Ok')
//			    );
//			
//		}
		
	};
	
	var makeNewDataset = function() {
					
			var object = {
					actions: "",
					catTypeId:"",
					catTypeVn:"",
					dataSource:"",
					dateIn:"",
					description:"",
					dsTypeCd:"",
					dsVersions:"",
					isPersisted:"",
					isPersistedHDFS:"",
					label:"",
					meta:[],
					name:"",
					owner:"",
					pars:"",
					persistTableName:"",
					pivotIsNumRows:"",
					query:"",
					queryScript:"",
					queryScriptLanguage:"",
					scopeCd:"",
					scopeId:"",
					usedByNDocs:"",
					userIn:"",
					versNum:""
			}
			
			$scope.datasetsListTemp.push(object);
			$scope.selectedDataSet = angular.copy($scope.datasetsListTemp[$scope.datasetsListTemp.length-1]);
			$scope.selectedDataSetInit = angular.copy($scope.datasetsListTemp[$scope.datasetsListTemp.length-1]); // Reset the selection (none dataset item will be selected) (danristo)
			$scope.showSaveAndCancelButtons = true;
			
			// Give a little time for the AT to render after the insertion of a new table element (new dataset) (danristo)
			// We do not need to check if the current page is the one that is return by a function, since we cannot add more than one empty dataset
			$timeout(function() { var page = $scope.tableLastPage("datasetList_id"); $scope.datasetTableLastPage = (page<=$scope.datasetTableLastPage)
			? $scope.datasetTableLastPage : page;  }, 100);
			
				
	}
	
	$scope.saveDataset = function() {
		
//		console.log($scope.selectedDataSet);
		
		if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="rest") {			
			
			//----------------------
			// REQUEST HEADERS
			//----------------------
			var restRequestHeadersTemp = {};
			
			for (i=0; i<$scope.restRequestHeaders.length; i++) {
				restRequestHeadersTemp[$scope.restRequestHeaders[i]["name"]] = $scope.restRequestHeaders[i]["value"];			
			}
			
			$scope.selectedDataSet.restJsonPathAttributes = angular.copy(JSON.stringify(restRequestHeadersTemp));	
			
			//----------------------
			// JSON PATH ATTRIBUTES
			//----------------------
			var restJsonPathAttributesTemp = {};						
			$scope.selectedDataSet.restJsonPathAttributes = angular.copy(JSON.stringify($scope.restJsonPathAttributes));
			
		}
		else if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="custom") {
			
			var customAttributesTemp = {};
			
			for (i=0; i<$scope.customAttributes.length; i++) {
				customAttributesTemp[$scope.customAttributes[i]["name"]] = $scope.customAttributes[i]["value"];			
			}
			
			$scope.selectedDataSet.customData = angular.copy(JSON.stringify(customAttributesTemp));
		}
		else if($scope.selectedDataSet.dsTypeCd.toLowerCase()=="file") {
			$scope.selectedDataSet.fileUploaded = !$scope.selectedDataSet.fileUploaded ? false : true;
		}
		
		$scope.selectedDataSet.recalculateMetadata = true;
		$scope.manageDatasetFieldMetadata($scope.selectedDataSet.meta);
		
		// Collect parameters 
		// TODO: maybe to remove the "index" property, if it makes trouble (it is excessive anyway)
		$scope.selectedDataSet.pars = $scope.parameterItems;
		
		// ADVANCED tab
		// For transforming
		console.log($scope.transformDatasetState);
		console.log($scope.transformationDataset.VALUE_CD);
		console.log($scope.transformationDataset);
		$scope.transformDatasetState==true ? $scope.selectedDataSet.trasfTypeCd=$scope.transformationDataset.VALUE_CD : null;
		console.log($scope.selectedDataSet);
		if ($scope.selectedDataSet) {
			
			$log.info("save");
			
			var indexOfExistingDSInAT = -1;
			
			//Existing DS (PUT)
			if ($scope.selectedDataSet.id) {
				for (i=0; i<$scope.datasetsListTemp.length; i++) {
					if ($scope.datasetsListTemp[i].id == $scope.selectedDataSet.id) {
//						console.log("nasao 3: ",$scope.datasetsListTemp[i]);
						indexOfExistingDSInAT = i;
					}
				}
			}
			// New DS (POST)
			else {
//				console.log("novi 3: ",$scope.datasetsListTemp[i]);
				indexOfExistingDSInAT = $scope.datasetsListTemp.length-1;
			}
			
			sbiModule_restServices.promisePost('1.0/datasets','', angular.toJson($scope.selectedDataSet))
				.then(
						function(response) {
										
							sbiModule_restServices.promiseGet('1.0/datasets/dataset/id',response.data.id)
								.then(
										function(responseDS) {
											$log.info("SUCCESS"); 
											var savedDataset = responseDS.data[0];									
											$scope.selectedDataSet = angular.copy(savedDataset);
											$scope.datasetsListTemp[indexOfExistingDSInAT] = angular.copy($scope.selectedDataSet);
											$scope.datasetsListPersisted = angular.copy($scope.datasetsListTemp);
											$scope.selectedDataSetInit = angular.copy($scope.selectedDataSet);
										},
										
										function(responseDS) {
											$log.warn("ERROR");
										}
									);
												
							console.log("[POST]: SUCCESS!");					
							sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');					
							
							// RELOAD ALL THE DATASETS AFTER SAVE OPERATION
		//					$scope.loadAllDatasets();
							//$scope.selectedDataSet = null;
							
							$scope.setFormNotDirty();
							
						}, 
						
						function(response) {
							sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
						}
					);
			
		}	
		else {
			// TODO: translate
			sbiModule_messaging.showErrorMessage("Before saving, the dataset must be selected");
		}
		
	};
	
	$scope.closeDatasetDetails = function() {
//		$log.info("cancel");
		$scope.selectedDataSetInit = null; // Reset the selection (none dataset item will be selected) (danristo)
		$scope.selectedDataSet = null;
		$scope.showSaveAndCancelButtons = false;
	};
	
	$scope.uploadFile= function(){
		
    	multipartForm.post(sbiModule_config.contextName +"/restful-services/selfservicedataset/fileupload",$scope.fileObj).success(

				function(data,status,headers,config){
					
					if(data.hasOwnProperty("errors")){						
						console.info("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
						
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						sbiModule_messaging.showErrorMessage($scope.translate.load(data.errors[0].message));
						
					}
					else {
						
						console.info("[UPLOAD]: SUCCESS!");
												
						// Take the toaster duration set inside the main controller of the Workspace. (danristo)
						sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.workspace.dataset.wizard.upload.success'), 
								$scope.fileObj.fileName));
					
						$scope.file={};
						$scope.selectedDataSet.fileType = data.fileType;
						$scope.selectedDataSet.fileName = data.fileName;
												
						/**
						 * When user re-uploads a file, we should reset all fields that we have on the bottom panel of the Step 1, for both file types 
						 * (CSV and XLS), so the user can start from the scratch when defining new/modifying existing file dataset. 
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.selectedDataSet.csvEncoding = $scope.csvEncodingDefault;
						$scope.selectedDataSet.csvDelimiter = $scope.csvDelimiterDefault;
						$scope.selectedDataSet.csvQuote = $scope.csvQuoteDefault;
						$scope.selectedDataSet.skipRows = $scope.skipRowsDefault ? $scope.skipRowsDefault : null;
						$scope.selectedDataSet.limitRows = $scope.limitRowsDefault ? $scope.limitRowsDefault : null;
						$scope.selectedDataSet.xslSheetNumber = $scope.xslSheetNumberDefault ? $scope.limitRowsDefault : null;
						
						/**
						 * Whenever we upload a file, keep the track of its name, in order to indicate when the new one is browsed but not uploaded.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.prevUploadedFile = $scope.selectedDataSet.fileName;
						$scope.selectedDataSet.fileUploaded=true;
						$scope.changingFile = false;
						
					}
				}).error(function(data, status, headers, config) {
					console.info("[UPLOAD]: FAIL! Status: "+status);					

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					sbiModule_messaging.showErrorMessage($scope.fileObj.fileName+" could not be uploaded."+data.errors[0].message);
					
				});
    	
    }
	
	$scope.changeUploadedFile=function(){
		
		console.info("CHANGE FILE [IN]");
		
		$scope.changingFile = true;
		
		/**
		 * If we are about to change the uploaded file in editing mode, we should keep the data about the name of the previously uploaded
		 * file, in order to keep it in the line for the browsed file.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
		 */
//		if ($scope.editingDatasetFile==true) {
			
			$scope.fileObj = 
			{
				file: { name:$scope.selectedDataSet.fileName }, 
				fileName: $scope.selectedDataSet.fileName
			};
			
//		}
		
		console.info("CHANGE FILE [OUT]");
		
	}
	
	$scope.codemirrorLoaded = function(_editor){
		 $scope.codeMirror = _editor;	  
	 }
	
	  // The ui-codemirror option
	  $scope.cmOption = {
	    lineNumbers: true,
	    tabMode: "indent",
	    onLoad : function(_cm){
	      
	      // HACK to have the codemirror instance in the scope...
	      $scope.modeChanged = function(type){
	    	  console.log(type)
	    	  if(type=='ECMAScript'||type=="MongoDB"){
	    		  _cm.setOption("mode", 'text/javascript');
	  		} else {
	  			_cm.setOption("mode", 'text/x-groovy');
	  		}
	        console.log(_cm)
	      };
	    }
	  };
	  	 
	 $scope.queryScripts = [
	                        {
	                         name: 'javascript',
		 					 mode: 'text/javascript'
	                        },
	                        {
	                         name: 'SQL',
	                         mode: 'text/x-sql'
	                        }
	                       ];
	
	 $scope.codemirrorOptions = {
			   mode : "text/x-sql",
			   indentWithTabs: true,
			   smartIndent: true,
			   lineWrapping : true,
			   matchBrackets : true,
			   autofocus: true,
			   theme:"eclipse",
			   lineNumbers: true,
			 };
	 
	 
	 $scope.getScriptTypes = function() {
			sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=SCRIPT_TYPE")
			.then(function(response) {
				$scope.listOfScriptTypes = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.toastr.title.error"));
				
			});
		}
	 
	 $scope.getScriptTypes();
	 
	 $scope.openEditScriptDialog = function () {
		   $mdDialog
		   .show({
		    scope : $scope,
		    preserveScope : true,
		    parent : angular.element(document.body),
		    controllerAs : 'openEditScriptDialogCtrl',
		    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/EditDataSetScript.html',
		    clickOutsideToClose : false,
		    hasBackdrop : false
		   });
	 }
	 
	 $scope.saveScript = function () {
		 $mdDialog.hide();
		 console.log("save")
	 }
	
	 $scope.closeScript = function () {
		 $mdDialog.hide();
	 }
	 
	$scope.openQbe = function() {
		
		if ($scope.selectedDataSet.dsTypeCd.toUpperCase() == "QBE") {
			
			if ($scope.selectedDataSet.qbeDataSource && $scope.selectedDataSet.qbeDatamarts) {
				$log.info("OPEN QBE FOR QBE DATASET");
			}
			else {
				// TODO: translate
				if (!$scope.selectedDataSet.qbeDataSource) {
//					sbiModule_messaging.showErrorMessage("The datasource must be selected before opening the QBE");
					
					$mdDialog
						.show(
								$mdDialog.alert()
							        .clickOutsideToClose(true)
							        .title('Cannot open QBE')
							        .textContent("The datasource must be selected before opening the QBE")
							        .ariaLabel('Cannot open QBE')
							        .ok('Ok')
						    );
					
				}
				else if (!$scope.selectedDataSet.qbeDatamarts) {
//					sbiModule_messaging.showErrorMessage("The datamart must be selected before opening the QBE");
					
					$mdDialog
						.show(
								$mdDialog.alert()
							        .clickOutsideToClose(true)
							        .title('Cannot open QBE')
							        .textContent("The datamart must be selected before opening the QBE")
							        .ariaLabel('Cannot open QBE')
							        .ok('Ok')
						    );
					
				}
			}
			
		}
		else {
			$log.info("OPEN QBE FOR FEDERATED DATASET");
		}		
		
	}
	
	$scope.viewQbe = function() {
//		$log.info("VIEW QBE QUERY");
				
		// If the Federated dataset is saved before (already existing).
		if ($scope.selectedDataSet.qbeJSONQuery) {
			
			$scope.selectedDataSet.qbeJSONQuery = JSON.stringify(JSON.parse($scope.selectedDataSet.qbeJSONQuery),null,2);
			
			$mdDialog
			   .show({
				    scope : $scope,
				    preserveScope : true,
				    parent : angular.element(document.body),
				    controllerAs : 'datasetController',
				    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/qbeQueryView.html',
				    clickOutsideToClose : false,
				    hasBackdrop : true
			   });
			
		}
		else {
			// TODO: translate
//			sbiModule_messaging.showErrorMessage("The dataset should be saved before viewing the QBE query");
			
			$mdDialog
				.show(
						$mdDialog.alert()
					        .clickOutsideToClose(true)
					        .title('Cannot open QBE query')
					        .textContent("The dataset should be saved before viewing the QBE query")
					        .ariaLabel('Cannot open QBE query')
					        .ok('Ok')
				    );
			
		}
		
	}
	
	 // The ui-codemirror option
	$scope.cmOptionQbe = {
	    lineNumbers: true,
	    mode: "application/json",
//	    lineWrapping: true,
//	    scrollbarStyle: null,
//	    viewportMargin: 50,
	    theme: "eclipse",
	    showCursorWhenSelecting: false,
	    readOnly: true
	};
	
	$scope.templateContent = "";
	
	$scope.showInfoForRestParams = function(item) {
				
		switch(item) {
			case "ngsi":
				takeTheInfoHtmlContent(sbiModule_config.contextName + "/themes/sbi_default/html/restdataset-ngsi.html");
				break;
			case "jsonPathItems":
				takeTheInfoHtmlContent(sbiModule_config.contextName + "/themes/sbi_default/html/restdataset-jsonpath-items.html");
				break;
			case "directJsonAttributes":
				takeTheInfoHtmlContent(sbiModule_config.contextName + "/themes/sbi_default/html/restdataset-attributes-directly.html");
				break;
			case "jsonPathAttributes":
				takeTheInfoHtmlContent(sbiModule_config.contextName + "/themes/sbi_default/html/restdataset-json-path-attributes.html");
				break;		
		}		
		
	}
	
	var takeTheInfoHtmlContent = function(url) {	
		
		$http.get(url)
			.then
			(
					function(response) {
						
						$scope.templateContent = response.data;
						
						$mdDialog
						   .show({
							    scope : $scope,
							    preserveScope : true,
							    parent : angular.element(document.body),
							    controllerAs : 'datasetController',
							    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/datasetRestParamsInfo.html',
							    clickOutsideToClose : false,
							    hasBackdrop : true
						   });
						
					},
					
					function() {
						alert("ERROR");
						return null;
					}
			);
	}
	
	/**
	 * ========================================
	 * THE PREVIEW OF THE DATASET LOGIC (START)
	 * ========================================
	 */
	
	function DatasetPreviewController($scope,$mdDialog,$http) {
		
//		$scope.paginationDisabled = false;
		
		$scope.closeDatasetPreviewDialog=function(){
			 $scope.previewDatasetModel=[];
			 $scope.previewDatasetColumns=[];
			 $scope.startPreviewIndex=0;
			 $scope.endPreviewIndex=0;
			 $scope.totalItemsInPreview=-1;	// modified by: danristo
			 $scope.datasetInPreview=undefined;
			 $scope.counter = 0;
			 $mdDialog.cancel();	 
	    }	
	}
	
	$scope.previewDataset = function() {
		
		$scope.previewDatasetColumns=[];
    		    	
		// If the dataset is selected, show the preview.
		if ($scope.selectedDataSet && $scope.selectedDataSet.meta) {
	    	
	    	var dataset = $scope.selectedDataSet;
//	    	$log.info("DATASET FOR PREVIEW: ",dataset);
	    	
	    	$scope.datasetInPreview=dataset;    	
	    	$scope.disableBack=true;
	    	
	    	/**
	    	 * Variable that serves as indicator if the dataset metadata exists and if it contains the 'resultNumber' 
	    	 * property (e.g. Query datasets).
	    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	    	 */ 
	    	var dsRespHasResultNumb = dataset.meta.dataset.length>0 && dataset.meta.dataset[0].pname=="resultNumber";
	    		    	
	    	/**
	    	 * The paginated dataset preview should contain the 'resultNumber' inside the 'dataset' property. If not, disable the
	    	 * pagination in the toolbar of the preview dataset dialog.
	    	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	    	 */
	    	if(dataset.meta.dataset.length>0 && dataset.meta.dataset[0].pname=="resultNumber"){
	    		$scope.totalItemsInPreview=dataset.meta.dataset[0].pvalue;
	    		$scope.previewPaginationEnabled=true;
	    	}
	    	else{
	    		$scope.previewPaginationEnabled=false;
	    	}
	    	
	    	console.log($scope.datasetInPreview);
	    	console.log($scope.totalItemsInPreview);
	    	console.log($scope.previewPaginationEnabled);
	    	
	    	$scope.getPreviewSet($scope.datasetInPreview);
	    	
	    	/**
	    	 * Execute this if-else block only if there is already an information about the total amount of rows in the dataset metadata.
	    	 * In other words, it should be executed for the e.g. Query dataset, since it has this property in its meta.
	    	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	    	 */
	    	if (dsRespHasResultNumb) {
	    		
	    		if($scope.totalItemsInPreview < $scope.itemsPerPage) {
	    			$scope.endPreviewIndex = $scope.totalItemsInPreview;
	    			$scope.disableNext = true;
	    		}
	    		else {
	    		 	$scope.endPreviewIndex = $scope.itemsPerPage;
	    		 	$scope.disableNext = false;
	    		}
	    		
	    	}
	    	
	     	$mdDialog.show({
				  scope:$scope,
				  preserveScope: true,
			      controller: DatasetPreviewController,
			      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/workspace/templates/datasetPreviewDialogTemplate.html',  
			      clickOutsideToClose:false,
			      escapeToClose :false,
			      //fullscreen: true,
			      locals:{
			    	 // previewDatasetModel:$scope.previewDatasetModel,
			         // previewDatasetColumns:$scope.previewDatasetColumns 
			      }
			    });
	    	
    	}	
//		else if(!$scope.selectedDataSet.meta) {
//			sbiModule_messaging.showErrorMessage("The dataset is not saved. Please, save the dataset before a preview");
//		}
	       	
    }
	 
    $scope.createColumnsForPreview=function(fields){
	    
    	for(i=1;i<fields.length;i++){
    	 var column={};
    	 column.label=fields[i].header;
    	 column.name=fields[i].name;
    	 
    	 $scope.previewDatasetColumns.push(column);
    	}
    	
    }
       
    $scope.getPreviewSet = function(dataset){    
    	
    	var datasetType = dataset.dsTypeCd.toUpperCase();
    	console.log(datasetType);
    	/**
    	 * If the type of the dataset is File, set these flags so the pagination toolbar on the Preview dataset panel
    	 * is hidden and the pagination is performed on the client-side. Other dataset types should have the server-side
    	 * pagination (else-branch).
    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    	 */
//    	if (datasetType!="FILE") {
//    		 $scope.paginationDisabled = true;
//    		 $scope.previewPaginationEnabled = true;
//    	}
//    	else {
//    		$scope.paginationDisabled = false;
//    		$scope.previewPaginationEnabled = false;
//    	}
    	
    	$scope.paginationDisabled = true;
		$scope.previewPaginationEnabled = true;
    	
    	params={};
    	params.start = $scope.startPreviewIndex;
    	params.limit = $scope.itemsPerPage;
    	params.page = 0;
    	params.dataSetParameters=null;
    	params.sort=null;
    	params.valueFilter=null;
    	params.columnsFilter=null;
    	params.columnsFilterDescription=null;
    	params.typeValueFilter=null;
    	params.typeFilter=null;
    	    	
    	config={};
    	config.params=params;
    	
    	console.log(params);
    	
    	sbiModule_restServices.promiseGet("selfservicedataset/values",dataset.label,"",config)
			.then(function(response) {			
						
				var totalItemsInPreviewInit = angular.copy($scope.totalItemsInPreview);
				console.log(response);
				/**
				 * If the responded dataset does not possess a metadata information (total amount of rows in the result)
				 * take this property if provided.
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				if (response.data.results) {
					$scope.totalItemsInPreview = response.data.results;
				}	
				
		    	/**
		    	 * If the the initial 'totalItemsInPreview' value is -1, that means that this property is not set yet or there is no this property in the response 
		    	 * (total number of results - rows). This serves just to initialize the indicators used in the if-else block (such as 'endPreviewIndex'), in order 
		    	 * to initialize the preview of the dataset types that do not have 'resultNumber' property in their 'meta'. This temporary variable should be -1
		    	 * only on the first call of this function.
		    	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		    	 */
				if(totalItemsInPreviewInit==-1) {
										
					if ($scope.totalItemsInPreview < $scope.itemsPerPage) {
		   		 		$scope.endPreviewIndex = $scope.totalItemsInPreview	
		   		 		$scope.disableNext = true;
					}
					else {
			   		 	$scope.endPreviewIndex = $scope.itemsPerPage;
			   		 	$scope.disableNext = false;
			       	}
					
		       	}
				
			    angular.copy(response.data.rows,$scope.previewDatasetModel);
			   
			    if( !$scope.previewDatasetColumns || $scope.previewDatasetColumns.length==0){
			    	$scope.createColumnsForPreview(response.data.metaData.fields);				
			    }		
			
			//$scope.startPreviewIndex=$scope.startPreviewIndex=0+20;
			
		},
		
		function(response){	console.log("ERROR");		
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message));			
		});
    	
    }
    
    $scope.getNextPreviewSet = function(){   	
    	console.log($scope.startPreviewIndex);
    	console.log($scope.itemsPerPage);
    	console.log($scope.totalItemsInPreview);
    	console.log($scope.previewDatasetModel);
    	 if($scope.startPreviewIndex+$scope.itemsPerPage > $scope.totalItemsInPreview){
  
    		 $scope.startPreviewIndex=$scope.totalItemsInPreview-($scope.totalItemsInPreview%$scope.itemsPerPage);  		
    		 $scope.endPreviewIndex=$scope.totalItemsInPreview;
    		 $scope.disableNext=true;
    		 $scope.disableBack=false;
    	 }else if($scope.startPreviewIndex+$scope.itemsPerPage == $scope.totalItemsInPreview){
    		 $scope.startPreviewIndex=$scope.totalItemsInPreview-$scope.itemsPerPage;
    		 $scope.endPreviewIndex=$scope.totalItemsInPreview;
    		 $scope.disableNext=true;
    		 $scope.disableBack=false;
    	 } else{
              $scope.startPreviewIndex= $scope.startPreviewIndex+$scope.itemsPerPage;
              $scope.endPreviewIndex=$scope.endPreviewIndex+$scope.itemsPerPage;
              
              if($scope.endPreviewIndex >= $scope.totalItemsInPreview){
            	  if($scope.endPreviewIndex == $scope.totalItemsInPreview){
            		  $scope.startPreviewIndex=$scope.totalItemsInPreview-$scope.itemsPerPage;
            	  }else{
            	  $scope.startPreviewIndex=$scope.totalItemsInPreview-($scope.totalItemsInPreview%$scope.itemsPerPage);
            	  }
         		 $scope.endPreviewIndex=$scope.totalItemsInPreview;
         		 $scope.disableNext=true;
         		 $scope.disableBack=false;
         	 }else{
              
              
              $scope.disableNext=false;
              $scope.disableBack=false;
         	 }
    	 }   
    	 
    	 $scope.getPreviewSet($scope.datasetInPreview);
        	 
    }
    
    $scope.getBackPreviewSet=function(){
    	
    	 if($scope.startPreviewIndex-$scope.itemsPerPage < 0){
    		 $scope.startPreviewIndex=0; 
    		 $scope.endPreviewIndex=$scope.itemsPerPage;
    		 $scope.disableBack=true;
    		 $scope.disableNext=false;
    	 }
    	 else{
    		 $scope.endPreviewIndex=$scope.startPreviewIndex;
             $scope.startPreviewIndex= $scope.startPreviewIndex-$scope.itemsPerPage;
             if($scope.startPreviewIndex-$scope.itemsPerPage < 0){
            	 $scope.startPreviewIndex=0; 
        		 $scope.endPreviewIndex=$scope.itemsPerPage;
        		 $scope.disableBack=true;
        		 $scope.disableNext=false;
             }else{
             $scope.disableBack=false;
             $scope.disableNext=false;
             }
    	 }
    	
    	 $scope.getPreviewSet($scope.datasetInPreview);
    	
    }
    
    /**
	 * ========================================
	 * THE PREVIEW OF THE DATASET LOGIC (END)
	 * ========================================
	 */
    
    $scope.changeSelectedTab = function(selectedTab) {
    	$log.info("Selected tab:",selectedTab);
    	$scope.selectedTab = selectedTab;
    }
    
    $scope.deleteAllRESTRequestHeaders = function() {
       	
    	// TODO: translate
    	var confirm = $mdDialog.confirm()
	         .title("Clear all REST dataset request headers")
	         .targetEvent(event)	 	          
	         .textContent("Are you sure you want to delete all request headers")
	         .ariaLabel("Clear all REST dataset request headers")
	         .ok($scope.translate.load("sbi.general.yes"))
	         .cancel($scope.translate.load("sbi.general.No"));
		
		$mdDialog
			.show(confirm)
			.then(					
					function() {
						$scope.setFormDirty();
						$scope.restRequestHeaders = [];	
						$scope.restDsRequestHeaderTableLastPage = 1;
			 		}
				);	
    	    	
    }
    
    $scope.deleteAllRESTJsonPathAttributes = function() {
    	
    	// TODO: translate
    	var confirm = $mdDialog.confirm()
	         .title("Clear all REST JSON path attributes")
	         .targetEvent(event)	 	          
	         .textContent("Are you sure you want to delete all JSON path attributes")
	         .ariaLabel("Clear all REST JSON path attributes")
	         .ok($scope.translate.load("sbi.general.yes"))
	         .cancel($scope.translate.load("sbi.general.No"));
		
		$mdDialog
			.show(confirm)
			.then(					
					function() {
						$scope.setFormDirty();
						$scope.restJsonPathAttributes = [];		 	        	
			 		}
				);	
    	
    }

    $scope.manageDatasetFieldMetadata =  function(fieldsColumns){
  		if(fieldsColumns){
  			//Temporary workaround because fieldsColumns is now an object with a new structure after changing DataSetJSONSerializer
  			if ((fieldsColumns.columns != undefined) && (fieldsColumns.columns != null)){
  				var columnsArray = new Array();
  				
  				
  				
  				var columnsNames = new Array();
  				//create columns list
  				for (var i = 0; i < fieldsColumns.columns.length; i++) {
  					var element = fieldsColumns.columns[i];
  					columnsNames.push(element.column); 
  				}
  				
  				columnsNames = $scope.removeDuplicates(columnsNames);
  				
  				
  				for (var i = 0; i < columnsNames.length; i++) {
  					var columnObject = {displayedName:'', name:'',fieldType:'',type:''};
  					var currentColumnName = columnsNames[i];
  					//this will remove the part before the double dot if the column is in the format ex: it.eng.spagobi.Customer:customerId
  					if (currentColumnName.indexOf(":") != -1){
  					    var arr = currentColumnName.split(':');
  					     
  	  					columnObject.displayedName = arr[1];
  					} else {
  	  					columnObject.displayedName = currentColumnName;
  					}

  					columnObject.name = currentColumnName;
  					for (var j = 0; j < fieldsColumns.columns.length; j++) {
  	  					var element = fieldsColumns.columns[j];
  	  					if (element.column == currentColumnName){
  	  						if(element.pname.toUpperCase() == 'type'.toUpperCase()){
  	  							columnObject.type = element.pvalue;
  	  						}
  	  						else if(element.pname.toUpperCase() == 'fieldType'.toUpperCase()){
  	  							columnObject.fieldType = element.pvalue;
  	  						}
  	  					}
  					}
  					columnsArray.push(columnObject);
	  			}			
  				
  				$scope.selectedDataSet.meta = columnsArray;
  				// end workaround ---------------------------------------------------
  			} else {
  	  			//this.fieldStore.loadData(fieldsColumns);
  			}			
  			//this.emptyStore = false;
  		}else{
  			//this.emptyStore = true;
  		}
	}
    
    $scope.removeDuplicates = function(array) {
	    var index = {};
	   
	    for (var i = array.length - 1; i >= 0; i--) {
	        if (array[i] in index) {
	            // remove this item
	            array.splice(i, 1);
	        } else {
	            // add this value to index
	            index[array[i]] = true;
	        }
	    }
	    return array;
	}

    $scope.saveWithoutMetadata = function () {
    	$scope.selectedDataSet.isFromSaveNoMetadata = true;
    	$scope.saveDataset();
    }
    
//    $scope.openHelp = function () {
//    	$mdDialog
//		   .show({
//		    scope : $scope,
//		    preserveScope : true,
//		    parent : angular.element(document.body),
//		    controllerAs : 'openHelpDataset',
//		    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/helpDataSet.html',
//		    clickOutsideToClose : false,
//		    hasBackdrop : false
//		   });
//    }
    
    $scope.openHelp = function() {	
		    	
    	// The HTML page for the Help dialog on the Type tab of the Dataset catalog
    	if ($scope.selectedTab==1) {
    		var url = sbiModule_config.contextName + "/themes/sbi_default/html/dsrules.html";
    	}
    	// The HTML page for the Help dialog on the Advanced tab of the Dataset catalog
    	else {
    		var url = sbiModule_config.contextName + "/themes/sbi_default/html/dsPersistenceHelp.html";
    	}
    	
		$http.get(url)
			.then
			(
					function(response) {
						
						$scope.templateContent = response.data;
						
						$mdDialog
						   .show({
							    scope : $scope,
							    preserveScope : true,
							    parent : angular.element(document.body),
							    controllerAs : 'datasetController',
							    templateUrl : sbiModule_config.contextName + '/js/src/angular_1.4/tools/catalogues/templates/helpDataSet.html',
							    clickOutsideToClose : false,
							    hasBackdrop : true
						   });
						
					},
					
					function() {
						alert("ERROR");
						return null;
					}
			);
	}
    
    $scope.openFieldsMetadata = function () {
    	$mdDialog
		   .show({
		    scope : $scope,
		    preserveScope : true,
		    parent : angular.element(document.body),
		    controllerAs : 'openFieldsMetadata',
		    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/fieldsMetadata.html',
		    clickOutsideToClose : false,
		    hasBackdrop : false
		   });
    }
    
    $scope.openAvaliableProfileAttributes = function () {
    	$mdDialog
		   .show({
		    scope : $scope,
		    preserveScope : true,
		    parent : angular.element(document.body),
		    controllerAs : 'openFieldsMetadata',
		    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/avaliableProfileAttributes.html',
		    clickOutsideToClose : false,
		    hasBackdrop : false
		   });
    }
    
    $scope.openLinkDataset = function () {
    	$mdDialog
		   .show({
		    scope : $scope,
		    preserveScope : true,
		    parent : angular.element(document.body),
		    controllerAs : 'openFieldsMetadata',
		    templateUrl : sbiModule_config.contextName +'/js/src/angular_1.4/tools/catalogues/templates/linkDataSet.html',
		    clickOutsideToClose : false,
		    hasBackdrop : false
		   });
    }
    
    $scope.resetWhenChangeDSType = function(dsType) {
    	
    	if (dsType.toLowerCase()=="file") {
    		
    		for (var key in $scope.fileObj) {
        		
        		if ($scope.fileObj.hasOwnProperty(key)) {				  
        			$scope.fileObj[key] = "";		    	
        		}	
        		
    		}
    		
    	}
    	
    }
	
};