<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
 <%@page import="java.util.ArrayList"%>
 

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="java.util.List"%>
<%@page import="it.eng.spagobi.commons.utilities.PortletUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("LovLookupAjaxModule");
    // get message info 
    String msgInfo = (String)moduleResponse.getAttribute(SpagoBIConstants.MESSAGE_INFO); 
    // if there's a message info show only it
    if(msgInfo!=null) {
%>

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

	<br/><br/>
	<center>
		<span class='portlet-form-field-label'><%=msgInfo%></span>
	</center>
	<br/><br/>

<%
    } else {
	    // get parameter field name
	    String parameterFieldName = (String)moduleResponse.getAttribute(SpagoBIConstants.PARAMETER_FIELD_NAME); 
	    // get value column name
	    String valColName = (String)moduleResponse.getAttribute(SpagoBIConstants.VALUE_COLUMN_NAME); 
	    // get rows
		List rows = moduleResponse.getAttributeAsList("PAGED_LIST.ROWS.ROW");
		SourceBean listConfig = (SourceBean)moduleResponse.getAttribute("CONFIG");
		
		// get visible columns
		List columns = new ArrayList();
		List configuredColumns = listConfig.getAttributeAsList("COLUMNS.COLUMN");
		for (int i = 0; i < configuredColumns.size(); i++) {
			String hidden = (String)((SourceBean) configuredColumns.get(i)).getAttribute("HIDDEN");
			if (hidden == null || hidden.trim().equalsIgnoreCase("FALSE"))
				columns.add((SourceBean) configuredColumns.get(i));
		}
%>

	<table>
		<thead>
			<tr>
				<!-- for each column design header -->
				<%
				for(int i = 0; i < columns.size(); i++) {
					String nameColumn = (String) ((SourceBean) columns.get(i)).getAttribute("NAME");
					String labelColumnCode = (String) ((SourceBean) columns.get(i)).getAttribute("LABEL");
					String labelColumn = "";
					if (labelColumnCode != null) 
						labelColumn = PortletUtilities.getMessage(labelColumnCode, "messages");
					else labelColumn = nameColumn;
					// if an horizontal-align is specified it is considered, otherwise the defualt is align='left'
					String align = (String) ((SourceBean) columns.get(i)).getAttribute("horizontal-align");
					if (align == null || align.trim().equals("")) 
						align = "left";
				%>
				<TH class='portlet-section-header' valign='center' align='<%=align%>'><%=labelColumn%></TH>
				<%
				} 	
				%>
				<!-- add check column header -->
				<TH class='portlet-section-header' >&nbsp;</TH>
			</tr>
		</thead>
		<tbody>
			<!-- for each row design table row -->
			<%
			String rowvalue = "";
			boolean alternate = false;
			for(int i = 0; i < rows.size(); i++) {
				rowvalue = "";
				SourceBean row = (SourceBean) rows.get(i);
				String rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
                alternate = !alternate;    
            %>
            <tr>
            <%
				for (int j = 0; j < columns.size(); j++) {
					String nameColumn = (String) ((SourceBean) columns.get(j)).getAttribute("NAME");
					Object fieldObject = row.getAttribute(nameColumn);
					String field = null;
					if (fieldObject != null) {
						field = fieldObject.toString();
						// set value of the row
						if(nameColumn.equalsIgnoreCase(valColName)) {
							rowvalue = field;
						}
					}
					else { field = "&nbsp;"; }
					// if an horizontal-align is specified it is considered, otherwise the defualt is align='left'
					String aligncell = (String) ((SourceBean) columns.get(j)).getAttribute("horizontal-align");
					if (aligncell == null || aligncell.trim().equals("")) 
						aligncell = "left";
			%>
				<td class='<%=rowClass%>' align='<%=aligncell%>' valign='top' ><%=field%></td>
			<%
				}
            %>
				<td width='20' class='<%=rowClass%>'>
					<input id='rowcheck' name='rowcheck' value='<%=rowvalue%>' type='checkbox'>
				</td>
			<%
			}
			%>
			</tr>
		</tbody>
	</table>

<%
	} 
%>




