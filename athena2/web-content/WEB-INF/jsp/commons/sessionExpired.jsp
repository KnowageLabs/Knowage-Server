<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" 
%>
<%@page session="false" %>

<%@page import="it.eng.spagobi.commons.utilities.SpagoBIUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%
String header = request.getHeader("Powered-By");
if (header != null && header.equals("Ext")) {
	response.setStatus(500);
	JSONObject sessionExpiredError = new JSONObject();
	sessionExpiredError.put("message", "session-expired");
	JSONArray array = new JSONArray();
	array.put(sessionExpiredError);
	JSONObject jsonObject = new JSONObject();
	jsonObject.put("errors", array);
	out.clear();
	out.write(jsonObject.toString());
	out.flush();
} else {
%>
	<%-- 
	SpagoBI Web Application can have different nested iframes. 
	If the session expires, the user would see SpagoBI start page on the nested iframe, that is not so good... 
	The top window contains a javascript variable which name is 'sessionExpiredSpagoBIJS' (see home.jsp), so the following javascript 
	looks for the parent window (using recursion) that contains that variable, and redirects that window.
	If this window is not found, than the current window is redirect to SpagoBI start page.
	--%>
	
	
<script>
	var sessionExpiredSpagoBIJSFound = false;
	try {
		var currentWindow = window;
		var parentWindow = parent;
		while (parentWindow != currentWindow) {
			if (parentWindow.sessionExpiredSpagoBIJS) {
				parentWindow.location = '<%= GeneralUtilities.getSpagoBiContext() %>';
				sessionExpiredSpagoBIJSFound = true;
				break;
			} else {
				currentWindow = parentWindow;
				parentWindow = currentWindow.parent;
			}
		}
	} catch (err) {}
	
	if (!sessionExpiredSpagoBIJSFound) {
		window.location = '<%= GeneralUtilities.getSpagoBiContext() %>';
	}
	</script>
	<%
}
%>