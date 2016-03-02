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


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@ page import="it.eng.spago.navigation.LightNavigationManager,
                 java.util.Map,
                 java.util.HashMap"%>

<%
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	String loginUrl = null;
   	Map loginUrlPars = new HashMap(); 
   	loginUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
   	loginUrl = urlBuilder.getUrl(request, loginUrlPars);
%>
  
   	<html>
   	
	   	<script>
		    function signup(){
		    	var form = document.getElementById('formId');
		    	var act = '${pageContext.request.contextPath}/restful-services/signup/prepare';
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
					<p><!-- Hai <a href="#">dimenticato la password</a>? --> <%=msgBuilder.getMessage("noAccount")%> <a href="#" onclick="signup();"><%=msgBuilder.getMessage("signup")%></a></p>
				</div>
	       </div>
	       </main> 
	  </body>
</html>
