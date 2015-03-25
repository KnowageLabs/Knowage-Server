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
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="java.util.Calendar"%>
<%@page import="it.eng.spagobi.tools.datasource.bo.DataSource"%>
<%@page import="it.eng.spagobi.monitoring.dao.AuditManager"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>

 <%
	    String objid = (String)aServiceResponse.getAttribute("OBJECT_ID");
	    String mess = (String)aServiceResponse.getAttribute("MESSAGEDET");	
	    boolean alreadyVoted = false ;
	    if (mess.equals("DOCUMENT_RATE")) alreadyVoted = true ;
	    String msg = "DOCUMENT_RATE";
	    Map formUrlPars = new HashMap();
		String ratingForm = GeneralUtilities.getSpagoBIProfileBaseUrl(userUniqueIdentifier)+"&ACTION_NAME=RATING_ACTION";	
	    String starUrl = urlBuilder.getResourceLinkByTheme(request, "/img/star.jpg", currTheme);
	    String halfStarUrl = urlBuilder.getResourceLinkByTheme(request, "/img/halfStar.jpg", currTheme);
	    String smileUrl = urlBuilder.getResourceLinkByTheme(request, "/img/smile.gif", currTheme);
	    BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectById(new Integer(objid));
	    Double rating = DAOFactory.getBIObjectRatingDAO().calculateBIObjectRating(obj);
	   
%>

	<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage' 
			    style='vertical-align:middle;padding-left:5px;font-size: 13px;font-weight:600;background:#e0e1e6;font-family: Arial,Verdana,Geneva,Helvetica,sans-serif;color: #074B88;'>
				<spagobi:message key = "metadata.currentRate" /> 
			</td>
		</tr>
	</table>
	<div id='rating1' class='div_background' style='text-align:center;'>
	
	<% for (int k= 0 ; k < rating.intValue(); k++ ){%>
	
	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
	
	<%}
	Double wholePart = new Double (rating.intValue());
	double rest = rating.doubleValue()- wholePart.doubleValue();
	if ((0.3 < rest) && (rest < 0.7)){
	%>
	<a><img width="22px" height="22px" src='<%= halfStarUrl%>' /></a>
	<%}
	else if (rest > 0.7){
	%>
	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
	<%} %>
	</div>
<br>

<form method='POST' action='<%=ratingForm%>' id='ratingForm' name='ratingForm' >
<% if (!alreadyVoted){ %>
<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage' 
			    style='vertical-align:middle;padding-left:5px;font-size: 13px;font-weight:600;background:#e0e1e6;font-family: Arial,Verdana,Geneva,Helvetica,sans-serif;color: #074B88;'>
				<spagobi:message key = "metadata.vote"  /> 
			</td>
		</tr>
	</table>
	

	<div id='rating' class='div_background' style='padding-top:5px;padding-left:5px;'>
	<div class='div_form_field'>
			<input name='RATING' id='rating1' type='radio' value='1'>&nbsp; <a><img width="22px" height="22px" src='<%= starUrl%>' /></a> <br>
			<input name='RATING' id='rating2' type='radio' value='2'>&nbsp; <a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a><br>
			<input name='RATING' id='rating3' type='radio' value='3'>&nbsp; <a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a><br>
			<input name='RATING' id='rating4' type='radio' value='4'>&nbsp; <a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a><br>
			<input name='RATING' id='rating5' type='radio' value='5'>&nbsp; <a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
																	<a><img width="22px" height="22px" src='<%= starUrl%>' /></a>
	</div>	
					
<input  type='hidden' name='voting' value='true'/>		
<input type='hidden' value='<%=objid%>' name='OBJECT_ID' />
<input type='hidden' value='<%=msg%>' name='MESSAGEDET' />
</div>	
<% } else { %>
	<input type='hidden' value='<%=objid%>' name='OBJECT_ID' />
	<input  type='hidden' name='voting' value='false'/>
	<table width="100%" cellspacing="0" border="0" class='header-table-portlet-section'>		
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section-noimage' 
			    style='vertical-align:middle;padding-left:5px;font-size: 13px;font-weight:600;background:#e0e1e6;font-family: Arial,Verdana,Geneva,Helvetica,sans-serif;color: #074B88;'>
				<spagobi:message key = "metadata.ThankYou" /> 
			</td>
		</tr>
	</table>
	<div  style='text-align:center;'>
	<a><img  src='<%= smileUrl%>' /></a>
	</div>
	<a href='<%=GeneralUtilities.getSpagoBIProfileBaseUrl(userUniqueIdentifier)%>&ACTION_NAME=RATING_ACTION&MESSAGEDET=GOTO_DOCUMENT_RATE&OBJECT_ID=<%=objid%>'><spagobi:message key = "metadata.changeVote" /></a>
<% } %>

<script>
// Watch out for the name of this function, if you change it you need to change it in header.jsp also
	function saveDL() {	
      	var objid = document.ratingForm.OBJECT_ID.value ; 
		var rating ;
		var voting = document.ratingForm.voting.value ; 
		if (voting == 'true'){
			if( document.ratingForm.RATING[0].checked ) rating = document.ratingForm.RATING[0].value ; 
			else if( document.ratingForm.RATING[1].checked ) rating = document.ratingForm.RATING[1].value ;
			else if( document.ratingForm.RATING[2].checked ) rating = document.ratingForm.RATING[2].value ;
			else if( document.ratingForm.RATING[3].checked ) rating = document.ratingForm.RATING[3].value ;
			else if( document.ratingForm.RATING[4].checked ) rating = document.ratingForm.RATING[4].value ;		 	  	  
	  	    if (objid != null && rating != null) {
	  	    	document.getElementById('ratingForm').submit();
		    } 
		}    
	}
</script>	
</form>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>