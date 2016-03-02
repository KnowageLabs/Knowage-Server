<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
    	 import="it.eng.spago.base.*,
    	 		 javax.portlet.*"%>
    
    
    <%!
    public String getResourceLink(HttpServletRequest aHttpServletRequest, String originalUrl){
		RenderRequest renderRequest =(RenderRequest)aHttpServletRequest.getAttribute("javax.portlet.request");
		RenderResponse renderResponse =(RenderResponse)aHttpServletRequest.getAttribute("javax.portlet.response");
		String urlToConvert = null; 
		originalUrl = originalUrl.trim();
		if(originalUrl.startsWith("/")) {
			urlToConvert = originalUrl.substring(1);
		} else {
			urlToConvert = originalUrl;
		}
		String newUrl = renderResponse.encodeURL(renderRequest.getContextPath() + "/" + urlToConvert).toString();
		return newUrl;
	}
	%>
    
    
<%
	
	ResponseContainer responseContainer = null;
	boolean webMode = false;

	responseContainer = ResponseContainerPortletAccess.getResponseContainer(request);
	if (responseContainer == null) {
		// case of web mode
		responseContainer = ResponseContainerAccess.getResponseContainer(request);
		webMode = true;
	}
	SourceBean serviceResponse = responseContainer.getServiceResponse();
	
	String rootDir;
	/*
	String mode = (String)serviceResponse.getAttribute(ChironStartAction.MODE);
	String rootDir = ChironStartAction.DEBUG_MODE.equalsIgnoreCase(mode)
						? "source"
						: "build";
	*/
	rootDir = "source";
%>


<script type="text/javascript">
	if(!window.qxsettings)qxsettings={};
	//if(qxsettings["qx.p1"]==undefined)qxsettings["qx.p1"]='value_p1';
	//if(qxsettings["qx.p2"]==undefined)qxsettings["qx.p2"]='value_p2';
</script>

<%if(webMode){ %>
<html>

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    	<title>Chiron - Demo</title>    	
    	
    	<script type="text/javascript">
			if(qxsettings["qooxdoo.executionMode"]==undefined)qxsettings["qooxdoo.executionMode"]='web';
			<%-- 
			alert('chironMainPage.jsp: qxsettings: ' + qxsettings.toSource());
			--%> 
		</script>
		  
    	<script type="text/javascript" src='../js/src/qooxdoo/<%=rootDir%>/script/chiron.web.js'></script>
<%} else {%>
		<script type="text/javascript">
			if(qxsettings["qx.resourceUri"]==undefined)qxsettings["qx.resourceUri"]='<%=getResourceLink(request, "js/src/qooxdoo/build/resource/qx")%>';
			if(qxsettings["qooxdoo.resourceUri"]==undefined)qxsettings["qooxdoo.resourceUri"]='<%=getResourceLink(request, "")%>';
			if(qxsettings["qooxdoo.executionMode"]==undefined)qxsettings["qooxdoo.executionMode"]='portal';
		</script>
		  
		<script type="text/javascript" src='<%=getResourceLink(request, "js/src/qooxdoo/" + "source" + "/script/chiron.portlet.js")%>'></script>

<% } %>
	
  <div style="height:600px;width:100%" id="myInlineWidget" > </div>  	


<%if(webMode){ %>    	
	</head>
	
	<body>

	</body>

</html>
<%} %>
