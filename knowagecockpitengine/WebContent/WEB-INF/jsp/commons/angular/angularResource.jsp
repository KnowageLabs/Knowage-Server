<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spagobi.services.common.EnginConf"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="it.eng.knowage.engine.cockpit.CockpitEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities" %>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="org.json.JSONObject"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>

<%
	SourceBean sb = ((SourceBean) EnginConf.getInstance().getConfig().getAttribute("ChartEngineContextName"));
	String chartEngineContextName = sb.getCharacters();

	CockpitEngineInstance engineInstance= (CockpitEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	IEngUserProfile profile= engineInstance.getUserProfile();
	Map env= engineInstance.getEnv();
	Locale locale= engineInstance.getLocale();
	String curr_language=locale.getLanguage();
	String curr_country=locale.getCountry();
	String template= engineInstance.getTemplate().toString();
	String documentMode = (request.getParameter("documentMode")==null)?"VIEW":request.getParameter("documentMode");
	String 	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);

	String userUniqueIdentifier="";
	String userId="";
	String userName="";
	String defaultRole="";
	List userRoles = new ArrayList();
	
	Integer docId = engineInstance.getDocumentId();
	String docLabel = (engineInstance.getDocumentLabel()==null)?"":engineInstance.getDocumentLabel().toString();
	String docName = (engineInstance.getDocumentName()==null)?"":engineInstance.getDocumentName().toString();
	String docDescription = (engineInstance.getDocumentDescription()==null)?"":engineInstance.getDocumentDescription().toString();
	
	
	//if (userProfile!=null) userId=(String)userProfile.getUserUniqueIdentifier();
	if (profile!=null){
		userId=(String)((UserProfile)profile).getUserId();
		userUniqueIdentifier=(String)profile.getUserUniqueIdentifier();
		userName=(String)((UserProfile)profile).getUserName();
		userRoles = (ArrayList)profile.getRoles();
		defaultRole = ((UserProfile)profile).getDefaultRole();		
		
	}
	
	 Map analyticalDrivers  = engineInstance.getAnalyticalDrivers();
	    Map driverParamsMap = new HashMap();
		for(Object key : engineInstance.getAnalyticalDrivers().keySet()){
			if(key instanceof String){
				String value = request.getParameter((String)key);
				if(value!=null){
					driverParamsMap.put(key, value);
				}
			}
		}
		String analyticalDriversParams = new JSONObject(driverParamsMap).toString().replaceAll("'", "\\\\'");
		
		List<String> outputParametersList  = engineInstance.getOutputParameters();
	    String outputParameters = "";  
		if(outputParametersList.size()>0){
			Map outParMap = new HashMap<String, Boolean>();
			for(int i = 0; i<outputParametersList.size(); i++){
				String par = outputParametersList.get(i);
				outParMap.put(par, true);
			}
		    outputParameters= new JSONObject(outParMap).toString().replaceAll("'", "\\\\'");
	        }
		
			
		
%>


	



<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>
