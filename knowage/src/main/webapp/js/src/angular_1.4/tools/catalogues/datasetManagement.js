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
agGrid.initialiseAgGridWithAngular1(angular);
var datasetModule = angular.module('datasetModule', ['ngMaterial', 'angular-list-detail', 'sbiModule', 'angular_table', 'file_upload', 'ui.codemirror','expander-box', 'qbe_viewer','driversExecutionModule','agGrid', 'tagsModule','componentTreeModule']); //ADDDD ,'driversExecutionModule'


datasetModule.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

datasetModule
	.filter('i18n', function(sbiModule_i18n) {
		return function(label) {
			return sbiModule_i18n.getI18n(label);
		}
	})
	.controller('datasetController', ["$scope", "$log", "$http", "sbiModule_config", "sbiModule_translate", "sbiModule_restServices", "sbiModule_messaging", "sbiModule_user","$mdDialog", "multipartForm", "$timeout", "$qbeViewer","$q" ,"driversExecutionService", "$filter", "$mdSidenav","tagsHandlerService","sbiModule_urlBuilderService","$httpParamSerializer","sbiModule_download","sbiModule_i18n", datasetFunction]) /// aaddd ,"driversExecutionService"
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

		}])
		;


function datasetFunction($scope, $log, $http, sbiModule_config, sbiModule_translate, sbiModule_restServices, sbiModule_messaging, sbiModule_user, $mdDialog, multipartForm, $timeout, $qbeViewer , $q, driversExecutionService, $filter, $mdSidenav,tagsHandlerService, sbiModule_urlBuilderService, $httpParamSerializer, sbiModule_download, sbiModule_i18n){

	$scope.maxSizeStr = maxSizeStr;

	$scope.csvEncodingDefault = "UTF-8";
	$scope.csvDelimiterDefault = ",";
	$scope.csvQuoteDefault = "\"";
	$scope.skipRowsDefault = 0;
	$scope.limitRowsDefault = null;
	$scope.xslSheetNumberDefault = 1;
	$scope.dateFormatDefault = "dd/MM/yyyy";
	$scope.timestampFormatDefault = "dd/MM/yyyy HH:mm:ss";
	$scope.datasetParameters = []
	$scope.allTags = [];
	$scope.tags = [];
	$scope.location = "catalog"
	$scope.i18n=sbiModule_i18n;

	$scope.i18n.loadI18nMap();

	var parameterDeletingMessage = "Are you sure you want to delete the dataset parameter ? ";
	var qbeParameterDeletingMessage = "";
	var urlBuilderService = sbiModule_urlBuilderService;

	$scope.$watch('parameterItems',function(newValue,oldValue){
		$scope.datasetForm.$setValidity("duplicates",!$scope.hasDuplicates(newValue,'name'))
		if ($scope.selectedDataSet) $scope.selectedDataSet.pars = $scope.parameterItems
	},true)

	$scope.hasDuplicates = function(array,property){
		if(!array && !Array.isArray(array)){
			return false;
		}
		var obj = {};
		for(var i in array){
			obj[array[i][property]] = array[i]
		}

		return array.length > Object.keys(obj).length
	}

	$scope.$watch("selectedDataSet.restNGSI",function(newValue,oldValue){
		if(newValue && (newValue===true || newValue==="true")){
			$scope.selectedDataSet.restNGSI = true;
		}
	});

	var getAllTags = function(){
		sbiModule_restServices.promiseGet("2.0/tags","")
		.then(function(response) {
			$scope.allTags = response.data;
		});
	}
    getAllTags();

	$scope.$watch("selectedDataSet.restDirectlyJSONAttributes",function(newValue,oldValue){
		if(newValue && (newValue===true || newValue==="true")){
			$scope.selectedDataSet.restDirectlyJSONAttributes = true;
		}
	});

	$scope.showDatasetScheduler = sbiModule_user.functionalities.indexOf("SchedulingDatasetManagement")>-1;
	$scope.showExportHDFS = sbiModule_user.functionalities.indexOf("DataSourceBigData")>-1;

	$scope.translate = sbiModule_translate;
	$scope.codeMirror = null;
	$scope.isSomething = false;
	$scope.step=1;
	$scope.validated = false;
	$scope.dsMetaValue = [];

	$scope.dataSetListColumns = [
	    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
	    {"label":$scope.translate.load("sbi.generic.label"),"name":"label"},
	    {"label":$scope.translate.load("sbi.generic.type"), "name":"dsTypeCd", "size":"70px"},
	    {"label":$scope.translate.load("sbi.ds.numDocs"), "name":"usedByNDocs", "size":"60px"}
    ];

	$scope.sortableColumn = ["name","label","dsTypeCd"]

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
	$scope.restDsRequestAdditionalParametersTableLastPage = 1;
	$scope.restDsJsonPathAttribTableLastPage = 1;

	$scope.currentPageNumber = 0;

	$scope.fileObj={};
	$scope.selectedTab = 0;	// Initially, the first tab is selected.
	$scope.tempScope = {};
	$scope.showSaveAndCancelButtons = false;

	$scope.scheduling = {};

	$scope.disablePersisting = false;

	// The current date for data pickers for Scheduling
	var currentDate = new Date();

	$scope.minStartDate = new Date(
		currentDate.getFullYear(),
		currentDate.getMonth(),
		currentDate.getDate()
	);

	$scope.minEndDate = $scope.minStartDate;

	$scope.checkPickedEndDate = function() {

		if (new Date($scope.selectedDataSet.endDate) < new Date($scope.selectedDataSet.startDate)) {
			$scope.selectedDataSet.startDate = null;
		}

	}

	$scope.checkPickedStartDate = function() {

		if (new Date($scope.selectedDataSet.startDate) > new Date($scope.selectedDataSet.endDate)) {
			$scope.selectedDataSet.endDate = null;
		}

	}

	// Manage the Dataset type combobox items
	$scope.filterDatasetTypes = function (item) {
	    return item.VALUE_CD!='Custom' && item.VALUE_CD!='Federated';
	};

	// Flag that indicates if the Dataset form is dirty (changed)
	$scope.dirtyForm = false;

	// Functions for setting the indicator
	$scope.setFormDirty = function() {
//		console.log("set dirty");
		$scope.dirtyForm = true;
	}

	// Functions for resetting the indicator
	$scope.setFormNotDirty = function() {
		$scope.dirtyForm = false;
	}

	// =================================
	// Fields metadata AT data (START)
	// =================================

	$scope.fieldsMetadataTypes = [

      {
    	  name: "ATTRIBUTE",
    	  value: "ATTRIBUTE"
      },

      {
    	  name: "SPATIAL ATTRIBUTE",
    	  value: "SPATIAL_ATTRIBUTE"
      },

      {
    	  name: "MEASURE",
    	  value: "MEASURE"
      }

    ];

	$scope.fieldsMetadataScopeFunctions = {
		fieldsMetadataTypes: $scope.fieldsMetadataTypes,
		setFormDirty: $scope.setFormDirty
	};

	$scope.jsonPathTypes = ["string", "int", "double", "date yyy-MM-dd", "timestamp yyyy-MM-dd HH:mm:ss", "ISO8601 yyyy-MM-ddTHH:mm:ss.SSSZ", "time HH:mm:ss", "boolean"];

	$scope.fieldsMetadataColumns = [
		{
			label:"Field name",
			name:"fieldAlias",
			hideTooltip: true,

			transformer: function() {
				return '<md-input-container class="md-block" style="margin:0"><input readonly ng-model="row.fieldAlias"></md-input-container>';
			}
		},
		{
			label:"Field metadata",
			name:"fieldType",
			hideTooltip: true,

			transformer: function() {
				return '<md-select ng-model=row.fieldType class="noMargin" ng-change="scopeFunctions.setFormDirty()"><md-option ng-repeat="col in scopeFunctions.fieldsMetadataTypes" value="{{col.value}}">{{col.name}}</md-option></md-select>';
			}
		}
	];

	// =================================
	// Fields metadata AT data (END)
	// =================================


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

		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.january"), 	value: 1});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.february"), 	value: 2});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.march"), 	value: 3});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.april"), 	value: 4});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.may"), 		value: 5});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.june"), 		value: 6});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.july"), 		value: 7});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.august"), 	value: 8});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.september"), value: 9});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.october"), 	value: 10});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.november"), 	value: 11});
		$scope.months.push({name: $scope.translate.load("sbi.ds.persist.cron.month.december"), 	value: 12});

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

		$scope.weekdays.push({name: $scope.translate.load("sbi.ds.persist.cron.weekday.monday"), 	value: 1});
		$scope.weekdays.push({name: $scope.translate.load("sbi.ds.persist.cron.weekday.tuesday"), 	value: 2});
		$scope.weekdays.push({name: $scope.translate.load("sbi.ds.persist.cron.weekday.wednesday"), value: 3});
		$scope.weekdays.push({name: $scope.translate.load("sbi.ds.persist.cron.weekday.thursday"), 	value: 4});
		$scope.weekdays.push({name: $scope.translate.load("sbi.ds.persist.cron.weekday.friday"), 	value: 5});
		$scope.weekdays.push({name: $scope.translate.load("sbi.ds.persist.cron.weekday.saturday"), 	value: 6});
		$scope.weekdays.push({name: $scope.translate.load("sbi.ds.persist.cron.weekday.sunday"), 	value: 7});

	}

	populateWeekdays();

	// TODO:
	$scope.weekdaysClearSelections = function() {
		console.log("Clear weekdays (TODO)");
		$scope.weekdaysSelected = [];
	}

	// CKAN DATASET CONFIG
	$scope.ckanFileType =
	[
	 	{value:"xls",name:"XLS"},
	 	{value:"csv",name:"CSV"}
 	];

	// PYTHON ENVIRONMENTS CONFIG
	sbiModule_restServices.promiseGet('2.0/configs/category', 'PYTHON_CONFIGURATION')
	.then(function(response){
		$scope.pythonEnvironments = $scope.buildEnvironments(response.data);
	});

	// R ENVIRONMENTS CONFIG
	sbiModule_restServices.promiseGet('2.0/configs/category', 'R_CONFIGURATION')
	.then(function(response){
		$scope.rEnvironments = $scope.buildEnvironments(response.data);
	});

	$scope.buildEnvironments = function (data) {
		toReturn = []
		for (i=0; i<data.length; i++) {
			key = data[i].label;
			val = data[i].valueCheck;
			toReturn[i] = {"label": key, "value": val};
		}
		return toReturn;
	}

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

	$scope.dateFormatTypes =
		[
		 	{value:"dd/MM/yyyy",name:"dd/MM/yyyy"},
		 	{value:"MM/dd/yyyy",name:"MM/dd/yyyy"},
		 	{value:"dd-MM-yyyy",name:"dd-MM-yyyy"},
		 	{value:"MM-dd-yyyy",name:"MM-dd-yyyy"},
			{value:"yyyy-MM-dd",name:"yyyy-MM-dd"},
		 	{value:"yyyy:MM:dd",name:"yyyy:MM:dd"},
		 	{value:"dd.MM.yyyy",name:"dd.MM.yyyy"},
		 	{value:"MM.dd.yyyy",name:"MM.dd.yyyy"}

		 ];

	$scope.timestampFormatTypes = [
		{ value:"dd/MM/yyyy HH:mm:ss", name:"dd/MM/yyyy HH:mm:ss" },
	 	{ value:"MM/dd/yyyy hh:mm:ss a", name:"MM/dd/yyyy hh:mm:ss a" },
	 	{ value:"dd-MM-yyyy hh:mm:ss a", name:"dd-MM-yyyy hh:mm:ss a" },
	 	{ value:"MM-dd-yyyy hh:mm:ss a", name:"MM-dd-yyyy hh:mm:ss a" },
		{ value:"yyyy-MM-dd hh:mm:ss a", name:"yyyy-MM-dd hh:mm:ss a" },
	 	{ value:"yyyy:MM:dd hh:mm:ss a", name:"yyyy:MM:dd hh:mm:ss a" },
	 	{ value:"dd.MM.yyyy HH:mm:ss", name:"dd.MM.yyyy HH:mm:ss" },
	 	{ value:"MM.dd.yyyy HH:mm:ss", name:"MM.dd.yyyy HH:mm:ss" }
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

	$scope.chooseDateFormat = function(dateFormat) {
		$scope.selectedDataSet.dateFormat = dateFormat;
	}

	$scope.chooseTimestampFormat = function(timestampFormat) {
		$scope.selectedDataSet.timestampFormat = timestampFormat;
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
	$scope.restRequestAdditionalParameters = [];

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

	$scope.requestAdditionalParameterAddItem = function() {

		$scope.restRequestAdditionalParameters.push({"name":"","value":"","index":$scope.counterRequestAdditionalParameters++});

		$timeout(
				function() {
					var page = $scope.tableLastPage("requestAdditionalParametersTable");
					console.log(page);
					console.log($scope.restDsRequestAdditionalParametersTableLastPage);
					// If the page that is returned is less than the current one, that means that we are already on that page, so keep it (danristo)
					//console.log(page<$scope.restDsRequestHeaderTableLastPage);
					$scope.restDsRequestAdditionalParametersTableLastPage = (page<=$scope.restDsRequestAdditionalParametersTableLastPage)
						? $scope.restDsRequestAdditionalParametersTableLastPage : page;

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

	// Provide unique IDs for elements in the Request header grid, so we can remove them easily
	$scope.counterRequestAdditionalParameters = 0;

	$scope.requestAdditionalParametersDelete =
	[
		{
			label: $scope.translate.load("sbi.generic.delete"),
		 	icon:'fa fa-trash' ,
		 	backgroundColor:'transparent',

		 	action: function(item) {

		 		console.log(item);

		 		// TODO: translate
		 		var confirm = $mdDialog.confirm()
	 	          .title("Delete of REST dataset request parameter")
	 	          .targetEvent(event)
	 	          .textContent("Are you sure you want to delete request parameter")
	 	          .ariaLabel("Delete of REST dataset request parameter")
	 	          .ok($scope.translate.load("sbi.general.yes"))
	 	          .cancel($scope.translate.load("sbi.general.No"));

		 		$mdDialog
	 				.show(confirm)
	 				.then(

	 						function() {

				 	        	for (i=0; i<$scope.restRequestAdditionalParameters.length; i++) {

						 			if ($scope.restRequestAdditionalParameters[i].index == item.index) {
						 				$scope.setFormDirty();
						 				$scope.restRequestAdditionalParameters.splice(i,1);
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
	        	 return '<md-select ng-model=row.jsonPathType class="noMargin"><md-option ng-repeat="type in scopeFunctions.jsonPathTypes" value="{{type}}">{{type}}</md-option></md-select>';
	    	 }
	     }
     ];

	$scope.restJsonPathAttributes = [];
	$scope.counterJsonAttributes = 0;

	$scope.jsonPathAttrScopeFunctions = {
		setFormDirty: $scope.setFormDirty,
		jsonPathTypes: $scope.jsonPathTypes
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
        		return '<md-input-container class="md-block" style="margin:0"><input ng-trim="false" ng-model="row.defaultValue" ng-change="scopeFunctions.setFormDirty()"></md-input-container>';
        	}
		},

		{
			"label":$scope.translate.load("sbi.ds.multivalue"),
			"name":"multiValue",
			hideTooltip:true,

        	transformer: function() {
        		return '<md-checkbox ng-model="row.multiValue"  aria-label="Checkbox"></md-checkbox>';
        	}
		}

    ];

	$scope.parameterItems = [];
	$scope.parametersCounter = 0;

	$scope.parametersAddItem = function(event) {

		if($scope.selectedDataSet.isPersisted == true) {
			var confirm = $mdDialog.confirm()
				         .title($scope.translate.load("sbi.ds.parameters.dialog.title"))
				         .targetEvent(event)
				         .textContent($scope.translate.load("sbi.ds.parameters.dialog.text"))
				         .ariaLabel("Add dataset parameter")
				         .ok($scope.translate.load("sbi.general.yes"))
				         .cancel($scope.translate.load("sbi.general.No"));

			$mdDialog.show(confirm).then(
					function() {
						$scope.selectedDataSet.isPersisted = false;
						$scope.selectedDataSet.persistTableName = '';
						$scope.disablePersisting = true;
						$scope.parameterItems.push({"name":"","type":"", "defaultValue":"","multiValue":"","index":$scope.parametersCounter++});

						$timeout(
									function() {
										var page = $scope.tableLastPage("datasetParametersTable");
										// If the page that is returned is less than the current one, that means that we are already on that page, so keep it (danristo)
										$scope.parametersTableLastPage = page<$scope.parametersTableLastPage ? $scope.parametersTableLastPage : page;
									},

									300
								);
					}
			);
		} else {
			$scope.disablePersisting = true;
			$scope.parameterItems.push({"name":"","type":"", "defaultValue":"","multiValue":"","index":$scope.parametersCounter++});

			$timeout(
						function() {
							var page = $scope.tableLastPage("datasetParametersTable");
							// If the page that is returned is less than the current one, that means that we are already on that page, so keep it (danristo)
							$scope.parametersTableLastPage = page<$scope.parametersTableLastPage ? $scope.parametersTableLastPage : page;
						},

						300
					);
		}

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
			         .textContent($scope.selectedDataSet.dsTypeCd == "Qbe" ? qbeParameterDeletingMessage : parameterDeletingMessage)
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

								if($scope.parameterItems.length == 0) {
									$scope.disablePersisting = false;
								}

					 		}
						);
	 		}

	 	}
	 ];

	$scope.deleteAllParameters = function() {

		if ($scope.parameterItems.length>0)
		{
			// TODO: translate
	    	var confirm = $mdDialog.confirm()
		         .title("Clear all dataset parameters")
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
							$scope.disablePersisting = false;
				 		}
					);
		}
		else {

			$mdDialog
			.show(
					$mdDialog.alert()
				        .clickOutsideToClose(true)
				        .title('Dataset has no parameters to delete')
				        .textContent('There are not parameters to delete for the selected dataset')
				        .ariaLabel('Dataset has no parameters to delete')
				        .ok('Ok')
			    );

		}

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

    $scope.filterByTags = function(){
    	$timeout(function () {
			$scope.datasetLike($scope.searchValue,$scope.itemsPerPage, $scope.currentPageNumber, $scope.columnsSearch, $scope.columnOrdering, $scope.reverseOrdering)
		 }, 1000);
    }
    $scope.datasetLike = function (searchValue, itemsPerPage,currentPageNumber, columnsSearch, columnOrdering, reverseOrdering) {
    	$scope.reverseOrdering = reverseOrdering;
    	$scope.columnOrdering = columnOrdering;
    	$scope.searchValue = searchValue;
    	var columnOrderingLabel = "";
    	if($scope.columnOrdering){
    		columnOrderingLabel = $scope.columnOrdering.name;
    	}
    	urlBuilderService.setBaseUrl("countDataSetSearch/");

    	if($scope.searchValue != "" || tagsHandlerService.getFilteredTagIds($scope.allTags).length > 0){

    		var tags = {"tags":tagsHandlerService.getFilteredTagIds($scope.allTags)};
    		var searchValue = {"searchValue" : $scope.searchValue}
    		if($scope.searchValue != null)
    		urlBuilderService.addQueryParams(searchValue);
    		if(tagsHandlerService.getFilteredTagIds($scope.allTags).length > 0)
    		urlBuilderService.addQueryParams(tags);
    		var url = tagsHandlerService.getFilteredTagIds($scope.allTags).length > 0 || $scope.searchValue != null ?  urlBuilderService.build() : "countDataSets";
    		sbiModule_restServices.promiseGet("1.0/datasets",  url)
    		.then(function(response) {
    			$scope.numOfDs = response.data;
    			$scope.loadDatasetList(0, itemsPerPage, $scope.searchValue,  columnOrderingLabel,reverseOrdering);
    		}, function(response) {
    			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
    		});

    	} else if ($scope.searchValue=="") {
    		$scope.loadDatasetList(0, itemsPerPage, searchValue, columnOrderingLabel,reverseOrdering);
    	}
    };

    $scope.changeDatasetPage=function(itemsPerPage,currentPageNumber){
    	$scope.currentItemsPerPage = itemsPerPage;
    	if($scope.currentPageNumber != currentPageNumber){
	    	$scope.currentPageNumber = currentPageNumber;
	    	if($scope.searchValue==undefined || $scope.searchValue.length==0 ){
	    		sbiModule_restServices.promiseGet("1.0/datasets", "countDataSets")
	    		.then(function(response) {
	    			$scope.numOfDs = response.data;
	    			var start = 0;
	    			if(currentPageNumber>1){
	    				start = (currentPageNumber - 1) * itemsPerPage;
	    			}
	    			if($scope.searchValue==undefined){
	    				$scope.searchValue = null;
	    			}
	    			if($scope.reverseOrdering==undefined){
	    				$scope.reverseOrdering=false;
	    			}

	    			var columnOrderingLabel = "";
	    	    	if($scope.columnOrdering){
	    	    		columnOrderingLabel = $scope.columnOrdering.name;
	    	    	}
	    			$scope.loadDatasetList(start, itemsPerPage, $scope.searchValue,columnOrderingLabel, $scope.reverseOrdering);
	    		}, function(response) {
	    			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
	    		});
		} else if ($scope.searchValue!=undefined || $scope.searchValue.length!=0 ||  tagsHandlerService.getFilteredTagIds($scope.allTags).length > 0) {
	    		var tags = {"tags":tagsHandlerService.getFilteredTagIds($scope.allTags)};
	    	urlBuilderService.setBaseUrl("countDataSetSearch/");
	        var tags = {"tags":tagsHandlerService.getFilteredTagIds($scope.allTags)};
	        urlBuilderService.addQueryParams(tags);
	    	var searchValue = {"searchValue" : $scope.searchValue}
    		urlBuilderService.addQueryParams(searchValue);
			sbiModule_restServices.promiseGet("1.0/datasets", urlBuilderService.build())
	    		.then(function(response) {
	    			$scope.numOfDs = response.data;
	    			var start = 0;
	    			if(currentPageNumber>1){
	    				start = (currentPageNumber - 1) * itemsPerPage;
	    			}
	    			$scope.loadDatasetList(start, itemsPerPage, $scope.searchValue, "",null);
	    		}, function(response) {
	    			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
	    		});
			}
    	}
	}

	/*
	 * 	service that loads datasets filtered by provided configuration
	 *
	 */
	$scope.loadDatasetList = function(start, limit, filter,columnOrderingName, reverseOrdering){

		var queryParams = "offset="+start+"&fetchSize="+limit;
		if(filter!=null && filter!=""){
			var filters = [
				{"columnFilter":"label","typeValueFilter":"","typeFilter":"like","valueFilter":filter},
				{"columnFilter":"name","typeValueFilter":"","typeFilter":"like","valueFilter":filter}
			];
			queryParams = queryParams+"&filters="+angular.toJson(filters);
		}
		if(reverseOrdering!==null && reverseOrdering!==""){
			var ordering = {"reverseOrdering":reverseOrdering,
							"columnOrdering":columnOrderingName

			};
			queryParams = queryParams+"&ordering="+angular.toJson(ordering);
		}

		if(tagsHandlerService.getFilteredTagIds($scope.allTags).length > 0){
    		//queryParams = queryParams+"&tags="+tagsHandlerService.getFilteredTagIds();
			urlBuilderService.setBaseUrl("");
			var tagsForSending = {"tags":tagsHandlerService.getFilteredTagIds($scope.allTags)}
    		urlBuilderService.addQueryParams(tagsForSending);
    		queryParams = queryParams+"&"+urlBuilderService.build().substr(1)
		}


		sbiModule_restServices.promiseGet("3.0/datasets","pagopt", queryParams)
			.then(function(response) {
				$scope.datasetsListTemp = angular.copy(response.data.root);
				$scope.datasetsListPersisted = angular.copy($scope.datasetsListTemp);
			    if (filter == "") {
					sbiModule_restServices.promiseGet("1.0/datasets", "countDataSets")
					.then(function(response) {
						$scope.numOfDs = response.data;},function(response){
							sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
						});
				} else if(filter!=null || tagsHandlerService.getFilteredTagIds($scope.allTags).length > 0){
					urlBuilderService.setBaseUrl("countDataSetSearch/");
					var tags = {"tags":tagsHandlerService.getFilteredTagIds($scope.allTags)};
					urlBuilderService.addQueryParams(tags);
					var searchValue = {"searchValue" : $scope.searchValue}
		    		urlBuilderService.addQueryParams(searchValue);
					var url = (tagsHandlerService.getFilteredTagIds($scope.allTags).length == 0 && ($scope.searchValue == "" || $scope.searchValue == null)) ? "countDataSetSearch/" : urlBuilderService.build();
					sbiModule_restServices.promiseGet("1.0/datasets",url)
					.then(function(response) {
						$scope.numOfDs = response.data;},function(response){
							sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
						});
				}
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});

	}



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

									var escapedLabel = encodeURIComponent(item.label);
									sbiModule_restServices.promiseDelete("1.0/datasets", escapedLabel, "/")
										.then(
												function(response) {

													sbiModule_messaging.showSuccessMessage($scope.translate.format($scope.translate.load('sbi.ds.deletedataset.success'), item.label));
												var columnOrderingLabel = "";
												if($scope.columnOrdering){
													columnOrderingLabel = $scope.columnOrdering.name;
												}
													var start = 0;
													if($scope.currentPageNumber>1){
														start = ($scope.currentPageNumber - 1) * $scope.currentItemsPerPage;
													}
													$scope.loadDatasetList(start, $scope.currentItemsPerPage, $scope.searchValue,columnOrderingLabel,$scope.reverseOrdering);
													// If the dataset that is deleted is selected, deselect it and hence close its details.
													if ($scope.selectedDataSet && $scope.selectedDataSet.label == item.label) {
														$scope.selectedDataSet = null;
													}

													/*// Find the dataset, that is deleted on the server-side, in the array of all datasets and remove it from the array.
													for (var i=0; i<$scope.datasetsListTemp.length; i++) {
														if ($scope.datasetsListTemp[i].label == item.label) {
//															console.log($scope.dirtyForm);
//															console.log($scope.selectedDataSet.label == item.label);
															$scope.datasetsListTemp.splice(i,1);
															$scope.datasetsListPersisted.splice(i,1);
															$scope.showSaveAndCancelButtons = false;
															break;
														}
													}

													$scope.selectedTab = null;*/

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
				icon:'fa fa-files-o',
				backgroundColor:'transparent',

				action: function(item,event) {

					sbiModule_restServices.promiseGet('1.0/datasets', item.label).then(function(response) {

						var item = response.data[0];

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
					});
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

			 					   						//if($scope.selectedDataSet.dsTypeCd.toLowerCase()=="file") {
			 					   						//$scope.refactorFileDatasetConfig(response.data[0]);
			 					   						//$scope.changingFile = false;
			 					   						//}

			 					   						// Remove the dataset's version from the collection of all datasets (the array in the left angular-table).
	 					   								$scope.datasetsListTemp[i] = angular.copy(response.data[0]);
	 					   								// Remove the version from the currently selected dataset (the item in the left angular-table).
	 					   								$scope.selectedDataSet = angular.copy(response.data[0]);
	 					   								// Needed in order to have a copy of the selected dataset that will not influence the selected dataset in the AT while performing changes on it
	 					   								$scope.selectedDataSetInit = angular.copy(response.data[0]);
	 					   								// Call the scope function that is responsible for transformation of configuration data of the File dataset.

		 					   							// DATASET PARAMETERS
		 					   							var parameterItemsTemp = [];

		 					   							var parameterItems = $scope.selectedDataSet.pars;
		 					   							var parameterItemsLength = parameterItems.length;

		 					   							for (j=0; j<parameterItemsLength; j++) {

		 					   								var parameterItemTemp = {};

		 					   								parameterItemTemp["name"] = parameterItems[j]["name"];
		 					   								parameterItemTemp["type"] = parameterItems[j]["type"];
		 					   								parameterItemTemp["defaultValue"] = parameterItems[j]["defaultValue"];
		 					   								parameterItemTemp["multiValue"] = parameterItems[j]["multiValue"];
		 					   								parameterItemTemp["index"] = $scope.parametersCounter++;

		 					   								parameterItemsTemp.push(parameterItemTemp);

		 					   							}

		 					   							$scope.parameterItems = parameterItemsTemp;


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
				$scope.datasetTypeList = angular.copy(response.data);
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

	$scope.getProfileAttributes = function() {

		sbiModule_restServices.promiseGet("2.0/attributes","")
			.then(function(response) {
				$log.info("All Profile attributes are retrieved for the user");
				$scope.profileAttributes = angular.copy(response.data);
			}, function(response) {
				alert("ERROR");
	//			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});

	}

	var exctractFieldsMetadata = function(array, type) {

		var object = {};

		for(var item in array){
		     var element = object[array[item].column];
		     if(!element){
		    	 element = {};
		         object[array[item].column] = element;
		         element["column"] = array[item].column;
		     }
		     element[array[item].pname] = array[item].pvalue;
		}

		var fieldsMetadata = new Array();

		for (item in object) {
			fieldsMetadata.push(object[item]);
		}

		return fieldsMetadata;

	}

	$scope.saveFieldsMetadata = function() {

		console.log($scope.fieldsMetadata);

		var datasetFieldsMetadata = $scope.selectedDataSet.meta.columns;

		console.log(datasetFieldsMetadata);
		console.log($scope.datasetMetaWithFieldsMetaIndexes);

		var numberOfSpatialAttribute = 0;
		for (i=0; i<$scope.fieldsMetadata.length; i++) {
			if($scope.fieldsMetadata[i].fieldType == "SPATIAL_ATTRIBUTE"){
				numberOfSpatialAttribute++;
				if(numberOfSpatialAttribute > 1) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.ds.field.metadata.duplicateSpatialAttribute"), sbiModule_translate.load('sbi.generic.error'));
					return;
				}
			}
		}

		for (i=0; i<$scope.fieldsMetadata.length; i++) {
			//var index = $scope.datasetMetaWithFieldsMetaIndexes[i];
			//$scope.selectedDataSet.meta.columns[index].pvalue = $scope.fieldsMetadata[i].fieldType;
			for(j=0; j<$scope.selectedDataSet.meta.columns.length; j++){
				if($scope.fieldsMetadata[i].column==$scope.selectedDataSet.meta.columns[j].column && $scope.selectedDataSet.meta.columns[j].pname=="fieldType"){
					$scope.selectedDataSet.meta.columns[j].pvalue=$scope.fieldsMetadata[i].fieldType;
				}
			}
		}

		console.log("posle: ",$scope.selectedDataSet.meta.columns);

		$scope.closeScript();
	}

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
		sbiModule_restServices.promiseGet('1.0/datasets', item.label).then(function(response) {

			var item = response.data[0];

			$scope.isSelected = true;
			$scope.step=1;

			// Load the Dataset's older versions
			if(!item.hasOwnProperty('selected') ||
					(item.hasOwnProperty('selected') && item.selected != true)) {
				var defer = $q.defer();
				var olderVersionsPromise = loadOlderVersions(item.id);
				olderVersionsPromise.then(function(response){
					item.dsVersions = response;
					item.selected = true;
					$scope.selectedDataSetInit = angular.copy(item);
					$scope.selectedDataSet = angular.copy(item);
					defer.resolve(response);
				}, function(error){
					sbiModule_messaging.showErrorMessage(error, 'Error');
				});
			}

			 if (!$scope.selectedDataSet) {
				 //console.log("a2");
				 $scope.setFormNotDirty();
				 selectDataset(item,index);
			 }
			 // Moving from selected new DS to existing DS
			 else if (!$scope.selectedDataSet.id) {

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

						 selectDataset(item, index);
						 $scope.setFormNotDirty();
					 }

				 }

			 }
			 // Moving from an existing DS to another one
//			 else if ($scope.selectedDataSet && $scope.selectedDataSet.id!=item.id) {
			 else if ($scope.selectedDataSet.id!=item.id) {
				 //console.log("c4");
				if ($scope.dirtyForm) {

					// TODO: translate
					var confirm = $mdDialog.confirm()
					        .title($scope.translate.load("sbi.catalogues.generic.modify"))
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
			 if($scope.selectedDataSet.dsTypeCd == "Qbe" && !qbeParameterDeletingMessage.includes("Qbe") ){
					qbeParameterDeletingMessage =  parameterDeletingMessage + "Parameters for Qbe Dataset should be deleted from qbeDesigner";
			 }
			 if($scope.selectedDataSet.dsTypeCd == "Qbe" && item.hasOwnProperty('selected') && item.selected == true) {
				 $scope.getDatasetParametersFromBusinessModel($scope.selectedDataSet);
			 }

		},

			   			function(response) {
			   				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			   			});
	};

	$scope.getDatasetParametersFromBusinessModel = function (selectedDataset){
			sbiModule_restServices.post("dataset","drivers/",selectedDataset.qbeDatamarts).then(function(response){
				$scope.selectedDataSet.drivers = angular.copy(response.data.filterStatus);
				if ($scope.selectedDataSet.drivers) {
					for(var i = 0; i < $scope.selectedDataSet.drivers.length; i++) {
						if($scope.selectedDataSet.drivers[i].parameterValue && $scope.selectedDataSet.drivers[i].parameterDescription) {
							$scope.selectedDataSet.drivers[i].hasDefaultOrOneAdmissibleValue = true;
						}
					}
				}
				var selectedModel = $filter('filter')($scope.datamartList, {name: $scope.selectedDataSet.qbeDatamarts},true)[0];
				 if (!selectedModel) delete $scope.selectedDataSet.qbeJSONQuery;
			})
		}
	var selectDataset = function(item,index) {

		$scope.selectedDataSetInit = angular.copy(item);
		$scope.selectedDataSet = angular.copy(item);
		$scope.showSaveAndCancelButtons = true;
		getTagsForDataset(item);
		if($scope.selectedDataSet.pars.length > 0) {
			$scope.disablePersisting = true;
		} else {
			$scope.disablePersisting = false;
		}

		// SCHEDULING
		if ($scope.selectedDataSet.isScheduled) {
			$scope.selectedDataSet.startDate = new Date($scope.selectedDataSet.startDate);

			if (!$scope.selectedDataSet.endDate) {
				$scope.selectedDataSet.endDate = null;
			}
			else {
				$scope.selectedDataSet.endDate = new Date($scope.selectedDataSet.endDate);
			}

			// Deparse the CRON from the response (since we do not need seconds from it)
			var splitCron = $scope.selectedDataSet.schedulingCronLine.split(" ");
			var cronNoSeconds = "";
			var selectedMinutesCronString = splitCron[1]!="*" ? splitCron[1] : null;
			var selectedHoursCronString = splitCron[2]!="*" ? splitCron[2] : null;
			var selectedDaysCronString = splitCron[3]!="*" ? splitCron[3] : null;
			var selectedMonthsCronString = splitCron[4]!="*" ? splitCron[4] : null;
			var selectedWeekdaysCronString = splitCron[5]!="*" && splitCron[5]!="?" ? splitCron[5] : null;

			for (i=1; i<splitCron.length; i++) {
				cronNoSeconds += splitCron[i] + " ";
			}

			$scope.selectedDataSet.schedulingCronLine = cronNoSeconds;

			$scope.scheduling.cronDescriptionDate = prettyCron.toString($scope.selectedDataSet.schedulingCronLine);
			$scope.scheduling.cronDescriptionTime = prettyCron.getNext($scope.selectedDataSet.schedulingCronLine);

			// =====================
			// Comboboxes
			// =====================

			// MINUTES

			var splitMinutes = new Array();

			if (selectedMinutesCronString!=null) {

				var minutesTemp = selectedMinutesCronString.split(",");

				for (i=0; i<minutesTemp.length; i++) {
					splitMinutes.push(minutesTemp[i]);
				}

				$scope.scheduling.minutesSelected = splitMinutes;
				$scope.scheduling.minutesCustom = true;

			}
			else {
				$scope.scheduling.minutesSelected = [];
				$scope.scheduling.minutesCustom = false;
			}

			// HOURS

			var splitHours = new Array();

			if (selectedHoursCronString!=null) {

				var hoursTemp = selectedHoursCronString.split(",");

				for (i=0; i<hoursTemp.length; i++) {
					splitHours.push(hoursTemp[i]);
				}

				$scope.scheduling.hoursSelected = splitHours;
				$scope.scheduling.hoursCustom = true;

			}
			else {
				$scope.scheduling.hoursSelected = [];
				$scope.scheduling.hoursCustom = false;
			}

			// DAYS

			var splitDays = new Array();

			if (selectedDaysCronString!=null) {

				var daysTemp = selectedDaysCronString.split(",");

				for (i=0; i<daysTemp.length; i++) {
					splitDays.push(daysTemp[i]);
				}

				$scope.scheduling.daysSelected = splitDays;
				$scope.scheduling.daysCustom = true;

			}
			else {
				$scope.scheduling.daysSelected = [];
				$scope.scheduling.daysCustom = false;
			}

			// MONTHS

			var splitMonths = new Array();

			if (selectedMonthsCronString!=null) {

				var monthsTemp = selectedMonthsCronString.split(",");

				for (i=0; i<monthsTemp.length; i++) {
					splitMonths.push(monthsTemp[i]);
				}

				$scope.scheduling.monthsSelected = splitMonths;
				$scope.scheduling.monthsCustom = true;

			}
			else {
				$scope.scheduling.monthsSelected = [];
				$scope.scheduling.monthsCustom = false;
			}

			// WEEKDAYS

			var splitWeekdays = new Array();

			if (selectedWeekdaysCronString!=null) {

				var weekdaysTemp = selectedWeekdaysCronString.split(",");

				for (i=0; i<weekdaysTemp.length; i++) {
					splitWeekdays.push(weekdaysTemp[i]);
				}

				$scope.scheduling.weekdaysSelected = splitWeekdays;
				$scope.scheduling.weekdaysCustom = true;

			}
			else {
				$scope.scheduling.weekdaysSelected = [];
				$scope.scheduling.weekdaysCustom = false;
			}

		}

		// DATASET PARAMETERS
		var parameterItemsTemp = [];

		var parameterItems = $scope.selectedDataSet.pars;
		var parameterItemsLength = parameterItems.length;
		if($scope.selectedDataSet.dsTypeCd == "Qbe")
		 $scope.getDatasetParametersFromBusinessModel($scope.selectedDataSet);
		for (j=0; j<parameterItemsLength; j++) {

			var parameterItemTemp = {};

			parameterItemTemp["name"] = parameterItems[j]["name"];
			parameterItemTemp["type"] = parameterItems[j]["type"];
			parameterItemTemp["defaultValue"] = parameterItems[j]["defaultValue"];
			parameterItemTemp["multiValue"] = parameterItems[j]["multiValue"];
			parameterItemTemp["index"] = $scope.parametersCounter++;

			parameterItemsTemp.push(parameterItemTemp);

		}

		$scope.parameterItems = parameterItemsTemp;

		if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="rest" || $scope.selectedDataSet.dsTypeCd.toLowerCase()=="python/r") {
			// Cast the REST NGSI (transform from the String)
			if($scope.selectedDataSet.restNGSI){
				$scope.selectedDataSet.restNGSI = JSON.parse($scope.selectedDataSet.restNGSI);
			}
		}

		if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="rest" || $scope.selectedDataSet.dsTypeCd.toLowerCase()=="python/r" || $scope.selectedDataSet.dsTypeCd.toLowerCase()=="solr") {



			// Cast the REST directly JSON attributes (transform from the String)
			if($scope.selectedDataSet.restDirectlyJSONAttributes){
				$scope.selectedDataSet.restDirectlyJSONAttributes = JSON.parse($scope.selectedDataSet.restDirectlyJSONAttributes);
			} else {
				$scope.selectedDataSet.restDirectlyJSONAttributes = $scope.selectedDataSet.restDirectlyJSONAttributes;

			}
			// REST REQUEST HEADERS
			var restRequestHeadersTemp = [];
			//var counter = 0;

			function isObjectEmpty(obj) {
				  for(var prop in obj) {
				    if(obj.hasOwnProperty(prop)) {
				      return false;
				    }
				  }
				  return JSON.stringify(obj) === JSON.stringify({});
				}

			if($scope.selectedDataSet.restRequestHeaders != undefined && !isObjectEmpty($scope.selectedDataSet.restRequestHeaders)) {
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
			}



			$scope.restRequestHeaders = restRequestHeadersTemp;

			// REST ADDITIONAL PARAMETERS
			if($scope.selectedDataSet.restRequestAdditionalParameters){
				$scope.restRequestAdditionalParameters = angular.copy($scope.selectedDataSet.restRequestAdditionalParameters);
			}else{
				$scope.restRequestAdditionalParameters=[];
			}

			// REST JSON PATH
			var restJsonPathAttributesTemp = [];

			if($scope.selectedDataSet.restJsonPathAttributes) {
				var restJsonPathAttributes = JSON.parse($scope.selectedDataSet.restJsonPathAttributes);
				var restJsonPathAttributesLength = restJsonPathAttributes.length;
			}

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

		$scope.showSaveAndCancelButtons = true;

		$log.info("Selected dataset: ",$scope.selectedDataSet);

		$scope.setFormNotDirty();

	}



	var loadOlderVersions = function(id) {
		var deferred = $q.defer();

		var promise = sbiModule_restServices.promiseGet('1.0/datasets/olderversions', id);
		promise.then(function(response){
			var result = response.data.root;
			for (i in result) {
				var currResult = result[i];
				if (currResult.dateIn != null) {
					currResult.dateIn = $filter('date')(currResult.dateIn, 'yyyy/MM/dd hh:mm:ss');
				}
			}
			deferred.resolve(result);
		}, function(error) {
			if(error.data && error.data.errors)
				deferred.reject(error.data.errors[0].message);
   		});

		return deferred.promise;
	}

	$scope.refactorFileDatasetConfig = function(item) {

		$scope.selectedDataSet.fileType = item!=undefined ? item.fileType : "";
		$scope.selectedDataSet.fileName = item!=undefined ? item.fileName : "";
		$scope.selectedDataSetInitialFileName = $scope.selectedDataSet.fileName;

		$scope.limitPreviewChecked = false;

		$scope.selectedDataSet.csvEncoding = item!=undefined ? item.csvEncoding : $scope.csvEncodingDefault;
		$scope.selectedDataSet.csvDelimiter = item!=undefined ? item.csvDelimiter : $scope.csvDelimiterDefault;
		$scope.selectedDataSet.csvQuote = item!=undefined ? item.csvQuote : $scope.csvQuoteDefault;
		$scope.selectedDataSet.csvQuote = item!=undefined ? item.csvQuote : $scope.csvQuoteDefault;
		$scope.selectedDataSet.dateFormat = (item!=undefined && item.dateFormat!=undefined) ? item.dateFormat : $scope.dateFormatDefault;
		$scope.selectedDataSet.timestampFormat = (item!=undefined && item.timestampFormat!=undefined) ? item.timestampFormat : $scope.timestampFormatDefault;

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

		$scope.step=1;

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

	};

	var makeNewDataset = function() {

			var object = {
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
					versNum:"",
					trasfTypeCd: "",
					restAddress: "",
					pythonScript: "",
					dataframeName: "",
					solrCollection: "",
					restDirectlyJSONAttributes: "",
					restFetchSize: "",
					restHttpMethod: "",
					restJsonPathAttributes: "",
					restJsonPathItems: "",
					restMaxResults: "",
					restNGSI: "",
					restOffset: "",
					restRequestBody: "",
					restRequestHeaders: "",
					restRequestAdditionalParameters: [],
					solrFieldList: "",
					solrType: "FACETS",
					solrFacetQuery: "",
					solrFacetField: "",
					solrFacetPrefix: "",
					sparqlEndpoint: "",
					sparqlDefaultGraphIri: "",
					sparqlQuery: "",
					sparqlExecutionTimeout: 30

			}

			$scope.datasetsListTemp.push(object);
			$scope.selectedDataSet = angular.copy($scope.datasetsListTemp[$scope.datasetsListTemp.length-1]);
			$scope.selectedDataSetInit = angular.copy($scope.datasetsListTemp[$scope.datasetsListTemp.length-1]); // Reset the selection (none dataset item will be selected) (danristo)
			$scope.showSaveAndCancelButtons = true;

			$scope.transformDatasetState = false;
			$scope.disablePersisting = false;

			// Give a little time for the AT to render after the insertion of a new table element (new dataset) (danristo)
			// We do not need to check if the current page is the one that is return by a function, since we cannot add more than one empty dataset
			$timeout(function() { var page = $scope.tableLastPage("datasetList_id"); $scope.datasetTableLastPage = (page<=$scope.datasetTableLastPage)
			? $scope.datasetTableLastPage : page;  }, 100);

			$scope.isCategoryRequired = false;

	}

	$scope.saveDataset = function() {

		// Transformation refactoring (if the transformation is not checked, clean all the data that bind to the model for this option)
		if ($scope.transformDatasetState==false) {
			$scope.selectedDataSet.trasfTypeCd ? $scope.selectedDataSet.trasfTypeCd="" : null;
			$scope.selectedDataSet.pivotColName ? $scope.selectedDataSet.pivotColName="" : null;
			$scope.selectedDataSet.pivotColValue ? $scope.selectedDataSet.pivotColValue="" : null;
			$scope.selectedDataSet.pivotIsNumRows ? $scope.selectedDataSet.pivotIsNumRows="" : null;
			$scope.selectedDataSet.pivotRowName ? $scope.selectedDataSet.pivotRowName="" : null;
		}

		if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="rest" || $scope.selectedDataSet.dsTypeCd.toLowerCase()=="python/r" || $scope.selectedDataSet.dsTypeCd.toLowerCase()=="solr") {

			//----------------------
			// REQUEST HEADERS
			//----------------------
			var restRequestHeadersTemp = {};

			for (i=0; i<$scope.restRequestHeaders.length; i++) {
				restRequestHeadersTemp[$scope.restRequestHeaders[i]["name"]] = $scope.restRequestHeaders[i]["value"];
			}

			$scope.selectedDataSet.restRequestHeaders = angular.copy(JSON.stringify(restRequestHeadersTemp));

			if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="solr") {
				//----------------------
				// REQUEST ADDITIONAL PARAMETERS
				//----------------------
				$scope.selectedDataSet.restRequestAdditionalParameters = $scope.restRequestAdditionalParameters;

			}

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
			if($scope.dataset!=undefined && $scope.dataset.hasOwnProperty("meta")){
				$scope.selectedDataSet.meta = $scope.dataset.meta;
			}
		}

		// Scheduling refactoring
		if ($scope.selectedDataSet.isScheduled) {

			// If the start date is not set, set today's date as the value
			if ($scope.selectedDataSet.startDate==null) {
				$scope.selectedDataSet.startDate = new Date();
			}

			var finalCronString = "";

			var secondsForCron = 0;
			var minutesForCron = "";
			var hoursForCron = "";
			var daysForCron = "";
			var monthsForCron = "";
			var weekdaysForCron = "";

			if ($scope.scheduling.minutesCustom) {
				for (i=0; i<$scope.scheduling.minutesSelected.length; i++) {
					minutesForCron += "" + $scope.scheduling.minutesSelected[i];

					if (i<$scope.scheduling.minutesSelected.length-1) {
						minutesForCron += ",";
					}
				}
			}
			else {
				minutesForCron = "*";
			}

			if ($scope.scheduling.hoursCustom) {
				for (i=0; i<$scope.scheduling.hoursSelected.length; i++) {
					hoursForCron += "" + $scope.scheduling.hoursSelected[i];

					if (i<$scope.scheduling.hoursSelected.length-1) {
						hoursForCron += ",";
					}
				}
			}
			else {
				hoursForCron = "*";
			}

			if ($scope.scheduling.daysCustom) {
				for (i=0; i<$scope.scheduling.daysSelected.length; i++) {
					daysForCron += "" + $scope.scheduling.daysSelected[i];

					if (i<$scope.scheduling.daysSelected.length-1) {
						daysForCron += ",";
					}
				}
			}
			else {
				daysForCron = "*";
			}

			if ($scope.scheduling.monthsCustom) {
				for (i=0; i<$scope.scheduling.monthsSelected.length; i++) {
					monthsForCron += "" + $scope.scheduling.monthsSelected[i];

					if (i<$scope.scheduling.monthsSelected.length-1) {
						monthsForCron += ",";
					}
				}
			}
			else {
				monthsForCron = "*";
			}

			if ($scope.scheduling.weekdaysCustom) {
				for (i=0; i<$scope.scheduling.weekdaysSelected.length; i++) {
					weekdaysForCron += "" + $scope.scheduling.weekdaysSelected[i];

					if (i<$scope.scheduling.weekdaysSelected.length-1) {
						weekdaysForCron += ",";
					}
				}
			}
			else {
				weekdaysForCron = "*";
			}

			if (daysForCron == '*' && weekdaysForCron != '*') {
				daysForCron = '?';
			} else {
				weekdaysForCron = '?';
			}

			finalCronString = minutesForCron + " " + hoursForCron +
								" " + daysForCron + " " + monthsForCron +  " " + weekdaysForCron;

			console.log(finalCronString);
			$scope.selectedDataSet.schedulingCronLine = secondsForCron + " " + finalCronString;

			$scope.scheduling.cronDescriptionDate = prettyCron.toString(finalCronString);
			$scope.scheduling.cronDescriptionTime = prettyCron.getNext(finalCronString);

			console.log($scope.scheduling.cronDescriptionDate);
			console.log($scope.scheduling.cronDescriptionTime);

		}

		$scope.selectedDataSet.recalculateMetadata = true;
		$scope.manageDatasetFieldMetadata($scope.selectedDataSet.meta);

		// ADVANCED tab
		// For transforming
		$scope.transformDatasetState==true ? $scope.selectedDataSet.trasfTypeCd=$scope.transformationDataset.VALUE_CD : null;

		if ($scope.selectedDataSet) {

			$log.info("save");

			var indexOfExistingDSInAT = -1;

			//Existing DS (PUT)
			if ($scope.selectedDataSet.id) {
				for (i=0; i<$scope.datasetsListTemp.length; i++) {
					if ($scope.datasetsListTemp[i].id == $scope.selectedDataSet.id) {
						indexOfExistingDSInAT = i;
					}
				}
			}
			// New DS (POST)
			else {
				indexOfExistingDSInAT = $scope.datasetsListTemp.length-1;
			}

			/**
			 * Reconfigure the start and end date by substracting the time difference for the data that will be stringified (since stringify() or
			 * angular.toJson()), since this stringification introduces this time time offset. (danristo)
			 */
			$scope.selectedDataSet.startDate ? $scope.selectedDataSet.startDate.setHours($scope.selectedDataSet.startDate.getHours() - $scope.selectedDataSet.startDate.getTimezoneOffset() / 60) : null;
			$scope.selectedDataSet.endDate ? $scope.selectedDataSet.endDate.setHours($scope.selectedDataSet.endDate.getHours() - $scope.selectedDataSet.endDate.getTimezoneOffset() / 60) : null;

            var datasetAsString = JSON.stringify($scope.selectedDataSet);
            if($scope.selectedDataSet.isPersisted && datasetAsString.includes("${")) {
                sbiModule_messaging.showErrorMessage("You cannot persist a dataset that uses profile attributes. Please verify your dataset configuration");
            } else {

                sbiModule_restServices.promisePost('1.0/datasets','', angular.toJson($scope.selectedDataSet))
                    .then(
                            function(response) {
                                sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');

                                if($scope.selectedDataSet.isPersistedHDFS) {
                                    sbiModule_restServices.promisePost('1.0/hdfs',response.data.id)
                                    .then(
                                            function(responseHDFS) {
                                                sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.ds.hdfs.request.work"), 'Success!');
                                            },

                                            function(responseHDFS) {
                                                sbiModule_messaging.showErrorMessage(responseHDFS.data.errors[0].message, 'Error');
                                            }
                                        );
                                }


                                if(tagsHandlerService.isTagDeleted() || $scope.tags.length > 0){
                                	sbiModule_restServices.promisePost("2.0/datasets", response.data.id + "/dstags/" , angular.toJson(tagsHandlerService.prepareTagsForSending($scope.selectedDataSet.versNum + 1,$scope.tags))).then(function(response){
                                		var array = response.data;
                                		for(var i = 0; i < $scope.tags.length; i++){
                                			var nameForFiltering = $scope.tags[i].name;
                                			if(!$scope.tags[i].tagId){
                                				var newTag =  ($filter('filter')(array,{name:nameForFiltering},true))[0]
                                				$scope.tags[i].tagId = newTag.tagId;
                                				$scope.allTags.push(newTag);
                                }
                                		}
                                		$scope.filterByTags();
                                	});
                                }

                                if( $scope.showDatasetScheduler){
                                    if($scope.selectedDataSet.isScheduled) {
                                        sbiModule_restServices.promisePost('scheduleree/persistence/dataset/id',response.data.id, angular.toJson($scope.selectedDataSet))
                                        .then(

                                                function(responseDS) {
                                                    console.log("[POST]: SUCCESS!");
                                                    getDatasetFromId($scope, indexOfExistingDSInAT, response.data.id);
                                                    $scope.setFormNotDirty();
                                                },

                                                function(responseDS) {
                                                    sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.catalogues.toast.warning.schedulation"), 'Warning!');
                                                    $log.warn("An error occured while trying to save a dataset persistence schedulation");
                                                }
                                        );
                                    } else {
                                        sbiModule_restServices.promiseDelete('scheduleree/persistence/dataset/label', $scope.selectedDataSet.label, "/")
                                        .then(
                                                function(responseDS) {
                                                    console.log("[DELETE]: SUCCESS!");
                                                    getDatasetFromId($scope, indexOfExistingDSInAT,response.data.id);
                                                    $scope.setFormNotDirty();
                                                },

                                                function(responseDS) {
                                                    sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.catalogues.toast.warning.schedulation"), 'Warning!');
                                                    $log.warn("An error occured while trying to delete a dataset persistence schedulation");
                                                }
                                        );
                                    }
                                }else{
                                    console.log("[POST]: SUCCESS!");
                                    getDatasetFromId($scope, indexOfExistingDSInAT, response.data.id);
                                    $scope.setFormNotDirty();
                                }


                            },

                            function(response) {
                                sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
                            }
                        );
            }
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
		$scope.selectedTab = null;
	};

	$scope.uploadFile= function(){

    	multipartForm.post(sbiModule_config.contextName +"/restful-services/selfservicedatasetupload/fileupload",$scope.fileObj).success(

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
						$scope.selectedDataSet.dateFormat = $scope.dateFormatDefault;
						$scope.selectedDataSet.timestampFormat = $scope.timestampFormatDefault;
						$scope.selectedDataSet.csvQuote = $scope.csvQuoteDefault;
						$scope.selectedDataSet.skipRows = ($scope.skipRowsDefault != null) ? $scope.skipRowsDefault : null;
						$scope.selectedDataSet.limitRows = ($scope.limitRowsDefault !=null) ? $scope.limitRowsDefault : null;
						$scope.selectedDataSet.xslSheetNumber = ($scope.xslSheetNumberDefault != null) ? $scope.xslSheetNumberDefault : null;

						/**
						 * Whenever we upload a file, keep the track of its name, in order to indicate when the new one is browsed but not uploaded.
						 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						$scope.prevUploadedFile = $scope.selectedDataSet.fileName;
						$scope.selectedDataSet.fileUploaded=true;
						$scope.changingFile = false;
						$scope.initialUpload = true;

						if($scope.selectedDataSet.fileType=="XLS" || $scope.selectedDataSet.fileType=="XLSX"){
							$scope.selectedDataSet.limitRows = "";
							$scope.selectedDataSet.csvDelimiter = "";
							$scope.selectedDataSet.dateFormat = "";
							$scope.selectedDataSet.timestampFormat = "";
							$scope.selectedDataSet.csvQuote = "";
						}

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

		$scope.initialUpload = true;

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

	$scope.downloadUploadedFile=function(){

		console.info("DOWNLOAD FILE [IN]");

		var params = {};
		params.dsLabel = $scope.selectedDataSet.label;
		params.type = $scope.selectedDataSet.fileType;
		var requestParams = '?' + $httpParamSerializer(params);
		var config = {"responseType": "arraybuffer"};
		sbiModule_restServices.promiseGet('2.0/datasets', 'download/file' + requestParams, undefined, config)
			.then(function(response){
				var mimeType = response.headers("Content-type");
				var paramsString = response.headers("Content-Disposition");
				if (mimeType == 'application/octet-stream' || paramsString == null) {
					toastr.error('', sbiModule_translate.load("sbi.workspace.dataset.download.error"), $scope.toasterConfig);
				} else {
					var arrayParam = paramsString.split(';');
					var fileName = "";
					var fileType = "";
					var extensionFile = "";
					for (var i = 0; i< arrayParam.length; i++){
						var p = arrayParam[i].toLowerCase();
						if (p.includes("filename")){
							fileName = arrayParam[i].split("=")[1];
						}else if (p.includes("filetype")){
							fileType = arrayParam[i].split("=")[1];
						}else if (p.includes("extensionfile")){
							extensionFile = arrayParam[i].split("=")[1];
						}
					}
					if (fileName && fileName.endsWith("." + extensionFile)){
						fileName = fileName.split("." + extensionFile)[0];
					}
					sbiModule_download.getBlob(response.data, fileName, fileType, extensionFile);
				}
			}, function(response){
				toastr.error(response.data, sbiModule_translate.load("sbi.generic.error"), $scope.toasterConfig);
			});

		console.info("DOWNLOAD FILE [OUT]");

	}

	$scope.codemirrorLoaded = function(_editor){
		 $scope.codeMirror = _editor;
	 }

	  // The ui-codemirror option
	  $scope.cmOption = {
			  indentWithTabs: true,
			   smartIndent: true,
			   lineWrapping : true,
			   matchBrackets : true,
			   autofocus: true,
			   theme:"eclipse",
			   lineNumbers: true,
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
	   mode: 'text/x-sql',
	   lineWrapping : true,
	   indentWithTabs: true,
	   smartIndent: true,
	   matchBrackets : true,
	   theme:"eclipse",
	   lineNumbers: true
	 };

	 $scope.editorOptionsPython = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name: "python"}
	};

	 $scope.codemirrorSparqlOptions = {
		   mode: 'application/sparql-query',
		   lineWrapping : true,
		   indentWithTabs: true,
		   smartIndent: true,
		   matchBrackets : true,
		   theme:"eclipse",
		   lineNumbers: true
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
		 $scope.forceRefresh = false;
		   $mdDialog
		   .show({
		    scope : $scope,
		    preserveScope : true,
		    parent : angular.element(document.body),
		    controllerAs : 'openEditScriptDialogCtrl',
		    templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/EditDataSetScript.html',
		    clickOutsideToClose : false,
		    hasBackdrop : false
		   });

		   $timeout(function(){
			   if(angular.element(document).find('md-dialog').length > 0){
					 $scope.forceRefresh = true;
				 }
		   },1000)

	 }

	 $scope.openREnvironmentDialog = function () {
			sbiModule_restServices.promiseGet('2.0/backendservices/widgets/RWidget/libraries', JSON.parse($scope.selectedDataSet.pythonEnvironment).label)
			.then(function(response){
				$scope.libraries = [];
				var librariesArray = JSON.parse((response.data.result));
				for (idx in librariesArray) {
					lib = librariesArray[idx];
					name = lib[0];
					version = lib[1];
					$scope.libraries.push({"name": name, "version": version})
				}
	            $scope.forceRefresh = false;
				   $mdDialog
				   .show({
				    scope : $scope,
				    preserveScope : true,
				    parent : angular.element(document.body),
				    controllerAs : 'openPythonEnvironmentDialogCtrl',
				    templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/pythonEnvironment.html',
				    clickOutsideToClose : false,
				    hasBackdrop : false
				   });
			}, function(error){
			});

		   $timeout(function(){
			   if(angular.element(document).find('md-dialog').length > 0){
					 $scope.forceRefresh = true;
				 }
		   },1000)

	 }

	 $scope.openPythonEnvironmentDialog = function () {

		 sbiModule_restServices.promiseGet('2.0/backendservices/widgets/python/libraries', JSON.parse($scope.selectedDataSet.pythonEnvironment).label)
			.then(function(response) { //success
				$scope.libraries = JSON.parse(response.data.result);
	            $scope.forceRefresh = false;
				   $mdDialog
				   .show({
				    scope : $scope,
				    preserveScope : true,
				    parent : angular.element(document.body),
				    controllerAs : 'openPythonEnvironmentDialogCtrl',
				    templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/pythonEnvironment.html',
				    clickOutsideToClose : false,
				    hasBackdrop : false
				   });
			},function(response){ //failed
			});

		   $timeout(function(){
			   if(angular.element(document).find('md-dialog').length > 0){
					 $scope.forceRefresh = true;
				 }
		   },1000)

	 }

	 $scope.saveScript = function () {
		 $mdDialog.hide();
		 console.log("save")
	 }

	 $scope.closeScript = function (fromWhere) {

		 if (fromWhere=="fieldsMetadata") {
			 $scope.setFormNotDirty();
		 }

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

			if(typeof $scope.selectedDataSet.qbeJSONQuery === 'string'){
				$scope.selectedDataSet.qbeJSONQuery = JSON.stringify(JSON.parse($scope.selectedDataSet.qbeJSONQuery),null,2);
			} else {
			$scope.selectedDataSet.qbeJSONQuery = JSON.stringify($scope.selectedDataSet.qbeJSONQuery,null,2);
			}

			$mdDialog
			   .show({
				    scope : $scope,
				    preserveScope : true,
				    parent : angular.element(document.body),
				    controllerAs : 'datasetController',
				    templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/qbeQueryView.html',
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
			case "facetQuery":
				takeTheInfoHtmlContent(sbiModule_config.contextName + "/themes/sbi_default/html/facet-query.html");
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
							    templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/datasetRestParamsInfo.html',
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
	$scope.gridOptions = {
		enableColResize: true,
		enableSorting: false,
		enableFilter: false,
		pagination: false,
		suppressDragLeaveHidesColumns : true,
		headerHeight: 48,
		columnDefs: [],
		rowData: []
	};

	function DatasetPreviewController($scope,$mdDialog,$http,$sce) {
			if($scope.selectedDataSet && $scope.selectedDataSet.dsTypeCd != "File"){
				$scope.drivers = [];
				$scope.dataset = $scope.selectedDataSet;
				if($scope.dataset.drivers) {
					$scope.drivers = $scope.dataset.drivers;
					if($scope.drivers && $scope.drivers.length > 0) {
							for (var i=0; i<$scope.dataset.drivers.length; i++) {
								if($scope.dataset.drivers[i].selectionType=='TREE' && $scope.dataset.drivers[i].parameterValue && Array.isArray($scope.dataset.drivers[i].parameterValue)){
									parameterValue = $scope.dataset.drivers[i].parameterValue[0].value;
									$scope.dataset.drivers[i].parameterValue = [parameterValue];
								}
							}
						var driverValuesAreSet = driversExecutionService.driversAreSet($scope.drivers);
						if($scope.drivers.length > 0 && !driverValuesAreSet || $scope.selectedDataSet.pars.length > 0) {
							$scope.showDrivers = true;
						} else {
							$scope.showDrivers = false;
						}
						driversExecutionService.hasMandatoryDrivers($scope.drivers);
					}
				} else {
					$scope.showDrivers = $scope.selectedDataSet.pars.length > 0;
				}
				$scope.dataset.executed = !$scope.showDrivers;
				$scope.showFilterIcon = driversExecutionService.showFilterIcon;

				$scope.isExecuteParameterDisabled = function() {
					for(var i = 0; i < $scope.drivers.length; i++) {
						if($scope.drivers[i].mandatory && (typeof $scope.drivers[i].parameterValue === 'undefined' || $scope.drivers[i].parameterValue == '')){
							return true;
						}
					}
					return false;
				};

				$scope.toggleDrivers =function(){
					$scope.showDrivers = !$scope.showDrivers;
				}
			} else {
				if (!$scope.dataset) {
					// If we click on dataset for the first time
					$scope.dataset = {}
				}
				$scope.dataset.executed = true;
				$scope.drivers = [];
				$scope.showDrivers = false;
			}

		$scope.executeParameter = function(){
			$scope.showDrivers = false;
			$scope.dataset.executed = true;
			$scope.selectedDataSet["DRIVERS"] =  driversExecutionService.prepareDriversForSending($scope.drivers);
			sbiModule_restServices.promisePost('1.0/datasets','preview', angular.toJson($scope.selectedDataSet))
				.then(function(response){

					$scope.gridOptions.api.setColumnDefs(getColumns(response.data.metaData.fields));

					function getColumns(fields) {
						var columns = [];
						for(var f in fields){
							if(typeof fields[f] != 'object') continue;
							var tempCol = {"headerName":fields[f].header,"field":fields[f].name, "tooltipField":fields[f].name};
							// If there is a subtype, show that
							tempCol.headerComponentParams = {template: headerTemplate(fields[f].subtype || fields[f].type)};
							columns.push(tempCol);
						}
						return columns;
					}

					$scope.getPreviewSet(response.data);
				},
			function(response) {
				// Since the repsonse contains the error that is related to the Query syntax and/or content, close the parameters dialog
				$mdDialog.cancel();
				sbiModule_messaging.showErrorMessage($scope.translate.load(response.data.errors[0].message), 'Error');
			})
		}

		$scope.closeDatasetPreviewDialog=function(){
			$scope.previewDatasetModel=[];
			$scope.previewDatasetColumns=[];
			$scope.startPreviewIndex=0;
			$scope.endPreviewIndex=0;
			$scope.totalItemsInPreview=-1;	// modified by: danristo
			$scope.datasetInPreview=undefined;
			$scope.counter = 0;
			$scope.selectedDataSet.start = 0;
			$mdDialog.hide($scope.selectedDataSet);
		}

		// If drivers/params is not needed, show the data
		if (!$scope.showDrivers) {
			$scope.executeParameter();
		}
	}

	$scope.parametersPreviewColumns = [

       {
       	"label":$scope.translate.load("sbi.generic.name"),
       	"name":"name",
       	hideTooltip:true,
       	transformer: function() {
       		return '<md-input-container class="md-block" style="margin:0"><input ng-disabled=true ng-model="row.name"></md-input-container>';
       	}
   	},

   	{
		"label":$scope.translate.load("sbi.generic.value"),
		"name":"defaultValue",
		hideTooltip:true,

       	transformer: function() {
       		return '<md-input-container class="md-block" style="margin:0"><input placeholder="If not set, parameter will have default value." ng-model="row.value"></md-input-container>';
       	}
	}

   ];

	$scope.parameterHasValue = false;

	$scope.parametersAreSet = function(parameters) {
		  for(var i = 0; i < parameters.length; i++) {
			  if(parameters[i].value) {
				  $scope.parameterHasValue = true;
			  } else {
				  $scope.parameterHasValue = false;
			  }
		  }
		  return $scope.parameterHasValue;
	}

	$scope.buildFileDataSetMetaData=function(dataset){
		var toReturn = [];
		if (dataset && dataset.meta && dataset.meta.columns) {
			for (var i=0; i<dataset.meta.columns.length; i++) {
				var currMeta = dataset.meta.columns[i];
				var newMeta = {};
				newMeta['column'] = currMeta.column;
				newMeta['pname'] = currMeta.pname;
				newMeta['pvalue'] = currMeta.pvalue;
				toReturn[i] = newMeta;
			}
		}
		return toReturn;
	}

	$scope.continueToRestExecutionOfPreview = function() {

		$scope.disableBack = true;

		if ($scope.selectedDataSet.dsTypeCd.toLowerCase()=="rest" || $scope.selectedDataSet.dsTypeCd.toLowerCase()=="python/r" || $scope.selectedDataSet.dsTypeCd.toLowerCase()=="solr") {

			//----------------------
			// REQUEST HEADERS
			//----------------------
			var restRequestHeadersTemp = {};

			for (i=0; i<$scope.restRequestHeaders.length; i++) {
				restRequestHeadersTemp[$scope.restRequestHeaders[i]["name"]] = $scope.restRequestHeaders[i]["value"];
			}

			$scope.selectedDataSet.restRequestHeaders = angular.copy(JSON.stringify(restRequestHeadersTemp));


			if($scope.selectedDataSet.dsTypeCd.toLowerCase()=="solr"){
				//----------------------
				// REQUEST ADDITIONAL PARAMETERS
				//----------------------

				$scope.selectedDataSet.restRequestAdditionalParameters = $scope.restRequestAdditionalParameters;

			}


			//----------------------
			// JSON PATH ATTRIBUTES
			//----------------------
			var restJsonPathAttributesTemp = {};
			$scope.selectedDataSet.restJsonPathAttributes = angular.copy(JSON.stringify($scope.restJsonPathAttributes));

			if($scope.selectedDataSet.dsTypeCd.toLowerCase()=="python/r") {
    			$scope.selectedDataSet.restJsonPathItems = "$[*]";
    			$scope.selectedDataSet.restDirectlyJSONAttributes = true;
    			$scope.selectedDataSet.parameters = true;
    		}

		}

		/*
		 * Take the parameters and put it on the dataset. We have to do it before all the checks.
		 */
		$scope.selectedDataSet.pars = angular.copy($scope.parameterItems);

		if($scope.selectedDataSet.drivers && $scope.selectedDataSet.drivers.length > 0 && driversExecutionService.driversAreSet($scope.selectedDataSet.drivers)) {
			$scope.selectedDataSet["DRIVERS"] = driversExecutionService.prepareDriversForSending($scope.selectedDataSet.drivers);
		} else if($scope.selectedDataSet.drivers && $scope.selectedDataSet.drivers.length > 0 && !driversExecutionService.driversAreSet($scope.selectedDataSet.drivers) ||
				($scope.selectedDataSet.pars && $scope.selectedDataSet.pars.length > 0 && !$scope.parametersAreSet($scope.selectedDataSet.pars))) {
			$mdDialog.show({
					scope:$scope,
					preserveScope: true,
					controller: DatasetPreviewController,
					templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/workspace/templates/datasetPreviewDialogTemplate.html',
					clickOutsideToClose:false,
					escapeToClose :false
				});
		}

		$scope.selectedDataSet.fileDsMetadata = $scope.buildFileDataSetMetaData($scope.dataset);

		if(((!$scope.selectedDataSet.drivers || $scope.selectedDataSet.drivers.length == 0) && (!$scope.selectedDataSet.pars || $scope.selectedDataSet.pars.length == 0))
				|| ($scope.selectedDataSet.drivers && $scope.selectedDataSet.drivers.length > 0 && driversExecutionService.driversAreSet($scope.selectedDataSet.drivers))) {
			$mdDialog.show({
					scope:$scope,
					preserveScope: true,
					controller: DatasetPreviewController,
					templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/tools/workspace/templates/datasetPreviewDialogTemplate.html',
					clickOutsideToClose:false,
					escapeToClose :false
				});
		}

	}

    $scope.createColumnsForPreview=function(fields){
    	for(i=1;i<fields.length;i++){
    	 var column={};
    	 column.label=fields[i].header;
    	 column.name=fields[i].name;

    	 $scope.previewDatasetColumns.push(column);
    	}

    }

    function headerTemplate(type) {
		return 	'<div class="ag-cell-label-container data-type-'+type+'" role="presentation">'+
				'	 <span ref="eMenu" class="ag-header-icon ag-header-cell-menu-button"></span>'+
				'    <div ref="eLabel" class="ag-header-cell-label" role="presentation">'+
				'       <span ref="eText" class="ag-header-cell-text" role="columnheader"></span>'+
				'       <span ref="eFilter" class="ag-header-icon ag-filter-icon"></span>'+
				'       <span ref="eSortOrder" class="ag-header-icon ag-sort-order" ></span>'+
				'    	<span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon" ></span>'+
				'   	<span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon" ></span>'+
				'  		<span ref="eSortNone" class="ag-header-icon ag-sort-none-icon" ></span>'+
				'		<span class="ag-cell-type">'+type+'</span>'+
				'	</div>'+
				'</div>';
	}

    $scope.getPreviewSet = function(data){
    	if(data!=null){
    		$scope.newDatasetMeta = data;
    	}
    	$scope.paginationDisabled = true;
		$scope.previewPaginationEnabled = true;
		var totalItemsInPreviewInit = angular.copy($scope.totalItemsInPreview);
		$scope.totalItemsInPreview = $scope.newDatasetMeta.results;
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
	    angular.copy($scope.newDatasetMeta.rows,$scope.previewDatasetModel);
	    if($scope.gridOptions && $scope.gridOptions.api) {
	    	$scope.gridOptions.api.setRowData($scope.previewDatasetModel);
	    }
	    if( !$scope.previewDatasetColumns || $scope.previewDatasetColumns.length==0){
	    	$scope.createColumnsForPreview($scope.newDatasetMeta.metaData.fields);
	    }
    }

    $scope.getNextPreviewSet = function(){
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
    	 $scope.selectedDataSet.start = $scope.startPreviewIndex;
     	 $scope.selectedDataSet.limit = $scope.itemsPerPage;
     	 sbiModule_restServices.promisePost('1.0/datasets','preview', angular.toJson($scope.selectedDataSet))
		 .then(
			function(response) {
				console.log(response.data);
				$scope.newDatasetMeta = response.data;
				$scope.getPreviewSet(null);
			},
			function(response) {
				$scope.translate.load(response.data.errors.messages)
			}
		 );
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
    	 $scope.selectedDataSet.start = $scope.startPreviewIndex;
     	 $scope.selectedDataSet.limit = $scope.itemsPerPage;
     	 sbiModule_restServices.promisePost('1.0/datasets','preview', angular.toJson($scope.selectedDataSet))
		 .then(
			function(response) {
				console.log(response.data);
				$scope.newDatasetMeta = response.data;
				$scope.getPreviewSet(null);
			},
			function(response) {
				$scope.translate.load(response.data.errors.messages)
			}
		 );

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

    	if ($scope.restRequestHeaders.length>0) {

    		// TODO: translate
        	var confirm = $mdDialog.confirm()
    	         .title("Clear all REST request headers")
    	         .targetEvent(event)
    	         .textContent("Are you sure you want to delete all REST request headers")
    	         .ariaLabel("Clear all REST request headers")
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
    	else {

    		$mdDialog
			.show(
					$mdDialog.alert()
				        .clickOutsideToClose(true)
				        .title('Dataset has no REST request headers to delete')
				        .textContent('There are not REST request headers to delete for the selected dataset')
				        .ariaLabel('Dataset has no REST request headers to delete')
				        .ok('Ok')
			    );

    	}

    }

    $scope.deleteAllRESTRequestAdditionalParameters = function() {

    	if ($scope.restRequestAdditionalParameters.length>0) {

    		// TODO: translate
        	var confirm = $mdDialog.confirm()
    	         .title("Clear all REST request headers")
    	         .targetEvent(event)
    	         .textContent("Are you sure you want to delete all REST request headers")
    	         .ariaLabel("Clear all REST request headers")
    	         .ok($scope.translate.load("sbi.general.yes"))
    	         .cancel($scope.translate.load("sbi.general.No"));

    		$mdDialog
    			.show(confirm)
    			.then(
    					function() {
    						$scope.setFormDirty();
    						$scope.restRequestAdditionalParameters = [];
    						$scope.restDsRequestAdditionalParameterTableLastPage = 1;
    			 		}
    				);

    	}
    	else {

    		$mdDialog
			.show(
					$mdDialog.alert()
				        .clickOutsideToClose(true)
				        .title('Dataset has no REST request headers to delete')
				        .textContent('There are not REST request headers to delete for the selected dataset')
				        .ariaLabel('Dataset has no REST request headers to delete')
				        .ok('Ok')
			    );

    	}

    }



    $scope.deleteAllRESTJsonPathAttributes = function() {

    	if ($scope.restJsonPathAttributes.length>0) {

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
    	else {

    		$mdDialog
				.show(
						$mdDialog.alert()
					        .clickOutsideToClose(true)
					        .title('Dataset has no REST JSON path attributes to delete')
					        .textContent('There are not REST JSON path attributes to delete for the selected dataset')
					        .ariaLabel('Dataset has no REST JSON path attributes to delete')
					        .ok('Ok')
				    );

    	}

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
    	$scope.isFromSaveNoMetadata = true;
    	$scope.saveDataset();
    }

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
							    templateUrl : sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/helpDataSet.html',
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

    	if ($scope.selectedDataSet.id) {

    		$scope.fieldsMetadata = [];

    		// If there are metadata...
    		if ($scope.selectedDataSet.meta) {
    			$scope.fieldsMetadata = exctractFieldsMetadata($scope.selectedDataSet.meta.columns, $scope.selectedDataSet.dsTypeCd);
    		}

        	$mdDialog
    		   .show({
    		    scope : $scope,
    		    preserveScope : true,
    		    parent : angular.element(document.body),
    		    templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/fieldsMetadata.html',
    		    clickOutsideToClose : false,
    		    hasBackdrop : false
    		   });

    	}
    	else {

    		$mdDialog.show(
				$mdDialog.alert()
			        .clickOutsideToClose(true)
			        .title('Cannot open fields metadata for not saved dataset')
			        .textContent("You cannot open fields metadata for the dataset that is not saved")
			        .ariaLabel('Cannot open fields metadata for not saved dataset')
			        .ok('Ok')
			);

    	}

    }

    $scope.openAvaliableProfileAttributes = function () {

    	$scope.getProfileAttributes();

    	$mdDialog
		   .show({
		    scope : $scope,
		    preserveScope : true,
		    parent : angular.element(document.body),
		    templateUrl : sbiModule_config.dynamicResourcesBasePath +'/angular_1.4/tools/catalogues/templates/avaliableProfileAttributes.html',
		    clickOutsideToClose : false,
		    hasBackdrop : false
		   });
    }

    $scope.openLinkDataset = function () {
   	 $mdDialog.show({
		  scope:$scope,
		  preserveScope: true,
	      controller: DialogLinkDataSetController,
	      templateUrl: sbiModule_config.contextName +"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/dataset/LinkDatasetIFrame.jsp",
	      clickOutsideToClose:true,
	      escapeToClose :true,
	      fullscreen: true
	    })
    	//document.location.href = sbiModule_config.contextName + "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/dataset/linkDataset.jsp&id="+$scope.selectedDataSet.id+"&label="+$scope.selectedDataSet.label;
    }

    function DialogLinkDataSetController($scope,$mdDialog,sbiModule_config){
    	$scope.iframeUrl = sbiModule_config.contextName + "/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/dataset/linkDataset.jsp&id="+$scope.selectedDataSet.id+"&label="+$scope.selectedDataSet.label;
		$scope.cancelDialog = function() {

			$mdDialog.cancel();
		}
    }

    $scope.resetWhenChangeDSType = function(dsType) {

    	if (dsType.toLowerCase()=="file") {

    		for (var key in $scope.fileObj) {

        		if ($scope.fileObj.hasOwnProperty(key)) {
        			$scope.fileObj[key] = "";
        		}
    		}
    	}
    	else if (dsType.toLowerCase()=="rest" || dsType.toLowerCase()=="python/r" || dsType.toLowerCase()=="solr") {
    		$scope.restRequestHeaders = [];
    		$scope.restRequestAdditionalParameters = [];
    		$scope.restJsonPathAttributes = [];
    		if(dsType.toLowerCase()=="solr"){
    			$scope.selectedDataSet.restRequestBody="*:*";
    			$scope.selectedDataSet.restOffset =  "";
    			$scope.selectedDataSet.restFetchSize=  "";
    			$scope.selectedDataSet.restMaxResults=  "";
    		}else{
    			$scope.selectedDataSet.restRequestBody="";
    		}
    		$scope.selectedDataSet.pythonDatasetType = "python";
    	}

    	$scope.parameterItems = [];

    }

    $scope.showQbeDataset= function(dataset){
    	  var url = null;
    	     if(dataset.dsTypeCd=='Federated'){
    	      url = datasetParameters.qbeEditFederatedDataSetServiceUrl
    	         +'&FEDERATION_ID='+dataset.federationId
    	         +'&DATA_SOURCE_ID='+ dataset.qbeDataSourceId;
    	     } else {
    	      var modelName= dataset.qbeDatamarts;
    	   var dataSource=dataset.qbeDataSource;
    	      url = datasetParameters.buildQbeDataSetServiceUrl
    	           +'&DATAMART_NAME='+modelName
    	           +'&DATASOURCE_LABEL='+ dataSource
    	           +'&DATA_SOURCE_ID='+ dataset.qbeDataSourceId;
    	     }

    	  //url = "http://localhost:8080/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=BUILD_QBE_DATASET_START_ACTION&user_id=biadmin&NEW_SESSION=TRUE&SBI_LANGUAGE=en&SBI_COUNTRY=US&DATASOURCE_LABEL=foodmart&DATAMART_NAME=foodmart";
    	  // $window.location.href=url;
    	  $scope.isFromDataSetCatalogue = true;
    	  $qbeViewer.openQbeInterfaceDSet($scope, true, url,false,$scope.selectedDataSet);

    	    }

    $scope.transformationCheck = function() {
    	$scope.selectedDataSet.trasfTypeCd = !$scope.transformDatasetState ? "" : $scope.transformationDataset.VALUE_CD;
    }

    function getDatasetFromId($scope,index,id){
    	sbiModule_restServices.promiseGet('1.0/datasets/dataset/id',id)
		.then(
				function(responseDS) {

					var savedDataset = responseDS.data[0];
					$scope.selectedDataSet = angular.copy(savedDataset);

					$scope.getDatasetParametersFromBusinessModel($scope.selectedDataSet);

					$scope.datasetsListTemp[index] = angular.copy($scope.selectedDataSet);
					$scope.datasetsListPersisted = angular.copy($scope.datasetsListTemp);
					$scope.selectedDataSetInit = angular.copy($scope.selectedDataSet);
					$scope.isCategoryRequired = false;

					/*
					 * This seems a workaround for something i don't know.
					 * I comment it hoping to see what it fixed.
					 */
					// if($scope.isFromSaveNoMetadata == true) {
					// 	$scope.selectedDataSet.isFromSaveNoMetadata = false;
					// 	$scope.isFromSaveNoMetadata = false;
					// 	$scope.saveDataset();
					//}

					// SCHEDULING
					if ($scope.selectedDataSet.isScheduled) {
						$scope.selectedDataSet.startDate = new Date($scope.selectedDataSet.startDate);
						console.log($scope.selectedDataSet);

						if (!$scope.selectedDataSet.endDate) {
							$scope.selectedDataSet.endDate = null;
						}
						else {
							$scope.selectedDataSet.endDate = new Date($scope.selectedDataSet.endDate);
						}

						// Deparse the CRON from the response (since we do not need seconds from it)
						var splitCron = $scope.selectedDataSet.schedulingCronLine.split(" ");
						var cronNoSeconds = "";
						var selectedMinutesCronString = splitCron[1]!="*" ? splitCron[1] : null;
						var selectedHoursCronString = splitCron[2]!="*" ? splitCron[2] : null;
						var selectedDaysCronString = splitCron[3]!="*" ? splitCron[3] : null;
						var selectedMonthsCronString = splitCron[4]!="*" ? splitCron[4] : null;
						var selectedWeekdaysCronString = splitCron[5]!="*" && splitCron[5]!="?" ? splitCron[5] : null;

						for (i=1; i<splitCron.length; i++) {
							cronNoSeconds += splitCron[i] + " ";
						}

						$scope.selectedDataSet.schedulingCronLine = cronNoSeconds;

						$scope.scheduling.cronDescriptionDate = prettyCron.toString($scope.selectedDataSet.schedulingCronLine);
						$scope.scheduling.cronDescriptionTime = prettyCron.getNext($scope.selectedDataSet.schedulingCronLine);

						// =====================
						// Comboboxes
						// =====================

						// MINUTES

						var splitMinutes = new Array();

						if (selectedMinutesCronString!=null) {

							var minutesTemp = selectedMinutesCronString.split(",");

							for (i=0; i<minutesTemp.length; i++) {
								splitMinutes.push(minutesTemp[i]);
							}

							$scope.scheduling.minutesSelected = splitMinutes;
							$scope.scheduling.minutesCustom = true;

						}
						else {
							$scope.scheduling.minutesSelected = [];
							$scope.scheduling.minutesCustom = false;
						}

						// HOURS

						var splitHours = new Array();

						if (selectedHoursCronString!=null) {

							var hoursTemp = selectedHoursCronString.split(",");

							for (i=0; i<hoursTemp.length; i++) {
								splitHours.push(hoursTemp[i]);
							}

							$scope.scheduling.hoursSelected = splitHours;
							$scope.scheduling.hoursCustom = true;

						}
						else {
							$scope.scheduling.hoursSelected = [];
							$scope.scheduling.hoursCustom = false;
						}

						// DAYS

						var splitDays = new Array();

						if (selectedDaysCronString!=null) {

							var daysTemp = selectedDaysCronString.split(",");

							for (i=0; i<daysTemp.length; i++) {
								splitDays.push(daysTemp[i]);
							}

							$scope.scheduling.daysSelected = splitDays;
							$scope.scheduling.daysCustom = true;

						}
						else {
							$scope.scheduling.daysSelected = [];
							$scope.scheduling.daysCustom = false;
						}

						// MONTHS

						var splitMonths = new Array();

						if (selectedMonthsCronString!=null) {

							var monthsTemp = selectedMonthsCronString.split(",");

							for (i=0; i<monthsTemp.length; i++) {
								splitMonths.push(monthsTemp[i]);
							}

							$scope.scheduling.monthsSelected = splitMonths;
							$scope.scheduling.monthsCustom = true;

						}
						else {
							$scope.scheduling.monthsSelected = [];
							$scope.scheduling.monthsCustom = false;
						}

						// WEEKDAYS

						var splitWeekdays = new Array();

						if (selectedWeekdaysCronString!=null) {

							var weekdaysTemp = selectedWeekdaysCronString.split(",");

							for (i=0; i<weekdaysTemp.length; i++) {
								splitWeekdays.push(weekdaysTemp[i]);
							}

							$scope.scheduling.weekdaysSelected = splitWeekdays;
							$scope.scheduling.weekdaysCustom = true;

						}
						else {
							$scope.scheduling.weekdaysSelected = [];
							$scope.scheduling.weekdaysCustom = false;
						}
					}

				},

				function(responseDS) {
					$log.warn("ERROR");
				}
			);
    }

	$scope.changeStep = function(way) {
		var oldStep = $scope.step;
		var tempStep = angular.copy($scope.step);
		if(way=='back' && way!=undefined) {
			tempStep = tempStep-1;
		} else if(way=='next') {
			tempStep = tempStep+1;
		}
		switch(tempStep) {
		case 1:
			$scope.step=1;
			break;
		case 2:
			console.log('step 2');
			if(way=='validate') {
				$scope.toStep3();
			} else {
				if((oldStep==1 && $scope.changingFile)||(oldStep==1 && $scope.initialUpload) || (oldStep==1&&$scope.isSelected)){
					loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_GEN_META_PROPERTY");
					loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_PROPERTY");
					loadDatasetValues("domainsforfinaluser/listValueDescriptionByType","?DOMAIN_TYPE=DS_META_VALUE");

				}
				$scope.toStep2();
			}

			break;
		default:
			break;
		}
	}

	$scope.toStep2 = function () {
		if($scope.changingFile || $scope.initialUpload) {
			var params = {};
			params.showDerivedDataset=false;
			params.SBI_EXECUTION_ID = -1;
			params.isTech = false;
			params.showOnlyOwner=true;

			$scope.dataset = {};
			$scope.dataset.catTypeId = $scope.selectedDataSet.catTypeId;
			$scope.dataset.catTypeVn = $scope.selectedDataSet.catTypeVn;
			if($scope.selectedDataSet.csvEncoding=="" || $scope.selectedDataSet.csvEncoding==undefined) {
				$scope.selectedDataSet.csvEncoding = "UTF-8";
			}
			$scope.dataset.csvEncoding = $scope.selectedDataSet.csvEncoding;
			$scope.dataset.csvDelimiter = $scope.selectedDataSet.csvDelimiter;
			$scope.dataset.csvQuote = $scope.selectedDataSet.csvQuote;
			$scope.dataset.dateFormat = $scope.selectedDataSet.dateFormat;
			$scope.dataset.timestampFormat = $scope.selectedDataSet.timestampFormat;
			$scope.dataset.description = $scope.selectedDataSet.description;
			$scope.dataset.exportToHdfs = $scope.selectedDataSet.isPersistedHDFS;
			$scope.dataset.fileName = $scope.selectedDataSet.fileName;
			$scope.dataset.fileType = $scope.selectedDataSet.fileType;
			$scope.dataset.fileUploaded = $scope.selectedDataSet.fileUploaded;
			$scope.dataset.id = $scope.selectedDataSet.id ? $scope.selectedDataSet.id : "";
			$scope.dataset.label = $scope.selectedDataSet.label;
			$scope.dataset.limitRows = $scope.selectedDataSet.limitRows;
			$scope.dataset.meta = JSON.stringify($scope.selectedDataSet.meta);
			$scope.dataset.name = $scope.selectedDataSet.name;
			$scope.dataset.persist = $scope.selectedDataSet.isPersisted;
			$scope.dataset.skipRows = $scope.selectedDataSet.skipRows;
			$scope.dataset.tableName = $scope.selectedDataSet.persistTableName;
			$scope.dataset.type = $scope.selectedDataSet.dsTypeCd;
			$scope.dataset.xslSheetNumber = $scope.selectedDataSet.xslSheetNumber;

			$http(
				{
					method: 'POST',
					url: sbiModule_config.contextName + '/restful-services/selfservicedataset/testDataSet',
					data: $scope.dataset,
					params: params,
					headers: {'Content-Type': 'application/x-www-form-urlencoded'},

					transformRequest: function(obj) {

						var str = [];

						for(var p in obj)
							str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));

						return str.join("&");

					},
				}
			)
			.then(
				function successCallback(response) {

					if (!response.data.errors) {

						$scope.step = 2;

						$scope.dataset.meta = {};
						$scope.dataset.meta = angular.copy(response.data.meta);
						angular.copy(response.data.datasetColumns,$scope.datasetColumns);

						$scope.validationPassed = false;
						$scope.prepareMetaForView();
						$scope.prepareDatasetForView();

						if($scope.initialUpload){
							$scope.initialUpload = false;
						}


					} else {
						console.info("[ERROR]: ",$scope.translate.load(response.data.errors[0].message));
						// Reset the meta after first unsuccessful try to go to Step 2
						$scope.dataset.meta = [];

						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					}

				},
				function errorCallback(response) {

				});
		} else {
			$scope.step = 2;

			$scope.validationPassed = false;
			$scope.prepareMetaForView();
			$scope.prepareDatasetForView();

			if($scope.initialUpload){
				$scope.initialUpload = false;
			}

		}
	}

    $scope.tableColumns =
        [
    	     {
    	      name:"columnView",
    	      label:"Column",
    	      hideTooltip:true
    	     },

    	     {
    	         name:"pnameView",
    	         label:"Attribute",
    	         hideTooltip:true
    	     },

    	     {
    	         name:"pvalueView",
    	         label:"Value",
    	         hideTooltip:true
    	     },

    	     /**
    	      * A new column on the Step 2 of the Dataset wizard. It contains a graphic description of a validation state
    	      * for all metadata column separately.
    	      * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    	      */
    	     {
    	    	 name: "metaValid",
    	    	 label: "Valid",
    	    	 hideTooltip: true
    	     }
         ];

    $scope.metaScopeFunctions={
        	translate: sbiModule_translate,
        	datasetColumns:$scope.datasetColumns,
        	dsMetaProperty:$scope.dsMetaProperty,
        	dsMetaValue   :$scope.dsMetaValue,
        	filterMetaValues: function(value,row){
        		console.log(row);
        		row.dsMetaValue=[];
        		if(value.toLowerCase()==="type".toLowerCase()){
        			for(i=0;i<this.dsMetaValue.length;i++){
        			 if(this.dsMetaValue[i].VALUE_CD.toLowerCase()==="string".toLowerCase()||
        			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="double".toLowerCase()||
        			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="long".toLowerCase()||
        			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="integer".toLowerCase()||
        			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="date".toLowerCase() ||
        			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="timestamp".toLowerCase())
        				 row.dsMetaValue.push(this.dsMetaValue[i]);

        			}

        		}else if(value.toLowerCase()==="fieldType".toLowerCase()){
        			for(i=0;i<this.dsMetaValue.length;i++){
           			 if(this.dsMetaValue[i].VALUE_CD.toLowerCase()==="attribute".toLowerCase()|| this.dsMetaValue[i].VALUE_CD.toLowerCase()==="spatial_attribute".toLowerCase()||
           			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="measure".toLowerCase())
           				 row.dsMetaValue.push(this.dsMetaValue[i]);

           			}

        		}else{

        			angular.copy(this.dsMetaValue,row.dsMetaValue);
        		}

        	}
        };

	loadDatasetValues = function(a,b) {

		sbiModule_restServices.promiseGet(a,b)
		.then(function(response) {

			if(b=="?DOMAIN_TYPE=DS_GEN_META_PROPERTY"){
				angular.copy(response.data,$scope.dsGenMetaProperty)
			} else if(b=="?DOMAIN_TYPE=DS_META_PROPERTY"){
				angular.copy(response.data,$scope.dsMetaProperty)
			} else if(b=="?DOMAIN_TYPE=DS_META_VALUE"){
				angular.copy(response.data,$scope.dsMetaValue)
			}
		},function(response){

			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

    $scope.toStep3 = function () {


    	$scope.dataset.isPublicDS = false;
		$scope.dataset.datasetMetadata={};
		$scope.dataset.datasetMetadata.version=1;
		$scope.dataset.datasetMetadata.dataset=[];
		$scope.dataset.datasetMetadata.columns=[];
		angular.copy($scope.dataset.meta.dataset,$scope.dataset.datasetMetadata.dataset);
		angular.copy($scope.dataset.meta.columns,$scope.dataset.datasetMetadata.columns);

		c=$scope.dataset.datasetMetadata.columns;

		for (var i = 0; i < c.length; i++) {
			delete c[i].columnView;
			delete c[i].pvalueView;
			delete c[i].pnameView;
			delete c[i].dsMetaValue;
		}

		d=$scope.dataset.datasetMetadata.dataset;

		for (var i = 0; i < d.length; i++) {
			delete d[i].pvalueView;
			delete d[i].pnameView;
		}

		$scope.dataset.datasetMetadata = JSON.stringify($scope.dataset.datasetMetadata);
		$scope.dataset.limitPreview = true;

		$scope.dataset.page = 1;
		$scope.dataset.start = "";
		$scope.dataset.page = 10;


    	var params = {};
		params.SBI_EXECUTION_ID = -1;

		$http(
			{
				method: 'POST',
				url: sbiModule_config.contextName + '/restful-services/selfservicedataset/getDataStore',
				data: $scope.dataset,
				params: params,
				headers: {'Content-Type': 'application/x-www-form-urlencoded'},

				transformRequest: function(obj) {

					var str = [];

					for(var p in obj)
						str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));

					return str.join("&");

				},
			}
		)
		.then(
			function successCallback(response) {

				if (!response.data.validationErrors) {

					//$scope.step=3;
					$scope.validated = true;

					sbiModule_messaging.showSuccessMessage($scope.translate.load('sbi.workspace.dataset.wizard.metadata.validation.success.msg'));

					$scope.resultMetaDataStep2 = [];
					$scope.resultRowsStep2 = [];
					angular.copy(response.data.metaData.fields,$scope.resultMetaDataStep2);

					// Take all results (pure data) for rows of the Angular table
					angular.copy(response.data.rows,$scope.resultRowsStep2);
					$scope.collectHeadersForStep3Preview();

					//validation properties when validation is successful
					$scope.step2ValidationErrors = null;
					$scope.validationStatus = true;

					$scope.validationPassed = true;
					$scope.csvConfChanged = false;
					$scope.validationError = false;

					$scope.prepareMetaForView();

				} else if(response.data.validationErrors) {

					console.warn("[VALIDATION ERRORS]: Validation failed!");

					$scope.validationStatus = false;

//					($scope.step2ValidationFirstTime == true) ? $scope.step2ValidationFirstTime = false : null;

					/**
					 * Now, since we are having validation errors when submitting the Step 3, change the value of the
					 * scope variable that contains these data.
					 */
					$scope.step2ValidationErrors = response.data.validationErrors;

					/**
					 * Since the validation of the Step 2 went wrong (metadata are not valid), set flags that indicate that
					 * the validation did not passed, that the CSV configration parameters are not changed (anymore) and that
					 * there were validation errors.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					$scope.validationPassed = false;
					$scope.csvConfChanged = false;
					$scope.validationError = true;

					// "Refresh" the Step 2 table, in order to where the validation error appears.
					$scope.prepareMetaForView();
					$scope.prepareDatasetForView();

				} else {
					console.info("[ERROR]: ",response.data.errors[0].message);
					// Reset the meta after first unsuccessful try to go to Step 2
					//$scope.dataset.meta = [];

					//sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				}

			},
			function errorCallback(response) {

			});
    }

    $scope.collectHeadersForStep3Preview = function() {

		$scope.allHeadersForStep3Preview = [];

		for (i=0; i<$scope.resultMetaDataStep2.length; i++) {

			var temp = {};

			temp['label'] = $scope.resultMetaDataStep2[i].header;
			temp['name'] = 'column_' + (i+1);

			$scope.allHeadersForStep3Preview.push(temp);

		}

	}

	$scope.prepareMetaForView = function(item,index){

		/**
		 * If the user just opens the Dataset wizard dialog and goes to the Step 2, the grid will be initialized with the saved (when updating/editing) or with the
		 * default (when creating a new File dataset) data. In that situation, the 'item' and 'index' will be undefined. These two values are defined only when user
		 * clicks on the Value column comboboxes for Field type of the particular column. They tell us the type of the Field type (ATTRIBUTE or MEASURE). So, this
		 * variable will be true only when just opening (entering) the Step 2.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var initialization = !item && !index;

		$scope.prepareMetaValue($scope.dataset.meta.columns,item,index);

		/**
		 * Overwrite the existing metadata (the array of all rows for the metadata on the Step 2 of the Dataset wizard)
		 * with the filtered array - the one that passes only the 'type' and 'fieldType' rows for each column in the
		 * file dataset. Other will be filtered (they will not pass).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.dataset.meta.columns = filterMetadataRows($scope.dataset.meta.columns);

		for(i=0; i< $scope.dataset.meta.columns.length;i++) {

			loc = $scope.dataset.meta.columns[i];

			var pname = loc.pname;

			loc.dsMetaValue=[];

			/**
			 * If initializing (entering) the Step 2, the expression (pname=="type") will indicate that we are dealing with the Type type of the Attribute column
			 * (possible values of these combo boxes: String, Integer, Double). In that case, inspect the subsequent Field type type (ATTRIBUTE or MEASURE) and in
			 * the case it is a MEASURE, remove the String item from the current Type combobox, since the MEASURE can be only Integer/Double. Otherwise, if the
			 * Attribute column value is the Field type, just proceed with the filtering of metadata.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if (pname=="type" || pname=="fieldType") {
				if (initialization) {

					if (pname=="type") {
						loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined,$scope.dataset.meta.columns[i+1].pvalue);
					}
					else {
						loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined);
					}

				}
				// Click
				else  {

					if (index-1==i) {
						loc.dsMetaValue = $scope.filterMetaValue($scope.dataset.meta.columns[index-1].pname,item,i,index);
					}
					else {

						if (pname=="type")
							loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined,$scope.dataset.meta.columns[i+1].pvalue);
						else
							loc.dsMetaValue = $scope.filterMetaValue(pname,undefined,i,undefined);

					}
				}

				/**
				 * danristo
				 */
				loc.indexOfRow = i;

				/**
				 * If user selects the MEASURE field type after having a field type of ATTRIBUTE and type String for a particular data column, the first
				 * item in the type combobox for a MEASURE field type will be selected (e.g. the Integer will be selected). This is implemented instead of
				 * having an empty combo for type when performing this scenario.
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				if (i%2==0 && loc.dsMetaValue && item) {

					if (item.toUpperCase()=="MEASURE" && loc.pvalue.toLowerCase()=="string") {
						loc.pvalue = loc.dsMetaValue[0].VALUE_CD;
					}

				}

				/**
				 * Change the GUI element type for first two columns of the Step 2 of the Dataset wizard, from combo box ('md-select') to the label (fixed value).
				 * This is done as a temporary solution - besides this, these things are removed: 'Columns/Dataset' combo box, 'Add new row' button, 'Clear all'
				 * button, delete row item in each row. The only dynamic behavior has the 'Value' column, since we can choose between Integer and Double for MEASURES
				 * and an additional String option for ATTRIBUTES, so for this reason we are keeping the combo box element.
				 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
//					loc.columnView='<md-select aria-label="column-view" ng-model=row.column class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumns" value="{{col.columnName}}">{{col.columnName}}</md-option></md-select>';
//					loc.pnameView='<md-select aria-label="pname-view" ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsMetaProperty" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.filterMetaValues(col.VALUE_CD,row)">{{col.VALUE_NM}}</md-option></md-select>';

				loc.columnView='<label>{{row.column}}</label>';
				loc.pnameView='<label>{{row.pname}}</label>';
				loc.pvalueView='<md-select aria-label="pvalue-view" ng-model=row.pvalue class="noMargin" style=styleString><md-option ng-repeat="col in row.dsMetaValue" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.valueChanged(col,row.indexOfRow)">{{col.VALUE_NM}}</md-option></md-select>';

				var msg = '';

				/**
				 * Manage the Step 2 "Valid" column state according to the validation after submitting the Step 2.
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				if (($scope.validationPassed==true || $scope.validationError==true) && $scope.csvConfChanged==false) {

					var columnName = loc.column;

					var invalidColumns = $filter('filter')($scope.step2ValidationErrors, {columnName: columnName}, true);

					var invalidType = false;

					if (invalidColumns != null && invalidColumns.length > 0 && invalidColumns[0]['column_' + i] != undefined) {
						msg = invalidColumns[0]['column_' + i];
						invalidType = true;
						loc.columnErrorDetails = {
							errors: invalidColumns,
							skipRows: $scope.dataset.skipRows,
							index: i
						};
					} else {
						msg = "sbi.workspace.dataset.wizard.metadata.validation.success.title";
					}

					var invalidColumnValidContent = '<md-content class="metadataValidationColumn metadataInvalidColumn" ng-click="scopeFunctions.showErrorDetails(row.columnErrorDetails)"><div><div class="leftInavlidIcon"><md-icon md-font-icon="fa fa-times fa-1x" class="invalidTypeMetadata" title="' + eval("sbiModule_translate.load(msg)") + '" style="float:right;"></div></md-icon><div class="rightInvalidIcon"><md-icon md-font-icon="fa fa-info fa-1x" class="invalidTypeMetadata"></md-icon></div></div></md-content>';
					var validColumnValidContent = '<md-content class="metadataValidationColumn metadataValidColumn"><md-icon md-font-icon="fa fa-check fa-1x" class="validTypeMetadata" title="' + eval("sbiModule_translate.load(msg)") + '"></md-icon></md-content>';

					// Set the content of the "Valid" column for the current row to an appropriate state (passed/failed validation).
					loc.metaValid = (invalidType) ? invalidColumnValidContent : validColumnValidContent;

				}
				else {

					msg = "sbi.workspace.dataset.wizard.metadata.validation.pending.title";

					// Set the state of the Step 2 "Valid" column to the initial value - pending for the validation (default state).
					loc.metaValid = '<md-content class="metadataValidationColumn metadataDefaultColumn"><md-icon md-font-icon="fa fa-question fa-1x" class="defaultStateValidType" title="' + eval("sbiModule_translate.load(msg)") + '"></md-icon></md-content>';

				}
			}

		}
	}

	$scope.prepareDatasetForView = function() {

		var datasets = $scope.dataset.meta.dataset;

		for (var i = 0; i < datasets.length; i++) {
			datasets[i].pnameView = '<md-select aria-label="dspname-view" ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsGenMetaProperty" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>';
			datasets[i].pvalueView = '<input ng-model="row.pvalue"></input>';
		}

	}

	$scope.prepareMetaValue=function(values){

		for(i=0;i<values.length;i++){

			if (values[i].pname.toUpperCase() == 'type'.toUpperCase()){
				values[i].pname=values[i].pname.toLowerCase();
				typeValue = values[i].pvalue;
				typeValue = typeValue!=null ? typeValue.replace("java.lang.","") : null;
				typeValue = typeValue!=null ? typeValue.replace("java.util.","") : null;
				typeValue = typeValue!=null ? typeValue.replace("java.sql.","") : null;
				values[i].pvalue = typeValue;
			}
		}
	}

	$scope.metaScopeFunctions.showErrorDetails = function(columnErrorDetails) {

		$mdSidenav('errors-columndetails-sidenav')
			.open()
			.then(function(){

				$scope.columnErrorDetails = columnErrorDetails;
				$scope.columnString = 'column_';
				$scope.index = $scope.columnErrorDetails.index;
		    	$scope.invalidColumn = $scope.columnErrorDetails.errors[0].columnName;
		    	$scope.limit = 10;
		    	$scope.errorsCount = $scope.columnErrorDetails.errors.length;

		    	$scope.showMoreErrorsButton = function() {
		    		return $scope.errorsCount > $scope.limit;
		    	}

		    	$scope.remainingErros = function() {
		    		return $scope.errorsCount - $scope.limit;
		    	}

		    	$scope.extandErrorList = function() {
		    		if($scope.showMoreErrorsButton()) {
		    			$scope.limit += $scope.limit;
		    		} else {
		    			$scope.limit = $scope.remainingErros();
		    		}
		    	}
			});
	}

	$scope.closeErrorDetails = function() {
		$mdSidenav('errors-columndetails-sidenav')
			.close()
			.then(function(){
				$scope.columnErrorDetails = {};
			});
	}

	/**
	 * Local function that is used for filtering rows (metadata) for all columns available in the file dataset.
	 * It will pass only the 'type' and 'fieldType' rows, whilst others will be ignored (filtered).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	var filterMetadataRows = function() {

		/**
		 * The final (filtered) array of all rows in the Step 2
		 */
		var finalFilteredRows = [];
		var pname = "";

		for(i=0; i< $scope.dataset.meta.columns.length;i++) {

			loc = $scope.dataset.meta.columns[i];
			pname = loc.pname;

			if (pname=="type" || pname=="fieldType") {
				finalFilteredRows.push(loc);
			}

		}

		return finalFilteredRows;

	}

	$scope.filterMetaValue = function(pname,item,i,index,myFieldType){

		var filteredMetaValues = [];

		/**
		 * A flag that will indicate if the "String" item in the Value column combobox for the belonging Field type row for the current column
		 * should be excluded. It does if one of two cases specified below are true.
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		var insertString = true;

		/**
		 * Cases in which the "String" item in the Field type should be excluded:
		 *
		 * myFieldType=="MEASURE":
		 * 		If initializing (entering) the Step 2, inspect the next Field type value and if it is a MEASURE, remove the "String" values
		 * 		from the current Type row (since the column of the MEASURE field type cannot be a String). If it is not a MEASURE, i.e. if
		 * 		it is an ATTRIBUTE, continue with initializing items in the Type combobox for particular row without any modification (include
		 * 		the "String" item as well).
		 *
		 * index && item=="MEASURE":
		 * 		If the user clicks on some Field type combobox (the combo in the Value column in the Step 2) and choose a MEASURE, its
		 * 		belonging Type value (for the same column, e.g. Country) should exclude the "String" item. In that case the "index" input
		 * 		parameter is defined.
		 *
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		if (myFieldType=="MEASURE" || index && item=="MEASURE")
			insertString = false;

		if(pname.toLowerCase()==="type".toLowerCase()){

			for(j=0;j<$scope.dsMetaValue.length;j++){

				 if($scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="string".toLowerCase() && insertString ||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="double".toLowerCase()||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="integer".toLowerCase()||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="long".toLowerCase()||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="date".toLowerCase() ||
						 $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="timestamp".toLowerCase()){
					 filteredMetaValues.push($scope.dsMetaValue[j]);
				 }
			}

		}else if(pname.toLowerCase()==="fieldType".toLowerCase()){
			for(j=0;j<$scope.dsMetaValue.length;j++){
   			 if($scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="attribute".toLowerCase()|| $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="spatial_attribute".toLowerCase()||
   			    $scope.dsMetaValue[j].VALUE_CD.toLowerCase()==="measure".toLowerCase()){
   				filteredMetaValues.push($scope.dsMetaValue[j]);
   			 }

   			}

		}
		else {
			angular.copy($scope.dsMetaValue,filteredMetaValues);
		}

		return filteredMetaValues;
	}


	$scope.metaScopeFunctions.valueChanged = function(item,index) {

    	if (item.VALUE_CD=="MEASURE" || item.VALUE_CD=="ATTRIBUTE" || item.VALUE_CD=="SPATIAL_ATTRIBUTE") {
    		$scope.prepareMetaForView(item.VALUE_CD,index);
    	}

    }

	$scope.goToFirstStep = function () {
		$scope.step=1;
		$scope.validated = false;
	}

    $scope.metaScopeFunctions.dsGenMetaProperty = $scope.dsGenMetaProperty;

    $scope.hasDuplicates = function(array,property){
		if(!array && !Array.isArray(array)){
			return false;
		}
		var obj = {};
		for(var i in array){
			obj[array[i][property]] = array[i]
		}

		return array.length > Object.keys(obj).length
	}

    // TAGS


	 var getTagsForDataset = function(dataset){
			 $scope.tags = dataset.tags;
	 }


};