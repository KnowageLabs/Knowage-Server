<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%-- <%@include file="/WEB-INF/jsp/commons/angular/includeMessageResource.jspf"%> --%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="geoManager">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!-- openlayer import -->
<link rel="stylesheet" href="http://openlayers.org/en/v3.10.1/css/ol.css" type="text/css">
<script src="http://openlayers.org/en/v3.10.1/build/ol.js" type="text/javascript"></script>

<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/geo/Ellipsoid.js"></script>
<script type="text/javascript" src="/athena/js/src/angular_1.4/tools/geo/geoController.js"></script>




<!-- geo-map import -->

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/geo/geoMap/geoMapController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/geo/geoRigthMenu/geoRigthMenuController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/geo/geoBottom/geoLayers/geoLayersController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/geo/geoBottom/geoDistanceCalculator/geoDistanceCalculatorController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/geo/geoBottom/geoLegend/geoLegendController.js"></script>	

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/geo/config/geoConfig.js"></script>	

<!--  angulartable -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>


<title>Insert title here</title>

</head>
<body class="mapBodyStyle">

<div ng-controller="mapCtrl" ng-cloak>

	<geo-map map-id='myMap'></geo-map>

</div>

</body>
<style>

      

      
      

	    
      geo-layers #showLayers{
      position: absolute;
		top: 0px;
    	left: -5px;
      }
      
      geo-legend{
      z-index:1;
      position: absolute;
      bottom: 0px;
      left:5px;}
      
      geo-distance-calculator{
      z-index:1;
      position: absolute;
      bottom: 0px;
      left:55px;}
      

      
       .mapBodyStyle{
       overflow: hidden;}
       
       
       
       
       
#map{
position: relative;}     
     
   #map .ol-zoom {
    top: 3.5em;
    left: .5em;
}
 

#geoRigthMenu md-divider{
    margin: 8px 0;
    }
    
    
#geoRigthMenu p.titleStyle{
    margin: 0;
    font-size: large;
    }
    
#geoRigthMenu  .md-sidenav-right md-content.contentStyle,#geoLayerMenu  .md-sidenav-left md-content.contentStyle{
    background-color: #EBEBEB;
     height: calc( 100% - 30px);  
      padding: 0 5;
    }



#geoRigthMenu .whiteBG{
background-color: white;
}

 





#geoRigthMenu button#showMenu{
       top: 0px;
       right: 0px;
       position: absolute;
/*        background-color: rgb(158,158,158); */
}


#geoRigthMenu .md-sidenav-right md-toolbar,#geoLayerMenu .md-sidenav-left md-toolbar{
    min-height: 30;
    height: 30px;
}



.itemboxGU md-list-item, .itemboxGU md-list-item button,
	.itemboxGU md-list-item .md-list-item-inner {
/* 	min-height: 15px !important; */
min-height: 22px !important;
}

 md-list-item{
border-bottom: 2px solid #F9F9F9;
}

.itemboxGU .checkedIndicator{
	position: absolute;
    left: 0;
    color: #39C739;
        margin: 0 !important;
    }
    
    .itemboxGU  .indicatorList md-list-item button {
padding-left: 30px;
}

.itemboxGU  .indicatorList md-list-item button:hover {
    background-color: rgba(66, 132, 166, 0.49);
}
    
.itemboxGU {
    margin-top: 10px;
}

.itemboxGU .expanderIcon{
    min-height: 30px;
    position: absolute;
    top: 0;
    right: 0;
    height: 30px;
    width: 30px;
    
    }

.itemboxGU md-toolbar.titleToolbar{
    background-color: rgb(218, 209, 209);
}

.itemboxGU md-toolbar.titleToolbar:after {
	border-top: 10px solid #9E9E9E;
    border-right: 0px solid transparent;
    border-left: 10px solid transparent;
    content: "";
    position: absolute;
    left: 0px;
}

.itemboxGU md-toolbar.titleToolbar:before {
	border-top: 10px solid #9E9E9E;
    border-right: 10px solid transparent;
    border-left: 0px solid transparent;
    content: "";
    position: absolute;
    right: 0px;
    bottom: -10px;
}



