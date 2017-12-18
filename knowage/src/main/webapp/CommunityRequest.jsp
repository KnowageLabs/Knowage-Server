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

	String owner = request.getParameter("owner");
	String userToAccept = request.getParameter("userToAccept");
	String community = request.getParameter("community");
	
	//default url
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	String communityMngURL = contextName + "/restful-services/community/accept";

%>

<HTML>
<HEAD>
  <script type="text/javascript">
	function reject(user, community){
		alert('You refused user '+user+' to become member of the community ' +community);
	}
  </script>
<TITLE>Community Membership Request</TITLE> 

</HEAD>
<BODY>
<h2>Community Membership Request</h2> 
<div width="100%" style="float:left; width:100%;">
<p style="float:left; width:100%;">User <%=userToAccept%> requests to become memeber of <%=community%> community:</p>

<span style="float:left; width: 60%; text-align:center;">
<form name="input" method="post" action="<%=communityMngURL %>" >
<input type="hidden" name="owner" value="<%=owner%>"/>
<input type="hidden" name="userToAccept" value="<%=userToAccept%>"/>
<input type="hidden" name="community" value="<%=community%>"/>
<input type="hidden" name="MESSAGE_DET" value="ACCEPT_MEMBER"/>
<input type="button" value="Reject" onClick="javascript:reject('<%=userToAccept %>','<%=community %>');">
<input type="submit" value="Accept">
</form>

<p><b>WARNING: if you are not logged yet in SpagoBI, you will be redirected to the login page, before your decision is applied </b>

</span>

</div>
</BODY>
