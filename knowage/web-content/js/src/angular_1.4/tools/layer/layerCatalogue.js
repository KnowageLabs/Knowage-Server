var app = angular.module('layerWordManager', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'sbiModule', 'angular_2_col']);

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
		icon:"",
		roles:[],

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
	//variables
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
	$scope.filter=[];
	$scope.filter_set=[];
	$scope.forms = {};
	$scope.selectedTab = 0;
	$scope.typeWFS='geojson';

	$scope.tableFunction={

			download: function(item,evt){
				evt.stopPropagation();
				console.log("Download .....");
				console.log(item);
				$scope.showDetails(item);
				//	$scope.getDownload(item);
			}
	}


	$scope.loadLayer = function(){
		$scope.flagtype=true;
		console.log("dentro loadLayer");
		$scope.selectedTab = 0;
		sbiModule_restServices.get("layers", '').success(
				function(data, status, headers, config) {

					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						$scope.layerList = data.root;
						for(var i=0; i<$scope.layerList.length;i++){
							console.log($scope.layerList[i]);
							if($scope.layerList[i].type == "WFS" || $scope.layerList[i].type == "File" ){
								console.log("setto icon");
								$scope.layerList[i].icon = '<md-button class="md-icon-button" ng-click="scopeFunctions.download(row,$event)" > <md-icon md-font-icon="fa fa-download" style=" margin-top: 6px ; color: #153E7E;"></md-icon> </md-button>';
							} else{
								$scope.layerList[i].icon = '';
							}
						}

					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})

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

		$scope.selectedLayer.roles = $scope.rolesItem;
		$scope.selectedLayer.properties = $scope.filter_set;
		console.log($scope.selectedLayer.properties);
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
								$scope.flag=false;

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
								$scope.flag=false;
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
		$scope.setTab('Layer');
		if(item==null){
			$scope.flagtype=true;
			$scope.flag=false;
			$scope.pathFileCheck = false;
			$scope.isRequired=false;
			$scope.rolesItem=[];
			$scope.filter_set=[];
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
					if(item.pathFile!=null){
						//controllo se pathFile è diverso da null epr abilitarne la visualizzazione del nomefile
						console.log("true");
						$scope.pathFileCheck =true;
					} else{
						console.log("false");
						$scope.pathFileCheck = false;
					}
					$scope.flag=true;
					//$scope.filter_set=[];
					$scope.loadRolesItem(item);
					$scope.selectedLayer = angular.copy(item);


					$scope.object_temp = angular.copy(item);


				}, function() {
					console.log('Annulla');
				});

			}  else {
				if(item.pathFile!=null){
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
				$scope.selectedLayer = JSON.parse(JSON.stringify(EmptyLayer));
				$scope.flag=false;

			}
		}


	}
	$scope.loadFilter = function(){
		//funzione che carica i Filtri per ogni layer
		$scope.loadFilterAdded();
		console.log("carico filtri per");
		console.log($scope.selectedLayer);
		console.log("possiede già");
		console.log($scope.filter_set);

		sbiModule_restServices.get("layers", 'getFilter',"id="+$scope.selectedLayer.layerId).success(
				function(data, status, headers, config) {

					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {

						$scope.filter = data;
						//console.log("filtri ");
						//console.log($scope.filter);
						for(var i=0;i<$scope.filter_set.length;i++){
							//scorro tutti i filtri 


							//  console.log("entro per");
							//  console.log($scope.filter_set[i]);
							//prendo l'index del filter tot
							var index = $scope.filterInList($scope.filter_set[i],$scope.filter);
							//e lo rimuovo per non mostrarlo
							if(index > -1){
								$scope.filter.splice(index,1);
								//	 console.log("aggiornato");
								//	 console.log($scope.filter);  
							}


						}

					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
	}

	$scope.loadFilterAdded = function(){

		$scope.filter_set = [];

		if($scope.selectedLayer.properties){
			for(var i=0;i<$scope.selectedLayer.properties.length;i++){
				console.log($scope.selectedLayer.properties[i]);
				var prop = $scope.selectedLayer.properties[i];
				var obj={"property":prop};
				$scope.filter_set.push(obj );
			}
		}

	}



	$scope.loadRolesItem = function(item){

		sbiModule_restServices.post("layers", "postitem", item).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error

					} else {
						$scope.rolesItem = data;
						console.log($scope.rolesItem);
					}
				}).error(function(data, status, headers, config) {
					console.log("error");

				})

	}
	$scope.cancel = function(){
		console.log("CANCEL");
		$scope.setTab('Layer');

		if($scope.flag==true){
			//c'è un layer caricato
			$scope.isRequired=false;
			$scope.selectedLayer = angular.copy($scope.object_temp);
			$scope.rolesItem=$scope.loadRolesItem($scope.selectedLayer);
			$scope.filter_set = [];
			for(var i=0;i<$scope.selectedLayer.properties.length;i++){
				console.log($scope.selectedLayer.properties[i]);
				var prop = $scope.selectedLayer.properties[i];
				var obj={"property":prop};
				$scope.filter_set.push(obj );
				console.log(obj);
			}


		} else{
			console.log("Reset");
			$scope.selectedLayer = angular.copy({});
			$scope.rolesItem=[];
			$scope.flag=false;
			$scope.isRequired=false;
			$scope.filter_set=[];
			$scope.filter =[];

		}

		$scope.forms.contactForm.$setPristine();
		$scope.forms.contactForm.$setUntouched();

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
						$scope.category.push({});


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


	$scope.toggle = function (item, list) {
		var index = $scope.indexInList(item, list);

		if(index != -1){
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
	$scope.filterInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.property==item.property){
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

		$scope.flagtype=true;
		$scope.showme=false;
		$scope.flag=false;
		$scope.selectedLayer=null;
		$scope.rolesItem=[];
		$scope.filter_set=[];
		$scope.filter =[];


	}
	$scope.getDownload=function(item){

		if($scope.typeWFS == 'geojson'){
			console.log($scope.typeWFS);
		}else if($scope.typeWFS == 'kml'){
			console.log($scope.typeWFS);

		}else if($scope.typeWFS == 'shp'){
			console.log($scope.typeWFS);
		}
		sbiModule_restServices.get("layers","getDownload","id="+item.layerId+",typeWFS="+$scope.typeWFS).success(
				function(data, status, headers, config) {
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						var text ;						

						if($scope.typeWFS == 'geojson'){
							text = JSON.stringify(data);		
							var anchor = angular.element('<a/>');
							anchor.attr({
								href: 'data:text/json;charset=utf-8,' + encodeURI(text),
								target: '_blank',
								download: item.label+".json"
							})[0].click();

						} else if($scope.typeWFS == 'kml'){
							window.open(data.url,'_blank');
						} else if($scope.typeWFS == 'shp'){
							window.open(data.url,'_blank');
						}
						$scope.closeFilter();
					}
				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				});

	}

	$scope.showDetails = function(item){
		$scope.selectedLayer=item;	

		if(item.type=='WFS'){
			$scope.isWFS=true; 
		} else{
			$scope.isWFS=false;
			$scope.typeWFS='geojson';
		}

		$mdDialog.show({
			templateUrl: 'dialog1.tmpl.html',
			scope:$scope,
			preserveScope: true,
			targetEvent:item,
			parent: angular.element(document.body),
			clickOutsideToClose:true
		})

		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
	}
	$scope.setTypeWFS = function(val){
		$scope.typeWFS = val;
		console.log($scope.typeWFS);
	}
	$scope.showAdvanced = function(ev) {
		$mdDialog.show({
			templateUrl: 'dialog1.tmpl.html',
			scope:$scope,
			preserveScope: true,
			parent: angular.element(document.body),
			targetEvent: ev,
			clickOutsideToClose:true
		})
		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
	};
	$scope.closeFilter = function(){
		$mdDialog.cancel();
	}
	$scope.addFilter = function(item){
		console.log("Dentro addFilter");
		console.log($scope.filter_set.indexOf(item));
		if( $scope.filter_set.indexOf(item)>-1){
			//se presente non fare nulla
		} else{
			$scope.filter_set.push(item);
			var index = $scope.filter.indexOf(item);

			$scope.filter.splice(index,1);

		}
		console.log($scope.filter_set);
	}
	$scope.removeFilter = function(item){
		var index = $scope.filter_set.indexOf(item);
		$scope.filter_set.splice(index,1);
		$scope.filter.push(item);
	}
	$scope.removeIcon = [{

		label: sbiModule_translate.load("sbi.federationdefinition.delete"),
		icon:"fa fa-trash-o",
		backgroundColor:'red',
		action : function(ev) {
			console.log("ciaooooo");
			console.log(ev);
			$scope.removeFilter(ev);
		}
	}];

	/*	  $scope.info = [{
           		label: sbiModule_translate.load("sbi.federationdefinition.info"),
           		icon:"fa fa-info-circle",
           		backgroundColor:'green',
           		action : function(ev) {
           			console.log("hola");
    				console.log(ev);
           				$scope.showDetails(ev);
           			}  
	  }]
	 */ 


	$scope.setTab = function(Tab){
		$scope.selectedTab = Tab;
	}
	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}

};





