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



<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="true"
	import="it.eng.spago.base.*,
         		 it.eng.spagobi.commons.utilities.urls.IUrlBuilder,
         		 it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%--
The following directive catches exceptions thrown by jsps, must be commented in development environment
--%>
<%@page errorPage="/WEB-INF/jsp/commons/genericError.jsp"%>

<%@page import="it.eng.spagobi.commons.utilities.urls.WebUrlBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.PortletUrlBuilder"%>
<%@page
	import="it.eng.spagobi.commons.utilities.messages.MessageBuilder"%>
<%@page
	import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.Map"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="java.util.Enumeration"%>
<%@page import="it.eng.spagobi.container.CoreContextManager"%>
<%@page import="it.eng.spagobi.container.SpagoBISessionContainer"%>
<%@page
	import="it.eng.spagobi.container.strategy.LightNavigatorContextRetrieverStrategy"%>
<%@page import="java.util.Iterator"%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spagobi.commons.utilities.PortletUtilities"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>
<%@page import="it.eng.spagobi.commons.bo.SessionUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>

<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>

<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="java.util.Locale.Builder" %>

<!-- IMPORT TAG LIBRARY  -->
<%@ taglib uri="/WEB-INF/tlds/spagobi.tld" prefix="spagobi"%>

<%-- START SCRIPT FOR DOMAIN DEFINITION (MUST BE EQUAL BETWEEN SPAGOBI AND EXTERNAL ENGINES) -->
commented by Davide Zerbetto on 12/10/2009: there are problems with MIF (Ext ManagedIFrame library) library
<script type="text/javascript">
	document.domain='<%= GeneralUtilities.getSpagoBiDomain() %>';
</script>
<!-- END SCRIPT FOR DOMAIN DEFINITION --%>

