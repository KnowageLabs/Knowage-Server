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
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="it.eng.spagobi.commons.SingletonConfig"%>

<%
	IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder("WEB");
	IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	String currTheme = ThemesManager.getDefaultTheme();
	
    String securityLogoutUrl = SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.SECURITY_LOGOUT_URL");
    
	session.invalidate();
%>
  
   	<html>

   	  	<body>
   	   		 <link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/home40/standard.css", currTheme)%>'/>
   	   	  	 
		   	 <main class="main main-error" id="main">
		     <div class="aux"> 
				<div class="content-error">
		      		<h1><%=msgBuilder.getMessage("authError")%></h1>
					<span class="ops">Ooooooops!</span>
					<p class="retry"><%=msgBuilder.getMessage("unprofiledUser")%></p>
					<p class="retry"><%=msgBuilder.getMessage("youWillBeRedirectedIn")%> <span id="seconds">5</span> <%=msgBuilder.getMessage("seconds")%></p>
					
				</div>
	       </div>
	       </main> 
	       <script>

	       debugger;
		       var securityLogoutUrl = "<%= securityLogoutUrl %>";
	
			   var logoutUrl = securityLogoutUrl.replace("<%="${id_token}"%>", window.sessionStorage.getItem("id_token"));
	
			   let counter = document.getElementById("seconds").innerHTML;
	
			   const interval = setInterval(() => {
				    counter--;
				    document.getElementById("seconds").innerHTML = counter
				    if (counter === 0 ) {
				    	window.location = logoutUrl
				    }
				  }, 1000);

	   	   </script>
	  </body>
</html>
