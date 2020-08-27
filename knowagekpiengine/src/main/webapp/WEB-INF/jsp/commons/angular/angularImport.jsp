<%-- 
	This files must be use in every 
	SpagoBI${pageContext.request.contextPath}/Knowage page that 
	makes use of AngularJS  
--%>
<script>
/*${disable.console.logging}*/
</script>
<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
<meta name="viewport" content="width=device-width">


<link rel="stylesheet" href="${pageContext.request.contextPath}/fonts/font-awesome-4.4.0/css/font-awesome.min.css">

<!-- angular reference-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-1.5.0/angular.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-1.5.0/angular-animate.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-1.5.0/angular-aria.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-1.5.0/angular-sanitize.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-1.5.0/angular-messages.min.js"></script>

<!-- angular-material-->
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourcePath(knowageContext, "/themes/commons/css/customStyle.css")%>">
<link rel="stylesheet" href="<%= KnowageSystemConfiguration.getKnowageContext() %>/js/lib/angular/angular-material_1.1.0/angular-material.min.css">

<script type="text/javascript" src="<%= KnowageSystemConfiguration.getKnowageContext() %>/js/lib/angular/angular-material_1.1.0/angular-material.min.js"></script>


<!-- context menu -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>

<!--pagination-->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/pagination/dirPagination.js"></script>
<!-- expanderBox -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/expander-box/expanderBox.js"></script>

<!-- angular tree -->
<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
	
<!-- angular table -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(knowageContext, "/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js")%>"></script>

<!-- colorpicker -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/color-picker/tinycolor-min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/color-picker/tinygradient.min.js"></script>

<script type="text/javascript" src="<%= KnowageSystemConfiguration.getKnowageContext() %>/js/lib/angular/color-picker/angularjs-color-picker.js"></script>
<link rel="stylesheet" href="<%= KnowageSystemConfiguration.getKnowageContext() %>/js/lib/angular/color-picker/angularjs-color-picker.min.css">

<!--  r-linear-gauge  -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/gaugeNgDirective/rLinearGauge/rLinearGauge.js"></script>


<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.x/module/sbiModule.js"></script>
<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jsp"%>

<!-- sbiModule_dateServices -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/angular_1.x/sbiModule_services/sbiModule_dateServices.js"></script>
<!-- sbiModule_dateServices -->
<script type="text/javascript" src="<%=urlBuilder.getResourcePath(knowageContext, "/js/src/angular_1.4/tools/commons/sbiModule_services/knModule_selections.js")%>"></script>	