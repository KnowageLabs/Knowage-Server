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
    List thrTypesCd = (List) aSessionContainer.getAttribute("thrTypesList");
    List thrSeverityTypesCd = (List) aSessionContainer.getAttribute("thrSeverityTypes");
	List kpiTypesCd = (List) aSessionContainer.getAttribute("kpiTypesList");
	List measureTypesCd = (List) aSessionContainer.getAttribute("measureTypesList");
	List udpListCd = (List) aSessionContainer.getAttribute("udpKpiList");

	List metricScaleTypesCd = (List) aSessionContainer.getAttribute("metricScaleTypesList");

%>


<%@page import="it.eng.spagobi.tools.udp.bo.Udp"%>
<%@page import="it.eng.spagobi.commons.serializer.UdpJSONSerializer"%>
<%@page import="org.json.JSONObject"%>

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
   
	
	
    // create jason array for udp attributes
	
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

    var config = {};  
	config.kpiTypesCd = <%= kpiTypes%>;
	config.thrSeverityTypesCd = <%= severityTypes%>;
	config.measureTypesCd = <%= measureTypes%>;
	config.metricScaleTypesCd = <%= metricScalesTypes%>;
	config.thrTypes = <%= thrTypes%>;
	config.udpEmptyList = <%= udpEmptyListJSON%>;
	config.udpList = <%= udpListJSON%>;

	
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
		var manageKpis = new Sbi.kpi.ManageKpis(config);
		var viewport = new Ext.Viewport({
			layout: 'border'
			, items: [
			    {
			       region: 'center',
			       layout: 'fit',
			       items: [manageKpis]
			    }
			]
	
		});
	   	
	});


</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
