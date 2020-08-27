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
			dateFormat: '<%= GeneralUtilities.getLocaleDateFormat(MessageBundle.getUserLocale()) %>', 
		 	timestampFormat: '<%= GeneralUtilities.getServerTimeStampFormat() %>',
			contextLogo: "<%=KnowageSystemConfiguration.getKnowageContext()%>/themes/sbi_default/img/wapp/logo.png", 
		 	<%--
			docDatasetLabel: "<%=docDatasetLabel%>",
		 	docDatasetName: "<%=docDatasetName%>",
		 	visibleDataSet: "<%=visibleDataSet%>",
		 	--%>
		 	externalBasePath:"<%= KnowageSystemConfiguration.getKnowageContext()%>"
		};
	});

})();
</script>