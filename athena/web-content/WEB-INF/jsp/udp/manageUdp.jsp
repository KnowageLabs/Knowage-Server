<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>
<%@ page import="it.eng.spagobi.commons.bo.Domain,
				 java.util.ArrayList,
				 java.util.List,
				 org.json.JSONArray" %>
<%

	List typesList = (List) aSessionContainer.getAttribute("TYPE_LIST");
	List familiesList = (List) aSessionContainer.getAttribute("FAMILY_LIST");

%>

<script type="text/javascript">
<%
	JSONArray typesArray = new JSONArray();
	if(typesList != null){
		for(int i=0; i< typesList.size(); i++){
			Domain domain = (Domain)typesList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			typesArray.put(temp);
		}
	}	
	String types = typesArray.toString();
	types = types.replaceAll("\"","'");	
	
	JSONArray familyArray = new JSONArray();
	if(familiesList != null){
		for(int i=0; i< familiesList.size(); i++){
			Domain domain = (Domain)familiesList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			familyArray.put(temp);
		}
	}	
	String families = familyArray.toString();
	families = families.replaceAll("\"","'");
%>

	var config = {};
	
	config.types = <%= types%>;
	config.families = <%= families%>;

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
	var manageUdp = new Sbi.udp.ManageUdp(config);
	var viewport = new Ext.Viewport({
		layout: 'border'
		, items: [
		    {
		       region: 'center',
		       layout: 'fit',
		       items: [manageUdp]
		    }
		]

	});
   	
	});
</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>