<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject,
				 it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter,				 
				 it.eng.spagobi.commons.dao.DAOFactory,			
				 java.util.List,java.util.Map,java.util.HashMap,			 
				 it.eng.spagobi.commons.bo.Domain,
				 java.util.Iterator,
				 it.eng.spagobi.engines.config.bo.Engine,			
				 it.eng.spago.base.SourceBean,			
				 java.util.Date"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO"%>
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
<form method='POST' action='<%=insertNotesForm%>' id='insertNotesForm' name='insertNotesForm' >

<% if (notes != null && !notes.equals("SpagoBIError:Error")) {%>	
<% if(conflict!= null && conflict.equals("true")) {%>	
<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>	
	<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage' 
			    style='vertical-align:middle;padding-left:5px;font-size: 13px;font-weight:600;background:#e0e1e6;font-family: Arial,Verdana,Geneva,Helvetica,sans-serif;color: #074B88;'>
				<spagobi:message key = "sbi.execution.notes.notesConflict"  /> 
			</td>
		</tr>
	</table>	
<% } %>	
	<spagobi:error/>
	<div id= "notes"></div> 
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
<input type='hidden' value='<%=StringEscapeUtils.escapeHtml(notes)%>' name='OLD_NOTES' />	
<input type='hidden' value='<%=objid%>' name='OBJECT_ID' />
<input type='hidden' value='<%=msg%>' name='MESSAGEDET' />
<input type='hidden' value='<%=execIdentifier%>' name='execIdentifier' />
<% } else {%>	
  <table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>	
	<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage' 
			    style='vertical-align:middle;padding-left:5px;font-size: 13px;font-weight:600;background:#e0e1e6;font-family: Arial,Verdana,Geneva,Helvetica,sans-serif;color: #074B88;'>
				<spagobi:message key = "sbi.execution.notes.notesLoadError"  /> 
			</td>
		</tr>
	</table>
<% } %>	

</form>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>