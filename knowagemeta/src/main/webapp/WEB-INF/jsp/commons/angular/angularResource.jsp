<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<!-- this imports are used for language controls  -->
<%@page import="it.eng.spago.base.*"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.PortletUtilities"%>
<%@page import="it.eng.spago.security.IEngUserProfile" %>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities" %>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineStartServletIOManager"%>
<%@page import="it.eng.knowage.commons.utilities.urls.UrlBuilder"%>
<%@page import="it.eng.knowage.wapp.Version"%>
<%@page import="it.eng.knowage.wapp.Environment"%>

<%
	UrlBuilder urlBuilder = new UrlBuilder();
	String spagoBiContext = GeneralUtilities.getSpagoBiContext();	//  /knowage
	String metaEngineContext = request.getContextPath(); 			//  /knowagemetaengine
	String dynamicResourcesBasePath;  								//  /knowage/js/src
	String dynamicResourcesEnginePath;  							//  /knowagemetaengine/js/src
	Environment environment = Version.getEnvironment();
	
	if (environment == Environment.PRODUCTION) {
		dynamicResourcesBasePath = spagoBiContext + "/js/src-" + Version.getCompleteVersion();
		dynamicResourcesEnginePath = metaEngineContext + "/js/src-" + Version.getCompleteVersion();
	} else {
		dynamicResourcesBasePath = GeneralUtilities.getSpagoBiContext() + "/js/src";
		dynamicResourcesEnginePath = metaEngineContext + "/js/src";
	}	

	RequestContainer aRequestContainer = null;
	ResponseContainer aResponseContainer = null;
	SessionContainer aSessionContainer = null;
	IMessageBuilder msgBuilder = null;
	String sbiMode = null;

	// create message builder
	msgBuilder = MessageBuilderFactory.getMessageBuilder();
	EngineStartServletIOManager ioManager= (EngineStartServletIOManager) request.getSession().getAttribute("ioManager");
	
	List<String> productTypes = (List<String>) request.getSession().getAttribute("productTypes") ;
	List<String> profileAttributes = (List<String>) request.getSession().getAttribute("profileAttributes") ;
	List<String> avaiableRoles = (List<String>) request.getSession().getAttribute("avaiableRoles") ;
	
	
	Locale locale= locale =ioManager.getLocale();

	
	//load user profile 
	IEngUserProfile userProfile = (IEngUserProfile) request.getSession().getAttribute("userProfile");
	
	String userUniqueIdentifier="";
	String userId="";
	String userName="";
	String defaultRole="";
	List userRoles = new ArrayList();
	//if (userProfile!=null) userId=(String)userProfile.getUserUniqueIdentifier();
	if (userProfile!=null){
		userId=(String)((UserProfile)userProfile).getUserId();
		userUniqueIdentifier=(String)userProfile.getUserUniqueIdentifier();
		userName=(String)((UserProfile)userProfile).getUserName();
		userRoles = (ArrayList)userProfile.getRoles();
		defaultRole = ((UserProfile)userProfile).getDefaultRole();		
		
	}
	
	String translatedModel= (String) request.getSession().getAttribute("translatedModel");
	
%>


<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>
