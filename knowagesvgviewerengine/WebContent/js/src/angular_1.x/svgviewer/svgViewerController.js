var app = angular.module('svgViewerApp', ['ngMaterial','sbiModule']);

app.controller('SvgViewerController', ['$scope','sbiModule_restServices','$mdSidenav','sbiModule_logger','$window',SvgViewerControllerFunction] );
		
function SvgViewerControllerFunction($scope, sbiModule_restServices, $mdSidenav,sbiModule_logger,$window)	{
  $scope.isSidenavOpen = false;
  $scope.layerSelected = true;
    
  $scope.openSideNav = function() {
    $mdSidenav('svgSideNav').toggle();
  };
    
  $scope.$watch('isSidenavOpen', function(isSidenavOpen) {
	  
  });
  
  //Call functions when the SVG is loaded
  angular.element(document.getElementById('svgContainer')).ready(function() {
	  //retrieve Measures
	  //$scope.getMeasures();
	  //retrieve Layers
	 // $scope.getLayers();	
	 
  });
  
  $window.document.addEventListener("SVGLoaded", function(e) {
	 // alert(e.detail); // Prints "Example of an event"
	  //retrieve Measures
	  $scope.getMeasures();
	  //retrieve Layers
	  $scope.getLayers();
	  $scope.getLegendColors();
	});
  
  
  /**
   * Loads the measures list with a REST service
   * */
  $scope.getMeasures = function(){
	  sbiModule_restServices.get("1.0/svgviewer", 'getMeasures').success(
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
	  sbiModule_restServices.get("1.0/svgviewer", 'getLayers').success(
			  function(data, status, headers, config) {
				  if (data.hasOwnProperty("errors")) {
					  sbiModule_logger.log("layers not retrivied");
				  } else {
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