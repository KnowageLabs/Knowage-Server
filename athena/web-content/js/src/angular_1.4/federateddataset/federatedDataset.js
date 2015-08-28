var app = angular.module('MYAPPNIKOLA', ['ngMaterial']);

app.controller('MyCRTL', function($http, $scope, $mdDialog){
	console.log("verzija163");
	
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
	$scope.RelationshipsArray = [];
	$scope.checkBranch = false;
	$scope.beforeRel = {};
	$scope.finalJSON = "";
	$scope.updatedHeader = "";
	
	$scope.arrayElement = {
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
	
	$scope.update = $scope.federateddataset;
	angular.toJson($scope.update);
	 
	$scope.showAdvanced = function(ev) {
	  
	  angular.forEach($scope.listaNew, function(dataset){
		  
		  if($scope.checkBranch==false){
			  
			  console.log('false grana ['+$scope.checkBranch+' kontrolna]');
			  angular.forEach(dataset.metadata.fieldsMeta, function(listField){
				  
				  if(listField.selected){
					  console.log('selected field');
					  $scope.beforeRel = dataset;
					  $scope.checkBranch = true;
				  }
			  })
		  } else {
			  console.log('true grana  ['+$scope.checkBranch+' kontrolna]');
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
					
					  t.sourceTable.name = $scope.beforeRel.name; 
					  t.sourceTable.className = $scope.beforeRel.name;
					  //t.sourceColumns.push(beforeRel.listField.name);
					  
					  t.destinationTable.name = dataset.name;
					  t.destinationTable.className = dataset.name; 
					  t.destinationColumns.push(polje.name);
					  
					  $scope.beforeRel = polje;
					  $scope.RelationshipsArray.push(t);
				  }
			  })
		  }
	  })
	  console.log(JSON.stringify($scope.RelationshipsArray));
	  $mdDialog.show({
		  parent: angular.element(document.body),
	      templateUrl: 'datasetsTemp.jsp',
	      scope: $scope,
	      targetEvent: ev
	    })
	};
	
	$scope.saveFedDataSet = function() {
		console.log(JSON.stringify($scope.update).slice(0,-1)+",\"relationships\":"+JSON.stringify($scope.RelationshipsArray)+"}");
	}

	$scope.selektuj = function(listField,dataset){
		  
		  if(dataset==undefined)return 
		  if(listField.selected===true){
		   listField.selected = false;
		  } else {
		   angular.forEach(dataset.metadata.fieldsMeta, function(att) {
		      att.selected = false;
		       });
		   listField.selected = true;
		   }
		 }
		
	/*restServices.get("2.0/datasets").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log(data.errors[0].message);
				} else {
					$scope.list = data;
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


			})*/
			
/*	restServices.post("federateddataset/post").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log(data.errors[0].message);
				} else {
					//
				});
			});
				}

			}).error(function(data, status, headers, config) {
				// called asynchronously if an error occurs
			    // or server returns response with an error status.
				console.log("Datasets not obtained " + status);


			})*/
	
	$http.get('http://localhost:8080/athena/restful-services/2.0/datasets').
	  success(function(data, status, headers, config) {
	    // this callback will be called asynchronously
	    // when the response is available
		  
		  $scope.list = data;
		  angular.forEach($scope.lista, function(dataset) {
			  angular.forEach(dataset.metadata.fieldsMeta, function(listField) {
				  listField.selected = false;
				});
			});
	  }).
	  error(function(data, status, headers, config) {
	    // called asynchronously if an error occurs
	    // or server returns response with an error status.
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
		var index = $scope.list.indexOf(param);
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
	
	$scope.createAssociations = function(){
		angular.forEach($scope.listaNew, function(dataset) {
			  angular.forEach(dataset.metadata.fieldsMeta, function(listField) {
				  if(listField.selected===true){
					  $scope.relation += "="+dataset.name.toUpperCase()+"."+listField.name;
					  $scope.relNew = $scope.relation.substring(1);  
				  }
				});
			});
		$scope.associationArray.push($scope.relNew);
		$scope.relation = "";
		$scope.relNew = "";
	}
	
	$scope.kickOutFromAssociatonArray = function(param) {
		var index = $scope.associationArray.indexOf(param);
        if (index != -1) {
          $scope.associationArray.splice(index, 1);
        }
	}
	
	$scope.hide = function() {
	    $mdDialog.hide();
	  };

	$scope.cancel = function() {
	    $mdDialog.cancel();
	  };

	$scope.answer = function(answer) {
	    $mdDialog.hide(answer);
	  };
});

