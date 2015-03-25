<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>
<%@ page import="it.eng.spagobi.commons.bo.Domain,
				 java.util.ArrayList,
				 java.util.List" %>
<%

	List<Domain> roleTypesCd = (List<Domain>) aSessionContainer.getAttribute("roleTypes");

%>

<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/service/ServiceRegistry.js")%>'></script>

<script type="text/javascript">

	<%
	String types ="{}";
	if(roleTypesCd != null){
		types="[";
		for(int i=0; i< roleTypesCd.size(); i++){
			Domain domain = roleTypesCd.get(i);
			types+="{'typeCd': '"+domain.getValueCd()+"', 'valueNm': '"+domain.getValueName()+"'}";
			if(i != (roleTypesCd.size()-1)){
				types+=",";
			}
		}
		types+="]";
		
	}
	
	//getting security type: if it's internal (SpagoBI) active pwd management and checks
	SingletonConfig serverConfig = SingletonConfig.getInstance();
	String strInternalSecurity = serverConfig.getConfigValue("SPAGOBI.SECURITY.PORTAL-SECURITY-CLASS.className");
	boolean isInternalSecurity = (strInternalSecurity.indexOf("InternalSecurity")>0)?true:false;
	%>

	var config=<%= types%>;
	config.isInternalSecurity = <%=isInternalSecurity%>  
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
	var manageRoles = new Sbi.profiling.ManageRoles(config);
	var viewport = new Ext.Viewport({
		layout: 'border'
		, items: [
		    {
		       region: 'center',
		       layout: 'fit',
		       items: [manageRoles]
		    }
		]

	});
   	
});


</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>