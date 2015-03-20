<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>

<%
		SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("CheckLinksModule"); 
		String objId = (String)moduleResponse.getAttribute("SUBJECT_ID");
		BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(new Integer(objId));
		String objLabel = obj.getLabel();
		String objName = obj.getName();
		String descr = obj.getDescription();	
	%>
	<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-sub-row-portlet-section'>
			<td class='header-title-column-portlet-section-no-buttons'>
				<spagobi:message key = "sbi.detailbiobj.DocLinked"  />  
			</td>
			
		</tr>
	</table>	
	
	<table style='width:100%;margin-top:1px' id = "docTable" >
	<tr>
	<td class='portlet-section-header' style='text-align:left'>
				<spagobi:message key = "SBISet.ListDataSet.columnLabel"  />   
			</td>
	<td class='portlet-section-header' style='text-align:left'>
		<spagobi:message key = "SBISet.ListDataSet.columnName"  />  
			</td>
	<td class='portlet-section-header' style='text-align:left'>		
			<spagobi:message key = "SBISet.ListDataSet.columnDescr"  />  
			</td>
	</tr>	
	<tr class='portlet-font'>
		 			<td class='portlet-section-body' style='vertical-align:left;text-align:left;'>
				    	<%= objLabel%>
				    	</td>
				   	<td class='portlet-section-body' style='vertical-align:left;text-align:left;'>	
					<%= objName%>		   				
				   	</td>	
				    <td class='portlet-section-body' style='vertical-align:left;text-align:left;'>	
					<%= descr%>			   				
				   	</td>				
	</tr>		
	</table> 

<spagobi:checkbox moduleName="CheckLinksModule" filter="enabled"/>
