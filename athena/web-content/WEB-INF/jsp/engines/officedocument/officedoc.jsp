<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

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
