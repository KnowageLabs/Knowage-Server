/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('geo_module')
.directive('geoLayers',function(){
	return{
		 restrict: "E",
		templateUrl:'/knowage/js/src/angular_1.4/tools/commons/geo/geoBottom/geoLayers/templates/geoLayersTemplate.jspf',
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
function geoLayersControllerFunction($map,$scope,$mdSidenav,$mdDialog,$timeout,baseLayer,layerServices,sbiModule_restServices){
	$scope.layerServices=layerServices;

	$scope.openLayersMenu=false;
	$scope.baseLayers=baseLayer;
	
	
	$scope.toggleLayersMenu=function(){
		$scope.openLayersMenu=!$scope.openLayersMenu;
		$timeout(function() {
			$map.updateSize();
		}, 500);
		
	}
	$scope.layers={};
//	$scope.layers= {"Category 2":{
//								'America WMS':{
//									id:"997",
//									type: 'WMS',
//							    	name: "America WMS",
//					     	        url: "http://demo.boundlessgeo.com/geoserver/wms",
//					     	        params: {'LAYERS': 'topp:states', 'TILED': true},
//					     	        category:"Category 2"
//					     	       
//								},
//								'ASTUTO 1':{
//									id:"998",
//									type: 'WFS',
//							    	name: "ASTUTO1",
//					     	        url: "http://pacweb.eng.it/astuto-geoserver/ATeSO/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=ATeSO:v_at_gis_limite_comunale_wgs84&maxFeatures=50&outputFormat=application/json",
//					     	       propsName:"Astutalu",
//					     	      category:"Category 2"
//					     	       
//								}
//						},
//					"Category 1":{
//								'ASTUTO 2':{
//									id:"999",
//									type: 'WFS',
//							    	name: "ASTUTO2",
//					     	        url: "http://sif.regione.sicilia.it/astuto-geoserver/ATeSO/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=ATeSO:v_at_gis_limite_comunale&maxFeatures=50&outputFormat=application/json",
//					     	       propsName:"Astutalu",
//					     	      category:"Category 1" 
//								}	
//					}
//	};
	
	
	
	
	$scope.showBaseLayer=function(layerConf){
		console.log("show base layer")
		layerServices.alterBaseLayer(layerConf)
	}
	
	$scope.toggleLayer=function(layerConf){
		console.log("toggleLayer");
		
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
	    
	    $scope.layerFromCatalogueController=function($scope, $mdDialog) {	    	
	    	$scope.layerCatalogueList=[];
	    	$scope.selectedLayerList=[];
	    	
	    	console.log("$scope.layers",$scope.layers)
    		
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
	    	    		
	    	    		console.log("$scope.selectedLayerList",$scope.selectedLayerList)
	    	    		
	    	    	}
	    	$scope.loadSelectedLayerList();    	
	    	    	
	    	sbiModule_restServices.get("layers", '').success(
			function(data, status, headers, config) {

				console.log("layer caricati",data);
				if (data.hasOwnProperty("errors")) {
					console.log("layer non Ottenuti");
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

