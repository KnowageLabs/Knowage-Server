<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>

<%@page import="it.eng.spagobi.profiling.bean.SbiAttribute"%>
<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page import="it.eng.spagobi.commons.bo.Domain,
				 it.eng.spagobi.tools.datasource.bo.*,
				 java.util.ArrayList,
				 java.util.List,
				 java.util.Map,
				 org.json.JSONArray" %>
<%@page import="org.json.JSONObject"%>
<%@page import="it.eng.spagobi.engines.config.bo.Engine"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.tools.dataset.bo.IDataSet"%>
<%@page import="it.eng.spagobi.tools.dataset.constants.DataSetConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	
	String executionId = request.getParameter("SBI_EXECUTION_ID");
	if(executionId != null) {
		executionId = "'" + request.getParameter("SBI_EXECUTION_ID") + "'";
	} else {
		executionId = "null";
	}  
	
	SourceBean sbModuleResponse = (SourceBean) aServiceResponse.getAttribute("ExecuteBIObjectModule");
	
	//gets the metadata of dataset
	String dsId =  String.valueOf(sbModuleResponse.getAttribute(DataSetConstants.ID));
	String dsLabel =  String.valueOf(sbModuleResponse.getAttribute(DataSetConstants.LABEL));
	String dsTypeCd =  (String) sbModuleResponse.getAttribute(DataSetConstants.DS_TYPE_CD);
	JSONArray dsPars =  (JSONArray) sbModuleResponse.getAttribute(DataSetConstants.PARS);
	String dsTransformerType =  (String) sbModuleResponse.getAttribute(DataSetConstants.TRASFORMER_TYPE_CD);
	
	String divId = (executionId != null)?executionId:"highchartDiv";
	String divWidth = (String) sbModuleResponse.getAttribute("divWidth");
	String divHeight = (String) sbModuleResponse.getAttribute("divHeight");
	String theme = (String) sbModuleResponse.getAttribute("themeHighchart");
	Integer numCharts = (Integer) sbModuleResponse.getAttribute("numCharts");
	String subType = (String) sbModuleResponse.getAttribute("subType");
	String divHeightDetail = divHeight;
	String divHeightMaster = divHeightDetail;
	//only for master/detail chart sets the height of the master chart as 1/3 of the detail height
	if (subType != null && subType.equalsIgnoreCase("MasterDetail")) {		
		Integer tmpDetailHeight = 0;
		String postFix = "";
		if (divHeightDetail.indexOf("%")>=0){
			postFix = "%";
			tmpDetailHeight =Integer.valueOf(divHeightDetail.substring(0,divHeightDetail.indexOf("%")));
		}else if (divHeightMaster.indexOf("px")>=0){
			postFix = "px";
			tmpDetailHeight =Integer.valueOf(divHeightDetail.substring(0,divHeightDetail.indexOf("px")));
		}
		divHeightMaster = String.valueOf(Math.round(tmpDetailHeight/3)) + postFix;
	}
	 
	
	//gets the json template
	JSONObject template = (JSONObject)sbModuleResponse.getAttribute("template");
	String docLabel = (String)sbModuleResponse.getAttribute("documentLabel");
	String templateString = null;
	try{
		String s = template.toString();
		templateString = s;

	} catch (Exception e){
		
	}

%>


