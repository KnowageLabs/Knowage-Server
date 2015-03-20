<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject,
				 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter,				 
				 it.eng.spagobi.commons.dao.DAOFactory,			
				 java.util.List,			 
				 it.eng.spagobi.commons.bo.Domain,
				 java.util.Iterator,
				 it.eng.spagobi.engines.config.bo.Engine,			
				 it.eng.spago.base.SourceBean,	
				 it.eng.spagobi.monitoring.metadata.SbiAudit,		
				 java.util.Date"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="it.eng.spagobi.tools.datasource.bo.DataSource"%>
<%@page import="it.eng.spagobi.monitoring.dao.AuditManager"%>
<%@page import="it.eng.spagobi.kpi.threshold.bo.Threshold"%>
<%@page import="it.eng.spagobi.kpi.threshold.bo.ThresholdValue"%>
<%@page import="java.awt.Color"%>
 

<%
	// GET RESPONSE OBJECTS
	
	String kpiBeginDate = (String)aServiceResponse.getAttribute("KPI_BEGIN_DATE");
	String kpiEndDate = (String)aServiceResponse.getAttribute("KPI_END_DATE");
	if (kpiEndDate.contains("9999")) kpiEndDate = "";
	String kpiTarget = (String)aServiceResponse.getAttribute("KPI_TARGET");
	String kpiValueDescr = (String)aServiceResponse.getAttribute("KPI_VALUE_DESCR");	
	String kpiCode = (String)aServiceResponse.getAttribute("KPI_CODE");
	String kpiDescription = (String)aServiceResponse.getAttribute("KPI_DESCRIPTION");
	String kpiName = (String)aServiceResponse.getAttribute("KPI_NAME");
	String modelInstName = (String)aServiceResponse.getAttribute("MODEL_INST_NAME");
	String modelInstanceDescr = (String)aServiceResponse.getAttribute("MODEL_INST_DESCR");
	String kpiInterpretation = (String)aServiceResponse.getAttribute("KPI_INTERPRETATION");
	String kpiValue = (String)aServiceResponse.getAttribute("KPI_VALUE");
	String kpiWeight = (String)aServiceResponse.getAttribute("KPI_WEIGHT");
	String threshName = (String)aServiceResponse.getAttribute("THRESHOLD_NAME");
	String weightedValue = (String)aServiceResponse.getAttribute("WEIGHTED_VALUE");
	
	String kpiWeightedValue = "";
	if (kpiValue!=null && !kpiValue.equals("") && kpiWeight!=null && !kpiWeight.equals("")){
			Double val = new Double(kpiValue);
			Double weight =  new Double(kpiWeight);
			if(weightedValue!=null && weightedValue.equals("true")){
				kpiWeightedValue=new Float(val*weight).toString();
				kpiValue=new Float(val).toString();
			}else{
				kpiWeightedValue =new Float(val*weight).toString();
			}
	}

%>


<script>
Ext.onReady(function(){
    var p = new Ext.Panel({
        title: '<spagobi:message key = "metadata.kpiOverview" />',
        collapsible:true,
        collapsed : false,
        renderTo: 'container1',
        contentEl : 'generalData'
    });
});
</script>
<script>
Ext.onReady(function(){
    var p = new Ext.Panel({
        title: '<spagobi:message key = "metadata.kpiDescription" />',
        collapsible:true,
        collapsed : true,
        renderTo: 'container2',
        contentEl : 'description'
    });
});
</script>
<script>
Ext.onReady(function(){
    var p = new Ext.Panel({
        title: '<spagobi:message key = "metadata.kpiInterpretation" />',
        collapsible:true,
        collapsed : true,
        renderTo: 'container3',
        contentEl : 'interpretation'
    });
});
</script>
<script>
Ext.onReady(function(){
    var p = new Ext.Panel({
        title: '<spagobi:message key = "metadata.modelInstance" />',
        collapsible:true,
        collapsed : true,
        renderTo: 'container4',
        contentEl : 'modelInstance'
    });
});
</script>
<script>
Ext.onReady(function(){
    var p = new Ext.Panel({
        title: '<spagobi:message key = "metadata.tecData" />',
        collapsible:true,
        collapsed : true,
        renderTo: 'container5',
        contentEl : 'technicalData'
    });
});
</script>

		<div id="container1"> </div>	
		<div id="container2"> </div>
		<div id="container3"> </div>	
		<div id="container4"> </div>	
		<div id="container5"> </div>	
			
<div id="generalData" class="div_background_no_img" style="padding-top:5px;padding-left:5px;">
	
	<!-- TABLE GENERAL DATA -->		
	
	<table style="width:100%;margin-top:1px" >
		<!-- KPI NAME -->
		<tr>
		    <td class="portlet-section-header" width="140" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiName" />		
			</td>				
			<td class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiName%>
			</td>
		</tr>	
		
		<!-- KPI CODE -->
		<tr>
		    <td class="portlet-section-header" width="140" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiCode" />		
			</td>				
			<td class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiCode%>
			</td>
		</tr>
		
		<spagobi:error/>
	</table> 
