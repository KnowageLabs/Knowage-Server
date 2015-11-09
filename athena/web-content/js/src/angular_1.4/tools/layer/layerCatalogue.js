var app = angular.module('layerWordManager', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'sbiModule']);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
	'blue-grey');
});



var EmptyLayer = {
		category_id:"",
		name : "",
		label : "",
		descr : "",
		type : "",
		baseLayer : false,
		layerLabel: "",
		layerName: "",
		layerId2: "",	

};



app.controller('Controller', [ "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);



app.directive("fileread", [function () {
	return {
		scope: {
			fileread: "="
		},
		link: function (scope, element, attributes) {
			element.bind("change", function (changeEvent) {
				var reader = new FileReader();
				reader.onload = function (loadEvent) {
					scope.$apply(function () {
						scope.fileread = loadEvent.target.result;
					});
				}
				reader.readAsDataURL(changeEvent.target.files[0]);
			});
		}
	}
}]);

function funzione(sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	$scope.showme=false;
	$scope.pathFileCheck = false;
	$scope.isRequired=true;
	$scope.flagtype=true;
	$scope.flag=false;
	$scope.translate = sbiModule_translate;
	$scope.layerList = [];
	$scope.object_temp={};
	$scope.roles = [];
	$scope.rolesItem=[];
	
	
	$scope.loadLayer = function(){
		$scope.flagtype=true;
		console.log("dentro loadLayer");
		
		sbiModule_restServices.get("layers", '').success(
				function(data, status, headers, config) {

					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						$scope.layerList = data.root;


					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
		//fare una getRole


	}

	$scope.listType = [
	                   {value : 'File', label : sbiModule_translate.load("sbi.tools.layer.props.type.file")},
	                   {value : 'WFS', label: sbiModule_translate.load("sbi.tools.layer.props.type.wfs")},
	                   {value : 'WMS', label: sbiModule_translate.load("sbi.tools.layer.props.type.wms")},
	                   {value : 'TMS', label: sbiModule_translate.load("sbi.tools.layer.props.type.tms")},
	                   {value : 'Google', label: sbiModule_translate.load("sbi.tools.layer.props.type.google")},
	                   {value : 'OSM', label: sbiModule_translate.load("sbi.tools.layer.props.type.osm")}

	                   ];


	$scope.loadLayer();
	


	$scope.saveLayer = function(){
		$scope.selectedLayer.roles=$scope.rolesItem;
		console.log($scope.selectedLayer);
		if($scope.flag){

			//siamo nel caso di modifica dati già precedentemente inseriti
			if($scope.selectedLayer.layerFile == null || $scope.selectedLayer.layerFile == undefined){
				//siamo nel caso in cui la modifica del layer non coinvolge caricamento di file
				sbiModule_restServices.put("layers", '', $scope.selectedLayer).success(
						
						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();
								$scope.closeForm();

							} else {
								$scope.flag=false;
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
								
							}

						}).error(function(data, status, headers, config) {
							console.log("layer non Ottenuti " + status);
							$scope.loadLayer();
							$scope.showActionError();
							$scope.closeForm();
						})

			} else {
				//siamo nel caso in cui la modifica del layer comporta il caricamento di un file
				var fd = new FormData();
				fd.append('data', angular.toJson($scope.selectedLayer));
				fd.append('layerFile', $scope.selectedLayer.layerFile);

				sbiModule_restServices.put("layers", 'updateData', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}}).success(

						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();
								$scope.closeForm();

							} else {
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
								
							}

						}).error(function(data, status, headers, config) {
							$scope.showActionError();
							console.log("layer non Ottenuti " + status);
							$scope.loadLayer();
							$scope.closeForm();

						})

			}
		}else{
			//siamo nel caso in cui si sta aggiungengo un nuovo layer
			console.log($scope.selectedLayer.layerFile);
			var fd = new FormData();
			fd.append('data', angular.toJson($scope.selectedLayer));
			fd.append('layerFile', $scope.selectedLayer.layerFile);

			if($scope.selectedLayer.layerFile == null || $scope.selectedLayer.layerFile == undefined){
				console.log($scope.selectedLayer);
				//siamo nel caso in cui la post non prevede aggiunzioni di file
				sbiModule_restServices.post("layers",'',$scope.selectedLayer).success(
						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();
								$scope.closeForm();

							} else {
								$scope.flag=false;
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
							
							}

						}).error(function(data, status, headers, config) {
							$scope.showActionError();
							console.log("layer non Ottenuti " + status);
							$scope.loadLayer();
							$scope.closeForm();
						})

			} else{
				//siamo nel caso in cui si sta aggiungengo un layer con file
				sbiModule_restServices.post("layers", 'addData', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}}).success(

						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();
								$scope.closeForm();

							} else {
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
								
							}

						}).error(function(data, status, headers, config) {
							$scope.showActionError();
							console.log("layer non Ottenuti " + status);
							$scope.loadLayer();
							$scope.closeForm();
						})

			}
		}

	}

	$scope.loadLayerList = function(item){
	
		
		//function calls when you clic on the list of layers
		$scope.showme=true;
		if(item==null){
			$scope.flagtype=true;
			$scope.flag=false;
			$scope.isRequired=false;
			$scope.rolesItem=[];

		}
		$scope.object_temp = angular.copy(item);
		if(item!= null){
			
			$scope.flagtype=false;
			
			//siamo in una condizione di caricamento dati dalla lista
			if($scope.selectedLayer != null){
				var confirm = $mdDialog
				.confirm()
				.title(sbiModule_translate.load("sbi.layer.modify.progress"))
				.content(
						sbiModule_translate
						.load("sbi.layer.modify.progress.message.modify"))
						.ariaLabel('Lucky day').ok(
								sbiModule_translate.load("sbi.general.continue")).cancel(
										sbiModule_translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					if(item.pathFile!="null"){
						//controllo se pathFile è diverso da null epr abilitarne la visualizzazione del nomefile
						console.log("true");
						$scope.pathFileCheck =true;
					} else{
						console.log("false");
						$scope.pathFileCheck = false;
					}
					$scope.flag=true;
					$scope.loadRolesItem(item);
					$scope.selectedLayer = angular.copy(item);
					$scope.object_temp = angular.copy(item);
					

				}, function() {
					console.log('Annulla');
				});

			}  else {
				if(item.pathFile!="null"){
					//controllo se pathFile è diverso da null epr abilitarne la visualizzazione del nomefile
					console.log("true");
					console.log(item.pathFile);
					$scope.pathFileCheck =true;
				} else{
					console.log("false");
					$scope.pathFileCheck = false;
				}
				$scope.flag = true;
				$scope.loadRolesItem(item);
				$scope.selectedLayer = angular.copy(item);
				$scope.object_temp = angular.copy($scope.selectedLayer);
		
				
			}
		} else {
			if($scope.selectedLayer != null ){
				var confirm = $mdDialog
				.confirm()
				.title(sbiModule_translate.load("sbi.layer.modify.progress"))
				.content(
						sbiModule_translate
						.load("sbi.layer.modify.progress.message.modify"))
						.ariaLabel('Lucky day').ok(
								sbiModule_translate.load("sbi.general.continue")).cancel(
										sbiModule_translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					$scope.cancel();		

				}, function() {
					console.log('Annulla');
				});

			} else if($scope.selectedLayer == null ) {
				$scope.newLayer = JSON.parse(JSON.stringify(EmptyLayer));
				$scope.flag=false;
				
			}
		}


	}

	$scope.loadRolesItem = function(item){
		console.log("chiamata loadRolesItem");
		console.log(item);
		sbiModule_restServices.post("layers", "postitem", item).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						
					} else {
						$scope.rolesItem = data;
					}
				}).error(function(data, status, headers, config) {
					console.log("error");

				})
		
	}
