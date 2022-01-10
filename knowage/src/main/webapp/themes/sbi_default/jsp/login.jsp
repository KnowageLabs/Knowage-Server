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

  
<%@page import="it.eng.spagobi.security.google.config.GoogleSignInConfig"%>
<%@page import="it.eng.spagobi.security.azure.config.AzureSignInConfig"%>
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
<%@page import="java.net.URLEncoder"%>
<%@page import="java.nio.charset.StandardCharsets" %>
<%@page import="java.util.Enumeration"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>


<%
	String contextName = ChannelUtilities.getSpagoBIContextName(request);	
    
	String authFailed = "";
	ResponseContainer aResponseContainer = ResponseContainerAccess.getResponseContainer(request);
	RequestContainer requestContainer = RequestContainer.getRequestContainer();

	String activationMsg = request.getParameter("activationMsg");
	
	SingletonConfig serverConfig = SingletonConfig.getInstance();
	String strActiveSignup = serverConfig
			.getConfigValue("SPAGOBI.SECURITY.ACTIVE_SIGNUP_FUNCTIONALITY");
	boolean activeSignup = (strActiveSignup.equalsIgnoreCase("true"))?true:false;

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
				String authFailedMessage = (String) loginModuleResponse .getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE);
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
	    <link rel="manifest" href="<%=urlBuilder.getResourceLink(request, "manifest.json")%>" crossorigin="use-credentials"/>
	    
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
		<link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "themes/sbi_default/img/favicon.ico")%>" />
		   <!-- Bootstrap -->
		<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/bootstrap/css/bootstrap.min.css")%>">
		<link rel='StyleSheet' href='<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>' type='text/css' />
		
		<% if (GoogleSignInConfig.isEnabled()) {%>
		<%-- Resources for Google Sign-In authentication --%>
		<script>
			function onSignIn(googleUser) {
			  var profile = googleUser.getBasicProfile();
			  var id_token = googleUser.getAuthResponse().id_token;
			  $.post("/knowage/servlet/AdapterHTTP", {
				  "ACTION_NAME": "LOGIN_ACTION_BY_TOKEN",
				  "NEW_SESSION" : true,
				  "token" : id_token
			  }).done(function( data ) {
				  // reload current page, in order to keep input GET parameters (such as required document and so on)
				  location.reload();
			  }).fail(function (error) {
				  $("#kn-infoerror-message").show();
				  $(".kn-infoerror").html("Authentication failed. Please check if you are to allowed to enter this application.");
			  });
			}
		</script>
		<script src="https://apis.google.com/js/platform.js" async defer></script>
		<meta name="google-signin-client_id" content="<%= GoogleSignInConfig.getClientId() %>">
		<% } %>
		
		<% if (AzureSignInConfig.isEnabled()) {%>
		<%-- Resources for Azure Sign-In authentication --%>
		<script>
			var msalConfig = {
				    auth: {
				        clientId: "<%= AzureSignInConfig.getClientId() %>",
				        authority: "<%= AzureSignInConfig.getAuthorityId() %>"
				    },
				    cache: {
				        cacheLocation: "localStorage", // This configures where your cache will be stored
				        storeAuthStateInCookie: false, // Set this to "true" if you are having issues on IE11 or Edge
				    }
			};
			
			var loginRequest = {
			    scopes: ["User.Read", "email", "openid", "profile"]
			};
		
			function onAzureSignIn() {
				myMSALObj = new msal.PublicClientApplication(msalConfig);
				myMSALObj.loginPopup(loginRequest).then(handleResponse);
			}
			
			function handleResponse(response) {
				$.post("/knowage/servlet/AdapterHTTP", {
					  "ACTION_NAME": "LOGIN_ACTION_BY_TOKEN",
					  "NEW_SESSION" : true,
					  "token" : response.idToken
				}).done(function( data ) {
					  // reload current page, in order to keep input GET parameters (such as required document and so on)
					  location.reload();
				}).fail(function (error) {
					  $("#kn-infoerror-message").show();
					  $(".kn-infoerror").html("Authentication failed. Please check if you are to allowed to enter this application.");
				});
			}
		</script>
		<script type="text/javascript" src="https://alcdn.msauth.net/browser/2.0.0-beta.0/js/msal-browser.js" integrity="sha384-r7Qxfs6PYHyfoBR6zG62DGzptfLBxnREThAlcJyEfzJ4dq5rqExc1Xj3TPFE/9TH" crossorigin="anonymous" async defer></script>
		<% } %>
	</head>

  	<body class="kn-login">
  		<div class="container-fluid" style="height:100%;">
        	<!--  div class="card card-container"-->
        	<div class="col-sm-5 col-sm-offset-7" style="height:100%;background-color:white;display:flex;flex-direction:column;padding:20px;justify-content:center;align-items:center">
            	<img id="profile-img" class="col-xs-8" src='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/img/defaultTheme/logoCover.svg", currTheme)%>' />
            	<p id="profile-name" class="profile-name-card"></p>
            	
            	
            	<% if (GoogleSignInConfig.isEnabled()) { %>
            		<%-- Google button for authentication --%>
	           		<div class="g-signin2" data-onsuccess="onSignIn"></div>
	           	
                <% } else if (AzureSignInConfig.isEnabled()){ %>
            		<%-- Azure button for authentication --%>
	           		<button class="btn-signin-azure" onclick="onAzureSignIn()">
						<svg xmlns="http://www.w3.org/2000/svg" width="21" height="21" viewBox="0 0 21 21" style="height: 1em; width: 1em; top: .125em; position: relative;"><title>MS-SymbolLockup</title><rect x="1" y="1" width="9" height="9" fill="#f25022"/><rect x="1" y="11" width="9" height="9" fill="#00a4ef"/><rect x="11" y="1" width="9" height="9" fill="#7fba00"/><rect x="11" y="11" width="9" height="9" fill="#ffb900"/></svg> Sign In
	           		</button>
	           		
            	<% } else { %>
            	
            	<div class="col-xs-8">
           			<form class="form-signin"  id="formId" name="login" action="<%=contextName%>/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE" method="POST" onsubmit="return escapeUserName()">
		        		<input type="hidden" id="<%=roleToCheckLbl%>" name="<%=roleToCheckLbl%>" value="<%=roleToCheckVal%>" />
		        		<%	
		        		
		        		if(request.getParameter("targetService") != null) {
		        			%>
							<input type="hidden" name="<%= "targetService" %>" value="<%= StringEscapeUtils.escapeHtml(request.getParameter("targetService")) %>" />
							<%
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
            	<!-- img class="col-xs-8 col-sm-offset-4 col-sm-4" src='<%=urlBuilder.getResourceLink(request, "themes/commons/img/defaultTheme/poweredBy.svg")%>' / -->
            	<% } %>
            	
            	
            	<%-- Box that can be reused to display error messages, initially empty --%>
            	<div class="row" id="kn-infoerror-message" style="display:none">
            		<div class="kn-infoerror">
            		</div>
            	</div>
            	
				<div>
					<div class="row">					
						<!-- Uncomment this to add the Change Password Link -->
						<!-- <div class="col-sm-6"><a class="lightLink" href="<%=contextName %>/ChangePwdServlet">	<%=msgBuilder.getMessage("changePwd")%> </a></div> -->
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
