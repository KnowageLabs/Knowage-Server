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

/**
 * Directive used in OLAP template build.
 * @author Nikola Simovic (nsimovic, nikola.simovic@mht.net)
 * @author Stefan Petrovic (spetrovic, stefan.petrovic@mht.net)
 */
angular.module('olap_designer_toolbar', ['sbiModule','olap_template'])
		.directive(
				'olapDesignerToolbar',
				function(sbiModule_config) {
					return {
						restrict : "E",
						replace : 'true',
						templateUrl : function() {
						return sbiModule_config.contextName + '/html/template/right/edit/olapDesignerToolbar.html'
						},
						controller : olapDesignerToolbarController
					}
				});


function olapDesignerToolbarController($scope, channelMessagingToolbar, $timeout, $window, $mdDialog, $http, $sce,
		sbiModule_messaging, sbiModule_restServices, sbiModule_translate,
		toastr, $cookies, sbiModule_docInfo, sbiModule_config,sbiModule_user,OlapTemplateService) {

	/**
	 * TOOLBAR is the array of button objects to send to olap template object.
	 */
	$scope.toolbar = [];

	$scope.cubeList = [];

	$scope.schemaName = schemaName;

	$scope.mdxQueryObj = {
			"mdxQuery" : ""
	}

	/**
	 * SCENARIO is the temporary object that will be bind to olapTemplate if the scenario is defined.
	 */
	$scope.scenario = {
			name: "scenario",
			editCube : "",
			measures: []
	};
	/**
	 * Array that hold cross navigation types
	 */
	$scope.crossNavType = null;
	$scope.showCNType = false;
	$scope.crossNavTypeList = [{
	   	 "value": "cell",
		  "name": "From Cell"
	},
	{
		 "value": "member",
		  "name": "From Member"
	}];

	$scope.crossNavList = [];

	/**
	 * Object that holds Nav from member
	 */
	var clickableArray = [];
	$scope.crossNavfromMemberObj = {
			"uniqueName" : "",
			"clickParameter": {
				"name": "",
				"value":"{0}"
			}
	};

	var parameter = [];

	$scope.crossNavfromCellObj = {
			 "name":"",
             "dimension":"",
             "hierarchy":"",
             "level":""
	};
	$scope.cellForShowObj = {
		"name":""
	}

	$scope.showInfoo= false;

	$scope.pagination = false;

	var counter = 0;

	angular.element(document).ready(function () {
		counter = 1;
		var json = JSON.parse(jsonTemplate);

		if(json != null){

			OlapTemplateService.setTemplateObject(json);

			if(editModeCurrentContentId!="null"){
				currentContentId = editModeCurrentContentId;
			}


		}else{
			console.log("No TEMPLATE");
		}

    });



	/**
	 * Loads cubes and opens a new what-if scenario dialog.
	 */
	$scope.runScenarioWizard = function(){
		if(OlapTemplateService.getScenarioObject() != undefined){
			$scope.scenario = OlapTemplateService.getScenarioObject();
			$scope.loadAllMeasures($scope.scenario.editCube);
		}else{
			$scope.scenario = {
					name: "scenario",
					editCube : "",
					measures: []
			};
		}

		$scope.getAllCubes();

		$mdDialog
		.show({
			scope : $scope,
			preserveScope : true,
			parent : angular.element(document.body),
			controllerAs : 'olapDesignerCtrl',
			templateUrl : sbiModule_config.contextName + '/html/template/right/edit/scenarioWizard.html',
			clickOutsideToClose : false,
			hasBackdrop : false
		});
	}



	$scope.initMDX = function() {

		 $scope.mdxQueryObj.mdxQuery = $scope.MDXWithoutCF;
		 OlapTemplateService.setMDXMondrianQueryTag($scope.MDXWithoutCF);
		 OlapTemplateService.setMdxQueryTag($scope.mdxQueryObj);
		 var params= $scope.getBindedAttributes();
		 OlapTemplateService.injectParametersToMdxQueryTag(params);
	}

$scope.setAndLoadCN = function(num) {

		if(jsonTemplate!="null" && num == 1 ){
			console.log(jsonTemplate)
			var json = JSON.parse(jsonTemplate);
			if(json.olap.hasOwnProperty("CROSS_NAVIGATION")){
				if(json.olap.CROSS_NAVIGATION.PARAMETERS.PARAMETER.constructor === Array){

					OlapTemplateService.setCrossNavigationTag(json.olap.CROSS_NAVIGATION.PARAMETERS.PARAMETER);
				}else{
					var tmpArray = [];
					tmpArray.push(json.olap.CROSS_NAVIGATION.PARAMETERS.PARAMETER);
					OlapTemplateService.setCrossNavigationTag(tmpArray);

				}
			}

			if(json.olap.hasOwnProperty("MDXQUERY")){
				if(json.olap.MDXQUERY.hasOwnProperty("clickable")){

					if(json.olap.MDXQUERY.clickable.constructor === Array){
						$scope.initMDX();
						if(OlapTemplateService.getMdxQueryTag){
							OlapTemplateService.setClickableTag(json.olap.MDXQUERY.clickable);
						}
					}else{
						var tmpArray = [];
						tmpArray.push(json.olap.MDXQUERY.clickable);
						$scope.initMDX();
						if(OlapTemplateService.getMdxQueryTag){
							OlapTemplateService.setClickableTag(tmpArray);
						}

					}
				}
			}
			counter++;
		}else{
			console.log("First time making template")
		}

	}



	/**
	 * Function that check validity of finish button
	 */
	$scope.checkCNValidity = function(type){

		if(type =='cell' && $scope.crossNavfromCellObj.name =='' || type =='member' && $scope.crossNavfromMemberObj.clickParameter.name ==''){
			return true;
		}else{
			return false;
		}
	}

	/**
	  * Formats cell object for saving
	  */
	var formatCellNavigation = function() {
		$scope.cellForShowObj.name = 'dimension='+$scope.selectedMember.dimension+' '+'hierarchy='+$scope.selectedMember.hierarchyUniqueName+' '+'level='+$scope.selectedMember.level;
		$scope.crossNavfromCellObj.dimension = $scope.selectedMember.dimension;
		$scope.crossNavfromCellObj.hierarchy = $scope.selectedMember.hierarchyUniqueName;
		$scope.crossNavfromCellObj.level = $scope.selectedMember.level;
	}

	/**
	 * Loads schema cubes.
	 */
	$scope.getAllCubes = function(){
		sbiModule_restServices.promiseGet("1.0/designer/cubes/"+currentContentId,"?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		.then(function(response) {
			$scope.cubeList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	};

	/**
	 * Loads all measures.
	 */

	$scope.loadAllMeasures = function (editCube) {
		if(editModeCurrentContentId!="null"){
			currentContentId = editModeCurrentContentId;
		}
		sbiModule_restServices.promiseGet("1.0/designer/measures/"+currentContentId + "/"+ editCube,"?SBI_EXECUTION_ID=" + JSsbiExecutionID)
		.then(function(response) {
			$scope.measuresList = [];
			for (var i = 0; i < response.data.length; i++) {
				var measuresListItem = { name: ""};
				measuresListItem.name = response.data[i];
				$scope.measuresList.push(measuresListItem);
			}

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}

	/**
	 * Opens a new dialog for crossnav definition.
	 */
	$scope.openCrossNavWizard = function() {

		$scope.crossNavType = null;

		$scope.readCNJson();

		 $mdDialog
		  .show({
		   scope : $scope,
		   preserveScope : true,
		   parent : angular.element(document.body),
		   controllerAs : 'olapDesignerCtrl',
		   templateUrl : sbiModule_config.contextName + '/html/template/right/edit/crossNavWizardS1.html',
		   clickOutsideToClose : false,
		   hasBackdrop : false
		  });

	};

	/**
	 * Opens a new dialog for crossnav definition.
	 */
	$scope.nextCNStep = function() {

		 $mdDialog
		  .show({
		   scope : $scope,
		   preserveScope : true,
		   parent : angular.element(document.body),
		   controllerAs : 'olapDesignerCtrl',
		   templateUrl : sbiModule_config.contextName + '/html/template/right/edit/crossNavWizardS2.html',
		   clickOutsideToClose : false,
		   hasBackdrop : false
		  });

	};
	/**
	 * Binds temporary scenario object to olap template object via service after validation check.
	 */
	$scope.saveScenario = function() {
		if($scope.clearData){
    		OlapTemplateService.deleteScenarioTag();
    		$scope.clearData = false;
    		$mdDialog.hide();
    		sbiModule_messaging.showSuccessMessage('Successfully removed scenario from template', 'Success');
    	} else {
    		if($scope.scenario.hasOwnProperty('variables')){
    			if($scope.scenario.variables.length==0){
    				delete $scope.scenario.variables;
    			}
    		}
    		for (var i = 0; i < $scope.scenario.measures.length; i++) {
    			if($scope.scenario.measures[i]==undefined){
    				delete $scope.scenario.measures[i];
    			}
    		}
    	    if($scope.scenario.editCube==""&&$scope.scenario.measures.length==0){
    			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.designer.cubeValidation.error'), 'Error');
    			console.log($scope.scenario)
    		}
    	    else if($scope.scenario.editCube==""){
    			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.designer.cubeValidation2.error'), 'Error');
    		}
    		else if($scope.scenario.measures.length==0){
    			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.designer.cubeValidation3.error'), 'Error');
    		}
    	    else {
    	    		OlapTemplateService.setScenarioTag($scope.scenario);
    				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.designer.templateAdd'), 'Success');
    				console.log($scope.scenario);
    				console.log(OlapTemplateService.getTempateJson());
    				$mdDialog.hide();
    				console.log(OlapTemplateService.getScenarioObject());
    		}
    	}
	}

	/**
	 * Changes the state of the selected checkbox item. [editable measures]
	 */
	$scope.toggle = function (item) {

	   if($scope.scenario.measures){



			 for (var i = 0; i < $scope.scenario.measures.length; i++) {
					if(item.name == $scope.scenario.measures[i].name){
						$scope.scenario.measures.splice(i, 1);
						return;
					}
				}
			 $scope.scenario.measures.push(item);

		 }


	 };

	 /**
	  * Used for ng-change directive on single checkbox item.
	  * If this expression evaluates as truthy, the 'md-checked' css class is added to the checkbox and it will appear checked.[editable measures]
	  */
	 $scope.exists = function (item) {
		 if($scope.scenario.measures){

			 for (var i = 0; i < $scope.scenario.measures.length; i++) {
					if(item.name == $scope.scenario.measures[i].name){
						return true;
					}
				}
				return false;

		 }

	 };

	 /**
	  * Used for md-indeterminate
	  */
	 $scope.isIndeterminate = function() {
	   return ($scope.scenario.measures.length !== 0 &&
	       $scope.scenario.measures.length !== $scope.measuresList.length);
	 };

	 /**
	  * Used for Select All check box to determine if all check boxes are checked.
	  */
	 $scope.isChecked = function() {
		 if($scope.scenario.measures!=undefined&&$scope.measuresList!=undefined){
			 return $scope.scenario.measures.length === $scope.measuresList.length;
		 }
     };

     $scope.hasSelectedCube = function () {
    	 if($scope.scenario.editCube!=""){
			 return true;
		 } else {
			 return false;
		 }
     }

     /**
	  * Selects or un-selects all measures check boxes.
	  */
	 $scope.toggleAll = function() {
	   if ($scope.scenario.measures.length === $scope.measuresList.length) {
		   $scope.scenario.measures = [];
	   } else if ($scope.scenario.measures.length === 0 || $scope.scenario.measures.length > 0) {
		   $scope.scenario.measures = $scope.measuresList.slice(0);
	   }
	 };

	 /**
	  * Closes dialog in olap designer.
	  */
	 $scope.closeDialogOlapDesigner = function() {
		 $mdDialog.hide();
		 $scope.showCNType = false;
		 $scope.toolbar = [];
		 $scope.scenario.measures = [];


	 }

	 $scope.clearScenario = function () {
		 $scope.scenario = {
					name: "scenario",
					editCube : "",
					measures: []
			};
		 $scope.measuresList = [];
		 $scope.clearData = true;
	 }

	 /**
	  * Adds variable map(name,value) object to scenario property array variable.
	  */
	 $scope.addVariable=function(){
			var variable={};
			if($scope.scenario.variables==undefined){
				$scope.scenario.variables = [];
			}
			$scope.scenario.variables.push(variable);
			console.log($scope.scenario.variables);
			return variable;
	 }

	 /**
	  * Removes selected variable from array property of $scope.scenario object.
	  */
	 $scope.removeVariable=function(inputVariable){
			var index=$scope.scenario.variables.indexOf(inputVariable);
			$scope.scenario.variables.splice(index, 1);
			console.log($scope.scenario.variables);
	 }

	 /**
	  * Opens dialog with table of buttons to be selected as visible, and checked.
	  */
	 $scope.openButtonWizard = function() {
		 $scope.toolbar = angular.copy($scope.buttons);
		 if(OlapTemplateService.getToolbarButtons().length>0){

			 for(var i =0;i<$scope.toolbar.length;i++){

				 for(var j =0;j<OlapTemplateService.getToolbarButtons().length;j++){
					 if(OlapTemplateService.getToolbarButtons()[j].name === $scope.toolbar[i].name){

						 $scope.toolbar[i].clicked = OlapTemplateService.getToolbarButtons()[j].clicked;
						 $scope.toolbar[i].visible = OlapTemplateService.getToolbarButtons()[j].visible;

					 }
				 }


			 }


		 }



		 //
		 for (var i = $scope.toolbar.length-1; i >= 0; i--) {
			 if($scope.toolbar[i].category == 'OLAP_DESIGNER'){
		    	 $scope.toolbar.splice(i, 1);
			 }
		 }


		 if(engineName==="knowageolapengine"){
			 for (var i = $scope.toolbar.length-1; i >= 0; i--) {
				 if($scope.toolbar[i].category == 'WHAT_IF'){
			    	 $scope.toolbar.splice(i, 1);
				 }
			 }

		 }

		 for (var i = $scope.toolbar.length-1; i >= 0; i--) {

		    	 $scope.toolbar[i].displayLabel = sbiModule_translate.load($scope.toolbar[i].name);

		 }





		 $mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent : angular.element(document.body),
				controllerAs : 'olapDesignerCtrl',
				templateUrl : sbiModule_config.contextName + '/html/template/right/edit/olapDesignerButtonsWizard.html',
				clickOutsideToClose : false,
				hasBackdrop : false
			});
	 }

	 $scope.openButtonPaginationWizard = function(){

		 $scope.pagination = OlapTemplateService.getPagination();

		 $mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				parent : angular.element(document.body),
				controllerAs : 'olapDesignerCtrl',
				templateUrl : sbiModule_config.contextName + '/html/template/right/edit/pagination_wizard.html',
				clickOutsideToClose : false,
				hasBackdrop : false
			});
	 }
	 $scope.savePagination = function(){
		 OlapTemplateService.setPaginationTag( $scope.pagination);
		 sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.designer.buttonAdd'), 'Success');
		 $scope.closeDialogOlapDesigner();
	 }

	 /**
	  * Calls service to bind temporary buttons object to olap template object.
	  */
	 $scope.saveTemplateButtons = function() {
		 console.log($scope.toolbar);
		 OlapTemplateService.setToolbarTag($scope.toolbar);
		 sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.designer.buttonAdd'), 'Success');
		 $scope.closeDialogOlapDesigner();
	 }

	 /**
	  * Defining columns property for angular table with id="olapDesignerButtonsList"
	  */
	 $scope.olapDesignerButtonsColumns = [
                                          {
                                              label:"Label",
                                              name:"displayLabel",
                                              size:"200px"
                                          },
                                          {
                                              label:"Visible",
                                              name:"visible",
                                              hideTooltip:true,
                                              transformer:function(){
                                                  return " <md-checkbox ng-model='row.visible' ng-change='scopeFunctions.checkVisibility(row)' aria-label='buttonVisible'></md-checkbox>";
                                              }
                                          },
                                          {
                                              label:"Clicked",
                                              name:"clicked",
                                              hideTooltip:true,
                                              transformer:function(){
                                                  return " <md-checkbox ng-model='row.clicked' ng-change='scopeFunctions.checkVisibility(row)' ng-disabled='{{row.clickable==false}}'  aria-label='buttonClicked'></md-checkbox>";
                                              }
                                          }

                                          ]



	 $scope.tableFunction={

			 	checkVisibility: function(row){
			 		if(row.clicked == true){
			 	       row.visible = true;
			 	      }
				}
		};

	 /**
	  * Defining columns property for angular table with id="olapDesignerCrossNavList"
	  */
	$scope.olapDesignerCrossNavColumns = [

											{
											    label:"Name",
											    name:"name",
											},
											{
											    label:"Type",
											    name:"type",
											    hideTooltip:true,

											},
											/*{
											    label:"Description",
											    name:"description",
											    hideTooltip:true,
											    size:"300px"
											}*/

											]
	 /**
	  * Delete button table with id="olapDesignerCrossNavList"
	  */
	 $scope.cnSpeedMenu= [
	                         {
	                            label:sbiModule_translate.load("Delete"),
	                            icon:'fa fa-trash',
	                            action:function(item,event){

	                            	$scope.confirmDelete(item,event);
	                            }
	                         }
	                        ];
	 /**
	  * Confirming deletion of item in table with id="olapDesignerCrossNavList"
	  */
	 $scope.confirmDelete = function(item,ev) {
		    var confirm = $mdDialog.confirm()
		          .title(sbiModule_translate.load('sbi.common.confirmDelete'))
		          .content(sbiModule_translate.load('sbi.common.confirmDelete.content'))
		          .ariaLabel("confirm_delete")
		          .targetEvent(ev)
		          .ok(sbiModule_translate.load('sbi.common.continue'))
		          .cancel(sbiModule_translate.load('sbi.common.cancel'));
		    $mdDialog.show(confirm).then(function() {
		    	$scope.deleteCNItem(item);
		    }, function() {

		    });
		  };

	/**
	* Function that enables adding new cross navigation
	*/
	 $scope.addNewCN = function() {

		if ($scope.showCNType == false)
			$scope.showCNType = true;

		$scope.crossNavfromCellObj = {
				 "name":"",
	             "dimension":"",
	             "hierarchy":"",
	             "level":""
		};
		$scope.cellForShowObj = {
			"name":""
		};
		$scope.crossNavfromMemberObj = {
				"uniqueName" : "",
				"clickParameter": {
					"name": "",
					"value":"{0}"
				}
		};

	}

	 /**
	  * Select mode for selecting member in table
	  */
	$scope.enterSelectModeCN = function() {

		$mdDialog.hide();
		toastr
				.info(
						'Click ok to finish selection<br /><br /><md-button class="md-raised">OK</md-button>',
						{
							allowHtml : true,
							timeOut : 0,
							extendedTimeOut : 0,

							onTap : function() {
								if($scope.crossNavType == 'member'){
									$scope.crossNavfromMemberObj.uniqueName = $scope.selectedMember.level;
								}else if ($scope.crossNavType == 'cell') {
									formatCellNavigation();
								}

								$scope.nextCNStep();
								toastr.clear();

							}

						});
	}


	/**
	  * Saving crossnavigation in template json
	  */
	 $scope.saveCN = function(type) {
		  if(type == 'member'){
		  if($scope.itemForEditing != null && $scope.itemForEditing.type == 'From Member'){
		   console.log("actually editing");
		   console.log($scope.itemForEditing);
		   for (var i = clickableArray.length - 1; i >= 0; i--) {
		    if (clickableArray[i].uniqueName == $scope.itemForEditing.uniqueName) {
		     console.log("same item replacing");
		     clickableArray.splice(i, 1);
		    }
		   }
		  }

		   if(OlapTemplateService.getMdxQueryClickables()!=undefined){
			   clickableArray = OlapTemplateService.getMdxQueryClickables();
		   }
		   clickableArray.push($scope.crossNavfromMemberObj);

		  }else if (type == 'cell') {
		   if($scope.itemForEditing != null && $scope.itemForEditing.type == 'From Cell'){
		    console.log("actually editing");
		    console.log($scope.itemForEditing);
		    for (var i = parameter.length - 1; i >= 0; i--) {
		     if (parameter[i].name == $scope.itemForEditing.name) {
		      console.log("same item replacing");
		      parameter.splice(i, 1);

		     }
		    }
		   }

		   if(OlapTemplateService.getCrossNavigation()!=undefined){
			   parameter = OlapTemplateService.getCrossNavigation();
		   }
		   parameter.push($scope.crossNavfromCellObj);
		  }

		  if(type == 'member' && clickableArray.length > 0){
		   $scope.initMDX();
		   if(OlapTemplateService.getMdxQueryTag){
		    var success = OlapTemplateService.setClickableTag(clickableArray);
		    if(success){

		     sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.designer.cnAdd'), 'Success');
		     $scope.crossNavfromMemberObj = {
		       "uniqueName" : "",
		       "clickParameter": {
		        "name": "",
		        "value":"{0}"
		       }
		     };
		    }
		   }


		  }else if (type == 'cell' && parameter.length > 0) {
		   var success = OlapTemplateService.setCrossNavigationTag(parameter);
		   if(success){

		    sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.designer.cnSave'), 'Success');
		    $scope.crossNavfromCellObj = {
		       "name":"",
		                "dimension":"",
		                "hierarchy":"",
		                "level":""
		    };
		    $scope.cellForShowObj = {
		     "name":""
		    }

		   }

		  }
		   $scope.closeDialogOlapDesigner();
		 }

	/**

	  * Reading from json to fill table
	  */
	$scope.readCNJson = function() {

		$scope.crossNavList= [];
		var tempCellCN = [];
		var tempMemberCN =[];
		$scope.setAndLoadCN(counter);

		if( OlapTemplateService.getCrossNavigation() != undefined){

			 tempCellCN = OlapTemplateService.getCrossNavigation();
			for (var i = 0; i < tempCellCN.length; i++) {
				tempCellCN[i].type ="From Cell";
				//tempCellCN[i].description = $scope.cellForShowObj.name;
				$scope.crossNavList.push(tempCellCN[i]);
			}

		}else{
			console.log("Cross Navigation tag is empty")
		}

		if( OlapTemplateService.getMdxQueryClickables() != undefined){
			tempMemberCN = OlapTemplateService.getMdxQueryClickables();
			for (var i = 0; i < tempMemberCN.length; i++) {
				tempMemberCN[i].type ="From Member";
				tempMemberCN[i].name = tempMemberCN[i].clickParameter.name
				$scope.crossNavList.push(tempMemberCN[i]);
			}

		}else{
			console.log("Clickable tag is empty")
		}

	}

	/**
	  * Function that deletes cross navigation
	  */

	$scope.deleteCNItem = function(item) {
		if(item.type == "From Cell"){
			OlapTemplateService.deleteParamFromCrossNavigationTag(item);
		}
		if(item.type == "From Member"){
			OlapTemplateService.deleteParamFromClickables(item);
		}
		$scope.readCNJson();
		$scope.openCrossNavWizard();


	}

	$scope.editCNItem = function(item) {
		console.log("editing...");
		$scope.itemForEditing = item;
		var tempCellCN = [];
		var tempMemberCN =[];
		console.log(item);
		if(item.type == "From Cell"){
			$scope.crossNavType = 'cell';
			 tempCellCN = OlapTemplateService.getCrossNavigation();
			 for (var i = 0; i < tempCellCN.length; i++) {
					if (item.name === tempCellCN[i].name) {
						console.log("same one in editing")
						$scope.cellForShowObj.name = 'dimension='+item.dimension+' '+'hierarchy='+item.hierarchy+' '+'level='+item.level;
						$scope.crossNavfromCellObj.name = item.name;
						$scope.crossNavfromCellObj.dimension = item.dimension;
						$scope.crossNavfromCellObj.hierarchy = item.hierarchy;
						$scope.crossNavfromCellObj.level = item.level
					}
				}
		}
		if(item.type == "From Member"){
			$scope.crossNavType = 'member';
			tempMemberCN = OlapTemplateService.getMdxQueryClickables();
			 for (var i = 0; i < tempMemberCN.length; i++) {
					if (item.name === tempMemberCN[i].name) {
						console.log("same one in editing")
						$scope.crossNavfromMemberObj.uniqueName = item.uniqueName;
						$scope.crossNavfromMemberObj.clickParameter.name = item.name;
					}
				}
		}


		$scope.nextCNStep();

	}

	/**
	  * Calls service to bind temporary cube object to olap template object.
	  */
	 $scope.appendCubeObjectToJsonTemplate = function() {
		 OlapTemplateService.setCubeTag($scope.schemaName);
	 }

	 $scope.appendCubeObjectToJsonTemplate();

	 String.prototype.replaceAll = function(search, replace) {
	      if (replace === undefined) {
	          return this.toString();
	      }
	      return this.split(search).join(replace);
	  }

	 /**
	  * Calls service to bind temporary MDXMondrianQuery object to olap template json.
	  * Sends final template json to beckend.
	  */
	 $scope.sendOlapJsonTemplate = function() {

		 $scope.initMDX();
		 console.log(OlapTemplateService.getTempateJson());
		 OlapTemplateService.setCalculatedFieldsTag($scope.calculatedFields);
		 sbiModule_restServices.alterContextPath("/knowage");
		 sbiModule_restServices.promisePost("1.0/documents/",sbiModule_docInfo.label+'/saveOlapTemplate', OlapTemplateService.getTempateJson())
			.then(function(response) {
				console.log("[POST]: SUCCESS!");
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load('sbi.olap.designer.templateSave'),'Success');
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
	 }

	 /**
	  * Close the wizard
	  */
	 $scope.closeOlapTemplate = function(){

		 channelMessagingToolbar.sendMessage();

	 }

	 /**
	  * Open first step
	  */
	 $scope.openFisrtStep = function(){

	 	if(sbiModule_config.externalBasePath == 'null'){
	 		sbiModule_config.externalBasePath = '/knowage';
	 	}
		 var url= sbiModule_config.protocol+"://"+sbiModule_config.host+":"+sbiModule_config.port
		 url+= 	"/"+sbiModule_config.contextName+"/restful-services/olap/startwhatif/edit?" +
		 		"&DOCUMENT_LABEL="+sbiModule_docInfo.label +
		 		"&mode=edit" +
		 		"&user_id=" + sbiModule_user.userId +
		 		"&document="+sbiModule_docInfo.id +
		 		"&ENGINE=knowagewhatifengine" +
		 		"&SBI_LANGUAGE" + sbiModule_config.curr_language
		 		"&SBI_COUNTRY" + sbiModule_config.curr_country
				"&SBI_SCRIPT" + sbiModule_config.curr_script
		 		"&SBI_EXECUTION_ID=" + JSsbiExecutionID

		 window.parent.location.href=url;

	 }


		$scope.getBindedAttributes = function (){

			var bindedAttributes = [];

			for (var i = 0; i < $scope.adParams.length; i++) {
				if($scope.adParams[i].bindObj != null){
					var memberObj = {};
					var uniqueName = $scope.adParams[i].bindObj.filter.uniqueName;
					var paramName = '${'+ $scope.adParams[i].url +'}';
					memberObj.memberUniqueName = $scope.adParams[i].bindObj.filter.uniqueName;
					memberObj.replaceName = uniqueName.replace($scope.adParams[i].bindObj.filter.replaceItem,paramName);
					memberObj.parameter = {};

					memberObj.parameter.name= $scope.adParams[i].url;
					memberObj.parameter.as = $scope.adParams[i].url;

					//memberObj.parameter.name= $scope.adParams[i].url;
					//memberObj.parameter.as = $scope.adParams[i].label;

					bindedAttributes.push(memberObj);
				}
			}

			for (var i = 0; i < $scope.profileAttributes.length; i++) {
				if($scope.profileAttributes[i].bindObj != null){
					var memberObj = {};
					var uniqueName = $scope.profileAttributes[i].bindObj.filter.uniqueName;
					var paramName = '${'+ $scope.profileAttributes[i].attributeName +'}';
					memberObj.memberUniqueName = $scope.profileAttributes[i].bindObj.filter.uniqueName;
					memberObj.replaceName = uniqueName.replace($scope.profileAttributes[i].bindObj.filter.replaceItem,paramName);

					bindedAttributes.push(memberObj);
				}
			}

			console.log(bindedAttributes);

			return bindedAttributes;
		};

		$scope.showEditCubeInfo=function(){
			$scope.showInfo=!$scope.showInfo;
		}



		 $scope.isClickedChecked = function() {
			  for(var i =0;i<$scope.toolbar.length;i++){
				  if($scope.toolbar[i].clicked!=true&&$scope.toolbar[i].clickable)
					return false;
				}
			  return true;
		};

		$scope.toggleAllClicked = function() {
			var toChange = !$scope.isClickedChecked();

			for(var i =0;i<$scope.toolbar.length;i++){
					if($scope.toolbar[i].clickable){
						$scope.toolbar[i].clicked = toChange;
						if(toChange){
							$scope.toolbar[i].visible = toChange;
						}
					}
				}
		}

		$scope.isVisibleChecked = function() {
			  for(var i =0;i<$scope.toolbar.length;i++){
				  if($scope.toolbar[i].visible!=true)
					return false;
				}
			  return true;
		};

		$scope.toggleAllVisible = function() {
			var toChange = !$scope.isVisibleChecked();

			for(var i =0;i<$scope.toolbar.length;i++){

					$scope.toolbar[i].visible = toChange;
					if($scope.toolbar[i].clickable&&!toChange){
						$scope.toolbar[i].clicked = toChange;
					}

			}
		}

};

