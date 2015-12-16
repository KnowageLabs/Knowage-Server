/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geoModule')
.directive('geoLayers',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoLayers/templates/geoLayersTemplate.jspf',
		controller: geoLayersControllerFunction,
		require: "^geoMap",
		scope: {
			id:"@"
		},
		disableParentScroll:true,
	}
});

function geoLayersControllerFunction(sbiModule_config,$map,$scope,$mdDialog,$timeout,baseLayer,geoModule_layerServices,
		sbiModule_restServices,sbiModule_logger,geoModule_template,geoModule_constant,sbiModule_translate){

	$scope.geoModule_layerServices=geoModule_layerServices;
	$scope.layers={};
	$scope.openLayersMenu=false;
	$scope.baseLayers=baseLayer;
	$scope.translate=sbiModule_translate;
	$scope.filters=[];
	$scope.insertVal={};
	$scope.layerSelected;
	$scope.layerconf;
	
	$scope.loadLayerFromTemplate=function(){
		//if geoModule_template has baseLayersConf, add them to layerlist
		if(geoModule_template.hasOwnProperty("baseLayersConf") && geoModule_template.baseLayersConf.length!=0 ){
			$scope.baseLayers[geoModule_constant.templateLayer]={};
			$scope.layers[geoModule_constant.templateLayer]={};
			for(var i=0;i<geoModule_template.baseLayersConf.length;i++){
				geoModule_template.baseLayersConf[i].category={valueNm: geoModule_constant.templateLayer};

				if(geoModule_template.baseLayersConf[i].hasOwnProperty("baseLayer") && geoModule_template.baseLayersConf[i].baseLayer==true){
					$scope.baseLayers[geoModule_constant.templateLayer][geoModule_template.baseLayersConf[i].label]=geoModule_template.baseLayersConf[i];
				}else{
					$scope.layers[geoModule_constant.templateLayer][geoModule_template.baseLayersConf[i].label]=geoModule_template.baseLayersConf[i];
				}
			}

			if(Object.keys($scope.baseLayers[geoModule_constant.templateLayer]).length==0){
				// delete category in baseLayers if empty
				delete $scope.baseLayers[geoModule_constant.templateLayer];
			}
			if(Object.keys($scope.layers[geoModule_constant.templateLayer]).length==0){
				// delete category in layers if empty
				delete $scope.layers[geoModule_constant.templateLayer];
			}
		}


		if(geoModule_template.hasOwnProperty("layersLoaded") && geoModule_template.layersLoaded.length!=0){
			//load from catalogue
			sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath+'restful-services/');
			sbiModule_restServices.post("layers", 'getLayerFromList',{items: geoModule_template.layersLoaded}).success(
					function(data, status, headers, config) {

						sbiModule_logger.trace("layer caricati",data);
						if (data.hasOwnProperty("errors")) {
							sbiModule_logger.log("layer non Ottenuti");
						} else {
							for(var i=0;i<data.root.length;i++){
								var tmp=data.root[i];
//								if(tmp.category== null){
//									tmp.category={valueNm: geoModule_constant.noCategory};
//								}
								var categ=(tmp.hasOwnProperty("category") && tmp.category!=null)? tmp.category.valueNm : geoModule_constant.noCategory;
								
								if(tmp.hasOwnProperty("baseLayer") && tmp.baseLayer==true){
									//insert category if not present
									if(!$scope.baseLayers.hasOwnProperty(categ)){
										$scope.baseLayers[categ]={};
									}
									$scope.baseLayers[categ][tmp.label]=tmp;
								}else{
									//insert category if not present
									if(!$scope.layers.hasOwnProperty(categ)){
										$scope.layers[categ]={};
									}
									$scope.layers[categ][tmp.label]=tmp;
								}
							}
						}

						$scope.initBaseLayer();
					}).error(function(data, status, headers, config) {
						console.log("layer non Ottenuti " + status);
						$scope.initBaseLayer();
					});

		}
	};

	$scope.loadLayerFromTemplate();

	$scope.initBaseLayer=function(){
		var found=false;
		if(geoModule_template.hasOwnProperty('selectedBaseLayer')){
			if($scope.baseLayers[geoModule_constant.templateLayer].hasOwnProperty(geoModule_template.selectedBaseLayer)){
				//search if the selectedBaseLayer is in template layers prev loaded
				geoModule_layerServices.alterBaseLayer($scope.baseLayers[geoModule_constant.templateLayer][geoModule_template.selectedBaseLayer]); 
				found=true;
			}else{
				//search in baseLayers
				for(cat in $scope.baseLayers){
					if(cat!=geoModule_constant.templateLayer){
						for(lay in $scope.baseLayers[cat] ){
							if(lay==geoModule_template.selectedBaseLayer){
								geoModule_layerServices.alterBaseLayer($scope.baseLayers[cat][lay]); 
								found=true;
							}
						}
					}
				}
			}
		}

		if(!found){
			//selectedBaseLayer not find
			if(geoModule_template.hasOwnProperty('selectedBaseLayer')){
				alert("selectedBaseLayer="+geoModule_template.selectedBaseLayer+" non trovato. verrà caricato il layer di base")
			}else{
				console.log("selectedBaseLayer non set. Load Base layer...")
			}

			geoModule_layerServices.alterBaseLayer(baseLayer[geoModule_constant.defaultBaseLayer].OpenStreetMap); 
		}
	};

	$scope.toggleLayersMenu=function(){
		$scope.openLayersMenu=!$scope.openLayersMenu;
		$timeout(function() {
			$map.updateSize();
		}, 500);
	};

	$scope.showBaseLayer=function(layerConf){
		sbiModule_logger.log("show base layer")
		geoModule_layerServices.alterBaseLayer(layerConf)
	};

	$scope.toggleLayer=function(layerConf){
		sbiModule_logger.log("toggleLayer");
		geoModule_layerServices.toggleLayer(layerConf)
	};
	//inizio filtri
	$scope.getFilter=function(val){
		$scope.layerSelected = val;
		$scope.filters=[];
		var values = val.properties;
		for(var i = 0;i<values.length;i++){
			$scope.filters.push({
				filter: values[i],
				model:""
			})
		}
		console.log($scope.filters);
	}
	$scope.selectFilters = function(ev,val){
		$scope.getFilter(val);
		
		$scope.layerconf=val;
		$mdDialog.show({
			controller: $scope.layerFromCatalogueController,
			templateUrl: 'filtersforLayerTemplate.html',
			parent: angular.element(document.body),
			targetEvent: ev,
			openFrom: '#addLayer',
			closeTo: '#map',
			clickOutsideToClose:true,
			preserveScope :true,
			scope: $scope
		});
	}
	$scope.applyFilter = function(){
		$scope.toggleLayer($scope.layerconf);
		if(geoModule_layerServices.layerIsLoaded($scope.layerconf)){
			var layer = geoModule_layerServices.createLayer($scope.layerconf, false);
			$map.removeLayer(layer);
		}
		
		$scope.toggleLayer($scope.layerconf);
		console.log("ok",$scope.filters);
		$mdDialog.cancel();
		geoModule_layerServices.filters = $scope.filters;
		$map.render();
		$map.updateSize();
	}
	//fine filtri
	$scope.addLayerFromCatalogue = function(ev){
		$mdDialog.show({
			controller: $scope.layerFromCatalogueController,
			templateUrl: 'layerFromCatalogueTemplate.html',
			parent: angular.element(document.body),
			targetEvent: ev,
			openFrom: '#addLayer',
			closeTo: '#map',
			clickOutsideToClose:true,
			preserveScope :true,
			scope: $scope
		});
	};

	$scope.layerFromCatalogueController=function($scope, $mdDialog,geoModule_template,sbiModule_translate,geoModule_constant) {	
		
		$scope.title=sbiModule_translate.load("gisengine.info.message.selectFromCatalogue");
		$scope.layerCatalogueList=[];
		$scope.selectedLayerList=[];
		$scope.columnList=[
		                   {label:sbiModule_translate.load("gisengine.geoLayer.catalogue.tableColumn.label"),name:"layerLabel"},
		                   {label:sbiModule_translate.load("gisengine.geoLayer.catalogue.tableColumn.type"),name:"type",size:"100px"},
		                   {label:sbiModule_translate.load("gisengine.geoLayer.catalogue.tableColumn.layerUrl"),name:"layerURL"},
		                   {label:sbiModule_translate.load("gisengine.geoLayer.catalogue.tableColumn.baseLayer"),name:"baseLayer",size:"100px"}
		                   ];
		
		$scope.columnSearch=["layerLabel","type","baseLayer"];
		
		$scope.loadSelectedLayerList=function(){
			for(cat in $scope.layers){
				for(lay in $scope.layers[cat] ){
					$scope.selectedLayerList.push($scope.layers[cat][lay]);
				}
			}

			for(cat in $scope.baseLayers){
				for(lay in $scope.baseLayers[cat] ){
					$scope.selectedLayerList.push($scope.baseLayers[cat][lay]);
				}
			}

			sbiModule_logger.log("$scope.selectedLayerList",$scope.selectedLayerList)
		};

		$scope.loadSelectedLayerList(); 

		sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath+'restful-services/');
		sbiModule_restServices.get("layers", '').success(
				function(data, status, headers, config) {

					sbiModule_logger.trace("layer caricati",data);
					if (data.hasOwnProperty("errors")) {
						sbiModule_logger.log("layer non Ottenuti");
					} else {
						$scope.layerCatalogueList = data.root;
					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);
				});


		$scope.updateChange=function(){
			$mdDialog.cancel();
		};

		$scope.toggleLayerFromCatalogue=function(item){
			var categ=(item.hasOwnProperty("category") && item.category!=null)? item.category.valueNm : geoModule_constant.noCategory;

			if(item.baseLayer){
				//insert category if not present
				if(!$scope.baseLayers.hasOwnProperty(categ)){
					$scope.baseLayers[categ]={};
				}

				if(	$scope.baseLayers[categ].hasOwnProperty(item.label)){
					//remove
					delete $scope.baseLayers[categ][item.label];
				}else{
					//add
					$scope.baseLayers[categ][item.label]=item;
				}

				//remove category if empty
				if(Object.keys($scope.baseLayers[categ]).length==0){
					delete $scope.baseLayers[categ];
				}
			}else{
				//insert category if not present
				if(!$scope.layers.hasOwnProperty(categ)){
					$scope.layers[categ]={};
				}

				if(	$scope.layers[categ].hasOwnProperty(item.label)){
					//remove
					delete $scope.layers[categ][item.label];
				}else{
					//add
					$scope.layers[categ][item.label]=item;
				}

				//remove category if empty
				if(Object.keys($scope.layers[categ]).length==0){
					delete $scope.layers[categ];
				}

			}
		};
	};
};
