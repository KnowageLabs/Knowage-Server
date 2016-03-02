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


<%@page import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetStartAction,
			it.eng.spago.error.EMFErrorHandler,
		    it.eng.spago.error.EMFAbstractError,
		    java.util.HashMap,
		    java.util.Set,
		    java.util.Iterator"%>
<%@page import="java.util.Collection"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/main.css", currTheme)%>'/>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/listview.css", currTheme)%>'/>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/catalogue-item-small.css",currTheme)%>'/>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/browser/standard.css",currTheme)%>'/>


<%
    String executionId = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_EXECUTION_ID);
    String worksheetEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL);
    String qbeEditFromBMActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_QBE_EDIT_FROM_BM_SERVICE_URL);
    String qbeEditFromDataSetActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_QBE_EDIT_FROM_DATA_SET_SERVICE_URL);
    String georeportEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL);
    String cockpitEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_COCKPIT_EDIT_SERVICE_URL);
    String fromMyAnalysis = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_MYANALYSIS);
	String isWorksheetEnabled = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_WORKSHEET_ENABLED);
    String contextName = ChannelUtilities.getSpagoBIContextName(request);
    
    boolean checkCache = false;
    EMFErrorHandler errorHandler = aResponseContainer.getErrorHandler();  
    Collection errors = errorHandler.getErrors();
    Iterator iter = errors.iterator();  
  	if (iter != null && iter.hasNext()){
	    EMFAbstractError abErr = (EMFAbstractError)iter.next();	   
	    if (abErr.getDescription() != null){
	  	  checkCache = true;
	    }
  	}
%>

<script type="text/javascript">

<% if (checkCache){ %>
debugger alert(LN('sbi.myanalysis.noCorrectSettingsForCache'));
<% }else{ %>

    Ext.onReady(function(){
    	Sbi.settings.mydata.isWorksheetEnabled = <%= isWorksheetEnabled %>;
		var selfService = Ext.create('Sbi.adhocreporting.AdhocreportingContainer',{
        	worksheetEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(worksheetEditActionUrl) %>'
            , qbeFromBMBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditFromBMActionUrl) %>'
            , qbeFromDataSetBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditFromDataSetActionUrl) %>'
            , user: Sbi.user.userUniqueIdentifier
            , georeportEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(georeportEditActionUrl) %>'
            , cockpitEngineBaseUrl: '<%= StringEscapeUtils.escapeJavaScript(cockpitEditActionUrl) %>'
            , myAnalysisServicePath: 'documents/myAnalysisDocsList'
            , fromMyAnalysis: '<%=fromMyAnalysis%>'
            , contextName: '<%= StringEscapeUtils.escapeJavaScript(contextName) %>'
		}); //by alias
		
		var datasetListViewport = Ext.create('Ext.container.Viewport', {
			layout:'fit',
	     	items: [selfService]	     	
	    });
    });
<%}%>

</script>
 

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
