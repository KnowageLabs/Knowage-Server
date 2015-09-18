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
	
	$scope.update = $scope.federateddataset;
	angular.toJson($scope.update);
	
	/*$scope.ispisi = function(){
		console.log($scope.list);
	}*/
	

	$scope.napuniNiz = function() {
		$scope.multiArray.push($scope.createAssociations());
		//console.log("f_napuniNiz "+angular.toJson($scope.multiArray));
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
	  $mdDialog.show({
		  templateUrl: '/athena/js/src/angular_1.4/tools/federateddataset/commons/templates/saveFederatedDatasetTemp.html',
		  parent: angular.element(document.body),	      
	      scope: $scope,
	      targetEvent: ev
	    })
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
		$scope.state=!$scope.state;
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
});

