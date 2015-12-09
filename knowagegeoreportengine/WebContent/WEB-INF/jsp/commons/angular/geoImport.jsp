<!-- openlayer import -->

<link rel="stylesheet" href="http://openlayers.org/en/v3.10.1/css/ol.css" type="text/css">
<script src="http://openlayers.org/en/v3.10.1/build/ol.js" type="text/javascript"></script>



<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script>
   google.load('visualization', '1.0', {'packages':['corechart', 'bar']});
</script>
	    

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/utils/Ellipsoid.js"></script>

<!-- geo-map import -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/module/geoModule.js"></script>
<%@include file="/WEB-INF/jsp/commons/angular/geoModule.jsp"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/utils/geoUtils.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/thematizer/geoThematizer.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoMap/geoMapController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoRigthMenu/geoRigthMenuController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoMapMenu/geoMapMenuController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoLayers/geoLayersController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoDistanceCalculator/geoDistanceCalculatorController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoLegend/geoLegendController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoConfig/geoConfigController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoCrossNavMultiselect/geoCrossNavMultiselectController.js"></script>	

<script src="http://api.tiles.mapbox.com/mapbox.js/plugins/turf/v2.0.0/turf.min.js"></script>	
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/angularGeoReport.css" type="text/css">