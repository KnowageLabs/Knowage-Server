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