/*	
	$scope.deleteRole = function(id){
		//cancella ruolo associato a layer 
		console.log("Ruolo cancellato"+id);
		var obj = {id: id, id_l : $scope.selectedLayer.layerId};
		sbiModule_restServices.post("layers", 'deleterole',obj).success(

				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						$scope.loadRolesItem($scope.selectedLayer);
						$scope.showActionDelete();
					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
		
	}*/

	$scope.cancel = function(){
		console.log("CANCEL");

		if($scope.flag==true){
			$scope.isRequired=false;
			$scope.selectedLayer = angular.copy($scope.object_temp);
			$scope.rolesItem=$scope.loadRolesItem($scope.selectedLayer);
		} else{
			console.log("Reset");
			$scope.selectedLayer = angular.copy({});
			$scope.rolesItem=[];
			$scope.flag=false;
			$scope.isRequired=false;
		//	console.log(angular.element('<div class="md-char-counter">'));
	
		}
		
		$scope.contactForm.$setPristine();
		$scope.contactForm.$setUntouched();
		
	}

	$scope.menuLayer= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		action : function(item,event) {
			$scope.selectedLayer = item;

			var confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.layer.delete.action"))
			.content(
					sbiModule_translate
					.load("sbi.layer.modify.progress.message.modify"))
					.ariaLabel('Lucky day').ok(
							sbiModule_translate.load("sbi.general.continue")).cancel(
									sbiModule_translate.load("sbi.general.cancel"));

			$mdDialog.show(confirm).then(function() {
				$scope.deleteLayer();	

			}, function() {
				console.log('Annulla');
			});


		}
	}];	

	$scope.deleteLayer = function(){
		console.log($scope.selectedLayer);
		sbiModule_restServices.remove("layers", 'deleteLayer',"id="+$scope.selectedLayer.layerId).success(

				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						$scope.loadLayer();
						$scope.closeForm();
						$scope.showActionDelete();
					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
				$scope.cancel();


	}
	$scope.loadCategory = function() {
		sbiModule_restServices.get("domains", "listValueDescriptionByType",
				"DOMAIN_TYPE=GEO_CATEGORY").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);
					} else {

						$scope.category = data;
					
					
					}
				}).error(function(data, status, headers, config) {
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

		})
	}
	$scope.loadCategory();
	
	
	
	$scope.showRoles=function(){
		console.log("show roles");
		sbiModule_restServices.get("layers", "getroles","").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {
						console.log("contiene data");
						console.log(data);
						//mostro tutti i ruoli
						$scope.roles = data;
									
						
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

				})
				
				
	}
	$scope.showRoles();
	
	/*$scope.checkFileType = function(element){
		console.log("checkFileType");
		console.log(element.files);
		//0 perchè è permesso di caricare solo un file
		$scope.selectedLayer.layerFile = element.files[0];
		if(element.files[0].type != ""){
			alert("Format file invalid... Please Load .json");
		}
		
	}*/
	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item, list);
		
		if(index!=-1){
		      $scope.rolesItem.splice(index,1);
		}else{
			$scope.rolesItem.push(item);
		}

	
		
      };
      $scope.exists = function (item, list) {
    	return  $scope.indexInList(item, list)>-1;
    	
      };
      
      $scope.indexInList=function(item, list) {
    	
    	  for (var i = 0; i < list.length; i++) {
    		    var object = list[i];
    		       if(object.id==item.id){
    		    	   //se nella lista è presente l'item è checked
    		    	   return i;
    		      }
    		}
    	
    		 return -1;
      };
      
 
	$scope.showActionOK = function() {
		var toast = $mdToast.simple()
		.content('Layer saved corretly...')
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};

	$scope.showActionError = function() {
		var toast = $mdToast.simple()
		.content('Error...A problem occured.Retry it')
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};

	$scope.showActionDelete = function() {
		var toast = $mdToast.simple()
		.content('Layer Deleted')
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {


			}
		});
	};
	//per vedere suggerimento pathFile
	 $scope.demo = {
			    showTooltip : false,
			    tipDirection : 'buttom'
			    	
			  };
	$scope.$watch('demo.tipDirection',function(val) {
		    if (val && val.length ) {
		      $scope.demo.showTooltip = true;
		    }
		  })
	$scope.closeForm = function(){
		$scope.cancel();
		$scope.flagtype=true;
		$scope.showme=false;
		$scope.flag=false;
		$scope.selectedLayer=null;
		
	}
	
	
	$scope.example = function(){
		sbiModule_restServices.get_item("layers", 'getFileContent', "id="+$scope.selectedLayer.layerId).success(
				function(data, status, headers, config) {

					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						//$scope.layerList = data.root;


					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
	}
};





