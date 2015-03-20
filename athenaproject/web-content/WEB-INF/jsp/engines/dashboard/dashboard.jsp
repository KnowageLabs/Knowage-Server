<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ page import="java.util.Set" %>
<%@page import="java.net.URLEncoder"%>

<%
   SourceBean sbModuleResponse = (SourceBean) aServiceResponse.getAttribute("ExecuteBIObjectModule");
   ExecutionInstance instanceO = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
 
   String execContext = instanceO.getExecutionModality();
   
   Integer executionAuditId_dash = null;
   String crossNavigationUrl = "";
   if (execContext == null || !execContext.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION)){%>
  		<%@ include file="/WEB-INF/jsp/analiticalmodel/execution/header.jsp"%>
		<%		
		executionAuditId_dash = executionAuditId;
		Map crossNavigationParameters = new HashMap();
	//	crossNavigationParameters.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
		crossNavigationParameters.put(ObjectsTreeConstants.ACTION, SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);
		crossNavigationParameters.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.EXEC_CROSS_NAVIGATION);
		crossNavigationParameters.put("EXECUTION_FLOW_ID", executionFlowId);
		crossNavigationParameters.put("SOURCE_EXECUTION_ID", uuid);
		crossNavigationParameters.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "TRUE");
		crossNavigationUrl = urlBuilder.getUrl(request, crossNavigationParameters);%>
		
		<form id="crossNavigationForm<%= uuid %>" method="post" action="<%= crossNavigationUrl %>" style="display:none;">
		    <input type="hidden" id="targetDocumentLabel<%= uuid %>" name="<%= ObjectsTreeConstants.OBJECT_LABEL %>" value="" />  
			<input type="hidden" id="targetDocumentParameters<%= uuid %>" name="<%= ObjectsTreeConstants.PARAMETERS %>" value="" />
		</form>
		
	
		<script>
			function execCrossNavigation(windowName, label, parameters) {
				if(this.uiType === 'ext'){
					sendMessage({'label': label, parameters: parameters},'crossnavigation');
				} else {
					var uuid = "<%=uuid%>";
					document.getElementById('targetDocumentLabel' + uuid).value = label;
					document.getElementById('targetDocumentParameters' + uuid).value = parameters;
					document.getElementById('crossNavigationForm' + uuid).submit();
				}
	
			}
		
		</script>
		<%-- end cross navigation scripts --%>
