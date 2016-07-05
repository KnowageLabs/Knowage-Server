
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

	<title>SVG Viewer</title>
	
	<style type="text/css">
		.sidenavOpenButton {
			position:absolute;
			top:0px;
			left:0px;
			z-index: 2;
			
		} 
		
		.backButton {
			position:absolute;
			top:0px;
			right:0px;
			z-index: 2;
			
		} 
	</style>
	</head>

	
	<body>        
        
			<div ng-app="svgViewerApp">
			    <div ng-controller="SvgViewerController">
			        <md-sidenav layout="column" md-component-id="svgSideNav"  class="md-sidenav-<%= propertiesPanelPosition %>"  >
			           <md-toolbar class="secondaryToolbar" layout="row" layout-align="start center"> 
			           	<span layout-padding>Measures </span>	           
			           </md-toolbar>
			           <div>
					    <md-radio-group ng-model="measureValue" layout-padding layout="column" layout-align="center start">
					      <md-radio-button  class="noPadding" ng-repeat="(key,prop) in measures track by $index" ng-value="prop.columnId"  ng-click="changeSelectedMeasure(prop.columnId,prop.description)">
					      	{{prop.description}}                 
					      </md-radio-button>
					    </md-radio-group>					           
			           </div>
			           <md-toolbar class="secondaryToolbar" layout="row" layout-align="start center"> 
			           	<span layout-padding>Layers</span>	           
			           </md-toolbar>
					   <div layout="row" layout-wrap layout-padding>
						    <md-checkbox class="noPadding" flex="100" ng-model="prop.selected"  ng-repeat="(key,prop) in layers track by $index" ng-click="changeSelectedLayer(prop.name,!prop.selected)">
						      	{{prop.description}}                 
						     </md-checkbox>     
					    </div>			    
						<md-toolbar class="secondaryToolbar" layout="row" layout-align="start center"> 
			           		<span layout-padding>Legend</span>	           
			            </md-toolbar>
			            <div layout="row" layout-wrap layout-padding>
			            </div>
					     <md-card ng-repeat="color in legend.colors track by $index" ng-style="{'background-color':color}" style="padding:5px;">
					     	 [{{legend.tresholds[$index] | number:2}} - {{legend.tresholds[$index+1] | number:2}}] 
					     </md-card>        
			        </md-sidenav>
			        <md-button class="sidenavOpenButton" ng-click="openSideNav()">
			              <i class="fa fa-2x fa-bar-chart" aria-hidden="true"></i>
			        </md-button>
			        <md-button class="backButton" ng-click="goToPreviousLevel()">
			              <i class="fa fa-2x fa-arrow-circle-left" aria-hidden="true"></i>
			        </md-button>
			         <md-content layout-fill layout="column">
			            
			            <div id="container" layout-fill>
							<iframe id="svgContainer" 
								src='${pageContext.request.contextPath}/api/1.0/svgviewer/drawMap'
								width="100%" height="100%" frameborder="0"
								style="background-color: white;"> 
							</iframe>
						</div>
			          </md-content>
			    </div>
			</div>        

</body>

</html>
	
	
	
	
	
    