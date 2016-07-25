/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
var app = angular.module('svgViewerApp', ['ngMaterial','sbiModule']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {
	    $mdThemingProvider.theme('knowage')
	    $mdThemingProvider.setDefaultTheme('knowage');
	}]);

app.controller('SvgViewerController', ['$scope','sbiModule_restServices','$mdSidenav','sbiModule_logger','$window','sbiModule_config','$rootScope',SvgViewerControllerFunction] );
		
function SvgViewerControllerFunction($scope, sbiModule_restServices, $mdSidenav,sbiModule_logger,$window,sbiModule_config,$rootScope)	{

  //$scope.isSidenavOpen 	= false;
  $scope.showBackButton 		= false;
  //initialize for the first level
  $scope.currentLevel 			= 1;
  $scope.currentMember 			= null;
  //optional
  $scope.currentParent 			= null;
  $scope.svgWidth 				= null;
  $scope.svgHeight 				= null;
  $scope.numZoom 				= 0;  
  $scope.sidenavOpened 			= false;
  $scope.sidenavButtonOffset 	= 0;
  
  $scope.cursorX, $scope.cursorY;
	
  
  //stack that contains the drill path elements
  $scope.drillPathStack = [];
  $noError = false;
    
  $scope.openSideNav = function() {
    //$mdSidenav('svgSideNav').toggle();
	  $scope.sidenavOpened = !$scope.sidenavOpened;

  };
  
  //Go back to the previous level
  $scope.goToPreviousLevel = function(){
	  $scope.currentLevel = $scope.currentLevel - 1;
	  
	  var pathElement = $scope.drillPathStack.pop();
	  $scope.currentMember = pathElement.member;
	  $scope.currentParent = pathElement.parent;

	  
	  if (pathElement.level == 1){
		  document.getElementById('svgContainer').src = sbiModule_config.contextName+"/api/1.0/svgviewer/drillMap?level="+pathElement.level;
	  } else {
		  var urlToCall = sbiModule_config.contextName+"/api/1.0/svgviewer/drillMap?member="+pathElement.member+"&level="+pathElement.level;
		  if (pathElement.parent != undefined && pathElement.parent != null){
			  urlToCall = urlToCall + "&parent=" + pathElement.parent;
		  }
		  document.getElementById('svgContainer').src = urlToCall;
	  }
	  
	  if($scope.currentLevel == 1){
		  $scope.showBackButton = false;
	  }
  }
    
  
  $window.document.addEventListener("SVGLoaded", function(e) {
	  //retrieve Measures
	  $scope.getMeasures();
	  //retrieve Layers
	  $scope.getLayers();
	  $scope.getLegendColors();
	  
	  $scope.noError = true;
	});
  
  //Listener called when an element on the svg is clicked
  $window.document.addEventListener("SVGElementClicked", function(e) {
	  $scope.hideTooltip();
	  //update drill path with stack
	  var pathElement = new Object();
	  pathElement.level = $scope.currentLevel;
	  pathElement.member = $scope.currentMember;
	  pathElement.parent = $scope.currentParent;
	  $scope.drillPathStack.push(pathElement);
	  
	  //alert("Clicked element with id "+e.detail);  
	  $scope.currentLevel = $scope.currentLevel +1;
	  
	  //check if the member name is specified in the dataset configuration o directly from the svg id
	  if (e.detail.memberName != undefined && e.detail.memberName != null){
		  $scope.currentMember = e.detail.memberName;
		  $scope.currentParent = e.detail.idElement;
		  document.getElementById('svgContainer').src = sbiModule_config.contextName+"/api/1.0/svgviewer/drillMap?member="+e.detail.memberName+"&level="+$scope.currentLevel+"&parent="+e.detail.idElement;
	  } else {
		  //get svg element's id 
		  $scope.currentMember = e.detail.idElement;
		  $scope.currentParent = null;
		  document.getElementById('svgContainer').src = sbiModule_config.contextName+"/api/1.0/svgviewer/drillMap?member="+e.detail.idElement+"&level="+$scope.currentLevel;
	  }
	  
	  
	  if  ($scope.currentLevel > 1){
		  $scope.showBackButton = true;
	  }
	  $scope.noError = false;
	  $scope.$apply();
	});
  
  //Listener called when an element on the svg is clicked and cross navigation is required
  $window.document.addEventListener("SVGElementClickedCrossNavigation", function(e) {
	  	$scope.hideTooltip();
	  	var driversParameter = getDriverParameters();
	  	
	  	//pass the clicked element id as output parameter
	  	var clickedElement = e.detail;
	  	var crossData=[];
	  	var object = new Object();
	  	object.ELEMENT_ID = clickedElement;
	  	crossData.push(object);
		parent.execExternalCrossNavigation(crossData,driversParameter,undefined,driversParameter.DOCUMENT_LABEL);

	});
  
  //Listener called when an element on the svg is mouseovered and a tooltip is required
  $window.document.addEventListener("SVGElementMouseOver", function(e) {
	  	//get the element position
	  	var mouseOverElement = e.detail;		  	
	  	
	  	var domIframe = document.getElementById("svgContainer");
	  	var domEl = domIframe.contentDocument.getElementById(mouseOverElement.idElement);
	  	if (domEl){	 	
	  		//get element position
	  		var viewportOffset = domEl.getBoundingClientRect();
		  	var top = viewportOffset.top;
		  	var left = viewportOffset.left;
		  	var tooltipText = "";
		  	
		  	//get element content (as default shows the active measure)
		  	if (!mouseOverElement.tooltipText){
			  	var svgwin = $scope.getSVG('svgContainer');
			  	var svgInfos = svgwin.myMapApp;
			  	tooltipText = "<b>"+ svgInfos.curDescription +": </b>" +  domEl.getAttribute("attrib:"+svgInfos.curKpi);
		  	}else{
		  		tooltipText = mouseOverElement.tooltipText;
		  	}	  	
	  		
		  	//shows the tooltip ONLY if its valorized
		  	if (tooltipText && tooltipText.length > 0){
		  		//update the div content
			  	var domTooltip = document.getElementById("svgTooltip");
			  	var w =  parseInt(domEl.getAttribute("width"));
		  		domTooltip.style.left = left + parseInt(domEl.getAttribute("width")); //$scope.cursorX; 
		  		domTooltip.style.top = top; //$scope.cursorY; 
		  		domTooltip.innerHTML = tooltipText;
		  		domTooltip.style.display = "block";
		  	
		  	}
	  	}
	});
  
  //Listener called when an element on the svg is mouseout and a tooltip must be hidden
  $window.document.addEventListener("SVGElementMouseOut", function(e) {
	  $scope.hideTooltip();
//	  var domTooltip = document.getElementById("svgTooltip");
//	  domTooltip.style.display = "none";
  });
  
  
  $window.document.addEventListener("SVGElementMouseMove", function(e) {
			$scope.cursorX = e.pageX;
			$scope.cursorY = e.pageY;
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
	
	
  $scope.hideTooltip = function(){
	  var domTooltip = document.getElementById("svgTooltip");
	  domTooltip.style.display = "none";
  }

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
  
  /**
   * Manage zoom 
   * */
  $scope.zoom = function(type, evt){
	  var svgobj = $scope.getSVG('svgContainer'); 	//svg element
	  var svgwin = document.getElementById("svgContainer"); //iframe element
	  var viewBox = svgobj.myMainMap.mapSVG.getAttributeNS(null,"viewBox");
	  var viewBoxArray = viewBox.split(" ");
//	  set new iframe dimensions
	  var iframe =  document.getElementById("svgContainer"); 
	  var container = document.getElementById("container");
	  var height = container.offsetHeight;
	  var width = container.offsetWidth;
	  
	  var svgwidth = svgobj.myMainMap.mapSVG.getAttribute("width");
	  var svgheight = svgobj.myMainMap.mapSVG.getAttribute("height");
	  
	  //sets default values
	  if ($scope.svgHeight==null) $scope.svgHeight = 100; //alias 100%
	  if ($scope.svgWidth==null) $scope.svgWidth = 100;
	  
	  if (type == 'zoomIn'){
		  $scope.numZoom++;
		  $scope.svgWidth =  100+(25*$scope.numZoom);
		  $scope.svgHeight =  100+(25*$scope.numZoom);
//		  svgobj.myMainMap.mapSVG.setAttributeNS(null,"width",  $scope.svgWidth + '%');
//		  svgobj.myMainMap.mapSVG.setAttributeNS(null,"height", $scope.svgHeight + '%'); 
	  }else if (type == 'zoomOut'){		  		  		 
		  $scope.svgWidth =  100 + (25*$scope.numZoom)-25;
		  $scope.svgHeight = 100 + (25*$scope.numZoom)-25;
//		  svgobj.myMainMap.mapSVG.setAttributeNS(null,"width",  $scope.svgWidth + '%');
//		  svgobj.myMainMap.mapSVG.setAttributeNS(null,"height", $scope.svgHeight + '%');
		  $scope.numZoom--;	 
	  }	  
	  iframe.style.height = $scope.svgHeight + '%';   
	  iframe.style.width = $scope.svgWidth + '%' ;
	  
//	  viewBox =  viewBoxArray[0]+" " +viewBoxArray[1] + " " + viewBoxArray[2] + " " + viewBoxArray[3];
//	  svgobj.myMainMap.mapSVG.setAttributeNS(null,"viewBox", viewBox);
	  //scale ONLY the svg
//	  svgobj.zoomImageButtons(type, evt);
  }
};