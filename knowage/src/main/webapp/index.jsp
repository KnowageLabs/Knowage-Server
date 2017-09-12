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
