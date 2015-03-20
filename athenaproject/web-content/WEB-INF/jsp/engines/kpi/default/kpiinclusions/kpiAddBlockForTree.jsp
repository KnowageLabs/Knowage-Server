<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<% 
//START CONSTRUCTING TREE FOR EACH RESOURCE
	//PERCENTAGE WIDTH OF EVERY COLUMN (SUM MUST BE 100%)
final String MODEL_COL_W = "53";
final String META_COL_W = "4";
final String VAL_COL_W = "9";
final String WEIGHT_COL_W = "5";
final String CHART_OR_IMAGE_COL_W = "22";
final String TREND_COL_W = "3";
final String DOC_COL_W = "2";
final String ALARM_COL_W = "2";
//in case both Chart and image are visible
final String CHART_COL_W = "15";
final String IMG_COL_W = "7";
//css classes
final String table_css_class = "kpi_table";
final String res_css_class = "kpi_resource_section";
final String tr_odd_css_class = "kpi_first_line_section_odd";
final String td_first_line_css_class = "kpi_first_line_td";

		KpiResourceBlock block = (KpiResourceBlock) blocksIt.next();
		options = block.getOptions();
		HashMap parMap = block.getParMap() ;
		KpiLine root = block.getRoot();
		Resource r = null;
		Date d = block.getD();
		if(block.getR()!=null){
			r = block.getR();
		}

		String id = "";
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		if (r!=null){
			
			String resourceName = r.getName();
			Integer resourceId = r.getId();
			%>
			<!-- START DIV containing each resource tree -->
			<div id='<%=resourceName%>' >
			
			<!-- START Table containing a specific resource -->		
			<table class='<%=table_css_class%>' id='KPI_TABLE<%=resourceId%>' >
			<TBODY>
			<% if (options.getDisplay_bullet_chart().booleanValue() && options.getDisplay_threshold_image().booleanValue() ){%>
				 <tr class='<%=res_css_class%>' >
				 	<td colspan='10' id='ext-gen58' >
				 		<spagobi:message key="sbi.kpi.RESOURCE" /><%=resourceName%>
				 	</td>
				 </tr>
			<%}else{%>
				 <tr class='<%=res_css_class%>' >
				 	<td colspan='9' id='ext-gen58' >
				 		<spagobi:message key="sbi.kpi.RESOURCE" /><%=resourceName%>
				 	</td>
				 </tr>
			<%}
			id = "node"+resourceId;
			
		}else{%>
			
			<!-- START Table in case there are no resources -->	
			<table class='<%=table_css_class%>' id='KPI_TABLE' >
			<TBODY>
			<% 
			id = "node1";
		}%>
			
			<!-- START TITLE ROW -->
		 		 <tr class='<%=tr_odd_css_class%>' >
		 		 
		 		 	<!-- START MODEL TITLE COLUMN -->
					<td width='<%=MODEL_COL_W%>%'  class='<%=td_first_line_css_class%>' style='text-align:left;' >
						<%=options.getModel_title()%>
					</td>
					<td width='<%=META_COL_W%>%' >
						<div></div>
					</td>
					<!-- END MODEL TITLE COLUMN -->
					
					<!-- START KPI TITLE COLUMN -->
				<% if(options.getKpi_title()!=null){%>
					<td  width='<%=VAL_COL_W%>%' class='<%=td_first_line_css_class%>' >
						<%=options.getKpi_title()%>
					</td>
				<% }else{ %>
					<td  width='<%=VAL_COL_W%>%' class='<%=td_first_line_css_class%>' >
						<div></div>
					</td>
					<!-- END KPI TITLE COLUMN -->
					
					<!-- START KPI WEIGHT COLUMN -->
				<% } 
				
				if (options.getDisplay_weight().booleanValue() && options.getWeight_title()!=null){ %>
					<td width='<%=WEIGHT_COL_W%>%' class='<%=td_first_line_css_class%>' >
						<%=options.getWeight_title()%>
					</td>
				<% }else{%>
					<td width='<%=WEIGHT_COL_W%>%' class='<%=td_first_line_css_class%>' >
						<div></div>
					</td>
					<!-- END KPI WEIGHT COLUMN -->
					
					<!-- START BULLET CHART AND THRESHOLD IMAGE COLUMN -->
				<% } 
				
				if (options.getDisplay_bullet_chart().booleanValue() && options.getDisplay_threshold_image().booleanValue() && options.getBullet_chart_title()!=null && options.getBullet_chart_title()!=null){ %>
					<td width='<%=CHART_COL_W%>%' class='<%=td_first_line_css_class%>' style='text-align:center;' >
						<%=options.getBullet_chart_title()%>
					</td>
					<td width='<%=IMG_COL_W%>%' class='<%=td_first_line_css_class%>' >
						<%=(options.getThreshold_image_title()!=null?options.getThreshold_image_title():"")%>
					</td>
				<% }else if(options.getDisplay_bullet_chart().booleanValue()  && options.getBullet_chart_title()!=null){%>
					<td width='<%=CHART_OR_IMAGE_COL_W%>%' class='<%=td_first_line_css_class%>' style='text-align:center;' >
						<%=options.getBullet_chart_title()%>
					</td>
				<% }else if(options.getDisplay_threshold_image().booleanValue()  && options.getThreshold_image_title()!=null){%>
					<td width='<%=CHART_OR_IMAGE_COL_W%>%' class='<%=td_first_line_css_class%>' style='text-align:center;' >
						<%=options.getThreshold_image_title()%>
					</td>
				<% }else{%>
					<td width='<%=CHART_OR_IMAGE_COL_W%>%' class='<%=td_first_line_css_class%>' style='text-align:center;' >
						<div></div>
					</td>
				<% }%>
					<!-- END BULLET CHART AND THRESHOLD IMAGE COLUMN -->
					
				<td width='<%=TREND_COL_W%>%' ><div></div></td>
				<td width='<%=DOC_COL_W%>%' ><div></div></td>
				<td width='<%=ALARM_COL_W%>%' ><div></div></td>
		 </tr>
		 <!-- END TITLE ROW -->
		
		<% StringBuffer _htmlStream = new StringBuffer();
		   ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
		   String parsToDetailDocs = "";
		   if(instance!=null && instance.getBIObject()!=null){
		   List pars = instance.getBIObject().getBiObjectParameters();			
			if(pars!=null && !pars.isEmpty()){
				Iterator ite=pars.iterator();
				while(ite.hasNext()){
					BIObjectParameter p = (BIObjectParameter)ite.next();
					String url = p.getParameterUrlName();
					String value = p.getParameterValuesAsString();
					parsToDetailDocs += url+"="+value+"&";
				}		
			}
		   }
		   _htmlStream = addItemForTree(id ,instance,userId,0,Boolean.FALSE,request, root,_htmlStream,options,currTheme,parMap,r,d,metadata_publisher_Name,trend_publisher_Name,parsToDetailDocs);%>
		   
		<%= _htmlStream%>
			</TBODY>
			</TABLE>
			<!-- END Table containing a specific resource -->	
		<%if (r!=null){	%>
			</div>
			<!-- END DIV containing each resource tree -->
		<%}
//END CONSTRUCTING TREE FOR EACH RESOURCE
			
%>			