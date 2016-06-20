
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
	</style>
	</head>

	
	<body>        
        
			<div ng-app="svgViewerApp">
			    <div ng-controller="SvgViewerController">
			        <md-sidenav md-disable-backdrop md-component-id="svgSideNav" md-is-open="isSidenavOpen" class="md-sidenav-<%= propertiesPanelPosition %>"  >
			           	Properties
			            <br/>
					    <md-radio-group ng-model="measureValue">
					   		 Measures
					      <md-radio-button  ng-repeat="(key,prop) in measures track by $index" ng-value="prop.columnId"  ng-click="changeSelectedMeasure(prop.columnId,prop.description)">
					      	{{prop.description}}                 
					      </md-radio-button>
					    </md-radio-group>		
					    <br/>
					    Layers
					    <br/>					    
					    <md-checkbox ng-model="layerSelected"  ng-repeat="(key,prop) in layers track by $index" ng-click="changeSelectedLayer(prop.name,!layerSelected)">
					      	{{prop.description}}                 
					     </md-checkbox>             
			        </md-sidenav>
			        <md-button class="sidenavOpenButton" ng-click="openSideNav()">
			              <i class="fa fa-2x fa-bar-chart" aria-hidden="true"></i>
			        </md-button>
			         <md-content layout-fill layout="column">
			            
			            <div id="container" layout-fill>
							<iframe id="svgContainer" name="iframe_1"
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
	
	
	
	
	
    