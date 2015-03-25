<%--
SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
--%>
<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" %>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities,
                it.eng.spagobi.commons.constants.SpagoBIConstants,
                it.eng.spagobi.commons.SingletonConfig"%>
                
<% 
	SingletonConfig serverConfig = SingletonConfig.getInstance();
	String strUsePublicUser = serverConfig.getConfigValue(SpagoBIConstants.USE_PUBLIC_USER);
	Boolean usePublicUser = (strUsePublicUser == null)?false:Boolean.valueOf(strUsePublicUser);
	String callLogin = (request.getParameter("login")==null)?"false":(String)request.getParameter("login");
	//default url
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	String redirectURL = contextName + "/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE";
	if (usePublicUser && callLogin.equals("false")){
		redirectURL = contextName + "/servlet/AdapterHTTP?ACTION_NAME=START_ACTION_PUBLIC_USER&NEW_SESSION=TRUE";
	}
    response.sendRedirect(redirectURL);
%>

<HTML>
<HEAD>
<TITLE>Redirect...</TITLE> 

</HEAD>
<BODY>
Redirect in corso...
</BODY>
