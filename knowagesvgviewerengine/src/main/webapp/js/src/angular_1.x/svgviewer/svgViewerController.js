/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of tohe License, or
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

(function(){
	'use strict';
	var app = angular.module('svgViewerApp', ['ngMaterial','sbiModule']);

	app.config(['$mdThemingProvider', function($mdThemingProvider) {
		    $mdThemingProvider.theme('knowage')
		    $mdThemingProvider.setDefaultTheme('knowage');
		}]);

app.controller('SvgViewerController', ['$scope','sbiModule_restServices','$mdSidenav','sbiModule_logger','$window','sbiModule_config','$rootScope','$sce','$timeout',SvgViewerControllerFunction] );

function SvgViewerControllerFunction($scope, sbiModule_restServices, $mdSidenav,sbiModule_logger,$window,sbiModule_config,$rootScope,$sce,$timeout)	{
	
	//hidden Iframe initialization
	$scope.requestParameterMap = _requestParameterMap;
	function createForm(){

		var svgForm = document.createElement("form");
		svgForm.id="svgForm";
		svgForm.action = sbiModule_config.contextName + "/api/1.0/svgviewer/drawMap";
		svgForm.method = "post";
	    svgForm.target = "svgContainer";
	    document.body.appendChild(svgForm);
		
	    for (var k in $scope.requestParameterMap) {
			var element = document.createElement("input");
	        element.type = "hidden";
	        element.id= 'svgForm_' + k;
	        element.name = k;
	        element.value = $scope.requestParameterMap[k];
	        svgForm.appendChild(element);
		}
		svgForm.submit();
	}
	createForm();
	
	function submitForm(){
		document.getElementById("svgForm").submit();
	}
	
	function updateForm(property,value){
		var inputElement = document.getElementById("svgForm_" + property);
		if(!inputElement) {
			inputElement = document.createElement("input");
			inputElement.type = "hidden";
			inputElement.id= 'svgForm_' + property;
			inputElement.name = property;
			document.getElementById("svgForm").appendChild(inputElement);
		}
		inputElement.value = value;
	}

  $scope.showBackButton 		= false;
  //initialize for the first level
  $scope.currentLevel 			= 1;
  $scope.currentMember 			= null;
  $scope.document				= null;
  $scope.env					= null;
  //optional
  $scope.currentParent 			= null;
  $scope.svgWidth 				= null;
  $scope.svgHeight 				= null;
  $scope.numZoom 				= 0;
  $scope.sidenavOpened 			= false;
  $scope.sidenavButtonOffset 	= 0;
  $scope.infoText				= null;
  $scope.showInfo				= false;
  $scope.cursorX, $scope.cursorY;

  //stack that contains the drill path elements
  $scope.drillPathStack = [];
  $scope.noError = false;

  $scope.openSideNav = function(customized) {
	  	if(customized){
	  		$mdSidenav('svgSideNav').toggle();
	  	}else{
	  		$scope.sidenavOpened = !$scope.sidenavOpened;
	  	}
  };

  //Goes back to the previous level
  $scope.goToPreviousLevel = function(){
	  $scope.currentLevel = $scope.currentLevel - 1;

	  var pathElement = $scope.drillPathStack.pop();
	  $scope.currentMember = pathElement.member;
	  $scope.currentParent = pathElement.parent;
	  $scope.document = pathElement.document;
	  $scope.env = pathElement.env;

	  if (pathElement.level == 1){
		  updateForm('level',pathElement.level+pathElement.env);
		  updateForm('document',pathElement.document);
	  } else {
		  updateForm('level',pathElement.level+pathElement.env);
		  updateForm('member',pathElement.member);
		  updateForm('document',pathElement.document);
		  if (pathElement.parent != undefined && pathElement.parent != null){
			  updateForm('parent',pathElement.parent);
		  }
	  }
	  submitForm();

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
	  $scope.getInfoText();
	  $scope.hideTooltip();

	  $scope.noError = true;
	});

  //Listener called when an element on the svg is clicked
  $window.document.addEventListener("SVGElementClicked", function(e) {
	  $scope.resetElements();


	  //update drill path with stack
	  var pathElement = new Object();
	  pathElement.level = $scope.currentLevel;
	  pathElement.member = $scope.currentMember;
	  pathElement.parent = $scope.currentParent;
	  pathElement.document = $scope.document || e.detail.document;
	  pathElement.env = $scope.env || e.detail.env;
	  $scope.drillPathStack.push(pathElement);

	  $scope.currentLevel = $scope.currentLevel +1;

	  //check if the member name is specified in the dataset configuration o directly from the svg id
	  if (e.detail.memberName != undefined && e.detail.memberName != null){
		  $scope.currentMember = e.detail.memberName;
		  $scope.currentDocument = e.detail.document;
	  } else {
		  $scope.currentMember = e.detail.idElement;
	  }
	  $scope.currentParent = e.detail.idElement;
	  $scope.document = e.detail.document;
	  $scope.env = e.detail.env;
	  updateForm('level',$scope.currentLevel);
	  updateForm('document',$scope.document+$scope.env);
	  updateForm('member',$scope.currentMember);
	  updateForm('parent',$scope.currentParent);
	  submitForm();

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

	  	//pass the values element as output parameter
	  	var crossData = JSON.parse(e.detail.values);
	  	var crossLabels = (e.detail.crossLabels) ? e.detail.crossLabels : undefined;
	  	if (crossLabels ){
	  		var crossLabelsJSON = JSON.parse(crossLabels);
	  		if (crossLabelsJSON && crossLabelsJSON.length == 0)
	  			crossLabels = undefined;
	  	}
		parent.execExternalCrossNavigation(crossData,driversParameter,crossLabels,driversParameter.DOCUMENT_LABEL);

	});

  //Listener called when an element on the svg is mouseovered and a tooltip is required
  $window.document.addEventListener("SVGElementMouseOver", function(e) {
	  	//get the element position
	  	var mouseOverElement = e.detail;

	  	var domIframe 	= document.getElementById("svgContainer");
	  	var domEl		= domIframe.contentDocument.getElementById(mouseOverElement.idElement);
	  	if (domEl){
	  		//get element position
	  		var viewportOffset = domEl.getBoundingClientRect();
	  		var top = viewportOffset.top + document.documentElement.scrollTop;

		  	var left = viewportOffset.left  + document.documentElement.scrollLeft;
		  	var tooltipText = "";

		  	//get element content (as default shows the active measure)
		  	if (!mouseOverElement.tooltipText || mouseOverElement.tooltipText == "null"){
			  	var svgwin = $scope.getSVG('svgContainer');
			  	var svgInfos = svgwin.myMapApp;
			  	if (svgInfos.curDescription){
			  		tooltipText = "<b>"+ svgInfos.curDescription +": </b>" +  domEl.getAttribute("attrib:"+svgInfos.curKpi);
			  	}
		  	}else{
		  		tooltipText = mouseOverElement.tooltipText;
		  	}

		  	//shows the tooltip ONLY if its valorized
		  	if (tooltipText && tooltipText.length > 0){
		  		//update the div content
			  	var domTooltip = document.getElementById("svgTooltip");
			  	var wEl = 0;
			  	var domLeftPanel = document.getElementById("svgInfoSidenav");
			  	var wLeftPanel =  0 + parseInt(domLeftPanel.offsetWidth);
			  	left = left + wLeftPanel;

			  	if(viewportOffset.left + viewportOffset.width + 200 >= window.innerWidth){
			  		domTooltip.style.left = left - (viewportOffset.width/2); //$scope.cursorX;
			  	}else{
			  		domTooltip.style.left = left + (viewportOffset.width/2); //$scope.cursorX;
			  	}

//		  		domTooltip.style.top = top+(viewportOffset.height/3); //$scope.cursorY;
		  		domTooltip.innerHTML = tooltipText;
		  		domTooltip.style.display = "block";
		  		//At least sets the top position (linked to the top or bottom values depending from the height of the final tooltip )
		  		if(domTooltip.offsetHeight > viewportOffset.top){
		  			domTooltip.style.top = viewportOffset.bottom - ((viewportOffset.bottom - viewportOffset.top)/3);
		  		}else if(document.documentElement.scrollTop + viewportOffset.top + viewportOffset.height + domTooltip.offsetHeight >= window.innerHeight){
	            	domTooltip.style.top = top - domTooltip.offsetHeight + ((viewportOffset.bottom - viewportOffset.top)/3);
	            }else{
	            	domTooltip.style.top = viewportOffset.bottom - ((viewportOffset.bottom - viewportOffset.top)/3);
	            }
		  	}
	  	}
	});

  //Listener called when an element on the svg is mouseout and a tooltip must be hidden
  $window.document.addEventListener("SVGElementMouseOut", function(e) {
	  $scope.hideTooltip();
  });

  /**
   * Reset informations about tooltip, info, ...
   */
  $scope.resetElements = function(){
	  $scope.hideTooltip();
	  $scope.showInfo = false;
  }

  /**
   * Loads the measures list with a REST service
   * */
  $scope.getMeasures = function(){
	  var parametersClone = angular.copy($scope.requestParameterMap);
	  parametersClone.level = $scope.currentLevel;
	  sbiModule_restServices.post("1.0/svgviewer", 'getMeasures', parametersClone).then(
			  function(response) {
				  if (response.data.hasOwnProperty("errors")) {
					  sbiModule_logger.log("measures not retrivied");
				  } else {
					  $scope.measures = response.data;
					  for (var propt in $scope.measures){
						  if ($scope.measures[propt].selected){
							  //set default selected measure
							  $scope.measureValue = $scope.measures[propt].columnId;
						  }
					  }
					  sbiModule_logger.trace("measures correctly retrieved",response.data);
				  }
			  },function(error) {
				  sbiModule_logger.log("measures not retrieved");
			  });
  };

 /**
 * Hides the tooltip div
 */
  $scope.hideTooltip = function(){
	  var domTooltip = document.getElementById("svgTooltip");
	  domTooltip.style.display = "none";
  }

  /**
   * Loads the measures list with a REST service
   * */
  $scope.getLayers = function(){
	  var parametersClone = angular.copy($scope.requestParameterMap);
	  parametersClone.level = $scope.currentLevel;
	  sbiModule_restServices.post("1.0/svgviewer", 'getLayers',parametersClone).then(
			  function(response) {
				  if (response.data.hasOwnProperty("errors")) {
					  sbiModule_logger.log("layers not retrieved");
				  } else {

					  for (var key in response.data) {
						  //force all layers to be selected by default
						  data[key].selected = true;
					  }
					  $scope.layers = response.data;
					  sbiModule_logger.trace("layers correctly retrieved",response.data);

				  }
			  },function(error) {
				  sbiModule_logger.log("layers not retrieved");
			  });
  };

  /**
   * Retrieves the SVG element from the specified container
   */
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
   * Gets the info's content and set specific div
   */
  $scope.getInfoText = function(){
	  var svgwin = $scope.getSVG('svgContainer');
      $scope.infoText =  $sce.trustAsHtml(svgwin.getInfoText());
      $scope.showInfo =  ($scope.infoText != undefined  && $scope.infoText != null && $scope.infoText != "");
  }

  /**
  * Changes the selected measure and apply the color visualization
  * */
  $scope.changeSelectedMeasure =  function(columnId,description)  {
	  var svgwin = $scope.getSVG('svgContainer');
	  //call setKPI function inside the SVG
      svgwin.setKpi('radioButtons',columnId,description);
	  $scope.getLegendColors();

  }
  /**
   * Shows or hides a specific layer
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
		  $scope.legend.labels = svgwin.myMapApp.labelArray;
		  $scope.showLabel = (svgwin.myMapApp.labelArray && svgwin.myMapApp.labelArray.length > 0 ) ? true : false;
	  }
  }

  /**
   * Manages zoom (max 200%)
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
	  }else if (type == 'zoomOut'){
		  $scope.svgWidth =  100 + (25*$scope.numZoom)-25;
		  $scope.svgHeight = 100 + (25*$scope.numZoom)-25;
		  $scope.numZoom--;
	  }
	  iframe.style.height = $scope.svgHeight + '%';
	  iframe.style.width = $scope.svgWidth + '%' ;
  }
};
})();