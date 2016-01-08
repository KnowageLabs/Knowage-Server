<!-- openlayer import -->

<link rel="stylesheet" href="http://openlayers.org/en/v3.10.1/css/ol.css" type="text/css">
<script src="http://openlayers.org/en/v3.10.1/build/ol.js" type="text/javascript"></script>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script>
   google.load('visualization', '1.0', {'packages':['corechart', 'bar']});
</script>
	    
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/mapFilter/utils/Ellipsoid.js"></script>


<!-- geo-map import -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/mapFilter/geoModule.js"></script>
<script>
var sbiModule=angular.module('sbiModule');
sbiModule.factory('sbiModule_config',function(){
	return {
		protocol: '<%= request.getScheme()%>' ,
		host: '<%= request.getServerName()%>',
	    port: '<%= request.getServerPort()%>',
	    contextName: '/<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>',
	    <%-- 
	    controllerPath: null ,// no cotroller just servlets   
	    docLabel :"<%=docLabel%>", 
		docVersion : "<%=docVersion%>",
	 	docName :"<%=docName.replace('\n', ' ')%>",
	 	docDescription: "<%=docDescription.replace('\n', ' ')%>",
	 	docIsPublic: "<%=docIsPublic%>",
	 	docIsVisible: "<%=docIsVisible%>",
	 	docPreviewFile: "<%=docPreviewFile%>",
	 	docCommunities: "<%=docCommunity%>",
	 	docFunctionalities: "<%=docFunctionalities%>",
	 	docDatasetLabel: "<%=docDatasetLabel%>",
	 	docDatasetName: "<%=docDatasetName%>",
	 	visibleDataSet: "<%=visibleDataSet%>",
	 	userId : "<%=userId%>",
	 	docAuthor :"<%=docAuthor%>",
	    --%>
	 	externalBasePath:"<%=request.getParameter(SpagoBIConstants.SBI_CONTEXT)%>/"
	};
});
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/mapFilter/utils/geoUtils.js"></script>

<!--
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/thematizer/geoThematizer.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/mapFilter/geoMap/geoMapController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoRigthMenu/geoRigthMenuController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoMapMenu/geoMapMenuController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoLayers/geoLayersController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoDistanceCalculator/geoDistanceCalculatorController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoLegend/geoLegendController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoConfig/geoConfigController.js"></script>	
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/geo/geoCrossNavMultiselect/geoCrossNavMultiselectController.js"></script>	
-->

<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/mapFilter/css/mapFilter.css" type="text/css">