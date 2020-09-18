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
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineStartServletIOManager"%>
<%@page import="it.eng.knowage.commons.utilities.urls.UrlBuilder"%>

<%
	String spagoBiContext = KnowageSystemConfiguration.getKnowageContext();						//  /knowage
	String metaEngineContext = request.getContextPath(); 								//  /knowagemetaengine
	UrlBuilder urlBuilder = new UrlBuilder(spagoBiContext, metaEngineContext);
	String dynamicResourcesBasePath = urlBuilder.getDynamicResorucesBasePath();  		//  /knowage/js/src
	String dynamicResourcesEnginePath = urlBuilder.getDynamicResourcesEnginePath(); 	//  /knowagemetaengine/js/src
	
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
	List userRoles = new ArrayList();
	//if (userProfile!=null) userId=(String)userProfile.getUserUniqueIdentifier();
	if (userProfile!=null){
		userId=(String)((UserProfile)userProfile).getUserId();
		userUniqueIdentifier=(String)userProfile.getUserUniqueIdentifier();
		userName=(String)((UserProfile)userProfile).getUserName();
		userRoles = (ArrayList)userProfile.getRoles();
		
	}
	
	String translatedModel= (String) request.getSession().getAttribute("translatedModel");
	
%>

<script type="text/javascript">
var locale = '<%=locale.getLanguage()%>_<%=locale.getCountry()%>';
</script>


<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>
