<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="it.eng.spagobi.commons.serializer.DocumentsJSONDecorator"%>
<%@page import="org.json.JSONObject"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>    
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="it.eng.spagobi.analiticalmodel.execution.service.ExecuteDocumentAction"%>
<%@page import="it.eng.spagobi.commons.bo.Domain"%>
<%@page import="it.eng.spagobi.commons.dao.IDomainDAO"%>
<%@page import="it.eng.spagobi.engines.config.dao.IEngineDAO"%>
<%@page import="it.eng.spagobi.engines.config.bo.Engine"%>
<%@page import="java.util.ArrayList"%>
<%@page import="it.eng.spagobi.commons.metadata.SbiDomains"%>
<%@page import="it.eng.spagobi.engines.config.metadata.SbiExporters"%>
<%@page import="it.eng.spagobi.engines.config.bo.Exporters"%>
<%@page import="java.util.List"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.SubObject"%>
<%@page import="it.eng.spagobi.commons.serializer.SerializerFactory"%>

<!--  jQuery (HighCharts dependency) -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/jquery-1.5.1/jquery-1.5.1.js")%>"></script>

<!--  HighCharts -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/highcharts-3.0.7/highcharts.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/highcharts-3.0.7/highcharts-more.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/highcharts-3.0.7/modules/exporting.js")%>"></script>

