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


<%@page import="it.eng.spagobi.commons.services.LoginModule"%>
<%@page import="it.eng.spago.dispatching.httpchannel.AdapterHTTP"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@ page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@ page import="it.eng.spago.navigation.LightNavigationManager,
                 java.util.Map,
                 java.util.HashMap,
                 it.eng.spago.base.Constants"%>

<%
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	String loginUrl = null;
   	Map loginUrlPars = new HashMap(); 
   	loginUrlPars.put(AdapterHTTP.NEW_SESSION, "TRUE");
   	loginUrlPars.put(Constants.PAGE, LoginModule.PAGE_NAME);
   	loginUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_RESET, "TRUE");
   	IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder("WEB");
   	loginUrl = urlBuilder.getUrl(request, loginUrlPars);
   	IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
    String currTheme = ThemesManager.getDefaultTheme();
%>
  
   	<html>
	   	<script>
		    function signup(){
		    	var form = document.getElementById('formId');
		    	var act = '<%=urlBuilder.getResourceLink(request, "/restful-services/signup/prepare")%>';
		    	form.action = act;
		    	form.submit();		    	
		    }
	   	</script>
   	  	<body>
   	   		 <link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/home40/standard.css",currTheme)%>'/>
   	   	  	 <form id="formId" name="login" action="<%=contextName%>/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE" method="POST">
   	   	  	 </form>
   	   	  	 
		   	 <main class="main main-error" id="main">
		     <div class="aux"> 
				<div class="content-error">
		      		<h1><%=msgBuilder.getMessage("authError")%></h1>
					<span class="ops">Ooooooops!</span>
					<p class="retry"><%=msgBuilder.getMessage("userPwdInvalid")%>, <a href="<%=loginUrl%>"><%=msgBuilder.getMessage("retry")%>!</a></p>
					
				</div>
	       </div>
	       </main> 
	  </body>
</html>
