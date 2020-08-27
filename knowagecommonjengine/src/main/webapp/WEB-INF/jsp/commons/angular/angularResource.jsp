<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<!-- this imports are used for language controls  -->
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="java.util.Locale"%>
<%@page import="it.eng.spagobi.engines.commonj.CommonjEngine"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="org.json.JSONObject"%>


<%
/*
*/
String contextName;
String environment;

Locale locale = null;
String template;
List<String> includes;
String datasetLabel;
String aggregations = "";
String selections = "";
String associations = "";
String widgetId = "";
String metaData = "";

contextName = KnowageSystemConfiguration.getKnowageContext(); 
environment = request.getParameter("SBI_ENVIRONMENT"); 

String country = request.getParameter(SpagoBIConstants.SBI_COUNTRY); 
String language = request.getParameter(SpagoBIConstants.SBI_LANGUAGE); 
locale = new Locale(language, country);

String uuidO=request.getParameter("SBI_EXECUTION_ID")!=null? request.getParameter("SBI_EXECUTION_ID"): "null";
%>



<%@include file="../../commons/includeMessageResource.jspf"%>
