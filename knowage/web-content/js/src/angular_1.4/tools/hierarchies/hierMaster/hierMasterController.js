var app = angular.module('hierManager');

app.controller('hierMasterController', ["sbiModule_translate","$scope","$mdDialog","sbiModule_restServices","$mdDialog",masterControllerFunction ]);

function masterControllerFunction (sbiModule_translate, $scope, $mdDialog, sbiModule_restServices,$mdDialog){
	
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.hierarchiesType = ['Master', $scope.translate.load('sbi.hierarchies.type.technical')];
	$scope.hierarchiesMap = {};
	$scope.keys = {'subfolders' : 'children'};
	$scope.dimensions = [];
	$scope.dimensionsTable = {};
	$scope.hierTree = [];
	$scope.dateTree = new Date();
	$scope.dateDim = new Date();
	$scope.metadataMap = {};
	
	$scope.hierTree.push(angular.copy(dataJson));
	
	$scope.restService.get("dimensions","getDimensions")
		.success(
			function(data, status, headers, config) {
				$scope.dimensions=angular.copy(data);
			})	
		.error(function(data, status){
			var message = 'GET dimensions error of ' + data + ' with status :' + status;
			$scope.showAlert('ERROR',message);
			
		});
	
	$scope.getDimensionsTable = function(dim,filterDate,filterHierarchy){
		if ($scope.dateDim && $scope.dim){
			var dateFormatted = $scope.formatDate($scope.dateDim);
			var config = {};
			config.params = {
					dimension : $scope.dim.DIMENSION_NM,
					validityDate : dateFormatted
			}
			if (filterDate !== undefined && filterDate !== null){
				config.params.filterDate = filterDate;
			}
			if (filterHierarchy !== undefined && filterHierarchy !== null){
				config.params.filterHierarchy = filterHierarchy;
			}
			$scope.restService.get("dimensions","dimensionData",null,config)
				.success(
					function(data, status, headers, config) {
						if (data.error == undefined){
							$scope.dimensionsTable=angular.copy(data);
						}else{
							$scope.showAlert('ERROR',data.error[0].message);
						}
					})	
				.error(function(data, status){
					var message = 'GET dimensions error of ' + data + ' with status :' + status;
					$scope.showAlert('ERROR',message);
			});
		}
	}
	
	$scope.getHierarchies = function (){
		var type = $scope.hierType;
		var dim = $scope.dim;
		var map = $scope.hierarchiesMap; 
		var hierarchies = choose == 'src' ? $scope.hierarchiesSrc : $scope.hierarchiesTarget;
		if (type !== undefined && dim !== undefined){
			var dimName = dim.DIMENSION_NM;
			var keyMap = type+'_'+dimName; 
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'getHierarchiesMaster' : 'getHierarchiesTechnical';
			
			//if the hierarchies[dim][type] is not defined, get the hierarchies and save in the map. Else, get them from the map 
			if (map[keyMap] === undefined){
				$scope.restService.get("hierarchies",serviceName,"dimension="+dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								map[keyMap] = data;
								choose == 'src' ?  $scope.hierarchiesSrc =  angular.copy(data) : $scope.hierarchiesTarget =  angular.copy(data);
							}else{
								$scope.showAlert('ERROR',data.errors[0].message);
							}
						})
					.error(function(data, status){
						var message='GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
						$scope.showAlert('ERROR',message);
						
					});
			}else{
				choose == 'src' ? $scope.hierarchiesSrc = map[keyMap] : $scope.hierarchiesTarget = map[keyMap];
			}
		}
		//get the metadata for the dimension
		var dimName = dim.DIMENSION_NM;
		if ($scope.metadataMap !== undefined && $scope.metadataMap[dimName] == undefined){
			$scope.restService.get("hierarchies","nodeMetadata","dimension="+dimName+"&excludeLeaf=false")
			.success(
				function(data, status, headers, config) {
					if (data.errors === undefined){
						$scope.metadataMap[dimName] = data;
					}else{
						$scope.showAlert('ERROR',data.errors[0].message);
					}
			})
			.error(function(data, status){
				var message = 'GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
				$scope.showAlert('ERROR',message);
			});
		}
	}
	
