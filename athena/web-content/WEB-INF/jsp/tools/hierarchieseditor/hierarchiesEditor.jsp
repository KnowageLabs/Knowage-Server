<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>

<%
	boolean isAdmin = UserUtilities.isAdministrator(userProfile);
%>



<script type="text/javascript">
	
Ext.onReady(function(){
	var hierarchyEditor = Ext.create('Sbi.tools.hierarchieseditor.HierarchiesEditorSplittedPanel',{
		isAdmin: '<%= StringEscapeUtils.escapeJavaScript(Boolean.toString(isAdmin)) %>'
	}); //by alias
	var hierarchyEditorViewport = Ext.create('Ext.container.Viewport', {
		layout:'fit',
     	items: [hierarchyEditor]
    });
});


</script>
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>