.itemboxGU md-toolbar.categoryToolbar{
    background-color:rgb(59, 102, 140);
        width: calc(100% - 20px);
            color: white;
}


/* freccia sulla categoria */
/* .itemboxGU md-toolbar.categoryToolbar:after { */
/* 	border-top: 15px solid transparent; */
/*     border-bottom: 15px solid transparent; */
/*     border-left: 20px solid rgb(59, 102, 140); */
/*     content: ""; */
/*     position: absolute; */
/*     right: -20px; */
/*     top: 0; */
/* } */





.itemboxGU md-content{
    background-color: rgb(251, 251, 251);;
    margin-left: 10px;
    margin-right: 10px;
        min-height: 10px;}
       
    
 geo-legend .legendBox .colorbox{
	height: 18px;
	width: 18px;
	border-radius: 50%;
}

geo-legend .legendBox {
	background-color: white;
	border-radius: 5px;
	padding: 5px;
}

geo-legend  .legendBox:before {
	border-top: 7px solid white;
	border-right: 6px solid transparent;
	border-left: 6px solid transparent;
	content: "";
	position: absolute;
	bottom: 45px;
	left: 23px;
}

geo-legend  .changeStyleButton {
	    position: absolute;
    /* top: -8px; */
    z-index: 21 !important;
    width: 20px !important;
    height: 20px !important;
    min-height: 20px;
    margin: 0;
    /* right: -8px; */
    background-color: green!important;
    left: 3px;
    bottom: 28px;
}


geo-legend  .changeStyleButton md-icon {
    line-height: 20px;
    margin-left: -2px;
}

 
 geo-distance-calculator .clearMeasure {
	position: absolute;
	bottom: 25px;
	z-index: 21 !important;
	width: 20px !important;
	height: 20px !important;
	min-height: 20px;
}

geo-distance-calculator .clearMeasure md-icon {
	line-height: 19px;
	margin-left: -1px;
}

geo-distance-calculator md-input-container {
	padding: 3px;
}



geo-distance-calculator .calcBox {
	background-color: white;
	border-radius: 5px;
}

geo-distance-calculator .calcBox:before {
	border-top: 7px solid white;
	border-right: 6px solid transparent;
	border-left: 6px solid transparent;
	content: "";
	position: absolute;
	bottom: 45px;
	margin-left: -25px;
	left: 50%;
}

geo-distance-calculator .calcBox .measureDisplay {
	margin: 0px;
	text-align: center;
}

  .tooltip {
	position: relative;
	background: rgba(0, 0, 0, 0.5);
	border-radius: 4px;
	color: white;
	padding: 4px 8px;
	opacity: 0.7;
	white-space: nowrap;
}

 .tooltip-measure {
	opacity: 1;
	font-weight: bold;
}

 .tooltip-static {
	background-color: #ffcc33;
	color: black;
	border: 1px solid white;
}

 .tooltip-measure:before,  .tooltip-static:before {
	border-top: 6px solid rgba(0, 0, 0, 0.5);
	border-right: 6px solid transparent;
	border-left: 6px solid transparent;
	content: "";
	position: absolute;
	bottom: -6px;
	margin-left: -7px;
	left: 50%;
}

.tooltip-static:before {
	border-top-color: #ffcc33;
}   


 .toolbarBotton{
    min-height: 25px !important;
    height: 28px !important;
    width: 28px !important;
    line-height: 28px !important;
    margin: 0 10px;
    top:0 !important;
}
.toolbarBotton md-icon{
margin: -7px 0 0 -3px;
}

.zoomAnimation{
  -webkit-transition: all 0.5s ease-in-out !important;
    -moz-transition: all 0.5s ease-in-out !important;
    -o-transition: all 0.5s ease-in-out !important;
    transition: all 0.5s ease-in-out !important;
    }

.myAnim{
transition: all .5s linear all;
    overflow: hidden
    }

      
 </style>
</html>