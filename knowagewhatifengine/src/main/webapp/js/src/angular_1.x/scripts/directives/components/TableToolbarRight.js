/*
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
(function() {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	var contextBasePath = currentScriptPath + '../../../../../';

angular.module('sbi_table_toolbar',['sbiModule'])
.directive('sbiTableToolbar',function(sbiModule_messaging,sbiModule_config){
	return{
		restrict:"E",
		replace: true,
//		templateUrl:'/knowagewhatifengine/html/template/right/tableToolbar.html',
		templateUrl: function(){
			return sbiModule_config.contextName + '/html/template/right/tableToolbar.html'
		},
		controller: tableToolobarController
	}
});

function tableToolobarController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate, sbiModule_config,sbiModule_download, sbiModule_user,olapSharedSettings) {

	// see if calculated field is enabled
	$scope.showCalculatedField = sbiModule_user.isAbleTo("OlapCalculatedFieldView");
	$scope.isWorkFlowExists = (sbiModule_user.functionalities.indexOf("WorkFlowManagment")>-1)? true:false;
	var saveAsTimeout = olapSharedSettings.getSettings().persistNewVersionTransformations;
	$scope.availAlgorithms = [];
	$scope.activeAlg;
	$scope.clickedButtons = [];
	$scope.outputWizardDescription = sbiModule_translate.load('sbi.olap.toolbar.export.wizard.type.description');
	$scope.outputWizardTitle = sbiModule_translate.load('sbi.olap.toolbar.export.wizard.title');
	$scope.outputWizardTypeLabel = sbiModule_translate.load('sbi.olap.toolbar.export.wizard.type');
	$scope.outputTypes = [{name:'table',value:'table'},{name:'file',value:'csv'}]
	$scope.outputVersions = []; //TODO
	$scope.versionsForDelete = [];
	$scope.lockTooltip;
	$scope.DTEnabled = false;
	$scope.delimiter = "|";
	$scope.tableName = "WHATIFOUTPUTTABLE";
	$scope.outputType = $scope.outputTypes.length > 0 ? $scope.outputTypes[0].value:'';
	$scope.outputVersion= undefined;
	$scope.saveAsName = "";
	$scope.saveAsDescription ="";
	whatifToolbarButtonsVisible=[];
	$scope.lockerClass = "";
	$scope.showFile = false;
	$scope.showTable = false;
	$scope.showOVDescription = false;
	$scope.wiGridNeeded=false;
	$scope.tableGridNeeded = true;
	$scope.olapGridNeeded = true;
	$scope.subObject ={

		name:"",
		description:"",
		scope:"",
		isValid:function(){

			if(this.name!=undefined&&this.name!==""){
				return true;
			}

			return false;
		},
		setInitialState : function(){

			this.name = "";
			this.description = "";
			this.scope = "";

		},
		saveSubObject: function(){

			var successMsg = sbiModule_translate.load('sbi.olap.subobject.save.ok');
			var errorMsg = sbiModule_translate.load('sbi.olap.subobject.save.error');
			console.log("Saving subObject");
			sbiModule_restServices.promisePost
			("1.0",'/subobject?SBI_EXECUTION_ID='+ JSsbiExecutionID,this)
			.then(function(response) {
				console.log(successMsg);
				sbiModule_messaging.showSuccessMessage(successMsg, 'Success');



		  },function(response){
			  sbiModule_messaging.showErrorMessage(errorMsg, 'Error');
		  });


		}
	};
	var exportBtn = {};
	var exportEditableBtn = {};
	var result;
	var algsLoaded = false;
	$scope.wiMsg ="";
	$scope.wiMessageNeeded = false;

	var olapButtonNames = [];
	var whatifButtonNames = ["unlock-icon","lock-icon"];
	var tableButtonNames = [];
	$scope.olapDesignerButtonNames = [];

	enableEditBtns = function(mode) {
		if (mode == 'edit') {
			$scope.olapMode = true;
		}else {
			$scope.olapMode = false;
		}
	}

	$scope.getToolbarButtons = function() {

    	sbiModule_restServices.promiseGet
		("1.0",'/buttons')
		.then(function(response) {
			$scope.buttons = response.data;
			$scope.classiffyToolbarButtons();
			filterXMLResult(toolbarVisibleBtns);
		 }, function(response) {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');
		});
    }

	$scope.getToolbarButtons();

	  $scope.classiffyToolbarButtons = function() {
		  for (var i = 0; i < $scope.buttons.length; i++) {

				switch ($scope.buttons[i].category) {
				/*case "DRILL_ON_DATA":
					whatifButtonNames.push($scope.buttons[i].name);
					break;
				case "DRILL_ON_DIMENSION":
					whatifButtonNames.push($scope.buttons[i].name);
					break;*/
				case "WHAT_IF":
					whatifButtonNames.push($scope.buttons[i].name);
					break;
				case "TABLE_FUNCTIONS":
					tableButtonNames.push($scope.buttons[i].name);
					break;
				case "OLAP_FUNCTIONS":
					olapButtonNames.push($scope.buttons[i].name);
					break;
				case "OLAP_DESIGNER":
					if($scope.buttons[i].name == 'BUTTON_CC' && !$scope.showCalculatedField) {
						break;
					}
					$scope.olapDesignerButtonNames.push($scope.buttons[i]);
					break;
				default:
					break;
				}

			}
			  if(engineName == 'knowageolapengine'){
				for (var i = 0; i < $scope.olapDesignerButtonNames.length; i++) {
					if($scope.olapDesignerButtonNames[i].name == 'BUTTON_SCENARIO_WIZARD'){
						$scope.olapDesignerButtonNames.splice(i, 1);
					}
				}

			  }

	  }

	whatIfBtns = function(status){
			if(status == 'locked_by_user')
				$scope.whatifToolbarButtons = whatifToolbarButtonsVisible;
			else{
				var btn;
				//var exists = $scope.whatifToolbarButtons.indexOf("BUTTON_EXPORT_OUTPUT")>-1;
				$scope.whatifToolbarButtons = [];

				if(exportBtn.img == "BUTTON_EXPORT_OUTPUT"){
					$scope.whatifToolbarButtons.push(exportBtn);
				}
				if(exportEditableBtn.img == "BUTTON_EDITABLE_EXCEL_EXPORT"){
					$scope.whatifToolbarButtons.push(exportEditableBtn);
				}

			}

	};

	$scope.lockFunction = function(){
		if(status == 'unlocked'){
			lockUnlock('lock', $scope.modelConfig.artifactId);
		}
		if(status == 'locked_by_user'){
			lockUnlock('unlock', $scope.modelConfig.artifactId);
		}
		//$scope.$apply();
	}

	//Getting all buttons from XML and moving them inside grids(arrays) where they need to be
	filterXMLResult = function(res) {

		var regEx = /([A-Z]+_*)+/g;
		var i;
		while (i = regEx.exec(res)){
			var btn = {};
			btn.tooltip = sbiModule_translate.load("sbi.olap.toolbar."+ i[0]);// messageResource.get("sbi.olap.toolbar."+ i[0], 'messages');
			btn.img =i[0];//"../img/show_parent_members.png"// url(../img/show_parent_members.png);
			btn.name =i[0];

			if(btn.name == "BUTTON_EDITABLE_EXCEL_EXPORT")
				exportEditableBtn = btn;
			if(btn.name == "BUTTON_EXPORT_OUTPUT")
				exportBtn = btn;
			if(btn.name == "BUTTON_DRILL_THROUGH")
				$scope.DTEnabled = true;
			if(olapButtonNames.indexOf(btn.name)>-1)
				$scope.olapToolbarButtons.push(btn);
			else if(whatifButtonNames.indexOf(btn.name)>-1)
				$scope.whatifToolbarButtons.push(btn);
			else if(tableButtonNames.indexOf(btn.name)>-1) {
				if (btn.name == "BUTTON_SAVE_SUBOBJECT" && !sbiModule_user.isAbleTo("SaveSubobjectFunctionality")) {
					// in case user is not allowed to save subobjects (customized views), skip save subobject button
					continue;
				}
				$scope.tableToolbarButtons.push(btn);
			}

		}
		whatifToolbarButtonsVisible = $scope.whatifToolbarButtons;
		whatIfBtns(status);
		enableEditBtns(mode);

		if($scope.olapToolbarButtons.length == 0)
			$scope.olapGridNeeded = false;
		if($scope.tableToolbarButtons.length == 0)
			$scope.tableGridNeeded = false;
	};

	//Getting clicked buttons trough XML (clicked="true")
	filterClickedButtons = function(data){
		var regEx = /([A-Z]+_*)+/g;
		var i;
		while (i = regEx.exec(data)){
			$scope.clickedButtons.push(i[0]);
		}
	}



	//Call off function remove comment bellow to get names of clicked buttons
	filterClickedButtons(toolbarClickedBtns);

	$scope.executeClicks = function(){
		$scope.toggleRight();
		var temp;
		for(var i=0; i< $scope.clickedButtons.length; i++){
			if($scope.clickedButtons[i] == 'BUTTON_SORTING'){
				temp = $scope.clickedButtons[i];
				$scope.clickedButtons.splice(i,1);
			}
			$scope.btnFunctions($scope.clickedButtons[i]);
		}
		if(temp != null && $scope.ready){

			$scope.sortDisable();
		}
		$scope.toggleRight();
	}

	//Handling clicks on buttons inside filter panel right
	$scope.btnFunctions = function(name){
		var sendModelConfig = true;

		switch(name){
			case "BUTTON_FATHER_MEMBERS":
				$scope.modelConfig.showParentMembers = !$scope.modelConfig.showParentMembers;
				changeIcon(name);
				break;
			case "BUTTON_HIDE_SPANS":
				$scope.modelConfig.hideSpans = !$scope.modelConfig.hideSpans;
				changeIcon(name);
				break;
			case "BUTTON_SHOW_PROPERTIES":
				$scope.modelConfig.showProperties = !$scope.modelConfig.showProperties;
				changeIcon(name);
				break;
			case "BUTTON_HIDE_EMPTY":
				$scope.modelConfig.suppressEmpty = !$scope.modelConfig.suppressEmpty;
				//$scope.tableSubsets=[];//added by dragan for caching clear
				changeIcon(name);
				break;
			case "BUTTON_EXPORT_OUTPUT":
				$scope.showExportDialog(null);
				sendModelConfig = false;
				break;
			case "BUTTON_FLUSH_CACHE":
				flushCache();
				sendModelConfig = false;
				break;
			case "BUTTON_MDX":
				$scope.showDialog(null,$scope.showMdxDial);
				sendModelConfig = false;
				break;
			case "BUTTON_EDIT_MDX":
				$scope.showDialog(null,$scope.sendMdxDial);
				sendModelConfig = false;
				break;
			case "BUTTON_SAVE_SUBOBJECT":
				$scope.showDialog(null,$scope.saveSubObjectDial);
				sendModelConfig = false;
				break;
			case "BUTTON_SORTING_SETTINGS":
				$scope.showDialog(null,$scope.sortSetDial);
				sendModelConfig = false;
				break;
			case "BUTTON_CC":
				$scope.showCCWizard();
				sendModelConfig = false;
				break;
			case "BUTTON_SORTING":
				$scope.enableDisableSorting();
				//changeIcon(name);
				sendModelConfig = false;

				break;
			case "BUTTON_EDITABLE_EXCEL_EXPORT":
				$scope.exportDynamic();
				sendModelConfig = false;
				break;
			case "BUTTON_SAVE_NEW":
				$scope.showDialog(null,$scope.saveAsNew);
				sendModelConfig = false;
				break;
			case "BUTTON_VERSION_MANAGER":
				$scope.versionsForDelete = [];
				$scope.showDialog(null,$scope.deleteVersionDialog);
				sendModelConfig = false;
				break;
			case "BUTTON_SAVE":
				persistTransformations();
				sendModelConfig = false;
				break;
			case "BUTTON_UNDO":
				undo();
				sendModelConfig = false;
				break;
			case "BUTTON_ALGORITHMS":
				if(!algsLoaded)
					loadAlgorithms();
				$scope.showDialog(null, $scope.allocationAlgDialog);
				sendModelConfig = false;
				break;
			case "BUTTON_SCENARIO_WIZARD":
				$scope.runScenarioWizard();
				sendModelConfig = false;
				break;
			case "BUTTON_CROSSNAV_WIZARD":
				$scope.openCrossNavWizard();
				sendModelConfig = false;
				break;
			case "BUTTON_WIZARD":
				$scope.openButtonWizard();
				sendModelConfig = false;
				break;
			case "BUTTON_PAGINATION_WIZARD":
				$scope.openButtonPaginationWizard();
				sendModelConfig = false;
				break;
			default:
				console.log("something else clicked");
		}

		if(sendModelConfig){
			$scope.sendModelConfig($scope.modelConfig);
		}

	}

	$scope.enableDisableSorting = function(){

		$scope.sortDisable();
	}

	$scope.enableCompactProperties = function(){
		if($scope.modelConfig.showProperties != true){
			$scope.modelConfig.showCompactProperties = !$scope.modelConfig.showCompactProperties;
			$scope.sendModelConfig($scope.modelConfig);
		}else{
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.tableProperties.error'), 'Error');
		}
	}


	$scope.changeDrillType = function(type){
		$scope.modelConfig.drillType = type;
		$scope.sendModelConfig($scope.modelConfig);

	}

	$scope.isDisabledType = function(name){
		if($scope.modelConfig != undefined){
			if($scope.modelConfig.drillType == name)
				return true;
			else
				return false;
		}
	}

	$scope.enableCrossNavigation = function() {
    	sbiModule_restServices.promiseGet
		("1.0",'/crossnavigation/initialize/?SBI_EXECUTION_ID='+ JSsbiExecutionID)
		.then(function(response) {
			$scope.handleResponse(response);
			$scope.crossNavigationEnabled = !$scope.crossNavigationEnabled;
		 }, function(response) {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');
		});
    }

	 $scope.sortDisable = function(){

		 sbiModule_restServices.promiseGet("1.0","/member/sort/disable?SBI_EXECUTION_ID="+JSsbiExecutionID)
			.then(function(response) {
				$scope.handleResponse(response);

				changeIcon("BUTTON_SORTING");

			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.sorting.error'), 'Error');

			});
		  }

	  $scope.sort = function(axisToSort,axis,positionUniqueName){

		  toSend = {};
		  toSend.axisToSort =axisToSort;
		  toSend.axis =axis;
		  toSend.positionUniqueName = positionUniqueName;
		  toSend.sortMode = $scope.selectedSortingMode;
		  toSend.topBottomCount = $scope.sortingCount;

		  var path ='/member/sort/?SBI_EXECUTION_ID='+JSsbiExecutionID;



		 sbiModule_restServices.promisePost("1.0",path,toSend)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.sorting.error'), 'Error');

			});
		  }


	  //checking the lock state of document in order to show/hide buttons and info
	  checkLock = function(s){

		  if($scope.modelConfig.whatIfScenario==false){
			  $scope.lockTooltip = "";
			  $scope.wiMessageNeeded = false;
		  }
		  else if(s=="locked_by_user"){
			  $scope.lockerClass="unlock-icon";
			  $scope.lockTooltip = sbiModule_translate.load('sbi.olap.toolbar.unlock');
			  $scope.wiMessageNeeded = false;
		  }
		  else if(s=="locked_by_other"){
			  $scope.lockerClass="lock-other-icon";
			  $scope.lockTooltip = sbiModule_translate.load('sbi.olap.toolbar.lock_other');
			  $scope.lockTooltip += " "+locker;
			  $scope.wiMsg = sbiModule_translate.load('sbi.olap.toolbar.lock_other') +" "+locker;
			  $scope.wiMessageNeeded = true;
		  }
		  else if(s=="unlocked"){
			  $scope.lockerClass="lock-icon";
			  $scope.lockTooltip = sbiModule_translate.load('sbi.olap.toolbar.lock');
			  $scope.wiMsg = "Workflow finished";
			  $scope.wiMessageNeeded = true;
		  }
	  };



	  function lockUnlock(type, id){
		  sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath );

		  sbiModule_restServices.promisePost
			("1.0",'/locker/'+id)
			.then(function(response) {
				status = response.data.status;
				locker = response.data.locker;
				whatIfBtns(response.data.status);
				checkLock(response.data.status);
		  },function(response){
			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.workflow.error'), 'Error');
		  });

	  };

	  $scope.showExportDialog= function(ev){
		  $scope.showDialog(ev,"/right/exportOutputDialog.html");
	  };

	  $scope.exportDialogNext = function(){
		  if($scope.outputType == "csv")
			  $scope.showFile = true;
		  else
			  $scope.showTable = true;
	  }

	  $scope.exportDialogBack = function(){
		  $scope.showFile = false;
		  $scope.showTable = false;
	  }

	  flushCache = function(){
		  sbiModule_restServices.promisePost
			("1.0",'/cache/?SBI_EXECUTION_ID='+ JSsbiExecutionID)
			.then(function(response) {
						$scope.handleResponse(response);
		  },function(response){
			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.refresh.error'), 'Error');
		  });
	  };


	  persistTransformations = function(){
		  sbiModule_restServices.promisePost
			("1.0",'/model/persistTransformations/?SBI_EXECUTION_ID='+ JSsbiExecutionID)
			.then(function(response) {
						$scope.handleResponse(response);
		  },function(response){
			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.refresh.error'), 'Error');
		  });
	  };

	  undo = function(){
		  sbiModule_restServices.promisePost
			("1.0",'/model/undo/?SBI_EXECUTION_ID='+ JSsbiExecutionID)
			.then(function(response) {
						$scope.handleResponse(response);
		  },function(response){
			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.refresh.error'), 'Error');
		  });
	  };

	  $scope.exportOutputVersion = function(){
		  var sucessMsg = sbiModule_translate.load('sbi.olap.toolbar.exportoutput.ok');
		  $scope.showOVDescription = true;
		  var value = $scope.outputType == "csv"? $scope.delimiter : $scope.tableName;



		  if($scope.outputType == "csv"){
			  var link = '/restful-services/1.0/analysis/'+$scope.outputType+'/'+$scope.outputVersion+'/'+value+'?SBI_EXECUTION_ID='+ JSsbiExecutionID;
			  sbiModule_download.getLink(link);

		  }else{
			  sbiModule_restServices.promiseGet
				("1.0",'/analysis/'+$scope.outputType+'/'+$scope.outputVersion+'/'+value+'?SBI_EXECUTION_ID='+ JSsbiExecutionID)
				.then(function(response) {
					var name = documentDownloadName();
					sbiModule_messaging.showInfoMessage(sucessMsg, 'Info');


			  },function(response){
				  sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.versionExport.error'), 'Error');

			  });

		  }
		  $scope.closeDialogToolbarRight();
		  initDialogs();



	  };

	  initDialogs = function(){
		  $scope.showFile = false;
		  $scope.showTable = false;
		  $scope.showOVDescription = false;
	  };

	  $scope.isOkBtnDisabled = function(){
		  if(!$scope.showFile && !$scope.showTable || $scope.showOVDescription)
			  return true;
		  else if($scope.outputType == "table" && $scope.tableName.length < 1)
			  return true;
		  else if($scope.outputType == "csv" && $scope.delimiter.length < 1)
			  return true;
		  else
			  return false;

	  };

	  //function that handling changing of icon in order to know which button is clicked
	  changeIcon = function(name){

		  for(var i=0; i < $scope.tableToolbarButtons.length;i++){
			  if($scope.tableToolbarButtons[i].name == name){
				  if($scope.tableToolbarButtons[i].img == name){
					  $scope.tableToolbarButtons[i].img = name+"_CLICKED"
				  }
				  else{
					  $scope.tableToolbarButtons[i].img = name;
				  }
			  }
		  }
	  }

	  documentDownloadName = function(){
		  var date = new Date();
		  var d = date.getDate();
		  var m = date.getMonth()+1;
		  var y = date.getFullYear();

		  return "KnowageOlapExport-" + d + "." + m + "." + y;
	  }

	  $scope.cancelSavingSubObject = function(){

		  console.log("closing Save customized view dialog");
		  $scope.closeDialogToolbarRight();
		  console.log("setting subObject to empty");
		  $scope.subObject.setInitialState();
	  }

	  $scope.saveSubObject = function(){
		  console.log($scope.subObject);
		  $scope.subObject.saveSubObject();
		  $scope.closeDialogToolbarRight();
	  }

	  $scope.saveAsFunction = function(){
		  if($scope.saveAsDescription == undefined)
			  $scope.saveAsDescription ="";

		  var content={};
		  if($scope.saveAsName){
			 content.name = $scope.saveAsName;
		  }else{
			 content.name = "sbiNoDescription";
		  }
		  if($scope.saveAsDescription){
			 content.descr = $scope.saveAsDescription;
		  }else{
			 content.descr = "sbiNoDescription";
		  }


		  sbiModule_restServices.promisePost
			("1.0","/model/saveAs?SBI_EXECUTION_ID="+ JSsbiExecutionID,content,{timeout:saveAsTimeout})
			.then(function(response) {
				$scope.handleResponse(response);
				$mdDialog.hide();
				sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.olap.versionSave.info'), 'Info');
		  },function(response){
			  sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.versionSave.error'), 'Error');
		  });
	  }

	  $scope.exists = function(id){
		  return $scope.versionsForDelete.indexOf(id) > -1;
	  }

	  $scope.selectForDelete = function(id){
		  var idx = $scope.versionsForDelete.indexOf(id);

		  if(idx > -1)
			  $scope.versionsForDelete.splice(idx,1);
		  else
			  $scope.versionsForDelete.push(id);
	  }

	  $scope.selectUnselectAll = function(){
		  if($scope.versionsForDelete.length == $scope.outputVersions.length){
			  $scope.versionsForDelete = [];
		  }
		  else{
			  $scope.versionsForDelete = [];
			  for(var i=0;i < $scope.outputVersions.length;i++){
				  $scope.versionsForDelete.push($scope.outputVersions[i].id);
			  }
		  }
	  }

	  $scope.deleteVersions = function(){

		  var okToDelete = isOkToDeleteVersion($scope.versionsForDelete);

		  if(okToDelete){
			  var path ='/version/delete/'+ $scope.versionsForDelete+'?SBI_EXECUTION_ID='+JSsbiExecutionID;

				 sbiModule_restServices.promisePost("1.0",path)
					.then(function(response) {
						sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.versionDelete.success'), 'Success');
						$scope.getVersions();
						$scope.closeDialogToolbarRight();
					}, function(response) {
						sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.versionDelete.error'), 'Error');

					});
		  }
		  else{
			  sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.versionDeleteActual.warning'), 'Warning');
		  }
	  }

	  loadAlgorithms = function(){
		  var path ='/allocationalgorithm/?SBI_EXECUTION_ID='+JSsbiExecutionID;

		  sbiModule_restServices.promiseGet("1.0",path)
			.then(function(response) {
				$scope.availAlgorithms = response.data;
				for(var i=0; i< response.data.length;i++){
					if(response.data[i].defaultAlgorithm)
						$scope.activeAlg = response.data[i];
				}
				algsLoaded = true;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.algorithmsLoad.error'), 'Error');

			});
	  }

	  $scope.setAlgorithm = function(){
		  var path ='/allocationalgorithm/' + $scope.activeAlg.className + '?SBI_EXECUTION_ID='+JSsbiExecutionID;
		  sbiModule_restServices.promisePost("1.0",path)
			.then(function(response) {

				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.algorithmsSave.success'), 'Success');
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.algorithmsSave.error'), 'Error');

			});
	  }

	  $scope.closeDialogToolbarRight = function(){
		  $mdDialog.hide();
	  }

	  isOkToDeleteVersion = function(versions){
		  for(var i=0; i< versions.length;i++){
			  if(versions[i].id == $scope.selectedVersion){
				  return true;
			  }
		  }
		  return false;
	  }


};
})();