<%	} 
	else{
		ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
		AuditManager auditManager = AuditManager.getInstance();
		executionAuditId_dash = auditManager.insertAudit(instance.getBIObject(), null, userProfile, instance.getExecutionRole(), instance.getExecutionModality());
		String uuid = instanceO.getExecutionId(); 
   }
   String uuidO=instanceO.getExecutionId();
   %>
   <%-- div with wait while loading message --%>
   <div id="divLoadingMessage<%= uuidO %>" style="display: inline;">
   <img	src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/analiticalmodel/loading.gif", currTheme)%>' />
   <spagobi:message key='sbi.execution.pleaseWait' /></div>
   
   <%
    String movie = ChannelUtilities.getSpagoBIContextName(request);
    String relMovie = (String)sbModuleResponse.getAttribute("movie");
    if(relMovie.startsWith("/"))
    	movie = movie + relMovie;
    else 
    	movie = movie + "/" + relMovie;
	String width = (String)sbModuleResponse.getAttribute("width");
	String height = (String)sbModuleResponse.getAttribute("height");
	String dataurl = ChannelUtilities.getSpagoBIContextName(request);
	String dataurlRel = (String)sbModuleResponse.getAttribute("dataurl");
	if(dataurlRel.startsWith("/"))
		dataurl = dataurl + dataurlRel;
	else dataurl = dataurl + "/" + dataurlRel;
	
	Map confParameters = (Map)sbModuleResponse.getAttribute("confParameters");
	Map dataParameters = (Map)sbModuleResponse.getAttribute("dataParameters");
	Map drillParameters = (Map)sbModuleResponse.getAttribute("drillParameters");
	
	dataParameters.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
	// adding parameters for AUDIT updating
	if (executionAuditId_dash != null) {
		dataParameters.put(AuditManager.AUDIT_ID, executionAuditId_dash.toString());
	}
	
	// start to create the calling url
	// put the two dimension parameter
	movie += "?paramHeight="+height+"&paramWidth="+width; 
	// create the dataurl string
	if (dataurl.contains("?")) dataurl += "&";
	else dataurl += "?";
	// for each data parameter append to the dataurl 
	Set dataKeys = dataParameters.keySet();
	Iterator iterDataKeys = dataKeys.iterator();
	while(iterDataKeys.hasNext()) {
		String name = (String)iterDataKeys.next();
		String value = (String)dataParameters.get(name);
	    dataurl += name + "=" + value + "&"; 
	}
	
    // for each conf parameter append to the movie url  
	Set confKeys = confParameters.keySet();
	Iterator iterConfKeys = confKeys.iterator();
	while(iterConfKeys.hasNext()) {
		String name = (String)iterConfKeys.next();
		if (!name.startsWith("dash__")){
			String value = (String)confParameters.get(name);
			movie += "&" + name + "=" + value;
		}
	}
	
	// for drill parameter append to the movie url  
	Set drillKeys = drillParameters.keySet();
	Iterator iterDrillKeys = drillKeys.iterator();
	while(iterDrillKeys.hasNext()) {
		String name = (String)iterDrillKeys.next();	
		String value = (String)drillParameters.get(name);
		movie += "&" + name + "=" + value; 
	}
	
    // append to the calling url the dataurl
    movie += "&dataurl=" + URLEncoder.encode(dataurl);
  
    
	//defines dynamic parameters for multichart management (ie. recNum)
	int numCharts = (confParameters.get("numCharts")==null)? 1:Integer.valueOf((String)confParameters.get("numCharts")).intValue();
	int numChartsForRow = (confParameters.get("numChartsForRow")==null)? numCharts:Integer.valueOf((String)confParameters.get("numChartsForRow")).intValue();
	int contChartsForRow =  numChartsForRow;
	String multichart = (confParameters.get("multichart")==null)?"false":(String)confParameters.get("multichart");
	String orientation = (confParameters.get("orientation_multichart")==null)?"vertical":(String)confParameters.get("orientation_multichart");
	String legend = (confParameters.get("legend")==null)?"true":(String)confParameters.get("legend");
	//String displayTitle = (confParameters.get("displayTitleBar")==null)?"false":(String)confParameters.get("displayTitleBar");
	String title =(confParameters.get("title")==null)?"":(String)confParameters.get("title");	
	String styleTitle = "text-align:center;font-weight:bold;";
	styleTitle = styleTitle + "color:" + (confParameters.get("colorTitle")==null?"":(String)confParameters.get("colorTitle")) + ";";
	styleTitle = styleTitle + "fontSize:" + (confParameters.get("sizeTitle")==null?"":(String)confParameters.get("sizeTitle")) + ";";
	styleTitle = styleTitle + "fontStyle:" + (confParameters.get("fontTitle")==null?"":(String)confParameters.get("fontTitle")) + ";";
	//defines radius for get dynamic height : only the last chart with the legend uses the total height; the others are riduced.
	double radiusByWidth = (Integer.valueOf(width).intValue()-2*10)/2;
	double radiusByHeight = (Integer.valueOf(height).intValue()-2*10)/(1+(1/4));
	double radius = 0;
	String dinHeight = height;
	
    if (radiusByWidth < radiusByHeight) {
		radius = radiusByWidth;
    } else {
		radius = radiusByHeight;
	}
   
	if (orientation.equalsIgnoreCase("horizontal")){
		dinHeight = String.valueOf(Integer.valueOf(height).intValue()-radius+20);
	}
	%>
	<!-- <br>  -->
	<div align="center" ><span style="<%=styleTitle%>"><%=title%></span></div>
	<br>
		<table align="center" >
		  <tr>
	<%	 
	//}
	for (int idx = 0; idx < numCharts; idx++){
		//add the single chart configuration (lowValue, minValue, ...)
		Map singleDashConf = (Map)confParameters.get("dash__" + idx);
		if (singleDashConf != null){
			Set singleConfKeys = singleDashConf.keySet();
			Iterator iterSingleConfKeys = singleConfKeys.iterator();
			while(iterSingleConfKeys.hasNext()) {
				String name = (String)iterSingleConfKeys.next();
				String value = (String)singleDashConf.get(name);
				if (movie.indexOf(name) != -1){
					//replace the old parameter value
					int posStart = movie.indexOf(name);
					int posEnd = movie.indexOf("&", posStart);
					String oldParam = movie.substring(posStart, posEnd);
					String newParam = name + "=" + value;		
					movie = movie.replace(oldParam, newParam);
				}else{
					//add the new parameter value
					movie += "&" + name + "=" + value;
				}
			}
		}
		if (idx==0){
		 	movie += "&recNumber="+String.valueOf(idx);
		 	/*if it's a multichart type and it's required the legend  
		 	  it's shows only on the last chart while if is required the title it's shows only on the first one*/
		 	if (multichart.equalsIgnoreCase("true")) {
				if (legend.equalsIgnoreCase("true")){
			 		movie = movie.replace("&legend=true","&legend=false");
			 		dinHeight = String.valueOf(Integer.valueOf(height).intValue()-radius+20);
				}
		 	}
		 	/*
		 	if (displayTitle.equalsIgnoreCase("true")){
		 		int heightMargin = 40;
		 		dinHeight = String.valueOf(Double.valueOf(dinHeight).intValue()+heightMargin);
		 		movie +="&winHeightMargin="+heightMargin;
			}*/
		}
		else{ 
			movie = movie.replace("&recNumber="+(idx-1),"&recNumber="+idx);
			if (multichart.equalsIgnoreCase("true")){
				if (legend.equalsIgnoreCase("true")){
					if (idx < (numCharts-1)){
				 		movie = movie.replace("&legend=true","&legend=false");
				 		dinHeight = String.valueOf(Integer.valueOf(height).intValue()-radius+20);
					}
					else{
						movie = movie.replace("&legend=false","&legend=true");
					//	dinHeight = height;
						 dinHeight = String.valueOf(Integer.valueOf(height).intValue()+20);
					}
					/*
					if (displayTitle.equalsIgnoreCase("true")){
						movie = movie.replace("&displayTitleBar=true","&displayTitleBar=false");
					}*/
				}
		 	}
		}
 		if (orientation.equalsIgnoreCase("horizontal")){%>
			<td> 
	<%  } 

// HTML CODE FOR THE FLASH COMPONENT %> 
 <div align="center" id="swfDiv"> 
       <object  classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" 
                codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" 
                type="application/x-shockwave-flash"
                data="<%=movie%>"  
                width="<%=width%>" 
                height="<%=dinHeight%>" >
       	  <param name="movie" value="<%=movie%>">
       	  <param name="quality" value="high">
       	  <param name="scale" value="noscale">
       	  <param name="salign" value="LT">
       	  <param name="menu" value="false">
       	  <param name="wmode" value="transparent">
        <EMBED  src="<%=movie%>" 
                quality=high 
                width="<%=width%>" 
                height="<%=dinHeight%>" 
                wmode="transparent" 
   			 TYPE="application/x-shockwave-flash" PLUGINSPAGE="http://www.macromedia.com/go/getflashplayer">
   		</EMBED>
	</object>    
</div>  


<%-- 
<div align="center">
<script type="text/javascript">
  lz.embed({url: "<%=movie%>", width:"<%=width%>", height:"<%=dinHeight%>"});
</script>
</div>
 --%>
<% if (orientation.equalsIgnoreCase("horizontal")){%>
  </td>  
<%   //checks for new rows
	 if (idx == contChartsForRow -1){
		 contChartsForRow += numChartsForRow; 
%>
		
		</tr>
		<tr>	
<%	 } 
   }
} //for 
if (orientation.equalsIgnoreCase("horizontal")){%>
	</tr>
</table>
<%} %>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>

<%-- when the execution is performed, the please while loading message is hidden --%>
<script type="text/javascript">
document.getElementById('divLoadingMessage<%= uuidO %>').style.display = 'none';
</script>