<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
         import="it.eng.spago.base.*,
                 it.eng.spagobi.commons.constants.SpagoBIConstants"
%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.Enumeration"%>

<%      
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	String authFailed = "";
	String startUrl = "";
	ResponseContainer aResponseContainer = ResponseContainerAccess.getResponseContainer(request);
	//RequestContainer requestContainer = RequestContainerAccess.getRequestContainer(request); 
	
	SingletonConfig serverConfig = SingletonConfig.getInstance();
	String strInternalSecurity = serverConfig.getConfigValue("SPAGOBI.SECURITY.PORTAL-SECURITY-CLASS.className");
	boolean isInternalSecurity = (strInternalSecurity.indexOf("InternalSecurity")>0)?true:false;
	
	RequestContainer requestContainer = RequestContainer.getRequestContainer();
	
	String currTheme=ThemesManager.getCurrentTheme(requestContainer);
	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
	 
	if(aResponseContainer!=null) {
		SourceBean aServiceResponse = aResponseContainer.getServiceResponse();
		if(aServiceResponse!=null) {
			SourceBean loginModuleResponse = (SourceBean)aServiceResponse.getAttribute("LoginModule");
			if(loginModuleResponse!=null) {
				String authFailedMessage = (String)loginModuleResponse.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE);
				startUrl = (loginModuleResponse.getAttribute("start_url")==null)?"":(String)loginModuleResponse.getAttribute("start_url");
				if(authFailedMessage!=null) {
					authFailed = authFailedMessage;
				}				
			}
		}
	}

	
	IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
	String sbiMode = "WEB";
	IUrlBuilder urlBuilder = null;
	urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);
%>






<%@page import="it.eng.spagobi.commons.SingletonConfig"%><html>
  <head>
  <script type="text/javascript">
	function escapeUserName(){
	
	userName = document.login.userID.value;
	
		if (userName.indexOf("<")>-1 || userName.indexOf(">")>-1 || userName.indexOf("'")>-1 || userName.indexOf("\"")>-1 || userName.indexOf("%")>-1)
			{
			alert('Invalid username');
			return false;
			}
		else
			{return true;}
	}
	
	function setUser(userV, pswV){
		var password = document.getElementById('password');
		var user = document.getElementById('userID');
		password.value = pswV;
		user.value = userV;
	//	document.forms[0].submit();
	}
	</script>
	<link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "img/favicon.ico")%>" />
    <title>SpagoBI</title>
    <style>
      body {
	       padding: 0;
	       margin: 0;
      }
    </style> 
  </head>

  <body>
<% 
String url="";
if(ThemesManager.resourceExists(currTheme,"/html/banner.html")){
	url = "/themes/"+currTheme+"/html/banner.html";	
}
else {
	url = "/themes/sbi_default/html/banner.html";	
} 
    
