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
                it.eng.spagobi.commons.SingletonConfig,
                it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="java.util.Locale"%>
                
<% 
	SingletonConfig serverConfig = SingletonConfig.getInstance();

	IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();


	String owner = request.getParameter("owner");
	String userToAccept = request.getParameter("userToAccept");
	String community = request.getParameter("community");
	String strLocale = request.getParameter("locale");
	String currTheme = request.getParameter("currTheme");
	
	Locale locale=null;
	if (strLocale != null){
		locale=new Locale(strLocale.substring(0, strLocale.indexOf("_")), strLocale.substring(strLocale.indexOf("_")+1), "");
	}

	//default url
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	ResponseContainer aResponseContainer = ResponseContainerAccess
			.getResponseContainer(request);
	RequestContainer requestContainer = RequestContainer
			.getRequestContainer();

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
<link rel='stylesheet' type='text/css' href='<%=contextName%>/themes/<%=currTheme%>/css/home40/standard.css'/>
<span style="float:left; width: 100%; text-align:center;">
<form name="input" method="post" action="<%=communityMngURL %>" class="reserved-area-form login">
<main class="main main-msg" id="main">
 <div class="aux"> 
	 <div>  
		<input type="hidden" name="owner" value="<%=owner%>"/>
		<input type="hidden" name="userToAccept" value="<%=userToAccept%>"/>
		<input type="hidden" name="community" value="<%=community%>"/>
		<input type="hidden" name="MESSAGE_DET" value="ACCEPT_MEMBER"/>    		
		<span class="ops"><h2><%=msgBuilder.getMessage("community.save.membership.title",locale) %></h2></span> 
		<p>User <b><%=userToAccept%></b>&nbsp;<%=msgBuilder.getMessage("community.accept.mail.warn.1",locale) %> <b><%=community%></b> <%=msgBuilder.getMessage("community.accept.mail.warn.2",locale) %>:</p>
		<div class="submit">
			<input type="button" value="<%=msgBuilder.getMessage("community.accept.mail.btn.reject",locale) %>" onClick="javascript:reject('<%=userToAccept %>','<%=community %>');">
			<input type="submit" value="<%=msgBuilder.getMessage("community.accept.mail.btn.accept",locale) %>">
		</div>
		<br><br>
		<p class="retry"><%=msgBuilder.getMessage("community.accept.mail.warn",locale) %></p>
  	 </div>
 </div>
</main> 
</form>

<!-- <p><b>WARNING: if you are not logged yet in SpagoBI, you will be redirected to the login page, before your decision is applied </b> -->

</span>

</div>
</BODY>
