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


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ page
	import="it.eng.spagobi.analiticalmodel.document.bo.BIObject,
				 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter,				 
				 it.eng.spagobi.commons.dao.DAOFactory,			
				 java.util.List,java.util.Map,java.util.HashMap,			 
				 it.eng.spagobi.commons.bo.Domain,
				 java.util.Iterator,
				 it.eng.spagobi.engines.config.bo.Engine,			
				 it.eng.spago.base.SourceBean,			
				 java.util.Date"%>
<%@page
	import="it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.ObjNote"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="it.eng.spagobi.tools.datasource.bo.DataSource"%>
<%@page import="it.eng.spagobi.monitoring.dao.AuditManager"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="sun.misc.BASE64Decoder"%>
<%@page import="java.io.IOException"%>




<%
	    String objid = (String)aServiceResponse.getAttribute("OBJECT_ID");
	    String mess = (String)aServiceResponse.getAttribute("MESSAGEDET");	
	    String notes = (String)aServiceResponse.getAttribute("notes");
	    String notesEditor = "";
	    try {
	   		 notesEditor = new String(new BASE64Decoder().decodeBuffer(notes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    
	    String execIdentifier = (String)aServiceResponse.getAttribute("execIdentifier");

		
	    String conflict = (String)aServiceResponse.getAttribute("NOTES_CONFLICT");
	    String msg = "INSERT_NOTES";
		String insertNotesForm = GeneralUtilities.getSpagoBIProfileBaseUrl(userId)+"&ACTION_NAME=INSERT_NOTES_ACTION";	
%>
<form method='POST' action='<%=insertNotesForm%>' id='insertNotesForm'
	name='insertNotesForm'>

	<% if (notes != null && !notes.equals("SpagoBIError:Error")) {%>
	<% if(conflict!= null && conflict.equals("true")) {%>
	<table width="100%" cellspacing="0" border="0"
		class='header-table-portlet-section'>
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage'
				style='vertical-align: middle; padding-left: 5px; font-size: 13px; font-weight: 600; background: #e0e1e6; font-family: Arial, Verdana, Geneva, Helvetica, sans-serif; color: #074B88;'>
				<spagobi:message key="sbi.execution.notes.notesConflict" />
			</td>
		</tr>
	</table>
	<% } %>
	<spagobi:error />
	<div id="notes"></div>
	<script>
	var top1 ;
Ext.onReady(function(){

    Ext.QuickTips.init();
    
    top1 = new Ext.form.HtmlEditor({
        frame: true,
        value: '<%=StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(notesEditor))%>',
        bodyStyle:'padding:5px 5px 0',
        width:'100%',
	    height: 265,
        renderTo: 'notes',
            id:'notes'        
    });   
     
	});	
	
	function saveNotes() {	
      	var objid = document.insertNotesForm.OBJECT_ID.value ; 
		var note = top1.getValue();
  	    if (objid != null && note != null) {
  	    	document.getElementById('insertNotesForm').submit();
	    } 
	}  
	

</script>
	<input type='hidden' value='<%=StringEscapeUtils.escapeHtml(notes)%>'
		name='OLD_NOTES' /> <input type='hidden' value='<%=objid%>'
		name='OBJECT_ID' /> <input type='hidden' value='<%=msg%>'
		name='MESSAGEDET' /> <input type='hidden' value='<%=execIdentifier%>'
		name='execIdentifier' />
	<% } else {%>
	<table width="100%" cellspacing="0" border="0"
		class='header-table-portlet-section'>
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage'
				style='vertical-align: middle; padding-left: 5px; font-size: 13px; font-weight: 600; background: #e0e1e6; font-family: Arial, Verdana, Geneva, Helvetica, sans-serif; color: #074B88;'>
				<spagobi:message key="sbi.execution.notes.notesLoadError" />
			</td>
		</tr>
	</table>
	<% } %>

</form>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
