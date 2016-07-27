
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
	
	<style> 
		.md-accordion .expandCollapse { width:30px; height:30px; position:relative; font-size:20px; font-weight:bold; cursor:pointer; color:#fff; display:block; margin-top: -2px; margin-left: -2px; overflow:hidden; } 
		.md-accordion .expandCollapse:active { border:0px; }
		.md-accordion .expandCollapse:before, .md-accordion .expandCollapse:after { width:30px; height:30px; display:block; position:absolute; top:0; left:0; line-height:32px; text-align:center; -webkit-transition: .3s all ease-out; transition: .3s all ease-out; }
		.md-accordion .expandCollapse:before { opacity:1 -webkit-transform: rotate(0deg); transform: rotate(0deg); content: "|"; margin-top:-3px; }
		.md-accordion .expandCollapse:after { opacity:1; -webkit-transform: rotate(-90deg); transform: rotate(-90deg); content: "|"; margin-left:-3px; }
		.md-accordion .active:before { opacity:1; -webkit-transform: rotate(90deg); transform: rotate(90deg); margin-left:3px; margin-top:0px; }
		.md-accordion .dataContent { background: #F2F2F2; height:0px; overflow:hidden; -webkit-transition: .3s all ease-out; transition: .3s all ease-out; }
		/*.md-accordion .activeContent { height:30vh; padding:0; display:block; }*/
		.md-accordion .activeContent {height:auto; padding:0; display:block; }
		.md-accordion md-toolbar{ cursor:pointer; border-bottom:1px solid rgb(63,107,181) }
	</style>
	
	</head>

	
	<body class="kn-svgviewer">        
        	
			<!-- <div ng-app="svgViewerApp">
			    <div ng-controller="SvgViewerController" layout="row">
			        <md-sidenav layout="column"  ng-class="{'_md-locked-open':sidenavOpened}" id="svgInfoSidenav" md-component-id="svgSideNav" ng-show="noError" class="md-sidenav-<%= propertiesPanelPosition %>"  >
			         <md-toolbar class="secondaryToolbar" layout="row" layout-align="start center"  ng-show="showInfo"> 
			           	<span layout-padding>Info </span>	           
			           </md-toolbar>
			           <div id="info" layout="row" layout-wrap  ng-show="showInfo" ng-bind-html="infoText">              	           
			           </div>
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
			        
			        <md-button class="backButton" ng-click="goToPreviousLevel()"  ng-show="noError && showBackButton"  title="Go back to previous level">
			              <i class="fa fa-2x fa-arrow-circle-left" aria-hidden="true"></i>
			        </md-button>
			        <div layout="column" style="position:absolute;bottom:50px;right:50px;z-index:10000" >
			        	<md-button class="zoomIn" ng-click="zoom('zoomIn', $event)" ng-show="noError" ng-disabled="numZoom>=4" title="Zoom In">
				              <i class="fa fa-2x fa-plus" aria-hidden="true"></i>
				        </md-button> 
				        <md-button class="zoomOut" ng-click="zoom('zoomOut', $event)" ng-show="noError" ng-disabled="numZoom<1" title="Zoom Out">
				              <i class="fa fa-2x fa-minus" aria-hidden="true"></i>
				        </md-button>
				     </div> 
			         <md-content layout-fill layout="column"> 
				         <md-button class="sidenavOpenButton" ng-click="openSideNav()"  ng-show="noError"  title="Open options panel" ng-style="{'left':sidenavButtonOffset}">
				              <i class="fa fa-2x fa-bar-chart" aria-hidden="true"></i>
				        </md-button>					 		
   					   <div id="container" style="position:relative;" layout-fill>   		             
							<iframe id="svgContainer" 
							    src='${pageContext.request.contextPath}/api/1.0/svgviewer/drawMap'  
								width='100%'; height='100%'; frameborder="0" 
								style="background-color: white; position:relative; "> 
							</iframe>  
					    </div> 
			         </md-content>
			    </div>
			</div>      -->
			<div ng-app="svgViewerApp">
			    <div ng-controller="SvgViewerController" ng-scope style="display: flex;">
			     <md-sidenav layout="column"  ng-class="{'_md-locked-open':sidenavOpened}" id="svgInfoSidenav" md-component-id="svgSideNav" ng-show="noError" class="md-sidenav-<%= propertiesPanelPosition %>"  >
			      <section  >
				     <div class="md-accordion" layout="column">
				     		<md-toolbar ng-init="expandedInfo = false"  ng-show="showInfo" layout="row" ng-click="expandedInfo = !expandedInfo">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Info </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedInfo}"  ng-show="showInfo" ng-bind-html="infoText">
						    </div>
						    
						    <md-toolbar ng-init="expandedMeasure = false" layout="row" ng-click="expandedMeasure = !expandedMeasure">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Measures </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedMeasure}">
						    	<md-radio-group ng-model="measureValue" layout-padding layout="column" layout-align="center start">
								      <md-radio-button  class="noPadding" ng-repeat="(key,prop) in measures track by $index" ng-value="prop.columnId"  ng-click="changeSelectedMeasure(prop.columnId,prop.description)">
								      	{{prop.description}}                 
								      </md-radio-button>
								</md-radio-group>		
						    </div>
					
						    <md-toolbar ng-init="expandedLayer = false" layout="row" ng-click="expandedLayer = !expandedLayer">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Layers </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedLayer}" layout="row" layout-wrap >
						    	<md-checkbox class="noPadding" flex="100" ng-model="prop.selected"  ng-repeat="(key,prop) in layers track by $index" ng-click="changeSelectedLayer(prop.name,!prop.selected)">
						      		{{prop.description}}                 
						        </md-checkbox>     	
						    </div>
						    
						    <md-toolbar ng-init="expandedLegend = false" layout="row" ng-click="expandedLegend = !expandedLegend">
					        	<div class="md-toolbar-tools"> 
						           		<span layout-padding>Legend </span>
						        </div>	    
						    </md-toolbar>
						    <div ng-class="{dataContent:true, activeContent:expandedLegend}" layout="row" layout-wrap >
						    	  <md-card ng-repeat="color in legend.colors track by $index" ng-style="{'background-color':color}" style="padding:5px;">
							     	 [{{legend.tresholds[$index] | number:2}} - {{legend.tresholds[$index+1] | number:2}}] 
							     </md-card>   	
						    </div>
						<div>
						</section>
					</md-sidenav> 
			   	    
			       
			        <md-button class="backButton" ng-click="goToPreviousLevel()"  ng-show="noError && showBackButton"  title="Go back to previous level">
			              <i class="fa fa-2x fa-arrow-circle-left" aria-hidden="true"></i>
			        </md-button>
			        <div layout="column" style="position:absolute;bottom:50px;right:50px;z-index:10000" >
			        	<md-button class="zoomIn" ng-click="zoom('zoomIn', $event)" ng-show="noError" ng-disabled="numZoom>=4" title="Zoom In">
				              <i class="fa fa-2x fa-plus" aria-hidden="true"></i>
				        </md-button> 
				        <md-button class="zoomOut" ng-click="zoom('zoomOut', $event)" ng-show="noError" ng-disabled="numZoom<1" title="Zoom Out">
				              <i class="fa fa-2x fa-minus" aria-hidden="true"></i>
				        </md-button>
				     </div> 
			         <md-content layout-fill layout="column"> 
				         <md-button class="sidenavOpenButton" ng-click="openSideNav()"  ng-show="noError"  title="Open options panel" ng-style="{'left':sidenavButtonOffset}">
				              <i class="fa fa-2x fa-bar-chart" aria-hidden="true"></i>
				        </md-button>					 		
   					   <div id="container" style="position:relative;" layout-fill>   		             
							<iframe id="svgContainer" 
							    src='${pageContext.request.contextPath}/api/1.0/svgviewer/drawMap'  
								width='100%'; height='100%'; frameborder="0" 
								style="background-color: white; position:relative; "> 
							</iframe>  
					    </div> 
			         </md-content>
			    </div>
			</div>          
			<div id="svgTooltip" style="position:absolute; display:none; width:auto; border-radius:4px; height:auto; white-space: nowrap; font-size:small; box-shadow:1px 1px 3px 2px; background-color:#fcefbd;"></div>
</body>

</html>
	
	
	
	
	
    