<!-- GET SPAGO OBJECTS  -->
<%
	//Enumeration headers = request.getHeaderNames();
	//while (headers.hasMoreElements()) {
	//	String headerName = (String) headers.nextElement();
	//	String header = request.getHeader(headerName);
	//	System.out.println(header + ": ");
	//}

	RequestContainer aRequestContainer = null;
	ResponseContainer aResponseContainer = null;
	SessionContainer aSessionContainer = null;
	IUrlBuilder urlBuilder = null;
	IMessageBuilder msgBuilder = null;
	
	String sbiMode = null;
		
	// case of portlet mode
	aRequestContainer = RequestContainerPortletAccess.getRequestContainer(request);
	aResponseContainer = ResponseContainerPortletAccess.getResponseContainer(request);
	if (aRequestContainer == null) {
		// case of web mode
		//aRequestContainer = RequestContainerAccess.getRequestContainer(request);
		aRequestContainer = RequestContainer.getRequestContainer();
		//aResponseContainer = ResponseContainerAccess.getResponseContainer(request);
		aResponseContainer = ResponseContainer.getResponseContainer();
	}
	
	String channelType = aRequestContainer.getChannelType();
	if ("PORTLET".equalsIgnoreCase(channelType)) sbiMode = "PORTLET";
	else sbiMode = "WEB";

    // = (String)sessionContainer.getAttribute(Constants.USER_LANGUAGE);
    //country = (String)sessionContainer.getAttribute(Constants.USER_COUNTRY);
	
	// create url builder 
	urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);

	// create message builder
	msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
	// get other spago object
	SourceBean aServiceRequest = aRequestContainer.getServiceRequest();
	SourceBean aServiceResponse = aResponseContainer.getServiceResponse();
	aSessionContainer = aRequestContainer.getSessionContainer();
	
	
	//get session access control object
	CoreContextManager contextManager = new CoreContextManager(new SpagoBISessionContainer(aSessionContainer), 
				new LightNavigatorContextRetrieverStrategy(aServiceRequest));
	
	// urls for resources
	String linkSbijs = urlBuilder.getResourceLink(request, "/js/spagobi.js");
	String linkProto = urlBuilder.getResourceLink(request, "/js/prototype/javascripts/prototype.js");
	String linkProtoWin = urlBuilder.getResourceLink(request, "/js/prototype/javascripts/window.js");
	String linkProtoEff = urlBuilder.getResourceLink(request, "/js/prototype/javascripts/effects.js");
	String linkProtoDefThem = urlBuilder.getResourceLink(request, "/js/prototype/themes/default.css");
	String linkProtoAlphaThem = urlBuilder.getResourceLink(request, "/js/prototype/themes/alphacube.css");

	SessionContainer permanentSession = aSessionContainer.getPermanentContainer();
	

	// If Language is alredy defined keep it
	
	String curr_language=(String)permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
	String curr_country=(String)permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
	String sessScript = (String) permanentSession.getAttribute(SpagoBIConstants.AF_SCRIPT);
	String curr_script = (sessScript!=null && !sessScript.isEmpty()) ? sessScript : "";

	Locale locale = null;
	
	if (curr_language != null && curr_country != null	&& !curr_language.equals("") && !curr_country.equals("")) {
		Builder tmpLocale = new Locale.Builder().setLanguage(curr_language).setRegion(curr_country);

		if (StringUtils.isNotBlank(curr_script)) {
			tmpLocale.setScript(curr_script);
		}

		locale = tmpLocale.build();
	}
	else {	
	if (sbiMode.equals("PORTLET")) {
		locale = PortletUtilities.getLocaleForMessage();
	} else {
		locale = MessageBuilder.getBrowserLocaleFromSpago();
	}
	// updates locale information on permanent container for Spago messages mechanism
	if (locale != null) {
		permanentSession.setAttribute(Constants.USER_LANGUAGE, locale.getLanguage());
		permanentSession.setAttribute(Constants.USER_COUNTRY, locale.getCountry());
	}
	}
	
	IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	
	String userUniqueIdentifier="";
	String userId="";
	String userName="";
	String sessionRole="";
	boolean isTechnicalUser=false;
	List userRoles = new ArrayList();;
	
	if (userProfile!=null){
		userId=(String)((UserProfile)userProfile).getUserId();
		userUniqueIdentifier=(String)userProfile.getUserUniqueIdentifier();
		userName=(String)((UserProfile)userProfile).getUserName();
		userRoles = (ArrayList)userProfile.getRoles();
		sessionRole = userProfile instanceof SessionUserProfile ? ((SessionUserProfile)userProfile).getSessionRole() : null;
		isTechnicalUser = UserUtilities.isTechnicalUser(userProfile);
	}
	
	
	
	
	// Set Theme
	String currTheme=ThemesManager.getCurrentTheme(aRequestContainer);
	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
	
	String currViewThemeName = ThemesManager.getCurrentThemeName(currTheme);
	
	String parametersStatePersistenceEnabled = SingletonConfig.getInstance().getConfigValue("SPAGOBI.EXECUTION.PARAMETERS.statePersistenceEnabled");
	String parameterStatePersistenceScope = SingletonConfig.getInstance().getConfigValue("SPAGOBI.EXECUTION.PARAMETERS.statePersistenceScope");
	// to ensure back compatibility
	if(parametersStatePersistenceEnabled == null) {
		parametersStatePersistenceEnabled = SingletonConfig.getInstance().getConfigValue("SPAGOBI.SESSION_PARAMETERS_MANAGER.enabled");
	}
	String parametersMementoPersistenceEnabled= SingletonConfig.getInstance().getConfigValue("SPAGOBI.EXECUTION.PARAMETERS.mementoPersistenceEnabled");
	String parameterMementoPersistenceScope = SingletonConfig.getInstance().getConfigValue("SPAGOBI.EXECUTION.PARAMETERS.mementoPersistenceScope");
	String parameterMementoPersistenceDepth = SingletonConfig.getInstance().getConfigValue("SPAGOBI.EXECUTION.PARAMETERS.mementoPersistenceDepth");
	
	String flatViewModality = SingletonConfig.getInstance().getConfigValue("SPAGOBI.DOCUMENTBROWSER.FLAT");
	%>


