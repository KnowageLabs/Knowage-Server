<%@ page language="java"
         extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true" 
         import="it.eng.spago.base.*,
                 it.eng.spagobi.commons.constants.SpagoBIConstants"
%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.Enumeration"%>


<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%
  
   String baseParams = "NEW_SESSION=TRUE&SBI_ENVIRONMENT=WORKSPACE" +
   				"&user_id=" + userProfile.getUserUniqueIdentifier() +
   				"&SBI_LANGUAGE=" +  permanentSession.getAttribute(Constants.USER_LANGUAGE) + 
				"&SBI_COUNTRY=" + permanentSession.getAttribute(Constants.USER_COUNTRY) + 
				"&SBI_HOST=" +  GeneralUtilities.getSpagoBiHost() + 
				"&SBICONTEXT=" +  GeneralUtilities.getSpagoBiContext();
				 
	String baseUrlKPI = GeneralUtilities.getSpagoBiHost() + "/knowagekpiengine/restful-services/1.0/pages/edit?";
	String urlKPI =  baseUrlKPI + baseParams;
				
	String baseUrlCockpit = GeneralUtilities.getSpagoBiHost() + "/knowagecockpitengine/api/1.0/pages/edit?";
	String urlCockpit = baseUrlCockpit + baseParams + "&IS_TECHNICAL_USER=true&documentMode=EDIT"; 
   
%>


<html>
<head>

<!--custom_prj-->
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/custom_prj/css/angular-material.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/custom_prj/material-icons/material-icons.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/custom_prj/css/style.css">
<!--custom_prj fine -->

<link id="spagobi-angular" rel="styleSheet"	href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>" type="text/css" />  
</head>
<body class="landingPageAdmin" data-ng-controller="MainController" ng-app="knowageIntro">

  <%@ include file="/themes/sbi_default/custom_prj/html/welcomePage.jspf"%>
  
</body>
</html>