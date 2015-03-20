<%@tag language="java" pageEncoding="UTF-8" %>
<%@tag import="java.net.URLEncoder"%>
<%@tag import="java.util.Set"%>
<%@tag import="java.util.Iterator"%>

<%@attribute name="spagobiContext" required="true" type="java.lang.String"%>
<%@attribute name="documentId" required="false" type="java.lang.String"%>
<%@attribute name="documentLabel" required="false" type="java.lang.String"%>
<%@attribute name="executionRole" required="false" type="java.lang.String"%>
<%@attribute name="parametersStr" required="false" type="java.lang.String"%>
<%@attribute name="parametersMap" required="false" type="java.util.Map"%>
<%@attribute name="displayToolbar" required="false" type="java.lang.Boolean"%>
<%@attribute name="displaySliders" required="false" type="java.lang.Boolean"%>
<%@attribute name="iframeStyle" required="false" type="java.lang.String"%>
<%@attribute name="theme" required="false" type="java.lang.String"%>
<%@attribute name="authenticationTicket" required="false" type="java.lang.String"%>

<%
StringBuffer iframeUrl = new StringBuffer();
iframeUrl.append(spagobiContext + "/servlet/AdapterHTTP?PAGE=ExecuteBIObjectPage&NEW_SESSION=true&MODALITY=SINGLE_OBJECT_EXECUTION_MODALITY");

if (documentId == null && documentLabel == null) {
	throw new Exception("Neither document id nor document label are specified!!");
}
if (documentId != null) {
	iframeUrl.append("&OBJECT_ID=" + documentId);
} else {
	iframeUrl.append("&OBJECT_LABEL=" + documentLabel);
}
if (parametersStr != null) iframeUrl.append("&PARAMETERS=" + URLEncoder.encode(parametersStr));
if (parametersMap != null && !parametersMap.isEmpty()) {
	Set keys = parametersMap.keySet();
	Iterator keysIt = keys.iterator();
	while (keysIt.hasNext()) {
		String urlName = (String) keysIt.next();
		Object valueObj = parametersMap.get(urlName);
		if (valueObj != null) {
			iframeUrl.append("&" + URLEncoder.encode(urlName) + "=" + URLEncoder.encode(valueObj.toString()));
		}
	}
}
if (executionRole != null) iframeUrl.append("&ROLE=" + URLEncoder.encode(executionRole));
if (displayToolbar != null) iframeUrl.append("&TOOLBAR_VISIBLE=" + displayToolbar.toString());
if (displaySliders != null) iframeUrl.append("&SLIDERS_VISIBLE=" + displaySliders.toString());
if (theme != null)	iframeUrl.append("&theme=" + theme);
if (authenticationTicket != null) iframeUrl.append("&auth_ticket=" + URLEncoder.encode(authenticationTicket));
%>

<iframe src="<%= iframeUrl.toString() %>" style="<%= iframeStyle != null ? iframeStyle : "" %>"></iframe>