/*	
	$scope.loadDimensionTable = function(e){
		//chiamare servizio rest per recuperare i dati reali delle dimensioni
		$scope.data.root=[{"dimId":"ID1","codice":"CDC1","name":"Conto1", "descr":"Conto numero 1","dt_inizio":"01/01/2015","dt_fine":"31/12/9999"}, 
		                       {"dimId":"ID2","codice":"CDC2","name":"Conto2", "descr":"Conto numero 2","dt_inizio":"01/01/2015","dt_fine":"30/05/2015"}, 
		                       {"dimId":"ID3","codice":"CDC3","name":"Conto3", "descr":"Conto numero 3","dt_inizio":"01/06/2015","dt_fine":"31/12/9999"},
		                       {"dimId":"ID4","codice":"CDC4","name":"Conto4", "descr":"Conto numero 4","dt_inizio":"01/01/2015","dt_fine":"31/12/9999"}, 
		                       {"dimId":"ID5","codice":"CDC5","name":"Conto5", "descr":"Conto numero 5","dt_inizio":"01/01/2015","dt_fine":"30/05/2015"}, 
		                       {"dimId":"ID6","codice":"CDC6","name":"Conto6", "descr":"Conto numero 6","dt_inizio":"01/06/2015","dt_fine":"31/12/9999"},
		                       {"dimId":"ID7","codice":"CDC7","name":"Conto7", "descr":"Conto numero 7","dt_inizio":"01/02/2015","dt_fine":"31/12/9999"}, 
		                       {"dimId":"ID8","codice":"CDC8","name":"Conto8", "descr":"Conto numero 8","dt_inizio":"01/03/2015","dt_fine":"30/05/2015"}, 
		                       {"dimId":"ID9","codice":"CDC9","name":"Conto9", "descr":"Conto numero 9","dt_inizio":"01/06/2015","dt_fine":"31/12/9999"},
		                       {"dimId":"ID10","codice":"CDC10","name":"Conto10", "descr":"Conto numero 10","dt_inizio":"05/01/2015","dt_fine":"31/12/9999"}, 
		                       {"dimId":"ID11","codice":"CDC11","name":"Conto11", "descr":"Conto numero 11","dt_inizio":"01/06/2015","dt_fine":"30/05/2015"}, 
		                       {"dimId":"ID12","codice":"CDC12","name":"Conto12", "descr":"Conto numero 12","dt_inizio":"01/06/2015","dt_fine":"31/12/9999"},
		                       {"dimId":"ID13","codice":"CDC13","name":"Conto13", "descr":"Conto numero 13","dt_inizio":"01/07/2015","dt_fine":"31/12/9999"}, 
		                       {"dimId":"ID14","codice":"CDC14","name":"Conto14", "descr":"Conto numero 14","dt_inizio":"01/01/2015","dt_fine":"30/05/2015"}, 
		                       {"dimId":"ID15","codice":"CDC15","name":"Conto15", "descr":"Conto numero 15","dt_inizio":"01/06/2015","dt_fine":"31/12/9999"}
		                     ];
		$scope.data.columns=[{"label":"ID","name":"dimId","visible":"false"},
		                          {"label":"Codice","name":"codice","visible":"true"},
		                          {"label":"Nome","name":"name","visible":"true"},
		                          {"label":"Descrizione","name":"descr","size":"100px","visible":"true"},
		                          {"label":"Data inizio","name":"dt_inizio","visible":"false"},
		                          {"label":"Data fine","name":"dt_fine","visible":"false"}
		                         ];
		$scope.data.columns_search=["dimId","codice", "name", "descr", "dt_inizio", "dt_fine"];
		
		$scope.initTable($scope.data.columns);
						
	}
	
	$scope.showDetails = function(row,cells,listId) {
		var cloneRow = [];
		var idx=0; //order for the list of properties
		for (c in cells){			
			var item = cells[c];
			var value = $scope.getValueColumn(item, row);
			var el = {};
			el.index = idx;
			el[item.label]=value;			
			idx++;
			cloneRow.push(el);
		}
		row = cloneRow;
		
        $mdDialog.show({
        	locals:{lrow: row},
        	 controllerAs : 'infCtrl',
	         preserveScope : true,
	         clickOutsideToClose : true,
	         template:
	           '<md-dialog style="width: 50%;  overflow-y: visible;"  class="infoBox" aria-label="Dettaglio">' +
	           '  <md-dialog-content class="md-padding">'+
	           '     <md-toolbar class="minihead">'+
	           '  	   <div class="md-toolbar-tools">'+
	           '		   <h4 class="md-flex" >Dettagli</h4>'+
	           ' 	  </div>'+
	           '   </md-toolbar>'+
	           '    <md-list>'+    
	           '      <md-list-item ng-repeat="(key,value) in infCtrl.row | orderBy: \'index\' ">'+
	           '		<span ng-repeat="(label,val) in value">'+
        	   '			<span ng-if="label!=\'index\'"><b>{{label}}:</b> {{val}}</span>'+
	           '		</span>'+  
	           '      '+
	           '    </md-list-item></md-list>'+
	           '  </md-dialog-content>' +
	           '  <div class="md-actions" layout="row">'+    
	           '  	<md-button ng-click="infCtrl.closeDialog()" class="md-raised">'+
//	           ' 		{{translate.load("sbi.ds.wizard.cancel");}} '+
	           '		Chiudi ' +	
	           ' 	</md-button>'+	           
	           '  </div>'+
	           '</md-dialog>',

	         controller: function(locals, $mdDialog) {
	        	 this.row = locals.lrow;
	        	 this.closeDialog = function() {	        		 
			          $mdDialog.hide();
			        }
	         }
	      });
	}
	
	$scope.getLabelColumn = function(c, cells){
		for (n in cells){
			if (c == cells[n].name){
				return cells[n].label;
			}
		}
		return c;
	}
	
	$scope.getValueColumn = function(c, values){
		for (v in values){
			if (c.name == v){
				return values[v];
			}
		}
		return null;
	}
	
	$scope.initTable = function(metadata){
		$scope.selDate = new Date();
		//filter columns throught the 'visible' property
		var visibleMetadata = [];
		for (m in metadata){
			if (metadata[m].visible === "true"){
				visibleMetadata.push(metadata[m]);
			}
		}
		$scope.visibleMetadata = visibleMetadata;
		
		$scope.loadTableDimension = true;
	}
	
	$scope.filterData = function(value, type){
		alert("Apply filter for " + type + " - value: " + value);
	}
	
	$scope.pageChanged = function(newPageNumber,itemsPerPage,searchValue){
		//alert("paginazione lato server");
	}
	*/
	//Create an alert dialog with a message
	$scope.showAlert = function (title, message){
		$scope.log.log(message);
		//if angular material version < 1.0.0_rc5 not has textContent function
		if (typeof $mdDialog.alert().textContent == 'function'){
			$mdDialog.show( 
				$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(title)
			        .textContent(message) //FROM angular material 1.0 
			        .ok('Ok')
				);
		}else {
			$mdDialog.show( 
				$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(title)
			        .content(message)
			        .ok('Ok')
		        );
		}
	};
	
	$scope.formatDate = function (date){
		return date.getFullYear() + '-' + date.getMonth()+'-'+ date.getDate();
	}
}

