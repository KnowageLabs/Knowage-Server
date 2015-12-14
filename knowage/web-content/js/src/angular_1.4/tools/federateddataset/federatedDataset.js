var app = angular.module('federationDefinitionModule', ['ngMaterial','angular_list','sbiModule']);


app.controller('federationDefinitionCTRL', ["$scope",
                   		"$mdDialog", "$timeout", "sbiModule_translate","sbiModule_restServices",funkcija]);

function funkcija($scope, $mdDialog, $timeout, sbiModule_translate, sbiModule_restServices) {
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
	ctr.multiArray = [];
	ctr.bla = {}
	
	//used for highlighting dataset fields 
	ctr.item = {};
	ctr.selectedVariable = {};
	ctr.myselectedvariable = {};
	
	ctr.fillTheArray = function() {
		
		var check = false;
		var obj1 = ctr.createAssociations();
		var counter = 0;
		
		angular.forEach(ctr.listaNew, function(dataset){
			angular.forEach(dataset.metadata.fieldsMeta, function(listField){
				if(listField.selected==true){
					counter +=1;
				}
			});
		});
		
		if(counter==0){
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("sbi.federationdefinition.no.field.selected"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		} else if(counter==1){
			$mdDialog.show(
					$mdDialog.alert()
						.clickOutsideToClose(true)
						.content(sbiModule_translate.load("sbi.federationdefinition.only.one.field.selected"))
						.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
			);
		} else{
			if(ctr.multiArray.length==0){
				ctr.multiArray.push(ctr.createAssociations());
				ctr.clearSelections();
			} else {
				angular.forEach(ctr.multiArray, function(obj2){
					if(JSON.stringify(obj1) === JSON.stringify(obj2)){
						check = true;
						console.log("The relation is already created!")
						$mdDialog.show(
								$mdDialog.alert()
									.clickOutsideToClose(true)
									.content(sbiModule_translate.load("sbi.federationdefinition.relation.exists"))
									.ok(sbiModule_translate.load("sbi.federationdefinition.ok"))
						);
					}
				});
				if(!check){
					console.log("Add new to array.")
					ctr.multiArray.push(ctr.createAssociations());
					ctr.clearSelections();
				}
			}
		}
		
		
	}
	
	ctr.createAssociations = function(){
		var RelationshipsArray = [];
		var checkBranch = false;
		angular.forEach(ctr.listaNew, function(dataset){
			if(checkBranch==false){
				console.log(checkBranch+"branch");
				angular.forEach(dataset.metadata.fieldsMeta, function(listField){
					if(listField.selected){
						console.log('selected field');
						ctr.beforeRel = dataset;
						ctr.beforeRel.firstSelectedListField = listField.name;
						ctr.beforeRel.ime = dataset.label;
						checkBranch = true;
					}
				});
			} else {
				console.log(checkBranch+"branch");
				angular.forEach(dataset.metadata.fieldsMeta, function(polje){
					if(polje.selected){
						var t = {
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
						
						t.sourceTable.name = ctr.beforeRel.ime;
						t.sourceTable.className = ctr.beforeRel.ime;
						t.sourceColumns.push(ctr.beforeRel.firstSelectedListField);
						
						t.destinationTable.name = dataset.label;
						t.destinationTable.className = dataset.label;
						t.destinationColumns.push(polje.name);
						
						ctr.beforeRel = polje;
						ctr.beforeRel.ime = dataset.label;
						ctr.beforeRel.firstSelectedListField = polje.name;
						
						RelationshipsArray.push(t);
					}
				});
			}
		});
		return RelationshipsArray;
	}
	
	ctr.showAdvanced = function(ev){
		
		/*if(value){
			ctr.SaveOrUpdate = "Update";
		} else {
			ctr.SaveOrUpdate = "Save";
		}*/
		
		ctr.multiArrayDatasets = [];
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
		else{
			ctr.clearSelections()
			$mdDialog
				.show({
					scope: $scope,
					preserveScope: true,
					controllerAs: 'feddsCtrl',
					controller: function($mdDialog) {
						var fdsctrl = this;
						fdsctrl.saveFedDataSet = function() {
							
							var item = {};
							item.name = $scope.update.name;
							item.label = $scope.update.label;
							item.description = $scope.update.description;
							item.relationships = "";
							item.relationships = ctr.multiArray;
							
							if(item.hasOwnProperty("federation_id")){
								
								sbiModule_restServices.put("2.0/federateddataset", item.federation_id ,angular.toJson(item))
								.success(
										function(data, status, headers, config) {
											
											if (data.hasOwnProperty("errors")) {
												
												console.log("[PUT]: DATA HAS ERRORS PROPERTY!");
												
												ctr.showError();
												
											} else {
												
												console.log("[PUT]: SUCCESS!");
														
												ctr.showAlert();
												
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
								
								sbiModule_restServices.post("federateddataset","post",angular.toJson(item))
								.success(
										function(data, status, headers, config) {
											
											if (data.hasOwnProperty("errors")) {
												
												console.log("[POST]: DATA HAS ERRORS PROPERTY!");
												
												ctr.showError();
												
											} else {
												
												console.log("[POST]: SUCCESS!");
														
												ctr.showAlert();
												
											}
											
											
										}
										
								)
								.error(
										function(data, status, headers, config) {
											console.log("error")
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

	ctr.selectDeselect = function(item, listId){
		console.log(item)
		if(ctr.myselectedvariable[listId]!=undefined || ctr.myselectedvariable[listId]!=null){
			if(item.name==ctr.myselectedvariable[listId].name){
				ctr.myselectedvariable[listId] = null;
				console.log("Field is unhighlighted.")
			}
		}
		angular.forEach(ctr.listaNew, function(dataset){
			if(dataset.label==listId){
				angular.forEach(dataset.metadata.fieldsMeta, function(listField){
					if(listField.name==item.name){
						if(listField.selected==true){
							listField.selected = false;
							ctr.selectedVariable = null; //chechk this
						} else {
							angular.forEach(dataset.metadata.fieldsMeta, function(att){
								att.selected = false;
							});
							listField.selected = true;
							//add code for changing background with icon
						}
					} else {
						//listField.name==item.name
					}
				});
			} else {
				//dataset.label==listId
			}
		});
	}
	
	sbiModule_restServices.get("2.0/datasets","listNotDerivedDataset")
		.success(
				function(data, status, headers, config){
					if(data.hasOwnProperty("errors")) {
						console.log(data.errors[0].message);
					} else {
						ctr.list = data;
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
					}
					ctr.loadedList = true;
					if(ctr.loadedListAllO==true && ctr.loadedList==true) {
						if(value!=0) {
							ctr.loadDatasetsEditMode();
							
						}						
					} else {
						console.log("Only loadedList is loaded")
					}
				}
		)
		.error(
				function(data, status, headers, config) {
					// called asynchronously if an error occurs
				    // or server returns response with an error status.
					console.log("Datasets not obtained " + status);
				}
		)
		
	sbiModule_restServices.get("2.0/datasets","listNotDerivedDataset")
		.success(
				function(data, status, headers, config){
					if(data.hasOwnProperty("errors")) {
						console.log(data.errors[0].message);
					} else {
						ctr.listAllO = data;
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
					}

					ctr.loadedListAllO = true;
					if(ctr.loadedListAllO==true && ctr.loadedList==true) {
						if(value!=0) {
							ctr.loadDatasetsEditMode();
						}
					} else {
						console.log("Only loadedListAllO is loaded")
					}
				}
		)
		.error(
				function(data, status, headers, config) {
					// called asynchronously if an error occurs
				    // or server returns response with an error status.
					console.log("Datasets not obtained " + status);
				}
		)
		
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
			
			console.log("izvrsava se loaddataseteditmode")
			
    		console.log(value)
        	if(value!=null){
        		console.log(value)
        		var arr = value.replace(/ /g,'')
        		var array = arr.substring(1,arr.length-1);
        		var array1 = array.split(',');
        		for (var i = 0; i < array1.length; i++) {
        			console.log("array[i]")
        			console.log(array1[i])
    				for (var j = 0; j < ctr.listAllO.length; j++) {
    					console.log("listAllO")
    					console.log(ctr.listAllO.length)
        				if(ctr.listAllO[j].label==array1[i]){
        					for (var k = 0; k < ctr.list.length; k++) {
        						console.log("list")
        						console.log(ctr.list.length)
    							if(ctr.list[k].name==ctr.listAllO[j].name) {
    								var index = ctr.list.indexOf(ctr.list[k]);
    								if(index != -1) {
    									ctr.list.splice(index,1);
    									console.log("did splice")
    								}
    							}
    						}
        					ctr.listaNew.push(ctr.listAllO[j])
        					console.log("did push")
        				} else {
        					console.log("no dataset like that in ALL DATASETS")
        				}
        			}
    			}
        	}
			ctr.multiArray = JSON.parse(valueRelString);
    }
	
	ctr.clearSelections = function() {
		ctr.myselectedvariable = {};
		angular.forEach(ctr.listaNew, function(dataset){
			angular.forEach(dataset.metadata.fieldsMeta, function(listField){
				if(listField.selected==true){
					listField.selected=false;
				}
			});
		});
		
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
							ctr.myselectedvariable[ctr.listaNew[i].label] = ctr.listaNew[i].metadata.fieldsMeta[z];
							ctr.listaNew[i].metadata.fieldsMeta[z].selected=true;
						}
					}
				}
			  }
		   }			
		}
	}
	
}



