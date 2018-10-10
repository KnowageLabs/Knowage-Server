var app = angular.module('layerWordManager', [ 'ngMaterial', 'ui.tree',
                                               'angularUtils.directives.dirPagination', 'ng-context-menu',
                                               'angular_list', 'angular_table' ,'sbiModule', 'angular-list-detail','file_upload']);


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



app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","sbiModule_messaging","sbiModule_user","sbiModule_config", funzione ]);



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

function funzione(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast,sbiModule_messaging,sbiModule_user,sbiModule_config) {
	//variables
	$scope.showFilters = sbiModule_user.functionalities.indexOf("SpatialFilter")>-1;
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
	$scope.fileMaxSize=sbiModule_config.layerFileMaxSize;

	$scope.tableFunction={

			download: function(item,evt){
				evt.stopPropagation();
				$scope.showDetails(item);
			}
	}


	$scope.loadLayer = function(){
		$scope.flagtype=true;
		$scope.selectedTab = 0;
		sbiModule_restServices.get("layers", '').then(
				function(result) {
					if (result.data.hasOwnProperty("errors")) {
						console.log("Error loading layer");
						$scope.showActionError(result.data.errors);
					} else {
						$scope.layerList = result.data.root;
					}

				},function(result) {
					$scope.showActionError(result.data.errors);
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


		if($scope.flag){

			//case: modify layer
			if($scope.selectedLayer.layerFile == undefined || $scope.selectedLayer.layerFile.file == null || $scope.selectedLayer.layerFile.file == undefined){ //.file added
				//modify layer without upload file
				sbiModule_restServices.put("layers", '', $scope.selectedLayer).then(

						function(result) {

							if (result.data.hasOwnProperty("errors")) {
								$scope.showActionError(result.data.errors);
								$scope.closeForm();

							} else {
								$scope.flag=false;
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
							}

						},function (result) {
							console.log("Error on saving layer " + result.status);
							$scope.loadLayer();
							$scope.showActionError(result.data.errors);
							$scope.closeForm();
						})

			} else {
				//modify layer with upload file
				var fd = new FormData();
				fd.append('data', angular.toJson($scope.selectedLayer));
				fd.append('layerFile', $scope.selectedLayer.layerFile.file);

				sbiModule_restServices.put("layers", 'updateData', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}}).then(

						function(result) {
							if (result.data.hasOwnProperty("errors")) {
								console.log("Error on saving layer " );
								$scope.showActionError(result.data.errors);
								$scope.closeForm();

							} else {
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();
								$scope.flag=false;

							}

						}, function(result) {
							$scope.showActionError(result.data.errors);
							console.log("Error on saving layer " + result.status);
							$scope.loadLayer();
							$scope.closeForm();

						})

			}
		}else{

			if($scope.selectedLayer.layerFile == undefined || $scope.selectedLayer.layerFile.file == null || $scope.selectedLayer.layerFile.file == undefined){
				//add layer without upload file
				sbiModule_restServices.post("layers",'',$scope.selectedLayer).then(
						function(result) {

							if (result.data.hasOwnProperty("errors")) {
								console.log("Error on saving layer");
								$scope.showActionError(result.data.errors);
								$scope.closeForm();

							} else {
								$scope.flag=false;
								$scope.loadLayer();
								$scope.closeForm();
								$scope.showActionOK();

							}

						}, function(result) {
							$scope.showActionError(result.data.errors);
							console.log("Error on saving layer " + result.status);
							$scope.loadLayer();
							$scope.closeForm();
						})

			} else{
				//add new Layer
				var fd = new FormData();
				fd.append('data', angular.toJson($scope.selectedLayer));
				fd.append('layerFile', $scope.selectedLayer.layerFile.file); //file added
				//add Layer with file
				sbiModule_restServices.post("layers", 'addData', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}}).then(

						function(result) {
							if (result.data.hasOwnProperty("errors")) {
								console.log("Error while adding layer");
								$scope.showActionError(result.data.errors);
								$scope.closeForm();

							} else {
								$scope.loadLayer();
								$scope.closeForm();
								$scope.flag=false;
								$scope.showActionOK();
							}

						},function(result) {
							$scope.showActionError(result.data.errors);
							console.log("Error while adding layer " + result.status);
							$scope.loadLayer();
							$scope.closeForm();
						})

			}
		}

	}

	$scope.loadLayerList = function(item){
		//function calls when you click on the list of layers
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
			if($scope.selectedLayer != null && Object.keys($scope.selectedLayer).length != 0){
				var confirm = $mdDialog
				.confirm()
				.title(sbiModule_translate.load("sbi.layer.modify.progress"))
				.content(
						sbiModule_translate
						.load("sbi.layer.modify.progress.message.modify"))
						.ariaLabel('Modify message').ok(
								sbiModule_translate.load("sbi.general.continue")).cancel(
										sbiModule_translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					if(item.pathFile!=null && item.type =="File"){
						//if pathfile!=null enable the visualization of filename
						$scope.pathFileCheck =true;
					} else{
						$scope.pathFileCheck = false;
					}
					$scope.flag=true;
					$scope.loadRolesItem(item);
					$scope.selectedLayer = angular.copy(item);

					$scope.object_temp = angular.copy(item);


				}, function() {
				});

			}  else {
				if(item.pathFile!=null && item.type =="File"){
					//if pathfile!=null enable the visualization of filename
					$scope.pathFileCheck =true;
				} else{
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
						.ariaLabel('Layer modify progress').ok(
								sbiModule_translate.load("sbi.general.continue")).cancel(
										sbiModule_translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					$scope.cancel(true);

				}, function() {

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

		sbiModule_restServices.get("layers", 'getFilter',"id="+$scope.selectedLayer.layerId).then(
				function(result) {

					if (result.data.hasOwnProperty("errors")) {
						console.log("Filters don't loaded ");
						$scope.showActionError(result.data.errors);
						$scope.closeForm();
					} else {
						$scope.filter = result.data;

						for(var i=0;i<$scope.filter_set.length;i++){
							//if filer is selected
							var index = $scope.filterInList($scope.filter_set[i],$scope.filter);
							//remove it from the list of all filters
							if(index > -1){
								$scope.filter.splice(index,1);
							}
						}
					}
				},
				function(result) {
					console.log("Filters don't loaded " + status);

				})
	}

	$scope.loadFilterAdded = function(){

		$scope.filter_set = [];

		if($scope.selectedLayer.properties){
			for(var i=0;i<$scope.selectedLayer.properties.length;i++){

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
					}
				}).error(function(data, status, headers, config) {
					console.log("error");

				})

	}

	$scope.cancel = function(notHide){
		$scope.setTab('Layer');

		if($scope.flag==true){
			//there is a layer loaded in the form
			$scope.isRequired=false;

			$scope.rolesItem=$scope.loadRolesItem($scope.selectedLayer);
			$scope.filter_set = [];
			if($scope.selectedLayer.properties!=null){
			for(var i=0;i<$scope.selectedLayer.properties.length;i++){
				var prop = $scope.selectedLayer.properties[i];
				var obj={"property":prop};
				$scope.filter_set.push(obj );

			}}
			$scope.selectedLayer = angular.copy({});


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
		if(notHide!=true){
			$scope.showme=false;
		}

	}

	$scope.menuLayer= [{
		label : sbiModule_translate.load('sbi.generic.download'),
		icon:'fa fa-download' ,
		action : function(item,event) {
			$scope.showDetails(item);
		}
	},{
		label : sbiModule_translate.load('sbi.generic.delete'),
		 icon:'fa fa-trash' ,

		action : function(item,event) {
			$scope.selectedLayer = item;

			var confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.layer.delete.action"))
			.content(
					sbiModule_translate
					.load("sbi.layer.modify.progress.message.modify"))
					.ariaLabel('Layer modify').ok(
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

		sbiModule_restServices.remove("layers", 'deleteLayer',"id="+$scope.selectedLayer.layerId).then(

				function(result) {
					if (result.data.hasOwnProperty("errors")) {
						if(result.data.errors!=undefined)
						{
							$scope.showActionError(result.data.errors);
						}

					} else {
						$scope.loadLayer();
						$scope.closeForm();
						$scope.showActionDelete();
					}

				}, function(result) {
					if (result.data.hasOwnProperty("errors")) {
						if(result.data.errors!=undefined)
						{
							$scope.showActionError(data.errors);
						}

					}
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
		if(list==undefined)return false;
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


	$scope.showActionOK = function(msg) {
		if (msg === undefined || msg === "") msg = sbiModule_translate.load("sbi.layercatalogue.save");
		sbiModule_messaging.showInfoMessage(msg,"");
	};

	$scope.showActionError = function(val) {

		var msg = sbiModule_translate.load("sbi.layercatalogue.problem");
		if (val !== undefined) {
			if (typeof val === 'string' && val !== ""){
				msg = val;
			}else{
				msg = "";
				for(var i=0;i < val.length;i++)
				{
					msg = msg + " , " + val[i].message;
				}
				if(msg.length>=3){
					msg=msg.substring(3);
				}
			}
		}
		//shows the final message
		sbiModule_messaging.showErrorMessage(msg,"");
	};

	$scope.showActionDelete = function(msg) {
		if (msg === undefined || msg === "") msg = sbiModule_translate.load("sbi.layer.deleted");
		sbiModule_messaging.showInfoMessage(msg,"");


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

		sbiModule_restServices.get("layers","getDownload","id="+item.layerId+",typeWFS="+$scope.typeWFS).then(
				function(result) {
					if (result.data == null) {
						$scope.showAction($scope.translate.load("sbi.layercatalogue.errorretrylayer"));
						console.log($scope.translate.load("sbi.layercatalogue.errorretrylayer"));
					} else {
						var text ;

						if($scope.typeWFS == 'geojson'){
							$scope.download.getPlain(JSON.stringify(result.data), item.label, 'text/json', 'json');
						} else if($scope.typeWFS == 'kml' || $scope.typeWFS == 'shp'){
							$scope.download.getLink(data.url);
						}
						$scope.closeFilter();
					}
				}, function(result) {
					console.log($scope.translate.load("sbi.layercatalogue.errorretrylayer") + result.status);
					$scope.showAction($scope.translate.load("sbi.layercatalogue.errorretrylayer"));
				});


	}
	$scope.showAction = function(text) {
		sbiModule_messaging.showInfoMessage(text,"");
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
		if(Tab == 'Filter'){
			$scope.loadFilter();
		}
	}

	$scope.isSelectedTab = function(Tab){
		return (Tab == $scope.selectedTab) ;
	}

};

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);




