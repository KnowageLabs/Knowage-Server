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
                
<%  String contextName = ChannelUtilities.getSpagoBIContextName(request);
	IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
	ResponseContainer responseContainer = ResponseContainerAccess.getResponseContainer(request);
	RequestContainer requestContainer = RequestContainer.getRequestContainer();
	
	String strLocale = "";
	String currTheme = "";
	String url = "";
	String msg = "";
	String title = "";
	String lblBtn = "";
	String backUrl = "";
	String docLabel = "";
	
	if(responseContainer!=null) {
		SourceBean aServiceResponse = responseContainer.getServiceResponse();
		if(aServiceResponse!=null) {
			SourceBean loginModuleResponse = (SourceBean)aServiceResponse.getAttribute("LoginModule");
			if(loginModuleResponse!=null) {
				strLocale = (String)loginModuleResponse.getAttribute("locale");
				currTheme = (String) loginModuleResponse.getAttribute("currTheme");
				url = (String)loginModuleResponse.getAttribute("URL_BTN");
				msg = (String)loginModuleResponse.getAttribute("MSG");
				title = (String)loginModuleResponse.getAttribute("TITLE");
				lblBtn = (String)loginModuleResponse.getAttribute("LBL_BTN");
				//variables for redirect from login
				backUrl = (loginModuleResponse.getAttribute("BACK_URL")==null)?"":(String)loginModuleResponse.getAttribute("BACK_URL");
				docLabel = (loginModuleResponse.getAttribute("OBJECT_LABEL")==null)?"":(String)loginModuleResponse.getAttribute("OBJECT_LABEL");
			}
		}
	}
	
%>

<HTML>

<HEAD>
	<TITLE>Warning message</TITLE> 
</HEAD>

<BODY>
	<link rel='stylesheet' type='text/css' href='<%=contextName%>/themes/<%=currTheme%>/css/home40/standard.css'/>
	<span style="float:left; width: 100%; text-align:center;">
		<form name="input" method="post" action="<%=url %>" class="reserved-area-form login">
			<input type="hidden" name="BACK_URL" value="<%=backUrl%>"/>
			<input type="hidden" name="OBJECT_LABEL" value="<%=docLabel%>"/>
			<input type="hidden" name="fromWarning" value="TRUE"/>
			<main class="main main-warn" id="main">
			 <div class="aux"> 
				 <div>  	
				 	<br/>
					<p><%=title%></p>
					<span class="ops"><%=msg%></span> 
					<div class="submit">
						<input type="submit" value="<%=lblBtn %>">
					</div>
			  	 </div>
			 </div>
			</main> 
		</form>
	</span>
</BODY>
