<%-- 
   Knowage, Open Source Business Intelligence suite
   Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
   
   Knowage is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   Knowage is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.
   
   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" 
%>
<%@page session="false" %>

<%@page import="it.eng.spagobi.commons.utilities.SpagoBIUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
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
		function setParentUrl(currentWindow, parentWindow){
			if(parentWindow == currentWindow){
				parentWindow.parent.postMessage({
					'status': 401
				     }, "*")
    		}else{
    			currentWindow = parentWindow;
				parentWindow = currentWindow.parent;
				setParentUrl(currentWindow,parentWindow);
    		}
		}
		setParentUrl(currentWindow, parentWindow);
	} catch (err) {
		if (!sessionExpiredSpagoBIJSFound) {
			window.location = '<%= KnowageSystemConfiguration.getKnowageContext() %>';
		}
	}
	
	
	</script>
	<%
}
%>
