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


<%@page import="it.eng.spagobi.profiling.bean.SbiAttribute"%>
<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>

<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page
	import="it.eng.spagobi.commons.bo.Domain,
				 it.eng.spagobi.tools.datasource.bo.*,
				 java.util.ArrayList,
				 java.util.List,
				 org.json.JSONArray"%>
<%@page import="it.eng.spagobi.tools.udp.bo.Udp"%>
<%@page import="it.eng.spagobi.commons.serializer.UdpJSONSerializer"%>
<%@page import="org.json.JSONObject"%>
<%@page import="it.eng.spagobi.engines.drivers.qbe.QbeDriver"%>
<%@page import="it.eng.spagobi.engines.config.bo.Engine"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>

<%
    List dsTypesList = (List) aSessionContainer.getAttribute("dsTypesList");
    List catTypesCd = (List) aSessionContainer.getAttribute("catTypesList");
    List dataSourceList = (List) aSessionContainer.getAttribute("dataSourceList");
    List scriptLanguageList = (List) aSessionContainer.getAttribute("scriptLanguageList");
    List trasfTypesList = (List) aSessionContainer.getAttribute("trasfTypesList");
    List sbiAttrsList = (List) aSessionContainer.getAttribute("sbiAttrsList");
    
    List scopeCdList = (List) aSessionContainer.getAttribute("scopeCdList");
    String[] fileNamesList = ( String[]) aSessionContainer.getAttribute("fileNames");
%>

<%@include file="/WEB-INF/jsp/commons/includePrettyCron.jspf" %>

<script type="text/javascript"
	src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/service/ServiceRegistry.js")%>'></script>

