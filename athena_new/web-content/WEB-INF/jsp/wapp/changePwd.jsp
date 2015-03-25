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
<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>

<%      
	String userId = (request.getParameter("user_id")==null)?"":request.getParameter("user_id");
	String startUrl = request.getParameter("start_url");
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	String authFailed = (request.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE) == null)?"":
						(String)request.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE);
	
	ResponseContainer responseContainer = ResponseContainerAccess.getResponseContainer(request);
	RequestContainer requestContainer = RequestContainer.getRequestContainer();
	
	String currTheme=ThemesManager.getDefaultTheme();
	if (requestContainer != null){
		currTheme=ThemesManager.getCurrentTheme(requestContainer);
		if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
	
		if(responseContainer!=null) {
			SourceBean aServiceResponse = responseContainer.getServiceResponse();
			if(aServiceResponse!=null) {
				SourceBean loginModuleResponse = (SourceBean)aServiceResponse.getAttribute("LoginModule");
				if(loginModuleResponse!=null) {
					userId = (String)loginModuleResponse.getAttribute("user_id");
					startUrl = (String)loginModuleResponse.getAttribute("start_url");
          			String authFailedMessage = (String)loginModuleResponse.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE);
  					if(authFailedMessage!=null) authFailed = authFailedMessage;
				}
			}
		}
	
		
	}

	//IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();

	String sbiMode = "WEB";
	IUrlBuilder urlBuilder = null;
	urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);
%>

<html>
  <head>
    <title>SpagoBI</title>
<style media="screen" type="text/css">

	input.login    {
	display:block;
	border: 1px solid #a9a9a9; 
	color: #7b7575;
	background: #d4d4d4; 
	height: 25px;
	width: 300px;
	-webkit-box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.3);
	-moz-box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.3);
	box-shadow: 0px 0px 8px rgba(0, 0, 0, 0.3);
	}
	body {
background: #dedede; /* Old browsers */
background: -moz-linear-gradient(top,  #dedede 0%, #efefef 100%); /* FF3.6+ */
background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#dedede), color-stop(100%,#efefef)); /* Chrome,Safari4+ */
background: -webkit-linear-gradient(top,  #dedede 0%,#efefef 100%); /* Chrome10+,Safari5.1+ */
background: -o-linear-gradient(top,  #dedede 0%,#efefef 100%); /* Opera 11.10+ */
background: -ms-linear-gradient(top,  #dedede 0%,#efefef 100%); /* IE10+ */
background: linear-gradient(to bottom,  #dedede 0%,#efefef 100%); /* W3C */
filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#dedede', endColorstr='#efefef',GradientType=0 ); /* IE6-9 */

	}
	td.login-label{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 10 px;
	color: #7d7d7d;
}

a:link{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 9px;
	color: #7d7d7d;
}
a:visited{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 9px;
	color: #7d7d7d;
}
a:hover{
 	font-family: Tahoma,Verdana,Geneva,Helvetica,sans-serif;
	font-size: 9px;
	color: #7d7d7d;
}

 </style>
  </head>


	
  <body>
	
  <LINK rel='StyleSheet' 
      href='<%=urlBuilder.getResourceLinkByTheme(request, "css/spagobi_shared.css",currTheme)%>' 
      type='text/css' />
      
	
	<form action="<%=contextName%>/ChangePwdServlet" method="POST" >
		<input type="hidden" id="MESSAGE" name="MESSAGE" value="CHANGE_PWD" />
		<input type="hidden" id="user_id" name="user_id" value="<%=userId%>" />
		<input type="hidden" id="start_url" name="start_url" value="<%=startUrl%>" />
	    

    <div id="content" style="width:100%;height:100%" >
      			        	<div style="float:left;!important;width:570px;height:310px;margin-top:80px;margin-left:50px; " >

      
      		<table border=0>
      			<tr>
						<td></td>
						<td><img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/wapp/spagobi40logo.png", currTheme)%>' width='180px' height='51px'/></td>
						<td></td>
					</tr>
      			<tr>
      			<tr>
      					<td></td>
						<td class='login-label'>Change your password here.</td>
						<td></td>
      			</tr>
      				<td class='header-title-column-portlet-section-nogrey' width = "100px">
      				  
      				</td>
      				<td>
  				    <br/> 
  				    <table>		
                  <% if (("").equals(userId)) { %>        				  
        				     <tr class='header-row-portlet-section'>
        				    		<td class='login-label' width="150px" align="left">
        						      Username : 
        							</td>
        							<td width="25px">&nbsp;</td>

        						</tr> 
        					<tr class='header-row-portlet-section'>
        						<td class='header-title-column-portlet-section-nogrey'>
        						<input name="username" type="text" size="30" class="login" />
        						</td>	
        						<td></td>
        					</tr>	
      						<% } %>
      						<tr class='header-row-portlet-section'>
      				    	<td class='login-label' width="150px" align="left">
      								Old Password:
      							</td>
      							<td class='header-title-column-portlet-section-nogrey' width="30px">&nbsp;</td>

      						</tr>
      						<tr class='header-row-portlet-section'>
      						    <td>
      								<input name="oldPassword" type="password" size="30" class="login"/>
      							</td>	
      							<td>
      							</td>
      						</tr>
      						<tr class='header-row-portlet-section'>
      				    	<td class='login-label' width="150px" align="left">
      								New Password:
      							</td>
      							<td class='header-title-column-portlet-section-nogrey' width="30px">&nbsp;</td>
      						</tr>
      						<tr>
      						   <td>
      								<input name="NewPassword" type="password" size="30" class="login"/>
      							</td>	
      							<td>
      							</td>
      						</tr>
      						<tr class='header-row-portlet-section'>
      				    	<td class='login-label' width="150px" align="left">
      								Retype New Password:
      							</td>
      							<td class='header-title-column-portlet-section-nogrey' width="30px">&nbsp;</td>
	
      						</tr>
      						<tr>
      						    <td>
      								<input name="NewPassword2" type="password" size="30" class="login"/>
      							</td>
      							<td>
      							</td>
      						</tr>
      					</table>	
      				</td>
      			</tr>
      			<tr>
      				<td class='header-title-column-portlet-section-nogrey'>&nbsp;</td>
      				<td class='header-title-column-portlet-section-nogrey' style='color:red;font-size:11pt;'><br/><%=authFailed%></td>
      			</tr>
      			<tr><td>&nbsp;</td></tr>
      			<tr>
      			 <td class='header-title-column-portlet-section-nogrey'>&nbsp;</td>
      			 <td align="left"> 					    		        					      
     						<input type="image" border="0" width="100px" height="37px" title='Confirm'
     							 src="<%=urlBuilder.getResourceLinkByTheme(request, "/img/wapp/confirm_button.png", currTheme)%>" 
     							alt='Confirm' />                  				
      			
    					&nbsp;
    			        	 <a href='<%=startUrl%>'>
             						<img border="0" width="100px" height="37px" title='Cancel'
             							 src="<%=urlBuilder.getResourceLinkByTheme(request, "/img/wapp/cancel_button.png", currTheme)%>" 
             							alt='Cancel' />
             				</a>
      				</td>
      			</tr>

      		</table>
      	</div>
      </div>
	</form>
	<spagobi:error/>


  	
  	
  </body>
  

</html>
