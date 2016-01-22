var app = angular.module('layerWordManager', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'sbiModule', 'angular-list-detail']);


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



app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", funzione ]);



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

function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast) {
	//variables
	$scope.showme=false;
	$scope.pathFileCheck = false;
	$scope.isRequired=true;
	$scope.flagtype=true;
	$scope.flag=false;
	$scope.translate = sbiModule_translate;
	$scope.download = sbiModule_download;
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
			}
	}


	$scope.loadLayer = function(){
		$scope.flagtype=true;
		$scope.selectedTab = 0;
		sbiModule_restServices.get("layers", '').success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						$scope.layerList = data.root;
						for(var i=0; i<$scope.layerList.length;i++){
							if($scope.layerList[i].type == "WFS" || $scope.layerList[i].type == "File" ){
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

			//case: modify layer
			if($scope.selectedLayer.layerFile == null || $scope.selectedLayer.layerFile == undefined){
				//modify layer without upload file
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
				//modify layer with upload file
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
			//add new Layer
			console.log($scope.selectedLayer.layerFile);
			var fd = new FormData();
			fd.append('data', angular.toJson($scope.selectedLayer));
			fd.append('layerFile', $scope.selectedLayer.layerFile);

			if($scope.selectedLayer.layerFile == null || $scope.selectedLayer.layerFile == undefined){
				console.log($scope.selectedLayer);
				//add layer without upload file
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
				//add Layer with file
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

			//load Layer in the form
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
						//if pathfile!=null enable the visualization of filename
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
				if(item.pathFile!=null){
					//if pathfile!=null enable the visualization of filename
					console.log("true");
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
		//load filters for each layer
		$scope.loadFilterAdded();

		sbiModule_restServices.get("layers", 'getFilter',"id="+$scope.selectedLayer.layerId).success(
				function(data, status, headers, config) {

					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						$scope.filter = data;

						for(var i=0;i<$scope.filter_set.length;i++){
							//if filer is selected 
							var index = $scope.filterInList($scope.filter_set[i],$scope.filter);
							//remove it from the list of all filters
							if(index > -1){
								$scope.filter.splice(index,1);
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

					} else {
						$scope.rolesItem = data;
						console.log($scope.rolesItem);
					}
				}).error(function(data, status, headers, config) {
					console.log("error");

				})

	}
	$scope.cancel = function(){
		$scope.setTab('Layer');

		if($scope.flag==true){
			//there is a layer loaded in the form
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
			$scope.selectedLayer = angular.copy({});
			$scope.rolesItem=[];
			$scope.flag=false;
			$scope.isRequired=false;
			$scope.filter_set=[];
			$scope.filter =[];

		}

		$scope.forms.contactForm.$setPristine();
		$scope.forms.contactForm.$setUntouched();
		$scope.showme=false;

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
		sbiModule_restServices.get("layers", "getroles","").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {
						//show all roles
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
				return i;
			}
		}

		return -1;
	};
	$scope.filterInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.property==item.property){
				return i;
			}
		}

		return -1;
	};


	$scope.showActionOK = function() {
		var toast = $mdToast.simple()
		.content(sbiModule_translate.load("sbi.layercatalogue.save"))
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
		.content(sbiModule_translate.load("sbi.layercatalogue.problem"))
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

		sbiModule_restServices.get("layers","getDownload","id="+item.layerId+",typeWFS="+$scope.typeWFS).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						var text ;						

						if($scope.typeWFS == 'geojson'){
							$scope.download.getPlain(data, item.label, 'text/json', 'json');
						} else if($scope.typeWFS == 'kml' || $scope.typeWFS == 'shp'){
							$scope.download.getLink(data.url);
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
		if( $scope.filter_set.indexOf(item)>-1){
			//if it present no action
		} else{
			$scope.filter_set.push(item);
			var index = $scope.filter.indexOf(item);

			$scope.filter.splice(index,1);

		}
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
			$scope.removeFilter(ev);
		}
	}];


	$scope.setTab = function(Tab){
		$scope.selectedTab = Tab;
	}
	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}

};





