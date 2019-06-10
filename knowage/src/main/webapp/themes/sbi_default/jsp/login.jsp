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
	RequestContainer requestContainer = RequestContainer.getRequestContainer();

	String activationMsg = request.getParameter("activationMsg");
	
	SingletonConfig serverConfig = SingletonConfig.getInstance();
	String strActiveSignup = serverConfig
			.getConfigValue("SPAGOBI.SECURITY.ACTIVE_SIGNUP_FUNCTIONALITY");
	boolean activeSignup = (strActiveSignup.equalsIgnoreCase("true"))?true:false;

	String strInternalSecurity = serverConfig
			.getConfigValue("SPAGOBI.SECURITY.PORTAL-SECURITY-CLASS.className");
	boolean isInternalSecurity = (strInternalSecurity
			.indexOf("InternalSecurity") > 0) ? true : false;
	String roleToCheckLbl = 
			(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ROLE_LOGIN") == null)?"" :
			 SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.ROLE_LOGIN");
	String roleToCheckVal = "";
	if (!("").equals(roleToCheckLbl)) {
		roleToCheckVal = (request.getParameter(roleToCheckLbl) != null) ? request
				.getParameter(roleToCheckLbl) : "";
		if (("").equals(roleToCheckVal)) {
			//			roleToCheckVal = ( sessionContainer.getAttribute(roleToCheckLbl)!=null)?
			//						(String)sessionContainer.getAttribute(roleToCheckLbl):"";
			roleToCheckVal = (session.getAttribute(roleToCheckLbl) != null) ? (String) session
					.getAttribute(roleToCheckLbl) : "";
		}
	}

	String currTheme = ThemesManager.getCurrentTheme(requestContainer);
	if (currTheme == null)
		currTheme = ThemesManager.getDefaultTheme();

	if (aResponseContainer != null) {
		SourceBean aServiceResponse = aResponseContainer
				.getServiceResponse();
		if (aServiceResponse != null) {
			SourceBean loginModuleResponse = (SourceBean) aServiceResponse
					.getAttribute("LoginModule");
			if (loginModuleResponse != null) {
				String authFailedMessage = (String) loginModuleResponse
						.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE);
				startUrl = (loginModuleResponse
						.getAttribute("start_url") == null) ? ""
						: (String) loginModuleResponse
								.getAttribute("start_url");
				if (authFailedMessage != null) {
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

<%@page import="it.eng.spagobi.commons.SingletonConfig"%>


<!DOCTYPE html>
<html>
	<head>
	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge">
	    <meta name="viewport" content="width=device-width, initial-scale=1">
	    <meta name="apple-mobile-web-app-capable" content="yes">
	    <meta name="apple-mobile-web-app-title" content="Knowage">
	    <link rel="manifest" href="<%=urlBuilder.getResourceLink(request, "manifest.json")%>" />
	    
	    <title>Knowage</title>
  
		<script type="text/javascript">
			function signup(){
			 	var form = document.getElementById('formId');
			 	var act = '<%=urlBuilder.getResourceLink(request, "restful-services/signup/prepare")%>';
			   	form.action = act;
			   	form.submit();
			}
			function escapeUserName(){
				userName = document.login.userID.value;
				if (userName.indexOf("<")>-1 || userName.indexOf(">")>-1 || userName.indexOf("'")>-1 || userName.indexOf("\"")>-1 || userName.indexOf("%")>-1){
					alert('Invalid username');
					return false;
				}else return true;
			}
		
			function setUser(userV, pswV){
				var password = document.getElementById('password');
				var user = document.getElementById('userID');
				password.value = pswV;
				user.value = userV;
			}
		</script>
		<link rel="shortcut icon" href="<%=urlBuilder.getResourceLinkByTheme(request, "img/favicon.ico",currTheme)%>" />
		   <!-- Bootstrap -->
		<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/bootstrap/css/bootstrap.min.css")%>">
		<link rel='StyleSheet' href='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/css/customStyle.css",currTheme)%>' type='text/css' />
	</head>

  	<body class="kn-login">
  		<div class="container-fluid" style="height:100%;">
        	<!--  div class="card card-container"-->
        	<div class="col-sm-5 col-sm-offset-7" style="height:100%;background-color:white;display:flex;flex-direction:column;padding:20px;justify-content:center;align-items:center">
            	<img id="profile-img" class="col-xs-8" src='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/img/defaultTheme/logoCover.svg", currTheme)%>' />
            	<p id="profile-name" class="profile-name-card"></p>
            	<div class="col-xs-8">
           			<form class="form-signin"  id="formId" name="login" action="<%=contextName%>/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE" method="POST" onsubmit="return escapeUserName()">
		                <input type="hidden" id="isInternalSecurity" name="isInternalSecurity" value="<%=isInternalSecurity %>" />        	
		        		<input type="hidden" id="<%=roleToCheckLbl%>" name="<%=roleToCheckLbl%>" value="<%=roleToCheckVal%>" />
		        		<%	
				        	// propagates parameters (if any) for document execution
				        	if (request.getParameter(ObjectsTreeConstants.OBJECT_LABEL) != null) {
				        		String label = request.getParameter(ObjectsTreeConstants.OBJECT_LABEL);
								%>
								<input type="hidden" name="<%= SpagoBIConstants.OBJECT_LABEL %>" value="<%= label %>" />
								<%
				        	    String subobjectName = request.getParameter(SpagoBIConstants.SUBOBJECT_NAME);
				        	    %>
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
		                <input type="text" id="userID" name="userID" class="form-control" placeholder="<%=msgBuilder.getMessage("username")%>" required autofocus>
		                <input type="password" id="password" name="password" class="form-control" placeholder="<%=msgBuilder.getMessage("password")%>" required>
					
						<% if(activationMsg != null){
							String style = null;
							if(activationMsg.contains("KO")){
								style ="'color:red;font-size:12pt;'";
							}
							else{style ="'font-size:12pt;'";}
			             %>
		             	<br/><div style=<%=style%> ><%=msgBuilder.getMessage(activationMsg)%></div><br/>
	                	<% } %>
				
	            		<button class="btn btn-lg btn-primary btn-block btn-signin" type="submit"><%=msgBuilder.getMessage("login")%></button>
	            	</form>
	            	<% if (activeSignup){ %>
		               	<button class="btn btn-lg btn-primary btn-block btn-signup" onclick="signup();" ><%=msgBuilder.getMessage("signup")%></button>
					<%} %> 
            	</div>
            	
	           	
                
				<div>
					<div class="row">
						<!--
						<div class="col-sm-4">
							<button data-toggle="tooltip" data-placement="bottom" aria-label="Administrator (biadmin/biadmin)" title="Administrator (biadmin/biadmin)" type="button" class="btn btn-default" onclick="setUser('biadmin','biadmin'); login.submit()" >
							<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
							</button>
						</div>
						<div class="col-sm-4">
							<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="bottom" aria-label="Showcase User (bidemo/bidemo)" title="Showcase User (bidemo/bidemo)" onclick="setUser('bidemo','bidemo'); login.submit()">
							<span class="glyphicon glyphicon-briefcase" aria-hidden="true"></span>
							</button>
						</div>
						<div class="col-sm-4">
						 
							<button type="button" class="btn btn-default" data-toggle="tooltip" data-placement="bottom" aria-label="Business User (biuser/biuser)" title="Business User (biuser/biuser)" onclick="setUser('biuser','biuser'); login.submit()">
							<span class="glyphicon glyphicon-user" aria-hidden="true"></span>
							</button>
						
						</div>
						-->
						
						<!-- Uncomment this for adding the Change Password Link -->
						<!-- <div class="col-sm-6"><a class="lightLink" href="<%=contextName %>/ChangePwdServlet?start_url=<%=startUrl%>">	<%=msgBuilder.getMessage("changePwd")%> </a></div> -->
					</div>
			
		 			<spagobi:error/>
				</div><!-- /card-container -->
				<div class="version"><span>Version:</span> <%=it.eng.knowage.wapp.Version.getVersionForDatabase()%></div>
			</div><!-- /container -->
		</div>
 		<script src="<%=urlBuilder.getResourceLink(request, "js/lib/jquery-1.11.3/jquery-1.11.3.min.js")%>"></script>
 		<script src="<%=urlBuilder.getResourceLink(request, "js/lib/bootstrap/bootstrap.min.js")%>"></script>
	
		<script>
			$(document).ready(function(){
				// Select all elements with data-toggle="tooltips" in the document
				$('[data-toggle="tooltip"]').tooltip(); 
				
				// Select a specified element
				$('#myTooltip').tooltip();
			});
		</script>  
	</body>
</html>
