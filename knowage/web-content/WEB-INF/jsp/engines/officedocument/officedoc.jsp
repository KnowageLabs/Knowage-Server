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


<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.monitoring.dao.AuditManager"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance"%>
<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Locale"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@include file="/WEB-INF/jsp/commons/includeMessageResource.jspf" %>

<% 
	SourceBean sbModuleResponse = (SourceBean) aServiceResponse.getAttribute("ExecuteBIObjectModule");
	Boolean isImage = (sbModuleResponse.getAttribute("isImage") != null )? 
			(Boolean)sbModuleResponse.getAttribute("isImage"):false; 
	ExecutionInstance instanceO = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
	String execContext = instanceO.getExecutionModality();

	Integer executionAuditId_office = null;
	String spagobiContext = request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
				 			 	request.getContextPath().substring(1):request.getContextPath();
	// identity string for object of the page
	String strUuid = instanceO.getExecutionId();
	BIObject biObj = instanceO.getBIObject();
	
	AuditManager auditManager = AuditManager.getInstance();
	String modality = instanceO.getExecutionModality();
	String executionRole = instanceO.getExecutionRole();
	executionAuditId_office = auditManager.insertAudit(biObj, null, userProfile, executionRole, modality);
	
	//get the url for document retrieval
	String officeDocUrl = GeneralUtilities.getSpagoBIProfileBaseUrl(userUniqueIdentifier);
	officeDocUrl += "&ACTION_NAME=GET_OFFICE_DOC&documentId=" + biObj.getId().toString() + "&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";
	// adding parameters for AUDIT updating
	if (executionAuditId_office != null) {
		officeDocUrl += "&" + AuditManager.AUDIT_ID + "=" + executionAuditId_office.toString();
	}
	if (!isImage){
		response.sendRedirect(officeDocUrl);
	}else{
    %>
	<%-- ---------------------------------------------------------------------- --%>
	<%-- HTML CODE FOR ZOOM FUNCTIONALITY										--%>
	<%-- ---------------------------------------------------------------------- --%>
	<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/lib/ext-3.1.1/ux/imageeditor/ImageEditor.js")%>'></script>
	<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/lib/ext-3.1.1/ux/imageeditor/PanPanel.js")%>'></script>
	<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/formats/"+ locale.getLanguage() +".js")%>'></script>
	<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/formats/LocaleUtils.js")%>'></script> 
	<script type="text/javascript">
		Ext.onReady(function() { 
			Ext.QuickTips.init();		
			
			var image = document.createElement('img');
            image.src = '<%=officeDocUrl%>';
            var editor = new Ext.ux.ImageEditor();
            var p = new Ext.ux.PanPanel({
                frame: true,
                border: false,
                client: image,  		
		  		autoHeight: true,
		  		autoWidth: true,
                floating: true,
                plugins: [editor],
                autoScroll: true,
                x: 5, y: 5,
                renderTo: Ext.getBody(),
                listeners: {
                    render: function(p) {
                        new Ext.Resizable(p.getEl(), {
                            handles: 'all',
                            pinned: true,
                            transparent: true,
                            resizeElement: function() {
                                var box = this.proxy.getBox();
                                p.updateBox(box);
                                if (p.layout) {
                                    p.doLayout();
                                }
                                return box;
                            }
                        });
                    }
                },
                tbar: []
            });
            p.show();
		});
		
	</script>
<%} %>
