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


<%@ page import="java.util.Map" %>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.jfree.chart.JFreeChart"%>
<%@page import="org.jfree.chart.ChartUtilities"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator"%>
<%@page import="org.jfree.chart.imagemap.StandardURLTagFragmentGenerator"%>
<%@page import="org.jfree.chart.ChartRenderingInfo"%>
<%@page import="org.jfree.data.general.Dataset"%>
<%@page import="org.safehaus.uuid.UUIDGenerator"%>
<%@page import="it.eng.spagobi.kpi.config.bo.*"%>
<%@page import="it.eng.spagobi.kpi.threshold.bo.*"%>
<%@page import="it.eng.spagobi.kpi.model.bo.*"%>
<%@page import="org.safehaus.uuid.UUID"%>
<%@page import="org.jfree.chart.entity.StandardEntityCollection"%>
<%@page import="it.eng.spago.error.EMFErrorHandler"%>
<%@page import="java.util.Vector"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="org.jfree.data.category.DefaultCategoryDataset"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spagobi.engines.kpi.utils.StyleLabel"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.ChartImpl"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.KpiResourceBlock"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.KpiLine"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.KpiLineVisibilityOptions"%>
<%@page import="java.util.ArrayList"%> 
<%@page import="it.eng.spagobi.commons.utilities.*"%> 
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="java.util.Date"%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.awt.Color"%>
<%@page import="java.io.IOException"%>
<%@page import="java.text.ParseException"%>

<%
	boolean docComposition=false;
    List resources = new ArrayList();
    
	SourceBean sbModuleResponse = (SourceBean) aServiceResponse.getAttribute("ExecuteBIObjectModule");
	Integer executionAuditId_chart = null;
 	EMFErrorHandler errorHandler=aResponseContainer.getErrorHandler();
	if(errorHandler.isOK()){    
		SessionContainer permSession = aSessionContainer.getPermanentContainer();

		if(userProfile==null){
			userProfile = (IEngUserProfile) permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			userId=(String)((UserProfile)userProfile).getUserId();
		}
	}
	String crossNavigationUrl = "";
	ExecutionInstance instanceO = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
	String execContext = instanceO.getExecutionModality();
   	// if in document composition case do not include header.jsp
    if (execContext == null || !execContext.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION)){%>
		<%@ include file="/WEB-INF/jsp/analiticalmodel/execution/header.jsp"%>
		<%
		 executionAuditId_chart = executionAuditId; 

		Map crossNavigationParameters = new HashMap();
		//crossNavigationParameters.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
		crossNavigationParameters.put(ObjectsTreeConstants.ACTION, SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);		
		crossNavigationParameters.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.EXEC_CROSS_NAVIGATION);
		crossNavigationParameters.put("EXECUTION_FLOW_ID", executionFlowId);
		crossNavigationParameters.put("SOURCE_EXECUTION_ID", uuid);
		crossNavigationParameters.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "TRUE");
		crossNavigationUrl = urlBuilder.getUrl(request, crossNavigationParameters);%>
		
		<form id="crossNavigationForm<%= uuid %>" method="post" action="<%= crossNavigationUrl %>" style="display:none;">
		    <input type="hidden" id="targetDocumentLabel<%= uuid %>" name="<%= ObjectsTreeConstants.OBJECT_LABEL %>" value="" />  
			<input type="hidden" id="targetDocumentParameters<%= uuid %>" name="<%= ObjectsTreeConstants.PARAMETERS %>" value="" />
		</form>
		
		<script>
			function execCrossNavigation(windowName, label, parameters) {
				var uuid = "<%=uuid%>";
				document.getElementById('targetDocumentLabel' + uuid).value = label;
				document.getElementById('targetDocumentParameters' + uuid).value = parameters;
				document.getElementById('crossNavigationForm' + uuid).submit();
			}
		</script>
		<%-- end cross navigation scripts --%>
		<%   
	}else // in document composition case doesn't call header so set Object and uuid
	{
   		docComposition=true;
	}
%>
