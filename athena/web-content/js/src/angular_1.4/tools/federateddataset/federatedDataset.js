var app = angular.module('MYAPPNIKOLA', ['ngMaterial','angular_rest','angular_list']);

app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

app.controller('MyCRTL', function(restServices, $scope, $mdDialog){
	console.log("verzija230");
	
	$scope.federateddataset = {};
	$scope.update = {};
	$scope.list = [];
	$scope.listaNew = [];
	$scope.state = true;
	$scope.relation = "";
	$scope.relNew = null;
	$scope.associationArray = [];
	$scope.alert = '';
	$scope.test = {};
	$scope.beforeRel = {};
	$scope.finalJSON = "";
	$scope.updatedHeader = "";
	$scope.item = {};
	$scope.multiArray = [];
	$scope.bla = {};
	$scope.selectedVariable = {};
	$scope.myselectedvariable = {};
	$scope.update = $scope.federateddataset;
	angular.toJson($scope.update);
	
	$scope.napuniNiz = function() {
		var check = false;
		var obj1 = $scope.createAssociations();
		var counter = 0;
		
		
			angular.forEach($scope.listaNew, function(dataset){
				angular.forEach(dataset.metadata.fieldsMeta, function(listField){
					
					if(listField.selected==true){
						 counter += 1;
						 console.log("kaunter"+counter)
					 }
				})
				 
			})
		
		
		
		if(counter<2) {
			$mdDialog.show(
	       		      $mdDialog.alert()
	       		        .parent(angular.element(document.querySelector('#popupContainer')))
	       		        .clickOutsideToClose(true)
	       		        .content('You have to select at least two fields to create a relation!')
	       		        .ok('OK')
	       		    );
		} else {
			
			if($scope.multiArray.length==0){
				
				$scope.multiArray.push($scope.createAssociations());
				
				
			} else {

				angular.forEach($scope.multiArray, function(obj2){


					if (JSON.stringify(obj1) === JSON.stringify(obj2)) {
						check = true;
			        	console.log("The relation is already created!")
			        	 $mdDialog.show(
			       		      $mdDialog.alert()
			       		        .parent(angular.element(document.querySelector('#popupContainer')))
			       		        .clickOutsideToClose(true)
			       		        .content('The relation is already created!')
			       		        .ok('OK')
			       		    );
			        }
					
				})
				if(!check){
					console.log("dodaj novi u niz")
					$scope.multiArray.push($scope.createAssociations());
				}
			} 
			
		}
		
		//console.log(obj1.length)
		

	}
		
	$scope.createAssociations = function(){
		  
		
		  var RelationshipsArray = [];
		  var checkBranch = false;
		  angular.forEach($scope.listaNew, function(dataset){
			  
			  if(checkBranch==false){
				  
				  console.log('false branch ['+checkBranch+' kontrolna]');
				  angular.forEach(dataset.metadata.fieldsMeta, function(listField){
					  
					  if(listField.selected){
						  console.log('selected field');
						  $scope.beforeRel = dataset;
						  $scope.beforeRel.firstSelectedListField = listField.name;
						  $scope.beforeRel.ime = dataset.name;
						  checkBranch = true;
					  }
				  })
			  } else {
				  console.log('true branch  ['+checkBranch+' kontrolna]');
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
					
						  t.sourceTable.name = $scope.beforeRel.ime; 
						  t.sourceTable.className = $scope.beforeRel.ime;
						  t.sourceColumns.push($scope.beforeRel.firstSelectedListField);
						  
						  t.destinationTable.name = dataset.name;
						  t.destinationTable.className = dataset.name; 
						  t.destinationColumns.push(polje.name);
						  
						  $scope.beforeRel = polje;
						  $scope.beforeRel.ime = dataset.name;
						  $scope.beforeRel.firstSelectedListField = polje.name;
						  RelationshipsArray.push(t);
						  
					  }
				  })
			  }
			  
		  })
		  //console.log("f_napuniMaliNiz "+JSON.stringify(RelationshipsArray));
		  return  RelationshipsArray;
	}
	
	//$scope.multiArray.push($scope.nesto);
	
	$scope.showAdvanced = function(ev) {
		if($scope.multiArray.length==0){
			$mdDialog.show(
	       		      $mdDialog.alert()
	       		        .parent(angular.element(document.querySelector('#popupContainer')))
	       		        .clickOutsideToClose(true)
	       		        .content('You didn\'t create any relationships!')
	       		        .ok('OK')
	       		    );
		}
		else{
			$mdDialog.show({
				  templateUrl: '/athena/js/src/angular_1.4/tools/federateddataset/commons/templates/saveFederatedDatasetTemp.html',
				  parent: angular.element(document.body),	      
			      scope: $scope,
			      targetEvent: ev
			    })
		}
	  
	};
	
	
	
	$scope.saveFedDataSet = function() {
		
		console.log($scope.multiArray)
		var item = {};
		item.name = $scope.update.name;
		item.label = $scope.update.label;
		item.description = $scope.update.description;
		item.relationships = "";
		item.relationships = $scope.multiArray;
		//item.relationships = [];
		//item.relationships.push($scope.RelationshipsArray);
		//item = JSON.stringify($scope.update).slice(0,-1)+",\"relationships\":"+JSON.stringify($scope.RelationshipsArray)+"}";
		console.log(JSON.stringify($scope.update).slice(0,-1)+",\"relationships\":"+JSON.stringify($scope.multiArray)+"}");
		//angular.toJson(item);
		//console.log("dsadsad"+item);
		restServices.post("federateddataset","post", item)
		
		.success(
				console.log("ok je")
				
		).
		error(
				console.log("nije ok")
		);
	}
	

	
	$scope.selektuj = function(item,listId){
		
		if($scope.myselectedvariable[listId]!=undefined || $scope.myselectedvariable[listId]!=null){
			if(item.name==$scope.myselectedvariable[listId].name){
				console.log("they are the same");
				$scope.myselectedvariable[listId] = null;	
			}
		}
		
		
		
		 angular.forEach($scope.listaNew, function(dataset){
			 if(dataset.label==listId){
				 angular.forEach(dataset.metadata.fieldsMeta, function(listField){
				 if(listField.name==item.name){
					 if(listField.selected===true) {
						 listField.selected = false;
						 $scope.selectedVariable = null;
					 } else {
						 angular.forEach(dataset.metadata.fieldsMeta, function(att){
							 att.selected = false;
						 });
						 listField.selected = true;
					 }
				 } else {
					 //listField.name==listField.name
				 } 
			 }); 
		 } else {
			 //dataset.label==listId
		 }
		  
		 });
		 
		 
	}
		
	restServices.get("2.0/datasets", "").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log(data.errors[0].message);
				} else {
					$scope.list = data;
					console.log($scope.list);
					angular.toJson($scope.list);
					console.log("sdada"+$scope.list);
					angular.forEach($scope.lista, function(dataset) {
						angular.forEach(dataset.metadata.fieldsMeta, function(listField) {
							listField.selected = false;
				});
			});
				}

			}).error(function(data, status, headers, config) {
				// called asynchronously if an error occurs
			    // or server returns response with an error status.
				console.log("Datasets not obtained " + status);


			});
		
	$scope.kickOutFromListNew = function(param){
		var index = $scope.listaNew.indexOf(param);
        if (index != -1) {
          $scope.listaNew.splice(index, 1);
        }
        if($scope.list.indexOf(param)===-1){
			$scope.list.push(param);
		} else {
			console.log("Parameter is already in the list.");
		}
	}
	

	$scope.moveToListNew = function(param){
		console.log("objekat"+param);
		var index = $scope.list.indexOf(param);
		console.log(""+index);
        if (index != -1) {
          $scope.list.splice(index, 1);
        }	
		if($scope.listaNew.indexOf(param)===-1){
			$scope.listaNew.push(param);
		} else {
			console.log("Parameter is already in the list.");
		}	
	}
	
	$scope.toggle = function(){
		if($scope.listaNew.length==0){
			$mdDialog.show(
	       		      $mdDialog.alert()
	       		        .parent(angular.element(document.querySelector('#popupContainer')))
	       		        .clickOutsideToClose(true)
	       		        .content('You didn\'t select any datasets!')
	       		        .ok('OK')
	       		    );
		} 
		else if($scope.listaNew.length==1){
			$mdDialog.show(
	       		      $mdDialog.alert()
	       		        .parent(angular.element(document.querySelector('#popupContainer')))
	       		        .clickOutsideToClose(true)
	       		        .content('Select at least two datasets!')
	       		        .ok('OK')
	       		    );
		}
		else {
			$scope.state=!$scope.state;
		}
		
	}
	
	$scope.kickOutFromAssociatonArray = function(param) {
		var index = $scope.associationArray.indexOf(param);
        if (index != -1) {
          $scope.associationArray.splice(index, 1);
        }
	}
	$scope.deleteFromMultiArray = function(param){
		var index = $scope.multiArray.indexOf(param);
		if(index !=-1){
			$scope.multiArray.splice(index, 1);
		}
	}
	
	$scope.hide = function() {
	    $mdDialog.cancel();
	  };

	$scope.cancel = function() {
	    $mdDialog.cancel();
	  };

	$scope.answer = function(answer) {
	    $mdDialog.hide(answer);
	  };
	  
	$scope.showAlert = function(ev) {
		    $mdDialog.show(
		      $mdDialog.alert()
		        .parent(angular.element(document.querySelector('#popupContainer')))
		        .clickOutsideToClose(true)
		        .title('Operation succeeded')
		        .ok('OK')
		        .targetEvent(ev)
		    );
		  };

	$scope.showDatasetDetails = function(ev) {
		$mdDialog.show(
				  				  	  
			      $mdDialog.alert()
			        .parent(angular.element(document.querySelector('#popupContainer')))
			        .clickOutsideToClose(true)
			        .title('Dataset information')
			        .content()
			        .ok('OK')
			        .targetEvent(ev)
			        
			    );
			/*$mdDialog.show({
				templateUrl: '/athena/js/src/angular_1.4/tools/federateddataset/commons/templates/datasetDetails.html',
				parent: angular.element(document.body),	      
			      scope: $scope,
			      targetEvent: ev
			    })*/
			};
			
			
	$scope.glossSpeedMenuOpt = [ 			 		               	
			 		               	{
			 		               		label: 'Delete',
			 		               		icon:"fa fa-trash-o",
			 		               		backgroundColor:'red',
			 		               		action : function(param) {
			 		               			$scope.kickOutFromListNew(param);
			 		               			}
			 		               	}
			 		             ];
	
	$scope.glossSpeedMenuOptAD = [ 			 		               	
		 		               	{
		 		               		label: 'Details',
		 		               		icon:"fa fa-info-circle",
		 		               		backgroundColor:'green',
		 		               		/*action : function(param) {
		 		               				$scope.showDSDetails(param);
		 		               			}*/
		 		               		
		 		               		
		 		               	}
		 		             ];
	$scope.showDSDetails = function(ev) {
		  $mdDialog.show({
			  templateUrl: '/athena/js/src/angular_1.4/tools/federateddataset/commons/templates/datasetDetails.html',
			  parent: angular.element(document.body),	      
		      scope: $scope,
		      targetEvent: ev
		    })
		};
		
	$scope.relaodFirstPage = function(){
		$route.reload();
	}
});