<%! private static transient Logger logger = Logger.getLogger(ExecuteDocumentAction.class);%>

	<%@ include file="/WEB-INF/jsp/commons/includeMessageResource.jspf" %>
	<%@ include file="/WEB-INF/jsp/commons/importSbiJS.jspf"%>
	
	<script type="text/javascript">
    Ext.BLANK_IMAGE_URL = '<%=urlBuilder.getResourceLink(request, "/js/lib/ext-2.0.1/resources/images/default/s.gif")%>';
    
    Sbi.config = {};

    // the user language
    Sbi.config.language = '<%= locale.getLanguage() %>';
	// the user country
    Sbi.config.country = '<%= locale.getCountry() %>';
    // the date format localized according to user language and country
    Sbi.config.localizedDateFormat = '<%= GeneralUtilities.getLocaleDateFormatForExtJs(permanentSession) %>';
    // the date format to be used when communicating with server
    Sbi.config.clientServerDateFormat = '<%= GeneralUtilities.getServerDateFormatExtJs() %>';
    // the timestamp format to be used when communicating with server
    Sbi.config.clientServerTimestampFormat = '<%= GeneralUtilities.getServerTimestampFormatExtJs() %>';
    //the SpagoBI Context name
    Sbi.config.contextName = '<%= GeneralUtilities.getSpagoBiContext() %>';

    
    var url = {
    	host: '<%= request.getServerName()%>'
    	, port: '<%= request.getServerPort()%>'
    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
    	   				  request.getContextPath().substring(1):
    	   				  request.getContextPath()%>'
    	    
    };
    var params = {
    	SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
    	, SBI_ENVIRONMENT: <%= (request.getParameter("MYANALYSIS") !=null && request.getParameter("MYANALYSIS").equalsIgnoreCase("TRUE"))?"'MYANALYSIS'": "'DOCBROWSER'" %>
    	, LIGHT_NAVIGATOR_DISABLED: 'TRUE'
    };
    

    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
    	baseUrl: url
        , baseParams: params
    });

    <%BIObject obj = (BIObject) aServiceResponse.getAttribute(SpagoBIConstants.OBJECT);
	SubObject subObject = (SubObject) aServiceResponse.getAttribute(SpagoBIConstants.SUBOBJECT);
    String parameters = (String) aServiceRequest.getAttribute(ObjectsTreeConstants.PARAMETERS);
    logger.debug("Document parameters in request are [" + parameters + "]");
    String subobjectName = (String) aServiceRequest.getAttribute(SpagoBIConstants.SUBOBJECT_NAME);
    logger.debug("Subobject name in request is [" + subobjectName + "]");
    String snapshotName = (String) aServiceRequest.getAttribute(SpagoBIConstants.SNAPSHOT_NAME);
    logger.debug("Snapshot name in request is [" + snapshotName + "]");
    String snapshotHistoryNumber = (String) aServiceRequest.getAttribute(SpagoBIConstants.SNAPSHOT_HISTORY_NUMBER);
    logger.debug("Snapshot history number in request is [" + snapshotHistoryNumber + "]");
    
    String toolbarVisible = (String) aServiceRequest.getAttribute(SpagoBIConstants.TOOLBAR_VISIBLE);
    boolean toolbarHidden = (toolbarVisible == null || toolbarVisible.trim().equals("")) ? false : !Boolean.parseBoolean(toolbarVisible);
    String shortcutsVisible = (String) aServiceRequest.getAttribute(SpagoBIConstants.SLIDERS_VISIBLE);
    boolean shortcutsHidden = (shortcutsVisible == null || shortcutsVisible.trim().equals("")) ? false : !Boolean.parseBoolean(shortcutsVisible);
    
    Integer engineId = null;
	Engine engineObj = (obj == null) ? null : obj.getEngine();
	String exportersJSArray = "";
	if(engineObj!=null){
	
		IEngineDAO engineDao=DAOFactory.getEngineDAO();
		List exporters=new ArrayList();
		exporters=engineDao.getAssociatedExporters(engineObj);			
		if(!exporters.isEmpty()){
			exportersJSArray = "[" ;
			for (Iterator iterator = exporters.iterator(); iterator.hasNext();) {
				
				 Exporters exp = (Exporters) iterator.next();
				 Integer domainId=exp.getDomainId();
				 
				 IDomainDAO domainDao=DAOFactory.getDomainDAO();
				 Domain domain=domainDao.loadDomainById(domainId);
				 if(domain!=null){
					 String value_cd=domain.getValueCd();
					 String urlExporter=null;	
					 if (value_cd!=null){
						 if(iterator.hasNext()){
							 exportersJSArray +="'"+value_cd+"'," ;
						 }else{
							 exportersJSArray +="'"+value_cd+"']" ;
						 }
					 }
				 }
			}
		}
	}
	// 20100505: check if in request there is parameter
	boolean comingFromDocOrTreeList = false;
	if(request.getParameter("BIOBJECT_TREE_LIST") != null){
		comingFromDocOrTreeList = true;
	}
	
	boolean fromMyAnalysis = false;
	if(request.getParameter("MYANALYSIS") != null && request.getParameter("MYANALYSIS").equalsIgnoreCase("TRUE")){
		fromMyAnalysis = true;
	}
	
	
	JSONObject biobjectJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize( obj , locale);
	DocumentsJSONDecorator.decorateDocument(biobjectJSON, userProfile, null);%>
    //var menuConfig = <%= aServiceResponse.getAttribute("metaConfiguration")%>;
    
	<%
	if (obj == null) {
		%>
		var object = undefined;
		<%
	} else {
		%>
		var object = <%= biobjectJSON.toString()%>
	<%		
	}
	%>

	var parameters = <%= (parameters != null  && !parameters.trim().equals("")) ? ("'" + parameters.replace("'", "\\\'") + "'") : "undefined" %>;
	<% if (subObject != null) { %>
	var subobject = {id: <%= subObject.getId() %>, 'name': '<%= subObject.getName().replace("'", "\\\'") %>'};
	<% } else { %>
	var subobject = undefined;
	<% } %>
	var snapshotName = <%= (snapshotName != null && !snapshotName.trim().equals("")) ? ("'" + snapshotName.replace("'", "\\\'") + "'") : "undefined" %>;
	var snapshotHistoryNumber = <%= (snapshotHistoryNumber != null && !snapshotHistoryNumber.trim().equals("")) ? snapshotHistoryNumber : "0" %>;
	var snaphost = {'name': snapshotName, 'historyNumber': snapshotHistoryNumber};
	var shortcutsHidden = <%= shortcutsHidden %>;
	var toolbarHidden = <%= toolbarHidden %>;

// 20100505, added fromDOcTreeOrList parameter	
    var config = {
    	document: object
    	, preferences: {
			parameters: parameters
			, subobject: subobject
			, snapshot: snaphost
			, toolbarHidden: toolbarHidden
			, shortcutsHidden: shortcutsHidden
			, fromDocTreeOrList : <%=comingFromDocOrTreeList%>
    		, fromMyAnalysis : <%=fromMyAnalysis%>
	    }
	};
	
	Ext.onReady(function(){
		Ext.QuickTips.init();
		if (object === undefined) {
        	Ext.MessageBox.show({
           		title: 'Error'
           		, msg: 'Required document was not found or you cannot see it'
           		, buttons: Ext.MessageBox.OK     
           		, icon: Ext.MessageBox.ERROR
           		, modal: false
       		});
		} else {
			var executionPanel = new Sbi.execution.ExecutionPanel(config, object);
			var viewport = new Ext.Viewport({
				layout: 'border'
				, items: [
				    {
				       region: 'center',
				       layout: 'fit',
				       items: [executionPanel],
				       border : false
				    }
				]
				, border : false
			});
			executionPanel.execute();
			
			// utility class for invoking export from an external application
			Sbi.execution.ExporterUtils.setExecutionPanel(executionPanel);
		}
	});
    
    </script>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>