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
<%@page import="it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact"%>
<%@page import="java.util.ArrayList,
				it.eng.spagobi.kpi.config.bo.KpiAlarmInstance,
				java.util.List" %>

<%
	List contacts = (List) aSessionContainer.getAttribute("contactsList");
	List kpis = (List) aSessionContainer.getAttribute("KPI_LIST");
	//List<ThresholdValue> tresholds = (List<ThresholdValue>) aResponseContainer.getAttribute("TRESHOLD_LIST");
%>

<script type="text/javascript">
<%		
	String contactsList ="{}";
	if(contacts != null){
		contactsList="[";
		for(int i=0; i< contacts.size(); i++){
			SbiAlarmContact contact = (SbiAlarmContact) contacts.get(i);
			contactsList+="{";
			contactsList+="'id':"+contact.getId()+",";
			contactsList+="'name':'"+contact.getName()+"',";
			contactsList+="'mobile':'"+contact.getMobile()+"',";
			contactsList+="'email':'"+contact.getEmail()+"',";
			contactsList+="'resources':'"+contact.getResources()+"'";
			contactsList+="}";
			if(i != (contacts.size()-1)){
				contactsList+=",";
			}
		}
		contactsList+="]";
	}
	
	String kpisEmptyList ="{}";
	if(kpis != null){
		kpisEmptyList="[";
		for(int i=0; i< kpis.size(); i++){
			KpiAlarmInstance kpiAlarm = (KpiAlarmInstance)kpis.get(i);
			kpisEmptyList+="{";
			kpisEmptyList+="'id':"+kpiAlarm.getKpiInstanceId()+",";
			kpisEmptyList+="'kpiModel':'"+kpiAlarm.getKpiModelName()+"',";
			kpisEmptyList+="'kpiName':'"+kpiAlarm.getKpiName()+"'";
			kpisEmptyList+="}";
			if(i != (kpis.size()-1)){
				kpisEmptyList+=",";
			}
		}
		kpisEmptyList+="]";
	}
	String tresholdsList ="{}";

	%>
	var config = {
				  contactsEmpyList:<%=contactsList%>
	  			  , kpisEmptyList:<%=kpisEmptyList%>
	  			  , tresholdsList:<%=tresholdsList%>
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
	var manageAlarms = new Sbi.alarms.ManageAlarms(config);
	var viewport = new Ext.Viewport({
		layout: 'border'
		, items: [
		    {
		       region: 'center',
		       layout: 'fit',
		       items: [manageAlarms]
		    }
		]

	});
   	
	});

</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
