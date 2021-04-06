<%-- this is part of sbiModule.js --%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
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
		externalBasePath:"<%=KnowageSystemConfiguration.getKnowageContext()%>/",
		adapterPath:'<%= KnowageSystemConfiguration.getKnowageContext() + GeneralUtilities.getSpagoAdapterHttpUrl() %>',
		dynamicResourcesBasePath: "<%=dynamicResourcesBasePath%>",		//  /knowage/js/src
		dynamicResourcesEnginePath: "<%=dynamicResourcesEnginePath%>"	//  /georeport/js/src

	};
});

sbiM.factory('sbiModule_user',function(){
	
	var user={};

 	//set functionalities
 	user.functionalities =[];
 	<% if (profile != null && profile.getFunctionalities() != null && !profile.getFunctionalities().isEmpty()) { 
 		for(Object fun :  profile.getFunctionalities()){ %> 
 			user.functionalities.push('<%=StringEscapeUtils.escapeJavaScript(fun.toString())%>'); 
 		<% } 
 		}%> 
		
	return user;
});

</script>