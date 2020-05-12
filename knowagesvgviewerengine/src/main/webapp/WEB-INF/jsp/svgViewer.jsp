
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	
	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>



<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	
	<head>
		<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
		<%@include file="/WEB-INF/jsp/commons/angular/svgViewerImport.jsp"%>
	
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/svgviewer/svgViewerController.js"></script>
		<script>var _requestParameterMap = <%=new ObjectMapper().writeValueAsString(request.getParameterMap())%>;</script>
		<% if (isCustomizedSVG) {%>
			<%-- This is because dynamicSvg.js is plain javascript, and it is impossible to get the angular scope ---------------- --%>
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/svgviewer/directives/dynamicSvg.js"></script>
		<% } %>
		<title>SVG Viewer</title>
	</head>

	
	<body class="kn-svgviewer">    
	          	
			<div ng-app="svgViewerApp" class="svgMainContainer">
			     <div  class="divFlex" ng-controller="SvgViewerController" ng-scope> 
			     <% if (isCustomizedSVG) {%><md-sidenav layout="column"  id="svgInfoSidenav" md-component-id="svgSideNav" ng-show="<%=propertiesPanelVisible%> && noError" class="md-sidenav-<%= propertiesPanelPosition %>"  ><% }else{ %>
			      <md-sidenav layout="column" ng-class="{'_md-locked-open':sidenavOpened}" id="svgInfoSidenav" md-component-id="svgSideNav" ng-show="<%=propertiesPanelVisible%> && noError" class="md-sidenav-<%= propertiesPanelPosition %>"  ><% } %>
			      <section  >
				     <div class="md-accordion" layout="column">
				     		<md-toolbar ng-init="expandedInfo = true"  ng-show="showInfo" layout="row" ng-click="expandedInfo = !expandedInfo">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Info </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedInfo}"  ng-show="showInfo" ng-bind-html="infoText">
						    </div>
						    
						    <md-toolbar ng-init="expandedMeasure = false" layout="row" ng-click="expandedMeasure = !expandedMeasure" ng-show="<%=propertiesPanelVisibleMeasures%>">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Measures </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedMeasure}"  ng-show="<%=propertiesPanelVisibleMeasures%>" >
						    	<md-radio-group ng-model="measureValue" layout-padding layout="column" layout-align="center start">
								      <md-radio-button  class="noPadding" ng-repeat="(key,prop) in measures track by $index" ng-value="prop.columnId"  ng-click="changeSelectedMeasure(prop.columnId,prop.description)">
								      	{{prop.description}}                 
								      </md-radio-button>
								</md-radio-group>		
						    </div>
					
						    <md-toolbar ng-init="expandedLayer = false" layout="row" ng-click="expandedLayer = !expandedLayer" ng-show="<%=propertiesPanelVisibleLayers%>">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Layers </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedLayer}" layout="row" layout-wrap  ng-show="<%=propertiesPanelVisibleLayers%>">
						    	<md-checkbox class="noPadding" flex="100" ng-model="prop.selected"  ng-repeat="(key,prop) in layers track by $index" ng-click="changeSelectedLayer(prop.name,!prop.selected)">
						      		{{prop.description}}                 
						        </md-checkbox>     	
						    </div>
						    
						    <md-toolbar ng-init="expandedLegend = false" layout="row" ng-click="expandedLegend = !expandedLegend" ng-show="<%=propertiesPanelVisibleMeasures%>">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Legend </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedLegend}" layout="row" layout-wrap ng-show="showLabel && <%=propertiesPanelVisibleMeasures%>" >
						    	  <md-card ng-repeat="color in legend.colors track by $index" ng-style="{'background-color':color}" layout-padding">
							     	 [{{legend.labels[$index]}}] 
							     </md-card>   	
						    </div>
						    <div ng-class="{dataContent:true, activeContent:expandedLegend}" layout="row" layout-wrap ng-show="!showLabel && <%=propertiesPanelVisibleMeasures%>" >
						    	  <md-card ng-repeat="color in legend.colors track by $index" ng-style="{'background-color':color}" layout-padding">
							     	 [{{legend.tresholds[$index] | number:2}} - {{legend.tresholds[$index+1] | number:2}}] 
							     </md-card>   	
						    </div>
						<div>
						</section>
					</md-sidenav> 
			   	    
			       
			        <md-button class="backButton" ng-click="goToPreviousLevel()"  ng-show="noError && showBackButton"  title="Go back to previous level">
			              <i class="fa fa-2x fa-arrow-circle-left" aria-hidden="true"></i>
			        </md-button>
			        <% if (!isCustomizedSVG) {%>
				        <div layout="column" class="zoomButton"  >
				        	<md-button class="zoomIn" ng-click="zoom('zoomIn', $event)" ng-show="noError" ng-disabled="numZoom>=4" title="Zoom In">
					              <i class="fa fa-2x fa-plus" aria-hidden="true"></i>
					        </md-button> 
					        <md-button class="zoomOut" ng-click="zoom('zoomOut', $event)" ng-show="noError" ng-disabled="numZoom<1" title="Zoom Out">
					              <i class="fa fa-2x fa-minus" aria-hidden="true"></i>
					        </md-button>
					     </div> 
					   <% } %>
			         <md-content class="ie11fix" layout-fill layout="column"> 
				         <md-button class="sidenavOpenButton" ng-click="openSideNav(<%=isCustomizedSVG %>)"  ng-show="<%=propertiesPanelVisible%> && noError"  title="Open options panel" ng-style="{'left':sidenavButtonOffset}">
				              <i class="fa fa-2x fa-bar-chart" aria-hidden="true"></i>
				        </md-button>					 		
   					   <div id="container" layout-fill>   		             
							<iframe id="svgContainer" width='100%'; height='100%'; frameborder="0" name="svgContainer"> </iframe>  			
							<% if (isCustomizedSVG) {%>
								<div id="dynamic-svg"></div> 
								<div id="graphLegend"></div>
							<% } %>
					    </div> 
			         </md-content>
			    </div>
			</div>          
			<div id="svgTooltip" ></div>
	</body>

</html>
	
	
	
	
	
    