<script type="text/javascript">

	<%	
	JSONArray empty = new JSONArray();
	empty.put("");
	
	JSONArray fileNamesArray = new JSONArray();
	if(fileNamesList != null){
		for(int i=0; i< fileNamesList.length ; i++){
			String filename = fileNamesList[i];
			JSONArray temp = new JSONArray();
			temp.put(filename);
			fileNamesArray.put(temp);
		}
	}	
	String fileNames = fileNamesArray.toString();
	fileNames = fileNames.replaceAll("\"","'");
	
	JSONArray dsTypesArray = new JSONArray();
	if(dsTypesList != null){
		for(int i=0; i< dsTypesList.size(); i++){
			Domain domain = (Domain)dsTypesList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			dsTypesArray.put(temp);
		}
	}	
	String dsTypes = dsTypesArray.toString();
	dsTypes = dsTypes.replaceAll("\"","'");
	
	JSONArray profAttrsArray = new JSONArray();
	if(sbiAttrsList != null){
		for(int i=0; i< sbiAttrsList.size(); i++){
			SbiAttribute attr = (SbiAttribute)sbiAttrsList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(attr.getAttributeName());
			profAttrsArray.put(temp);
		}
	}	
	String attrs = profAttrsArray.toString();
	attrs = attrs.replaceAll("\"","'");
	
	JSONArray catTypesArray = new JSONArray();
	catTypesArray.put(empty);
	if(catTypesCd != null){
		for(int i=0; i< catTypesCd.size(); i++){
			Domain domain = (Domain)catTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueName());
			catTypesArray.put(temp);
		}
	}	
	String catTypes = catTypesArray.toString();
	catTypes = catTypes.replaceAll("\"","'");
	///------scope USER, ENTERPRISE, TECHNICAL
	JSONArray scopeCdArray = new JSONArray();
	//scopeCdArray.put(empty);
	if(scopeCdList != null){
		for(int i=0; i< scopeCdList.size(); i++){
			Domain domain = (Domain)scopeCdList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueName());
			scopeCdArray.put(temp);
		}
	}	
	String scopeCd = scopeCdArray.toString();
	scopeCd = scopeCd.replaceAll("\"","'");
	///-----------END
	JSONArray dataSourcesArray = new JSONArray();
	if(dataSourceList != null){
		for(int i=0; i< dataSourceList.size(); i++){
			DataSource datasource = (DataSource)dataSourceList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(datasource.getLabel());
			temp.put(datasource.getDescr());
			dataSourcesArray.put(temp);
		}
	}	
	String dataSourceLabels = dataSourcesArray.toString();
	dataSourceLabels = dataSourceLabels.replaceAll("\"","'");
	
	JSONArray scriptLanguagesArray = new JSONArray();
	if(scriptLanguageList != null){
		for(int i=0; i< scriptLanguageList.size(); i++){
			Domain domain = (Domain)scriptLanguageList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			temp.put(domain.getValueName());
			scriptLanguagesArray.put(temp);
		}
	}	
	String scriptTypes = scriptLanguagesArray.toString();
	scriptTypes = scriptTypes.replaceAll("\"","'");
	
	JSONArray trasfTypesArray = new JSONArray();
	//trasfTypesArray.put(empty);
	JSONArray emptyValue = new JSONArray();
	emptyValue.put("&nbsp;");
	trasfTypesArray.put(emptyValue);
	if(trasfTypesList != null){
		for(int i=0; i< trasfTypesList.size(); i++){
			Domain domain = (Domain)trasfTypesList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			trasfTypesArray.put(temp);
		}
	}	
	String trasfTypes = trasfTypesArray.toString();
	trasfTypes = trasfTypes.replaceAll("\"","'");
	
	String qbeEngineBaseUrl = null;
	StringBuffer qbeEngineBuildDatasetUrl = new StringBuffer();
	StringBuffer qbeEngineGetDatamartsUrl = new StringBuffer();
	List engines = DAOFactory.getEngineDAO().loadAllEngines();
	Iterator it = engines.iterator();
	while (it.hasNext()) {
		Engine engine = (Engine) it.next();
		String driver = engine.getDriverName();
		if (driver != null && driver.equals(QbeDriver.class.getName())) {
			qbeEngineBaseUrl = engine.getUrl();
			qbeEngineBuildDatasetUrl.append(qbeEngineBaseUrl);
			qbeEngineGetDatamartsUrl.append(qbeEngineBaseUrl);
			break;
		}
	}
	qbeEngineBuildDatasetUrl.append("?ACTION_NAME=BUILD_QBE_DATASET_START_ACTION");
	qbeEngineGetDatamartsUrl.append("?ACTION_NAME=GET_DATAMARTS_NAMES");
	if (!GeneralUtilities.isSSOEnabled()) {
		qbeEngineBuildDatasetUrl.append("&" + SsoServiceInterface.USER_ID + "=" + userUniqueIdentifier);
		qbeEngineGetDatamartsUrl.append("&" + SsoServiceInterface.USER_ID + "=" + userUniqueIdentifier);
	}
	qbeEngineBuildDatasetUrl.append("&" + QbeDriver.PARAM_NEW_SESSION + "=TRUE");
	qbeEngineGetDatamartsUrl.append("&" + QbeDriver.PARAM_NEW_SESSION + "=TRUE");
	qbeEngineBuildDatasetUrl.append("&" + SpagoBIConstants.SBI_LANGUAGE + "=" + curr_language);
	qbeEngineBuildDatasetUrl.append("&" + SpagoBIConstants.SBI_COUNTRY + "=" + curr_country);
    %>

    var config = {};  
    config.dsTypes = <%= dsTypes%>;
    config.catTypeVn = <%= catTypes%>;
    config.dataSourceLabels = <%= dataSourceLabels%>;
    config.scriptTypes = <%= scriptTypes%>;
    config.trasfTypes = <%= trasfTypes%>;    
    config.attrs = <%= attrs%>;  
    config.fileTypes = <%= fileNames%>;  
    config.scopeCd = <%= scopeCd%>;  
	
	var url = {
    	host: '<%= request.getServerName()%>'
    	, port: '<%= request.getServerPort()%>'
    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
    	   				  request.getContextPath().substring(1):
    	   				  request.getContextPath()%>'
    	    
    };

    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
    	baseUrl: url
    });
    
    Sbi.config.qbeDatasetBuildUrl = '<%= StringEscapeUtils.escapeJavaScript(qbeEngineBuildDatasetUrl.toString()) %>';
    Sbi.config.qbeGetDatamartsUrl = '<%= StringEscapeUtils.escapeJavaScript(qbeEngineGetDatamartsUrl.toString()) %>';

    // for DataStorePanel.js
    Sbi.config.queryLimit = {};
    Sbi.config.queryLimit.maxRecords = <%= GeneralUtilities.getDatasetMaxResults() %>;
    
	Ext.onReady(function(){
		Ext.QuickTips.init();
		var manageDatasets = new Sbi.tools.dataset.DatasetManagementPanel(config);
		var viewport = new Ext.Viewport({
			layout: 'border'
			, items: [
			    {
			       region: 'center',
			       layout: 'fit',
			       items: [manageDatasets]
			    }
			]
	
		});
	   	
	});


</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
