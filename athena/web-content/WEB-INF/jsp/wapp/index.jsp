<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<% String contextName = ChannelUtilities.getSpagoBIContextName(request);
    String redirectURL = contextName+"/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE&MESSAGE=START_LOGIN";
	if(request.getAttribute(SpagoBIConstants.BACK_URL)!=null){
		String backUrl=(String)request.getAttribute(SpagoBIConstants.BACK_URL);
		if(!backUrl.equalsIgnoreCase("")){
			redirectURL+="&"+SpagoBIConstants.BACK_URL+"="+backUrl;
		}
	}
    
    response.sendRedirect(redirectURL);
%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<HTML>
<HEAD>
<TITLE>Redirect...</TITLE> 

</HEAD>
<BODY>
Redirect in corso...
</BODY>
</HTML>
