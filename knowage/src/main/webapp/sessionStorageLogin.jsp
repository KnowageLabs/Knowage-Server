<html>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%
if (session!=null) session.invalidate();
%>
<head>
</head>
<body>
<form name="myForm" action="/knowage/servlet/AdapterHTTP" method="post">
  <input type="hidden" name="PAGE" value="LoginPage">
  <input type="hidden" name="NEW_SESSION" value="TRUE">
  <input type="hidden" name="<%=System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL")) %>" value="">
</form>
</body>
<script>

function invalidateNoError(url) {
	return new Promise(function(resolve, reject) {
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, true);
		xhr.onload = function() {
			resolve(true);
		};
		xhr.onerror = function() {
			resolve(true);
		};
		xhr.send();
	});
}

function invalidateAll() {
	Promise.all([
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageBirtReportEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageCockpitEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageCommonjEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageDossierEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageGeoReportEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageJasperReportEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageKpiEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageMetaContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageQbeEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageSvgViewerEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageTalendEngineContext()%>/invalidateSession.jsp"),
		invalidateNoError("<%=KnowageSystemConfiguration.getKnowageWhatifEngineContext()%>/invalidateSession.jsp")
	]);
}

invalidateAll();
document.forms["myForm"]['<%=System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL")) %>'].value = sessionStorage.getItem('<%=System.getProperty("JWT_SESSION_STORAGE", System.getenv("JWT_SESSION_STORAGE")) %>');
document.forms["myForm"].submit();
</script>
</html>