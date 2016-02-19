<%-- 
	This files must be use in every 
	SpagoBI${pageContext.request.contextPath}/Knowage page that 
	makes use of AngularJS  
--%>

<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no" />
	<meta name="viewport" content="width=device-width">
	
	<link href="https://fonts.googleapis.com/icon?family=Material+Icons"   rel="stylesheet">
	
	<link rel="stylesheet" href="${pageContext.request.contextPath}/fonts/font-awesome-4.4.0/css/font-awesome.min.css">

	<!-- angular reference-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-animate.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-aria.min.js"></script>
	
	<!-- angular-material-->
	<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/angular-material_0.10.0/angular-material.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-material_0.10.0/angular-material.js"></script>
	
	<!-- angular-sanitize  -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-sanitize/angular-sanitize.js"></script>
	
	<!-- context menu -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/pagination/dirPagination.js"></script>

	<!-- angular tree -->
	<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-tree/angular-ui-tree.js"></script>
	
	<!-- ng-draggable -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/ng-draggable/ngDraggable.js"></script>
	
	<!-- toastr -->
	<link rel="stylesheet" 	href="${pageContext.request.contextPath}/js/lib/angular/toastr/angular-toastr.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/toastr/angular-toastr.tpls.js"></script>
	
	<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>
