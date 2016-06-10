
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
	
	</head>
	
	<body>        
        
			<div ng-app="myapp">
			    <div layout="column" ng-controller="MyController">
			        <md-sidenav md-disable-backdrop md-component-id="left" md-is-open="isSidenavOpen" class="md-sidenav-left">
			            Left Nav!
			        </md-sidenav>
			         <md-content>
			            <md-button ng-click="openLeftMenu()">
			              Open Side Nav
			            </md-button>
			            <div id="container">
							<object id="svgContainer" width="100%" height="100%"
								data="http://localhost:8080/knowagesvgviewerengine/api/1.0/svgviewer/drawMap"
								type="image/svg+xml"> Your browser does not support SVG 
							</object>
						</div>
			          </md-content>
			    </div>
			</div>        
        	
		 
		 <!-- 
		<iframe id="iframe_1"
		        name="iframe_1"
		        src='http://localhost:8080/knowagesvgviewerengine/api/1.0/svgviewer/drawMap'
		        width="100%"
		        height="100%"
		        frameborder="0"
		        style="background-color:white;">
		</iframe>
		 -->
		
		


</body>

</html>
	
	
	
	
	
    