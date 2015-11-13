/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geo_module')
.directive('geoLayers',function(sbiModule_config){
	return{
		 restrict: "E",
		templateUrl:sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoLayers/templates/geoLayersTemplate.jspf',
//		template: '<md-button  id="showLayers" class="md-fab md-mini" ng-click="showBottomSheet($event)" aria-label="toggle menu"><md-icon md-font-icon="fa fa-wifi fa-2x"></md-icon> </md-button>',
		 controller: geoLayersControllerFunction,
		require: "^geoMap",
		scope: {
			id:"@"
		},
		disableParentScroll:true,
		link: function(scope,elm,attrs){
			console.log("inizializzo geo-layers con id= "+scope.id);
			
		}
	}
})
function geoLayersControllerFunction(sbiModule_config,$map,$scope,$mdSidenav,$mdDialog,$timeout,baseLayer,layerServices,sbiModule_restServices,sbiModule_logger,geo_template,geoConstant){
	$scope.layerServices=layerServices;
	$scope.layers={};
	$scope.openLayersMenu=false;
	$scope.baseLayers=baseLayer;
	$scope.loadLayerFromTemplate=function(){
		//if geo_template has baseLayersConf, add them to layerlist
		if(geo_template.hasOwnProperty("baseLayersConf") && geo_template.baseLayersConf.length!=0 ){
			$scope.baseLayers[geoConstant.templateLayer]={};
			$scope.layers[geoConstant.templateLayer]={};
			for(var i=0;i<geo_template.baseLayersConf.length;i++){
				geo_template.baseLayersConf[i].category={valueNm: geoConstant.templateLayer};
				
				if(geo_template.baseLayersConf[i].hasOwnProperty("baseLayer") && geo_template.baseLayersConf[i].baseLayer==true){
					$scope.baseLayers[geoConstant.templateLayer][geo_template.baseLayersConf[i].label]=geo_template.baseLayersConf[i];
				}else{
					$scope.layers[geoConstant.templateLayer][geo_template.baseLayersConf[i].label]=geo_template.baseLayersConf[i];
				}
			}
			
			if(Object.keys($scope.baseLayers[geoConstant.templateLayer]).length==0){
				// delete category in baseLayers if empty
				delete $scope.baseLayers[geoConstant.templateLayer];
			}
			if(Object.keys($scope.layers[geoConstant.templateLayer]).length==0){
				// delete category in layers if empty
				delete $scope.layers[geoConstant.templateLayer];
			}
		}
		
		
		if(geo_template.hasOwnProperty("layersLoaded") && geo_template.layersLoaded.length!=0){
			//load from catalogue
			sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath+'restful-services/');
	    	sbiModule_restServices.post("layers", 'getLayerFromList',{items: geo_template.layersLoaded}).success(
			function(data, status, headers, config) {
	
				sbiModule_logger.trace("layer caricati",data);
				if (data.hasOwnProperty("errors")) {
					sbiModule_logger.log("layer non Ottenuti");
				} else {
				
	
					for(var i=0;i<data.root.length;i++){
						var tmp=data.root[i];
						var categ=tmp.category.valueNm;
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
    	
		
		console.log("$scope.baseLayers",$scope.baseLayers)
	}
	
	$scope.loadLayerFromTemplate();
	
	$scope.initBaseLayer=function(){
		var finded=false;
		if(geo_template.hasOwnProperty('selectedBaseLayer')){
			if($scope.baseLayers[geoConstant.templateLayer].hasOwnProperty(geo_template.selectedBaseLayer)){
				//search if the selectedBaseLayer is in template layers prev loaded
				layerServices.alterBaseLayer($scope.baseLayers[geoConstant.templateLayer][geo_template.selectedBaseLayer]); 
				finded=true;
			}else{
				//search in baseLayers
				for(cat in $scope.baseLayers){
					if(cat!=geoConstant.templateLayer){
		    			for(lay in $scope.baseLayers[cat] ){
		    				if(lay==geo_template.selectedBaseLayer){
		    					layerServices.alterBaseLayer($scope.baseLayers[cat][lay]); 
		    					finded=true;
		    				}
		    			}
					}
	    		}
			}
		}
		
		if(!finded){
			//selectedBaseLayer not find
			if(geo_template.hasOwnProperty('selectedBaseLayer')){
				alert("selectedBaseLayer="+geo_template.selectedBaseLayer+" non trovato. verrà caricato il layer di base")
			}else{
				alert("selectedBaseLayer non settato. verrà caricato il layer di base")
			}
			
			layerServices.alterBaseLayer(baseLayer.Default.OpenStreetMap); 
		}
	}

	
	$scope.toggleLayersMenu=function(){
		$scope.openLayersMenu=!$scope.openLayersMenu;
		$timeout(function() {
			$map.updateSize();
		}, 500);
		
	}
	

	$scope.showBaseLayer=function(layerConf){
		sbiModule_logger.log("show base layer")
		layerServices.alterBaseLayer(layerConf)
	}
	
	$scope.toggleLayer=function(layerConf){
		sbiModule_logger.log("toggleLayer");
		layerServices.toggleLayer(layerConf)
	}
	
	
	   $scope.addLayerFromCatalogue=function(ev){
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
	    }
	    
	    $scope.layerFromCatalogueController=function($scope, $mdDialog,geo_template) {	    	
	    	$scope.layerCatalogueList=[];
	    	$scope.selectedLayerList=[];
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
	    	    		
	    	    	}
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
			console.log("toggleitem",item);
			console.log("$scope.selectedLayerList",$scope.selectedLayerList)
			
			var categ=item.hasOwnProperty("category")? item.category.valueNm : "Default";
			
			if(item.baseLayer){
				//insert category if not present
				if(!$scope.baseLayers.hasOwnProperty(categ)){
					$scope.baseLayers[categ]={};
				}
				
				if(	$scope.baseLayers[categ].hasOwnProperty(item.layerId)){
					//remove
					delete $scope.baseLayers[categ][item.layerId];
				}else{
					//add
					$scope.baseLayers[categ][item.layerId]=item;
				}
				
				//remove category if empty
				if($scope.baseLayers[categ].length==0){
					delete $scope.baseLayers[categ];
				}
			}else{
				
				//insert category if not present
				if(!$scope.layers.hasOwnProperty(categ)){
					$scope.layers[categ]={};
				}
				
				if(	$scope.layers[categ].hasOwnProperty(item.layerId)){
					//remove
					delete $scope.layers[categ][item.layerId];
				}else{
					//add
					$scope.layers[categ][item.layerId]=item;
				}
				
				//remove category if empty
				if($scope.layers[categ].length==0){
					delete $scope.layers[categ];
				}
				
			}
	    							
	    				
	    }
	  }
	    
	    
	   
	
	
	}

