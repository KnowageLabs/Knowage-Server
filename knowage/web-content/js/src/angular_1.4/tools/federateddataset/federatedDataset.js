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
	
	//relationships
	$scope.selectedDatasets = [];
	$scope.multiRelationships = [];
	$scope.relationshipsJSON = [];
	
	//used to compare if all selected datasets are used in relationships
	$scope.sourceDatasetsUsedInRelations = [];
	ctr.listaNewDatasets = [];
	
	angular.element(document).ready(function () {
        $scope.getDataSets();
    });
	
	
	$scope.getDataSets = function() {
		
		sbiModule_restServices.promiseGet("2.0/datasets", "listNotDerivedDataset")
			.then(function(response) {
				ctr.list = response.data;
				angular.forEach(ctr.list, function(dataset){
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
						ctr.getFederationById();
						ctr.loadDatasetsEditMode();
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
			angular.forEach(ctr.listAllO, function(dataset){
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
					ctr.getFederationById();
					ctr.loadDatasetsEditMode();
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
		sbiModule_restServices.promiseGet("federateddataset/"+federation_id,"")
		  .then(function(response) {
		   
		   $scope.federateddataset.federation_id = response.data.federation_id;
		   $scope.federateddataset.name = response.data.name;
		   $scope.federateddataset.label = response.data.label;
		   $scope.federateddataset.description = response.data.description;
		   $scope.federateddataset.relationships = JSON.parse(response.data.relationships);
		   $scope.relationshipsJSON = JSON.parse(response.data.relationships);

		   //array of objects
		   var aoo = $scope.federateddataset.relationships;
		   
		   for (var key in aoo) {
			   for ( var obj in aoo[key]) {
				var eSourceTable = aoo[key][obj].sourceTable;
				var eDestinationTable = aoo[key][obj].destinationTable;
				var eSourceColumn = aoo[key][obj].sourceColumns;
			    var eDestinationColumn = aoo[key][obj].destinationColumns;
				   
				var editSingleRelation = eSourceTable.name.toUpperCase() + "." + eSourceColumn[0] + " -> " + eDestinationTable.name.toUpperCase() + "." + eDestinationColumn[0];
				
				$scope.sourceDatasetsUsedInRelations.push(eSourceTable.name);
				$scope.sourceDatasetsUsedInRelations.push(eDestinationTable.name);
				
				$scope.multiRelationships.push(editSingleRelation);
			}
		   }
		   
		   
		   
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		 });
	}
	
	$scope.goToFederationCatalogue = function() {
		if($window.frameElement.name==="angularIframe"){
			 $window.parent.angular.element(window.frameElement).scope().closeDialogFromExt();	
		}else{
		     $window.location.href = contextName + "/servlet/AdapterHTTP?ACTION_NAME=SELF_SERVICE_DATASET_START_ACTION&LIGHT_NAVIGATOR_RESET_INSERT=TRUE&MYDATA=TRUE&CALLBACK_FUNCTION=openFederation";
		}
	};
	
	ctr.areAllSelectedDatasetsUsed = function(lDS, lUDS) {
		for (var i = 0; i < lDS.length; i++) {
			if(lUDS.indexOf(lDS[i])==-1) {
				return false;
			}
		}
		return true;
	}
		
	ctr.showAdvanced = function(ev){

		if(!ctr.areAllSelectedDatasetsUsed(ctr.listaNewDatasets, $scope.sourceDatasetsUsedInRelations)) {
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.title(sbiModule_translate.load("sbi.generic.error"))
						.content(sbiModule_translate.load("sbi.federationdefinition.contain.all.selected.datasets"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
						.targetEvent(ev)
			);
		} else {
			ctr.clearSelections();
			$mdDialog
				.show({
					scope: $scope,
					preserveScope: true,
					controllerAs: 'feddsCtrl',
					controller: function($mdDialog) {
						var fdsctrl = this;
						fdsctrl.saveFedDataSet = function() {
							
							if($scope.federateddataset.hasOwnProperty("federation_id")){
								
								
								$scope.federateddataset.relationships = $scope.relationshipsJSON;
								
								sbiModule_restServices.put("federateddataset", $scope.federateddataset.federation_id, $scope.federateddataset)
								.success(
										function(data, status, headers, config) {
											
											if (data.hasOwnProperty("errors")) {
												
												console.log("[PUT]: DATA HAS ERRORS PROPERTY!");
																								
												sbiModule_messaging.showErrorMessage(data.errors[0].message, 'Error');
												
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
											sbiModule_messaging.showErrorMessage(data.errors[0].message, 'Error');
										}
								)
							}
							else {
								
																
								$scope.federateddataset.relationships = $scope.relationshipsJSON;
																
								sbiModule_restServices.post("federateddataset","post",angular.toJson($scope.federateddataset))
								.success(
										function(data, status, headers, config) {
											
											if (data.hasOwnProperty("errors")) {
												
												console.log("[POST]: DATA HAS ERRORS PROPERTY!");
												
												sbiModule_messaging.showErrorMessage(data.errors[0].message, 'Error');
												
											} else {
												
												console.log("[POST]: SUCCESS!");
												
												sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
												
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
		}
				
	}
	
	$scope.counter = function() {
		var count = 0;
		for (key in ctr.myselectedvariable) {
			if(ctr.myselectedvariable[key]!=null) {
					count++;
			}
		}
		return count;
	}
			
	$scope.isDSCountained= function(name){
		return $scope.selectedDatasets.indexOf(name) >=0;
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
	
		if(($scope.t.sourceTable.name=='' || $scope.t.sourceTable.name==listId)){
			$scope.t.sourceTable.name = listId;
			$scope.t.sourceTable.className = listId;
			$scope.t.sourceColumns = [];
			$scope.t.sourceColumns.push(item.alias);
			ctr.colorTheSelectedItem(listId,'sourceItem');

	
		} else if($scope.t.destinationTable.name=='' || $scope.t.destinationTable.name==listId) {
			$scope.t.destinationTable.name = listId;
			$scope.t.destinationTable.className = listId;
			$scope.t.destinationColumns = [];
			$scope.t.destinationColumns.push(item.alias);
			ctr.colorTheSelectedItem(listId, 'destItem');						
		} 
	}
	
	ctr.colorTheSelectedItem = function(listId, color) {
		$timeout(function() {
			var selectedItemStyle = angular.element(document.querySelector('#'+listId+' .selectedRow'));
			selectedItemStyle.addClass(color)
	    }, 250);
	}
	
	ctr.removeItemFromRelation = function(item, listId) {
		if(listId==$scope.t.sourceTable.name) {
			$scope.t.sourceTable.name = '';
			$scope.t.sourceTable.className = '';
			$scope.t.sourceColumns.splice(item.alias);
			
		} else if(listId==$scope.t.destinationTable.name) {
			$scope.t.destinationTable.name = '';
			$scope.t.destinationTable.className = '';
			$scope.t.destinationColumns.splice(item.alias);
			
		}
	}
		
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
				
				var relObj = $scope.relationshipsJSON[index];
								
				var destDS = relObj[0].destinationTable.name;
				var sourceDS = relObj[0].sourceTable.name;
				
				ctr.deleteDSFromSourceDS(destDS, sourceDS);
				
				$scope.relationshipsJSON.splice(index,1);
			}
						
		}, function() {
			console.log('Canceled');
		});
				
	}
		
	$scope.addJSONStructureToRelationshipsJSONAndMultiRelationships = function(t, singleRelation) {
		
		var tSourceTable = t.sourceTable;
		var tDestinationTable = t.destinationTable;
		
		var index = $scope.multiRelationships.indexOf(singleRelation);

		if(!(index in $scope.multiRelationships)) {
			
			var array = [];
			array.push(t);
			
			$scope.relationshipsJSON.push(array);
			$scope.multiRelationships.push(singleRelation);
			ctr.addToUsedDS(tSourceTable.name, tDestinationTable.name);
			ctr.clearSelections();
		} else {
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("Relationship already exists"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
			ctr.clearSelections();
		}
	}
	
	ctr.addSingleRelation = function() {
		
		ctr.tSourceTable = $scope.t.sourceTable;
		ctr.tDestinationTable = $scope.t.destinationTable;
		ctr.tSourceColumn = $scope.t.sourceColumns;
		ctr.tDestinationColumn = $scope.t.destinationColumns;
		
		if (ctr.tSourceColumn[0]==undefined && ctr.tDestinationColumn[0]==undefined) {
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("sbi.federationdefinition.no.field.selected"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		}
		else if (ctr.tSourceColumn[0]==undefined || ctr.tDestinationColumn[0]==undefined) {
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("sbi.federationdefinition.only.one.field.selected"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		}
		else if (ctr.tSourceColumn[0]!=undefined && ctr.tDestinationColumn[0]!=undefined) {
			$scope.oneRelation = ctr.tSourceTable.name.toUpperCase() + "." + ctr.tSourceColumn[0] + " -> " + ctr.tDestinationTable.name.toUpperCase() + "." + ctr.tDestinationColumn[0];
			$scope.addJSONStructureToRelationshipsJSONAndMultiRelationships($scope.t, $scope.oneRelation);
		}
	}
	
	ctr.addToUsedDS = function(sourceDS, destDS) {
		
		var indexA = $scope.sourceDatasetsUsedInRelations.indexOf(sourceDS);
		var indexB = $scope.sourceDatasetsUsedInRelations.indexOf(destDS);
		
		$scope.sourceDatasetsUsedInRelations.push(sourceDS);
		$scope.sourceDatasetsUsedInRelations.push(destDS);
	}
	
	ctr.deleteDSFromSourceDS = function(sourceDS, destDS){
		var indexA = $scope.sourceDatasetsUsedInRelations.indexOf(destDS);
		if(indexA!=-1) {
			$scope.sourceDatasetsUsedInRelations.splice(indexA,1);
		}
		var indexB = $scope.sourceDatasetsUsedInRelations.indexOf(sourceDS);
		if (indexB!=-1) {
			$scope.sourceDatasetsUsedInRelations.splice(indexB,1);
		}
	}

	ctr.addDataSetToArrayIfNotAlreadySelected = function(listId) {
		var index = $scope.selectedDatasets.indexOf(listId);
		if(!(index in $scope.selectedDatasets)) {
			$scope.selectedDatasets.push(listId);
		}
	}
	
	ctr.selectDeselect = function(item, listId){
		
		ctr.addDataSetToArrayIfNotAlreadySelected(listId);
		var index = $scope.selectedDatasets.indexOf(listId);
		if(index==0||index==1) {
			
			angular.forEach(ctr.listaNew, function(dataset){
				if(dataset.label==listId){
					angular.forEach(dataset.metadata.fieldsMeta, function(listField){
						
						if(listField.alias==item.alias){
							if(listField.selected==true){
								var index2 = $scope.selectedDatasets.indexOf(listId);
								$scope.selectedDatasets.splice(index2,1);
								listField.selected = false;
								ctr.myselectedvariable[listId] = null;
								ctr.removeClassesById(listId);
								ctr.removeItemFromRelation(item, listId);
								
								
								
							} else {
								angular.forEach(dataset.metadata.fieldsMeta, function(att){
									att.selected = false;
								});
								listField.selected = true;
								ctr.removeClassesById(listId);
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

			$scope.selectedDatasets.splice(index,1);
		}

		
	}
	
	ctr.removeClassesById = function(listId) {
		$timeout(function() {
			
			var selectedItemStyle = angular.element(document.querySelector('#'+listId+' .sourceItem'));
			selectedItemStyle.removeClass('sourceItem');
			
			var selectedItemStyle = angular.element(document.querySelector('#'+listId+' .destItem'));
			selectedItemStyle.removeClass('destItem');
			
	    }, 250);
	}
		
	ctr.kickOutFromListNew = function(param) {
		ctr.usedDatasets = [];
		for (var i = 0; i <$scope.relationshipsJSON.length; i++) {
			for (var j = 0; j < $scope.relationshipsJSON[i].length; j++) {
				if(j==0){
					ctr.usedDatasets.push($scope.relationshipsJSON[i][j].sourceTable.name)
					ctr.usedDatasets.push($scope.relationshipsJSON[i][j].destinationTable.name)
				} else {
					ctr.usedDatasets.push($scope.relationshipsJSON[i][j].destinationTable.name)
				}
				
			}
		}
			if(ctr.usedDatasets==0){
				var index = ctr.listaNew.indexOf(param);
				var indexLND = ctr.listaNewDatasets.indexOf(param.label);
				if (index != -1) {
					ctr.listaNew.splice(index, 1);
					ctr.listaNewDatasets.splice(indexLND,1);
				}
				if (ctr.list.indexOf(param) === -1) {
					ctr.list.push(param);
				} else {
					console.log("Parameter is already in the list.");
				}
			} else {
				if (ctr.usedDatasets.indexOf(param.label) >= 0) {
					
					$mdDialog
							.show($mdDialog
									.alert()
									.clickOutsideToClose(true)
									.content(sbiModule_translate.load("sbi.federationdefinition.cant.delete.dataset"))
									.ok(sbiModule_translate.load("sbi.federationdefinition.ok")));
					return false;
					
				} else {
					var index = ctr.listaNew.indexOf(param);
					var indexLND = ctr.listaNewDatasets.indexOf(param.label);
					if (index != -1) {
						ctr.listaNew.splice(index, 1);
						ctr.listaNewDatasets.splice(indexLND,1);
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
		if(index != -1) {
			ctr.list.splice(index,1);
		}
		if(ctr.listaNew.indexOf(param)===-1){
			ctr.listaNew.push(param);
			ctr.listaNewDatasets.push(param.label);
			
		} else {
			console.log("Parametar is already in the list.")
		}
	}
	
	ctr.toggle = function() {
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
		}
	}
	
	ctr.toggleBack = function() {
		ctr.state=!ctr.state;
	}
	
	ctr.kickOutFromAssociationArray = function(param) {//ispitati
		var index = ctr.associationArr.indexOf(param);
		if(index != -1){
			ctr.associationArray.splice(index, 1);
		}
	}
	
	
	ctr.hide = function(){
		$mdDialog.hide();
	}
	
	
	ctr.showAlert = function(ev){ 
		$mdDialog.show(
				$mdDialog.alert()
					.clickOutsideToClose(true)
					.title(sbiModule_translate.load("sbi.federationdefinition.operation.succeded"))
					.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
					.targetEvent(ev)
		);
	}
	
	ctr.showError = function(ev){ 
		$mdDialog.show(
				$mdDialog.alert()
					.clickOutsideToClose(true)
					.title(sbiModule_translate.load("sbi.federationdefinition.operation.failed"))
					.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
					.targetEvent(ev)
		);
	}
	
	ctr.removeDatasetFromListaNew = [ 			 		               	
		 		               	{
		 		               		label: sbiModule_translate.load("sbi.federationdefinition.delete"),
		 		               		icon:"fa fa-trash-o",
		 		               		backgroundColor:'red',
		 		               		action : function(param) {
		 		               				ctr.kickOutFromListNew(param);
		 		               			}
		 		               	}
		 		             ];

	ctr.showDatasetInfo = [ 			 		               	
		 		               	{
		 		               		label: sbiModule_translate.load("sbi.federationdefinition.info"),
		 		               		icon:"fa fa-info-circle",
		 		               		backgroundColor:'green',
		 		               		action : function(param) {
		 		               				ctr.showDSDetails(param);
		 		               			}
		 		               	}
		 		             ];
	
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
				$event: param
			});
	}
	
	//FAB Speed Dial customization for deleting and editing a relationship
	ctr.selectedDirection = 'left';
    ctr.selectedMode = 'md-scale';
    
    
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
        					ctr.listaNewDatasets.push(ctr.listAllO[j].label);        					
        				} else {
        					
        				}
        			}
    			}
        	}
    }
	
	ctr.removeAllClasses = function() {
		var selectedItemStyle = angular.element(document.querySelector('.sourceItem'));
		selectedItemStyle.removeClass('sourceItem');
		var selectedItemStyle = angular.element(document.querySelector('.destItem'));
		selectedItemStyle.removeClass('destItem');
	}
	
	ctr.clearSelections = function() {
		ctr.removeAllClasses();
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
		var index = $scope.multiRelationships.indexOf(k);
		if(index!=-1) {
			var param = $scope.relationshipsJSON[index];
			//ctr.retrieveSelections(param);
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
		
		for (var i = 0; i < ctr.listaNew.length; i++) {
			for (var key in json) {
			  if (json.hasOwnProperty(key)) {
				
				if(key==ctr.listaNew[i].label){

					for (var z = 0; z < ctr.listaNew[i].metadata.fieldsMeta.length; z++) {
						if(json[key]==ctr.listaNew[i].metadata.fieldsMeta[z].name){
							ctr.myselectedvariable[ctr.listaNew[i].name] = ctr.listaNew[i].metadata.fieldsMeta[z];
							var selected = ctr.listaNew[i].metadata.fieldsMeta[z].selected;
							selected = true;
							//color and highlight the item
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
