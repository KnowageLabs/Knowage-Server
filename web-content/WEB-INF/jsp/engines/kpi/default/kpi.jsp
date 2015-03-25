<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ include file="/WEB-INF/jsp/engines/kpi/default/kpiinclusions/kpiDefaultHeaderForSpagoBI.jsp"%>

<%	//START ADDING TITLE AND SUBTITLE
	String title = (String)sbModuleResponse.getAttribute("title");
	String subTitle = (String)sbModuleResponse.getAttribute("subName");
	if (title!=null){
		%>
		<div class="kpi_title_section"><%=title%></div>
	<%}
	if (subTitle!=null){%>
		<div class="kpi_subtitle_section"><%=subTitle%></div>
	<%}
	//END ADDING TITLE AND SUBTITLE

	String metadata_publisher_Name =(String)sbModuleResponse.getAttribute("metadata_publisher_Name");
	String trend_publisher_Name =(String)sbModuleResponse.getAttribute("trend_publisher_Name");
	
	List kpiRBlocks =(List)sbModuleResponse.getAttribute("kpiRBlocks");
	KpiLineVisibilityOptions options = new KpiLineVisibilityOptions();
	
	//START creating resources list
	if(!kpiRBlocks.isEmpty()){
		Iterator blocksIt = kpiRBlocks.iterator();
		while(blocksIt.hasNext()){
			KpiResourceBlock block = (KpiResourceBlock) blocksIt.next();
			if(block.getR()!=null){
				resources.add( block.getR());
			}
		}
	}
	//END creating resources list
	%>
	
	<%@ include file="/WEB-INF/jsp/engines/kpi/default/kpiinclusions/kpiScriptsForTree.jsp"%>	
	<%@ include file="/WEB-INF/jsp/engines/kpi/default/kpiinclusions/kpiAddItemForTree.jsp"%>		
		
    <%//START CONSTRUCTING TREE FOR EACH RESOURCE
	if(!kpiRBlocks.isEmpty()){
		Iterator blocksIt = kpiRBlocks.iterator();%>
		<p> 
		<% if(options.getWeighted_values().booleanValue()){%>
			<div class="kpi_note_section">Weighted Values</div>
	    <%}%>
		</p>
		    
		<%while(blocksIt.hasNext()){%>
			<%@ include file="/WEB-INF/jsp/engines/kpi/default/kpiinclusions/kpiAddBlockForTree.jsp"%>		
		<%}			
	}//END CONSTRUCTING TREE FOR EACH RESOURCE	
	
	// put in session the KpiResourceBlocks, title and subtitle for the PDF creation
	if(kpiRBlocks!=null){
		session.setAttribute("KPI_BLOCK",kpiRBlocks);
		session.setAttribute("TITLE",title);
		session.setAttribute("SUBTITLE",subTitle);
	}
%>		
<%@ include file="/WEB-INF/jsp/engines/kpi/default/kpiinclusions/kpiDefaultFooterForSpagoBI.jsp"%>
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>		

		