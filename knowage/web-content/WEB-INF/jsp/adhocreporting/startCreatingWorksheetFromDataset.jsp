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


<%@page language="java" 
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="it.eng.spagobi.analiticalmodel.execution.service.CreateDatasetForWorksheetAction"%>
<%@page import="it.eng.spagobi.adhocreporting.services.StartCreatingWorksheetFromDatasetAction"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>

<%
    String executionId = (String) aResponseContainer.getServiceResponse().getAttribute(CreateDatasetForWorksheetAction.OUTPUT_PARAMETER_EXECUTION_ID);
    String worksheetEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(CreateDatasetForWorksheetAction.OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL);
    String qbeEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(StartCreatingWorksheetFromDatasetAction.OUTPUT_PARAMETER_QBE_EDIT_SERVICE_URL);
%>


<script type="text/javascript">

    var url = {
            host: '<%= request.getServerName()%>'
            , port: '<%= request.getServerPort()%>'
            , contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
                              request.getContextPath().substring(1):
                              request.getContextPath()%>'
                
    };
    
    var params = {
        SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
        , LIGHT_NAVIGATOR_DISABLED: 'TRUE'
    };
    
    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
        baseUrl: url
        , baseParams: params
    });
    
    Ext.onReady(function(){
    	Ext.QuickTips.init();
    	
        var editorPanel = new Sbi.worksheet.WorksheetFromDatasetPanel({
        	worksheetEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(worksheetEditActionUrl) %>'
        	, qbeEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditActionUrl) %>'
        });
    
        
        var viewport = new Ext.Viewport({
            layout: 'border'
            , items: [
                {
                   region: 'center',
                   layout: 'fit',
                   items: [editorPanel]
                }
            ]
        });
            
    });

</script>
 

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