<%-- ---------------------------------------------------------------------- --%>
<%-- HTML CODE       														--%>
<%-- ---------------------------------------------------------------------- --%>

	<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/service/ServiceRegistry.js")%>'></script>
	<% if (theme != null && !theme.equals("") ) { %>
		<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/engines/chart/themes/"+theme+".js")%>'></script>
	<% }%>
	<!-- defines the export method only for the highcharts docs -->
	<script type="text/javascript">
		var chartPanel = {};
		var template =  <%= templateString%>;
		Sbi.config = {};

		var url = {
	    	host: '<%= request.getServerName()%>'
	    	, port: '<%= request.getServerPort()%>'
	    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
	    	   				  request.getContextPath().substring(1):
	    	   				  request.getContextPath()%>'
	    	    
	    };

		var params = {
				  SBI_EXECUTION_ID: <%= executionId %>
				, LIGHT_NAVIGATOR_DISABLED: 'TRUE'
			};
	
		
		Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
			  baseUrl: url
		    , baseParams: params
		});
		
		function exportChart(exportType) {
		  var svgArr = [],
		   	  top = 0,
		  	  width = 0,
		  	  svg = '';
		  //in case of multiple charts (ie. the master-detail type) redefines the svg object as a global (transforms each single svg in a group tag <g>)
		  for (var c=0; c < chartPanel.chartsArr.length; c++){
			var singleChart = chartPanel.chartsArr[c];
			if (singleChart !== undefined && singleChart !== null){
	          	var singleSvg = singleChart.getSVG();
	          	singleSvg = singleSvg.replace('<svg', '<g transform="translate(0,' + top + ')" ');
	          	singleSvg = singleSvg.replace('</svg>', '</g>');
	
	            top += singleChart.chartHeight;
	            width = Math.max(width, singleChart.chartWidth);
	
	            svgArr.push(singleSvg);
	         }
		  }
		  //defines the global svg
          svg = '<svg height="'+ top +'" width="' + width + '" version="1.1" xmlns="http://www.w3.org/2000/svg">';
          for (var s=0; s < svgArr.length; s++){
        	  svg += svgArr[s];
          }
          svg += '</svg>';

		  //var svg = chartPanel.chart.getSVG();
          params.type = exportType;
	  	  urlExporter = Sbi.config.serviceRegistry.getServiceUrl({serviceName: 'EXPORT_HIGHCHART_ACTION'
																 , baseParams:params
																   });
          Ext.DomHelper.useDom = true; // need to use dom because otherwise an html string is composed as a string concatenation,
          // but, if a value contains a " character, then the html produced is not correct!!!
          // See source of DomHelper.append and DomHelper.overwrite methods
          // Must use DomHelper.append method, since DomHelper.overwrite use HTML fragments in any case.
          var dh = Ext.DomHelper;

          var form = document.getElementById('export-chart-form');
          if (form === undefined || form === null) {
	          var form = dh.append(Ext.getBody(), { // creating the hidden form
	          id: 'export-chart-form'
	          , tag: 'form'
	          , method: 'post'
	          , cls: 'export-form'
	          });
	          
	          dh.append(form, {					// creating the hidden input in form
					tag: 'input'
					, type: 'hidden'
					, name: 'svg'
					, value: ''  // do not put value now since DomHelper.overwrite does not work properly!!
				});
          }          
          // putting the chart data into hidden input
          //form.elements[0].value = Ext.encode(svg);       
          debugger;
          form.elements[0].value = svg;
          form.action = urlExporter;
          form.target = '_blank'; // result into a new browser tab
          form.submit();		  
		}

		Ext.onReady(function() { 	
			Ext.QuickTips.init();		
			
			var config = <%=templateString%>;
			config.dsId = <%=dsId%>;
			config.dsLabel = "<%=dsLabel%>";
			config.dsTypeCd = "<%=dsTypeCd%>";
			config.dsPars =  <%=dsPars%>;
			config.dsTransformerType = "<%=dsTransformerType%>";
			config.divId = "<%=divId%>";
			config.docLabel ="<%=docLabel%>";
			config.theme = "<%=theme%>";
			config.numCharts = <%=numCharts%>;

			if (config.chart && config.chart.subType && config.chart.subType === 'MasterDetail') {
				chartPanel = new Sbi.engines.chart.MasterDetailChartPanel({'chartConfig':config});
			}else{
				chartPanel = new Sbi.engines.chart.HighchartsPanel({'chartConfig':config});
			}
			
			var viewport = new Ext.Viewport({
				layout: 'border'
				, items: [
				    {
				       region: 'center',
				       layout: 'fit',
				       items: [chartPanel]
				    }
				]
	
			});
		});
		
	</script>
	
	<%if (subType != null && subType.equalsIgnoreCase("MasterDetail")) {%>
		<div id="<%=divId%>__detail" style="height:<%=divHeightDetail%>; width:<%=divWidth%>; float:left;"></div>
		<div id="<%=divId%>__master" style="height:<%=divHeightMaster%>; width:<%=divWidth%>; float:left;"></div>
	<% }else{
		  for (int i=0; i<numCharts; i++ ) { %>
			<div id="<%=divId%>__<%=i%>" style="height:<%=divHeight%>; width:<%=divWidth%>; float:left;"></div>
	<%	  }
	   } %>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>