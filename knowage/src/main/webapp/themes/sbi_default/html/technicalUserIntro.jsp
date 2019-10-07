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

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<html>
	<head>
		<link id="spagobi-angular" rel="styleSheet"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>" type="text/css" /> 
	</head>
	<body class="homePage">
		<div class="layer">
		<!-- user id="user">
			  <section>
			    <img src="https://randomuser.me/api/portraits" />
			    <section>
			      <name>Sarah Dekhard</name>
			      <actions><a href="#logout">logout</a></actions>
			    </section>
			  </section>
			</user-->
		</div>
	</body>
</html>