<!-- based on ecexution mode include initial html  -->
<% if (sbiMode.equalsIgnoreCase("WEB")){ %>



<%@page import="it.eng.spagobi.commons.SingletonConfig"%><html
	lang="<%=locale != null ? locale.getLanguage() : GeneralUtilities.getDefaultLocale().getLanguage()%>">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>Knowage</title>
<link rel="shortcut icon"
	href="<%=urlBuilder.getResourceLinkByTheme(request, "img/favicon.ico", currTheme)%>" />
</head>
<body>
	<%} %>



	<script type="text/javascript"
		src="<%=urlBuilder.getResourceLink(request, "js/lib/ext-2.0.1/adapter/ext/ext-base.js")%>"></script>
	<%-- Ext lib debug: --%>
	<%--<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/ext-2.0.1/ext-all-debug.js")%>"></script>--%>
	<%-- Ext lib for release --%>
	<script type="text/javascript"
		src="<%=urlBuilder.getResourceLink(request, "js/lib/ext-2.0.1/ext-all.js")%>"></script>
	<%-- Ext js overrides --%>
	<script type="text/javascript"
		src="<%=urlBuilder.getResourceLink(request, "js/src/ext/sbi/overrides/overrides.js")%>"></script>

	<script type="text/javascript">
	Ext.BLANK_IMAGE_URL = '<%=urlBuilder.getResourceLink(request, "/js/lib/ext-2.0.1/resources/images/default/s.gif")%>';

	// See https://spagobi.eng.it/jira/browse/SPAGOBI-1916
	Ext.DatePicker.prototype.startDay = 1;

	Ext.Ajax.defaultHeaders = {
			'Powered-By': 'Ext'
	};
	Ext.Ajax.timeout = 300000;

    // general SpagoBI configuration
    Ext.ns("Sbi.config");
    Sbi.config = function () {
        return {
       		// login url, used when session is expired
        	loginUrl: '<%= KnowageSystemConfiguration.getKnowageContext() %>',
        	currTheme: '<%= currTheme %>',
        	curr_country: '<%= curr_country %>',
        	curr_language: '<%= curr_language%>',
        	curr_script: '<%= curr_script%>',
        	contextName: '<%= KnowageSystemConfiguration.getKnowageContext() %>',
        	adapterPath: '<%= KnowageSystemConfiguration.getKnowageContext() + GeneralUtilities.getSpagoAdapterHttpUrl() %>',
        	supportedLocales: <%= GeneralUtilities.getSupportedLocalesAsJSONArray().toString() %>,
            // the date format localized according to user language and country
            localizedDateFormat: '<%= GeneralUtilities.getLocaleDateFormatForExtJs(permanentSession) %>',
            // the timestamp format localized according to user language and country
            localizedTimestampFormat: '<%= GeneralUtilities.getLocaleDateFormatForExtJs(permanentSession) %> H:i:s',
            // the date format to be used when communicating with server
            clientServerDateFormat: '<%= GeneralUtilities.getServerDateFormatExtJs() %>',
            // the timestamp format to be used when communicating with server
            clientServerTimestampFormat: '<%= GeneralUtilities.getServerTimestampFormatExtJs() %>',
            
         	<%if(parametersStatePersistenceEnabled != null) {%>
        	isParametersStatePersistenceEnabled: <%= Boolean.valueOf(parametersStatePersistenceEnabled) %>,
        	<%}%>
        	
        	<%if(parameterStatePersistenceScope != null) {%>
        	parameterStatePersistenceScope: '<%= parameterStatePersistenceScope.toUpperCase() %>',
        	<%}%>
        	
        	<%if(parametersMementoPersistenceEnabled != null) {%>
        	isParametersMementoPersistenceEnabled: <%= Boolean.valueOf(parametersMementoPersistenceEnabled) %>,
        	<%}%>
        	
        	<%if(parameterMementoPersistenceScope != null) {%>
        	parameterMementoPersistenceScope: '<%= parameterMementoPersistenceScope.toUpperCase() %>',
        	<%}%>
        	
        	<%if(parameterMementoPersistenceDepth != null) {%>
        	parameterMementoPersistenceDepth: <%= parameterMementoPersistenceDepth %>,
        	<%}%>
        	
        	flatViewModality: <%= flatViewModality%>,
        	
        	isSSOEnabled: <%= GeneralUtilities.isSSOEnabled() %>
        	
        };
    }();

    // javascript-side user profile object
    Ext.ns("Sbi.user");
    Sbi.user.userUniqueIdentifier = '<%= StringEscapeUtils.escapeJavaScript(userUniqueIdentifier) %>';
    Sbi.user.userId = '<%= StringEscapeUtils.escapeJavaScript(userId) %>';
    Sbi.user.userName = '<%= StringEscapeUtils.escapeJavaScript(userName) %>';    
    Sbi.user.ismodeweb = <%= sbiMode.equals("WEB")? "true" : "false"%>;
    Sbi.user.isSuperAdmin = '<%= userProfile != null && ((UserProfile)userProfile).getIsSuperadmin() %>';
	Sbi.user.roles = new Array();
	Sbi.user.sessionRole = '<%= sessionRole != null ? StringEscapeUtils.escapeJavaScript(sessionRole)  : ""%>';
	<%
	StringBuffer buffer = new StringBuffer("[");
	if (userProfile != null && userProfile.getFunctionalities() != null && !userProfile.getFunctionalities().isEmpty()) {
		Iterator it = userProfile.getFunctionalities().iterator();
		while (it.hasNext()) {
			String functionalityName = (String) it.next();
			buffer.append("'" + functionalityName + "'");
			if (it.hasNext()) {
				buffer.append(",");
			}
		}
	}
	buffer.append("]");
	%>
	
	<%
	// Set roles
	Integer indexRoles = Integer.valueOf(0);
	for(Iterator it = userRoles.iterator();it.hasNext();)
	{
		String aRole = (String)it.next();
	%>
		Sbi.user.roles[<%=indexRoles.toString()%>] = '<%=StringEscapeUtils.escapeJavaScript(aRole)%>';
	<%
	indexRoles = Integer.valueOf( indexRoles.intValue()+1 );
	}
	%>
	
	
	// Sbi.user.functionalities is a javascript array containing all user functionalities' names
	Sbi.user.functionalities = <%= buffer.toString() %>;
