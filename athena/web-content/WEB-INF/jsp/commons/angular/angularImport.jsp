<%-- 
	This files must be use in every 
	SpagoBI${pageContext.request.contextPath}/Knowage page that 
	makes use of AngularJS  
--%>

	<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
	<meta name="viewport" content="width=device-width">
	
	<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css">
	
	<!-- angular reference-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-sanitize.min.js"></script>
	
	
	<!-- angular-material-->
	<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/angular-material_0.10.0/angular-material.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>
	
	<!-- angular tree -->
	<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
	
	<!-- angular list -->
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/angular-list.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/AngularList.js"></script>		
	
	<!-- context menu -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/pagination/dirPagination.js"></script>

	<!-- angular table -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-table/AngularTable.js"></script>

	<!-- 	angular time picker -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js"></script>

<!-- 	angular 2 column template -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/angular-2-col/angular2Col.js"></script>


<%-- 	<%@include file="/WEB-INF/jsp/commons/angular/includeMessageResource.jspf"%> --%>


	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/RestService.js"></script> <!-- da rimuovere -->
	<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>
	
