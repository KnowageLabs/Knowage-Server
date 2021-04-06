<%-- this is part of sbiModule.js --%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spago.message.MessageBundle"%>
<script>
(function() {
	var sbiM=angular.module('sbiModule');
	
	sbiM.factory('sbiModule_config',function(){
		return {
			protocol: '<%= request.getScheme()%>' ,
			host: '<%= request.getServerName()%>',
			port: '<%= request.getServerPort()%>',
			contextName: '/<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>',
			controllerPath: null ,// no cotroller just servlets   
			dateFormat: '<%= GeneralUtilities.getLocaleDateFormat(MessageBundle.getUserLocale()) %>',  // the date format localized according to user language and country 
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
//			contextLogo: "/knowage/themes/sbi_default/img/wapp/logo.png",
			contextLogo: "<%=KnowageSystemConfiguration.getKnowageContext()%>/themes/sbi_default/img/wapp/logo.png", 
			<%--
			docDatasetLabel: "<%=docDatasetLabel%>",
			docDatasetName: "<%=docDatasetName%>",
			visibleDataSet: "<%=visibleDataSet%>",
			--%>
			externalBasePath:"<%= KnowageSystemConfiguration.getKnowageContext()%>",
			sbiEnviroment:"<%=request.getParameter(SpagoBIConstants.SBI_ENVIRONMENT)%>",
			dynamicResourcesBasePath: "<%=dynamicResourcesBasePath%>",		//  /knowage/js/src
			dynamicResourcesEnginePath: "<%=dynamicResourcesEnginePath%>"	//  /kpi/js/src

		};
	});

	sbiM.factory('sbiModule_user',function(){

		var user={};
	 	user.functionalities =[];
	 	<% if (profile != null && profile.getFunctionalities() != null && !profile.getFunctionalities().isEmpty()) { 
	 		for(Object fun :  profile.getFunctionalities()){ %> 
	 		user.functionalities.push('<%=StringEscapeUtils.escapeJavaScript(fun.toString())%>'); 
	 		<% } }%> 
			
		return user;
	});	

})();
</script>