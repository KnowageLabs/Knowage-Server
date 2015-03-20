<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>
<%@ page import="it.eng.spagobi.commons.bo.Domain,
				 java.util.ArrayList,
				 java.util.List,
				 org.json.JSONArray, 
				 org.json.JSONObject,
				 it.eng.spagobi.kpi.config.bo.Periodicity" %>
<%

	List thrTypesCd = (List) aSessionContainer.getAttribute("thrTypesList");
	List kpiChartTypesCd = (List) aSessionContainer.getAttribute("kpiChartTypesList");	
	List udpListCd = (List) aSessionContainer.getAttribute("udpList");

%>


<%@page import="it.eng.spagobi.tools.udp.bo.Udp"%><LINK rel='StyleSheet' 
      href='<%=urlBuilder.getResourceLinkByTheme(request, "css/kpi/kpi.css",currTheme)%>' 
      type='text/css' />
      
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/service/ServiceRegistry.js")%>'></script>

<script type="text/javascript"><!--
	<%	
	JSONArray thrTypesArray = new JSONArray();
	if(thrTypesCd != null){
		for(int i=0; i< thrTypesCd.size(); i++){
			Domain domain = (Domain)thrTypesCd.get(i);
			JSONArray temp = new JSONArray();
			//temp.put(domain.getValueId());
			temp.put(domain.getValueCd());			
			thrTypesArray.put(temp);
		}
	}	
	String thrTypes = thrTypesArray.toString();
	thrTypes = thrTypes.replaceAll("\"","'");
	
	//chart types
	JSONArray kpiChartTypesArray = new JSONArray();
	if(kpiChartTypesCd != null){
		for(int i=0; i< kpiChartTypesCd.size(); i++){
			Domain domain = (Domain)kpiChartTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueId());
			temp.put(domain.getValueCd());			
			kpiChartTypesArray.put(temp);
		}
	}	
	String chartTypes = kpiChartTypesArray.toString();
	chartTypes = chartTypes.replaceAll("\"","'");

	
    // create jason arrays for udp attributes 
	// this is empty list label + empty value to fill firstly the tab
	String udpEmptyListJSON ="{}";
	if(udpListCd != null){
		udpEmptyListJSON="[";
		for(int i=0; i< udpListCd.size(); i++){
			Udp udp = (Udp)udpListCd.get(i);
			udpEmptyListJSON+="{";
			udpEmptyListJSON+="'label':'"+udp.getLabel()+"',";
			udpEmptyListJSON+="'value':''";
			udpEmptyListJSON+="}";
			if(i != (udpListCd.size()-1)){
				udpEmptyListJSON+=",";
			}
		}
		udpEmptyListJSON+="]";
	}
	// this is the list of udps carrying all udp nformations
	String udpListJSON ="{}";
	if(udpListCd != null){
		udpListJSON="[";
		for(int i=0; i< udpListCd.size(); i++){
			Udp udp = (Udp)udpListCd.get(i);
			udpListJSON+="{";
			udpListJSON+="'udpId':"+udp.getUdpId()+",";
			udpListJSON+="'label':'"+udp.getLabel()+"',";
			udpListJSON+="'name':'"+udp.getName()+"',";
			udpListJSON+="'description':'"+udp.getDescription()+"',";
			udpListJSON+="'dataTypeId':'"+udp.getDataTypeId()+"',";
			udpListJSON+="'familyId':'"+udp.getFamilyId()+"',";
			udpListJSON+="'multivalue':'"+udp.getMultivalue()+"',";
			udpListJSON+="'dataTypeCd':'"+udp.getDataTypeValeCd()+"'";

			udpListJSON+="}";
			if(i != (udpListCd.size()-1)){
				udpListJSON+=",";
			}
		}
			udpListJSON+="]";
	}    

	
	
	%>

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
    
	var config = {};  
	config.thrTypes = <%= thrTypes%>;
	config.kpiChartTypes = <%= chartTypes%>;
	config.udpEmptyList = <%= udpEmptyListJSON%>;
	config.udpList = <%= udpListJSON%>;
	
    
Ext.onReady(function(){
	Ext.QuickTips.init();
	var manageModelInstancesViewPort = new Sbi.kpi.ManageModelInstancesViewPort(config);
   	
});


--></script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>