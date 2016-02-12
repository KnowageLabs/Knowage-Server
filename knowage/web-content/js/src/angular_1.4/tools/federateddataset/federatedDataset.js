/**
 * @author Simovic Nikola (nikola.simovic@mht.net)
 */
var app = angular
.module
('federationDefinitionModule',
		[
		 'ngMaterial',
		 'angular_list',
		 'sbiModule',
		 ]
);

app
.controller
('federationDefinitionCTRL',
		[
		 "$location",
		 "$window",
		 "$scope",
		 "$mdDialog", 
		 "$timeout",
		 "sbiModule_translate",
		 "sbiModule_restServices",
		 "sbiModule_messaging",
		 federationDefinitionFunction
		 ]
);

function federationDefinitionFunction
(
		$location,
		$window,
		$scope,
		$mdDialog, 
		$timeout,
		sbiModule_translate, 
		sbiModule_restServices,
		sbiModule_messaging
) {
			
	//current scope
	ctr = this;
	
	//creating translate variable that is accessible from the global scope, and can be used as an expression inside html
	$scope.translate = sbiModule_translate;

	//data from the fields of saveFederateddataset.html
	$scope.federateddataset = {};
	$scope.update = {};
	$scope.update = $scope.federateddataset;
	angular.toJson($scope.update);
	
	//lists that will be filled after the sbiModule_restServices.get call
	ctr.list = {};
	ctr.listaNew = [];
	ctr.listAllO = {};
	ctr.listAll = [];

	//used to check if ctr.list and ctr.listALLO are loaded
	ctr.loadedList = false;
	ctr.loadedListAllO = false; 	
	
	//state is used to show or hide components on the page
	ctr.state = true;
	
	//used for the JSON structure of a relationship
	ctr.relation = "";
	ctr.relNew = null;
	ctr.associationArray = [];
	ctr.beforeRel = {};
	ctr.bla = {}
	
	//used for highlighting dataset fields 
	ctr.item = {};

	angular.element(document).ready(function () {
        $scope.getDataSets();
    });
	
	
	$scope.getDataSets = function() {
		
		sbiModule_restServices.promiseGet("2.0/datasets", "listNotDerivedDataset")
			.then(function(response) {
				ctr.list = response.data;
				console.log("List:")
				console.log(ctr.list)
				angular.forEach(ctr.list, function(dataset){
					////Fix for --> TypeError: Cannot read property 'fieldsMeta' of null
					if(dataset.metadata==null){
						dataset.metadata.fieldsMeta = [];
					}
					angular.forEach(dataset.metadata.fieldsMeta, function(listField){
						
						listField.selected =  false;
					});
				});
				
				ctr.loadedList = true;
				if(ctr.loadedListAllO==true && ctr.loadedList==true) {
					if(value!=0) {
						ctr.loadDatasetsEditMode();
						ctr.getFederationById();
					}						
				} else {
					console.log("Only loadedList is loaded")
				}
			},
			function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		  });
		
		sbiModule_restServices.promiseGet("2.0/datasets", "listNotDerivedDataset")
		.then(function(response) {
			ctr.listAllO = response.data;
			console.log("ListALLO:")
			console.log(ctr.listAllO)
			angular.forEach(ctr.listAllO, function(dataset){
				//Fix for --> TypeError: Cannot read property 'fieldsMeta' of null
				if(dataset.metadata==null){
					dataset.metadata.fieldsMeta = [];
				}
				angular.forEach(dataset.metadata.fieldsMeta, function(listField){
					listField.selected =  false;
				});
			});

			ctr.loadedListAllO = true;
			if(ctr.loadedListAllO==true && ctr.loadedList==true) {
				if(value!=0) {
					ctr.loadDatasetsEditMode();
					ctr.getFederationById();
				}
			} else {
				console.log("Only loadedListAllO is loaded")
			}
		},
		function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
	  });		
	}
	
	ctr.getFederationById = function(){
		sbiModule_restServices.promiseGet("2.0/federateddataset/"+federation_id,"")
		  .then(function(response) {
		   console.log("KALABUNGAAAAAAA")
		   console.log(response.data);
		   
		   $scope.federateddataset.federation_id = response.data.federation_id;
		   $scope.federateddataset.name = response.data.name;
		   $scope.federateddataset.label = response.data.label;
		   $scope.federateddataset.description = response.data.description;
		   $scope.relationshipsJSON = response.data.relationships;
		   console.log("relation")
		   console.log($scope.relationshipsJSON)
		   $scope.federateddataset.relationships = JSON.parse($scope.relationshipsJSON);
		   console.log("json formatted")
		   console.log($scope.federateddataset.relationships);
		   
		   //array of objects
		   var aoo = $scope.federateddataset.relationships;
		   
		   for (var key in aoo) {
			   for ( var obj in aoo[key]) {
				var eSourceTable = aoo[key][obj].sourceTable;
				var eDestinationTable = aoo[key][obj].destinationTable;
				var eSourceColumn = aoo[key][obj].sourceColumns;
			    var eDestinationColumn = aoo[key][obj].destinationColumns;
				   
				var editSingleRelation = eSourceTable.name.toUpperCase() + "." + eSourceColumn[0] + " -> " + eDestinationTable.name.toUpperCase() + "." + eDestinationColumn[0];
				$scope.multiRelationships.push(editSingleRelation);
			}
		   }
		   
		   
		   
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		 });
	}
	
	$scope.goToFederationCatalogue = function() {
		 $window.location.href = contextName + "/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=TRUE&CALLBACK_FUNCTION=openFederation";
	};
	
	ctr.showAdvanced = function(ev){
		
		/*if(value){
			ctr.SaveOrUpdate = "Update";
		} else {
			ctr.SaveOrUpdate = "Save";
		}*/
		
		/*ctr.multiArrayDatasets = [];
		ctr.listaNewDatasets = [];
		ctr.index = "";
		for (var i = 0; i < ctr.listaNew.length; i++) {
			ctr.listaNewDatasets.push(ctr.listaNew[i].label)
		}
		console.log(ctr.listaNewDatasets)
		for (var i = 0; i < ctr.multiArray.length; i++) {
			for (var j = 0; j < ctr.multiArray[i].length; j++) {
				if(j==0){
					ctr.multiArrayDatasets.push(ctr.multiArray[i][j].sourceTable.name)
					ctr.multiArrayDatasets.push(ctr.multiArray[i][j].destinationTable.name)
				} else {
						ctr.multiArrayDatasets.push(ctr.multiArray[i][j].destinationTable.name)					
				}
			}
		}
		for (var a = 0; a < ctr.listaNewDatasets.length; a++) {
			ctr.index = ctr.multiArrayDatasets.indexOf(ctr.listaNewDatasets[a])	
		}				
		if(ctr.multiArray.length==0){
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("sbi.federationdefinition.no.relation.created"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		} 
		else if(ctr.index==-1){
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("sbi.federationdefinition.contain.all.selected.datasets"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		}		
		else{*/
			ctr.clearSelections()
			$mdDialog
				.show({
					scope: $scope,
					preserveScope: true,
					controllerAs: 'feddsCtrl',
					controller: function($mdDialog) {
						var fdsctrl = this;
						fdsctrl.saveFedDataSet = function() {
							
							/*var item = {};
							item.name = $scope.update.name;
							item.label = $scope.update.label;
							item.description = $scope.update.description;
							item.relationships = "";
							item.relationships = ctr.multiArray;*/
							
							if($scope.federateddataset.hasOwnProperty("federation_id")){
								
								var myJsonString = JSON.stringify($scope.relationshipsJSON);
								$scope.federateddataset.relationships = myJsonString;
								
								sbiModule_restServices.put("2.0/federateddataset", $scope.federateddataset.federation_id, angular.toJson($scope.federateddataset))
								.success(
										function(data, status, headers, config) {
											
											if (data.hasOwnProperty("errors")) {
												
												console.log("[PUT]: DATA HAS ERRORS PROPERTY!");
												
												console.log(data.errors);
												
												ctr.showError();
												
											} else {
												
												console.log("[PUT]: SUCCESS!");
														
												sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
												
												$timeout(function() {
													$scope.goToFederationCatalogue();
											    }, 3000);
																								
											}
												
										}
										
								)
								.error(
										function(data, status, headers, config) {
											console.log("error")
										}
								)
							}
							else {
								
								//var myJsonString = JSON.stringify($scope.relationshipsJSON);
								
								$scope.federateddataset.relationships = $scope.relationshipsJSON;
																
								sbiModule_restServices.post("federateddataset","post",angular.toJson($scope.federateddataset))
								.success(
										function(data, status, headers, config) {
											
											if (data.hasOwnProperty("errors")) {
												
												console.log("[POST]: DATA HAS ERRORS PROPERTY!");
												
												sbiModule_messaging.showErrorMessage(data.errors[0].message, 'Error');
												
											} else {
												
												console.log("[POST]: SUCCESS!");
												
												sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
												
												$timeout(function() {
													$scope.goToFederationCatalogue();
											    }, 3000);
												
												
											}
											
											
										}
										
								)
								.error(
										function(data, status, headers, config) {
											sbiModule_messaging.showErrorMessage(data.errors[0].message, 'Error');
										}
								)
							}
							
						}
					},
					templateUrl: '/knowage/js/src/angular_1.4/tools/federateddataset/commons/templates/saveFederatedDatasetTemp.html',
					targetEvent: ev
				});
		//}
	}
	
	ctr.showDSDetails = function(param) {
		
		angular.forEach(ctr.list, function(dataset){
			if(dataset.name==param.name && dataset.label==param.label && dataset.description==param.description){
				$scope.dsname = dataset.name;
				$scope.dslabel = dataset.label;
				$scope.dsdescription = dataset.description;
			}
		});
					
		$mdDialog
			.show({
				scope: $scope,
				preserveScope: true,				
				templateUrl: '/knowage/js/src/angular_1.4/tools/federateddataset/commons/templates/datasetDetails.html',
				targetEvent: param
			});
	}
	
	$scope.counter = function() {
		var count = 0;
		for (key in ctr.myselectedvariable) {
			if(ctr.myselectedvariable[key]!=null) {
				count++
			}
		}
		return count;
	}
	
	$scope.selectedDatasets = [];
		
	$scope.isDSCountained= function(label){
		return $scope.selectedDatasets.indexOf(label) >=0;
	}
	
	$scope.t = {
			bidirectional: true,
	        cardinality: 'many-to-one',
	        sourceTable: {
	            name: '',
	            className: ''
	        },
	        sourceColumns: [],
	        destinationTable: {
	            name: '',
	            className: ''
	        }, 
	        destinationColumns: []
		}
	
	ctr.additemToRelation = function(item, listId){
	
		if($scope.counter()==0 || ($scope.counter()==1 && $scope.t.sourceTable.name=='')){
			$scope.t.sourceTable.name = listId;
			$scope.t.sourceTable.className = listId;
			$scope.t.sourceColumns.push(item.name);
			console.log($scope.t);
		} else if($scope.counter()==1) {
			$scope.t.destinationTable.name = listId;
			$scope.t.destinationTable.className = listId;
			$scope.t.destinationColumns.push(item.name);
			console.log($scope.t);
		} else if($scope.counter()==2){
			
			$scope.t.destinationTable.name = '';
			$scope.t.destinationTable.className = '';
			$scope.t.destinationColumns.splice(0,1);
			
			$scope.t.destinationTable.name = listId;
			$scope.t.destinationTable.className = listId;
			$scope.t.destinationColumns.push(item.name);
			console.log($scope.t);
		}
		
	}
	
	ctr.removeItemFromRelation = function(item, listId) {
		if(listId==$scope.t.sourceTable.name) {
			$scope.t.sourceTable.name = '';
			$scope.t.sourceTable.className = '';
			$scope.t.sourceColumns.splice(item.name);
			
		} else if(listId==$scope.t.destinationTable.name) {
			$scope.t.destinationTable.name = '';
			$scope.t.destinationTable.className = '';
			$scope.t.destinationColumns.splice(item.name);
			
		}
	}
	
	$scope.multiRelationships = [];
	$scope.relationshipsJSON = [];
	
	ctr.deleteRelationship = function(item) {
		
		var confirm = $mdDialog
		.confirm()
		.title(sbiModule_translate.load("sbi.federationdefinition.confirm.delete"))
		.content(
				sbiModule_translate
				.load("sbi.federationdefinition.confirm.delete.content"))
				.ariaLabel('Lucky day').ok(
						sbiModule_translate.load("sbi.general.continue")).cancel(
								sbiModule_translate.load("sbi.general.cancel"));

		$mdDialog.show(confirm).then(function() {

			var index = $scope.multiRelationships.indexOf(item);
			if(index!=-1) {
				$scope.multiRelationships.splice(index,1);
				$scope.relationshipsJSON.splice(index,1);
			}
						
		}, function() {
			console.log('Canceled');
		});
				
	}
	
	$scope.addJSONStructureToRelationshipsJSONAndMultiRelationships = function(t, singleRelation) {
		
		var index = $scope.multiRelationships.indexOf(singleRelation);

		if(!(index in $scope.multiRelationships)) {
			
			var nesto = [];
			nesto.push(t);
			
			$scope.relationshipsJSON.push(nesto);
			

			$scope.multiRelationships.push(singleRelation);
			ctr.clearSelections();
		} else {
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("Relationship already exists"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		}
	}
	
	ctr.addSingleRelation = function() {
		
		ctr.tSourceTable = $scope.t.sourceTable;
		ctr.tDestinationTable = $scope.t.destinationTable;
		ctr.tSourceColumn = $scope.t.sourceColumns;
		ctr.tDestinationColumn = $scope.t.destinationColumns;

		if($scope.counter()<2) {
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("sbi.federationdefinition.only.one.field.selected"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		}
		if($scope.counter()==2) {
			$scope.oneRelation = ctr.tSourceTable.name.toUpperCase() + "." + ctr.tSourceColumn[0] + " -> " + ctr.tDestinationTable.name.toUpperCase() + "." + ctr.tDestinationColumn[0];
			$scope.addJSONStructureToRelationshipsJSONAndMultiRelationships($scope.t, $scope.oneRelation);
		}
	}

	ctr.addDataSetToArrayIfNotAlreadySelected = function(listId) {
		var index = $scope.selectedDatasets.indexOf(listId);
		if(!(index in $scope.selectedDatasets)) {
			$scope.selectedDatasets.push(listId);
			console.log($scope.selectedDatasets)
		}
	}
	
	ctr.selectDeselect = function(item, listId){
		ctr.addDataSetToArrayIfNotAlreadySelected(listId);
		var index = $scope.selectedDatasets.indexOf(listId);
		if(index==0||index==1) {
			
			angular.forEach(ctr.listaNew, function(dataset){
				if(dataset.label==listId){
					angular.forEach(dataset.metadata.fieldsMeta, function(listField){
						if(listField.name==item.name){
							if(listField.selected==true){
								var index2 = $scope.selectedDatasets.indexOf(listId);
								$scope.selectedDatasets.splice(index2,1);
								console.log($scope.selectedDatasets);
								listField.selected = false;
								ctr.myselectedvariable[listId] = null;
								
								console.log($scope.selectedDatasets)
								console.log("Field is unhighlighted.")
								ctr.removeItemFromRelation(item, listId);
								console.log($scope.t);
								
							} else {
								angular.forEach(dataset.metadata.fieldsMeta, function(att){
									att.selected = false;
								});
								listField.selected = true;
								ctr.additemToRelation(item, listId);
							}
						} else {
							//listField.name==item.name
						}
					});
				} else {
					//dataset.label==listId
				}
			});
			
			
		} else {
			
			console.log("2 files are already selected")
			$scope.selectedDatasets.splice(index,1);
			console.log($scope.selectedDatasets)
			console.log(index);
			console.log($scope.t);
		}

		
	}
	
	
		
	ctr.kickOutFromListNew = function(param) {
		ctr.nizSourceva = [];
		for (var i = 0; i < ctr.multiArray.length; i++) {
			for (var j = 0; j < ctr.multiArray[i].length; j++) {
				if(j==0){
					ctr.nizSourceva.push(ctr.multiArray[i][j].sourceTable.name)
					ctr.nizSourceva.push(ctr.multiArray[i][j].destinationTable.name)
				} else {
					ctr.nizSourceva.push(ctr.multiArray[i][j].destinationTable.name)
				}
				
			}
		}
			console.log(ctr.nizSourceva.length)
			if(ctr.nizSourceva==0){
				var index = ctr.listaNew.indexOf(param);
				if (index != -1) {
					ctr.listaNew.splice(index, 1);
				}
				if (ctr.list.indexOf(param) === -1) {
					ctr.list.push(param);
				} else {
					console.log("Parameter is already in the list.");
				}
			} else {
				if (ctr.nizSourceva.indexOf(param.label) >= 0) {
					
					console.log("param leb")
					console.log(param.label)
					$mdDialog
							.show($mdDialog
									.alert()
									.clickOutsideToClose(true)
									.content(sbiModule_translate.load("sbi.federationdefinition.cant.delete.dataset"))
									.ok(sbiModule_translate.load("sbi.federationdefinition.ok")));
					return false;
					
				} else {
					console.log("else" + j)
					var index = ctr.listaNew.indexOf(param);
					if (index != -1) {
						ctr.listaNew.splice(index, 1);
					}
					if (ctr.list.indexOf(param) === -1) {
						ctr.list.push(param);
					} else {
						console.log("Parameter is already in the list.");
					}
				}
			}	
	}
	
	ctr.moveToListNew = function(param) {		
		var index = ctr.list.indexOf(param);
		console.log("param");
		console.log(param)
		if(index != -1) {
			ctr.list.splice(index,1);
		}
		if(ctr.listaNew.indexOf(param)===-1){
			ctr.listaNew.push(param);
			console.log("dodao u novu listu")
		} else {
			console.log("Parametar is already in the list.")
		}
	}
	
	ctr.toggle = function() {
		console.log("STATE1")
		console.log(ctr.state)
		if(ctr.listaNew.length==0){
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content('You didn\'t select any datasets!')
						.ok('OK')
			);
		} else if (ctr.listaNew.length==1){
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content('Select at least two datasets!')
						.ok('Ok')
			);
		} else {
			ctr.state=!ctr.state;
			console.log("STATE2")
			console.log(ctr.state)
		}
	}
	
	ctr.toggleBack = function() {
		console.log("STATE b 1")
		console.log(ctr.state)
		ctr.state=!ctr.state;
		console.log("STATE b 2")
		console.log(ctr.state)
	}
	
	ctr.kickOutFromAssociationArray = function(param) {//ispitati
		var index = ctr.associationArr.indexOf(param);
		if(index != -1){
			ctr.associationArray.splice(index, 1);
		}
	}
	
	ctr.deleteFromMultiArray = function(param) {
				
		var confirm = $mdDialog
		.confirm()
		.title(sbiModule_translate.load("sbi.federationdefinition.confirm.delete"))
		.content(
				sbiModule_translate
				.load("sbi.federationdefinition.confirm.delete.content"))
				.ariaLabel('Lucky day').ok(
						sbiModule_translate.load("sbi.general.continue")).cancel(
								sbiModule_translate.load("sbi.general.cancel"));

		$mdDialog.show(confirm).then(function() {
			var index = ctr.multiArray.indexOf(param);
			if(index !=-1){
				ctr.multiArray.splice(index, 1);
			}	

		}, function() {
			console.log('Canceled');
		});
				
	}
	
	ctr.hide = function(){
		$mdDialog.hide();
	}
	
	
	ctr.showAlert = function(ev){ //premesti u saveFedDataSet
		$mdDialog.show(
				$mdDialog.alert()
					.clickOutsideToClose(true)
					.title(sbiModule_translate.load("sbi.federationdefinition.operation.succeded"))
					.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
					.targetEvent(ev)
		);
	}
	
	ctr.showError = function(ev){ //premesti u saveFedDataSet
		$mdDialog.show(
				$mdDialog.alert()
					.clickOutsideToClose(true)
					.title(sbiModule_translate.load("sbi.federationdefinition.operation.failed"))
					.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
					.targetEvent(ev)
		);
	}
	
	ctr.fdsSpeedMenuOpt = [ 			 		               	
		 		               	{
		 		               		label: sbiModule_translate.load("sbi.federationdefinition.delete"),
		 		               		icon:"fa fa-trash-o",
		 		               		backgroundColor:'red',
		 		               		action : function(param) {
		 		               			ctr.kickOutFromListNew(param);
		 		               			}
		 		               	}
		 		             ];

	ctr.fdsSpeedMenuOptAD = [ 			 		               	
		 		               	{
		 		               		label: sbiModule_translate.load("sbi.federationdefinition.info"),
		 		               		icon:"fa fa-info-circle",
		 		               		backgroundColor:'green',
		 		               		action : function(ev) {
		 		               				ctr.showDSDetails(ev);
		 		               			}
		 		               	}
		 		             ];
	
	//FAB Speed Dial customization for deleting and editing a relationship
	ctr.selectedDirection = 'left';
    ctr.selectedMode = 'md-scale';
    
    /*ctr.prepRelForEdit = function(param) {
    	var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.federationdefinition.confirm.dialog"))
		.content(sbiModule_translate.load("sbi.federationdefinition.confirm.dialog.edit.relation"))
		.targetEvent(param)
		.ok(sbiModule_translate.load("sbi.federationdefinition.yes"))
		.cancel(sbiModule_translate.load("sbi.federationdefinition.no"))
		
		$mdDialog.show(confirm).then(function(){
			ctr.isEditState = true;
	    	ctr.listaNew = [];
	    	ctr.glupalista = [];
	    	
	    	for (var int = 0; int < param.length; int++) {
				if(int==0){
					ctr.glupalista.push(param[int].sourceTable.name);
					ctr.glupalista.push(param[int].destinationTable.name);
				} else{
					ctr.glupalista.push(param[int].destinationTable.name);
				}
			}
	    	
	    	for (var int = 0; int < ctr.glupalista.length; int++) {
	    		angular.forEach(ctr.listAllO, function(dataset){
	        		if(dataset.name==ctr.glupalista[int]){
	        			console.log(dataset)
	        			ctr.listaNew.push(dataset);
	        		}
	        	});
			}
	    	
	    	var index = ctr.multiArray.indexOf(param);
			if(index !=-1){
				ctr.multiArray.splice(index, 1);
			}
		})
    }
    
    ctr.saveEditedRelation = function() {
    	ctr.isEditState = false;
    	ctr.multiArray.push(ctr.createAssociations());
    }
    
    ctr.cancelEdit = function() {
    	ctr.isEditState = false;
    }*/
    
	ctr.loadDatasetsEditMode = function(){

        	if(value!=null){
        		var arr = value.replace(/ /g,'')
        		var array = arr.substring(1,arr.length-1);
        		var array1 = array.split(',');
        		for (var i = 0; i < array1.length; i++) {
    				for (var j = 0; j < ctr.listAllO.length; j++) {
        				if(ctr.listAllO[j].label==array1[i]){
        					for (var k = 0; k < ctr.list.length; k++) {
    							if(ctr.list[k].name==ctr.listAllO[j].name) {
    								var index = ctr.list.indexOf(ctr.list[k]);
    								if(index != -1) {
    									ctr.list.splice(index,1);
    								}
    							}
    						}
        					ctr.listaNew.push(ctr.listAllO[j])
        				} else {
        					console.log("no dataset like that in ALL DATASETS")
        				}
        			}
    			}
        	}
			ctr.multiArray = JSON.parse(valueRelString);
    }
	
	ctr.clearSelections = function() {
		$scope.t = {
				bidirectional: true,
		        cardinality: 'many-to-one',
		        sourceTable: {
		            name: '',
		            className: ''
		        },
		        sourceColumns: [],
		        destinationTable: {
		            name: '',
		            className: ''
		        }, 
		        destinationColumns: []
			}
		$scope.selectedDatasets = [];
		ctr.myselectedvariable = {};
		angular.forEach(ctr.listaNew, function(dataset){
			angular.forEach(dataset.metadata.fieldsMeta, function(listField){
				if(listField.selected==true){
					listField.selected=false;
				}
			});
		});
		
	}
	
	ctr.retrieveSelectionsString = function(k) {
		console.log(k)
		var index = $scope.multiRelationships.indexOf(k);
		if(index!=-1) {
			var param = $scope.relationshipsJSON[index];
			console.log(param)
			var param2 = [];
			param2.push(param);
			ctr.retrieveSelections(param2);
		}
	}
	
	ctr.retrieveSelections = function(param) {		
		
		for (var i = 0; i < param.length; i++) {
			if(i==0){
				var key1 = param[i].sourceTable.name;
				var key2 = param[i].destinationTable.name
				var json = { };
				json[key1] = param[i].sourceColumns[0];
				json[key2] = param[i].destinationColumns[0];
				
			} else {
				var key3 = param[i].destinationTable.name
				json[key3] = param[i].destinationColumns[0];
							
			}
		}	
				
		ctr.applySelections(json)
		
	}
	
	ctr.applySelections = function (json) {
		ctr.myselectedvariable = {};
		console.log("*********")
		console.log(json)
		console.log("*********")
		
		for (var i = 0; i < ctr.listaNew.length; i++) {
			for (var key in json) {
			  if (json.hasOwnProperty(key)) {
				
				if(key==ctr.listaNew[i].label){
					console.log(json[key])
					console.log("Pronadjen"+key)
					
					for (var z = 0; z < ctr.listaNew[i].metadata.fieldsMeta.length; z++) {
						if(json[key]==ctr.listaNew[i].metadata.fieldsMeta[z].name){
							console.log("pronadjen i item i dataset")
							console.log(ctr.listaNew[i].metadata.fieldsMeta[z])
							ctr.myselectedvariable[ctr.listaNew[i].name] = ctr.listaNew[i].metadata.fieldsMeta[z];
							ctr.listaNew[i].metadata.fieldsMeta[z].selected=true;
						}
					}
				}
			  }
		   }			
		}
	}
	
}

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);
