var app = angular.module('svgViewerApp', ['ngMaterial','sbiModule']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
	    $mdThemingProvider.theme('knowage')
	    $mdThemingProvider.setDefaultTheme('knowage');
	}]);

app.controller('SvgViewerController', ['$scope','sbiModule_restServices','$mdSidenav','sbiModule_logger','$window','sbiModule_config','$rootScope',SvgViewerControllerFunction] );
		
function SvgViewerControllerFunction($scope, sbiModule_restServices, $mdSidenav,sbiModule_logger,$window,sbiModule_config,$rootScope)	{
  $scope.isSidenavOpen = false;
  
  $scope.currentLevel = 1;
    
  $scope.openSideNav = function() {
    $mdSidenav('svgSideNav').toggle();
  };
  
  //TODO: to test and change
  $scope.goToPreviousLevel = function(){
	  $scope.currentLevel = $scope.currentLevel - 1;
	  if ($scope.currentLevel == 1){
		  document.getElementById('svgContainer').src = sbiModule_config.contextName+"/api/1.0/svgviewer/drillMap?level="+$scope.currentLevel;
	  } else {
		  document.getElementById('svgContainer').src = sbiModule_config.contextName+"/api/1.0/svgviewer/drillMap?name="+e.detail+"&level="+$scope.currentLevel;
	  }
  }
    
  
  $window.document.addEventListener("SVGLoaded", function(e) {
	  //retrieve Measures
	  $scope.getMeasures();
	  //retrieve Layers
	  $scope.getLayers();
	  $scope.getLegendColors();
	});
  
  $window.document.addEventListener("SVGElementClicked", function(e) {
	  
	  //alert("Clicked element with id "+e.detail);  
	  //TODO: to change
	  $scope.currentLevel = $scope.currentLevel +1;
	  document.getElementById('svgContainer').src = sbiModule_config.contextName+"/api/1.0/svgviewer/drillMap?name="+e.detail+"&level="+$scope.currentLevel;
	});
  
  /**
   * Loads the measures list with a REST service
   * */
  $scope.getMeasures = function(){
	  sbiModule_restServices.get("1.0/svgviewer", 'getMeasures','level='+$scope.currentLevel).success(
			  function(data, status, headers, config) {
				  if (data.hasOwnProperty("errors")) {
					  sbiModule_logger.log("measures not retrivied");
				  } else {
					  $scope.measures = data;
					  for (var propt in $scope.measures){
						  if ($scope.measures[propt].selected){
							  //set default selected measure
							  $scope.measureValue = $scope.measures[propt].columnId;
						  }
					  }
					  sbiModule_logger.trace("measures correctly retrivied",data);		

				  }
			  }).error(function(data, status, headers, config) {
				  sbiModule_logger.log("measures not retrivied");
			  });
  };
	
	
	

  /**
   * Loads the measures list with a REST service
   * */
  $scope.getLayers = function(){
	  sbiModule_restServices.get("1.0/svgviewer", 'getLayers','level='+$scope.currentLevel).success(
			  function(data, status, headers, config) {
				  if (data.hasOwnProperty("errors")) {
					  sbiModule_logger.log("layers not retrivied");
				  } else {
					  
					  for (var key in data) {
						  //force all layers to be selected by default
						  data[key].selected = true;
					  }
					  $scope.layers = data;
					  sbiModule_logger.trace("layers correctly retrivied",data);		

				  }
			  }).error(function(data, status, headers, config) {
				  sbiModule_logger.log("layers not retrivied");
			  });
  };


  //retrieve the SVG element from the specified container
  $scope.getSVG = function(containerId){
	  var svgdoc = null;
	  var svgwin = null;
	  var embed = document.getElementById(containerId);
	  try {
		  svgdoc = embed.getSVGDocument();
	  }
	  catch(exception) {
		  sbiModule_logger.log('The GetSVGDocument interface is not supported');
	  }

	  if (svgdoc && svgdoc.defaultView){  // try the W3C standard way first
		  svgwin = svgdoc.defaultView;
	  }
	  else if (embed.window)
		  svgwin = embed.window;
	  else try {
		  svgwin = embed.getWindow();
	  }
	  catch(exception) {
		  sbiModule_logger.log('The DocumentView interface is not supported\r\n' +
		  'Non-W3C methods of obtaining "window" also failed');
	  }

	  return svgwin;
  }
  
  /**
  * Change the selected measure and apply the color visualization
  * */  
  $scope.changeSelectedMeasure =  function(columnId,description)  {
	  var svgwin = $scope.getSVG('svgContainer');
	  //call setKPI function inside the SVG
      svgwin.setKpi('radioButtons',columnId,description);
	  $scope.getLegendColors();

  }
  /**
   * Show or hide a specific layer 
   * */  
  $scope.changeSelectedLayer =  function(layerName, showToggle)  {
	  var svgwin = $scope.getSVG('svgContainer');
	  //call toggleMapLayer function inside the SVG 
      svgwin.toggleMapLayer(layerName,showToggle);
  }
  
  $scope.getLegendColors = function(){
	  var svgwin = $scope.getSVG('svgContainer');
	  if (svgwin.myMapApp.colArray){
		  $scope.legend = new Object();
		  $scope.legend.colors = svgwin.myMapApp.colArray;
		  $scope.legend.tresholds = svgwin.myMapApp.threshArray;
	  }
  }
};