</div>		
<div id="description" class="div_background_no_img" style="padding-top:5px;padding-left:5px;">
	
	<!-- KPI INTERPRETATION -->		
	
	<table style="width:100%;margin-top:1px" >
		<!-- DOC NAME -->
		<tr>			
			<td class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiDescription%>
			</td>
		</tr>	
		<spagobi:error/>
	</table> 
</div>	

<div id="interpretation" class="div_background_no_img" style="padding-top:5px;padding-left:5px;">
	
	<!-- KPI INTERPRETATION -->		
	
	<table style="width:100%;margin-top:1px" >
		<!-- DOC NAME -->
		<tr>			
			<td class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiInterpretation%>
			</td>
		</tr>	
		<spagobi:error/>
	</table> 
</div>

<div id="modelInstance" class="div_background_no_img" style="padding-top:5px;padding-left:5px;">
	
	<!-- TABLE GENERAL DATA -->		
	
	<table style="width:100%;margin-top:1px" >
		<!-- MODEL INSTANCE NAME -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.modelInstanceName" />		
			</td>				
			<td class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=modelInstName%>
			</td>
		</tr>	
		
		<!-- MODEL INSTANCE DESCR -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.modelInstanceDescr" />		
			</td>				
			<td class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=modelInstanceDescr%>
			</td>
		</tr>
		
		<spagobi:error/>
	</table> 
</div>


<div id="technicalData" class="div_background_no_img" style="padding-top:5px;padding-left:5px;">	
	<!-- TABLE TECHNICAL DATA -->		
	<table style="width:100%;margin-top:1px">
		<!-- KPI BEGIN DATE -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiBegDate" />		
			</td>				
			<td colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiBeginDate%>
			</td>
		</tr>	
		
		<!-- KPI END DATE -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiEndDate" />		
			</td>				
			<td  colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiEndDate%>
			</td>
		</tr>
		
		<!-- KPI Value -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiValue" />		
			</td>				
			<td colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiValue%>
			</td>
		</tr>
		
		<!-- KPI Weighted Value -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiWeight" />		
			</td>				
			<td colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiWeight%>
			</td>
		</tr>
		
		<!-- KPI Weight -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiWeightedValue" />		
			</td>				
			<td colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiWeightedValue%>
			</td>
		</tr>
		
		
		<!-- KPI VALUE DESCR -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiValueDesc" />		
			</td>				
			<td colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiValueDescr%>
			</td>
		</tr>
		
		<!-- KPI TARGET -->
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiValueTarget" />		
			</td>				
			<td colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=kpiTarget%>
			</td>
		</tr>	
		
		<tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><spagobi:message key = "sbi.kpi.kpiThresholdName" />		
			</td>				
			<td colspan="2" class="portlet-section-body" style="vertical-align:left;text-align:left;">&nbsp;<%=threshName%>
			</td>
		</tr>	
		
		<% 
		List thresholdValues = (List)aServiceResponse.getAttribute("KPI_THRESHOLDS");
		if (thresholdValues!=null && !thresholdValues.isEmpty()){
			Iterator it = thresholdValues.iterator();
			
			while(it.hasNext()){
				ThresholdValue t =(ThresholdValue)it.next();
				
				Color c =t.getColor();
				String color = "rgb("+c.getRed()+", "+c.getGreen()+", "+c.getBlue()+")" ;
				Double min = t.getMinValue();
				Double max = t.getMaxValue();
				String type = t.getThresholdType();
				String minMax = "";
				
				if (min!=null && max !=null){
				  minMax = min.toString()+"-"+max.toString();
				}else if (min!=null && max==null){
					if(type.equals("RANGE")){
						minMax = "> "+min.toString();
					}else{
						minMax = min.toString();
					}
				}else if (min==null && max!=null){
					if(type.equals("RANGE")){
						minMax = "< "+max.toString();
					}else{
						minMax = max.toString();
					}
				}
				String label = "Threshold " + type+ " "+ t.getLabel();
				%>
		<!-- THRESHOLDS -->
		 <tr>
		    <td class="portlet-section-header" width="170" style="text-align:left;color:black;background-color:#DCDCDC;"><%=label%>	
			</td>				
			<td class="portlet-section-body" width="15" style="vertical-align:left;text-align:left;background-color:<%=color%>;">
			</td>
			<td class="portlet-section-body" style="vertical-align:left;text-align:left;" >&nbsp;<%=minMax%>
			</td>
		 </tr>
				<%
			}
		} 
	  %>
		
																										
	
	<spagobi:error/>
	</table> 
</div>  

	
	

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
