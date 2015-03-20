<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

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