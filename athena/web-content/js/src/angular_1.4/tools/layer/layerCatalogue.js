var app = angular.module('layerWordManager', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_rest', 'angular_list', 'angular_table' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
	'blue-grey');
});

app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});



var EmptyLayer = {

		name : "",
		label : "",
		descr : "",
		type : "",
		baseLayer : false,
		layerLabel: "",
		layerName: "",
		layerId2: "",	

};



app.controller('Controller', [ "translate", "restServices", "$scope","$mdDialog","$mdToast", funzione ]);



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

function funzione(translate, restServices, $scope, $mdDialog, $mdToast) {
	$scope.showme=false;
	$scope.isRequired=true;
	$scope.translate = translate;
	$scope.layerList = [];
	$scope.object_temp={};
	$scope.flagtype=true;
	$scope.flag=false;
	$scope.roles = [];
	
	$scope.loadLayer = function(){
		$scope.flagtype=true;
		console.log("dentro loadLayer");
		restServices.get("layers", '').success(
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


	}

	$scope.listType = [
	                   {value : 'File', label : translate.load("sbi.tools.layer.props.type.file")},
	                   {value : 'WFS', label: translate.load("sbi.tools.layer.props.type.wfs")},
	                   {value : 'WMS', label: translate.load("sbi.tools.layer.props.type.wms")},
	                   {value : 'TMS', label: translate.load("sbi.tools.layer.props.type.tms")},
	                   {value : 'Google', label: translate.load("sbi.tools.layer.props.type.google")},
	                   {value : 'OSM', label: translate.load("sbi.tools.layer.props.type.osm")}

	                   ];


	$scope.loadLayer();



	$scope.saveLayer = function(){

		console.log($scope.selectedLayer);
		if($scope.flag){

			//siamo nel caso di modifica dati già precedentemente inseriti
			if($scope.selectedLayer.layerFile == null || $scope.selectedLayer.layerFile == undefined){
				//siamo nel caso in cui la modifica del layer non coinvolge caricamento di file
				restServices.put("layers", '', $scope.selectedLayer).success(

						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();

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
						})


			} else {
				//siamo nel caso in cui la modifica del layer comporta il caricamento di un file
				var fd = new FormData();
				fd.append('data', angular.toJson($scope.selectedLayer));
				fd.append('layerFile', $scope.selectedLayer.layerFile);

				restServices.put("layers", 'updateData', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}}).success(

						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();

							} else {
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
								
							}

						}).error(function(data, status, headers, config) {
							$scope.showActionError();
							console.log("layer non Ottenuti " + status);
							$scope.loadLayer();

						})

			}
		}else{
			//siamo nel caso in cui si sta aggiungengo un nuovo layer
			console.log($scope.selectedLayer.layerFile);
			var fd = new FormData();
			fd.append('data', angular.toJson($scope.selectedLayer));
			fd.append('layerFile', $scope.selectedLayer.layerFile);

			if($scope.selectedLayer.layerFile == null || $scope.selectedLayer.layerFile == undefined){
				//siamo nel caso in cui la post non prevede aggiunzioni di file
				restServices.post("layers",'',$scope.selectedLayer).success(
						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();

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
						})




			} else{
				//siamo nel caso in cui si sta aggiungengo un layer con file
				restServices.post("layers", 'addData', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}}).success(

						function(data, status, headers, config) {
							console.log(data)

							if (data.hasOwnProperty("errors")) {
								console.log("layer non Ottenuti");
								$scope.showActionError();

							} else {
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
								
							}

						}).error(function(data, status, headers, config) {
							$scope.showActionError();
							console.log("layer non Ottenuti " + status);
							$scope.loadLayer();
						})



			}
		}

	}

	$scope.loadLayerList = function(item){
		//function calls when you clic on the list of layers
		if(item==null){
			$scope.flagtype=true;
			$scope.flag=false;
			$scope.isRequired=false;
		}
		$scope.object_temp = angular.copy(item);
		if(item!= null){
			$scope.flagtype=false;
			//siamo in una condizione di caricamento dati dalla lista
			if($scope.selectedLayer != null){
				var confirm = $mdDialog
				.confirm()
				.title(translate.load("sbi.layer.modify.progress"))
				.content(
						translate
						.load("sbi.layer.modify.progress.message.modify"))
						.ariaLabel('Lucky day').ok(
								translate.load("sbi.general.continue")).cancel(
										translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					$scope.flag=true;
					$scope.selectedLayer = angular.copy(item);
					$scope.object_temp = angular.copy(item);
		

				}, function() {
					console.log('Annulla');
				});

			} else {
				$scope.flag = true;
				$scope.selectedLayer = angular.copy(item);
				$scope.object_temp = angular.copy($scope.selectedLayer);
		
				
			}
		} else {
			if($scope.selectedLayer != null ){
				var confirm = $mdDialog
				.confirm()
				.title(translate.load("sbi.layer.modify.progress"))
				.content(
						translate
						.load("sbi.layer.modify.progress.message.modify"))
						.ariaLabel('Lucky day').ok(
								translate.load("sbi.general.continue")).cancel(
										translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					$scope.cancel();		

				}, function() {
					console.log('Annulla');
				});

			} else if($scope.selectedLayer == null ) {
				$scope.newLayer = JSON.parse(JSON.stringify(EmptyLayer));
				$scope.flag=false;
				$scope.showme=true;
			}
		}


	}



	$scope.cancel = function(){
		if($scope.flag==true){
			$scope.isRequired=false;
			$scope.selectedLayer = angular.copy($scope.object_temp);
		} else{
			console.log("Reset");
			$scope.selectedLayer = angular.copy({});
			$scope.flag=false;
			$scope.isRequired=false;
			}
	}

	$scope.menuLayer= [{
		label : translate.load('sbi.generic.delete'),
		action : function(item,event) {
			$scope.selectedLayer = item;

			var confirm = $mdDialog
			.confirm()
			.title(translate.load("sbi.layer.delete.action"))
			.content(
					translate
					.load("sbi.layer.modify.progress.message.modify"))
					.ariaLabel('Lucky day').ok(
							translate.load("sbi.general.continue")).cancel(
									translate.load("sbi.general.cancel"));

			$mdDialog.show(confirm).then(function() {
				$scope.deleteLayer();	

			}, function() {
				console.log('Annulla');
			});








		}
	}];	

	$scope.deleteLayer = function(){
		console.log($scope.selectedLayer);
		restServices.remove("layers", 'deleteLayer',"id="+$scope.selectedLayer.layerId).success(

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
		restServices.get("domains", "listValueDescriptionByType",
				"DOMAIN_TYPE=GEO_CATEGORY").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						showToast(translate.load("sbi.glossary.load.error"),
								3000);
					} else {

						$scope.category = data;
					}
				}).error(function(data, status, headers, config) {
			showToast(translate.load("sbi.glossary.load.error"), 3000);

		})
	}
	$scope.loadCategory();
	
	
	
	/*$scope.showRoles=function(){
		restServices.get("layers", "getroles","").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						console.log(translate.load("sbi.glossary.load.error"),3000);
					} else {
						console.log("contiene data");
						console.log(data);
						$scope.roles = data;
						
						
					}
				}).error(function(data, status, headers, config) {
					console.log(translate.load("sbi.glossary.load.error"), 3000);

				})
	}
	$scope.showRoles();
	*/
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
		.content('Error...Reload the Object or the label is not unique...try again')
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
	
	$scope.closeForm = function(){
		$scope.flagtype=true;
		$scope.showme=false;
		$scope.flag=false;
		$scope.selectedLayer=null;
	}
};





