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
	$scope.filters={};
	$scope.insertVal={};
	$scope.layerSelected;
	$scope.multipleFilters={};
	$scope.listCheckedfilter={};
	$scope.oneTime=true;
	
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
			var layerList=[];
			for(var key in geoModule_template.layersLoaded){
				layerList.push(key);
			}
			sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath+'restful-services/');
			sbiModule_restServices.post("layers", 'getLayerFromList',{items: layerList}).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							sbiModule_logger.log("layer non Ottenuti");
						} else {
							for(var i=0;i<data.root.length;i++){
								var tmp=data.root[i];
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
									
									//enable only the non base layer
									if(geoModule_template.layersLoaded[tmp.label]==true){
//										the layer is enabled
										$scope.toggleLayer(tmp);
									}
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
			if($scope.baseLayers.hasOwnProperty("geoModule_constant.templateLayer") && $scope.baseLayers[geoModule_constant.templateLayer].hasOwnProperty(geoModule_template.selectedBaseLayer)){
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

	$scope.toggleLayer = function(layerConf){
		sbiModule_logger.log("toggleLayer");
		/*if(geoModule_template.hasOwnProperty('filtersApplied')){
			$scope.listCheckedfilter=geoModule_template.filtersApplied;
			$scope.multipleFilters=geoModule_template.filtersApplied;
			//geoModule_layerServices.filters=geoModule_template.filtersApplied;
		}*/
		
		//$scope.listCheckedfilter=geoModule_layerServices.filters;
		//geoModule_template.filtersApplied=$scope.listCheckedfilter;
		
		$scope.checkLayerWithoutFilter(layerConf);
		
		if(geoModule_layerServices.filters[layerConf.layerId]){
				//lo contiene già quindi si sta rimuovendo CHECK OFF
				$scope.listCheckedfilter=$scope.removefromList($scope.listCheckedfilter,layerConf.layerId);
				
		}else{
			//check on 
			if($scope.multipleFilters[layerConf.layerId]){
				$scope.listCheckedfilter[layerConf.layerId]=$scope.multipleFilters[layerConf.layerId];
			}


			
		}
		geoModule_layerServices.filters =$scope.listCheckedfilter;
		geoModule_layerServices.toggleLayer(layerConf);
		

		$timeout(function() {
			$map.updateSize();
			$map.render();
		}, 500);
		
	};
	//start code for filters 
	$scope.checkLayerWithoutFilter = function(layerConf){
		geoModule_layerServices.layerWithoutFilter=true;
		if(!geoModule_layerServices.filters[layerConf.layerId]){
			//si sta aggiungendo un layer alla mappa
			if(!$scope.multipleFilters[layerConf.layerId]){
				//vuol dire che il layer non ha filtri!
				geoModule_layerServices.layerWithoutFilter=true;
				console.log("Layer senza filtri");
			}else{
				//verifico che nel form non siastato inserito niente
				geoModule_layerServices.layerWithoutFilter=$scope.checkFilterInthisLayer();
				
				if(Object.getOwnPropertyNames(geoModule_layerServices.filters).length>0){
					//verifico che non è gia caricato un layer senza filtri
					geoModule_layerServices.layerWithoutFilter=$scope.checkFilterInLayersLoaded();
				}
				
	
			}
		}else{
			geoModule_layerServices.layerWithoutFilter=false;
		}
	}
	
	$scope.checkFilterInthisLayer = function(){
		for(var i=0;i<$scope.multipleFilters[$scope.layerSelected.layerId].length;i++){
			//se tutti i campi sono null
			if($scope.multipleFilters[$scope.layerSelected.layerId][i].model!=""){
				console.log("Naaaaaa");
				return false;
			}

		}
		return true;
	}
	
	
	$scope.checkFilterInLayersLoaded = function(){
		for(var obj in geoModule_layerServices.filters){
			for(var j=0;j<geoModule_layerServices.filters[obj].length;j++){
				if(geoModule_layerServices.filters[obj][j].model!=""){
					
					return false;
				}
			}
		}
		return true;
	}
	$scope.removefromList=function(arr,k){
		var arr_tmp=[];
		for(var key in arr){
			if(key == k){
				
			}else{
				arr_tmp[key]=arr[key];
			}
		}
		return arr_tmp;
	}
	$scope.getFilter = function(selectedLayer){
		
		$scope.layerSelected = selectedLayer;
		$scope.filters=[];
		var selectedLayerProperties = selectedLayer.properties;
		if($scope.multipleFilters[selectedLayer.layerId]){
			//se è stato gia filtrato riprendi i valori
			$scope.filters = angular.copy($scope.multipleFilters[selectedLayer.layerId]);
		
		}else{
			//altrimenti inserisci i filtri per il layer selezionato
			for(var i = 0;i<selectedLayerProperties.length;i++){
				$scope.filters.push({
					filter: selectedLayerProperties[i],
					model:"",
					id:selectedLayer.layerId
				})
			}
			
		}
		

	}
	
	
	$scope.selectFilters = function(ev,val){
		$scope.getFilter(val);
		$scope.layerSelected = val;
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
		$scope.multipleFilters[$scope.layerSelected.layerId] = angular.copy($scope.filters);
		$scope.checkLayerWithoutFilter($scope.layerSelected);
		$mdDialog.cancel();
		var flag=false;
		
		if(geoModule_layerServices.layerIsLoaded($scope.layerSelected)){
			$scope.listCheckedfilter[$scope.filters[0].id]= $scope.multipleFilters[$scope.filters[0].id];
			geoModule_layerServices.filters =$scope.listCheckedfilter;
			var layer = geoModule_layerServices.createLayer($scope.layerSelected, false);
			$map.removeLayer(geoModule_layerServices.loadedLayer[$scope.layerSelected.layerId]);
			if(layer.hasOwnProperty("$$state")){
				layer.then(function(tmpLayer) {
					geoModule_layerServices.updateLayerLoaded(tmpLayer,$scope.layerSelected)

				});
			}
		}
		for(var i=0;i<$scope.multipleFilters[$scope.layerSelected.layerId].length;i++){
			//se tutti i campi sono null
			if($scope.multipleFilters[$scope.layerSelected.layerId][i].model!=""){
				flag=true;
			}

		}
		if(!flag){
			//rimuovi layer da multiFilters
			$scope.multipleFilters = $scope.removefromList($scope.multipleFilters ,$scope.layerSelected.layerId);
		}


		

		$timeout(function() {
			$map.updateSize();
			//$map.redraw();
		}, 500);
			
	}
	
	$scope.filterOFF =function(val){
		if($scope.multipleFilters[val.layerId]){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.cancelFilter = function(){
		$scope.filters = angular.copy($scope.multipleFilters[$scope.layerSelected.layerId]);
		$scope.updateChange();
	}
	//end code for filters
	$scope.addLayerFromCatalogue = function(ev){
		$mdDialog.show({
			controller: $scope.layerFromCatalogueController,
			templateUrl: 'layerFromCatalogueTemplate.html',
			parent: angular.element(document.body),
			targetEvent: ev,
			openFrom: '#addLayer',
			closeTo: '#map',
			clickOutsideToClose:false,
			preserveScope :true,
			scope: $scope
		});
	};

	$scope.layerFromCatalogueController=function($scope, $mdDialog,geoModule_template,sbiModule_translate,geoModule_constant,geoModule_layerServices) {	
		var addedLayer=[];
		var removedLayer=[];
		
		var tmpLayerList=[];
		var tmpBaseLayerList=[];
		angular.copy($scope.layers, tmpLayerList);
		angular.copy($scope.baseLayers, tmpBaseLayerList);
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


		function resetObject(elem){
			for(var key in elem){
				delete elem[key];
			}
		}
		function copyObject(source,dest){
			for(var key in source){
				dest[key]=  source[key];
			}
		}
		
		$scope.updateChange=function(){
			resetObject($scope.layers);
			copyObject(tmpLayerList,$scope.layers);
			resetObject($scope.baseLayers);
			copyObject(tmpBaseLayerList,$scope.baseLayers);
			
			
			for(var a=0;a<addedLayer.length;a++){
				if(!geoModule_template.layersLoaded.hasOwnProperty(addedLayer[a].label)){
					geoModule_template.layersLoaded[addedLayer[a].label]=false;
				}
			}
			for(var r=0;r<removedLayer.length;r++){
				if(geoModule_template.layersLoaded.hasOwnProperty(removedLayer[r].label)){
					if(geoModule_template.layersLoaded[removedLayer[r].label]==true){
						geoModule_layerServices.toggleLayer(removedLayer[r]);
					}
					delete geoModule_template.layersLoaded[removedLayer[r].label];
				}
			}
			
			//check for base layer
			var baseLayerFinded=false;
			for(var cat in $scope.baseLayers){
				for(var lay in $scope.baseLayers[cat]){
					if(angular.equals($scope.baseLayers[cat][lay],geoModule_layerServices.selectedBaseLayerOBJ)){
						geoModule_layerServices.selectedBaseLayerOBJ=$scope.baseLayers[cat][lay];
						baseLayerFinded=true;
						break;		
					}
				}
			}
			if(!baseLayerFinded){
				geoModule_layerServices.selectedBaseLayerOBJ=$scope.baseLayers.Default.OpenStreetMap;
			}
			
			
			$mdDialog.cancel();
		};
		
		$scope.cancel=function(){
			$mdDialog.cancel();
		};

		$scope.toggleLayerFromCatalogue=function(item){
			var categ=(item.hasOwnProperty("category") && item.category!=null)? item.category.valueNm : geoModule_constant.noCategory;
			var added=true;
			if(item.baseLayer){
				//insert category if not present
				if(!tmpBaseLayerList.hasOwnProperty(categ)){
					tmpBaseLayerList[categ]={};
				}

				if(	tmpBaseLayerList[categ].hasOwnProperty(item.label)){
					//remove
					delete tmpBaseLayerList[categ][item.label];
				}else{
					//add
					tmpBaseLayerList[categ][item.label]=item;
				}

				//remove category if empty
				if(Object.keys(tmpBaseLayerList[categ]).length==0){
					delete tmpBaseLayerList[categ];
				}
			}else{
				//insert category if not present
				if(!tmpLayerList.hasOwnProperty(categ)){
					tmpLayerList[categ]={};
				}

				if(	tmpLayerList[categ].hasOwnProperty(item.label)){
					//remove
					delete tmpLayerList[categ][item.label];
					added=false;
				}else{
					//add
					tmpLayerList[categ][item.label]=item;
				}

				//remove category if empty
				if(Object.keys(tmpLayerList[categ]).length==0){
					delete tmpLayerList[categ];
				}

			}
			
			if(added){
				if(removedLayer.indexOf(item)!=-1){
				removedLayer.splice(removedLayer.indexOf(item),1);
				}
				addedLayer.push(item);
			}else{
				removedLayer.push(item);
				if(addedLayer.indexOf(item)!=-1){
				addedLayer.splice(addedLayer.indexOf(item),1);
				}
			}
		};
	};
};
