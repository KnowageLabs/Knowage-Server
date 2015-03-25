<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>
<%@ page import="java.util.ArrayList,
				 java.util.List" %>
<%

	List resourcesList = (List) aSessionContainer.getAttribute("RESOURCES_LIST");

%>

<script type="text/javascript">
<%
String resourcesJSON ="{}";
if(resourcesList != null){
	resourcesJSON="[";
	for(int i=0; i< resourcesList.size(); i++){
		String res = (String)resourcesList.get(i);
		resourcesJSON+="['"+res+"']";
		if(i != (resourcesList.size()-1)){
			resourcesJSON+=",";
		}
	}
	resourcesJSON+="]";
}
%>
	var config=<%= resourcesJSON%>;
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
   Ext.onReady(function(){
	Ext.QuickTips.init();
	var manageContacts = new Sbi.alarm.ManageContacts(config);
	var viewport = new Ext.Viewport({
		layout: 'border'
		, items: [
		    {
		       region: 'center',
		       layout: 'fit',
		       items: [manageContacts]
		    }
		]

	});
   	
	});

</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>