%>
	
	<LINK rel='StyleSheet' 
    href='<%=urlBuilder.getResourceLinkByTheme(request, "css/spagobi_shared.css",currTheme)%>' 
    type='text/css' />


	<jsp:include page='<%=url%>' />
        <form name="login" action="<%=contextName%>/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE" method="POST" onsubmit="return escapeUserName()">
        	<input type="hidden" id="isInternalSecurity" name="isInternalSecurity" value="<%=isInternalSecurity %>" />
        	<%
        	// propagates parameters (if any) for document execution
        	if (request.getParameter(ObjectsTreeConstants.OBJECT_LABEL) != null) {
        		String label = request.getParameter(ObjectsTreeConstants.OBJECT_LABEL);
        	    String subobjectName = request.getParameter(SpagoBIConstants.SUBOBJECT_NAME);
        	    %>
        	    <input type="hidden" name="<%= ObjectsTreeConstants.OBJECT_LABEL %>" value="<%= StringEscapeUtils.escapeHtml(label) %>" />
        	    <% if (subobjectName != null && !subobjectName.trim().equals("")) { %>
        	    	<input type="hidden" name="<%= SpagoBIConstants.SUBOBJECT_NAME %>" value="<%= StringEscapeUtils.escapeHtml(subobjectName) %>" />
        	    <% } %>
        	    <%
        	    // propagates other request parameters than PAGE, NEW_SESSION, OBJECT_LABEL and SUBOBJECT_NAME
        	    Enumeration parameters = request.getParameterNames();
        	    while (parameters.hasMoreElements()) {
        	    	String aParameterName = (String) parameters.nextElement();
        	    	if (aParameterName != null 
        	    			&& !aParameterName.equalsIgnoreCase("PAGE") && !aParameterName.equalsIgnoreCase("NEW_SESSION") 
        	    			&& !aParameterName.equalsIgnoreCase(ObjectsTreeConstants.OBJECT_LABEL)
        	    			&& !aParameterName.equalsIgnoreCase(SpagoBIConstants.SUBOBJECT_NAME) 
        	    			&& request.getParameterValues(aParameterName) != null) {
        	    		String[] values = request.getParameterValues(aParameterName);
        	    		for (int i = 0; i < values.length; i++) {
        	    			%>
        	    			<input type="hidden" name="<%= StringEscapeUtils.escapeHtml(aParameterName) %>" 
        	    								 value="<%= StringEscapeUtils.escapeHtml(values[i]) %>" />
        	    			<%
        	    		}
        	    	}
        	    }
        	} 
        	%>
        	
	        <div id="content" style="width:100%;overflow:hidden;">
		        	<div style="float:left;background-image:url('/SpagoBI/themes/sbi_default/img/wapp/login_demo.png');background-repeat:no-repeat !important;width:570px;height:310px;margin-top:80px;margin-left:50px; " >
		        	<!--
		        	DO NOT DELETE THIS COMMENT
		        	If you change the tag table with this one  you can have the border of the box with the shadow via css
		        	the problem is that it doesn't work with ie	
		     		
		     		<table style="background: none repeat scroll 0 0 #fff; border-radius: 10px 10px 10px 10px;  box-shadow: 0 0 10px #888; color: #009DC3; display: block; font-size: 14px; line-height: 18px; padding: 20px;">
		        	 -->
		        	
		        		<table border=0>
		        			<tr>
		        				<td width = "120px">
		        					&nbsp;
		        				</td>
		        				<td width = "350px">
		        				    <br/> <br/><br/>
		        				    <table border=0>
		        				    	<tr class='header-row-portlet-section'>
		        				    		<td class='header-title-column-portlet-section-nogrey' width="90px" align="right" >
		        								<%=msgBuilder.getMessage("username")%>:
		        							</td>
		        							<td width="25px">&nbsp;</td>
		        							<td>
		        								<input id="userID" name="userID" type="text" size="25" />
		        							</td>	
		        						</tr>
		        						<tr class='header-row-portlet-section'>
											<td class='header-title-column-portlet-section-nogrey' width="90px" align="right" >
		        								<%=msgBuilder.getMessage("password")%>:
		        							</td>
		        							<td width="25px">&nbsp;</td>
		        							<td>
		        								<input id="password" name="password" type="password" size="25" />
		        							</td>	
		        						</tr>
		        						<% if (isInternalSecurity) {%>
										<tr><td colspan=3>&nbsp;</td></tr>
										<tr>
											<td>&nbsp;</td>
											<td>&nbsp;</td>
											<td class='header-title-column-portlet-section-nogrey' width="150px">
												<a href="<%=contextName %>/ChangePwdServlet?start_url=<%=startUrl%>">
														<%=msgBuilder.getMessage("changePwd")%>
												</a>
											</td>
										</tr>
			        					<% } %>
		        					</table>	
		        				</td>
		        				<td style="padding-top: 20px">
		        					&nbsp;&nbsp;
		        					<input type="image" 
		        					       src="<%=urlBuilder.getResourceLinkByTheme(request, "/img/wapp/next32.png", currTheme)%>" 
		        					       title="<%=msgBuilder.getMessage("login")%>" 
		        					       alt="<%=msgBuilder.getMessage("login")%>"/>
		        				</td>
		        			</tr>
							<tr>
								<td>&nbsp;</td>
								<td class='header-title-column-portlet-section-nogrey'>
									<div class="header-row-portlet-section" style = "line-height: 130%; margin-top: 10px; font-size:9pt;">														
										SpagoBI Demo users' credentials:<br/>
										&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" onclick="setUser('biuser','biuser')"><b>biuser/biuser</b></a>(business user)<br/>
										&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" onclick="setUser('bidemo','bidemo')"><b>bidemo/bidemo</b></a> (showcase user)<br/>
										&nbsp;&nbsp;&nbsp;&nbsp;<a href="#" onclick="setUser('biadmin','biadmin')"><b>biadmin/biadmin</b></a> (administrator)
									</div>
								</td>
							</tr>
		        			<tr>
		        				<td>&nbsp;</td>
		        				<td style='color:red;font-size:11pt;'><br/><%=authFailed%></td>
		        				<td>&nbsp;</td>
		        			</tr>
		
		        		</table>
		        	</div>
					<div style="float:right;background-image:url('/SpagoBI/themes/sbi_default/img/wapp/sfodno_login.png');width:330px;height:310px;margin-top:110px;margin-right:20px;"></div>
	        </div>
        </form>
        <spagobi:error/>
				<% 
			String url2="";
			if(ThemesManager.resourceExists(currTheme,"/html/footerLogin.html")){
				url2 = "/themes/"+currTheme+"/html/footerLogin.html";	
			}
			else {
				url2 = "/themes/sbi_default/html/footerLogin.html";	
			}
			%>
				<jsp:include page='<%=url2%>' />
   
  </body>
</html>
