<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>
<%@ page import="it.eng.spagobi.commons.bo.Role,
				 it.eng.spagobi.profiling.bean.SbiAttribute,
				 java.util.ArrayList,
				 java.util.List" %>
<%
	List<SbiAttribute> attributes = (List<SbiAttribute>) aSessionContainer.getAttribute("attributesList");
	List<Role> roles = (List<Role>) aSessionContainer.getAttribute("rolesList");
%>
<script type="text/javascript">
	<%
	String attributesList ="{}";
	if(attributes != null){
		attributesList="[";
		for(int i=0; i< attributes.size(); i++){
			SbiAttribute attr = attributes.get(i);
			attributesList+="{";
			attributesList+="'id':"+attr.getAttributeId()+",";
			attributesList+="'name':'"+attr.getAttributeName()+"',";
			attributesList+="'value':''";
			attributesList+="}";
			if(i != (attributes.size()-1)){
				attributesList+=",";
			}
		}
		attributesList+="]";
	}
	
	String rolesList ="{}";
	if(roles != null){
		rolesList="[";
		for(int i=0; i< roles.size(); i++){
			Role role = roles.get(i);
			rolesList+="{";
			rolesList+="'id':"+role.getId()+",";
			rolesList+="'name':'"+role.getName()+"',";
			rolesList+="'description':'"+role.getDescription()+"',";
			rolesList+="'checked':false";
			rolesList+="}";
			if(i != (roles.size()-1)){
				rolesList+=",";
			}
		}
		rolesList+="]";
	}
	%>
	Sbi.config.passwordAbilitated = false;
	
	var config = {
				  attributesEmpyList:<%=attributesList%>,
				  rolesEmptyList:<%=rolesList%>
				  };
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
		var manageUsers = new Sbi.profiling.ManageUsers(config);
		
		var viewport = new Ext.Viewport({
			layout: 'border'
			, items: [
			    {
			       region: 'center',
			       layout: 'fit',
			       items: [manageUsers]
			    }
			]
	
		});
	   	
	});


</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>