</script>

	<SCRIPT language='JavaScript' src='<%=linkSbijs%>'></SCRIPT>

	<!-- import css  -->
	<%
	// based on mode import right css 
	if (sbiMode.equalsIgnoreCase("WEB")) {
%>
	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLinkByTheme(request, "css/spagobi_wa.css",currTheme)%>'
		type='text/css' />
	<%  } else {  %>
	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLinkByTheme(request, "css/spagobi_portlet.css",currTheme)%>'
		type='text/css' />
	<%	} %>

	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLinkByTheme(request, "css/spagobi_shared.css",currTheme)%>'
		type='text/css' />

	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLinkByTheme(request, "css/jsr168.css",currTheme)%>'
		type='text/css' />

	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLinkByTheme(request, "css/external.css",currTheme)%>'
		type='text/css' />

	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLinkByTheme(request, "css/menu.css",currTheme)%>'
		type='text/css' />

	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLink(request, "js/lib/ext-2.0.1/resources/css/ext-all.css")%>'
		type='text/css' />
	<%-- Ext css overrides --%>
	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLink(request, "js/src/ext/sbi/overrides/overrides.css")%>'
		type='text/css' />


	<% // get the current ext theme
	 String extTheme=ThemesManager.getTheExtTheme(currTheme);
	 %>

	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLink(request, "js/lib/ext-2.0.1/resources/css/"+extTheme)%>'
		type='text/css' />

	<LINK rel='StyleSheet'
		href='<%=urlBuilder.getResourceLink(request, "js/lib/ext-2.0.1/overrides/resources/css/"+extTheme)%>'
		type='text/css' />

	<script>
	document.onselectstart = function() { return true; }
</script>
