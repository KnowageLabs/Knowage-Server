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


<%
System.out.println("1");
	RequestContainer aRequestContainer = null;
	ResponseContainer aResponseContainer = null;
	SessionContainer aSessionContainer = null;
	IMessageBuilder msgBuilder = null;
	String sbiMode = null;
	System.out.println("2");
	// case of portlet mode
	aRequestContainer = RequestContainerPortletAccess.getRequestContainer(request);
	aResponseContainer = ResponseContainerPortletAccess.getResponseContainer(request);
	if (aRequestContainer == null) {
		// case of web mode
		aRequestContainer = RequestContainer.getRequestContainer();
		if(aRequestContainer == null){
			//case of REST 
			aRequestContainer = RequestContainerAccess.getRequestContainer(request);
		}
		aResponseContainer = ResponseContainer.getResponseContainer();
		if(aResponseContainer == null){
			//case of REST
			aResponseContainer = ResponseContainerAccess.getResponseContainer(request);
		}
	}
	System.out.println("3");
	
	// create message builder
	msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
	
	
	// get other spago object
	aSessionContainer = aRequestContainer.getSessionContainer();
	SessionContainer permanentSession = aSessionContainer.getPermanentContainer();
	System.out.println("4");
	// If Language is alredy defined keep it
	String curr_language=(String)permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
	String curr_country=(String)permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
	Locale locale = null;
	
	if (curr_language != null && curr_country != null	&& !curr_language.equals("") && !curr_country.equals("")) {
		locale = new Locale(curr_language, curr_country, "");
	} else {
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
	}System.out.println("5");
	
	//load user profile 
	IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	
	String userUniqueIdentifier="";
	String userId="";
	String userName="";
	String defaultRole="";
	List userRoles = new ArrayList();
	System.out.println("6");
	//if (userProfile!=null) userId=(String)userProfile.getUserUniqueIdentifier();
	if (userProfile!=null){
		userId=(String)((UserProfile)userProfile).getUserId();
		userUniqueIdentifier=(String)userProfile.getUserUniqueIdentifier();
		userName=(String)((UserProfile)userProfile).getUserName();
		userRoles = (ArrayList)userProfile.getRoles();
		defaultRole = ((UserProfile)userProfile).getDefaultRole();		
		
	}
	System.out.println("7");
	
%>


<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>
