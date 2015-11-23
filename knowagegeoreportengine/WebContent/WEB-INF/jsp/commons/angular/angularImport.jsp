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
	
	
	<!-- context menu -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/contextmenu/ng-context-menu.min.js"></script>
	
	<!--pagination-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/pagination/dirPagination.js"></script>

<!-- angular table -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular-table/AngularTable.js"></script>



<%-- TODO modificare in js  --%> 
	<%@include file="/WEB-INF/jsp/commons/angular/sbiModule.jspf"%>
	
	
<script>
var sbiM=angular.module('sbiModule');

sbiM.factory('sbiModule_config',function(){
	return {
		protocol: '<%= request.getScheme()%>' ,
		host: '<%= request.getServerName()%>',
	    port: '<%= request.getServerPort()%>',
	    contextName: '/<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>',
	    controllerPath: null ,// no cotroller just servlets   
	    docLabel :"<%=docLabel%>",
		 docVersion : "<%=docVersion%>",
		 userId : "<%=userId%>",
		 docAuthor :"<%=docAuthor%>",
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
		 externalBasePath:"<%=request.getParameter(SpagoBIConstants.SBI_CONTEXT)%>/"
	};
});
</script>