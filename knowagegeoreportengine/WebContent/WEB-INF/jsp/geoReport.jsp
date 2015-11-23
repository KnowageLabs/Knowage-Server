<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="geoManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/commons/angular/geoImport.jsp"%>


<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoController.js"></script>

<title>GeoReport</title>

</head>
<body class="mapBodyStyle">

<div ng-controller="mapCtrl" ng-cloak>

	<geo-map map-id='myMap'></geo-map>
	
	<%--

<!--  						 columns='["layerLabel","type","layerURL","baseLayer"]' -->
<!-- 						columns='[{"label":"layerLb","name":"layerLabel"},{"label":"type","name":"type","size":"100px"},{"label":"layerURL","name":"layerURL"}]' -->
	
<!-- 	<div layout="column"> -->
	
<!-- 	<md-input-container flex  md-no-float class="tableSearchBar" ng-init="showSearch=false;"  > -->
<!-- 		<input  ng-model="itemc"  type="text" placeholder="Search "> -->
<!-- 	</md-input-container> -->
	
	
<!-- 	<angular-table  id="layerCatalogue" -->
<!-- 						 ng-model=mydata.root -->
<!-- 						 columns='[{"label":"Name","name":"layerLabel"},{"label":"Name","name":"name"},{"label":"Type","name":"type","size":"100px"},{"label":"URL","name":"layerURL"}]' -->
<!-- 						 columns-search='["layerLabel","type"]' -->
<!-- 						 highlights-selected-item=true -->
<!-- 						click-function=toggleLayerFromCatalogue(item) -->
<!-- 						menu-option=menuOpt -->
<!-- 						page-canged-function = "pageChanged(newPageNumber,itemsPerPage,searchValue, columnOrdering, reverseOrdering)" -->
<!-- 						speed-menu-option=SpeedMenuOpt   -->
<!-- 		></angular-table> -->
<!-- 	</div> -->
	
   --%>

</div>

</body>

</html>