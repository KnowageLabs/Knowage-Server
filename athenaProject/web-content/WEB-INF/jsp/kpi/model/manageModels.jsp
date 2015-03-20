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
				 it.eng.spagobi.tools.udp.bo.Udp" %>
<%

	List nodeTypesCd = (List) aSessionContainer.getAttribute("nodeTypesList");
    List thrSeverityTypesCd = (List) aSessionContainer.getAttribute("thrSeverityTypes");
	List thrTypesCd = (List) aSessionContainer.getAttribute("thrTypesList");
	List kpiTypesCd = (List) aSessionContainer.getAttribute("kpiTypesList");
	List measureTypesCd = (List) aSessionContainer.getAttribute("measureTypesList");
	List metricScaleTypesCd = (List) aSessionContainer.getAttribute("metricScaleTypesList");
	List udpModelListCd = (List) aSessionContainer.getAttribute("udpModelList");
	List udpKpiListCd = (List) aSessionContainer.getAttribute("udpKpiList");

%>

<LINK rel='StyleSheet' 
      href='<%=urlBuilder.getResourceLinkByTheme(request, "css/kpi/kpi.css",currTheme)%>' 
      type='text/css' />
      
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/service/ServiceRegistry.js")%>'></script>

<script type="text/javascript">
	<%	
	JSONArray thrTypesArray = new JSONArray();
	if(thrTypesCd != null){
		for(int i=0; i< thrTypesCd.size(); i++){
			Domain domain = (Domain)thrTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			thrTypesArray.put(temp);
		}
	}	
	String thrTypes = thrTypesArray.toString();
	thrTypes = thrTypes.replaceAll("\"","'");
	
	JSONArray severityTypesArray = new JSONArray();
	if(thrSeverityTypesCd != null){
		for(int i=0; i< thrSeverityTypesCd.size(); i++){
			Domain domain = (Domain)thrSeverityTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			severityTypesArray.put(temp);
		}
	}	
	String severityTypes = severityTypesArray.toString();
	severityTypes = severityTypes.replaceAll("\"","'");	
	
	JSONArray kpiTypesArray = new JSONArray();
	if(kpiTypesCd != null){
		for(int i=0; i< kpiTypesCd.size(); i++){
			Domain domain = (Domain)kpiTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			kpiTypesArray.put(temp);
		}
	}	
	String kpiTypes = kpiTypesArray.toString();
	kpiTypes = kpiTypes.replaceAll("\"","'");
	
	JSONArray measureTypesArray = new JSONArray();
	if(measureTypesCd != null){
		for(int i=0; i< measureTypesCd.size(); i++){
			Domain domain = (Domain)measureTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			measureTypesArray.put(temp);
		}
	}	
	String measureTypes = measureTypesArray.toString();
	measureTypes = measureTypes.replaceAll("\"","'");
	
	JSONArray metricScaleTypesArray = new JSONArray();
	if(metricScaleTypesCd != null){
		for(int i=0; i< metricScaleTypesCd.size(); i++){
			Domain domain = (Domain)metricScaleTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			metricScaleTypesArray.put(temp);
		}
	}	
	String metricScalesTypes = metricScaleTypesArray.toString();
	metricScalesTypes = metricScalesTypes.replaceAll("\"","'");	
	
	
	JSONArray nodeTypesArray = new JSONArray();
	if(nodeTypesCd != null){
		
		for(int i=0; i< nodeTypesCd.size(); i++){
			Domain domain = (Domain)nodeTypesCd.get(i);	
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueId());
			temp.put(domain.getValueCd());
			temp.put(domain.getValueDescription());
			temp.put(domain.getDomainCode());
			nodeTypesArray.put(temp);
		}
	}	
	String nodeTypes = nodeTypesArray.toString();
	nodeTypes = nodeTypes.replaceAll("\"","'");
	
	// create jason arrays for udp attributes of the model
	// this is empty list label + empty value to fill firstly the tab
	String udpModelEmptyListJSON ="{}";
	if(udpModelListCd != null){
		udpModelEmptyListJSON="[";
		for(int i=0; i< udpModelListCd.size(); i++){
			Udp udp = (Udp)udpModelListCd.get(i);
			udpModelEmptyListJSON+="{";
			udpModelEmptyListJSON+="'label':'"+udp.getLabel()+"',";
			udpModelEmptyListJSON+="'value':''";
			udpModelEmptyListJSON+="}";
			if(i != (udpModelListCd.size()-1)){
				udpModelEmptyListJSON+=",";
			}
		}
		udpModelEmptyListJSON+="]";
	}
	// this is the list of udps carrying all udp nformations
	String udpModelListJSON ="{}";
	if(udpModelListCd != null){
		udpModelListJSON="[";
		for(int i=0; i< udpModelListCd.size(); i++){
			Udp udp = (Udp)udpModelListCd.get(i);
			udpModelListJSON+="{";
			udpModelListJSON+="'udpId':"+udp.getUdpId()+",";
			udpModelListJSON+="'label':'"+udp.getLabel()+"',";
			udpModelListJSON+="'name':'"+udp.getName()+"',";
			udpModelListJSON+="'description':'"+udp.getDescription()+"',";
			udpModelListJSON+="'dataTypeId':'"+udp.getDataTypeId()+"',";
			udpModelListJSON+="'familyId':'"+udp.getFamilyId()+"',";
			udpModelListJSON+="'multivalue':'"+udp.getMultivalue()+"',";
			udpModelListJSON+="'dataTypeCd':'"+udp.getDataTypeValeCd()+"'";

			udpModelListJSON+="}";
			if(i != (udpModelListCd.size()-1)){
				udpModelListJSON+=",";
			}
		}
		udpModelListJSON+="]";
	}    

	// create jason arrays for udp attributes of the kpis
	// this is empty list label + empty value to fill firstly the tab
	String udpKpiEmptyListJSON ="{}";
	if(udpKpiListCd != null){
		udpKpiEmptyListJSON="[";
		for(int i=0; i< udpKpiListCd.size(); i++){
			Udp udp = (Udp)udpKpiListCd.get(i);
			udpKpiEmptyListJSON+="{";
			udpKpiEmptyListJSON+="'label':'"+udp.getLabel()+"',";
			udpKpiEmptyListJSON+="'value':''";
			udpKpiEmptyListJSON+="}";
			if(i != (udpKpiListCd.size()-1)){
				udpKpiEmptyListJSON+=",";
			}
		}
		udpKpiEmptyListJSON+="]";
	}
	// this is the list of udps carrying all udp nformations
	String udpKpiListJSON ="{}";
	if(udpKpiListCd != null){
		udpKpiListJSON="[";
		for(int i=0; i< udpKpiListCd.size(); i++){
			Udp udp = (Udp)udpKpiListCd.get(i);
			udpKpiListJSON+="{";
			udpKpiListJSON+="'udpId':"+udp.getUdpId()+",";
			udpKpiListJSON+="'label':'"+udp.getLabel()+"',";
			udpKpiListJSON+="'name':'"+udp.getName()+"',";
			udpKpiListJSON+="'description':'"+udp.getDescription()+"',";
			udpKpiListJSON+="'dataTypeId':'"+udp.getDataTypeId()+"',";
			udpKpiListJSON+="'familyId':'"+udp.getFamilyId()+"',";
			udpKpiListJSON+="'multivalue':'"+udp.getMultivalue()+"',";
			udpKpiListJSON+="'dataTypeCd':'"+udp.getDataTypeValeCd()+"'";

			udpKpiListJSON+="}";
			if(i != (udpKpiListCd.size()-1)){
				udpKpiListJSON+=",";
			}
		}
		udpKpiListJSON+="]";
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
	config.kpiTypesCd = <%= kpiTypes%>;
	config.thrSeverityTypesCd = <%= severityTypes%>;
	config.measureTypesCd = <%= measureTypes%>;
	config.metricScaleTypesCd = <%= metricScalesTypes%>;
	config.thrTypes = <%= thrTypes%>;
    config.nodeTypesCd = <%= nodeTypes%>;
    config.udpModelEmptyListJSON = <%= udpModelEmptyListJSON%>;
	config.udpModelListCdt = <%= udpModelListJSON%>;
	config.udpKpiEmptyListJSON = <%= udpKpiEmptyListJSON%>;
	config.udpKpiListCdt = <%= udpKpiListJSON%>;
    
	Ext.onReady(function(){
		Ext.QuickTips.init();
		var manageModelsViewPort = new Sbi.kpi.ManageModelsViewPort(config);
	   	
	});


</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>