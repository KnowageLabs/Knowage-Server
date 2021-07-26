<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@page import="org.json.JSONArray"%>
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
<%@page import="it.eng.spagobi.engine.chart.util.ChartEngineUtil"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="org.json.JSONObject"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter"%>
<%@page import="it.eng.spagobi.engine.chart.util.ChartEngineUtil"%>
<%@page import="it.eng.spagobi.commons.bo.AccessibilityPreferences" %>
<%@page import="it.eng.knowage.commons.utilities.urls.UrlBuilder"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>

<%
	String spagoBiContext = KnowageSystemConfiguration.getKnowageContext();							//  /knowage
	String cockpitEngineContext = request.getContextPath(); 								//  /cockpitengine
	UrlBuilder urlBuilder = new UrlBuilder(spagoBiContext, cockpitEngineContext);
	String dynamicResourcesBasePath = urlBuilder.getDynamicResorucesBasePath();  			//  /knowage/js/src
	String dynamicResourcesEnginePath = urlBuilder.getDynamicResourcesEnginePath();  		//  /cockpitengine/js/src
	
	SourceBean sb = ((SourceBean) EnginConf.getInstance().getConfig().getAttribute("ChartEngineContextName"));
	String chartEngineContextName = sb.getCharacters();

	CockpitEngineInstance engineInstance= (CockpitEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	IEngUserProfile profile= engineInstance.getUserProfile();
	Map env= engineInstance.getEnv();
	Locale locale= engineInstance.getLocale();
	String curr_language=locale.getLanguage();
	String curr_country=locale.getCountry();
	String curr_script=locale.getScript();
	String template= engineInstance.getTemplate().toString();
	String documentMode = (request.getParameter("documentMode")==null)?"VIEW":request.getParameter("documentMode");
	Integer initialSheet = (request.getParameter("sheet")==null)?0: Integer.parseInt(request.getParameter("sheet"));
	String initialSelections = (request.getParameter("COCKPIT_SELECTIONS")==null) ? "{}" : request.getParameter("COCKPIT_SELECTIONS");
	Boolean exportMode = (request.getParameter("export")==null)?false: Boolean.parseBoolean(request.getParameter("export"));
	String 	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	String folderId = (request.getParameter(SpagoBIConstants.FUNCTIONALITY_ID)==null)?"":request.getParameter(SpagoBIConstants.FUNCTIONALITY_ID);

	String userUniqueIdentifier="";
	String userId="";
	String userName="";
	List userRoles = new ArrayList();
	
	AccessibilityPreferences ap = null;
	Boolean isUIOEnabled = false;
	Boolean isRobobrailleEnabled = false;
	Boolean isVoiceEnable = false;
	Boolean isGraphSonificationEnabled = false;
	
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
		ap =  UserUtilities.readAccessibilityPreferencesByUser(profile);
	    
	    if(ap != null){
	    	 isUIOEnabled = ap.isEnableUio();
	    	 isRobobrailleEnabled = ap.isEnableRobobraille();
	    	 isVoiceEnable = ap.isEnableVoice();
	    	 isGraphSonificationEnabled = ap.isEnableGraphSonification(); 	
	    }
		
	}
	
	Map analyticalDrivers  = engineInstance.getAnalyticalDrivers();
    Map driverParamsMap = new HashMap();
    Map driverParamsObjMap = new HashMap();
	for(Object key : engineInstance.getAnalyticalDrivers().keySet()){
		if(key instanceof String){
			String value = request.getParameter((String)key);
			if(value!=null){
				driverParamsMap.put(key, value);
			}
			String keyDescription = (String)key+"_description";
            String description = request.getParameter(keyDescription);
            
            if(description!=null){
                driverParamsMap.put(key+"_description", description);
            }
            
            
            
			
			
		}
	}
	
	try {
		IBIObjectParameterDAO objectParameterDAO = DAOFactory.getBIObjectParameterDAO();
		IParameterDAO parameterDao = DAOFactory.getParameterDAO();
		
		List<BIObjectParameter> bIObjectParameters = objectParameterDAO.loadBIObjectParametersById(docId);
		for(BIObjectParameter bIObjectParameter : bIObjectParameters){
			JSONObject param = new JSONObject();
			param.put("label",bIObjectParameter.getLabel());
			param.put("url",bIObjectParameter.getParameterUrlName());
			driverParamsObjMap.put(bIObjectParameter.getLabel(), param);
			if(bIObjectParameter.getVisible().compareTo(1) == 0){
				Parameter parameter = parameterDao.loadForDetailByParameterID(bIObjectParameter.getParID());
				String parameterName = parameter.getName();
				if(!driverParamsMap.containsKey(parameterName)){
					driverParamsMap.put(parameterName, "");
				}
			}
		}
	} catch (Exception e) {
	}
	
	String analyticalDriversParams = new JSONObject(driverParamsMap).toString().replaceAll("'", "\\\\'");
	String analyticalDriversParamsObj = new JSONObject(driverParamsObjMap).toString().replaceAll("'", "\\\\'");
	
    String outputParameters = "{}"; 
	Map<String,String> outParMap = engineInstance.getOutputParameters();
	if(outParMap != null){
		   outputParameters= new JSONObject(outParMap).toString().replaceAll("'", "\\\\'");
	}
	
	
	/*
	List<String> outputParametersList  = engineInstance.getOutputParameters();
    String outputParameters = "{}";  
    if(outputParametersList != null){
	if(outputParametersList.size()>0){
		Map outParMap = new HashMap<String, Boolean>();
		for(int i = 0; i<outputParametersList.size(); i++){
			String par = outputParametersList.get(i);
			outParMap.put(par, true);
		}
	    outputParameters= new JSONObject(outParMap).toString().replaceAll("'", "\\\\'");
        }
    }
    */
%>

<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf"%>